package modelo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JFrame;
import vistas.VistaServidor;


public class Servidor{

    private int puerto;
    private ServerSocket servidor;
    private Socket cliente;
    private DataInputStream dis;

    public Servidor(){
            puerto=1234;
            try{
                    servidor=new ServerSocket(puerto);
                    System.out.println("Servidor listo... esperando clientes...");
            }catch(Exception e){
                    e.printStackTrace();
            }
    }

    public void aceptaConexion(VistaServidor vista){
        try {
            cliente=servidor.accept();
            dis=new DataInputStream(cliente.getInputStream());
            System.out.println("Cliente conectado desde "+cliente.getInetAddress().getHostAddress()+":"+cliente.getPort()); 
            vista.setTexto("Recibiendo archivo");
            vista.setTexto(this.recibe()+" recibido\n");
            this.terminaConexion();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void terminaConexion(){
        try {
            cliente.close();
            System.out.println("Cliente desconectado");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String recibe(){
        try{
            String nombre;
            nombre=dis.readUTF();
            DataOutputStream dos=new DataOutputStream(new FileOutputStream(new File("src/updates/archivos/"+nombre)));
            long tamanio=dis.readLong();
            long leidos=0;
            int n=0;
            int porcentaje=0;
            byte[] buffer=new byte[1500];
            while(leidos<tamanio){
                n=dis.read(buffer);
                dos.write(buffer, 0, n);
                leidos+=n;
                porcentaje=(int)(leidos*100/tamanio);
                System.out.print("\rRecibido: "+porcentaje+"%");
            }
            System.out.println("\n"+nombre+" recibido");
            dos.close();
            return nombre;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

    }

    public void recibe(int[] archivos){
        try {
            int numeroArchivos=dis.readInt();
            for(int i=0;i<numeroArchivos;i++){
                recibe();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private class HiloCliente extends Thread{
        private Socket cliente;
        public HiloCliente(Socket cliente){
            this.cliente = cliente;
        }
        public void run(){
            try{
                DataInputStream dis=new DataInputStream(cliente.getInputStream());
                System.out.println("Cliente conectado desde "+cliente.getInetAddress().getHostAddress()+":"+cliente.getPort()); 
                terminaConexion();
            }catch(IOException ioe){
                ioe.printStackTrace();
            }
        }
    }
    /*
    public static void main(String []args){
            Servidor servidor=new Servidor();
            while(true){
                servidor.aceptaConexion();
                servidor.recibe();
                servidor.terminaConexion();
            }
    }
    */
}
