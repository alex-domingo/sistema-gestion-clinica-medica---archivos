package com.archivos.sistemagestionclinicamedica.modelo;

import com.archivos.sistemagestionclinicamedica.modelo.enums.EstadoCita;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Una cita medica.
 *
 * Es la entidad que conecta las otras dos: guarda el DPI del paciente y el UUID
 * del medico como referencias (igual que el medico guarda el nombre de su
 * especialidad). No guarda copias de los datos del paciente ni del medico, solo
 * apunta a ellos por su llave.
 *
 * La cita tiene una hora de inicio y una duracion fija (60 minutos), con lo que
 * se calcula la hora de fin. Esa duracion es la que permite detectar si dos
 * citas se solapan.
 */
public class Cita {

    // Duracion fija de toda cita, en minutos. Es lo que hace posible calcular
    // traslapes y validar que la cita entre en el horario del medico.
    public static final int DURACION_MINUTOS = 60;

    public static final int CARACTERES_MOTIVO = 100;
    public static final int CARACTERES_OBSERVACIONES = 200;

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA
            = DateTimeFormatter.ofPattern("HH:mm");

    private UUID uuid;
    private String identificacionPaciente;   // DPI, referencia al paciente
    private UUID uuidMedico;                  // referencia al medico
    private LocalDate fecha;
    private LocalTime horaInicio;
    private String motivo;
    private EstadoCita estado;
    private String observaciones;             // opcional

    public Cita() {
    }

    public Cita(UUID uuid, String identificacionPaciente, UUID uuidMedico,
            LocalDate fecha, LocalTime horaInicio, String motivo,
            EstadoCita estado, String observaciones) {
        this.uuid = uuid;
        this.identificacionPaciente = identificacionPaciente;
        this.uuidMedico = uuidMedico;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.motivo = motivo;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getIdentificacionPaciente() {
        return identificacionPaciente;
    }

    public void setIdentificacionPaciente(String identificacionPaciente) {
        this.identificacionPaciente = identificacionPaciente;
    }

    public UUID getUuidMedico() {
        return uuidMedico;
    }

    public void setUuidMedico(UUID uuidMedico) {
        this.uuidMedico = uuidMedico;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    // --- Metodos de ayuda ---
    // Hora de fin, calculada a partir del inicio + la duracion fija.
    public LocalTime getHoraFin() {
        return horaInicio == null ? null : horaInicio.plusMinutes(DURACION_MINUTOS);
    }

    public String getFechaFormateada() {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA);
    }

    public String getHoraInicioFormateada() {
        return horaInicio == null ? "" : horaInicio.format(FORMATO_HORA);
    }

    public String getHorarioFormateado() {
        if (horaInicio == null) {
            return "";
        }
        return getHoraInicioFormateada() + " - " + getHoraFin().format(FORMATO_HORA);
    }

    /**
     * Dice si esta cita se solapa en el tiempo con otra. Solo tiene sentido
     * compararlas si son el mismo dia. Dos intervalos se solapan si uno empieza
     * antes de que el otro termine, y viceversa.
     */
    public boolean seSolapaCon(Cita otra) {
        if (fecha == null || otra.fecha == null || !fecha.equals(otra.fecha)) {
            return false;
        }
        LocalTime iniA = this.horaInicio;
        LocalTime finA = this.getHoraFin();
        LocalTime iniB = otra.horaInicio;
        LocalTime finB = otra.getHoraFin();
        // Se solapan si A empieza antes de que B termine y B empieza antes de
        // que A termine.
        return iniA.isBefore(finB) && iniB.isBefore(finA);
    }

    // UUID acortado para mostrar en la tabla.
    public String getUuidCorto() {
        return uuid == null ? "" : uuid.toString().substring(0, 8);
    }

    @Override
    public String toString() {
        return "Cita " + getUuidCorto() + " (" + getFechaFormateada() + " "
                + getHoraInicioFormateada() + ")";
    }
}
