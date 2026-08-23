package com.archivos.sistemagestionclinicamedica.modelo.enums;

/**
 * Sexo del paciente.
 *
 * Igual que los otros enums, se guarda en el archivo por su posicion (ordinal),
 * asi que no se puede cambiar el orden despues.
 */
public enum Sexo {
    MASCULINO("Masculino"),
    FEMENINO("Femenino");

    private final String descripcion;

    Sexo(String descripcion) {
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
