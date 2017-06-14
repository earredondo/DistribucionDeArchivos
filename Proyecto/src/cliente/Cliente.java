package cliente;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import modelo.Servidor;

public class Cliente {
    private ServerSocket servidor;
    private String ipServidor;
    private int puertoServidor;
    private String ipCliente;
    private int puertoCliente;
    private MulticastSocket clienteMulticast;
    private InetAddress grupo;
    private String ipMulticast;
    private int puertoMulticast;
    private hiloEscucha oyente;
    private ArrayList usuarios;
    private DefaultListModel contactos;
    private Anuncio anuncio;
    private final long FRAGMENT_SIZE;
    
    public Cliente(DefaultListModel contactos){
        this.FRAGMENT_SIZE = 268435456l;
        this.ipServidor = "127.0.0.1";
        this.ipCliente = "127.0.0.1";
        grupo = null;
        usuarios = new ArrayList();
        this.contactos = contactos;
        try{
            servidor=new ServerSocket(0);
            this.puertoServidor = this.servidor.getLocalPort();
            new Servidor(servidor).start();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    
    public void unirAGrupo(String ip,int puerto){
    	try{
            this.puertoMulticast = puerto;
            this.ipMulticast = ip;
            grupo = InetAddress.getByName(this.ipMulticast);
            clienteMulticast = new MulticastSocket(this.puertoMulticast);
            clienteMulticast.joinGroup(grupo);
            clienteMulticast.setTimeToLive(64);
    	}catch(Exception e){
    		e.printStackTrace();
    		System.exit(1);
    	}
    }
    
    public void enviarMensaje(String mensaje){
        DatagramPacket paquete = new DatagramPacket(mensaje.getBytes(),mensaje.length(),grupo,puertoMulticast);
        try {
            //System.out.println("Enviando: " + mensaje+"  con un TTL= "+clienteMulticast.getTimeToLive());
            clienteMulticast.send(paquete);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public void enviarMiIp(){
        anuncio=new Anuncio(usuarios,clienteMulticast,grupo,puertoMulticast,ipServidor + ":" + this.puertoServidor,contactos);
        anuncio.start();
    }
    
    public void escucharMensajes(){
        oyente=new hiloEscucha(clienteMulticast,usuarios,ipServidor + ":" + this.puertoServidor, contactos);
        oyente.start();
    }
    
    public void salir(){
        clienteMulticast.close();
    }
    
    public String getId(){
        return this.ipServidor + ":" + this.puertoServidor;
    }
    
    public void enviarArchivoFragmentado(File archivo){ //Por TCP
        try{
            /* ARCHIVO CON LA INFORMACION DE LOS FRAGMENTOS */
            File meta = new File("src/updates/metadatos/"+archivo.getName());
            
            int numClientes = contactos.size();
            long tam = archivo.length();
            byte []buffer = new byte[4096];
            long enviados = 0, totalEnviados = 0;
            FileInputStream fis = new FileInputStream(archivo);
            FileOutputStream fos = new FileOutputStream(meta);
            for (int i = 0; i < contactos.size(); i++) {    //Para cada cliente conectado
                if (!contactos.isEmpty()) {
                    String datosServidor = (String)contactos.getElementAt(i);
                    System.out.println(datosServidor);
                    String []ipPuerto = datosServidor.split(":");
                    String ip = ipPuerto[0];
                    int puerto = Integer.parseInt(ipPuerto[1]);
                    Socket cliente = new Socket(ip, puerto);
                    DataOutputStream dis = new DataOutputStream(cliente.getOutputStream());
                    if(totalEnviados >= tam){break;}
                    long restantes = tam - totalEnviados;
                    System.out.println("Enviando archivo " + archivo.getName());
                    dis.writeUTF(archivo.getName());
                    dis.writeInt(i + 1);
                    fos.write(((i+1) + ":" +datosServidor + "\n").getBytes());
                    if(restantes >= FRAGMENT_SIZE){dis.writeLong(FRAGMENT_SIZE);}
                    else{dis.writeLong(restantes);}
                    enviados = 0;
                    while(enviados < FRAGMENT_SIZE){
                        int leidos = fis.read(buffer);
                        if(leidos < 1){break;}
                        dis.write(buffer, 0 , leidos);
                        enviados+=leidos;
                        totalEnviados += leidos;
                        int porcentaje = (int) ((totalEnviados * 100)/tam);
                        System.out.print("\rEnviado: " + porcentaje + "%");
                    }
                    dis.close();
                    cliente.close();
                }
            }
            fos.close();
            
            /*Enviar por multicast el descriptor de archivo */
            enviarMensaje("<archivo>");
            enviarArchivo(meta);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    
    public void enviarArchivo(File []archivos){
        this.enviarArchivoFragmentado(archivos[0]);
    }
    
    private void enviarArchivo(File archivo) { //Por multicast
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            String nombre = archivo.getName();
            System.out.println("NOmbre: " + nombre);
            long tamanio = archivo.length();
            String path = archivo.getAbsolutePath();
            DataInputStream dis = new DataInputStream(new FileInputStream(path));
            byte[] buffer = new byte[1024];
            long enviados = 0;
            int n = 0;
            int porcentaje = 0;
            int numeroPaquete = (-1);

            /*Envia tamanio*/
            buffer = ("" + tamanio).getBytes();
            System.out.println(numeroPaquete + ":" + buffer.length);
            Paquete paquete = new Paquete(numeroPaquete++,buffer);
            oos.writeObject(paquete);
            oos.flush();
            byte[] datagrama = baos.toByteArray();
            DatagramPacket p = new DatagramPacket(datagrama,datagrama.length, grupo, puertoMulticast);
            clienteMulticast.send(p);

            /*Envia nombre*/
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            buffer = new byte[1400];
            buffer = nombre.getBytes();
            paquete = new Paquete(numeroPaquete++,buffer);
            oos.writeObject(paquete);
            oos.flush();
            datagrama = baos.toByteArray();
            p=new DatagramPacket(datagrama,datagrama.length, grupo, puertoMulticast);
            clienteMulticast.send(p);

            /*Envia datos*/
            while(enviados < tamanio){
                buffer = new byte[1400];
                baos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(baos);
                n = dis.read(buffer);
                byte[] buffer2 = new byte[n];
                System.arraycopy(buffer, 0, buffer2, 0, n);
                paquete = new Paquete(numeroPaquete++,buffer2);
                oos.writeObject(paquete);
                oos.flush();
                datagrama = baos.toByteArray();
                p = new DatagramPacket(datagrama,datagrama.length, grupo, puertoMulticast);
                clienteMulticast.send(p);
                enviados += n;
                porcentaje = (int)(enviados*100/tamanio);
                System.out.print("\rTransmitido: " + porcentaje+"%");
                Thread.sleep(2);
            }
            dis.close();
            oos.close();
            baos.close();
            System.out.println("\nArchivo enviado: " + numeroPaquete);
        }catch(IOException | InterruptedException ex){
            ex.printStackTrace();
            System.out.println("Ha ocurrido un grave error al enviar archivo: " + ex.getLocalizedMessage());
        }
    }
    
    
    /*public static void main(String[] args){
        Cliente cliente=new Cliente("Edgar");
        cliente.unirAGrupo("230.1.1.1",4000);
        cliente.entrarAChat();
        cliente.escucharMensajes();
        cliente.enviarMensajePublico("Hola :D amigos8|");
        cliente.enviarMensajePrivado("Jose", ":DComo estas? :D");
        while(true);
        //cliente.salir();
    }*/
}