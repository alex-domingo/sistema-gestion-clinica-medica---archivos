package com.archivos.sistemagestionclinicamedica.vista.reporte;

/**
 * Describe cada reporte disponible: su nombre para mostrar en el selector y que
 * tipo de parametro necesita (si es que necesita alguno).
 *
 * Tener esto en un enum evita llenar el panel de reportes con un if gigante: el
 * panel recorre estos valores para armar el selector, y segun el parametro de
 * cada uno decide que control mostrar (un combo, un campo de fecha, etc.).
 */
public enum TipoReporte {

    // --- Pacientes ---
    PACIENTES_COMPLETO("Pacientes: listado completo", Parametro.NINGUNO),
    PACIENTES_TIPO_SANGRE("Pacientes: por tipo de sangre", Parametro.TIPO_SANGRE),
    PACIENTES_MAYOR_CITAS("Pacientes: con mayor cantidad de citas", Parametro.NINGUNO),
    PACIENTES_SIN_CITAS("Pacientes: que nunca han tenido una cita", Parametro.NINGUNO),
    // --- Medicos ---
    MEDICOS_COMPLETO("Medicos: listado completo", Parametro.NINGUNO),
    MEDICOS_ESPECIALIDAD("Medicos: por especialidad", Parametro.ESPECIALIDAD),
    MEDICOS_MAYOR_CITAS("Medicos: con mayor cantidad de citas", Parametro.NINGUNO),
    MEDICOS_CITAS_FECHA("Medicos: con citas programadas en una fecha", Parametro.FECHA),
    // --- Citas ---
    CITAS_COMPLETO("Citas: listado completo", Parametro.NINGUNO),
    CITAS_RANGO("Citas: por rango de fechas", Parametro.RANGO_FECHAS),
    CITAS_MEDICO("Citas: por medico", Parametro.MEDICO),
    CITAS_PACIENTE("Citas: por paciente", Parametro.PACIENTE),
    CITAS_ESTADO("Citas: por estado", Parametro.ESTADO),
    CITAS_POR_ESPECIALIDAD("Citas: cantidad por especialidad", Parametro.NINGUNO),
    // --- Logs ---
    LOGS("Bitacora: todas las interacciones", Parametro.NINGUNO);

    /**
     * Tipo de parametro que pide un reporte para generarse.
     */
    public enum Parametro {
        NINGUNO, TIPO_SANGRE, ESPECIALIDAD, FECHA, RANGO_FECHAS, MEDICO, PACIENTE, ESTADO
    }

    private final String etiqueta;
    private final Parametro parametro;

    TipoReporte(String etiqueta, Parametro parametro) {
        this.etiqueta = etiqueta;
        this.parametro = parametro;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public Parametro getParametro() {
        return parametro;
    }

    // Para que el combo muestre la etiqueta legible.
    @Override
    public String toString() {
        return etiqueta;
    }
}
