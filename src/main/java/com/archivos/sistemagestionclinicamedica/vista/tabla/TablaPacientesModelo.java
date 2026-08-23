package com.archivos.sistemagestionclinicamedica.vista.tabla;

import com.archivos.sistemagestionclinicamedica.modelo.Paciente;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Conecta una lista de pacientes con una JTable.
 *
 * Swing no sabe mostrar una List directamente: necesita un "modelo de tabla"
 * que le responda cuantas filas hay, cuantas columnas, y que valor va en cada
 * celda. Esta clase es ese puente.
 *
 * Se extiende AbstractTableModel, que ya trae casi todo resuelto; nosotros solo
 * completamos los metodos que dicen el tamanio de la tabla y el contenido de
 * cada celda.
 */
public class TablaPacientesModelo extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private final String[] columnas = {
        "Identificacion (DPI)", "Nombres", "Apellidos", "Fecha Nacimiento",
        "Sexo", "Telefono", "Correo", "Tipo Sangre"
    };

    private transient List<Paciente> pacientes = new ArrayList<>();

    // Cambia todos los datos de la tabla y avisa a Swing para que se redibuje.
    public void setPacientes(List<Paciente> pacientes) {
        this.pacientes = (pacientes == null) ? new ArrayList<>() : pacientes;
        fireTableDataChanged();
    }

    // Devuelve el paciente de una fila, para saber cual se selecciono.
    public Paciente getPacienteEn(int fila) {
        if (fila < 0 || fila >= pacientes.size()) {
            return null;
        }
        return pacientes.get(fila);
    }

    @Override
    public int getRowCount() {
        return pacientes.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int columna) {
        return columnas[columna];
    }

    // Dice que texto va en cada celda segun la fila y la columna.
    @Override
    public Object getValueAt(int fila, int columna) {
        Paciente p = pacientes.get(fila);
        return switch (columna) {
            case 0 ->
                p.getIdentificacion();
            case 1 ->
                p.getNombres();
            case 2 ->
                p.getApellidos();
            case 3 ->
                p.getFechaNacimientoFormateada();
            case 4 ->
                p.getSexo();
            case 5 ->
                p.getTelefono();
            case 6 ->
                p.getCorreo() == null ? "" : p.getCorreo();
            case 7 ->
                p.getTipoSangre();
            default ->
                "";
        };
    }

    // Ninguna celda se edita directamente en la tabla: para eso esta el
    // formulario emergente. Asi se evita que el usuario cambie datos sin validar.
    @Override
    public boolean isCellEditable(int fila, int columna) {
        return false;
    }
}
