package com.archivos.sistemagestionclinicamedica.persistencia;

import com.archivos.sistemagestionclinicamedica.excepcion.PersistenciaException;
import com.archivos.sistemagestionclinicamedica.persistencia.util.BufferRegistro;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * Clase base para todos los archivos de datos.
 *
 * Maneja registros de longitud fija, lo que permite llegar a cualquier registro
 * con un solo seek (posicion = numero * tamanio) sin leer los anteriores, y
 * modificar uno sin tocar los demas.
 *
 * Cada registro se guarda asi: [ eliminado : 1 byte ][ datos de la entidad :
 * tamanioDatos bytes ]
 *
 * La bandera "eliminado" es control interno del archivo. No tiene nada que ver
 * con campos de negocio parecidos, como el "activo" del medico.
 *
 * Es generica (T) y abstracta porque la forma de manejar el archivo es igual
 * para todas las entidades; lo unico que cambia es como se arma cada registro,
 * y eso lo definen las subclases en escribirDatos y leerDatos.
 *
 * @param <T> tipo de entidad que guarda el archivo
 */
public abstract class ArchivoBase<T> implements Closeable {

    // Cuantos registros se leen juntos al recorrer el archivo. Se lee en
    // bloques y no de a uno porque RandomAccessFile no tiene buffer y hacerlo
    // de a uno seria muy lento con archivos grandes.
    private static final int REGISTROS_POR_BLOQUE = 64;

    // La bandera de eliminado ocupa 1 byte al inicio de cada registro.
    private static final int BYTES_BANDERA = 1;

    private final RandomAccessFile archivo;
    private final Path ruta;
    private final int tamanioDatos;      // bytes de los datos, sin la bandera
    private final int tamanioRegistro;   // bandera + datos

    // Posiciones de registros borrados, listas para reutilizarse.
    private final Deque<Long> posicionesLibres = new ArrayDeque<>();

    /**
     * Abre el archivo en lectura/escritura. Si no existe la carpeta o el
     * archivo, los crea.
     *
     * @param ruta donde esta el archivo
     * @param tamanioDatos bytes que ocupan los datos de la entidad
     */
    protected ArchivoBase(Path ruta, int tamanioDatos) throws PersistenciaException {
        this.ruta = ruta;
        this.tamanioDatos = tamanioDatos;
        this.tamanioRegistro = BYTES_BANDERA + tamanioDatos;
        try {
            Path carpeta = ruta.getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
            this.archivo = new RandomAccessFile(ruta.toFile(), "rw");
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo abrir el archivo: " + ruta, e);
        }
    }

    // ------------------------------------------------------------------
    // Lo que cada subclase debe definir
    // ------------------------------------------------------------------
    // Escribe los campos de la entidad en el buffer. Debe seguir el MISMO
    // orden que leerDatos.
    protected abstract void escribirDatos(T entidad, BufferRegistro buffer);

    // Reconstruye la entidad leyendo del buffer, en el mismo orden que escribirDatos.
    protected abstract T leerDatos(BufferRegistro buffer);

    // Las subclases que usan indices en memoria sobreescriben esto para
    // llenarlos durante el barrido inicial. Por defecto no hace nada.
    protected void alIndexar(long posicion, T entidad) {
    }

    /**
     * Recorre el archivo una vez al abrirlo para llenar los indices de las
     * subclases y armar la lista de huecos. Se llama al final del constructor
     * de cada archivo concreto.
     */
    protected final void inicializar() throws PersistenciaException {
        posicionesLibres.clear();
        escanear(true, this::alIndexar);
    }

    // ------------------------------------------------------------------
    // Operaciones principales
    // ------------------------------------------------------------------
    /**
     * Inserta una entidad. Si hay un hueco de un borrado anterior lo reutiliza;
     * si no, la agrega al final.
     *
     * @return la posicion donde quedo guardada
     */
    public long insertar(T entidad) throws PersistenciaException {
        long posicion = posicionesLibres.isEmpty() ? longitud() : posicionesLibres.pop();
        escribirRegistro(posicion, entidad, false);
        return posicion;
    }

    /**
     * Sobreescribe el registro que esta en esa posicion.
     */
    public void actualizar(long posicion, T entidad) throws PersistenciaException {
        escribirRegistro(posicion, entidad, false);
    }

    /**
     * Borrado logico: marca el registro como eliminado y guarda su posicion
     * para reutilizarla. Los datos siguen en el archivo pero ya no se ven.
     */
    public void eliminarLogico(long posicion) throws PersistenciaException {
        try {
            archivo.seek(posicion);
            archivo.writeBoolean(true);
            if (!posicionesLibres.contains(posicion)) {
                posicionesLibres.push(posicion);
            }
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo eliminar en la posicion " + posicion, e);
        }
    }

