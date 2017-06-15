/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import java.io.File;
import javax.swing.DefaultListModel;

/**
 *
 * @author edgar
 */
public class MonitorArchivos extends Thread{
    
    private DefaultListModel archivos;
    private String ruta;
    
    MonitorArchivos(DefaultListModel archivos, String servidor) {
        this.archivos = archivos;
        this.ruta = "src/updates/metadatos/" + servidor + "/";
    }
    
    public void run(){
        crearDirectorio();
        System.out.println(this.ruta);
        while(true){
            File dir = new File(this.ruta);
            File []archivos = dir.listFiles();
            for(File archivo : archivos){
                if(!this.archivos.contains(archivo.getName())){
                    this.archivos.addElement(archivo.getName());
                }
            }
            try{
                sleep(5000);
            }catch(InterruptedException ie){
                ie.printStackTrace();
            }
        }
    }
    
    private void crearDirectorio(){
        File theDir = new File(this.ruta);

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
    }
    
}
