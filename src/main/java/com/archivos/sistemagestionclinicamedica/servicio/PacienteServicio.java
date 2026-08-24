package com.archivos.sistemagestionclinicamedica.servicio;

import com.archivos.sistemagestionclinicamedica.excepcion.PersistenciaException;
import com.archivos.sistemagestionclinicamedica.excepcion.RegistroDuplicadoException;
import com.archivos.sistemagestionclinicamedica.excepcion.RegistroNoEncontradoException;
import com.archivos.sistemagestionclinicamedica.excepcion.ValidacionException;
import com.archivos.sistemagestionclinicamedica.modelo.Paciente;
import com.archivos.sistemagestionclinicamedica.modelo.enums.Accion;
import com.archivos.sistemagestionclinicamedica.modelo.enums.Modulo;
import com.archivos.sistemagestionclinicamedica.persistencia.ArchivoCitas;
import com.archivos.sistemagestionclinicamedica.persistencia.ArchivoPacientes;
import com.archivos.sistemagestionclinicamedica.util.ValidadorFormato;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Logica de negocio de los pacientes.
 *
 * Es la capa del medio: la interfaz habla con este servicio, y el servicio
 * habla con el archivo. La interfaz NUNCA toca el archivo directamente. Aca es
 * donde se validan los datos antes de guardarlos y donde se deja registro en la
 * bitacora de cada operacion.
 *
 * Reglas de validacion: - identificacion: 13 digitos numericos, unica - nombres
 * y apellidos: obligatorios - fecha de nacimiento: obligatoria y no futura -
 * correo: opcional, pero con formato valido si se ingresa
 */
public class PacienteServicio {

    private final ArchivoPacientes archivo;
    private final ArchivoCitas archivoCitas;   // para verificar citas antes de borrar
    private final LogServicio log;

    public PacienteServicio(ArchivoPacientes archivo, ArchivoCitas archivoCitas, LogServicio log) {
        this.archivo = archivo;
        this.archivoCitas = archivoCitas;
        this.log = log;
    }

    /**
     * Registra un paciente nuevo despues de validar todos sus datos.
     */
    public void registrar(Paciente paciente)
            throws ValidacionException, RegistroDuplicadoException, PersistenciaException {
        validar(paciente, true);
        archivo.agregar(paciente);
        log.registrar(Modulo.PACIENTES, Accion.CREACION,
                "Se registro el paciente " + paciente.getNombreCompleto()
                + " (" + paciente.getIdentificacion() + ")");
    }

    /**
     * Modifica un paciente existente. La identificacion no se valida como nueva
     * porque no cambia; sirve para ubicar el registro.
     */
    public void modificar(Paciente paciente)
            throws ValidacionException, RegistroNoEncontradoException, PersistenciaException {
        validar(paciente, false);
        if (!archivo.existe(paciente.getIdentificacion())) {
            throw new RegistroNoEncontradoException(
                    "No existe un paciente con la identificacion " + paciente.getIdentificacion());
        }
        archivo.modificar(paciente);
        log.registrar(Modulo.PACIENTES, Accion.ACTUALIZACION,
                "Se modifico el paciente " + paciente.getNombreCompleto()
                + " (" + paciente.getIdentificacion() + ")");
    }

    /**
     * Elimina un paciente por su identificacion.
     */
    public void eliminar(String identificacion)
            throws ValidacionException, RegistroNoEncontradoException, PersistenciaException {
        if (!archivo.existe(identificacion)) {
            throw new RegistroNoEncontradoException(
                    "No existe un paciente con la identificacion " + identificacion);
        }
        // No se puede borrar un paciente que tiene citas programadas: quedarian
        // citas apuntando a un paciente que ya no existe.
        if (archivoCitas.pacienteTieneCitasProgramadas(identificacion)) {
            throw new ValidacionException(
                    "No se puede eliminar el paciente porque tiene citas programadas. "
                    + "Cancele o atienda esas citas primero.");
        }
        archivo.eliminar(identificacion);
        log.registrar(Modulo.PACIENTES, Accion.ELIMINACION,
                "Se elimino el paciente con identificacion " + identificacion);
    }

