package cliente;


public class Usuario {
    private String nombre;
    private boolean estado;
    
    public Usuario(String nombre, boolean estado){
        this.nombre=nombre;
        this.estado=estado;
    }
    
    public void setEstado(boolean estado){this.estado=estado;}
    
    public boolean getEstado(){return estado;}
    public String getNombre(){return nombre;}
}
