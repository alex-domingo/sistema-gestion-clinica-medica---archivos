package com.archivos.sistemagestionclinicamedica.servicio;

import com.archivos.sistemagestionclinicamedica.excepcion.PersistenciaException;
import com.archivos.sistemagestionclinicamedica.excepcion.RegistroNoEncontradoException;
import com.archivos.sistemagestionclinicamedica.excepcion.ValidacionException;
import com.archivos.sistemagestionclinicamedica.modelo.Cita;
import com.archivos.sistemagestionclinicamedica.modelo.Medico;
import com.archivos.sistemagestionclinicamedica.modelo.enums.Accion;
import com.archivos.sistemagestionclinicamedica.modelo.enums.EstadoCita;
import com.archivos.sistemagestionclinicamedica.modelo.enums.Modulo;
import com.archivos.sistemagestionclinicamedica.persistencia.ArchivoCitas;
import com.archivos.sistemagestionclinicamedica.util.ValidadorFormato;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Logica de negocio de las citas. Es el servicio mas complejo, porque una cita
 * conecta un paciente con un medico y hay que validar cosas de las tres partes.
 *
 * Para validar, este servicio consulta a PacienteServicio y MedicoServicio (que
 * el paciente y el medico existan, que el medico este activo). Para los
 * traslapes y los filtros usa ArchivoCitas directamente.
 *
 * Reglas al programar una cita: - el paciente debe existir - el medico debe
 * existir y estar activo - la cita (inicio + 60 min) debe entrar dentro del
 * horario del medico - la fecha y hora no pueden ser pasadas - el medico no
 * puede tener otra cita programada que se solape - el paciente no puede tener
 * otra cita programada que se solape
 */
public class CitaServicio {

    private final ArchivoCitas archivo;
    private final PacienteServicio pacientes;
    private final MedicoServicio medicos;
    private final LogServicio log;

    public CitaServicio(ArchivoCitas archivo, PacienteServicio pacientes,
            MedicoServicio medicos, LogServicio log) {
        this.archivo = archivo;
        this.pacientes = pacientes;
        this.medicos = medicos;
        this.log = log;
    }

    /**
     * Programa una cita nueva despues de pasar todas las validaciones. El
     * estado inicial siempre es PROGRAMADA.
     */
    public UUID programar(Cita cita)
            throws ValidacionException, RegistroNoEncontradoException, PersistenciaException {
        cita.setEstado(EstadoCita.PROGRAMADA);
        validarParaProgramar(cita);
        UUID uuid = archivo.agregar(cita);
        log.registrar(Modulo.CITAS, Accion.CREACION,
                "Se programo cita para el paciente " + cita.getIdentificacionPaciente()
                + " el " + cita.getFechaFormateada() + " a las " + cita.getHoraInicioFormateada());
        return uuid;
    }

    /**
     * Cancela una cita. Solo se puede cancelar una que este programada.
     */
    public void cancelar(UUID uuid)
            throws ValidacionException, RegistroNoEncontradoException, PersistenciaException {
        Cita cita = buscarPorUuid(uuid);
        if (cita.getEstado() != EstadoCita.PROGRAMADA) {
            throw new ValidacionException(
                    "Solo se puede cancelar una cita que esta programada.");
        }
        cita.setEstado(EstadoCita.CANCELADA);
        archivo.modificar(cita);
        log.registrar(Modulo.CITAS, Accion.ACTUALIZACION, "Se cancelo la cita " + cita.getUuidCorto());
    }

    /**
     * Marca una cita como atendida. Solo aplica a una cita programada.
     */
    public void marcarAtendida(UUID uuid)
            throws ValidacionException, RegistroNoEncontradoException, PersistenciaException {
        Cita cita = buscarPorUuid(uuid);
        if (cita.getEstado() != EstadoCita.PROGRAMADA) {
            throw new ValidacionException(
                    "Solo se puede marcar como atendida una cita que esta programada.");
        }
        cita.setEstado(EstadoCita.ATENDIDA);
        archivo.modificar(cita);
        log.registrar(Modulo.CITAS, Accion.ACTUALIZACION,
                "Se marco como atendida la cita " + cita.getUuidCorto());
    }

