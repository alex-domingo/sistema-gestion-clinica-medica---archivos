package com.archivos.sistemagestionclinicamedica.util;

import java.util.regex.Pattern;

/**
 * Validaciones de formato que se usan en varios modulos (pacientes, medicos).
 *
 * Se pone aparte para no repetir el mismo codigo de validacion de correo o
 * telefono en cada servicio.
 */
public final class ValidadorFormato {

    // Patron de correo: algo@algo.algo. No es la regla oficial completa de
    // correos (que es enorme), pero cubre los casos normales sin dar falsos
    // rechazos a correos validos.
    private static final Pattern CORREO
            = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$");

    // Telefono: solo digitos, espacios y guiones. Entre 7 y 15 caracteres.
    private static final Pattern TELEFONO
            = Pattern.compile("^[0-9\\s-]{7,15}$");

    // Solo digitos.
    private static final Pattern SOLO_DIGITOS = Pattern.compile("^[0-9]+$");

    // Hora en formato HH:mm de 24 horas (00:00 a 23:59).
    private static final Pattern HORA_24
            = Pattern.compile("^([01][0-9]|2[0-3]):[0-5][0-9]$");

    private ValidadorFormato() {
    }

    public static boolean correoValido(String correo) {
        return correo != null && CORREO.matcher(correo).matches();
    }

    public static boolean telefonoValido(String telefono) {
        return telefono != null && TELEFONO.matcher(telefono).matches();
    }

    /**
     * Verifica que el texto tenga exactamente la cantidad de digitos indicada.
     * Se usa para el DPI (13 digitos).
     */
    public static boolean esDigitosExactos(String texto, int cantidad) {
        return texto != null
                && texto.length() == cantidad
                && SOLO_DIGITOS.matcher(texto).matches();
    }

    // Dice si un texto esta vacio o es solo espacios (o null).
    public static boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    /**
     * Verifica que el texto sea una hora valida en formato HH:mm (24 horas).
     */
    public static boolean horaValida(String texto) {
        return texto != null && HORA_24.matcher(texto.trim()).matches();
    }
}
