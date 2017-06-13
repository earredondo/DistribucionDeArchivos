package cliente;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JTextPane;

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
    
    public void enviarMensajePublico(String mensaje){
        enviarMensaje("<msj><"+ ipServidor+">"+mensaje);
    }
    
    public void enviarMensajePrivado(String usuario,String mensaje){
        enviarMensaje("<privado><"+this.ipServidor+"><"+usuario+">"+mensaje);
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