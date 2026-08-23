package com.archivos.sistemagestionclinicamedica.persistencia;

import com.archivos.sistemagestionclinicamedica.excepcion.PersistenciaException;
import com.archivos.sistemagestionclinicamedica.excepcion.RegistroDuplicadoException;
import com.archivos.sistemagestionclinicamedica.modelo.Paciente;
import com.archivos.sistemagestionclinicamedica.modelo.enums.Sexo;
import com.archivos.sistemagestionclinicamedica.modelo.enums.TipoSangre;
import com.archivos.sistemagestionclinicamedica.persistencia.util.BufferRegistro;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Archivo de pacientes.
 *
 * Es como ArchivoLogs pero con una cosa nueva: mantiene un indice en memoria
 * (un HashMap) que relaciona la identificacion de cada paciente con su posicion
 * en el archivo. Sirve para buscar por identificacion sin tener que recorrer
 * todo el archivo: se consulta el mapa y se salta directo a esa posicion.
 *
 * El indice guarda solo la identificacion (texto) y la posicion (un long), no
 * al paciente completo. Es liviano: aunque hubiera 50.000 pacientes, el mapa
 * ocupa unos pocos MB.
 *
 * El mapa se llena solo al abrir el archivo (metodo alIndexar, que hereda de
 * ArchivoBase) y se mantiene al dia en cada alta, cambio y baja.
 *
 * Registro de 407 bytes: eliminado 1 byte identificacion 26 bytes (13
 * caracteres) nombres 100 bytes (50) apellidos 100 bytes (50) fechaNacimiento 8
 * bytes sexo 1 byte telefono 30 bytes (15) correo 120 bytes (60) tipoSangre 1
 * byte
 */
public final class ArchivoPacientes extends ArchivoBase<Paciente> {

    public static final String NOMBRE_ARCHIVO = "pacientes.dat";

    private static final int TAMANIO_DATOS
            = BufferRegistro.bytesDeCadena(Paciente.CARACTERES_IDENTIFICACION)
            + BufferRegistro.bytesDeCadena(Paciente.CARACTERES_NOMBRES)
            + BufferRegistro.bytesDeCadena(Paciente.CARACTERES_APELLIDOS)
            + Long.BYTES // fechaNacimiento
            + 1 // sexo
            + BufferRegistro.bytesDeCadena(Paciente.CARACTERES_TELEFONO)
            + BufferRegistro.bytesDeCadena(Paciente.CARACTERES_CORREO)
            + 1;                                                           // tipoSangre

    // Indice: identificacion -> posicion en el archivo.
    private final Map<String, Long> indicePorIdentificacion = new HashMap<>();

    public ArchivoPacientes(Path ruta) throws PersistenciaException {
        super(ruta, TAMANIO_DATOS);
        inicializar();   // dispara el barrido que llama a alIndexar en cada registro
    }

    // ArchivoBase llama a este metodo una vez por cada paciente vigente durante
    // el barrido inicial. Aca aprovechamos para llenar el indice.
    @Override
    protected void alIndexar(long posicion, Paciente paciente) {
        indicePorIdentificacion.put(paciente.getIdentificacion(), posicion);
    }

    @Override
    protected void escribirDatos(Paciente p, BufferRegistro buffer) {
        buffer.escribirCadena(p.getIdentificacion(), Paciente.CARACTERES_IDENTIFICACION);
        buffer.escribirCadena(p.getNombres(), Paciente.CARACTERES_NOMBRES);
        buffer.escribirCadena(p.getApellidos(), Paciente.CARACTERES_APELLIDOS);
        buffer.escribirFecha(p.getFechaNacimiento());
        buffer.escribirEnum(p.getSexo());
        buffer.escribirCadena(p.getTelefono(), Paciente.CARACTERES_TELEFONO);
        buffer.escribirCadena(p.getCorreo(), Paciente.CARACTERES_CORREO);
        buffer.escribirEnum(p.getTipoSangre());
    }

    @Override
    protected Paciente leerDatos(BufferRegistro buffer) {
        // Mismo orden que escribirDatos.
        String identificacion = buffer.leerCadena(Paciente.CARACTERES_IDENTIFICACION);
        String nombres = buffer.leerCadena(Paciente.CARACTERES_NOMBRES);
        String apellidos = buffer.leerCadena(Paciente.CARACTERES_APELLIDOS);
        var fechaNacimiento = buffer.leerFecha();
        Sexo sexo = buffer.leerEnum(Sexo.values());
        String telefono = buffer.leerCadena(Paciente.CARACTERES_TELEFONO);
        String correo = buffer.leerCadena(Paciente.CARACTERES_CORREO);
        TipoSangre tipoSangre = buffer.leerEnum(TipoSangre.values());
        return new Paciente(identificacion, nombres, apellidos, fechaNacimiento,
                sexo, telefono, correo, tipoSangre);
    }

    // ------------------------------------------------------------------
    // Operaciones propias de pacientes
    // ------------------------------------------------------------------
    /**
     * Da de alta un paciente nuevo. Falla si la identificacion ya existe.
     */
    public void agregar(Paciente paciente) throws PersistenciaException, RegistroDuplicadoException {
        if (indicePorIdentificacion.containsKey(paciente.getIdentificacion())) {
            throw new RegistroDuplicadoException(
                    "Ya existe un paciente con la identificacion " + paciente.getIdentificacion());
        }
        long posicion = insertar(paciente);
        indicePorIdentificacion.put(paciente.getIdentificacion(), posicion);
    }

    /**
     * Busca un paciente por su identificacion usando el indice (rapido).
     *
     * @return el paciente, o vacio si no existe
     */
    public Optional<Paciente> buscarPorIdentificacion(String identificacion)
            throws PersistenciaException {
        Long posicion = indicePorIdentificacion.get(identificacion);
        if (posicion == null) {
            return Optional.empty();
        }
        return leerPorPosicion(posicion);
    }

    /**
     * Modifica un paciente existente. La identificacion no se puede cambiar (es
     * la llave), asi que se usa para ubicar el registro.
     */
    public void modificar(Paciente paciente) throws PersistenciaException {
        Long posicion = indicePorIdentificacion.get(paciente.getIdentificacion());
        if (posicion == null) {
            throw new PersistenciaException(
                    "No existe el paciente que se quiere modificar: " + paciente.getIdentificacion());
        }
        actualizar(posicion, paciente);
        // La posicion no cambia al actualizar in-place, pero reasignamos por claridad.
        indicePorIdentificacion.put(paciente.getIdentificacion(), posicion);
    }

    /**
     * Elimina un paciente por su identificacion (borrado logico) y lo saca del
     * indice.
     */
    public void eliminar(String identificacion) throws PersistenciaException {
        Long posicion = indicePorIdentificacion.get(identificacion);
        if (posicion == null) {
            throw new PersistenciaException("No existe el paciente a eliminar: " + identificacion);
        }
        eliminarLogico(posicion);
        indicePorIdentificacion.remove(identificacion);
    }

    /**
     * Dice si ya existe un paciente con esa identificacion. Lo usa el servicio
     * para validar antes de dar de alta.
     */
    public boolean existe(String identificacion) {
        return indicePorIdentificacion.containsKey(identificacion);
    }

    /**
     * Cantidad de pacientes registrados (usa el indice, no recorre el archivo).
     */
    public int cantidad() {
        return indicePorIdentificacion.size();
    }
}
