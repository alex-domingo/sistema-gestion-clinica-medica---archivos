package com.archivos.sistemagestionclinicamedica.persistencia;

import com.archivos.sistemagestionclinicamedica.excepcion.PersistenciaException;
import com.archivos.sistemagestionclinicamedica.modelo.Cita;
import com.archivos.sistemagestionclinicamedica.modelo.Paciente;
import com.archivos.sistemagestionclinicamedica.modelo.enums.EstadoCita;
import com.archivos.sistemagestionclinicamedica.persistencia.util.BufferRegistro;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Archivo de citas.
 *
 * Guarda las citas e indexa por su UUID (como ArchivoMedicos). Ademas de las
 * operaciones normales, ofrece varios metodos de consulta: por paciente, por
 * medico, por fecha y por estado.
 *
 * Esos metodos de consulta los usan tres servicios distintos: el de citas para
 * sus filtros, y los de paciente y medico para saber si pueden borrar un
 * registro (no se puede borrar un paciente o medico que tiene citas
 * programadas). Como este archivo no depende de ningun servicio, no se genera
 * ninguna dependencia circular.
 *
 * Registro de 672 bytes: eliminado 1 byte uuid 16 bytes idPaciente 26 bytes (13
 * caracteres, DPI) uuidMedico 16 bytes fecha 8 bytes horaInicio 4 bytes motivo
 * 200 bytes (100 caracteres) estado 1 byte observaciones 400 bytes (200
 * caracteres)
 */
public final class ArchivoCitas extends ArchivoBase<Cita> {

    public static final String NOMBRE_ARCHIVO = "citas.dat";

    private static final int TAMANIO_DATOS
            = 16 // uuid
            + BufferRegistro.bytesDeCadena(Paciente.CARACTERES_IDENTIFICACION)
            + 16 // uuidMedico
            + Long.BYTES // fecha
            + Integer.BYTES // horaInicio
            + BufferRegistro.bytesDeCadena(Cita.CARACTERES_MOTIVO)
            + 1 // estado
            + BufferRegistro.bytesDeCadena(Cita.CARACTERES_OBSERVACIONES);

    private final Map<UUID, Long> indicePorUuid = new HashMap<>();

    public ArchivoCitas(Path ruta) throws PersistenciaException {
        super(ruta, TAMANIO_DATOS);
        inicializar();
    }

    @Override
    protected void alIndexar(long posicion, Cita cita) {
        indicePorUuid.put(cita.getUuid(), posicion);
    }

    @Override
    protected void escribirDatos(Cita c, BufferRegistro buffer) {
        buffer.escribirUUID(c.getUuid());
        buffer.escribirCadena(c.getIdentificacionPaciente(), Paciente.CARACTERES_IDENTIFICACION);
        buffer.escribirUUID(c.getUuidMedico());
        buffer.escribirFecha(c.getFecha());
        buffer.escribirHora(c.getHoraInicio());
        buffer.escribirCadena(c.getMotivo(), Cita.CARACTERES_MOTIVO);
        buffer.escribirEnum(c.getEstado());
        buffer.escribirCadena(c.getObservaciones(), Cita.CARACTERES_OBSERVACIONES);
    }

    @Override
    protected Cita leerDatos(BufferRegistro buffer) {
        UUID uuid = buffer.leerUUID();
        String idPaciente = buffer.leerCadena(Paciente.CARACTERES_IDENTIFICACION);
        UUID uuidMedico = buffer.leerUUID();
        var fecha = buffer.leerFecha();
        var horaInicio = buffer.leerHora();
        String motivo = buffer.leerCadena(Cita.CARACTERES_MOTIVO);
        EstadoCita estado = buffer.leerEnum(EstadoCita.values());
        String observaciones = buffer.leerCadena(Cita.CARACTERES_OBSERVACIONES);
        return new Cita(uuid, idPaciente, uuidMedico, fecha, horaInicio,
                motivo, estado, observaciones);
    }

    // ------------------------------------------------------------------
    // Operaciones basicas
    // ------------------------------------------------------------------
    /**
     * Programa una cita nueva, generandole un UUID unico.
     */
    public UUID agregar(Cita cita) throws PersistenciaException {
        UUID uuid;
        do {
            uuid = UUID.randomUUID();
        } while (indicePorUuid.containsKey(uuid));

        cita.setUuid(uuid);
        long posicion = insertar(cita);
        indicePorUuid.put(uuid, posicion);
        return uuid;
    }

