package com.archivos.sistemagestionclinicamedica.servicio;

import com.archivos.sistemagestionclinicamedica.excepcion.PersistenciaException;
import com.archivos.sistemagestionclinicamedica.modelo.Cita;
import com.archivos.sistemagestionclinicamedica.modelo.Medico;
import com.archivos.sistemagestionclinicamedica.modelo.Paciente;
import com.archivos.sistemagestionclinicamedica.modelo.RegistroLog;
import com.archivos.sistemagestionclinicamedica.modelo.Reporte;
import com.archivos.sistemagestionclinicamedica.modelo.enums.EstadoCita;
import com.archivos.sistemagestionclinicamedica.modelo.enums.TipoSangre;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Genera los reportes del sistema.
 *
 * Cada metodo arma un objeto Reporte (titulo + columnas + filas) que la
 * interfaz muestra en una tabla y el exportador puede guardar en CSV/TXT.
 *
 * La mayoria de los reportes son listados directos de datos que ya sabemos
 * consultar. Los interesantes son los que necesitan calculo: contar citas por
 * paciente o por medico, encontrar quienes nunca tuvieron cita, y agrupar la
 * cantidad de citas por especialidad.
 *
 * Decision de conteo: para los reportes de "mayor cantidad de citas" y de
 * "cantidad por especialidad" se cuentan solo las citas programadas y
 * atendidas, no las canceladas, porque una cita cancelada no representa una
 * atencion real. Para "pacientes que nunca han tenido una cita" se considera
 * cualquier cita (de cualquier estado): si alguna vez se le agendo una, ya
 * "tuvo" una cita.
 */
public class ReporteServicio {

    private final PacienteServicio pacientes;
    private final MedicoServicio medicos;
    private final CitaServicio citas;
    private final LogServicio logs;

    public ReporteServicio(PacienteServicio pacientes, MedicoServicio medicos,
            CitaServicio citas, LogServicio logs) {
        this.pacientes = pacientes;
        this.medicos = medicos;
        this.citas = citas;
        this.logs = logs;
    }

    // Cuenta como "cita real" si esta programada o atendida (no cancelada).
    private boolean cuentaComoReal(Cita c) {
        return c.getEstado() == EstadoCita.PROGRAMADA || c.getEstado() == EstadoCita.ATENDIDA;
    }

    // ------------------------------------------------------------------
    // Reportes de pacientes
    // ------------------------------------------------------------------
    /**
     * Todos los pacientes.
     */
    public Reporte pacientesCompleto() throws PersistenciaException {
        Reporte r = new Reporte("Reporte completo de pacientes",
                List.of("Identificacion", "Nombres", "Apellidos", "Fecha Nac.",
                        "Sexo", "Telefono", "Correo", "Tipo Sangre"));
        for (Paciente p : pacientes.listarTodos()) {
            r.agregarFila(p.getIdentificacion(), p.getNombres(), p.getApellidos(),
                    p.getFechaNacimientoFormateada(), texto(p.getSexo()),
                    p.getTelefono(), p.getCorreo(), texto(p.getTipoSangre()));
        }
        return r;
    }

    /**
     * Pacientes de un tipo de sangre dado.
     */
    public Reporte pacientesPorTipoSangre(TipoSangre tipo) throws PersistenciaException {
        Reporte r = new Reporte("Reporte de pacientes con tipo de sangre " + tipo.getEtiqueta(),
                List.of("Identificacion", "Nombres", "Apellidos", "Tipo Sangre", "Telefono"));
        for (Paciente p : pacientes.listarTodos()) {
            if (p.getTipoSangre() == tipo) {
                r.agregarFila(p.getIdentificacion(), p.getNombres(), p.getApellidos(),
                        texto(p.getTipoSangre()), p.getTelefono());
            }
        }
        return r;
    }

    /**
     * Pacientes ordenados por cantidad de citas (reales), de mayor a menor.
     */
    public Reporte pacientesMayorCitas() throws PersistenciaException {
        // Contar citas reales por DPI.
        Map<String, Integer> conteo = new HashMap<>();
        for (Cita c : citas.listarTodos()) {
            if (cuentaComoReal(c)) {
                conteo.merge(c.getIdentificacionPaciente(), 1, Integer::sum);
            }
        }

        Reporte r = new Reporte("Reporte de pacientes con mayor cantidad de citas",
                List.of("Identificacion", "Nombres", "Apellidos", "Cantidad de citas"));

        // Todos los pacientes con su conteo (0 si no tiene), ordenados desc.
        pacientes.listarTodos().stream()
                .sorted(Comparator.comparingInt(
                        (Paciente p) -> conteo.getOrDefault(p.getIdentificacion(), 0)).reversed())
                .forEach(p -> r.agregarFila(
                p.getIdentificacion(), p.getNombres(), p.getApellidos(),
                String.valueOf(conteo.getOrDefault(p.getIdentificacion(), 0))));
        return r;
    }

