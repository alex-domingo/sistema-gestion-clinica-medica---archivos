package com.archivos.sistemagestionclinicamedica;

import com.archivos.sistemagestionclinicamedica.vista.VentanaPrincipal;
import com.archivos.sistemagestionclinicamedica.vista.estilo.Tema;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Clase principal. Aplica el tema visual y abre la ventana principal.
 */
public class SistemaGestionClinicaMedica {

    public static void main(String[] args) {
        // Aplicar el tema (paleta clara). Todo el estilo vive en la clase Tema;
        // para pasar a oscuro en el futuro seria Tema.Paleta.OSCURA aca.
        Tema.aplicar(Tema.Paleta.CLARA);

        // La interfaz de Swing debe crearse en el hilo de eventos (EDT).
        SwingUtilities.invokeLater(() -> {
            try {
                VentanaPrincipal ventana = new VentanaPrincipal();
                ventana.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "No se pudo iniciar la aplicacion:\n" + e.getMessage(),
                        "Error fatal", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