    public Optional<Cita> buscarPorUuid(UUID uuid) throws PersistenciaException {
        Long posicion = indicePorUuid.get(uuid);
        if (posicion == null) {
            return Optional.empty();
        }
        return leerPorPosicion(posicion);
    }

    public void modificar(Cita cita) throws PersistenciaException {
        Long posicion = indicePorUuid.get(cita.getUuid());
        if (posicion == null) {
            throw new PersistenciaException("No existe la cita que se quiere modificar.");
        }
        actualizar(posicion, cita);
    }

    public void eliminar(UUID uuid) throws PersistenciaException {
        Long posicion = indicePorUuid.get(uuid);
        if (posicion == null) {
            throw new PersistenciaException("No existe la cita a eliminar.");
        }
        eliminarLogico(posicion);
        indicePorUuid.remove(uuid);
    }

    public boolean existe(UUID uuid) {
        return indicePorUuid.containsKey(uuid);
    }

    public int cantidad() {
        return indicePorUuid.size();
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------
    /**
     * Todas las citas de un paciente, por su DPI.
     */
    public List<Cita> listarPorPaciente(String identificacionPaciente) throws PersistenciaException {
        return listarTodos().stream()
                .filter(c -> c.getIdentificacionPaciente() != null
                && c.getIdentificacionPaciente().equals(identificacionPaciente))
                .toList();
    }

    /**
     * Todas las citas de un medico, por su UUID.
     */
    public List<Cita> listarPorMedico(UUID uuidMedico) throws PersistenciaException {
        return listarTodos().stream()
                .filter(c -> uuidMedico.equals(c.getUuidMedico()))
                .toList();
    }

    /**
     * Todas las citas de una fecha.
     */
    public List<Cita> listarPorFecha(LocalDate fecha) throws PersistenciaException {
        return listarTodos().stream()
                .filter(c -> fecha.equals(c.getFecha()))
                .toList();
    }

    /**
     * Todas las citas en un estado dado.
     */
    public List<Cita> listarPorEstado(EstadoCita estado) throws PersistenciaException {
        return listarTodos().stream()
                .filter(c -> c.getEstado() == estado)
                .toList();
    }

    /**
     * Dice si un paciente tiene al menos una cita PROGRAMADA. Lo usa
     * PacienteServicio para no dejar borrar un paciente con citas pendientes.
     */
    public boolean pacienteTieneCitasProgramadas(String identificacionPaciente)
            throws PersistenciaException {
        return listarTodos().stream()
                .anyMatch(c -> c.getEstado() == EstadoCita.PROGRAMADA
                && identificacionPaciente.equals(c.getIdentificacionPaciente()));
    }

    /**
     * Dice si un medico tiene al menos una cita PROGRAMADA. Lo usa
     * MedicoServicio para el mismo fin.
     */
    public boolean medicoTieneCitasProgramadas(UUID uuidMedico) throws PersistenciaException {
        return listarTodos().stream()
                .anyMatch(c -> c.getEstado() == EstadoCita.PROGRAMADA
                && uuidMedico.equals(c.getUuidMedico()));
    }

    /**
     * Citas PROGRAMADAS de un medico. Sirve para revisar traslapes al programar
     * una cita nueva y para validar cambios de horario del medico.
     */
    public List<Cita> citasProgramadasDeMedico(UUID uuidMedico) throws PersistenciaException {
        return listarTodos().stream()
                .filter(c -> c.getEstado() == EstadoCita.PROGRAMADA
                && uuidMedico.equals(c.getUuidMedico()))
                .toList();
    }

    /**
     * Citas PROGRAMADAS de un paciente. Sirve para revisar traslapes al
     * programar una cita nueva.
     */
    public List<Cita> citasProgramadasDePaciente(String identificacionPaciente)
            throws PersistenciaException {
        return listarTodos().stream()
                .filter(c -> c.getEstado() == EstadoCita.PROGRAMADA
                && identificacionPaciente.equals(c.getIdentificacionPaciente()))
                .toList();
    }
}
