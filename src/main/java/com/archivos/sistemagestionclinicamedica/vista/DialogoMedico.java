package com.archivos.sistemagestionclinicamedica.vista;

import com.archivos.sistemagestionclinicamedica.excepcion.ClinicaException;
import com.archivos.sistemagestionclinicamedica.modelo.Especialidad;
import com.archivos.sistemagestionclinicamedica.modelo.Medico;
import com.archivos.sistemagestionclinicamedica.servicio.EspecialidadServicio;
import com.archivos.sistemagestionclinicamedica.servicio.MedicoServicio;
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
import java.time.LocalTime;
import java.util.List;

/**
 * Ventana emergente con el formulario de un medico.
 *
 * Crea uno nuevo o edita uno existente, igual que DialogoPaciente. Lo especial
 * aca es la especialidad: se elige de un combo cargado desde el catalogo, y hay
 * un boton "+ Nueva" al lado para agregar una especialidad que no este en la
 * lista, sin salir del formulario.
 *
 * El UUID no se muestra como campo editable (lo genera el sistema). En modo
 * edicion se muestra como texto de solo lectura.
 */
@SuppressWarnings("this-escape")
public class DialogoMedico extends JDialog {

    private static final long serialVersionUID = 1L;

    private final transient MedicoServicio servicio;
    private final transient EspecialidadServicio especialidades;
    private final boolean esEdicion;
    private final transient Medico medicoEditar;

    private final JTextField txtNombres = new JTextField(20);
    private final JTextField txtApellidos = new JTextField(20);
    private final JComboBox<Especialidad> cboEspecialidad = new JComboBox<>();
    private final JTextField txtTelefono = new JTextField(20);
    private final JTextField txtCorreo = new JTextField(20);
    private final JTextField txtHoraInicio = new JTextField(20);
    private final JTextField txtHoraFin = new JTextField(20);
    private final JComboBox<String> cboEstado = new JComboBox<>(new String[]{"Activo", "Inactivo"});

    private boolean guardado = false;

    public DialogoMedico(Frame padre, MedicoServicio servicio,
            EspecialidadServicio especialidades, Medico medicoEditar) {
        super(padre, true);
        this.servicio = servicio;
        this.especialidades = especialidades;
        this.esEdicion = (medicoEditar != null);
        this.medicoEditar = medicoEditar;

        setTitle(esEdicion ? "Editar medico" : "Nuevo medico");
        setLayout(new BorderLayout());
        cargarEspecialidades(null);
        add(construirFormulario(), BorderLayout.CENTER);
        add(construirBotones(), BorderLayout.SOUTH);

        if (esEdicion) {
            llenarCampos(medicoEditar);
        }

        pack();
        setLocationRelativeTo(padre);
    }

