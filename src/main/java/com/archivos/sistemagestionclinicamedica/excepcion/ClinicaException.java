package com.archivos.sistemagestionclinicamedica.excepcion;

/**
 * Excepcion padre de todas las excepciones propias del sistema.
 *
 * Tenerla permite que la interfaz grafica capture un solo tipo y muestre el
 * mensaje al usuario, sin importar si el error vino de un archivo o de una
 * regla de negocio.
 */
public class ClinicaException extends Exception {

    private static final long serialVersionUID = 1L;

    public ClinicaException(String mensaje) {
        super(mensaje);
    }

    // Se usa cuando hay que envolver otra excepcion, por ejemplo una IOException.
    public ClinicaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
