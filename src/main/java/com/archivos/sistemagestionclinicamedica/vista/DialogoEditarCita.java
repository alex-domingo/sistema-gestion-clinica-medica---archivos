package com.archivos.sistemagestionclinicamedica.vista;

import com.archivos.sistemagestionclinicamedica.excepcion.ClinicaException;
import com.archivos.sistemagestionclinicamedica.modelo.Cita;
import com.archivos.sistemagestionclinicamedica.servicio.CitaServicio;
import com.archivos.sistemagestionclinicamedica.vista.estilo.BotonEstilizado;
import com.archivos.sistemagestionclinicamedica.vista.estilo.Tema;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Ventana emergente para cambiar solo el motivo y las observaciones de una
 * cita.
 *
 * Es aparte del dialogo de programar porque cambiar el motivo no toca la fecha,
 * la hora ni el medico, asi que no hay que revalidar traslapes. El paciente y
 * el medico se muestran como texto de solo lectura, para dar contexto.
 */
@SuppressWarnings("this-escape")
public class DialogoEditarCita extends JDialog {

    private static final long serialVersionUID = 1L;

    private final transient CitaServicio servicio;
    private final transient Cita cita;

    private final JTextField txtMotivo = new JTextField(20);
    private final JTextField txtObservaciones = new JTextField(20);

    private boolean guardado = false;

    public DialogoEditarCita(Frame padre, CitaServicio servicio, Cita cita) {
        super(padre, true);
        this.servicio = servicio;
        this.cita = cita;

        setTitle("Editar motivo / observaciones");
        setLayout(new BorderLayout());
        txtMotivo.setText(cita.getMotivo());
        txtObservaciones.setText(cita.getObservaciones());
        add(construirFormulario(), BorderLayout.CENTER);
        add(construirBotones(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(padre);
    }

    private JPanel construirFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Tema.colores().superficie);
        panel.setBorder(BorderFactory.createEmptyBorder(
                Tema.ESPACIO_GRANDE, Tema.ESPACIO_GRANDE,
                Tema.ESPACIO_GRANDE, Tema.ESPACIO_GRANDE));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.anchor = GridBagConstraints.WEST;

        // Contexto de solo lectura: de que cita se trata.
        g.gridx = 0;
        g.gridy = 0;
        JLabel info = new JLabel("Cita del " + cita.getFechaFormateada()
                + " a las " + cita.getHoraInicioFormateada());
        info.setForeground(Tema.colores().textoTenue);
        g.gridwidth = 2;
        panel.add(info, g);
        g.gridwidth = 1;

        g.gridx = 0;
        g.gridy = 1;
        JLabel l1 = new JLabel("Motivo:");
        l1.setForeground(Tema.colores().textoTenue);
        panel.add(l1, g);
        g.gridx = 1;
        panel.add(txtMotivo, g);

        g.gridx = 0;
        g.gridy = 2;
        JLabel l2 = new JLabel("Observaciones:");
        l2.setForeground(Tema.colores().textoTenue);
        panel.add(l2, g);
        g.gridx = 1;
        panel.add(txtObservaciones, g);

        return panel;
    }

    private JPanel construirBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, Tema.ESPACIO_CHICO, Tema.ESPACIO_CHICO));
        panel.setBackground(Tema.colores().superficie);
        BotonEstilizado btnGuardar = new BotonEstilizado("Guardar", true);
        BotonEstilizado btnCancelar = new BotonEstilizado("Cancelar", false);
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());
        panel.add(btnGuardar);
        panel.add(btnCancelar);
        return panel;
    }

    private void guardar() {
        try {
            servicio.modificarMotivoObservaciones(
                    cita.getUuid(), txtMotivo.getText(), txtObservaciones.getText());
            guardado = true;
            dispose();
        } catch (ClinicaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "No se pudo guardar", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean fueGuardado() {
        return guardado;
    }
}
