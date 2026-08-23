package com.archivos.sistemagestionclinicamedica.vista;

import com.archivos.sistemagestionclinicamedica.excepcion.ClinicaException;
import com.archivos.sistemagestionclinicamedica.modelo.Paciente;
import com.archivos.sistemagestionclinicamedica.modelo.enums.Sexo;
import com.archivos.sistemagestionclinicamedica.modelo.enums.TipoSangre;
import com.archivos.sistemagestionclinicamedica.servicio.PacienteServicio;
import com.archivos.sistemagestionclinicamedica.vista.estilo.BotonEstilizado;
import com.archivos.sistemagestionclinicamedica.vista.estilo.Tema;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Ventana emergente (modal) con el formulario de un paciente.
 *
 * Sirve para dos cosas: crear un paciente nuevo o editar uno existente. La
 * diferencia la marca el constructor: si recibe un paciente, es edicion y llena
 * los campos; si recibe null, es alta y los deja vacios.
 *
 * Al editar, el campo de identificacion queda bloqueado, porque es la llave y
 * no se puede cambiar (lo explicamos en ArchivoPacientes).
 *
 * El dialogo valida a traves del servicio: si algo esta mal, muestra el mensaje
 * de la excepcion y no se cierra, para que el usuario corrija.
 */
@SuppressWarnings("this-escape")
public class DialogoPaciente extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final transient PacienteServicio servicio;
    private final boolean esEdicion;

    private final JTextField txtIdentificacion = new JTextField(20);
    private final JTextField txtNombres = new JTextField(20);
    private final JTextField txtApellidos = new JTextField(20);
    private final JTextField txtFechaNacimiento = new JTextField(20);
    private final JComboBox<Sexo> cboSexo = new JComboBox<>(Sexo.values());
    private final JTextField txtTelefono = new JTextField(20);
    private final JTextField txtCorreo = new JTextField(20);
    private final JComboBox<TipoSangre> cboTipoSangre = new JComboBox<>(TipoSangre.values());

    // Queda en true si se guardo con exito, para que el panel sepa si refrescar.
    private boolean guardado = false;

    public DialogoPaciente(Frame padre, PacienteServicio servicio, Paciente pacienteEditar) {
        super(padre, true);   // true = modal: bloquea la ventana de atras
        this.servicio = servicio;
        this.esEdicion = (pacienteEditar != null);

        setTitle(esEdicion ? "Editar paciente" : "Nuevo paciente");
        setLayout(new BorderLayout());
        add(construirFormulario(), BorderLayout.CENTER);
        add(construirBotones(), BorderLayout.SOUTH);

        if (esEdicion) {
            llenarCampos(pacienteEditar);
            txtIdentificacion.setEnabled(false);   // la llave no se cambia
        }

        pack();
        setLocationRelativeTo(padre);   // centrar sobre la ventana principal
    }

    // Arma la grilla de etiquetas + campos.
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
        agregarCampo(panel, g, fila++, "Identificacion (13 digitos):", txtIdentificacion);
        agregarCampo(panel, g, fila++, "Nombres:", txtNombres);
        agregarCampo(panel, g, fila++, "Apellidos:", txtApellidos);
        agregarCampo(panel, g, fila++, "Fecha nacimiento (dd/mm/aaaa):", txtFechaNacimiento);
        agregarCampo(panel, g, fila++, "Sexo:", cboSexo);
        agregarCampo(panel, g, fila++, "Telefono:", txtTelefono);
        agregarCampo(panel, g, fila++, "Correo (opcional):", txtCorreo);
        agregarCampo(panel, g, fila++, "Tipo de sangre:", cboTipoSangre);

        return panel;
    }

    // Agrega una fila del formulario: etiqueta a la izquierda, campo a la derecha.
    private void agregarCampo(JPanel panel, GridBagConstraints g, int fila,
            String etiqueta, java.awt.Component campo) {
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
        BotonEstilizado btnGuardar = new BotonEstilizado("Guardar", true);
        BotonEstilizado btnCancelar = new BotonEstilizado("Cancelar", false);

        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());

        panel.add(btnGuardar);
        panel.add(btnCancelar);
        return panel;
    }

    // Llena los campos con los datos de un paciente (modo edicion).
    private void llenarCampos(Paciente p) {
        txtIdentificacion.setText(p.getIdentificacion());
        txtNombres.setText(p.getNombres());
        txtApellidos.setText(p.getApellidos());
        txtFechaNacimiento.setText(p.getFechaNacimientoFormateada());
        cboSexo.setSelectedItem(p.getSexo());
        txtTelefono.setText(p.getTelefono());
        txtCorreo.setText(p.getCorreo());
        cboTipoSangre.setSelectedItem(p.getTipoSangre());
    }

    // Toma lo que escribio el usuario, lo arma en un Paciente y lo manda al
    // servicio. Si el servicio se queja, muestra el error y no cierra.
    private void guardar() {
        LocalDate fecha = parsearFecha(txtFechaNacimiento.getText());
        if (fecha == null) {
            return;   // parsearFecha ya mostro el mensaje
        }

        Paciente p = new Paciente(
                txtIdentificacion.getText().trim(),
                txtNombres.getText().trim(),
                txtApellidos.getText().trim(),
                fecha,
                (Sexo) cboSexo.getSelectedItem(),
                txtTelefono.getText().trim(),
                txtCorreo.getText().trim(),
                (TipoSangre) cboTipoSangre.getSelectedItem());

        try {
            if (esEdicion) {
                servicio.modificar(p);
            } else {
                servicio.registrar(p);
            }
            guardado = true;
            dispose();
        } catch (ClinicaException ex) {
            // Aca cae cualquier error del sistema (validacion, duplicado, etc.)
            // El mensaje ya viene listo para mostrar.
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "No se pudo guardar", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Convierte el texto de fecha a LocalDate. Si el formato esta mal, avisa.
    private LocalDate parsearFecha(String texto) {
        try {
            return LocalDate.parse(texto.trim(), FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "La fecha debe tener el formato dd/mm/aaaa, por ejemplo 15/05/1990.",
                    "Fecha invalida", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    public boolean fueGuardado() {
        return guardado;
    }
}
