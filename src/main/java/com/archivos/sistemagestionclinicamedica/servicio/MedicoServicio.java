package com.archivos.sistemagestionclinicamedica.servicio;

import com.archivos.sistemagestionclinicamedica.excepcion.PersistenciaException;
import com.archivos.sistemagestionclinicamedica.excepcion.RegistroNoEncontradoException;
import com.archivos.sistemagestionclinicamedica.excepcion.ValidacionException;
import com.archivos.sistemagestionclinicamedica.modelo.Medico;
import com.archivos.sistemagestionclinicamedica.modelo.enums.Accion;
import com.archivos.sistemagestionclinicamedica.modelo.enums.Modulo;
import com.archivos.sistemagestionclinicamedica.persistencia.ArchivoCitas;
import com.archivos.sistemagestionclinicamedica.persistencia.ArchivoMedicos;
import com.archivos.sistemagestionclinicamedica.util.ValidadorFormato;

import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Logica de negocio de los medicos.
 *
 * Igual que PacienteServicio, es la capa del medio entre la interfaz y el
 * archivo, y aca viven las validaciones y el registro en bitacora.
 *
 * La diferencia es que este servicio ademas conoce al EspecialidadServicio,
 * porque antes de guardar un medico tiene que verificar que su especialidad
 * exista en el catalogo.
 *
 * Reglas de validacion: - nombres, apellidos y especialidad: obligatorios -
 * especialidad: debe existir en el catalogo - horario: formato HH:mm valido, y
 * la hora de inicio antes que la de fin - correo: opcional, con formato valido
 * si se ingresa
 */
public class MedicoServicio {

    private final ArchivoMedicos archivo;
    private final EspecialidadServicio especialidades;
    private final ArchivoCitas archivoCitas;   // para verificar citas antes de borrar
    private final LogServicio log;

    public MedicoServicio(ArchivoMedicos archivo, EspecialidadServicio especialidades,
            ArchivoCitas archivoCitas, LogServicio log) {
        this.archivo = archivo;
        this.especialidades = especialidades;
        this.archivoCitas = archivoCitas;
        this.log = log;
    }

    /**
     * Registra un medico nuevo. El UUID lo genera el archivo, no viene del
     * formulario.
     */
    public UUID registrar(Medico medico) throws ValidacionException, PersistenciaException {
        validar(medico);
        UUID uuid = archivo.agregar(medico);
        log.registrar(Modulo.MEDICOS, Accion.CREACION,
                "Se registro el medico " + medico.getNombreCompleto()
                + " - " + medico.getEspecialidad());
        return uuid;
    }

    /**
     * Modifica un medico existente.
     */
    public void modificar(Medico medico)
            throws ValidacionException, RegistroNoEncontradoException, PersistenciaException {
        validar(medico);
        if (!archivo.existe(medico.getUuid())) {
            throw new RegistroNoEncontradoException("No existe el medico que se quiere modificar.");
        }
        archivo.modificar(medico);
        log.registrar(Modulo.MEDICOS, Accion.ACTUALIZACION,
                "Se modifico el medico " + medico.getNombreCompleto());
    }

    /**
     * Activa o desactiva un medico. Es distinto de eliminarlo: el medico sigue
     * existiendo, solo cambia su estado.
     *
     * No se puede DESACTIVAR un medico que tiene citas programadas, porque esas
     * citas quedarian apuntando a un medico marcado como no disponible. Activar
     * en cambio siempre se permite: no genera ninguna inconsistencia.
     */
    public void cambiarEstado(UUID uuid, boolean activo)
            throws ValidacionException, RegistroNoEncontradoException, PersistenciaException {
        Medico medico = buscarPorUuid(uuid);
        if (!activo && archivoCitas.medicoTieneCitasProgramadas(uuid)) {
            throw new ValidacionException(
                    "No se puede desactivar al medico " + medico.getNombreCompleto()
                    + " porque tiene citas programadas. "
                    + "Cancele o atienda esas citas primero.");
        }
        medico.setActivo(activo);
        archivo.modificar(medico);
        log.registrar(Modulo.MEDICOS, Accion.ACTUALIZACION,
                (activo ? "Se activo" : "Se desactivo") + " al medico " + medico.getNombreCompleto());
    }

    /**
     * Elimina un medico (borrado logico).
     */
    public void eliminar(UUID uuid)
            throws ValidacionException, RegistroNoEncontradoException, PersistenciaException {
        if (!archivo.existe(uuid)) {
            throw new RegistroNoEncontradoException("No existe el medico a eliminar.");
        }
        // No se puede borrar un medico que tiene citas programadas.
        if (archivoCitas.medicoTieneCitasProgramadas(uuid)) {
            throw new ValidacionException(
                    "No se puede eliminar el medico porque tiene citas programadas. "
                    + "Cancele o atienda esas citas primero.");
        }
        archivo.eliminar(uuid);
        log.registrar(Modulo.MEDICOS, Accion.ELIMINACION,
                "Se elimino al medico con UUID " + uuid);
    }