    /**
     * Modifica el motivo y las observaciones de una cita. No se tocan el
     * paciente, el medico, la fecha ni la hora: para cambiar eso se cancela y
     * se programa una nueva, asi no hay que revalidar traslapes aca.
     */
    public void modificarMotivoObservaciones(UUID uuid, String motivo, String observaciones)
            throws ValidacionException, RegistroNoEncontradoException, PersistenciaException {
        Cita cita = buscarPorUuid(uuid);
        if (ValidadorFormato.estaVacio(motivo)) {
            throw new ValidacionException("El motivo de la cita es obligatorio.");
        }
        cita.setMotivo(motivo.trim());
        cita.setObservaciones(observaciones == null ? "" : observaciones.trim());
        archivo.modificar(cita);
        log.registrar(Modulo.CITAS, Accion.ACTUALIZACION,
                "Se actualizo el motivo/observaciones de la cita " + cita.getUuidCorto());
    }

    /**
     * Elimina una cita (borrado logico).
     */
    public void eliminar(UUID uuid)
            throws RegistroNoEncontradoException, PersistenciaException {
        if (!archivo.existe(uuid)) {
            throw new RegistroNoEncontradoException("No existe la cita a eliminar.");
        }
        archivo.eliminar(uuid);
        log.registrar(Modulo.CITAS, Accion.ELIMINACION, "Se elimino la cita " + uuid);
    }