    // Carga el combo con las especialidades del catalogo. Si se le pasa un
    // nombre, deja esa seleccionada (util despues de agregar una nueva).
    private void cargarEspecialidades(String seleccionar) {
        try {
            List<Especialidad> lista = especialidades.listarTodas();
            DefaultComboBoxModel<Especialidad> modelo = new DefaultComboBoxModel<>();
            for (Especialidad e : lista) {
                modelo.addElement(e);
            }
            cboEspecialidad.setModel(modelo);

            if (seleccionar != null) {
                for (int i = 0; i < modelo.getSize(); i++) {
                    if (modelo.getElementAt(i).getNombre().equalsIgnoreCase(seleccionar)) {
                        cboEspecialidad.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (ClinicaException e) {
            JOptionPane.showMessageDialog(this,
                    "No se pudieron cargar las especialidades: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
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
        agregarCampo(panel, g, fila++, "Nombres:", txtNombres);
        agregarCampo(panel, g, fila++, "Apellidos:", txtApellidos);
        // La fila de especialidad es especial: combo + boton "+ Nueva".
        agregarEspecialidad(panel, g, fila++);
        agregarCampo(panel, g, fila++, "Telefono:", txtTelefono);
        agregarCampo(panel, g, fila++, "Correo (opcional):", txtCorreo);
        agregarCampo(panel, g, fila++, "Hora inicio (HH:mm):", txtHoraInicio);
        agregarCampo(panel, g, fila++, "Hora fin (HH:mm):", txtHoraFin);
        agregarCampo(panel, g, fila++, "Estado:", cboEstado);

        return panel;
    }

    private void agregarCampo(JPanel panel, GridBagConstraints g, int fila,
            String etiqueta, Component campo) {
        g.gridx = 0;
        g.gridy = fila;
        g.gridwidth = 1;
        JLabel lbl = new JLabel(etiqueta);
        lbl.setForeground(Tema.colores().textoTenue);
        panel.add(lbl, g);
        g.gridx = 1;
        panel.add(campo, g);
    }

    // Fila de especialidad: el combo y, al lado, el boton para agregar una nueva.
    private void agregarEspecialidad(JPanel panel, GridBagConstraints g, int fila) {
        g.gridx = 0;
        g.gridy = fila;
        JLabel lbl = new JLabel("Especialidad:");
        lbl.setForeground(Tema.colores().textoTenue);
        panel.add(lbl, g);

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, Tema.ESPACIO_CHICO, 0));
        fila2.setOpaque(false);
        fila2.add(cboEspecialidad);
        BotonEstilizado btnNueva = new BotonEstilizado("+ Nueva", false);
        btnNueva.addActionListener(e -> agregarEspecialidadNueva());
        fila2.add(btnNueva);

        g.gridx = 1;
        panel.add(fila2, g);
    }

    // Pide el nombre de una especialidad nueva, la agrega al catalogo y la deja
    // seleccionada en el combo. Resuelve el caso de un medico con especialidad
    // que no estaba en la lista.
    private void agregarEspecialidadNueva() {
        String nombre = JOptionPane.showInputDialog(this,
                "Nombre de la nueva especialidad:", "Agregar especialidad",
                JOptionPane.PLAIN_MESSAGE);
        if (nombre == null || nombre.isBlank()) {
            return;   // el usuario cancelo o no escribio nada
        }
        try {
            especialidades.agregar(nombre.trim());
            cargarEspecialidades(nombre.trim());   // recargar y dejarla elegida
        } catch (ClinicaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "No se pudo agregar", JOptionPane.WARNING_MESSAGE);
        }
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

    private void llenarCampos(Medico m) {
        txtNombres.setText(m.getNombres());
        txtApellidos.setText(m.getApellidos());
        cargarEspecialidades(m.getEspecialidad());
        txtTelefono.setText(m.getTelefono());
        txtCorreo.setText(m.getCorreo());
        txtHoraInicio.setText(m.getHoraInicioFormateada());
        txtHoraFin.setText(m.getHoraFinFormateada());
        cboEstado.setSelectedItem(m.isActivo() ? "Activo" : "Inactivo");
    }

    private void guardar() {
        // Parsear las horas; si el formato esta mal, avisar y no seguir.
        LocalTime horaInicio = MedicoServicio.parsearHora(txtHoraInicio.getText());
        LocalTime horaFin = MedicoServicio.parsearHora(txtHoraFin.getText());
        if (horaInicio == null || horaFin == null) {
            JOptionPane.showMessageDialog(this,
                    "Las horas deben tener el formato HH:mm (24 horas), por ejemplo 08:00.",
                    "Horario invalido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Especialidad esp = (Especialidad) cboEspecialidad.getSelectedItem();
        if (esp == null) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar una especialidad.",
                    "Falta especialidad", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean activo = "Activo".equals(cboEstado.getSelectedItem());

        try {
            if (esEdicion) {
                // Conservar el UUID del medico que se edita.
                Medico m = new Medico(medicoEditar.getUuid(),
                        txtNombres.getText().trim(), txtApellidos.getText().trim(),
                        esp.getNombre(), txtTelefono.getText().trim(),
                        txtCorreo.getText().trim(), horaInicio, horaFin, activo);
                servicio.modificar(m);
            } else {
                // UUID null: lo genera el servicio al registrar.
                Medico m = new Medico(null,
                        txtNombres.getText().trim(), txtApellidos.getText().trim(),
                        esp.getNombre(), txtTelefono.getText().trim(),
                        txtCorreo.getText().trim(), horaInicio, horaFin, activo);
                servicio.registrar(m);
            }
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
