package com.archivos.sistemagestionclinicamedica.vista.tabla;

import com.archivos.sistemagestionclinicamedica.modelo.Medico;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Conecta una lista de medicos con una JTable. Mismo rol que
 * TablaPacientesModelo: le dice a Swing cuantas filas y columnas hay y que va
 * en cada celda.
 */
public class TablaMedicosModelo extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private final String[] columnas = {
        "UUID", "Nombres", "Apellidos", "Especialidad",
        "Telefono", "Correo", "Horario Atencion", "Estado"
    };

    private transient List<Medico> medicos = new ArrayList<>();

    public void setMedicos(List<Medico> medicos) {
        this.medicos = (medicos == null) ? new ArrayList<>() : medicos;
        fireTableDataChanged();
    }

    public Medico getMedicoEn(int fila) {
        if (fila < 0 || fila >= medicos.size()) {
            return null;
        }
        return medicos.get(fila);
    }

    @Override
    public int getRowCount() {
        return medicos.size();
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
        Medico m = medicos.get(fila);
        return switch (columna) {
            case 0 ->
                m.getUuidCorto();       // solo los primeros 8 caracteres
            case 1 ->
                m.getNombres();
            case 2 ->
                m.getApellidos();
            case 3 ->
                m.getEspecialidad();
            case 4 ->
                m.getTelefono();
            case 5 ->
                m.getCorreo() == null ? "" : m.getCorreo();
            case 6 ->
                m.getHorarioFormateado();
            case 7 ->
                m.getEstadoTexto();
            default ->
                "";
        };
    }

    @Override
    public boolean isCellEditable(int fila, int columna) {
        return false;
    }
}
