package com.archivos.sistemagestionclinicamedica.modelo;

/**
 * Una especialidad medica del catalogo.
 *
 * Antes esto era un enum (lista fija en el codigo), pero ahora es una entidad
 * propia guardada en su archivo, para que el usuario pueda agregar nuevas
 * especialidades sin tocar el programa.
 *
 * El nombre es la llave: no puede haber dos especialidades con el mismo nombre.
 * El medico guarda el nombre de su especialidad como referencia a este
 * catalogo.
 */
public class Especialidad {

    public static final int CARACTERES_NOMBRE = 40;

    private String nombre;

    public Especialidad() {
    }

    public Especialidad(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Para que se muestre bien en los JComboBox y las tablas.
    @Override
    public String toString() {
        return nombre;
    }
}