    /**
     * Busca un medico por su UUID.
     */
    public Medico buscarPorUuid(UUID uuid)
            throws RegistroNoEncontradoException, PersistenciaException {
        return archivo.buscarPorUuid(uuid)
                .orElseThrow(() -> new RegistroNoEncontradoException(
                "No existe un medico con ese UUID."));
    }

    /**
     * Busca un medico y devuelve null si no existe (en vez de lanzar
     * excepcion). Lo usa CitaServicio para validar de forma comoda.
     */
    public Medico buscarSiExiste(UUID uuid) throws PersistenciaException {
        return archivo.buscarPorUuid(uuid).orElse(null);
    }

    /**
     * Devuelve todos los medicos.
     */
    public List<Medico> listarTodos() throws PersistenciaException {
        return archivo.listarTodos();
    }

    /**
     * Solo los medicos activos.
     */
    public List<Medico> listarActivos() throws PersistenciaException {
        return archivo.listarTodos().stream()
                .filter(Medico::isActivo)
                .toList();
    }

    /**
     * Solo los medicos inactivos.
     */
    public List<Medico> listarInactivos() throws PersistenciaException {
        return archivo.listarTodos().stream()
                .filter(m -> !m.isActivo())
                .toList();
    }

    /**
     * Medicos de una especialidad dada.
     */
    public List<Medico> listarPorEspecialidad(String especialidad) throws PersistenciaException {
        String buscada = especialidad == null ? "" : especialidad.trim();
        return archivo.listarTodos().stream()
                .filter(m -> m.getEspecialidad() != null
                && m.getEspecialidad().equalsIgnoreCase(buscada))
                .toList();
    }

    /**
     * Buscador unificado: por UUID, nombre, apellido o especialidad, todo en un
     * mismo campo (sin importar mayusculas).
     */
    public List<Medico> buscar(String texto) throws PersistenciaException {
        String buscado = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        if (buscado.isEmpty()) {
            return listarTodos();
        }
        return archivo.listarTodos().stream()
                .filter(m -> m.getUuid().toString().toLowerCase(Locale.ROOT).contains(buscado)
                || m.getNombres().toLowerCase(Locale.ROOT).contains(buscado)
                || m.getApellidos().toLowerCase(Locale.ROOT).contains(buscado)
                || (m.getEspecialidad() != null
                && m.getEspecialidad().toLowerCase(Locale.ROOT).contains(buscado)))
                .toList();
    }

    // ------------------------------------------------------------------
    // Validaciones
    // ------------------------------------------------------------------
    private void validar(Medico m) throws ValidacionException, PersistenciaException {
        if (m == null) {
            throw new ValidacionException("El medico no puede ser nulo.");
        }

        if (ValidadorFormato.estaVacio(m.getNombres())) {
            throw new ValidacionException("Los nombres son obligatorios.");
        }
        if (ValidadorFormato.estaVacio(m.getApellidos())) {
            throw new ValidacionException("Los apellidos son obligatorios.");
        }

        // Especialidad: obligatoria y debe existir en el catalogo.
        if (ValidadorFormato.estaVacio(m.getEspecialidad())) {
            throw new ValidacionException("La especialidad es obligatoria.");
        }
        if (!especialidades.existe(m.getEspecialidad())) {
            throw new ValidacionException(
                    "La especialidad \"" + m.getEspecialidad()
                    + "\" no existe en el catalogo. Agreguela primero.");
        }

        // Horario: ambas horas obligatorias.
        if (m.getHoraInicio() == null || m.getHoraFin() == null) {
            throw new ValidacionException("El horario de atencion (inicio y fin) es obligatorio.");
        }
        // La hora de inicio debe ser anterior a la de fin.
        if (!m.getHoraInicio().isBefore(m.getHoraFin())) {
            throw new ValidacionException(
                    "La hora de inicio debe ser anterior a la hora de fin.");
        }

        // Telefono: si se ingresa, formato valido.
        if (!ValidadorFormato.estaVacio(m.getTelefono())
                && !ValidadorFormato.telefonoValido(m.getTelefono())) {
            throw new ValidacionException(
                    "El telefono solo puede tener digitos, espacios y guiones (7 a 15 caracteres).");
        }

        // Correo: opcional, valido si se ingresa.
        if (!ValidadorFormato.estaVacio(m.getCorreo())
                && !ValidadorFormato.correoValido(m.getCorreo())) {
            throw new ValidacionException("El correo electronico no tiene un formato valido.");
        }
    }

    /**
     * Convierte un texto "HH:mm" a LocalTime. Lo usa la interfaz al leer los
     * campos de horario. Devuelve null si el formato es invalido, para que la
     * vista muestre el aviso correspondiente.
     */
    public static LocalTime parsearHora(String texto) {
        if (!ValidadorFormato.horaValida(texto)) {
            return null;
        }
        String[] partes = texto.trim().split(":");
        return LocalTime.of(Integer.parseInt(partes[0]), Integer.parseInt(partes[1]));
    }
}
