package com.archivos.sistemagestionclinicamedica.vista;

import com.archivos.sistemagestionclinicamedica.excepcion.ClinicaException;
import com.archivos.sistemagestionclinicamedica.modelo.Especialidad;
import com.archivos.sistemagestionclinicamedica.modelo.Medico;
import com.archivos.sistemagestionclinicamedica.servicio.EspecialidadServicio;
import com.archivos.sistemagestionclinicamedica.servicio.MedicoServicio;
import com.archivos.sistemagestionclinicamedica.vista.estilo.BotonEstilizado;
import com.archivos.sistemagestionclinicamedica.vista.estilo.RenderTablaAlterno;
import com.archivos.sistemagestionclinicamedica.vista.estilo.Tema;
import com.archivos.sistemagestionclinicamedica.vista.tabla.TablaMedicosModelo;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
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
import java.util.List;

/**
 * Pantalla del modulo de medicos.
 *
 * Misma idea que PanelPacientes: tabla como vista principal, formulario en
 * ventana emergente. Ademas trae un filtro por estado (todos/activos/inactivos)
 * y por especialidad, y un boton para activar o desactivar al medico elegido.
 */
@SuppressWarnings("this-escape")
public class PanelMedicos extends JPanel {

    private static final long serialVersionUID = 1L;

    private final transient MedicoServicio servicio;
    private final transient EspecialidadServicio especialidades;
    private final TablaMedicosModelo modeloTabla = new TablaMedicosModelo();
    private final JTable tabla = new JTable(modeloTabla);
    private final JTextField txtBuscar = new JTextField(20);
    private final JComboBox<String> cboEstado
            = new JComboBox<>(new String[]{"Todos", "Activos", "Inactivos"});
    private final JComboBox<String> cboEspecialidad = new JComboBox<>();

