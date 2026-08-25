package com.archivos.sistemagestionclinicamedica.vista.tabla;

import com.archivos.sistemagestionclinicamedica.modelo.Reporte;

import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.List;

/**
 * Modelo de tabla que muestra cualquier Reporte.
 *
 * Como todos los reportes tienen la misma estructura (columnas + filas de
 * texto), este unico modelo sirve para los 14. Cuando se elige otro reporte, se
 * le pasa el nuevo Reporte y la tabla se redibuja con otras columnas.
 */
public class TablaReporteModelo extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private transient List<String> columnas = Collections.emptyList();
    private transient List<List<String>> filas = Collections.emptyList();

    public void setReporte(Reporte reporte) {
        if (reporte == null) {
            this.columnas = Collections.emptyList();
            this.filas = Collections.emptyList();
        } else {
            this.columnas = reporte.getColumnas();
            this.filas = reporte.getFilas();
        }
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return filas.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.size();
    }

    @Override
    public String getColumnName(int columna) {
        return columnas.get(columna);
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        return filas.get(fila).get(columna);
    }

    @Override
    public boolean isCellEditable(int fila, int columna) {
        return false;
    }
}
