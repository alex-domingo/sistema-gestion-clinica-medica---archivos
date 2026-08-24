package com.archivos.sistemagestionclinicamedica.vista;

import com.archivos.sistemagestionclinicamedica.excepcion.ClinicaException;
import com.archivos.sistemagestionclinicamedica.modelo.Cita;
import com.archivos.sistemagestionclinicamedica.modelo.Medico;
import com.archivos.sistemagestionclinicamedica.modelo.Paciente;
import com.archivos.sistemagestionclinicamedica.modelo.enums.EstadoCita;
import com.archivos.sistemagestionclinicamedica.servicio.CitaServicio;
import com.archivos.sistemagestionclinicamedica.servicio.MedicoServicio;
import com.archivos.sistemagestionclinicamedica.servicio.PacienteServicio;
import com.archivos.sistemagestionclinicamedica.vista.estilo.BotonEstilizado;
import com.archivos.sistemagestionclinicamedica.vista.estilo.RenderTablaAlterno;
import com.archivos.sistemagestionclinicamedica.vista.estilo.Tema;
import com.archivos.sistemagestionclinicamedica.vista.tabla.TablaCitasModelo;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pantalla del modulo de citas.
 *
 * Es la que junta las tres entidades: muestra las citas con los nombres del
 * paciente y del medico (no sus llaves), y permite programar, cancelar, marcar
 * atendida, editar el motivo y eliminar.
 *
 * Antes de llenar la tabla, arma dos mapas (DPI -> nombre paciente, UUID ->
 * nombre medico) para que el modelo de tabla pueda mostrar nombres.
 */
