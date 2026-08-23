package com.archivos.sistemagestionclinicamedica.servicio;

import com.archivos.sistemagestionclinicamedica.excepcion.PersistenciaException;
import com.archivos.sistemagestionclinicamedica.excepcion.RegistroDuplicadoException;
import com.archivos.sistemagestionclinicamedica.excepcion.ValidacionException;
import com.archivos.sistemagestionclinicamedica.modelo.Especialidad;
import com.archivos.sistemagestionclinicamedica.modelo.enums.Accion;
import com.archivos.sistemagestionclinicamedica.modelo.enums.Modulo;
import com.archivos.sistemagestionclinicamedica.persistencia.ArchivoEspecialidades;
import com.archivos.sistemagestionclinicamedica.util.ValidadorFormato;

import java.util.Comparator;
import java.util.List;

/**
 * Logica del catalogo de especialidades.
 *
 * Permite listar y agregar especialidades. La primera vez que arranca la app
 * (catalogo vacio) siembra una lista basica, para que el usuario no tenga que
 * escribir todas desde cero.
 */
public class EspecialidadServicio {

    private final ArchivoEspecialidades archivo;
    private final LogServicio log;

    // Especialidades que se cargan la primera vez, si el catalogo esta vacio.
    private static final String[] INICIALES = {
        "Medicina General", "Cardiologia", "Pediatria", "Ginecologia",
        "Dermatologia", "Traumatologia", "Oftalmologia", "Neurologia",
        "Psiquiatria", "Odontologia"
    };

    public EspecialidadServicio(ArchivoEspecialidades archivo, LogServicio log)
            throws PersistenciaException {
        this.archivo = archivo;
        this.log = log;
        sembrarSiVacio();
    }

    // Si es la primera vez (no hay ninguna), carga la lista basica.
    private void sembrarSiVacio() throws PersistenciaException {
        if (archivo.cantidad() > 0) {
            return;
        }
        for (String nombre : INICIALES) {
            try {
                archivo.agregar(new Especialidad(nombre));
            } catch (RegistroDuplicadoException e) {
                // No deberia pasar en un catalogo vacio, se ignora.
            }
        }
        log.registrar(Modulo.SISTEMA, Accion.CREACION, "Catalogo de especialidades inicializado");
    }

    /**
     * Agrega una especialidad nueva al catalogo, despues de validar el nombre.
     */
    public void agregar(String nombre)
            throws ValidacionException, RegistroDuplicadoException, PersistenciaException {
        if (ValidadorFormato.estaVacio(nombre)) {
            throw new ValidacionException("El nombre de la especialidad es obligatorio.");
        }
        String limpio = nombre.trim();
        if (limpio.length() > Especialidad.CARACTERES_NOMBRE) {
            throw new ValidacionException(
                    "El nombre de la especialidad es demasiado largo (maximo "
                    + Especialidad.CARACTERES_NOMBRE + " caracteres).");
        }
        archivo.agregar(new Especialidad(limpio));
        log.registrar(Modulo.MEDICOS, Accion.CREACION,
                "Se agrego la especialidad " + limpio);
    }

    /**
     * Devuelve todas las especialidades ordenadas alfabeticamente.
     */
    public List<Especialidad> listarTodas() throws PersistenciaException {
        List<Especialidad> lista = archivo.listarTodos();
        lista.sort(Comparator.comparing(e -> e.getNombre().toLowerCase()));
        return lista;
    }

    /**
     * Dice si ya existe una especialidad con ese nombre.
     */
    public boolean existe(String nombre) {
        return archivo.existe(nombre);
    }
}
