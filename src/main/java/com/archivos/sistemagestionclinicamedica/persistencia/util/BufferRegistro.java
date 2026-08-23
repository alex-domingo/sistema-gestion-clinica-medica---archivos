package com.archivos.sistemagestionclinicamedica.persistencia.util;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Se encarga de convertir los campos de una entidad en bytes de ancho fijo y de
 * volver a leerlos.
 *
 * La idea es que todo el registro se arma primero en memoria, dentro de un
 * ByteBuffer, y despues se escribe al archivo de una sola vez. Se hace asi
 * porque RandomAccessFile no tiene buffer propio: si escribieramos campo por
 * campo, cada llamada podria ir directo al disco y seria lento.
 *
 * Las cadenas se guardan siempre con la misma cantidad de caracteres,
 * rellenando lo que sobra. No se usa writeUTF porque ese metodo guarda cadenas
 * de largo variable y entonces ya no se podria calcular la posicion exacta de
 * cada registro.
 */
public final class BufferRegistro {

    // Caracter con el que se rellenan las cadenas cortas hasta su ancho fijo.
    private static final char RELLENO = '\u0000';

    // En Java un char ocupa 2 bytes. Lo dejamos como constante para los calculos.
    public static final int BYTES_POR_CARACTER = Character.BYTES;

    // Valores que usamos para representar "vacio" en fechas y horas.
    private static final long FECHA_NULA = Long.MIN_VALUE;
    private static final int HORA_NULA = -1;

    private final ByteBuffer buffer;

