package com.archivos.sistemagestionclinicamedica.excepcion;

/**
 * Error al leer o escribir en los archivos de datos.
 *
 * Envuelve las IOException para que los servicios y la interfaz no tengan que
 * saber nada de entrada y salida.
 */
public class PersistenciaException extends ClinicaException {

    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensaje) {
        super(mensaje);
    }

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
