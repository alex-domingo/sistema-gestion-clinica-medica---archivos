package com.archivos.sistemagestionclinicamedica.vista;

import com.archivos.sistemagestionclinicamedica.excepcion.ClinicaException;
import com.archivos.sistemagestionclinicamedica.modelo.Especialidad;
import com.archivos.sistemagestionclinicamedica.modelo.Medico;
import com.archivos.sistemagestionclinicamedica.modelo.Paciente;
import com.archivos.sistemagestionclinicamedica.modelo.Reporte;
import com.archivos.sistemagestionclinicamedica.modelo.enums.EstadoCita;
import com.archivos.sistemagestionclinicamedica.modelo.enums.TipoSangre;
import com.archivos.sistemagestionclinicamedica.servicio.EspecialidadServicio;
import com.archivos.sistemagestionclinicamedica.servicio.MedicoServicio;
import com.archivos.sistemagestionclinicamedica.servicio.PacienteServicio;
import com.archivos.sistemagestionclinicamedica.servicio.ReporteServicio;
import com.archivos.sistemagestionclinicamedica.util.ExportadorReporte;
import com.archivos.sistemagestionclinicamedica.util.ExportadorReporte.Formato;
import com.archivos.sistemagestionclinicamedica.vista.estilo.BotonEstilizado;
import com.archivos.sistemagestionclinicamedica.vista.estilo.RenderTablaAlterno;
import com.archivos.sistemagestionclinicamedica.vista.estilo.Tema;
import com.archivos.sistemagestionclinicamedica.vista.reporte.TipoReporte;
import com.archivos.sistemagestionclinicamedica.vista.tabla.TablaReporteModelo;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.nio.file.Path;
import java.util.List;

/**
 * Pantalla del modulo de reportes (cuarta pestana).
 *
 * Tiene un selector con los 14 reportes. Segun cual se elija, aparecen los
 * controles del parametro que ese reporte necesita (un combo de tipo de sangre,
 * un campo de fecha, etc.) usando un CardLayout que muestra solo el panel que
 * corresponde. Al generar, arma el Reporte con el servicio y lo muestra en una
 * tabla generica. El boton exportar guarda el reporte actual en CSV o TXT.
 */
