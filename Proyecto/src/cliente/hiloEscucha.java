package cliente;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultListModel;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

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
        else if(operacion.toLowerCase().equals("privado")){mostrarMensajePrivado(mensaje.substring(indice2+1));}
    }

    private void agregarUsuario(String usuario){
        usuarios.add(usuario);
        System.out.println("Usuarios: "+usuarios.size());
        isConnected(usuario);
    }

    private void mostrarMensajePrivado(String datos){
        int indice1, indice2;
        String remitente,destinatario,mensaje;
        indice1=datos.indexOf('<');
        indice2=datos.indexOf('>');
        remitente=datos.substring(indice1+1, indice2);
        datos=datos.substring(indice2+1);
        indice1=datos.indexOf('<');
        indice2=datos.indexOf('>');
        destinatario=datos.substring(indice1+1, indice2);
        mensaje=datos.substring(indice2+1);
        if(destinatario.equals(usuario)){
            System.out.println(remitente+": "+mensaje);
            buscarEmoji(mensaje);
        }
    }

    private String buscarEmoji(String mensaje){
        String html="";
        String mensajeAnterior=mensaje;
        String[] emojis={":/",":)",":D",":(",":'(",":P","O:)","3:)","o.O",";)",":O"
                        ,"-_-",">:O",":*","<3","^_^","8-)","8|","(^^^)",":|]",">:("
                        ,":v",":3","(y)","<(\")"};
        String pathImagenes="file:"+System.getProperty("user.dir")+"/src/archivos/img";
        String pathImagen;
        for(int i=0;i<emojis.length;i++){
            pathImagen=pathImagenes+(i+1)+".png";
            html=mensajeAnterior.replace(emojis[i],"<img src='"+pathImagen+"' alt='Smiley face' height='16' width='16'>");
            mensajeAnterior=html;
        }
        return html;
    }
    
    private String buscarP(String mensaje){
        String[] lines = mensaje.split(System.getProperty("line.separator"));
        String texto="";
        String contenido;
        int j;
        for(int i=0;i<lines.length;i++){
            if(lines[i].toLowerCase().contains("<p>")){
                j=i+1;
                contenido="";
                while(!lines[j].toLowerCase().contains("</p>")){
                    contenido+=lines[j++];
                }
                texto+="<p>"+contenido+"</p>";
            }
        }
        return texto;
    }

    private String printFormatMessage(String usuario,String text) {
        Calendar fechaPedido = Calendar.getInstance();
        int hrs = fechaPedido.get(Calendar.HOUR_OF_DAY);
        int min = fechaPedido.get(Calendar.MINUTE);
        StringBuilder buildMessage = new StringBuilder("");
        buildMessage.append(usuario).append(":<br>");
        buildMessage.append("[").append(hrs).append(":").append(min).append("]: ");
        buildMessage.append(buscarEmoji(text)).append("");
        return buildMessage.toString();
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