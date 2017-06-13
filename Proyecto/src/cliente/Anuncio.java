package cliente;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListModel;

public class Anuncio extends Thread{
  private ArrayList usuarios;
  private MulticastSocket cliente;
  private InetAddress grupo;
  private int puerto;
  private String usuario;
  private DefaultListModel contactos;
  
  public Anuncio(ArrayList usuarios,MulticastSocket socket,InetAddress grupo,int puerto,String usuario,DefaultListModel contactos){
    super("Anuncio");
    this.usuarios=usuarios;
    cliente=socket;
    this.grupo=grupo;
    this.puerto=puerto;
    this.usuario=usuario;
    this.contactos=contactos;
  }
  
  public void run(){
    try{
        while(true){
            enviarAnuncio();
            verificarUsuarios();
            sleep(5000);
        }
    }catch(Exception e){
        e.printStackTrace();
    }
  }
  
  public void enviarMensaje(String mensaje){
        DatagramPacket paquete = new DatagramPacket(mensaje.getBytes(),mensaje.length(),grupo,puerto);
        try {
            System.out.println("Enviando: " + mensaje+"  con un TTL= "+cliente.getTimeToLive());
            cliente.send(paquete);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
  }
  
  public void enviarAnuncio(){
        enviarMensaje("<inicio>"+usuario);
  }
  
  public void verificarUsuarios(){
        for (int i = 0; i < contactos.size(); i++) {
            if (!contactos.isEmpty()) {
                String usuario=(String)contactos.getElementAt(i);
                if (usuarios.contains(usuario)){
                    usuarios.remove(usuario);
                }else{
                    System.out.println(usuario+" desconectado");
                    leaveConnection(usuario);
                }
            }
        }
    }
  
    private int findInList (String cadena) {
        for (int i = 0; i < contactos.size(); i++) {
            if (!contactos.isEmpty()) {
                if (((String)contactos.getElementAt(i)).contains(cadena)) {
                    return i;
                }
            }
        }
        return (-1);
    }
    
    public void leaveConnection(String usuario) {
        int index;
        if ((index = findInList(usuario)) != (-1)) {
            contactos.removeElement(usuario);
        }
    }
}
