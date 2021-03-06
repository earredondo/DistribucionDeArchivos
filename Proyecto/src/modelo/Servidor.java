package modelo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Servidor extends Thread{

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
    
    public Servidor(ServerSocket servidor){
        this.servidor = servidor;
    }

    public void aceptaConexion(){
        try {
            cliente=servidor.accept();
            System.out.println("Cliente conectado desde "+cliente.getInetAddress().getHostAddress()+":"+cliente.getPort()); 
            new HiloCliente(cliente).start();
            /*dis=new DataInputStream(cliente.getInputStream());
            vista.setTexto("Recibiendo archivo");
            vista.setTexto(this.recibe()+" recibido\n");
            this.terminaConexion();*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void run(){
        while(true){
            aceptaConexion();
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

    public String recibe(DataInputStream dis){
        try{
            String nombre;
            nombre=dis.readUTF();
            int parte = dis.readInt();
            long tamanio=dis.readLong();
            String path = crearDirectorio();
            System.out.println("Leyendo archivo " + nombre + ".part" + parte);
            DataOutputStream dos=new DataOutputStream(new FileOutputStream(new File(path + nombre + ".part" + parte)));
            System.out.println("Tam: " + tamanio);
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
    
    public String recibeMeta(DataInputStream dis){
        try{
            String nombre;
            nombre=dis.readUTF();
            long tamanio=dis.readLong();
            String path = crearDirectorioMeta();
            System.out.println("Leyendo archivo " + nombre);
            DataOutputStream dos=new DataOutputStream(new FileOutputStream(new File(path + nombre)));
            System.out.println("Tam: " + tamanio);
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
    
    public void enviar(Socket cliente){
        try{
            DataInputStream dis = new DataInputStream(cliente.getInputStream());
            DataOutputStream dos = new DataOutputStream(cliente.getOutputStream());
            String archivo = dis.readUTF();
            String fragmento = dis.readUTF();
            String ip = this.servidor.getInetAddress().getHostAddress();
            int puerto = this.servidor.getLocalPort();
            String id = ip + ":" + puerto;
            String pathArchivo = "src/updates/archivos/" + id + "/" + archivo + ".part" + fragmento;
            File elArchivo = new File(pathArchivo);
            long tam = elArchivo.length();
            dos.writeLong(tam);
            DataInputStream fis = new DataInputStream(new FileInputStream(elArchivo));
            long enviados = 0;
            byte []buffer = new byte[1500];
            while(enviados < tam){
                int leidos = fis.read(buffer);
                if(leidos < 1){break;}
                dos.write(buffer, 0 , leidos);
                enviados+=leidos;
                int porcentaje = (int) ((enviados * 100)/tam);
                System.out.print("\rEnviado: " + porcentaje + "%");
            }
            dis.close();
            dos.close();
            fis.close();
            cliente.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    
    private class HiloCliente extends Thread{
        private Socket cliente;
        public HiloCliente(Socket cliente){
            super();
            this.cliente = cliente;
        }
        public void run(){
            try{
                DataInputStream dis=new DataInputStream(cliente.getInputStream());
                System.out.println("Cliente conectado desde "+cliente.getInetAddress().getHostAddress()+":"+cliente.getPort()); 
                String operacion = dis.readUTF();
                if("subir".equals(operacion)){recibe(dis);}
                else if("descargar".equals(operacion)){enviar(cliente);}
                else if("meta".equals(operacion)){recibeMeta(dis);}
                terminaConexion();
            }catch(IOException ioe){
                ioe.printStackTrace();
            }
        }
    }
    
    private String crearDirectorio(){
        String ip = this.servidor.getInetAddress().getHostAddress();
        int puerto = this.servidor.getLocalPort();
        String id = ip + ":" + puerto;
        File theDir = new File("src/updates/archivos/" + id + "/");

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
        return "src/updates/archivos/" + id + "/";
    }
    
    private String crearDirectorioMeta(){
        String ip = this.servidor.getInetAddress().getHostAddress();
        int puerto = this.servidor.getLocalPort();
        String id = ip + ":" + puerto;
        File theDir = new File("src/updates/metadatos/" + id + "/");

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
        return "src/updates/metadatos/" + id + "/";
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
