package com.archivos.sistemagestionclinicamedica.persistencia;

import com.archivos.sistemagestionclinicamedica.excepcion.PersistenciaException;
import com.archivos.sistemagestionclinicamedica.modelo.Medico;
import com.archivos.sistemagestionclinicamedica.persistencia.util.BufferRegistro;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Archivo de medicos.
 *
 * Es muy parecido a ArchivoPacientes, con una diferencia clave: la llave del
 * indice es un UUID, no un texto. El paciente usaba su DPI (que escribe el
 * usuario), pero el UUID del medico lo genera el sistema al darlo de alta.
 *
 * Fijarse que ArchivoBase no cambio en nada para soportar esta llave distinta:
 * la clase base no sabe ni le importa cual es la llave; eso lo maneja cada
 * archivo concreto con su propio indice.
 *
 * Registro de 377 bytes: eliminado 1 byte activo 1 byte (estado del medico,
 * distinto de eliminado) uuid 16 bytes (2 long) nombres 100 bytes (50
 * caracteres) apellidos 100 bytes (50) especialidad 80 bytes (40, nombre que
 * referencia al catalogo) telefono 30 bytes (15) correo 120 bytes (60)
 * horaInicio 4 bytes (int, segundos desde medianoche) horaFin 4 bytes
 */
public final class ArchivoMedicos extends ArchivoBase<Medico> {

    public static final String NOMBRE_ARCHIVO = "medicos.dat";

    // Ancho fijo del nombre de la especialidad dentro del registro del medico.
    private static final int CARACTERES_ESPECIALIDAD = 40;

    private static final int TAMANIO_DATOS
            = 1 // activo
            + 16 // uuid
            + BufferRegistro.bytesDeCadena(Medico.CARACTERES_NOMBRES)
            + BufferRegistro.bytesDeCadena(Medico.CARACTERES_APELLIDOS)
            + BufferRegistro.bytesDeCadena(CARACTERES_ESPECIALIDAD)
            + BufferRegistro.bytesDeCadena(Medico.CARACTERES_TELEFONO)
            + BufferRegistro.bytesDeCadena(Medico.CARACTERES_CORREO)
            + Integer.BYTES // horaInicio
            + Integer.BYTES;                                             // horaFin

    // Indice: uuid -> posicion en el archivo.
    private final Map<UUID, Long> indicePorUuid = new HashMap<>();

    public ArchivoMedicos(Path ruta) throws PersistenciaException {
        super(ruta, TAMANIO_DATOS);
        inicializar();
    }

    @Override
    protected void alIndexar(long posicion, Medico medico) {
        indicePorUuid.put(medico.getUuid(), posicion);
    }

    @Override
    protected void escribirDatos(Medico m, BufferRegistro buffer) {
        buffer.escribirBooleano(m.isActivo());
        buffer.escribirUUID(m.getUuid());
        buffer.escribirCadena(m.getNombres(), Medico.CARACTERES_NOMBRES);
        buffer.escribirCadena(m.getApellidos(), Medico.CARACTERES_APELLIDOS);
        buffer.escribirCadena(m.getEspecialidad(), CARACTERES_ESPECIALIDAD);
        buffer.escribirCadena(m.getTelefono(), Medico.CARACTERES_TELEFONO);
        buffer.escribirCadena(m.getCorreo(), Medico.CARACTERES_CORREO);
        buffer.escribirHora(m.getHoraInicio());
        buffer.escribirHora(m.getHoraFin());
    }

    @Override
    protected Medico leerDatos(BufferRegistro buffer) {
        // Mismo orden que escribirDatos.
        boolean activo = buffer.leerBooleano();
        UUID uuid = buffer.leerUUID();
        String nombres = buffer.leerCadena(Medico.CARACTERES_NOMBRES);
        String apellidos = buffer.leerCadena(Medico.CARACTERES_APELLIDOS);
        String especialidad = buffer.leerCadena(CARACTERES_ESPECIALIDAD);
        String telefono = buffer.leerCadena(Medico.CARACTERES_TELEFONO);
        String correo = buffer.leerCadena(Medico.CARACTERES_CORREO);
        var horaInicio = buffer.leerHora();
        var horaFin = buffer.leerHora();
        return new Medico(uuid, nombres, apellidos, especialidad, telefono,
                correo, horaInicio, horaFin, activo);
    }

    // ------------------------------------------------------------------
    // Operaciones propias de medicos
    // ------------------------------------------------------------------
    /**
     * Da de alta un medico. Le genera un UUID nuevo aca mismo (no viene del
     * formulario) y lo devuelve por si se necesita.
     */
    public UUID agregar(Medico medico) throws PersistenciaException {
        // Generar el UUID unico. La probabilidad de que se repita es
        // practicamente nula, pero por las dudas se verifica.
        UUID uuid;
        do {
            uuid = UUID.randomUUID();
        } while (indicePorUuid.containsKey(uuid));

        medico.setUuid(uuid);
        long posicion = insertar(medico);
        indicePorUuid.put(uuid, posicion);
        return uuid;
    }

    /**
     * Busca un medico por su UUID usando el indice.
     */
    public Optional<Medico> buscarPorUuid(UUID uuid) throws PersistenciaException {
        Long posicion = indicePorUuid.get(uuid);
        if (posicion == null) {
            return Optional.empty();
        }
        return leerPorPosicion(posicion);
    }

    /**
     * Modifica un medico existente. Se ubica por su UUID, que no cambia.
     */
    public void modificar(Medico medico) throws PersistenciaException {
        Long posicion = indicePorUuid.get(medico.getUuid());
        if (posicion == null) {
            throw new PersistenciaException(
                    "No existe el medico que se quiere modificar: " + medico.getUuid());
        }
        actualizar(posicion, medico);
    }

    /**
     * Elimina un medico por su UUID (borrado logico) y lo saca del indice.
     */
    public void eliminar(UUID uuid) throws PersistenciaException {
        Long posicion = indicePorUuid.get(uuid);
        if (posicion == null) {
            throw new PersistenciaException("No existe el medico a eliminar: " + uuid);
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
}
