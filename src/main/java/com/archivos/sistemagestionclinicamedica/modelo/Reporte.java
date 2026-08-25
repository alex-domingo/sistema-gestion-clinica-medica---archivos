package com.archivos.sistemagestionclinicamedica.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Estructura generica de un reporte: un titulo, los nombres de las columnas y
 * las filas (cada fila es una lista de textos, una celda por columna).
 *
 * La gracia de que todos los reportes tengan esta misma forma es que un solo
 * modelo de tabla puede mostrar cualquiera, y un solo exportador puede
 * guardarlos todos en CSV o TXT. No importa si el reporte es de pacientes, de
 * citas o de conteos: para la tabla y el exportador todos se ven igual.
 */
public class Reporte {

    private final String titulo;
    private final List<String> columnas;
    private final List<List<String>> filas = new ArrayList<>();

    public Reporte(String titulo, List<String> columnas) {
        this.titulo = titulo;
        this.columnas = columnas;
    }

    /**
     * Agrega una fila. Recibe las celdas en el mismo orden que las columnas.
     * Los valores nulos se guardan como cadena vacia.
     */
    public void agregarFila(String... celdas) {
        List<String> fila = new ArrayList<>(celdas.length);
        for (String celda : celdas) {
            fila.add(celda == null ? "" : celda);
        }
        filas.add(fila);
    }

    public String getTitulo() {
        return titulo;
    }

    public List<String> getColumnas() {
        return columnas;
    }

    public List<List<String>> getFilas() {
        return filas;
    }

    public int cantidadFilas() {
        return filas.size();
    }
}
