package com.archivos.sistemagestionclinicamedica.vista.estilo;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

/**
 * Da estilo a cada celda de una tabla.
 *
 * Hace tres cosas para que la tabla se vea limpia: - pinta las filas con
 * colores alternados (una blanca, una gris muy clarito) - resalta la fila
 * seleccionada con el color teal suave - agrega un pequeno margen a la
 * izquierda para que el texto no quede pegado
 *
 * Se usa poniendo esta clase como renderizador de la tabla.
 */
public class RenderTablaAlterno extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(JTable tabla, Object valor,
            boolean seleccionada, boolean enfocada, int fila, int columna) {

        Component c = super.getTableCellRendererComponent(
                tabla, valor, seleccionada, enfocada, fila, columna);

        Tema.Paleta colores = Tema.colores();

        if (seleccionada) {
            c.setBackground(colores.primarioSuave);
            c.setForeground(colores.texto);
        } else {
            // Filas pares e impares con distinto fondo.
            c.setBackground(fila % 2 == 0 ? colores.superficie : colores.filaAlterna);
            c.setForeground(colores.texto);
        }

        // Margen interno a la izquierda.
        setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        return c;
    }
}