    public PanelMedicos(MedicoServicio servicio, EspecialidadServicio especialidades) {
        this.servicio = servicio;
        this.especialidades = especialidades;
        setLayout(new BorderLayout(Tema.ESPACIO, Tema.ESPACIO));
        setBackground(Tema.colores().fondo);
        setBorder(BorderFactory.createEmptyBorder(
                Tema.ESPACIO, Tema.ESPACIO, Tema.ESPACIO, Tema.ESPACIO));

        add(construirBarraSuperior(), BorderLayout.NORTH);
        add(construirTabla(), BorderLayout.CENTER);
        add(construirBarraBotones(), BorderLayout.SOUTH);

        cargarEspecialidadesFiltro();
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

    // Barra de arriba: buscador + filtro de estado + filtro de especialidad.
    private JPanel construirBarraSuperior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, Tema.ESPACIO_CHICO, 0));
        panel.setOpaque(false);

        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setForeground(Tema.colores().textoTenue);
        panel.add(lblBuscar);
        txtBuscar.setToolTipText("UUID, nombre, apellido o especialidad");
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

        JLabel lblEsp = new JLabel("  Especialidad:");
        lblEsp.setForeground(Tema.colores().textoTenue);
        panel.add(lblEsp);
        cboEspecialidad.addActionListener(e -> aplicarFiltros());
        panel.add(cboEspecialidad);

        return panel;
    }

    private JPanel construirBarraBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, Tema.ESPACIO_CHICO, 0));
        panel.setOpaque(false);
        BotonEstilizado btnNuevo = new BotonEstilizado("Nuevo", true);
        BotonEstilizado btnEditar = new BotonEstilizado("Editar", false);
        BotonEstilizado btnEstado = new BotonEstilizado("Activar/Desactivar", false);
        BotonEstilizado btnEliminar = new BotonEstilizado("Eliminar", false);

        btnNuevo.addActionListener(e -> abrirDialogoNuevo());
        btnEditar.addActionListener(e -> abrirDialogoEditar());
        btnEstado.addActionListener(e -> alternarEstado());
        btnEliminar.addActionListener(e -> eliminar());

        panel.add(btnNuevo);
        panel.add(btnEditar);
        panel.add(btnEstado);
        panel.add(btnEliminar);
        return panel;
    }

    // Carga el combo de especialidades del filtro, con "Todas" al inicio.
    private void cargarEspecialidadesFiltro() {
        try {
            DefaultComboBoxModel<String> modelo = new DefaultComboBoxModel<>();
            modelo.addElement("Todas");
            for (Especialidad e : especialidades.listarTodas()) {
                modelo.addElement(e.getNombre());
            }
            cboEspecialidad.setModel(modelo);
        } catch (ClinicaException e) {
            mostrarError(e.getMessage());
        }
    }

    private void cargarTodos() {
        try {
            modeloTabla.setMedicos(servicio.listarTodos());
        } catch (ClinicaException e) {
            mostrarError(e.getMessage());
        }
    }

    // Aplica los dos filtros (estado y especialidad) combinados.
    private void aplicarFiltros() {
        try {
            String estado = (String) cboEstado.getSelectedItem();
            String especialidad = (String) cboEspecialidad.getSelectedItem();

            // Punto de partida segun el estado elegido.
            List<Medico> lista = switch (estado) {
                case "Activos" ->
                    servicio.listarActivos();
                case "Inactivos" ->
                    servicio.listarInactivos();
                default ->
                    servicio.listarTodos();
            };

            // Si hay una especialidad concreta, filtrar ademas por ella.
            if (especialidad != null && !"Todas".equals(especialidad)) {
                lista = lista.stream()
                        .filter(m -> especialidad.equalsIgnoreCase(m.getEspecialidad()))
                        .toList();
            }
            modeloTabla.setMedicos(lista);
        } catch (ClinicaException e) {
            mostrarError(e.getMessage());
        }
    }

    private void buscar() {
        try {
            modeloTabla.setMedicos(servicio.buscar(txtBuscar.getText()));
        } catch (ClinicaException e) {
            mostrarError(e.getMessage());
        }
    }

    private void abrirDialogoNuevo() {
        Frame padre = (Frame) SwingUtilities.getWindowAncestor(this);
        DialogoMedico dialogo = new DialogoMedico(padre, servicio, especialidades, null);
        dialogo.setVisible(true);
        if (dialogo.fueGuardado()) {
            cargarEspecialidadesFiltro();   // por si se agrego una especialidad nueva
            cargarTodos();
        }
    }

    private void abrirDialogoEditar() {
        Medico seleccionado = obtenerSeleccionado();
        if (seleccionado == null) {
            return;
        }
        Frame padre = (Frame) SwingUtilities.getWindowAncestor(this);
        DialogoMedico dialogo = new DialogoMedico(padre, servicio, especialidades, seleccionado);
        dialogo.setVisible(true);
        if (dialogo.fueGuardado()) {
            cargarEspecialidadesFiltro();
            cargarTodos();
        }
    }

    // Alterna el estado del medico seleccionado (activo <-> inactivo).
    private void alternarEstado() {
        Medico seleccionado = obtenerSeleccionado();
        if (seleccionado == null) {
            return;
        }
        try {
            servicio.cambiarEstado(seleccionado.getUuid(), !seleccionado.isActivo());
            cargarTodos();
        } catch (ClinicaException e) {
            mostrarError(e.getMessage());
        }
    }

    private void eliminar() {
        Medico seleccionado = obtenerSeleccionado();
        if (seleccionado == null) {
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Esta seguro de eliminar al medico " + seleccionado.getNombreCompleto() + "?",
                "Confirmar eliminacion", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            servicio.eliminar(seleccionado.getUuid());
            cargarTodos();
        } catch (ClinicaException e) {
            mostrarError(e.getMessage());
        }
    }

    private Medico obtenerSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Primero seleccione un medico de la tabla.",
                    "Sin seleccion", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return modeloTabla.getMedicoEn(tabla.convertRowIndexToModel(fila));
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