    /**
     * Lee el registro de esa posicion.
     *
     * @return la entidad, o vacio si el registro estaba eliminado
     */
    public Optional<T> leerPorPosicion(long posicion) throws PersistenciaException {
        try {
            if (posicion < 0 || posicion + tamanioRegistro > archivo.length()) {
                throw new PersistenciaException("Posicion fuera del archivo: " + posicion);
            }
            byte[] crudo = new byte[tamanioRegistro];
            archivo.seek(posicion);
            archivo.readFully(crudo);

            // El primer byte es la bandera. Si esta encendida, esta borrado.
            if (crudo[0] != 0) {
                return Optional.empty();
            }
            BufferRegistro buffer = BufferRegistro.paraLectura(crudo, BYTES_BANDERA, tamanioDatos);
            return Optional.of(leerDatos(buffer));
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer en la posicion " + posicion, e);
        }
    }

    /**
     * Devuelve todos los registros que no estan borrados.
     */
    public List<T> listarTodos() throws PersistenciaException {
        List<T> lista = new ArrayList<>();
        escanear(false, (posicion, entidad) -> lista.add(entidad));
        return lista;
    }

    /**
     * Recorre los registros vigentes pasando cada uno con su posicion. Sirve
     * cuando no queremos cargar todo en una lista al mismo tiempo.
     */
    public void recorrer(RecorridoRegistro<T> accion) throws PersistenciaException {
        escanear(false, accion);
    }

    /**
     * Cuenta los registros vigentes.
     */
    public int contar() throws PersistenciaException {
        int[] total = {0};   // arreglo de 1 para poder modificarlo dentro del lambda
        escanear(false, (posicion, entidad) -> total[0]++);
        return total[0];
    }

    // ------------------------------------------------------------------
    // Parte interna
    // ------------------------------------------------------------------
    // Arma el registro completo en memoria y lo escribe de una sola vez.
    private void escribirRegistro(long posicion, T entidad, boolean eliminado)
            throws PersistenciaException {
        BufferRegistro buffer = BufferRegistro.paraEscritura(tamanioRegistro);
        buffer.escribirBooleano(eliminado);
        escribirDatos(entidad, buffer);
        try {
            archivo.seek(posicion);
            archivo.write(buffer.contenido());
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo escribir en la posicion " + posicion, e);
        }
    }

    // Recorre todo el archivo leyendo de a bloques de REGISTROS_POR_BLOQUE.
    private void escanear(boolean registrarLibres, RecorridoRegistro<T> accion)
            throws PersistenciaException {
        try {
            long total = archivo.length();
            if (total < tamanioRegistro) {
                return;   // archivo vacio
            }
            byte[] bloque = new byte[tamanioRegistro * REGISTROS_POR_BLOQUE];
            long inicioBloque = 0L;
            archivo.seek(0L);

            while (inicioBloque < total) {
                int porLeer = (int) Math.min(bloque.length, total - inicioBloque);
                // Descartamos cualquier resto que no complete un registro entero.
                // Esto protege si el archivo quedo cortado por un cierre brusco.
                porLeer -= porLeer % tamanioRegistro;
                if (porLeer <= 0) {
                    break;
                }
                archivo.readFully(bloque, 0, porLeer);

                for (int off = 0; off < porLeer; off += tamanioRegistro) {
                    long posicion = inicioBloque + off;
                    boolean eliminado = bloque[off] != 0;

                    if (eliminado) {
                        if (registrarLibres) {
                            posicionesLibres.push(posicion);
                        }
                        continue;
                    }
                    BufferRegistro buffer
                            = BufferRegistro.paraLectura(bloque, off + BYTES_BANDERA, tamanioDatos);
                    accion.procesar(posicion, leerDatos(buffer));
                }
                inicioBloque += porLeer;
            }
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo recorrer el archivo: " + ruta, e);
        }
    }

    // Tamanio actual del archivo en bytes.
    private long longitud() throws PersistenciaException {
        try {
            return archivo.length();
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo consultar el tamanio del archivo", e);
        }
    }

    // ------------------------------------------------------------------
    // Consultas de estado
    // ------------------------------------------------------------------
    public int tamanioRegistro() {
        return tamanioRegistro;
    }

    public int posicionesLibresDisponibles() {
        return posicionesLibres.size();
    }

    public Path ruta() {
        return ruta;
    }

    @Override
    public void close() throws IOException {
        archivo.close();
    }

    /**
     * Accion que se aplica a cada registro durante un recorrido. Es una
     * interfaz funcional para poder usar lambdas.
     */
    @FunctionalInterface
    public interface RecorridoRegistro<T> {

        void procesar(long posicion, T entidad) throws PersistenciaException;
    }
}
