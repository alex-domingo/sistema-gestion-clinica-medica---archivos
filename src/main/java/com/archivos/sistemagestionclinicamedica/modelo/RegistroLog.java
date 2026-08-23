package com.archivos.sistemagestionclinicamedica.modelo;

import com.archivos.sistemagestionclinicamedica.modelo.enums.Accion;
import com.archivos.sistemagestionclinicamedica.modelo.enums.Modulo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Una entrada de la bitacora: representa algo que un usuario hizo en algun
 * modulo del sistema.
 *
 * Los campos son final porque una entrada de bitacora, una vez creada, no se
 * cambia. La bitacora solo crece, nunca se edita ni se borra.
 *
 * Las constantes de tamanio (cuantos caracteres mide cada campo de texto) se
 * definen aca y las usa ArchivoLogs para calcular el tamanio del registro. Asi
 * el modelo y el archivo nunca se descoordinan.
 */
public class RegistroLog {

    public static final int CARACTERES_USUARIO = 30;
    public static final int CARACTERES_DETALLE = 150;

    // Formato para mostrar la fecha y hora en pantalla.
    private static final DateTimeFormatter FORMATO
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final String usuario;
    private final LocalDateTime marcaTiempo;
    private final Modulo modulo;
    private final Accion accion;
    private final String detalle;

    public RegistroLog(String usuario, LocalDateTime marcaTiempo, Modulo modulo,
            Accion accion, String detalle) {
        this.usuario = usuario;
        this.marcaTiempo = marcaTiempo;
        this.modulo = modulo;
        this.accion = accion;
        this.detalle = detalle;
    }

    public String getUsuario() {
        return usuario;
    }

    public LocalDateTime getMarcaTiempo() {
        return marcaTiempo;
    }

    public Modulo getModulo() {
        return modulo;
    }

    public Accion getAccion() {
        return accion;
    }

    public String getDetalle() {
        return detalle;
    }

    // Devuelve la fecha y hora ya formateada para mostrar en la tabla.
    public String getMarcaTiempoFormateada() {
        return marcaTiempo == null ? "" : marcaTiempo.format(FORMATO);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | %s | %s",
                getMarcaTiempoFormateada(), usuario, modulo, accion, detalle);
    }
}
