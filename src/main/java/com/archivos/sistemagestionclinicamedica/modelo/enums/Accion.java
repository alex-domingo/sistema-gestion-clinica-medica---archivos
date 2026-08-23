package com.archivos.sistemagestionclinicamedica.modelo.enums;

/**
 * Tipos de accion que se registran en la bitacora.
 *
 * El orden no se puede cambiar despues (ver la nota en Modulo): en el archivo
 * se guarda por su posicion ordinal.
 */
public enum Accion {
    CREACION("Creacion"),
    CONSULTA("Consulta"),
    ACTUALIZACION("Actualizacion"),
    ELIMINACION("Eliminacion"),
    EXPORTACION("Exportacion"),
    ERROR("Error");

    private final String descripcion;

    Accion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
