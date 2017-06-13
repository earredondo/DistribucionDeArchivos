package vistas;

import modelo.FileDrop;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import cliente.Cliente;


/**
 *
 * @author rafal
 */
public class VistaCliente extends javax.swing.JFrame {

    private Cliente cliente;
    private JFileChooser archivos;
    private File lista[];
    private javax.swing.DefaultListModel modelo;
            
    /**
     * Creates new form VistaCliente
     */
    public VistaCliente() {
        modelo = new javax.swing.DefaultListModel();
        lista=new File[0];
        initComponents();
        cliente=new Cliente(modelo);
        setTitle(cliente.getId());
        cliente.unirAGrupo("224.0.0.1",4000);
        cliente.enviarMiIp();
        cliente.escucharMensajes();
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contenedorPrincipal = new javax.swing.JPanel();
        tituloEtiquetaEditorPane = new javax.swing.JLabel();
        seleccionArchivos = new javax.swing.JButton();
        envioArchivos = new javax.swing.JButton();
        separador = new javax.swing.JSeparator();
        scrollPanelTextArea = new javax.swing.JScrollPane();
        areaArchivos = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        listConnections = new javax.swing.JList<>();
        labelConnections = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Cliente");

        tituloEtiquetaEditorPane.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        tituloEtiquetaEditorPane.setText("Lista de archivos: ");

        seleccionArchivos.setText("Seleccionar archivo");
        seleccionArchivos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                seleccionArchivosMouseClicked(evt);
            }
        });
        seleccionArchivos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seleccionArchivosActionPerformed(evt);
            }
        });

        envioArchivos.setText("Enviar");
        envioArchivos.setEnabled(false);
        envioArchivos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                envioArchivosMouseClicked(evt);
            }
        });
        envioArchivos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                envioArchivosActionPerformed(evt);
            }
        });

        areaArchivos.setEditable(false);
        areaArchivos.setColumns(20);
        areaArchivos.setRows(5);
        areaArchivos.setDragEnabled(true);
        new FileDrop( System.out, areaArchivos, /*dragBorder,*/ new FileDrop.Listener()
            {
                public void filesDropped( java.io.File[] files )
                {
                    for( int i = 0; i < files.length; i++ )
                    {
                        try {
                            areaArchivos.append( files[i].getCanonicalPath() + "\n" );
                        } catch( java.io.IOException e ) {
                            e.printStackTrace();
                        }
                    }
                    envioArchivos.setEnabled(true);
                    File[] tmp = new File[lista.length + files.length];
                    System.arraycopy(lista, 0, tmp, 0, lista.length);
                    System.arraycopy(files, 0, tmp, lista.length, files.length);
                    lista = tmp;
                }
            });
            scrollPanelTextArea.setViewportView(areaArchivos);

            listConnections.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
            listConnections.setModel(modelo);
            jScrollPane2.setViewportView(listConnections);

            labelConnections.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
            labelConnections.setText("Lista de usuarios:");

            javax.swing.GroupLayout contenedorPrincipalLayout = new javax.swing.GroupLayout(contenedorPrincipal);
            contenedorPrincipal.setLayout(contenedorPrincipalLayout);
            contenedorPrincipalLayout.setHorizontalGroup(
                contenedorPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(contenedorPrincipalLayout.createSequentialGroup()
                    .addGap(39, 39, 39)
                    .addGroup(contenedorPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(contenedorPrincipalLayout.createSequentialGroup()
                            .addComponent(tituloEtiquetaEditorPane)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labelConnections))
                        .addComponent(separador, javax.swing.GroupLayout.PREFERRED_SIZE, 538, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(contenedorPrincipalLayout.createSequentialGroup()
                            .addGroup(contenedorPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(contenedorPrincipalLayout.createSequentialGroup()
                                    .addGap(102, 102, 102)
                                    .addComponent(seleccionArchivos)
                                    .addGap(84, 84, 84)
                                    .addComponent(envioArchivos, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(scrollPanelTextArea, javax.swing.GroupLayout.PREFERRED_SIZE, 499, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            contenedorPrincipalLayout.setVerticalGroup(
                contenedorPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(contenedorPrincipalLayout.createSequentialGroup()
                    .addGap(23, 23, 23)
                    .addGroup(contenedorPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tituloEtiquetaEditorPane)
                        .addComponent(labelConnections))
                    .addGap(2, 2, 2)
                    .addComponent(separador, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(contenedorPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane2)
                        .addComponent(scrollPanelTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                    .addGap(28, 28, 28)
                    .addGroup(contenedorPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(seleccionArchivos)
                        .addComponent(envioArchivos))
                    .addContainerGap(19, Short.MAX_VALUE))
            );

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(contenedorPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(contenedorPrincipal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            );

            pack();
        }// </editor-fold>//GEN-END:initComponents

    private void seleccionArchivosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_seleccionArchivosMouseClicked
        archivos = new JFileChooser();
        archivos.setMultiSelectionEnabled(true);
        int r = archivos.showOpenDialog(null);
        if ( r == JFileChooser.APPROVE_OPTION ) {
            for (File file : archivos.getSelectedFiles()) {
                try {
                    areaArchivos.append( file.getCanonicalPath() + "\n" );
                } catch (IOException ex) {
                    System.out.println("Ha ocurrido un error de E/S: " + ex.getMessage());
                }
            }
            envioArchivos.setEnabled(true);
            File[] files=archivos.getSelectedFiles();
            File[] tmp = new File[lista.length + files.length];
            System.arraycopy(lista, 0, tmp, 0, lista.length);
            System.arraycopy(files, 0, tmp, lista.length, files.length);
            lista = tmp;
            //lista = archivos.getSelectedFiles();
        } else if ( r == JFileChooser.CANCEL_OPTION ) {
            envioArchivos.setEnabled(false);
        }
    }//GEN-LAST:event_seleccionArchivosMouseClicked

    private void seleccionArchivosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seleccionArchivosActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_seleccionArchivosActionPerformed

    private void envioArchivosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_envioArchivosActionPerformed
        
    }//GEN-LAST:event_envioArchivosActionPerformed

    private void envioArchivosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_envioArchivosMouseClicked
        //cliente.envia(lista);
        areaArchivos.append("\nSelecciona nuevos archivos\n\n");
        lista=new File[0];
    }//GEN-LAST:event_envioArchivosMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(VistaCliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(VistaCliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(VistaCliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VistaCliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VistaCliente().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea areaArchivos;
    private javax.swing.JPanel contenedorPrincipal;
    private javax.swing.JButton envioArchivos;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel labelConnections;
    private javax.swing.JList<String> listConnections;
    private javax.swing.JScrollPane scrollPanelTextArea;
    private javax.swing.JButton seleccionArchivos;
    private javax.swing.JSeparator separador;
    private javax.swing.JLabel tituloEtiquetaEditorPane;
    // End of variables declaration//GEN-END:variables
}