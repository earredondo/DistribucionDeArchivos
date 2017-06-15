package cliente;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.border.Border;
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
    private DefaultListModel archivos;
    private Anuncio anuncio;
    private final long FRAGMENT_SIZE;
    private final int VECES_REPETIDO;
    
    public Cliente(DefaultListModel contactos, DefaultListModel archivos){
        this.FRAGMENT_SIZE = 134217728l;
        this.VECES_REPETIDO = 2;
        this.ipServidor = "10.100.65.48";
        this.puertoServidor = 0;
        this.ipCliente = "127.0.0.1";
        grupo = null;
        usuarios = new ArrayList();
        this.contactos = contactos;
        this.archivos = archivos;
        try{
            servidor=new ServerSocket(this.puertoServidor, 5, InetAddress.getByName(ipServidor));
            this.puertoServidor = this.servidor.getLocalPort();
            new Servidor(servidor).start();
            new MonitorArchivos(this.archivos, this.ipServidor + ":" + this.puertoServidor).start();
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
            int i, j, vecesEnviado = 0;
            long tam = archivo.length();
            byte []buffer = new byte[4096];
            long enviados = 0, totalEnviados = 0;
            FileInputStream fis = new FileInputStream(archivo);
            FileOutputStream fos = new FileOutputStream(meta);
            fos.write(("" + tam + "\n").getBytes());
            /* Barra de progreso */
            JFrame f = new JFrame("Cargando archivo");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Container content = f.getContentPane();
            JProgressBar progressBar = new JProgressBar();
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            Border border = BorderFactory.createTitledBorder("Enviando...");
            progressBar.setBorder(border);
            content.add(progressBar, BorderLayout.NORTH);
            f.setSize(300, 100);
            f.setVisible(true);
            for (i = 0, j = 1; i < contactos.size(); i++, j++) {    //Para cada cliente conectado
                if (!contactos.isEmpty()) {                         //enviar un fragmento
                    String datosServidor = (String)contactos.getElementAt(i);
                    System.out.println(datosServidor);
                    String []ipPuerto = datosServidor.split(":");
                    String ip = ipPuerto[0];
                    int puerto = Integer.parseInt(ipPuerto[1]);
                    Socket cliente = new Socket(ip, puerto);
                    DataOutputStream dis = new DataOutputStream(cliente.getOutputStream());
                    long restantes = tam - totalEnviados;
                    System.out.println("Enviando archivo " + archivo.getName());
                    dis.writeUTF("subir");
                    dis.writeUTF(archivo.getName());
                    dis.writeInt(j);
                    fos.write(((j) + "-" +datosServidor + "\n").getBytes());
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
                        progressBar.setValue(porcentaje);
                    }
                    dis.close();
                    cliente.close();
                    if(totalEnviados >= tam){
                        if(++vecesEnviado == this.VECES_REPETIDO){break;}
                        totalEnviados = 0;
                        j = 0;
                        fis = new FileInputStream(archivo);
                    }
                }
            }
            f.setVisible(false);
            fos.close();
            
            /*Enviar por multicast el descriptor de archivo */
            /*anuncio.pause();
            enviarMensaje("<archivo>");
            enviarArchivo(meta);
            anuncio.resum();*/
            enviarArchivoCompleto(meta);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    
    public void enviarArchivoCompleto(File archivo){
        int i;
        long tam = archivo.length();
        byte []buffer = new byte[4096];
        try{
            FileInputStream fis = null;
            for (i = 0; i < contactos.size(); i++) {    //Para cada cliente conectado
                if (!contactos.isEmpty()) {                         //enviar un fragmento
                    fis = new FileInputStream(archivo);
                    long enviados = 0;
                    String datosServidor = (String)contactos.getElementAt(i);
                    System.out.println(datosServidor);
                    String []ipPuerto = datosServidor.split(":");
                    String ip = ipPuerto[0];
                    int puerto = Integer.parseInt(ipPuerto[1]);
                    Socket cliente = new Socket(ip, puerto);
                    DataOutputStream dis = new DataOutputStream(cliente.getOutputStream());
                    dis.writeUTF("meta");
                    dis.writeUTF(archivo.getName());
                    dis.writeLong(tam);

                    while(enviados < tam){
                        int leidos = fis.read(buffer);
                        if(leidos < 1){break;}
                        dis.write(buffer, 0 , leidos);
                        enviados+=leidos;
                        enviados += leidos;
                        int porcentaje = (int) ((enviados * 100)/tam);
                        System.out.print("\rEnviado: " + porcentaje + "%");
                    }
                    dis.close();
                    cliente.close();
                }
            }
            if(fis!=null){fis.close();}
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
        }catch(IOException | InterruptedException ex){
            ex.printStackTrace();
            System.out.println("Ha ocurrido un grave error al enviar archivo: " + ex.getLocalizedMessage());
        }
    }
    
    public void descargar(String archivo){
        String path = "src/updates/metadatos/" + this.getId() + "/";
        String pathArchivo = "src/updates/archivos/" + archivo;
        File file = new File(path + archivo);
        try{
            DataOutputStream fos=new DataOutputStream(new FileOutputStream(new File(pathArchivo)));
            FileReader f = new FileReader(file);
            BufferedReader br = new BufferedReader(f);
            long tamTotal = Long.parseLong(br.readLine());
            long totalLeidos = 0;
            String cadena;
            String []datosServidor;
            int fragmentoActual = 1;
            while(totalLeidos < tamTotal){
                while((cadena = br.readLine())!=null) {
                    datosServidor = cadena.split("-");
                    if(datosServidor[0].equals("" + fragmentoActual)){
                        datosServidor = datosServidor[1].split(":");
                        Socket cliente = null;
                        try{
                            cliente = new Socket(datosServidor[0], Integer.parseInt(datosServidor[1]));
                        }catch(IOException ioe){
                            System.out.println("El nodo con el fragmento esta desconectado");
                            continue;
                        }
                        DataOutputStream dos = new DataOutputStream(cliente.getOutputStream());
                        DataInputStream dis = new DataInputStream(cliente.getInputStream());
                        dos.writeUTF("descargar");
                        dos.writeUTF(archivo);
                        dos.writeUTF("" + fragmentoActual);
                        long tam = dis.readLong();
                        long leidos = 0;
                        int n;
                        byte []buffer =  new byte[1500];
                        int porcentaje;
                        while(leidos < tam){
                            n=dis.read(buffer);
                            fos.write(buffer, 0, n);
                            leidos+=n;
                            totalLeidos += n;
                            porcentaje=(int)(leidos*100/tam);
                            System.out.print("\rRecibido: "+porcentaje+"%");
                        }
                        dos.close();
                        dis.close();
                        fragmentoActual++;
                        if(totalLeidos >= tamTotal){break;}
                    }
                }
                f  = new FileReader(file);
                br = new BufferedReader(f);
                tamTotal = Long.parseLong(br.readLine());
            }
            fos.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    
    public static void main(String[] args){
        JFrame f = new JFrame("Cargando archivo");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container content = f.getContentPane();
        JProgressBar progressBar = new JProgressBar();
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        Border border = BorderFactory.createTitledBorder("Enviando...");
        progressBar.setBorder(border);
        content.add(progressBar, BorderLayout.NORTH);
        f.setSize(300, 100);
        f.setVisible(true);
        for (int i = 0; i < 100; i++) {
            progressBar.setValue(i);
            try{
                Thread.sleep(10);
            }catch(InterruptedException ie){
            }
        }
    }
}