    /**
     * Busca un paciente por identificacion exacta.
     */
    public Paciente buscarPorIdentificacion(String identificacion)
            throws RegistroNoEncontradoException, PersistenciaException {
        return archivo.buscarPorIdentificacion(identificacion)
                .orElseThrow(() -> new RegistroNoEncontradoException(
                "No existe un paciente con la identificacion " + identificacion));
    }

    /**
     * Dice si existe un paciente con esa identificacion. Lo usa CitaServicio
     * para validar sin tener que capturar una excepcion.
     */
    public boolean existe(String identificacion) {
        return archivo.existe(identificacion);
    }

    /**
     * Devuelve todos los pacientes.
     */
    public List<Paciente> listarTodos() throws PersistenciaException {
        return archivo.listarTodos();
    }

    /**
     * Busca pacientes por identificacion, nombre o apellido, todo en un mismo
     * campo. Coincide si el texto aparece en cualquiera de los tres (sin
     * importar mayusculas). Es el buscador de la interfaz.
     */
    public List<Paciente> buscar(String texto) throws PersistenciaException {
        String buscado = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        if (buscado.isEmpty()) {
            return listarTodos();
        }
        return archivo.listarTodos().stream()
                .filter(p -> p.getIdentificacion().toLowerCase(Locale.ROOT).contains(buscado)
                || p.getNombres().toLowerCase(Locale.ROOT).contains(buscado)
                || p.getApellidos().toLowerCase(Locale.ROOT).contains(buscado))
                .toList();
    }

    // ------------------------------------------------------------------
    // Validaciones
    // ------------------------------------------------------------------
    /**
     * Valida todos los campos del paciente.
     *
     * @param esNuevo si es true, ademas verifica que la identificacion no
     * exista
     */
    private void validar(Paciente p, boolean esNuevo) throws ValidacionException {
        if (p == null) {
            throw new ValidacionException("El paciente no puede ser nulo.");
        }

        // Identificacion: 13 digitos numericos.
        if (!ValidadorFormato.esDigitosExactos(
                p.getIdentificacion(), Paciente.CARACTERES_IDENTIFICACION)) {
            throw new ValidacionException(
                    "La identificacion debe tener exactamente 13 digitos numericos.");
        }

        // Solo al crear: que no exista ya.
        if (esNuevo && archivo.existe(p.getIdentificacion())) {
            throw new ValidacionException(
                    "Ya existe un paciente con la identificacion " + p.getIdentificacion() + ".");
        }

        // Nombres y apellidos obligatorios.
        if (ValidadorFormato.estaVacio(p.getNombres())) {
            throw new ValidacionException("Los nombres son obligatorios.");
        }
        if (ValidadorFormato.estaVacio(p.getApellidos())) {
            throw new ValidacionException("Los apellidos son obligatorios.");
        }

        // Sexo y tipo de sangre obligatorios (vienen de combos, pero por si acaso).
        if (p.getSexo() == null) {
            throw new ValidacionException("Debe seleccionar el sexo.");
        }
        if (p.getTipoSangre() == null) {
            throw new ValidacionException("Debe seleccionar el tipo de sangre.");
        }

        // Fecha de nacimiento obligatoria y no futura.
        if (p.getFechaNacimiento() == null) {
            throw new ValidacionException("La fecha de nacimiento es obligatoria.");
        }
        if (p.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new ValidacionException("La fecha de nacimiento no puede ser futura.");
        }

        // Telefono: si se ingresa, con formato valido.
        if (!ValidadorFormato.estaVacio(p.getTelefono())
                && !ValidadorFormato.telefonoValido(p.getTelefono())) {
            throw new ValidacionException(
                    "El telefono solo puede tener digitos, espacios y guiones (7 a 15 caracteres).");
        }

        // Correo: opcional, pero valido si se ingresa.
        if (!ValidadorFormato.estaVacio(p.getCorreo())
                && !ValidadorFormato.correoValido(p.getCorreo())) {
            throw new ValidacionException("El correo electronico no tiene un formato valido.");
        }
    }
}