    // El constructor es privado, se crea con los metodos de abajo segun si
    // vamos a escribir o a leer.
    private BufferRegistro(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * Crea un buffer vacio para escribir un registro nuevo.
     *
     * @param tamanioBytes cuantos bytes mide el registro completo
     */
    public static BufferRegistro paraEscritura(int tamanioBytes) {
        return new BufferRegistro(ByteBuffer.allocate(tamanioBytes));
    }

    /**
     * Envuelve un pedazo de un arreglo que ya leimos del disco para poder
     * interpretarlo. No copia los bytes, trabaja sobre el mismo arreglo.
     *
     * @param datos arreglo con uno o varios registros
     * @param desplazamiento donde empieza el registro dentro del arreglo
     * @param longitud cuantos bytes mide el registro
     */
    public static BufferRegistro paraLectura(byte[] datos, int desplazamiento, int longitud) {
        return new BufferRegistro(ByteBuffer.wrap(datos, desplazamiento, longitud).slice());
    }

    // Devuelve el arreglo de bytes de adentro, para poder escribirlo al archivo.
    public byte[] contenido() {
        return buffer.array();
    }

    // Cuenta cuantos bytes ocupa una cadena de cierta cantidad de caracteres.
    public static int bytesDeCadena(int caracteres) {
        return caracteres * BYTES_POR_CARACTER;
    }

    // ------------------------------------------------------------------
    // Cadenas de texto
    // ------------------------------------------------------------------
    /**
     * Escribe una cadena ocupando siempre la misma cantidad de caracteres. Si
     * el texto es mas corto rellena, y si es mas largo lo corta. Asi el
     * registro nunca cambia de tamanio.
     */
    public void escribirCadena(String valor, int maxCaracteres) {
        String texto = (valor == null) ? "" : valor;

        // Si viene mas largo que el campo, lo cortamos para no desalinear todo.
        if (texto.length() > maxCaracteres) {
            texto = texto.substring(0, maxCaracteres);
        }

        for (int i = 0; i < maxCaracteres; i++) {
            if (i < texto.length()) {
                buffer.putChar(texto.charAt(i));
            } else {
                buffer.putChar(RELLENO);   // completamos lo que falta
            }
        }
    }

    /**
     * Lee una cadena de ancho fijo y le quita el relleno.
     */
    public String leerCadena(int maxCaracteres) {
        StringBuilder sb = new StringBuilder(maxCaracteres);
        for (int i = 0; i < maxCaracteres; i++) {
            char c = buffer.getChar();
            if (c != RELLENO) {
                sb.append(c);
            }
        }
        return sb.toString().trim();
    }

    // ------------------------------------------------------------------
    // UUID
    // ------------------------------------------------------------------
    /**
     * Guarda un UUID como dos numeros long (16 bytes en total).
     *
     * Si lo guardaramos como texto seria de 36 caracteres = 72 bytes, asi que
     * de esta forma ocupa menos de la cuarta parte.
     */
    public void escribirUUID(UUID valor) {
        if (valor == null) {
            buffer.putLong(0L);
            buffer.putLong(0L);
        } else {
            buffer.putLong(valor.getMostSignificantBits());
            buffer.putLong(valor.getLeastSignificantBits());
        }
    }

    public UUID leerUUID() {
        long altos = buffer.getLong();
        long bajos = buffer.getLong();
        // Dos ceros significan que no habia UUID guardado.
        if (altos == 0L && bajos == 0L) {
            return null;
        }
        return new UUID(altos, bajos);
    }

    // ------------------------------------------------------------------
    // Fechas y horas
    // ------------------------------------------------------------------
    /**
     * Guarda una fecha como el numero de dias desde el 1 de enero de 1970. Es
     * la forma mas compacta: un solo long en vez de varios campos.
     */
    public void escribirFecha(LocalDate valor) {
        buffer.putLong(valor == null ? FECHA_NULA : valor.toEpochDay());
    }

    public LocalDate leerFecha() {
        long dias = buffer.getLong();
        return (dias == FECHA_NULA) ? null : LocalDate.ofEpochDay(dias);
    }

    /**
     * Guarda una hora como la cantidad de segundos desde la medianoche.
     */
    public void escribirHora(LocalTime valor) {
        buffer.putInt(valor == null ? HORA_NULA : valor.toSecondOfDay());
    }

    public LocalTime leerHora() {
        int segundos = buffer.getInt();
        return (segundos == HORA_NULA) ? null : LocalTime.ofSecondOfDay(segundos);
    }

    /**
     * Guarda una fecha con hora (para la bitacora) como milisegundos.
     *
     * Se usa la zona UTC para que el archivo no dependa de la zona horaria de
     * la computadora donde se ejecute.
     */
    public void escribirMarcaTiempo(LocalDateTime valor) {
        if (valor == null) {
            buffer.putLong(FECHA_NULA);
        } else {
            buffer.putLong(valor.toInstant(ZoneOffset.UTC).toEpochMilli());
        }
    }

    public LocalDateTime leerMarcaTiempo() {
        long ms = buffer.getLong();
        if (ms == FECHA_NULA) {
            return null;
        }
        // Separamos los milisegundos en segundos + nanosegundos para reconstruir.
        long segundos = Math.floorDiv(ms, 1000L);
        int nanos = (int) Math.floorMod(ms, 1000L) * 1_000_000;
        return LocalDateTime.ofEpochSecond(segundos, nanos, ZoneOffset.UTC);
    }

    // ------------------------------------------------------------------
    // Enums, booleanos y numeros
    // ------------------------------------------------------------------
    /**
     * Guarda un enum como un solo byte usando su posicion (ordinal).
     *
     * Por ejemplo, si el enum es PROGRAMADA, ATENDIDA, CANCELADA, entonces
     * ATENDIDA se guarda como 1. Se usa el numero y no el nombre porque el
     * nombre tendria largo variable.
     *
     * OJO: por esto no se puede cambiar el orden de los enums despues, porque
     * los archivos viejos quedarian mal leidos.
     */
    public void escribirEnum(Enum<?> valor) {
        buffer.put(valor == null ? (byte) -1 : (byte) valor.ordinal());
    }

    /**
     * Lee un enum a partir del numero guardado.
     *
     * @param constantes el arreglo que devuelve values() del enum
     */
    public <E extends Enum<E>> E leerEnum(E[] constantes) {
        byte ordinal = buffer.get();
        if (ordinal < 0 || ordinal >= constantes.length) {
            return null;
        }
        return constantes[ordinal];
    }

    public void escribirBooleano(boolean valor) {
        buffer.put(valor ? (byte) 1 : (byte) 0);
    }

    public boolean leerBooleano() {
        return buffer.get() != 0;
    }

    public void escribirEntero(int valor) {
        buffer.putInt(valor);
    }

    public int leerEntero() {
        return buffer.getInt();
    }

    public void escribirLargo(long valor) {
        buffer.putLong(valor);
    }

    public long leerLargo() {
        return buffer.getLong();
    }
}
