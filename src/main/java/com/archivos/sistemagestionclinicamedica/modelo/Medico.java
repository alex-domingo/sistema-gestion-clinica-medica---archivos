package com.archivos.sistemagestionclinicamedica.modelo;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Un medico de la clinica.
 *
 * La diferencia principal con Paciente es la llave: el paciente usa su DPI (que
 * escribe el usuario), pero el medico usa un UUID que genera el sistema solo.
 * Por eso el UUID no tiene setter: se asigna al crear y no se cambia.
 *
 * Ademas tiene un estado activo/inactivo. OJO: esto NO es lo mismo que borrar.
 * Un medico inactivo sigue existiendo y se ve en la tabla; solo que no puede
 * recibir citas nuevas. Borrarlo seria quitarlo del todo.
 */
public class Medico {

    public static final int CARACTERES_NOMBRES = 50;
    public static final int CARACTERES_APELLIDOS = 50;
    public static final int CARACTERES_TELEFONO = 15;
    public static final int CARACTERES_CORREO = 60;

    private static final DateTimeFormatter FORMATO_HORA
            = DateTimeFormatter.ofPattern("HH:mm");

    private UUID uuid;
    private String nombres;
    private String apellidos;
    private String especialidad;   // nombre de la especialidad (referencia al catalogo)
    private String telefono;
    private String correo;          // opcional
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private boolean activo;

    // Constructor vacio: lo usa el formulario para ir llenando el objeto.
    public Medico() {
    }

    public Medico(UUID uuid, String nombres, String apellidos, String especialidad,
            String telefono, String correo, LocalTime horaInicio, LocalTime horaFin,
            boolean activo) {
        this.uuid = uuid;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.especialidad = especialidad;
        this.telefono = telefono;
        this.correo = correo;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.activo = activo;
    }

    public UUID getUuid() {
        return uuid;
    }

    // El UUID se asigna una sola vez, cuando se crea el medico. No hay setter
    // publico para que nadie lo cambie despues (es la llave).
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // --- Metodos de ayuda para la interfaz ---
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public String getHoraInicioFormateada() {
        return horaInicio == null ? "" : horaInicio.format(FORMATO_HORA);
    }

    public String getHoraFinFormateada() {
        return horaFin == null ? "" : horaFin.format(FORMATO_HORA);
    }

    // Muestra el horario como "08:00 - 16:00", para la tabla.
    public String getHorarioFormateado() {
        return getHoraInicioFormateada() + " - " + getHoraFinFormateada();
    }

    // Texto del estado, para mostrar en la tabla.
    public String getEstadoTexto() {
        return activo ? "Activo" : "Inactivo";
    }

    // UUID acortado para mostrar en la tabla (el completo es muy largo).
    public String getUuidCorto() {
        if (uuid == null) {
            return "";
        }
        String texto = uuid.toString();
        return texto.substring(0, 8);
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + especialidad + ")";
    }
}
