package com.archivos.sistemagestionclinicamedica.excepcion;

/**
 * Se busco un registro por su llave y no existe, o ya fue eliminado.
 */
public class RegistroNoEncontradoException extends ClinicaException {

    private static final long serialVersionUID = 1L;

    public RegistroNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
