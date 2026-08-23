package com.archivos.sistemagestionclinicamedica.persistencia;

import com.archivos.sistemagestionclinicamedica.excepcion.PersistenciaException;
import com.archivos.sistemagestionclinicamedica.modelo.RegistroLog;
import com.archivos.sistemagestionclinicamedica.modelo.enums.Accion;
import com.archivos.sistemagestionclinicamedica.modelo.enums.Modulo;
import com.archivos.sistemagestionclinicamedica.persistencia.util.BufferRegistro;

import java.nio.file.Path;

/**
 * Archivo de la bitacora. Es la primera subclase concreta de ArchivoBase.
 *
 * Cada registro mide 371 bytes: eliminado 1 byte (siempre falso aca, pero lo
 * pide ArchivoBase) usuario 60 bytes (30 caracteres) marcaTiempo 8 bytes modulo
 * 1 byte (ordinal del enum) accion 1 byte (ordinal del enum) detalle 300 bytes
 * (150 caracteres)
 *
 * Solo agrega registros. No deja actualizar ni borrar porque la bitacora tiene
 * que guardar el historial completo.
 *
 * Es final: no tiene sentido heredar de ella, y ademas evita un aviso del
 * compilador por llamar a inicializar() dentro del constructor.
 */
public final class ArchivoLogs extends ArchivoBase<RegistroLog> {

    public static final String NOMBRE_ARCHIVO = "logs.dat";

    // Suma de los tamanios de los datos (sin contar la bandera, que la agrega
    // ArchivoBase). Se calcula con las constantes del modelo para que si algun
    // dia cambia un tamanio, este numero se ajuste solo.
    private static final int TAMANIO_DATOS
            = BufferRegistro.bytesDeCadena(RegistroLog.CARACTERES_USUARIO) // usuario
            + Long.BYTES // marcaTiempo
            + 1 // modulo
            + 1 // accion
            + BufferRegistro.bytesDeCadena(RegistroLog.CARACTERES_DETALLE); // detalle

    public ArchivoLogs(Path ruta) throws PersistenciaException {
        super(ruta, TAMANIO_DATOS);
        inicializar();   // barrido inicial: arma la lista de huecos
    }

    // Este metodo y el de abajo son el "contrato" con ArchivoBase.
    // El ORDEN de los campos debe ser identico en los dos.
    @Override
    protected void escribirDatos(RegistroLog r, BufferRegistro buffer) {
        buffer.escribirCadena(r.getUsuario(), RegistroLog.CARACTERES_USUARIO);
        buffer.escribirMarcaTiempo(r.getMarcaTiempo());
        buffer.escribirEnum(r.getModulo());
        buffer.escribirEnum(r.getAccion());
        buffer.escribirCadena(r.getDetalle(), RegistroLog.CARACTERES_DETALLE);
    }

    @Override
    protected RegistroLog leerDatos(BufferRegistro buffer) {
        // Mismo orden que escribirDatos.
        String usuario = buffer.leerCadena(RegistroLog.CARACTERES_USUARIO);
        var marcaTiempo = buffer.leerMarcaTiempo();
        Modulo modulo = buffer.leerEnum(Modulo.values());
        Accion accion = buffer.leerEnum(Accion.values());
        String detalle = buffer.leerCadena(RegistroLog.CARACTERES_DETALLE);
        return new RegistroLog(usuario, marcaTiempo, modulo, accion, detalle);
    }

    /**
     * Agrega una entrada al final de la bitacora.
     */
    public void agregar(RegistroLog registro) throws PersistenciaException {
        insertar(registro);
    }
}
