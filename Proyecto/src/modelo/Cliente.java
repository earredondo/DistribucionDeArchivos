package modelo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

public class Cliente{

	private int puerto;
	private String host;
	private Socket cliente;
        private DataOutputStream dos;

	public Cliente(){
		try{
			puerto=1234;
			host="localhost";
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void envia(File archivo){
            try{
                cliente=new Socket(host,puerto);
                dos=new DataOutputStream(cliente.getOutputStream());
                System.out.println("Conexion establecida");
                String nombre=archivo.getName();
                long tamanio=archivo.length();
                String path=archivo.getAbsolutePath();
                System.out.println(nombre);
                System.out.println(path);
                DataInputStream dis=new DataInputStream(new FileInputStream(path));
                dos.writeUTF(nombre);
                dos.writeLong(tamanio);
                byte[] buffer=new byte[1500];   //MTU Ethernet
                long enviados=0;
                int n=0;
                int porcentaje=0;
                while(enviados<tamanio){
                    n=dis.read(buffer);
                    dos.write(buffer, 0, n);
                    enviados+=n;
                    porcentaje=(int)(enviados*100/tamanio);
                    System.out.print("\rTransmitido: "+porcentaje+"%");
                }
                dos.flush();
                dis.close();
                destruir();
            }catch(Exception e){
                e.printStackTrace();
            }
		
	}
        
        public void envia(File[] archivos){
            try {
                for (File archivo : archivos) {
                    if(archivo.isDirectory()){
                        File[] narchivos=archivo.listFiles();
                        envia(narchivos);
                    }
                    else{
                        envia(archivo);
                    }
                    System.out.println("\nArchivo enviado");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

	public void destruir(){
		try{
                        dos.close();
			cliente.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
/*
    public static void main(String []args){
		Cliente cliente=new Cliente();
		JFileChooser jfc=new JFileChooser();
                jfc.setMultiSelectionEnabled(true);
            	int r=jfc.showOpenDialog(null);
            	if(r==JFileChooser.APPROVE_OPTION){
                    File[] archivos = jfc.getSelectedFiles();
                    cliente.envia(archivos);
		}
    }
*/
}