/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import java.io.Serializable;

/**
 *
 * @author edgar
 */
public class Paquete implements Serializable{
    private int numero;
    private byte[] datos;
    
    public Paquete(int numero, byte[] datos){
        this.numero = numero;
        this.datos = datos;
    }

    public int getNumero() {
        return numero;
    }

    public byte[] getDatos() {
        return datos;
    }
}
