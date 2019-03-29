/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codigo;

import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JLabel;
import javax.swing.Timer;

/**
 *
 * @author Jorge 
 */
public class VentanaJuego extends javax.swing.JFrame {

    static int ANCHOPANTALLA = 600;
    static int ALTOPANTALLA = 450;
    
    public static Label lbl_puntuacion = new Label();
    //numero de marcianos que van a aparecer
    int filas = 5;
    int columnas = 8;
    
    int puntuacion = 0;
    
    BufferedImage buffer = null;
    
    int a;
    int b;
    int contadorTiempo = 0;

    Nave miNave = new Nave();
    Disparo miDisparo = new Disparo();
    //Marciano miMarciano = new Marciano();
    Marciano[][] listaMarcianos = new Marciano[filas][columnas];
    
    ArrayList<Explosion> listaExplosiones = new ArrayList();
    
    boolean direccionMarcianos = false;
    boolean gameOver = false;
    //el contador sirve para decidir qué imagen del marciano toca poner
    int contador = 0;
    
    
    
    //image para cargar el spritesheet con todos los sprite del juego
    BufferedImage plantilla = null;
    Image [][] imagenes;
    Image [][] imagenesDisparo;
    Image [][] imagenesDisparoDos;
    Image [][] imagenesMago;
    Image fondo;
    Timer temporizador = new Timer(10, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            bucleDelJuego();
        }
    });
    /**
     * Creates new form VentanaJuego
     */
    public VentanaJuego() {
       initComponents();
        setTitle("Space Invaders");
        setLocationRelativeTo(null);
        Font font1;
        Color color1;
        Color color2;
        font1 = new Font("Courier New", Font.BOLD, 40);
        color1 = new Color(124, 252, 0);
        color2 = new Color(0, 0, 0);
        lbl_puntuacion.setFont(font1);
        lbl_puntuacion.setForeground(color1);
        lbl_puntuacion.setBackground(color2);
        lbl_puntuacion.setBounds( 500, 0, 100, 45);
        lbl_puntuacion.setText("0");
        jPanel1.add(lbl_puntuacion);
       try {
       fondo = ImageIO.read(getClass().getResource("/imagenes/fondo.jpg"));
       } catch (IOException ex) {}
       
        reproduce("/sonido/musica.wav");
        
        // para cargar el archi imagenes, primero la imagen, segundo las filas, las columnas, las medidas y la escala
        imagenes = cargaImagenes("/imagenes/esqueletos.png", 1, 4, 400, 400, 10);
        imagenesDisparo = cargaImagenes("/imagenes/bolafuego.png", 1, 1, 126, 204, 10);
        imagenesDisparoDos = cargaImagenes("/imagenes/bolafuego2.png", 1, 1, 202, 220, 2);
        imagenesMago = cargaImagenes("/imagenes/mago.png", 1, 1, 223, 327, 8);
        
        setSize(ANCHOPANTALLA, ALTOPANTALLA);
        buffer = (BufferedImage) jPanel1.createImage(ANCHOPANTALLA, ALTOPANTALLA);
        buffer.createGraphics();

        temporizador.start();
        
        //inicializo la posición inicial de la nave
        miNave.imagen = imagenesMago[0][0];
        miDisparo.imagen = imagenesDisparo [0][0];
        miNave.x = ANCHOPANTALLA / 2 - miNave.imagen.getWidth(this) / 2;
        miNave.y = ALTOPANTALLA - miNave.imagen.getHeight(this) - 40;

        //inicializo el array de marcianos
            
            //numero de fila de marcianos que estoy creando
            //fila dentro del sprite columna
            creaFilaMarcianos(0, 0, 1);
            creaFilaMarcianos(1, 0, 1);
            creaFilaMarcianos(2, 0, 1);
            creaFilaMarcianos(3, 0, 1);
            creaFilaMarcianos(4, 0, 1);
        
    }
    private void creaFilaMarcianos (int numFilas, int spriteFila, int spriteColumna){
                    for (int j = 0; j < columnas; j++) {
                listaMarcianos[numFilas][j] = new Marciano();
                listaMarcianos[numFilas][j].imagen1 = imagenes[spriteFila][spriteColumna];
                listaMarcianos[numFilas][j].imagen2 = imagenes[spriteFila][spriteColumna + 2];
                listaMarcianos[numFilas][j].x = j * (15 + listaMarcianos[numFilas][j].imagen1.getWidth(null));
                listaMarcianos[numFilas][j].y = numFilas * (10 + listaMarcianos[numFilas][j].imagen1.getHeight(null));
            }
    }
    //este metodo va a servir para crear el array de l¡imagenes tal y como estan 
    // en el sprite sheet
    private Image[][] cargaImagenes (String nombreArchivoImagen, int numFilas, int numColumnas, int ancho, int alto, int escala){
        
        try {
            plantilla = ImageIO.read(getClass().getResource(nombreArchivoImagen));
        } catch (IOException ex) {}
        Image [][] arrayImagenes = new Image[numFilas][numColumnas];
        //cargo las imagenes de forma individual en cada imagen del array imagenes
        for (int i=0; i<numFilas; i++){
            for (int j=0; j<numColumnas; j++){
                arrayImagenes[i][j] = plantilla.getSubimage(j*ancho, i*alto, ancho, alto);
                arrayImagenes[i][j] = arrayImagenes[i][j].getScaledInstance(ancho/escala, alto/escala, Image.SCALE_SMOOTH);
            }
        }
       return arrayImagenes;
    }

    private void bucleDelJuego() {
        //se encarga del redibujado de los objetos en el jPanel1
        //primero borro todo lo que hay en el buffer
        contador++;
        Graphics2D g2 = (Graphics2D) buffer.getGraphics();
        if(!gameOver){
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, ANCHOPANTALLA, ALTOPANTALLA);

            g2.drawImage(fondo, 0, 0, null);

            ///////////////////////////////////////////////////////
            //redibujaremos aquí cada elemento
            g2.drawImage(miDisparo.imagen, miDisparo.x, miDisparo.y, null);
            g2.drawImage(miNave.imagen, miNave.x, miNave.y, null);
            pintaExplosiones(g2);
            pintaMarcianos(g2);
            chequeaColision();
            actualizaContadorTiempo();
            miNave.mueve();
            miDisparo.mueve();
            /////////////////////////////////////////////////////////////
            //*****************   fase final, se dibuja ***************//
            //*****************   el buffer de golpe sobre el Jpanel***//
            if (puntuacion == 200){
                try {
                    ganaPartida(g2);
                } catch (IOException ex){
                    
                }
            }
        }
        else{
            try {
                finPartida(g2);
            } catch (IOException ex) {

            }
        }
        g2 = (Graphics2D) jPanel1.getGraphics();
        g2.drawImage(buffer, 0, 0, null);
        
    }

    private void chequeaColision(){
        Rectangle2D.Double rectanguloMarciano = new Rectangle2D.Double();
        Rectangle2D.Double rectanguloDisparo = new Rectangle2D.Double();
        Rectangle2D.Double rectanguloNave = new Rectangle2D.Double();
        
        rectanguloDisparo.setFrame( miDisparo.x, 
                                    miDisparo.y,
                                    miDisparo.imagen.getWidth(null),
                                    miDisparo.imagen.getHeight(null));
        
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if (listaMarcianos[i][j].vivo) {
                    rectanguloMarciano.setFrame(listaMarcianos[i][j].x,
                                                listaMarcianos[i][j].y,
                                                listaMarcianos[i][j].imagen1.getWidth(null),
                                                listaMarcianos[i][j].imagen1.getHeight(null)
                                                );
                    if (rectanguloDisparo.intersects(rectanguloMarciano)){
                        listaMarcianos[i][j].vivo = false;
                        miDisparo.posicionaDisparo(miNave);
                        miDisparo.y = 1000;
                        miDisparo.disparado = false;
                        puntuacion = puntuacion + 5;
                           lbl_puntuacion.setText("" + puntuacion);
                        AudioClip sonido;
                        sonido = java.applet.Applet.newAudioClip(getClass().getResource("/sonido/huesos.wav"));
                        sonido.play();
                        Explosion e = new Explosion();
                        a = i;
                        b = j;
                        listaExplosiones.add(e);
                    }
                    rectanguloNave.setFrame(miNave.x, miNave.y, 
                                            miNave.imagen.getWidth(null), 
                                            miNave.imagen.getHeight(null));
                    if (rectanguloNave.intersects(rectanguloMarciano)){
                        gameOver = true;
                    }
                }
            }
        }
    }
    
    private void cambiaDireccionMarcianos() {
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                listaMarcianos[i][j].setvX(listaMarcianos[i][j].getvX()* -1);
                listaMarcianos[i][j].y += 10;
                
            }
        }
    }
    
    private void pintaMarcianos(Graphics2D _g2) {

        int anchoMarciano = listaMarcianos[0][0].imagen1.getWidth(null);
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if (listaMarcianos[i][j].vivo) {
                    listaMarcianos[i][j].mueve();
                    //chequeo si el marciano ha chocado contra la pared para cambiar la dirección 
                    //de todos los marcianos
                    if (listaMarcianos[i][j].x + anchoMarciano == ANCHOPANTALLA || listaMarcianos[i][j].x == 0) {
                        direccionMarcianos = true;
                    }
                    if (contador < 50) {
                        _g2.drawImage(listaMarcianos[i][j].imagen1,
                                listaMarcianos[i][j].x,
                                listaMarcianos[i][j].y,
                                null);
                    } else if (contador < 100) {
                        _g2.drawImage(listaMarcianos[i][j].imagen2,
                                listaMarcianos[i][j].x,
                                listaMarcianos[i][j].y,
                                null);
                    } else {
                        contador = 0;
                    }
                }
            }
        }
        if (direccionMarcianos ){
            cambiaDireccionMarcianos();
            direccionMarcianos = false;
        }
    }
    private void reproduce (String cancion){
           try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream( getClass().getResource(cancion) ));
            clip.loop(0);
            Thread one = new Thread() {
                    public void run() {
                            while(clip.getFramePosition()<clip.getFrameLength())
                                Thread.yield();
                    }  
                };
            one.start();
        } catch (Exception e) {      
        } 
   }
     private void finPartida (Graphics2D muerto) throws IOException{
        try{
            Image gameOver1 = ImageIO.read(getClass().getResource("/imagenes/youdied.jpg"));
            muerto.drawImage(gameOver1, 0, 0, ANCHOPANTALLA, ALTOPANTALLA, null);
        }catch (IOException ex){
        }
        reproduce("/sonido/youdied.wav");
    }
      private void ganaPartida (Graphics2D win) throws IOException{
        try{
            Image ganador = ImageIO.read(getClass().getResource("/imagenes/bonfire.jpg"));
            win.drawImage(ganador, 0, 0, ANCHOPANTALLA, ALTOPANTALLA, null);
        }catch (IOException ex){
        }
    }
     private void pintaExplosiones( Graphics2D g2){
            //pinto las explosiones
        for (int i=0; i<listaExplosiones.size(); i++){
            Explosion e = listaExplosiones.get(i);
            e.setTiempoDeVida(e.getTiempoDeVida() - 1);
            if (e.getTiempoDeVida() > 25){
                g2.drawImage(e.imagenExplosion, listaMarcianos[a][b].x, listaMarcianos[a][b].y, null);
            }
            else {
                g2.drawImage(e.imagenExplosion2, listaMarcianos[a][b].x, listaMarcianos[a][b].y, null);
            }
            
             //si el tiempo de vida de la explosión es menor que 0 la elimino
            if (e.getTiempoDeVida() <= 0){
                listaExplosiones.remove(i);
            }
        }
    }
     private void actualizaContadorTiempo(){
        contadorTiempo ++;
        if (contadorTiempo > 100){
            contadorTiempo = 0;
    }
    }
     private void cambioDisparo(){
         if (puntuacion == 150){
                    miDisparo.imagen = imagenesDisparoDos [0][0];
                } else {
                    miDisparo.imagen = imagenesDisparo [0][0];
                }
     }
    


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 450, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                miNave.setPulsadoIzquierda(true);
                break;
            case KeyEvent.VK_RIGHT:
                miNave.setPulsadoDerecha(true);
                break;
            case KeyEvent.VK_SPACE:
                miDisparo.posicionaDisparo(miNave);
                miDisparo.disparado = true;
                cambioDisparo();
                reproduce ("/sonido/firesound.wav");
                break;
        }
    }//GEN-LAST:event_formKeyPressed

    private void formKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                miNave.setPulsadoIzquierda(false);
                break;
            case KeyEvent.VK_RIGHT:
                miNave.setPulsadoDerecha(false);
                break;
        }
    }//GEN-LAST:event_formKeyReleased

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
            java.util.logging.Logger.getLogger(VentanaJuego.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(VentanaJuego.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(VentanaJuego.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VentanaJuego.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VentanaJuego().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