@SuppressWarnings("this-escape")
public class PanelReportes extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final transient ReporteServicio reportes;
    private final transient PacienteServicio pacientes;
    private final transient MedicoServicio medicos;
    private final transient EspecialidadServicio especialidades;

    private final JComboBox<TipoReporte> cboReporte = new JComboBox<>(TipoReporte.values());
    private final TablaReporteModelo modeloTabla = new TablaReporteModelo();
    private final JTable tabla = new JTable(modeloTabla);

    // Panel de parametros con CardLayout: se muestra el que pide el reporte.
    private final CardLayout cardParametros = new CardLayout();
    private final JPanel panelParametros = new JPanel(cardParametros);

    // Controles de cada tipo de parametro.
    private final JComboBox<TipoSangre> cboTipoSangre = new JComboBox<>(TipoSangre.values());
    private final JComboBox<Especialidad> cboEspecialidad = new JComboBox<>();
    private final JComboBox<EstadoCita> cboEstado = new JComboBox<>(EstadoCita.values());
    private final JComboBox<Paciente> cboPaciente = new JComboBox<>();
    private final JComboBox<Medico> cboMedico = new JComboBox<>();
    private final JTextField txtFecha = new JTextField(10);
    private final JTextField txtDesde = new JTextField(10);
    private final JTextField txtHasta = new JTextField(10);

    // El ultimo reporte generado, para poder exportarlo.
    private transient Reporte reporteActual;

    public PanelReportes(ReporteServicio reportes, PacienteServicio pacientes,
            MedicoServicio medicos, EspecialidadServicio especialidades) {
        this.reportes = reportes;
        this.pacientes = pacientes;
        this.medicos = medicos;
        this.especialidades = especialidades;

        setLayout(new BorderLayout(Tema.ESPACIO, Tema.ESPACIO));
        setBackground(Tema.colores().fondo);
        setBorder(BorderFactory.createEmptyBorder(
                Tema.ESPACIO, Tema.ESPACIO, Tema.ESPACIO, Tema.ESPACIO));

        add(construirBarraSuperior(), BorderLayout.NORTH);
        add(construirTabla(), BorderLayout.CENTER);
        add(construirBarraInferior(), BorderLayout.SOUTH);

        cargarCombosDeParametros();
        // Mostrar el control del parametro del reporte inicial.
        cboReporte.addActionListener(e -> actualizarControlesParametro());
        actualizarControlesParametro();
    }

    private JScrollPane construirTabla() {
        tabla.setRowHeight(28);
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

    // Barra de arriba: selector de reporte + panel de parametros + boton generar.
    private JPanel construirBarraSuperior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, Tema.ESPACIO_CHICO, 0));
        panel.setOpaque(false);

        JLabel lbl = new JLabel("Reporte:");
        lbl.setForeground(Tema.colores().textoTenue);
        panel.add(lbl);
        panel.add(cboReporte);

        armarPanelParametros();
        panel.add(panelParametros);

        BotonEstilizado btnGenerar = new BotonEstilizado("Generar", true);
        btnGenerar.addActionListener(e -> generar());
        panel.add(btnGenerar);

        return panel;
    }

    // Cada parametro es una "tarjeta" del CardLayout, identificada por el nombre
    // del enum Parametro.
    private void armarPanelParametros() {
        panelParametros.setOpaque(false);

        panelParametros.add(new JPanel(), TipoReporte.Parametro.NINGUNO.name());

        panelParametros.add(conEtiqueta("Tipo de sangre:", cboTipoSangre),
                TipoReporte.Parametro.TIPO_SANGRE.name());
        panelParametros.add(conEtiqueta("Especialidad:", cboEspecialidad),
                TipoReporte.Parametro.ESPECIALIDAD.name());
        panelParametros.add(conEtiqueta("Estado:", cboEstado),
                TipoReporte.Parametro.ESTADO.name());
        panelParametros.add(conEtiqueta("Paciente:", cboPaciente),
                TipoReporte.Parametro.PACIENTE.name());
        panelParametros.add(conEtiqueta("Medico:", cboMedico),
                TipoReporte.Parametro.MEDICO.name());
        panelParametros.add(conEtiqueta("Fecha (dd/mm/aaaa):", txtFecha),
                TipoReporte.Parametro.FECHA.name());

        JPanel rango = new JPanel(new FlowLayout(FlowLayout.LEFT, Tema.ESPACIO_CHICO, 0));
        rango.setOpaque(false);
        JLabel lblD = new JLabel("Desde:");
        lblD.setForeground(Tema.colores().textoTenue);
        JLabel lblH = new JLabel("Hasta:");
        lblH.setForeground(Tema.colores().textoTenue);
        rango.add(lblD);
        rango.add(txtDesde);
        rango.add(lblH);
        rango.add(txtHasta);
        panelParametros.add(rango, TipoReporte.Parametro.RANGO_FECHAS.name());
    }

    private JPanel conEtiqueta(String etiqueta, java.awt.Component control) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, Tema.ESPACIO_CHICO, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(etiqueta);
        l.setForeground(Tema.colores().textoTenue);
        p.add(l);
        p.add(control);
        return p;
    }

    private JPanel construirBarraInferior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, Tema.ESPACIO_CHICO, 0));
        panel.setOpaque(false);
        BotonEstilizado btnCsv = new BotonEstilizado("Exportar CSV", false);
        BotonEstilizado btnTxt = new BotonEstilizado("Exportar TXT", false);
        btnCsv.addActionListener(e -> exportar(Formato.CSV));
        btnTxt.addActionListener(e -> exportar(Formato.TXT));
        panel.add(btnCsv);
        panel.add(btnTxt);
        return panel;
    }

    // Carga los combos de parametros que dependen de datos (especialidad,
    // paciente, medico).
    private void cargarCombosDeParametros() {
        try {
            cboEspecialidad.removeAllItems();
            for (Especialidad e : especialidades.listarTodas()) {
                cboEspecialidad.addItem(e);
            }
            cboPaciente.removeAllItems();
            for (Paciente p : pacientes.listarTodos()) {
                cboPaciente.addItem(p);
            }
            cboMedico.removeAllItems();
            for (Medico m : medicos.listarTodos()) {
                cboMedico.addItem(m);
            }
        } catch (ClinicaException e) {
            mostrarError(e.getMessage());
        }
    }

    // Muestra el control que corresponde al parametro del reporte elegido.
    // Aca se recargan los combos (paciente, medico, especialidad) para que
    // tengan datos frescos si cambiaron en otra pestana. Es el momento correcto:
    // justo cuando el control va a mostrarse, y NO al generar (si se recargara
    // al generar, se perderia la seleccion que el usuario acaba de hacer).
    private void actualizarControlesParametro() {
        TipoReporte tipo = (TipoReporte) cboReporte.getSelectedItem();
        if (tipo != null) {
            cargarCombosDeParametros();
            cardParametros.show(panelParametros, tipo.getParametro().name());
        }
    }

    // Genera el reporte elegido, leyendo el parametro que corresponda.
    private void generar() {
        TipoReporte tipo = (TipoReporte) cboReporte.getSelectedItem();
        if (tipo == null) {
            return;
        }
        try {
            Reporte r = switch (tipo) {
                case PACIENTES_COMPLETO ->
                    reportes.pacientesCompleto();
                case PACIENTES_TIPO_SANGRE ->
                    reportes.pacientesPorTipoSangre(
                    (TipoSangre) cboTipoSangre.getSelectedItem());
                case PACIENTES_MAYOR_CITAS ->
                    reportes.pacientesMayorCitas();
                case PACIENTES_SIN_CITAS ->
                    reportes.pacientesSinCitas();
                case MEDICOS_COMPLETO ->
                    reportes.medicosCompleto();
                case MEDICOS_ESPECIALIDAD ->
                    reportes.medicosPorEspecialidad(
                    nombreEspecialidadElegida());
                case MEDICOS_MAYOR_CITAS ->
                    reportes.medicosMayorCitas();
                case MEDICOS_CITAS_FECHA ->
                    reportes.medicosConCitasEnFecha(leerFecha(txtFecha));
                case CITAS_COMPLETO ->
                    reportes.citasCompleto();
                case CITAS_RANGO ->
                    reportes.citasPorRango(leerFecha(txtDesde), leerFecha(txtHasta));
                case CITAS_MEDICO ->
                    reportes.citasPorMedico(medicoElegido());
                case CITAS_PACIENTE ->
                    reportes.citasPorPaciente(pacienteElegido());
                case CITAS_ESTADO ->
                    reportes.citasPorEstado(
                    (EstadoCita) cboEstado.getSelectedItem());
                case CITAS_POR_ESPECIALIDAD ->
                    reportes.cantidadCitasPorEspecialidad();
                case LOGS ->
                    reportes.logsCompleto();
            };
            reporteActual = r;
            modeloTabla.setReporte(r);
        } catch (DatoParametroInvalido e) {
            mostrarAviso(e.getMessage());
        } catch (ClinicaException e) {
            mostrarError(e.getMessage());
        }
    }

    // --- Lectura de parametros con validacion ---
    private String nombreEspecialidadElegida() throws DatoParametroInvalido {
        Especialidad e = (Especialidad) cboEspecialidad.getSelectedItem();
        if (e == null) {
            throw new DatoParametroInvalido("Seleccione una especialidad.");
        }
        return e.getNombre();
    }

    private java.util.UUID medicoElegido() throws DatoParametroInvalido {
        Medico m = (Medico) cboMedico.getSelectedItem();
        if (m == null) {
            throw new DatoParametroInvalido("Seleccione un medico.");
        }
        return m.getUuid();
    }

    private String pacienteElegido() throws DatoParametroInvalido {
        Paciente p = (Paciente) cboPaciente.getSelectedItem();
        if (p == null) {
            throw new DatoParametroInvalido("Seleccione un paciente.");
        }
        return p.getIdentificacion();
    }

    private LocalDate leerFecha(JTextField campo) throws DatoParametroInvalido {
        try {
            return LocalDate.parse(campo.getText().trim(), FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            throw new DatoParametroInvalido("La fecha debe tener el formato dd/mm/aaaa.");
        }
    }

    // Excepcion interna para avisos de parametros mal ingresados.
    private static final class DatoParametroInvalido extends Exception {

        private static final long serialVersionUID = 1L;

        DatoParametroInvalido(String mensaje) {
            super(mensaje);
        }
    }

    // Exporta el reporte actualmente mostrado.
    private void exportar(Formato formato) {
        if (reporteActual == null) {
            mostrarAviso("Primero genere un reporte para poder exportarlo.");
            return;
        }
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar reporte");
        String ext = formato.getExtension();
        selector.setSelectedFile(new java.io.File("reporte" + ext));
        selector.setFileFilter(new FileNameExtensionFilter(
                formato.name() + " (*" + ext + ")", formato.name().toLowerCase()));

        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path destino = selector.getSelectedFile().toPath();
        // Asegurar la extension.
        if (!destino.toString().toLowerCase().endsWith(ext)) {
            destino = destino.resolveSibling(destino.getFileName() + ext);
        }
        try {
            ExportadorReporte.exportar(reporteActual, destino, formato);
            JOptionPane.showMessageDialog(this,
                    "Reporte exportado a:\n" + destino,
                    "Exportacion exitosa", JOptionPane.INFORMATION_MESSAGE);
        } catch (ClinicaException e) {
            mostrarError(e.getMessage());
        }
    }

    private void mostrarAviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Atencion", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
