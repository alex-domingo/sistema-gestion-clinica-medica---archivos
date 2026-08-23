package com.archivos.sistemagestionclinicamedica.excepcion;

/**
 * Se intento insertar un registro con una llave que ya existe, por ejemplo el
 * numero de identificacion de un paciente.
 */
public class RegistroDuplicadoException extends ClinicaException {

    private static final long serialVersionUID = 1L;

    public RegistroDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
