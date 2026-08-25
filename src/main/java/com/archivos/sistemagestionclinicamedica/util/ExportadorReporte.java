package com.archivos.sistemagestionclinicamedica.util;

import com.archivos.sistemagestionclinicamedica.excepcion.PersistenciaException;
import com.archivos.sistemagestionclinicamedica.modelo.Reporte;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Exporta un reporte a un archivo CSV o TXT.
 *
 * Como todos los reportes tienen la misma forma (titulo, columnas, filas), este
 * exportador sirve para los 14 sin saber cual es. Recibe un Reporte y lo
 * escribe; nada mas.
 *
 * El CSV necesita un cuidado especial: si una celda tiene una coma, comillas o
 * un salto de linea, hay que encerrarla entre comillas dobles y duplicar las
 * comillas internas. Si no se hace, las columnas se desalinean al abrir el
 * archivo. El TXT es mas simple: columnas separadas por tabulaciones.
 */
public final class ExportadorReporte {

    private ExportadorReporte() {
        // Clase de utilidad: no se instancia.
    }

    /**
     * Formato de exportacion soportado.
     */
    public enum Formato {
        CSV(".csv"),
        TXT(".txt");

        private final String extension;

        Formato(String extension) {
            this.extension = extension;
        }

        public String getExtension() {
            return extension;
        }
    }

    /**
     * Exporta el reporte al archivo indicado, en el formato dado.
     */
    public static void exportar(Reporte reporte, Path destino, Formato formato)
            throws PersistenciaException {
        try (BufferedWriter escritor = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            if (formato == Formato.CSV) {
                escribirCsv(reporte, escritor);
            } else {
                escribirTxt(reporte, escritor);
            }
        } catch (IOException e) {
            throw new PersistenciaException(
                    "No se pudo exportar el reporte a " + destino + ": " + e.getMessage(), e);
        }
    }

    // --- CSV ---
    private static void escribirCsv(Reporte reporte, BufferedWriter escritor) throws IOException {
        // Encabezado con los nombres de columna.
        escritor.write(unirCsv(reporte.getColumnas()));
        escritor.newLine();
        // Una linea por fila.
        for (List<String> fila : reporte.getFilas()) {
            escritor.write(unirCsv(fila));
            escritor.newLine();
        }
    }

    // Une las celdas de una fila con comas, escapando cada una.
    private static String unirCsv(List<String> celdas) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < celdas.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escaparCsv(celdas.get(i)));
        }
        return sb.toString();
    }

    // Encierra la celda entre comillas si tiene coma, comillas o salto de linea,
    // duplicando las comillas internas.
    private static String escaparCsv(String celda) {
        if (celda == null) {
            return "";
        }
        boolean necesitaComillas = celda.contains(",") || celda.contains("\"")
                || celda.contains("\n") || celda.contains("\r");
        if (!necesitaComillas) {
            return celda;
        }
        String escapada = celda.replace("\"", "\"\"");   // duplicar comillas internas
        return "\"" + escapada + "\"";
    }

    // --- TXT ---
    private static void escribirTxt(Reporte reporte, BufferedWriter escritor) throws IOException {
        // Titulo arriba, para que el TXT sea legible por si solo.
        escritor.write(reporte.getTitulo());
        escritor.newLine();
        escritor.newLine();
        // Encabezado y filas separados por tabulaciones.
        escritor.write(String.join("\t", reporte.getColumnas()));
        escritor.newLine();
        for (List<String> fila : reporte.getFilas()) {
            escritor.write(String.join("\t", fila));
            escritor.newLine();
        }
    }
}
