package com.archivos.sistemagestionclinicamedica.modelo.enums;

/**
 * Los 8 tipos de sangre posibles.
 *
 * Se guarda por ordinal en el archivo, no cambiar el orden despues. La etiqueta
 * ("A+", "O-", etc.) es lo que se muestra en la interfaz.
 */
public enum TipoSangre {
    A_POSITIVO("A+"),
    A_NEGATIVO("A-"),
    B_POSITIVO("B+"),
    B_NEGATIVO("B-"),
    AB_POSITIVO("AB+"),
    AB_NEGATIVO("AB-"),
    O_POSITIVO("O+"),
    O_NEGATIVO("O-");

    private final String etiqueta;

    TipoSangre(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
