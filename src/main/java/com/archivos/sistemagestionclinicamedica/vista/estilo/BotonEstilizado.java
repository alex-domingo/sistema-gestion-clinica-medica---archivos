package com.archivos.sistemagestionclinicamedica.vista.estilo;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Boton con estilo propio (esquinas redondeadas y color de relleno).
 *
 * Nimbus no deja cambiarle facil el color de fondo a un boton, asi que este lo
 * dibujamos nosotros mismos. Hay dos estilos: - primario: relleno de color
 * teal, texto blanco. Para la accion principal. - secundario: fondo blanco con
 * borde teal y texto teal. Para lo demas.
 *
 * Se pinta a mano en paintComponent, que es el metodo que Swing llama para
 * dibujar el componente. El texto lo sigue dibujando la clase padre.
 */
@SuppressWarnings("this-escape")
public class BotonEstilizado extends JButton {

    private static final long serialVersionUID = 1L;

    private static final int REDONDEO = 10;

    private final boolean primario;

    public BotonEstilizado(String texto, boolean primario) {
        super(texto);
        this.primario = primario;

        // Quitamos el dibujo por defecto para poner el nuestro.
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);

        setForeground(primario ? Color.WHITE : Tema.colores().primario);
        setFont(Tema.fuenteNegrita());
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Un poco de aire alrededor del texto.
        setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Suavizado tambien para el dibujo de lineas (el borde redondeado).
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int ancho = getWidth();
        int alto = getHeight();

        if (primario) {
            // Boton relleno: ocupa todo el area.
            g2.setColor(calcularColorFondo());
            g2.fillRoundRect(0, 0, ancho, alto, REDONDEO, REDONDEO);
        } else {
            // Boton secundario: relleno + borde. El borde tiene grosor, y como
            // se dibuja centrado sobre la linea, hay que dejarle medio pixel de
            // margen en cada lado para que no se corte en las esquinas.
            g2.setColor(calcularColorFondo());
            g2.fillRoundRect(1, 1, ancho - 2, alto - 2, REDONDEO, REDONDEO);

            g2.setStroke(new BasicStroke(1f));
            g2.setColor(Tema.colores().primario);
            g2.drawRoundRect(1, 1, ancho - 3, alto - 3, REDONDEO, REDONDEO);
        }

        g2.dispose();
        super.paintComponent(g);   // dibuja el texto encima
    }

    // El color cambia segun si el mouse esta encima o el boton esta presionado.
    private Color calcularColorFondo() {
        Tema.Paleta c = Tema.colores();
        if (primario) {
            if (getModel().isPressed()) {
                return c.primarioFuerte.darker();
            }
            if (getModel().isRollover()) {
                return c.primarioFuerte;
            }
            return c.primario;
        } else {
            // Secundario: fondo claro que se tinta un poco al pasar el mouse.
            if (getModel().isPressed()) {
                return c.primarioSuave;
            }
            if (getModel().isRollover()) {
                return c.primarioSuave;
            }
            return c.superficie;
        }
    }
}
