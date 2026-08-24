package com.archivos.sistemagestionclinicamedica.modelo.enums;

/**
 * Estados por los que pasa una cita.
 *
 * Se guarda por ordinal en el archivo, asi que no cambiar el orden.
 */
public enum EstadoCita {
    PROGRAMADA("Programada"),
    ATENDIDA("Atendida"),
    CANCELADA("Cancelada");

    private final String descripcion;

    EstadoCita(String descripcion) {
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
