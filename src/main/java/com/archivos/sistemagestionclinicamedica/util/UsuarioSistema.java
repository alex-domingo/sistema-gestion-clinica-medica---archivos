package com.archivos.sistemagestionclinicamedica.util;

/**
 * Devuelve quien esta usando la aplicacion.
 *
 * Como el sistema no tiene login, la bitacora atribuye cada accion al usuario
 * de la sesion del sistema operativo.
 */
public final class UsuarioSistema {

    private static final String USUARIO_DESCONOCIDO = "desconocido";

    private UsuarioSistema() {
    }

    public static String nombre() {
        String usuario = System.getProperty("user.name");
        // La propiedad puede no existir en algunos entornos, por eso el respaldo.
        if (usuario == null || usuario.isBlank()) {
            return USUARIO_DESCONOCIDO;
        }
        return usuario;
    }
}
