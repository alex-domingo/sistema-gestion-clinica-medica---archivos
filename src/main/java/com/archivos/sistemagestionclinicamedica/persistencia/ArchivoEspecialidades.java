package com.archivos.sistemagestionclinicamedica.persistencia;

import com.archivos.sistemagestionclinicamedica.excepcion.PersistenciaException;
import com.archivos.sistemagestionclinicamedica.excepcion.RegistroDuplicadoException;
import com.archivos.sistemagestionclinicamedica.modelo.Especialidad;
import com.archivos.sistemagestionclinicamedica.persistencia.util.BufferRegistro;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Archivo del catalogo de especialidades.
 *
 * Guarda las especialidades que el usuario va agregando. Mantiene un indice por
 * nombre (en minusculas) para detectar duplicados sin importar mayusculas, asi
 * "Cardiologia" y "cardiologia" cuentan como la misma.
 *
 * Registro de 81 bytes: eliminado 1 byte nombre 80 bytes (40 caracteres)
 */
public final class ArchivoEspecialidades extends ArchivoBase<Especialidad> {

    public static final String NOMBRE_ARCHIVO = "especialidades.dat";

    private static final int TAMANIO_DATOS
            = BufferRegistro.bytesDeCadena(Especialidad.CARACTERES_NOMBRE);

    // Indice: nombre en minusculas -> posicion. En minusculas para que la
    // comparacion de duplicados no distinga mayusculas.
    private final Map<String, Long> indicePorNombre = new HashMap<>();

    public ArchivoEspecialidades(Path ruta) throws PersistenciaException {
        super(ruta, TAMANIO_DATOS);
        inicializar();
    }

    @Override
    protected void alIndexar(long posicion, Especialidad especialidad) {
        indicePorNombre.put(clave(especialidad.getNombre()), posicion);
    }

    @Override
    protected void escribirDatos(Especialidad e, BufferRegistro buffer) {
        buffer.escribirCadena(e.getNombre(), Especialidad.CARACTERES_NOMBRE);
    }

    @Override
    protected Especialidad leerDatos(BufferRegistro buffer) {
        return new Especialidad(buffer.leerCadena(Especialidad.CARACTERES_NOMBRE));
    }

    /**
     * Agrega una especialidad nueva. Falla si ya existe una con el mismo nombre
     * (sin importar mayusculas).
     */
    public void agregar(Especialidad especialidad)
            throws PersistenciaException, RegistroDuplicadoException {
        if (existe(especialidad.getNombre())) {
            throw new RegistroDuplicadoException(
                    "Ya existe la especialidad " + especialidad.getNombre());
        }
        long posicion = insertar(especialidad);
        indicePorNombre.put(clave(especialidad.getNombre()), posicion);
    }

    /**
     * Dice si ya existe una especialidad con ese nombre (sin importar
     * mayusculas).
     */
    public boolean existe(String nombre) {
        return indicePorNombre.containsKey(clave(nombre));
    }

    public int cantidad() {
        return indicePorNombre.size();
    }

    // Normaliza el nombre para comparar: sin espacios de sobra y en minusculas.
    private String clave(String nombre) {
        return nombre == null ? "" : nombre.trim().toLowerCase(Locale.ROOT);
    }
}