    public Cita buscarPorUuid(UUID uuid)
            throws RegistroNoEncontradoException, PersistenciaException {
        return archivo.buscarPorUuid(uuid)
                .orElseThrow(() -> new RegistroNoEncontradoException("No existe una cita con ese UUID."));
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------
    public List<Cita> listarTodos() throws PersistenciaException {
        return archivo.listarTodos();
    }

    public List<Cita> listarPorPaciente(String dpi) throws PersistenciaException {
        return archivo.listarPorPaciente(dpi);
    }

    public List<Cita> listarPorMedico(UUID uuidMedico) throws PersistenciaException {
        return archivo.listarPorMedico(uuidMedico);
    }

    public List<Cita> listarPorFecha(LocalDate fecha) throws PersistenciaException {
        return archivo.listarPorFecha(fecha);
    }

    public List<Cita> listarPorEstado(EstadoCita estado) throws PersistenciaException {
        return archivo.listarPorEstado(estado);
    }

    /**
     * Citas dentro de un rango de fechas, ambos extremos incluidos.
     */
    public List<Cita> listarPorRangoFechas(LocalDate desde, LocalDate hasta)
            throws PersistenciaException {
        return archivo.listarTodos().stream()
                .filter(c -> c.getFecha() != null
                && !c.getFecha().isBefore(desde)
                && !c.getFecha().isAfter(hasta))
                .toList();
    }

    /**
     * Buscador unificado: por UUID de la cita, nombre del paciente o nombre del
     * medico, todo en un mismo campo (sin importar mayusculas).
     *
     * Como la cita solo guarda el DPI y el UUID del medico, para poder buscar
     * por nombre hay que traducir esas llaves. Se arman dos mapas (DPI ->
     * nombre, UUID -> nombre) una sola vez y se comparan contra ellos.
     */
    public List<Cita> buscar(String texto) throws PersistenciaException {
        String buscado = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        if (buscado.isEmpty()) {
            return listarTodos();
        }

        // Mapas de nombres para poder buscar por paciente/medico.
        Map<String, String> nombresPaciente = new HashMap<>();
        for (var p : pacientes.listarTodos()) {
            nombresPaciente.put(p.getIdentificacion(),
                    p.getNombreCompleto().toLowerCase(Locale.ROOT));
        }
        Map<UUID, String> nombresMedico = new HashMap<>();
        for (var m : medicos.listarTodos()) {
            nombresMedico.put(m.getUuid(), m.getNombreCompleto().toLowerCase(Locale.ROOT));
        }

        final String buscadoFinal = buscado;
        return archivo.listarTodos().stream()
                .filter(c -> {
                    // Por UUID de la cita.
                    if (c.getUuid() != null
                            && c.getUuid().toString().toLowerCase(Locale.ROOT).contains(buscadoFinal)) {
                        return true;
                    }
                    // Por nombre del paciente.
                    String nomPac = nombresPaciente.get(c.getIdentificacionPaciente());
                    if (nomPac != null && nomPac.contains(buscadoFinal)) {
                        return true;
                    }
                    // Por nombre del medico.
                    String nomMed = nombresMedico.get(c.getUuidMedico());
                    return nomMed != null && nomMed.contains(buscadoFinal);
                })
                .toList();
    }

    // ------------------------------------------------------------------
    // Validaciones cruzadas (el corazon del modulo)
    // ------------------------------------------------------------------
    private void validarParaProgramar(Cita cita)
            throws ValidacionException, RegistroNoEncontradoException, PersistenciaException {
        if (cita == null) {
            throw new ValidacionException("La cita no puede ser nula.");
        }

        // Motivo obligatorio.
        if (ValidadorFormato.estaVacio(cita.getMotivo())) {
            throw new ValidacionException("El motivo de la cita es obligatorio.");
        }

        // Fecha y hora obligatorias.
        if (cita.getFecha() == null || cita.getHoraInicio() == null) {
            throw new ValidacionException("La fecha y la hora de la cita son obligatorias.");
        }

        // 1. El paciente debe existir.
        if (!pacientes.existe(cita.getIdentificacionPaciente())) {
            throw new ValidacionException(
                    "El paciente con identificacion " + cita.getIdentificacionPaciente()
                    + " no existe.");
        }

        // 2. El medico debe existir y estar activo.
        Medico medico = medicos.buscarSiExiste(cita.getUuidMedico());
        if (medico == null) {
            throw new ValidacionException("El medico seleccionado no existe.");
        }
        if (!medico.isActivo()) {
            throw new ValidacionException(
                    "El medico " + medico.getNombreCompleto()
                    + " esta inactivo y no puede recibir citas.");
        }

        // 3. La fecha y hora no pueden ser pasadas.
        LocalDateTime momentoCita = LocalDateTime.of(cita.getFecha(), cita.getHoraInicio());
        if (momentoCita.isBefore(LocalDateTime.now())) {
            throw new ValidacionException("No se puede programar una cita en el pasado.");
        }

        // 4. La cita completa (inicio + 60 min) debe entrar en el horario del medico.
        LocalTime finCita = cita.getHoraFin();
        if (cita.getHoraInicio().isBefore(medico.getHoraInicio())
                || finCita.isAfter(medico.getHoraFin())) {
            throw new ValidacionException(
                    "La cita debe estar dentro del horario del medico ("
                    + medico.getHoraInicioFormateada() + " a " + medico.getHoraFinFormateada()
                    + "). La cita ocupa de " + cita.getHoraInicioFormateada()
                    + " a " + finCita + ".");
        }

        // 5. El medico no puede tener otra cita programada que se solape.
        for (Cita otra : archivo.citasProgramadasDeMedico(cita.getUuidMedico())) {
            if (cita.seSolapaCon(otra)) {
                throw new ValidacionException(
                        "El medico ya tiene una cita programada que se solapa: "
                        + otra.getFechaFormateada() + " " + otra.getHorarioFormateado() + ".");
            }
        }

        // 6. El paciente no puede tener otra cita programada que se solape.
        for (Cita otra : archivo.citasProgramadasDePaciente(cita.getIdentificacionPaciente())) {
            if (cita.seSolapaCon(otra)) {
                throw new ValidacionException(
                        "El paciente ya tiene una cita programada que se solapa: "
                        + otra.getFechaFormateada() + " " + otra.getHorarioFormateado() + ".");
            }
        }
    }
}