@SuppressWarnings("this-escape")
public class PanelCitas extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final transient CitaServicio servicio;
    private final transient PacienteServicio pacienteServicio;
    private final transient MedicoServicio medicoServicio;

    private final TablaCitasModelo modeloTabla = new TablaCitasModelo();
    private final JTable tabla = new JTable(modeloTabla);
    private final JComboBox<String> cboEstado
            = new JComboBox<>(new String[]{"Todas", "Programadas", "Atendidas", "Canceladas"});
    private final JTextField txtFecha = new JTextField(10);
    private final JTextField txtBuscar = new JTextField(18);

    public PanelCitas(CitaServicio servicio, PacienteServicio pacienteServicio,
            MedicoServicio medicoServicio) {
        this.servicio = servicio;
        this.pacienteServicio = pacienteServicio;
        this.medicoServicio = medicoServicio;
        setLayout(new BorderLayout(Tema.ESPACIO, Tema.ESPACIO));
        setBackground(Tema.colores().fondo);
        setBorder(BorderFactory.createEmptyBorder(
                Tema.ESPACIO, Tema.ESPACIO, Tema.ESPACIO, Tema.ESPACIO));

        add(construirBarraSuperior(), BorderLayout.NORTH);
        add(construirTabla(), BorderLayout.CENTER);
        add(construirBarraBotones(), BorderLayout.SOUTH);

        cargarTodos();
    }

    private JScrollPane construirTabla() {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(30);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(Tema.colores().borde);
        tabla.setSelectionBackground(Tema.colores().primarioSuave);
        tabla.setSelectionForeground(Tema.colores().texto);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setDefaultRenderer(Object.class, new RenderTablaAlterno());

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.colores().borde));
        scroll.getViewport().setBackground(Tema.colores().superficie);
        return scroll;
    }

    private JPanel construirBarraSuperior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, Tema.ESPACIO_CHICO, 0));
        panel.setOpaque(false);

        // Buscador unificado: UUID de la cita, nombre del paciente o del medico.
        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setForeground(Tema.colores().textoTenue);
        panel.add(lblBuscar);
        txtBuscar.setToolTipText("UUID de la cita, nombre del paciente o del medico");
        panel.add(txtBuscar);
        BotonEstilizado btnBuscar = new BotonEstilizado("Buscar", true);
        btnBuscar.addActionListener(e -> buscar());
        txtBuscar.addActionListener(e -> buscar());
        panel.add(btnBuscar);

        JLabel lblEstado = new JLabel("  Estado:");
        lblEstado.setForeground(Tema.colores().textoTenue);
        panel.add(lblEstado);
        cboEstado.addActionListener(e -> aplicarFiltros());
        panel.add(cboEstado);

        JLabel lblFecha = new JLabel("  Fecha (dd/mm/aaaa):");
        lblFecha.setForeground(Tema.colores().textoTenue);
        panel.add(lblFecha);
        txtFecha.setToolTipText("Filtrar por una fecha concreta");
        panel.add(txtFecha);
        BotonEstilizado btnFecha = new BotonEstilizado("Filtrar fecha", false);
        btnFecha.addActionListener(e -> filtrarPorFecha());
        panel.add(btnFecha);
        BotonEstilizado btnLimpiar = new BotonEstilizado("Mostrar todas", false);
        btnLimpiar.addActionListener(e -> {
            txtBuscar.setText("");
            txtFecha.setText("");
            cboEstado.setSelectedIndex(0);
            cargarTodos();
        });
        panel.add(btnLimpiar);

        return panel;
    }

    // Busca por UUID de la cita, nombre del paciente o del medico.
    private void buscar() {
        try {
            mostrar(servicio.buscar(txtBuscar.getText()));
        } catch (ClinicaException e) {
            mostrarError(e.getMessage());
        }
    }

    private JPanel construirBarraBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, Tema.ESPACIO_CHICO, 0));
        panel.setOpaque(false);
        BotonEstilizado btnNueva = new BotonEstilizado("Programar", true);
        BotonEstilizado btnAtender = new BotonEstilizado("Marcar atendida", false);
        BotonEstilizado btnCancelar = new BotonEstilizado("Cancelar cita", false);
        BotonEstilizado btnEditar = new BotonEstilizado("Editar motivo", false);
        BotonEstilizado btnEliminar = new BotonEstilizado("Eliminar", false);

        btnNueva.addActionListener(e -> programar());
        btnAtender.addActionListener(e -> marcarAtendida());
        btnCancelar.addActionListener(e -> cancelar());
        btnEditar.addActionListener(e -> editarMotivo());
        btnEliminar.addActionListener(e -> eliminar());

        panel.add(btnNueva);
        panel.add(btnAtender);
        panel.add(btnCancelar);
        panel.add(btnEditar);
        panel.add(btnEliminar);
        return panel;
    }

    // Arma los mapas de nombres y carga la tabla con la lista dada.
    private void mostrar(List<Cita> citas) {
        try {
            Map<String, String> nombresPac = new HashMap<>();
            for (Paciente p : pacienteServicio.listarTodos()) {
                nombresPac.put(p.getIdentificacion(), p.getNombreCompleto());
            }
            Map<String, String> nombresMed = new HashMap<>();
            for (Medico m : medicoServicio.listarTodos()) {
                nombresMed.put(m.getUuid().toString(), m.getNombreCompleto());
            }
            modeloTabla.setCitas(citas, nombresPac, nombresMed);
        } catch (ClinicaException e) {
            mostrarError(e.getMessage());
        }
    }

    private void cargarTodos() {
        try {
            mostrar(servicio.listarTodos());
        } catch (ClinicaException e) {
            mostrarError(e.getMessage());
        }
    }

    private void aplicarFiltros() {
        try {
            String estado = (String) cboEstado.getSelectedItem();
            List<Cita> lista = switch (estado) {
                case "Programadas" ->
                    servicio.listarPorEstado(EstadoCita.PROGRAMADA);
                case "Atendidas" ->
                    servicio.listarPorEstado(EstadoCita.ATENDIDA);
                case "Canceladas" ->
                    servicio.listarPorEstado(EstadoCita.CANCELADA);
                default ->
                    servicio.listarTodos();
            };
            mostrar(lista);
        } catch (ClinicaException e) {
            mostrarError(e.getMessage());
        }
    }

    private void filtrarPorFecha() {
        String texto = txtFecha.getText().trim();
        if (texto.isEmpty()) {
            cargarTodos();
            return;
        }
        try {
            LocalDate fecha = LocalDate.parse(texto, FORMATO_FECHA);
            mostrar(servicio.listarPorFecha(fecha));
        } catch (DateTimeParseException e) {
            mostrarAviso("La fecha debe tener el formato dd/mm/aaaa.");
        } catch (ClinicaException e) {
            mostrarError(e.getMessage());
        }
    }

    private void programar() {
        Frame padre = (Frame) SwingUtilities.getWindowAncestor(this);
        DialogoCita dialogo = new DialogoCita(padre, servicio, pacienteServicio, medicoServicio);
        dialogo.setVisible(true);
        if (dialogo.fueGuardado()) {
            cargarTodos();
        }
    }

    private void marcarAtendida() {
        Cita cita = obtenerSeleccionada();
        if (cita == null) {
            return;
        }
        try {
            servicio.marcarAtendida(cita.getUuid());
            cargarTodos();
        } catch (ClinicaException e) {
            mostrarAviso(e.getMessage());
        }
    }

    private void cancelar() {
        Cita cita = obtenerSeleccionada();
        if (cita == null) {
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Cancelar la cita del " + cita.getFechaFormateada()
                + " a las " + cita.getHoraInicioFormateada() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            servicio.cancelar(cita.getUuid());
            cargarTodos();
        } catch (ClinicaException e) {
            mostrarAviso(e.getMessage());
        }
    }

    private void editarMotivo() {
        Cita cita = obtenerSeleccionada();
        if (cita == null) {
            return;
        }
        Frame padre = (Frame) SwingUtilities.getWindowAncestor(this);
        DialogoEditarCita dialogo = new DialogoEditarCita(padre, servicio, cita);
        dialogo.setVisible(true);
        if (dialogo.fueGuardado()) {
            cargarTodos();
        }
    }

    private void eliminar() {
        Cita cita = obtenerSeleccionada();
        if (cita == null) {
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Eliminar definitivamente esta cita?",
                "Confirmar eliminacion", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            servicio.eliminar(cita.getUuid());
            cargarTodos();
        } catch (ClinicaException e) {
            mostrarError(e.getMessage());
        }
    }

    private Cita obtenerSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Primero seleccione una cita de la tabla.",
                    "Sin seleccion", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return modeloTabla.getCitaEn(tabla.convertRowIndexToModel(fila));
    }

    private void mostrarAviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Atencion", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
