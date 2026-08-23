package com.archivos.sistemagestionclinicamedica.vista.estilo;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

/**
 * Estilo visual de toda la aplicacion, en un solo lugar.
 *
 * La idea es que ningun color ni fuente este escrito suelto por ahi en los
 * paneles. Todos le piden los valores a esta clase. Asi la app se ve igual en
 * todos lados y, si algun dia queremos cambiar los colores o pasar a tema
 * oscuro, se cambia aca nomas.
 *
 * Hay dos paletas: CLARA (la que usamos ahora) y OSCURA (lista para el futuro).
 * Cambiar de una a otra es llamar a aplicar() con la otra paleta.
 */
public final class Tema {

    /**
     * Una paleta es un juego completo de colores. Cada constante del enum trae
     * su propio juego, asi cambiar de tema es cambiar de constante.
     */
    public enum Paleta {

        // Tema claro: verde/teal sobre mucho blanco.
        CLARA(
                new Color(0xF7, 0xFA, 0xFA), // fondo de la app (casi blanco)
                new Color(0xFF, 0xFF, 0xFF), // superficie (tarjetas, tablas)
                new Color(0x0D, 0x94, 0x88), // primario (teal)
                new Color(0x0F, 0x76, 0x6E), // primario fuerte (hover/click)
                new Color(0xCC, 0xFB, 0xF1), // primario suave (seleccion)
                new Color(0x1E, 0x29, 0x3B), // texto principal
                new Color(0x64, 0x74, 0x8B), // texto tenue (secundario)
                new Color(0xE2, 0xE8, 0xF0), // bordes y lineas
                new Color(0xF1, 0xF7, 0xF6) // fila alterna de tabla
        ),
        // Tema oscuro: mismos roles, colores para fondo oscuro.
        OSCURA(
                new Color(0x0F, 0x17, 0x2A),
                new Color(0x1E, 0x29, 0x3B),
                new Color(0x2D, 0xD4, 0xBF),
                new Color(0x14, 0xB8, 0xA6),
                new Color(0x13, 0x4E, 0x4A),
                new Color(0xF1, 0xF5, 0xF9),
                new Color(0x94, 0xA3, 0xB8),
                new Color(0x33, 0x41, 0x55),
                new Color(0x18, 0x22, 0x35)
        );

        public final Color fondo;
        public final Color superficie;
        public final Color primario;
        public final Color primarioFuerte;
        public final Color primarioSuave;
        public final Color texto;
        public final Color textoTenue;
        public final Color borde;
        public final Color filaAlterna;

        Paleta(Color fondo, Color superficie, Color primario, Color primarioFuerte,
                Color primarioSuave, Color texto, Color textoTenue, Color borde,
                Color filaAlterna) {
            this.fondo = fondo;
            this.superficie = superficie;
            this.primario = primario;
            this.primarioFuerte = primarioFuerte;
            this.primarioSuave = primarioSuave;
            this.texto = texto;
            this.textoTenue = textoTenue;
            this.borde = borde;
            this.filaAlterna = filaAlterna;
        }
    }

    // Espaciados estandar, para que los margenes sean parejos en toda la app.
    public static final int ESPACIO_CHICO = 6;
    public static final int ESPACIO = 12;
    public static final int ESPACIO_GRANDE = 20;

    // Nombre de la fuente elegida (se calcula una vez al inicio).
    private static String familiaFuente = "SansSerif";

    // La paleta que se esta usando ahora mismo.
    private static Paleta actual = Paleta.CLARA;

    private Tema() {
    }

    /**
     * Devuelve la paleta activa, para que los paneles pidan sus colores.
     */
    public static Paleta colores() {
        return actual;
    }

    // --- Fuentes ---
    public static Font fuenteNormal() {
        return new Font(familiaFuente, Font.PLAIN, 14);
    }

    public static Font fuenteEtiqueta() {
        return new Font(familiaFuente, Font.PLAIN, 13);
    }

    public static Font fuenteNegrita() {
        return new Font(familiaFuente, Font.BOLD, 14);
    }

    public static Font fuenteTitulo() {
        return new Font(familiaFuente, Font.BOLD, 20);
    }

    public static Font fuenteSubtitulo() {
        return new Font(familiaFuente, Font.PLAIN, 13);
    }

    /**
     * Aplica una paleta a toda la aplicacion. Configura Nimbus y le pasa los
     * colores. Debe llamarse una vez al inicio, antes de crear las ventanas.
     */
    public static void aplicar(Paleta paleta) {
        actual = paleta;
        familiaFuente = elegirFuente();

        try {
            // Buscar y activar Nimbus.
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }

            // Nimbus arma casi todos sus colores a partir de unas pocas claves
            // base. Cambiando estas, se re-tinta toda la interfaz de un solo golpe.
            UIManager.put("control", paleta.fondo);
            UIManager.put("background", paleta.fondo);
            UIManager.put("nimbusLightBackground", paleta.superficie);
            UIManager.put("text", paleta.texto);
            UIManager.put("controlText", paleta.texto);
            UIManager.put("infoText", paleta.texto);
            UIManager.put("nimbusBase", paleta.primario);
            UIManager.put("nimbusFocus", paleta.primario);
            UIManager.put("nimbusSelectionBackground", paleta.primario);
            UIManager.put("nimbusSelection", paleta.primario);
            UIManager.put("menu", paleta.superficie);
            UIManager.put("info", paleta.superficie);

            // Fuente por defecto para todos los componentes.
            Font fuente = fuenteNormal();
            UIManager.getLookAndFeelDefaults().put("defaultFont", fuente);
            UIManager.put("Label.font", fuenteEtiqueta());
            UIManager.put("Button.font", fuenteNegrita());
            UIManager.put("Table.font", fuenteNormal());
            UIManager.put("TableHeader.font", fuenteNegrita());
            UIManager.put("TextField.font", fuenteNormal());
            UIManager.put("ComboBox.font", fuenteNormal());
            UIManager.put("TabbedPane.font", fuenteNegrita());

        } catch (Exception e) {
            System.err.println("No se pudo aplicar el tema, se usara la apariencia por defecto: "
                    + e.getMessage());
        }
    }

    // Elige la primera fuente linda que este instalada. En Windows suele estar
    // Segoe UI; en Linux, alguna de las otras. Si no hay ninguna, SansSerif.
    private static String elegirFuente() {
        String[] preferidas = {"Segoe UI", "Roboto", "Noto Sans", "DejaVu Sans", "SansSerif"};
        String[] instaladas = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        for (String preferida : preferidas) {
            if (Arrays.asList(instaladas).contains(preferida)) {
                return preferida;
            }
        }
        return "SansSerif";
    }
}
