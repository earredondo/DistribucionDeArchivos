package cliente;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    
    public Cliente(DefaultListModel contactos){
        try{
            this.ipServidor = "127.0.0.1";
            this.ipCliente = "127.0.0.1";
            servidor=new ServerSocket(0);
            this.puertoServidor = this.servidor.getLocalPort();
            grupo = null;
            usuarios = new ArrayList();
            this.contactos = contactos;
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
            System.out.println("Enviando: " + mensaje+"  con un TTL= "+clienteMulticast.getTimeToLive());
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
        oyente=new hiloEscucha(clienteMulticast,usuarios,ipServidor, contactos);
        oyente.start();
    }
    
    public void salir(){
        clienteMulticast.close();
    }
    
    public String getId(){
        return this.ipServidor + ":" + this.puertoServidor;
    }
    
    public void enviarArchivo(File archivo){
        try{
            int numClientes = contactos.size();
            long tam = archivo.length();
            byte []buffer = new byte[4096];
            long enviados = 0, totalEnviados = 0;
            FileInputStream fis = new FileInputStream(archivo);
            System.out.println("Servidores conectados: " + numClientes);
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
                    System.out.println("Tam: " + tam + ":" + restantes);
                    dis.writeUTF(archivo.getName());
                    dis.writeInt(i + 1);
                    if(restantes >= 536870912l){dis.writeLong(536870912l);}
                    else{dis.writeLong(restantes);}
                    enviados = 0;
                    while(enviados < 536870912l){ // 500 Megas a cada cliente
                        System.out.println("Enviados: " + enviados);
                        int leidos = fis.read(buffer);
                        if(leidos < 1){break;}
                        dis.write(buffer, 0 , leidos);
                        enviados+=leidos;
                        totalEnviados += leidos;
                    }
                    dis.close();
                    cliente.close();
                }
            }
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    
    public void enviarArchivo(File []archivos){
        this.enviarArchivo(archivos[0]);
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