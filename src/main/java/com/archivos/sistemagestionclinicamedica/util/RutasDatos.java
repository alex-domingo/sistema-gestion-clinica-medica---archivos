package com.archivos.sistemagestionclinicamedica.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Arma las rutas de los archivos de datos.
 *
 * Se usa Paths.get en lugar de concatenar texto con "\" o "/", porque cada
 * sistema operativo usa un separador distinto. Asi el mismo JAR corre igual en
 * Windows y en Linux.
 */
public final class RutasDatos {

    public static final String DIRECTORIO = "datos";

    // Constructor privado: es una clase de utilidad, no se instancia.
    private RutasDatos() {
    }

    /**
     * Directorio donde viven los archivos .dat, junto a donde se ejecuta la
     * app.
     */
    public static Path directorioDatos() {
        return Paths.get(System.getProperty("user.dir"), DIRECTORIO);
    }

    /**
     * Ruta completa de un archivo dentro del directorio de datos.
     *
     * @param nombreArchivo por ejemplo "pacientes.dat"
     */
    public static Path archivo(String nombreArchivo) {
        return directorioDatos().resolve(nombreArchivo);
    }
}
