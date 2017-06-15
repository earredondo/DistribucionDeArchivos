package cliente;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
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
        else if(operacion.toLowerCase().equals("archivo")){recibirArchivo();}
    }

    private void agregarUsuario(String usuario){
        usuarios.add(usuario);
        //System.out.println("Usuarios: "+usuarios.size());
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
    
    private void recibirArchivo() {
        long tamanio = 100000;
        long recibidos = 0;
        int largo = 0;
        //String RUTA = "src/updates/metadatos/";
        String RUTA = crearDirectorio();
        String nombre = RUTA + "archivo.tmp";
        
        try {
            File archivo = new File(nombre);
            RandomAccessFile archivoAleatorio = null;
            for (int i = 0; recibidos < tamanio;) {
                DatagramPacket paquete = new DatagramPacket(new byte[1500],1500);
                cliente.receive(paquete);
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(paquete.getData()));
                Paquete objeto=(Paquete)ois.readObject();
                switch (objeto.getNumero()) {
                    case (-1):
                        tamanio = Long.parseLong(new String(objeto.getDatos(), 0, objeto.getDatos().length));
                        break;
                    case 0:
                        nombre=new String(objeto.getDatos(),0,objeto.getDatos().length);
                        if (archivo.renameTo(new File(RUTA + nombre))) {
                            //System.out.println("Archivo " + nombre + " renombrado exitosamente.");
                        } else {
                            archivo = new File(RUTA + nombre);
                        }
                        try {
                            archivoAleatorio = new RandomAccessFile(archivo, "rw");
                        } catch (FileNotFoundException ex) {
                            throw new FileNotFoundException("Error de archivo: " + ex.getMessage());
                        }
                        break;
                    default:
                        if (recibidos == 0) {
                            largo=objeto.getDatos().length;
                        }
                        try {
                            archivoAleatorio.seek((objeto.getNumero() - 1)*largo);
                            archivoAleatorio.write(objeto.getDatos());
                        } catch (IOException ex) {
                            throw new IOException("Error de E/S: " + ex.getMessage());
                        }
                        recibidos += objeto.getDatos().length;
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException | NumberFormatException ex) {
            System.out.println("Ha ocurrido un grave error: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        System.out.println("Archivo recibido");
    }
    
    private String crearDirectorio(){
        File theDir = new File("src/updates/metadatos/" + this.usuario + "/");

        // if the directory does not exist, create it
        if (!theDir.exists()) {
            //System.out.println("creating directory: " + theDir.getName());
            boolean result = false;

            try{
                theDir.mkdir();
                result = true;
            } 
            catch(SecurityException se){
                //handle it
            }        
            if(result) {    
                //System.out.println("DIR created");  
            }
        }
        return "src/updates/metadatos/" + this.usuario + "/";
    }
}