package com.archivos.sistemagestionclinicamedica.vista;

import com.archivos.sistemagestionclinicamedica.excepcion.ClinicaException;
import com.archivos.sistemagestionclinicamedica.modelo.Paciente;
import com.archivos.sistemagestionclinicamedica.servicio.PacienteServicio;
import com.archivos.sistemagestionclinicamedica.vista.estilo.BotonEstilizado;
import com.archivos.sistemagestionclinicamedica.vista.estilo.RenderTablaAlterno;
import com.archivos.sistemagestionclinicamedica.vista.estilo.Tema;
import com.archivos.sistemagestionclinicamedica.vista.tabla.TablaPacientesModelo;

import javax.swing.BorderFactory;
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
 * Pantalla del modulo de pacientes.
 *
 * La vista principal es la tabla con todos los pacientes. Arriba hay un
 * buscador y botones. El formulario para crear o editar no esta aca: aparece en
 * una ventana emergente (DialogoPaciente) al presionar Nuevo o Editar.
 *
 * Este panel no valida ni toca archivos: todo se lo pide al PacienteServicio.
 * Su unico trabajo es mostrar datos y pasar las acciones del usuario al
 * servicio.
 */
@SuppressWarnings("this-escape")
public class PanelPacientes extends JPanel {

    private static final long serialVersionUID = 1L;

    private final transient PacienteServicio servicio;
    private final TablaPacientesModelo modeloTabla = new TablaPacientesModelo();
    private final JTable tabla = new JTable(modeloTabla);
    private final JTextField txtBuscar = new JTextField(25);

    public PanelPacientes(PacienteServicio servicio) {
        this.servicio = servicio;
        setLayout(new BorderLayout(Tema.ESPACIO, Tema.ESPACIO));
        setBackground(Tema.colores().fondo);
        setBorder(BorderFactory.createEmptyBorder(
                Tema.ESPACIO, Tema.ESPACIO, Tema.ESPACIO, Tema.ESPACIO));

        add(construirBarraSuperior(), BorderLayout.NORTH);
        add(construirTabla(), BorderLayout.CENTER);
        add(construirBarraBotones(), BorderLayout.SOUTH);

        cargarTodos();
    }

    // Arma la tabla con su estilo y la mete en un panel con scroll.
    private JScrollPane construirTabla() {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(30);
        tabla.setShowVerticalLines(false);           // sin lineas verticales, mas limpio
        tabla.setGridColor(Tema.colores().borde);
        tabla.setSelectionBackground(Tema.colores().primarioSuave);
        tabla.setSelectionForeground(Tema.colores().texto);
        tabla.getTableHeader().setReorderingAllowed(false);
        // Renderizador con filas alternas para todas las columnas.
        tabla.setDefaultRenderer(Object.class, new RenderTablaAlterno());

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.colores().borde));
        scroll.getViewport().setBackground(Tema.colores().superficie);
        return scroll;
    }

    // Barra de arriba: campo de busqueda + boton buscar + boton limpiar.
    private JPanel construirBarraSuperior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, Tema.ESPACIO_CHICO, 0));
        panel.setOpaque(false);

        JLabel etiqueta = new JLabel("Buscar:");
        etiqueta.setForeground(Tema.colores().textoTenue);
        panel.add(etiqueta);

        txtBuscar.setToolTipText("Identificacion, nombre o apellido");
        panel.add(txtBuscar);

        BotonEstilizado btnBuscar = new BotonEstilizado("Buscar", true);
        BotonEstilizado btnLimpiar = new BotonEstilizado("Mostrar todos", false);
        btnBuscar.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> {
            txtBuscar.setText("");
            cargarTodos();
        });

        // Buscar tambien al presionar Enter en el campo.
        txtBuscar.addActionListener(e -> buscar());

        panel.add(btnBuscar);
        panel.add(btnLimpiar);
        return panel;
    }

    // Barra de abajo: los botones de acciones.
    private JPanel construirBarraBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, Tema.ESPACIO_CHICO, 0));
        panel.setOpaque(false);
        // "Nuevo" es la accion principal, va con estilo primario. El resto,
        // secundarios.
        BotonEstilizado btnNuevo = new BotonEstilizado("Nuevo", true);
        BotonEstilizado btnEditar = new BotonEstilizado("Editar", false);
        BotonEstilizado btnEliminar = new BotonEstilizado("Eliminar", false);
        BotonEstilizado btnActualizar = new BotonEstilizado("Actualizar", false);

        btnNuevo.addActionListener(e -> abrirDialogoNuevo());
        btnEditar.addActionListener(e -> abrirDialogoEditar());
        btnEliminar.addActionListener(e -> eliminar());
        btnActualizar.addActionListener(e -> cargarTodos());

        panel.add(btnNuevo);
        panel.add(btnEditar);
        panel.add(btnEliminar);
        panel.add(btnActualizar);
        return panel;
    }

    // Carga todos los pacientes en la tabla.
    private void cargarTodos() {
        try {
            modeloTabla.setPacientes(servicio.listarTodos());
        } catch (ClinicaException e) {
            mostrarError(e.getMessage());
        }
    }

    // Busca por nombre o apellido y muestra los resultados en la tabla.
    private void buscar() {
        try {
            List<Paciente> resultado = servicio.buscar(txtBuscar.getText());
            modeloTabla.setPacientes(resultado);
        } catch (ClinicaException e) {
            mostrarError(e.getMessage());
        }
    }

    private void abrirDialogoNuevo() {
        Frame padre = (Frame) SwingUtilities.getWindowAncestor(this);
        DialogoPaciente dialogo = new DialogoPaciente(padre, servicio, null);
        dialogo.setVisible(true);
        // Al cerrarse, si guardo algo, refrescamos la tabla.
        if (dialogo.fueGuardado()) {
            cargarTodos();
        }
    }

    private void abrirDialogoEditar() {
        Paciente seleccionado = obtenerSeleccionado();
        if (seleccionado == null) {
            return;
        }
        Frame padre = (Frame) SwingUtilities.getWindowAncestor(this);
        DialogoPaciente dialogo = new DialogoPaciente(padre, servicio, seleccionado);
        dialogo.setVisible(true);
        if (dialogo.fueGuardado()) {
            cargarTodos();
        }
    }

    private void eliminar() {
        Paciente seleccionado = obtenerSeleccionado();
        if (seleccionado == null) {
            return;
        }
        // Pedir confirmacion antes de borrar.
        int respuesta = JOptionPane.showConfirmDialog(this,
                "Esta seguro de eliminar al paciente " + seleccionado.getNombreCompleto() + "?",
                "Confirmar eliminacion", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            servicio.eliminar(seleccionado.getIdentificacion());
            cargarTodos();
        } catch (ClinicaException e) {
            mostrarError(e.getMessage());
        }
    }

    // Devuelve el paciente de la fila seleccionada, o avisa si no hay ninguna.
    private Paciente obtenerSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Primero seleccione un paciente de la tabla.",
                    "Sin seleccion", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        // La tabla puede estar filtrada u ordenada; convertimos por las dudas.
        return modeloTabla.getPacienteEn(tabla.convertRowIndexToModel(fila));
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
