package com.archivos.sistemagestionclinicamedica.vista.tabla;

import com.archivos.sistemagestionclinicamedica.modelo.Cita;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Conecta una lista de citas con una JTable.
 *
 * Tiene algo distinto a los otros modelos de tabla: la cita guarda el DPI del
 * paciente y el UUID del medico, pero en pantalla queremos mostrar sus nombres.
 * Por eso recibe dos "mapas de nombres" (DPI -> nombre del paciente, UUID en
 * texto -> nombre del medico) que el panel arma antes de cargar la tabla.
 *
 * Si un nombre no esta en el mapa (por ejemplo el registro fue borrado), se
 * muestra la llave como respaldo, para no dejar la celda vacia.
 */
public class TablaCitasModelo extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private final String[] columnas = {
        "Fecha", "Horario", "Paciente", "Medico", "Motivo", "Estado", "Observaciones"
    };

    private transient List<Cita> citas = new ArrayList<>();
    private transient Map<String, String> nombresPaciente = Map.of();
    private transient Map<String, String> nombresMedico = Map.of();

    /**
     * Carga las citas junto con los mapas para traducir llaves a nombres.
     *
     * @param citas lista a mostrar
     * @param nombresPaciente DPI -> nombre del paciente
     * @param nombresMedico UUID (texto) -> nombre del medico
     */
    public void setCitas(List<Cita> citas,
            Map<String, String> nombresPaciente,
            Map<String, String> nombresMedico) {
        this.citas = (citas == null) ? new ArrayList<>() : citas;
        this.nombresPaciente = (nombresPaciente == null) ? Map.of() : nombresPaciente;
        this.nombresMedico = (nombresMedico == null) ? Map.of() : nombresMedico;
        fireTableDataChanged();
    }

    public Cita getCitaEn(int fila) {
        if (fila < 0 || fila >= citas.size()) {
            return null;
        }
        return citas.get(fila);
    }

    @Override
    public int getRowCount() {
        return citas.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int columna) {
        return columnas[columna];
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Cita c = citas.get(fila);
        return switch (columna) {
            case 0 ->
                c.getFechaFormateada();
            case 1 ->
                c.getHorarioFormateado();
            case 2 ->
                nombrePaciente(c);
            case 3 ->
                nombreMedico(c);
            case 4 ->
                c.getMotivo();
            case 5 ->
                c.getEstado();
            case 6 ->
                c.getObservaciones() == null ? "" : c.getObservaciones();
            default ->
                "";
        };
    }

    // Busca el nombre del paciente; si no esta, muestra el DPI como respaldo.
    private String nombrePaciente(Cita c) {
        return nombresPaciente.getOrDefault(
                c.getIdentificacionPaciente(), c.getIdentificacionPaciente());
    }

    // Busca el nombre del medico; si no esta, muestra el UUID corto como respaldo.
    private String nombreMedico(Cita c) {
        String clave = c.getUuidMedico() == null ? "" : c.getUuidMedico().toString();
        return nombresMedico.getOrDefault(clave, c.getUuidCorto());
    }

    @Override
    public boolean isCellEditable(int fila, int columna) {
        return false;
    }
}