    /**
     * Pacientes que nunca han tenido una cita (de ningun estado).
     */
    public Reporte pacientesSinCitas() throws PersistenciaException {
        // DPIs que aparecen en alguna cita (cualquier estado).
        Map<String, Boolean> conCita = new HashMap<>();
        for (Cita c : citas.listarTodos()) {
            conCita.put(c.getIdentificacionPaciente(), Boolean.TRUE);
        }

        Reporte r = new Reporte("Reporte de pacientes que nunca han tenido una cita",
                List.of("Identificacion", "Nombres", "Apellidos", "Telefono", "Correo"));
        for (Paciente p : pacientes.listarTodos()) {
            if (!conCita.containsKey(p.getIdentificacion())) {
                r.agregarFila(p.getIdentificacion(), p.getNombres(), p.getApellidos(),
                        p.getTelefono(), p.getCorreo());
            }
        }
        return r;
    }

    // ------------------------------------------------------------------
    // Reportes de medicos
    // ------------------------------------------------------------------
    /**
     * Todos los medicos.
     */
    public Reporte medicosCompleto() throws PersistenciaException {
        Reporte r = new Reporte("Reporte completo de medicos",
                List.of("Nombres", "Apellidos", "Especialidad", "Telefono",
                        "Correo", "Horario", "Estado"));
        for (Medico m : medicos.listarTodos()) {
            r.agregarFila(m.getNombres(), m.getApellidos(), m.getEspecialidad(),
                    m.getTelefono(), m.getCorreo(), m.getHorarioFormateado(), m.getEstadoTexto());
        }
        return r;
    }

    /**
     * Medicos de una especialidad dada.
     */
    public Reporte medicosPorEspecialidad(String especialidad) throws PersistenciaException {
        Reporte r = new Reporte("Reporte de medicos de la especialidad " + especialidad,
                List.of("Nombres", "Apellidos", "Especialidad", "Horario", "Estado"));
        for (Medico m : medicos.listarTodos()) {
            if (m.getEspecialidad() != null && m.getEspecialidad().equalsIgnoreCase(especialidad)) {
                r.agregarFila(m.getNombres(), m.getApellidos(), m.getEspecialidad(),
                        m.getHorarioFormateado(), m.getEstadoTexto());
            }
        }
        return r;
    }

    /**
     * Medicos ordenados por cantidad de citas (reales), de mayor a menor.
     */
    public Reporte medicosMayorCitas() throws PersistenciaException {
        Map<UUID, Integer> conteo = new HashMap<>();
        for (Cita c : citas.listarTodos()) {
            if (cuentaComoReal(c)) {
                conteo.merge(c.getUuidMedico(), 1, Integer::sum);
            }
        }

        Reporte r = new Reporte("Reporte de medicos con mayor cantidad de citas",
                List.of("Nombres", "Apellidos", "Especialidad", "Cantidad de citas"));
        medicos.listarTodos().stream()
                .sorted(Comparator.comparingInt(
                        (Medico m) -> conteo.getOrDefault(m.getUuid(), 0)).reversed())
                .forEach(m -> r.agregarFila(
                m.getNombres(), m.getApellidos(), m.getEspecialidad(),
                String.valueOf(conteo.getOrDefault(m.getUuid(), 0))));
        return r;
    }

    /**
     * Medicos con al menos una cita programada en una fecha especifica.
     */
    public Reporte medicosConCitasEnFecha(LocalDate fecha) throws PersistenciaException {
        // UUIDs de medicos con cita programada ese dia.
        Map<UUID, Integer> programadasEseDia = new HashMap<>();
        for (Cita c : citas.listarPorFecha(fecha)) {
            if (c.getEstado() == EstadoCita.PROGRAMADA) {
                programadasEseDia.merge(c.getUuidMedico(), 1, Integer::sum);
            }
        }

        Reporte r = new Reporte("Reporte de medicos con citas programadas para el "
                + fecha.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                List.of("Nombres", "Apellidos", "Especialidad", "Citas ese dia"));
        for (Medico m : medicos.listarTodos()) {
            Integer cant = programadasEseDia.get(m.getUuid());
            if (cant != null) {
                r.agregarFila(m.getNombres(), m.getApellidos(), m.getEspecialidad(),
                        String.valueOf(cant));
            }
        }
        return r;
    }

