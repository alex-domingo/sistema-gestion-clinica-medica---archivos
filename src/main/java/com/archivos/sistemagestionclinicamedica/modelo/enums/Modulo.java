package com.archivos.sistemagestionclinicamedica.modelo.enums;

/**
 * Modulos del sistema. Sirve para saber en que parte de la aplicacion se hizo
 * cada accion que queda en la bitacora.
 *
 * Importante: el orden no se puede cambiar despues, porque en el archivo el
 * enum se guarda por su posicion (ordinal). Si hace falta agregar mas, van al
 * final. Esto vale para todos los enums del proyecto.
 */
public enum Modulo {
    PACIENTES("Pacientes"),
    MEDICOS("Medicos"),
    CITAS("Citas"),
    REPORTES("Reportes"),
    SISTEMA("Sistema");

    private final String descripcion;

    Modulo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    // Para que al mostrarlo en pantalla salga el texto lindo y no la constante.
    @Override
    public String toString() {
        return descripcion;
    }
}
