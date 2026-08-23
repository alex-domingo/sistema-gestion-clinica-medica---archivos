package com.archivos.sistemagestionclinicamedica.excepcion;

/**
 * Los datos no cumplen una regla de negocio: un campo obligatorio vacio, un
 * correo mal escrito, una fecha imposible, etc.
 */
public class ValidacionException extends ClinicaException {

    private static final long serialVersionUID = 1L;

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