    // ------------------------------------------------------------------
    // Reportes de citas
    // ------------------------------------------------------------------
    /**
     * Todas las citas.
     */
    public Reporte citasCompleto() throws PersistenciaException {
        return construirReporteCitas("Reporte completo de citas", citas.listarTodos());
    }

    /**
     * Citas en un rango de fechas.
     */
    public Reporte citasPorRango(LocalDate desde, LocalDate hasta) throws PersistenciaException {
        return construirReporteCitas("Reporte de citas por rango de fechas",
                citas.listarPorRangoFechas(desde, hasta));
    }

    /**
     * Citas de un medico.
     */
    public Reporte citasPorMedico(UUID uuidMedico) throws PersistenciaException {
        return construirReporteCitas("Reporte de citas por medico",
                citas.listarPorMedico(uuidMedico));
    }

    /**
     * Citas de un paciente.
     */
    public Reporte citasPorPaciente(String dpi) throws PersistenciaException {
        return construirReporteCitas("Reporte de citas por paciente",
                citas.listarPorPaciente(dpi));
    }

    /**
     * Citas en un estado dado.
     */
    public Reporte citasPorEstado(EstadoCita estado) throws PersistenciaException {
        return construirReporteCitas("Reporte de citas en estado " + estado.getDescripcion(),
                citas.listarPorEstado(estado));
    }

    /**
     * Cantidad de citas (reales) agrupadas por especialidad, de mayor a menor.
     */
    public Reporte cantidadCitasPorEspecialidad() throws PersistenciaException {
        // Mapa UUID de medico -> su especialidad.
        Map<UUID, String> espDeMedico = new HashMap<>();
        for (Medico m : medicos.listarTodos()) {
            espDeMedico.put(m.getUuid(), m.getEspecialidad());
        }

        // Contar citas reales por especialidad, yendo cita -> medico -> especialidad.
        Map<String, Integer> conteo = new HashMap<>();
        for (Cita c : citas.listarTodos()) {
            if (!cuentaComoReal(c)) {
                continue;
            }
            String esp = espDeMedico.getOrDefault(c.getUuidMedico(), "(medico eliminado)");
            conteo.merge(esp, 1, Integer::sum);
        }

        Reporte r = new Reporte("Reporte de cantidad de citas por especialidad",
                List.of("Especialidad", "Cantidad de citas"));
        conteo.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> r.agregarFila(e.getKey(), String.valueOf(e.getValue())));
        return r;
    }

    // ------------------------------------------------------------------
    // Reporte de logs
    // ------------------------------------------------------------------
    /**
     * Bitacora completa de interacciones.
     */
    public Reporte logsCompleto() throws PersistenciaException {
        Reporte r = new Reporte("Reporte de bitacora (logs)",
                List.of("Fecha y hora", "Usuario", "Modulo", "Accion", "Detalle"));
        for (RegistroLog log : logs.listarTodos()) {
            r.agregarFila(log.getMarcaTiempoFormateada(), log.getUsuario(),
                    texto(log.getModulo()), texto(log.getAccion()), log.getDetalle());
        }
        return r;
    }

    // ------------------------------------------------------------------
    // Ayudas internas
    // ------------------------------------------------------------------
    // Arma un reporte de citas con nombres de paciente y medico resueltos.
    private Reporte construirReporteCitas(String titulo, List<Cita> lista)
            throws PersistenciaException {
        // Mapas para traducir DPI y UUID a nombres.
        Map<String, String> nomPac = new HashMap<>();
        for (Paciente p : pacientes.listarTodos()) {
            nomPac.put(p.getIdentificacion(), p.getNombreCompleto());
        }
        Map<UUID, String> nomMed = new HashMap<>();
        for (Medico m : medicos.listarTodos()) {
            nomMed.put(m.getUuid(), m.getNombreCompleto());
        }

        Reporte r = new Reporte(titulo,
                List.of("Fecha", "Horario", "Paciente", "Medico", "Motivo", "Estado", "Observaciones"));
        for (Cita c : lista) {
            r.agregarFila(
                    c.getFechaFormateada(),
                    c.getHorarioFormateado(),
                    nomPac.getOrDefault(c.getIdentificacionPaciente(), c.getIdentificacionPaciente()),
                    nomMed.getOrDefault(c.getUuidMedico(), c.getUuidCorto()),
                    c.getMotivo(),
                    texto(c.getEstado()),
                    c.getObservaciones());
        }
        return r;
    }

    // Convierte un objeto a texto, cuidando los nulos.
    private String texto(Object o) {
        return o == null ? "" : o.toString();
    }
}
