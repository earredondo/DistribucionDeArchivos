package cliente;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.ArrayList;
import javax.swing.DefaultListModel;

public class hiloEscucha extends Thread{
    
    private MulticastSocket cliente;
    private ArrayList usuarios;
    private String usuario;
    private DefaultListModel contactos;

    public hiloEscucha(MulticastSocket socket,ArrayList usuarios,String usuario, DefaultListModel contactos){
        super("Oyente");   //Nombre del hilo
        cliente=socket;
        this.usuarios=usuarios;
        this.usuario=usuario;
        this.contactos=contactos;
    }

    public void run(){
        byte[] buffer,data;
        DatagramPacket paquete;
        String mensaje;
        while(true){
            try {
                buffer= new byte[512];
                paquete= new DatagramPacket(buffer,buffer.length);
                cliente.receive(paquete);// ya se tiene el datagram packet
                data = paquete.getData(); //aqui no se entienden los datos
                mensaje=new String(data,0,paquete.getLength());
                hacerOperacion(mensaje);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private void hacerOperacion(String mensaje){
        int indice1, indice2;
        String operacion;
        indice1=mensaje.indexOf('<');
        indice2=mensaje.indexOf('>');
        operacion=mensaje.substring(indice1+1, indice2);
        if(operacion.toLowerCase().equals("inicio")){agregarUsuario(mensaje.substring(indice2+1));}
    }

    private void agregarUsuario(String usuario){
        usuarios.add(usuario);
        System.out.println("Usuarios: "+usuarios.size());
        isConnected(usuario);
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
    
    public void isConnected(String usuario) {
        if (findInList(usuario) == (-1)) {
            //contactos.addElement(usuario + " conectado");
            contactos.addElement(usuario);
            System.out.println(usuario+" esta en linea");
        }
    }
}