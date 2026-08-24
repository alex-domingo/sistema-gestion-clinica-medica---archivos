package com.archivos.sistemagestionclinicamedica.vista;

import com.archivos.sistemagestionclinicamedica.excepcion.ClinicaException;
import com.archivos.sistemagestionclinicamedica.modelo.Cita;
import com.archivos.sistemagestionclinicamedica.modelo.Medico;
import com.archivos.sistemagestionclinicamedica.modelo.Paciente;
import com.archivos.sistemagestionclinicamedica.servicio.CitaServicio;
import com.archivos.sistemagestionclinicamedica.servicio.MedicoServicio;
import com.archivos.sistemagestionclinicamedica.servicio.PacienteServicio;
import com.archivos.sistemagestionclinicamedica.vista.estilo.BotonEstilizado;
import com.archivos.sistemagestionclinicamedica.vista.estilo.Tema;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Ventana emergente para programar una cita nueva.
 *
 * El paciente y el medico se eligen de combos (no se escriben a mano), asi no
 * hay errores de tipeo con el DPI ni el UUID. El combo de medicos muestra solo
 * los activos, porque un medico inactivo no puede recibir citas.
 *
 * Este dialogo solo programa citas nuevas. Cambiar el motivo/observaciones de
 * una existente se hace desde otro dialogo mas simple; cambiar fecha/medico se
 * hace cancelando y programando de nuevo (asi se revalidan los traslapes).
 */
@SuppressWarnings("this-escape")
public class DialogoCita extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final transient CitaServicio servicio;

    private final JComboBox<Paciente> cboPaciente = new JComboBox<>();
    private final JComboBox<Medico> cboMedico = new JComboBox<>();
    private final JLabel lblHorarioMedico = new JLabel(" ");
    private final JTextField txtFecha = new JTextField(18);
    private final JTextField txtHora = new JTextField(18);
    private final JTextField txtMotivo = new JTextField(18);
    private final JTextField txtObservaciones = new JTextField(18);

    private boolean guardado = false;

    public DialogoCita(Frame padre, CitaServicio servicio,
            PacienteServicio pacienteServicio, MedicoServicio medicoServicio) {
        super(padre, true);
        this.servicio = servicio;

        setTitle("Programar cita");
        setLayout(new BorderLayout());
        cargarPacientes(pacienteServicio);
        cargarMedicosActivos(medicoServicio);

        // Cuando cambia el medico elegido, se actualiza el horario mostrado.
        cboMedico.addActionListener(e -> actualizarHorarioMedico());
        actualizarHorarioMedico();   // mostrar el del medico inicial

        add(construirFormulario(), BorderLayout.CENTER);
        add(construirBotones(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(padre);
    }

    // Muestra el horario de atencion del medico seleccionado, como ayuda para
    // no ingresar una hora fuera de rango.
    private void actualizarHorarioMedico() {
        Medico medico = (Medico) cboMedico.getSelectedItem();
        if (medico == null) {
            lblHorarioMedico.setText("Sin medicos activos disponibles.");
        } else {
            lblHorarioMedico.setText("Atiende de " + medico.getHoraInicioFormateada()
                    + " a " + medico.getHoraFinFormateada()
                    + " (la cita dura " + Cita.DURACION_MINUTOS + " min).");
        }
    }

    private void cargarPacientes(PacienteServicio pacienteServicio) {
        try {
            DefaultComboBoxModel<Paciente> modelo = new DefaultComboBoxModel<>();
            List<Paciente> lista = pacienteServicio.listarTodos();
            for (Paciente p : lista) {
                modelo.addElement(p);
            }
            cboPaciente.setModel(modelo);
        } catch (ClinicaException e) {
            mostrarError("No se pudieron cargar los pacientes: " + e.getMessage());
        }
    }

    // Solo medicos activos: un inactivo no puede recibir citas.
    private void cargarMedicosActivos(MedicoServicio medicoServicio) {
        try {
            DefaultComboBoxModel<Medico> modelo = new DefaultComboBoxModel<>();
            for (Medico m : medicoServicio.listarActivos()) {
                modelo.addElement(m);
            }
            cboMedico.setModel(modelo);
        } catch (ClinicaException e) {
            mostrarError("No se pudieron cargar los medicos: " + e.getMessage());
        }
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

        int fila = 0;
        agregarCampo(panel, g, fila++, "Paciente:", cboPaciente);
        agregarCampo(panel, g, fila++, "Medico (solo activos):", cboMedico);

        // Horario del medico, debajo del combo, alineado con los campos.
        g.gridx = 1;
        g.gridy = fila++;
        lblHorarioMedico.setForeground(Tema.colores().primario);
        lblHorarioMedico.setFont(Tema.fuenteSubtitulo());
        panel.add(lblHorarioMedico, g);

        agregarCampo(panel, g, fila++, "Fecha (dd/mm/aaaa):", txtFecha);
        agregarCampo(panel, g, fila++, "Hora inicio (HH:mm):", txtHora);
        agregarCampo(panel, g, fila++, "Motivo:", txtMotivo);
        agregarCampo(panel, g, fila++, "Observaciones (opcional):", txtObservaciones);

        return panel;
    }

    private void agregarCampo(JPanel panel, GridBagConstraints g, int fila,
            String etiqueta, Component campo) {
        g.gridx = 0;
        g.gridy = fila;
        JLabel lbl = new JLabel(etiqueta);
        lbl.setForeground(Tema.colores().textoTenue);
        panel.add(lbl, g);
        g.gridx = 1;
        panel.add(campo, g);
    }

    private JPanel construirBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, Tema.ESPACIO_CHICO, Tema.ESPACIO_CHICO));
        panel.setBackground(Tema.colores().superficie);
        BotonEstilizado btnGuardar = new BotonEstilizado("Programar", true);
        BotonEstilizado btnCancelar = new BotonEstilizado("Cancelar", false);
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());
        panel.add(btnGuardar);
        panel.add(btnCancelar);
        return panel;
    }

    private void guardar() {
        Paciente paciente = (Paciente) cboPaciente.getSelectedItem();
        Medico medico = (Medico) cboMedico.getSelectedItem();
        if (paciente == null) {
            mostrarAviso("Debe seleccionar un paciente.");
            return;
        }
        if (medico == null) {
            mostrarAviso("Debe seleccionar un medico. Si no hay medicos, verifique que haya activos.");
            return;
        }

        LocalDate fecha = parsearFecha(txtFecha.getText());
        if (fecha == null) {
            return;
        }
        LocalTime hora = MedicoServicio.parsearHora(txtHora.getText());
        if (hora == null) {
            mostrarAviso("La hora debe tener el formato HH:mm (24 horas), por ejemplo 09:00.");
            return;
        }

        Cita cita = new Cita(null, paciente.getIdentificacion(), medico.getUuid(),
                fecha, hora, txtMotivo.getText().trim(), null,
                txtObservaciones.getText().trim());

        try {
            servicio.programar(cita);
            guardado = true;
            dispose();
        } catch (ClinicaException ex) {
            // Aca caen todas las validaciones cruzadas del servicio.
            mostrarAviso(ex.getMessage());
        }
    }

    private LocalDate parsearFecha(String texto) {
        try {
            return LocalDate.parse(texto.trim(), FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            mostrarAviso("La fecha debe tener el formato dd/mm/aaaa, por ejemplo 25/08/2026.");
            return null;
        }
    }

    private void mostrarAviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Atencion", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public boolean fueGuardado() {
        return guardado;
    }
}
