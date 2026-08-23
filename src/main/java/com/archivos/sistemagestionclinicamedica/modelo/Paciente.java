package com.archivos.sistemagestionclinicamedica.modelo;

import com.archivos.sistemagestionclinicamedica.modelo.enums.Sexo;
import com.archivos.sistemagestionclinicamedica.modelo.enums.TipoSangre;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

/**
 * Un paciente de la clinica.
 *
 * A diferencia del RegistroLog, este modelo SI se puede modificar (el paciente
 * puede cambiar de telefono, correo, etc.), por eso los campos no son final y
 * tienen sus setters.
 *
 * La identificacion es la llave unica: es el numero de DPI, de 13 digitos.
 * Nombres y apellidos son obligatorios; el correo es opcional.
 *
 * Las constantes CARACTERES_* definen el ancho fijo de cada campo de texto en
 * el archivo. ArchivoPacientes las usa para calcular el tamanio del registro,
 * asi el modelo y el archivo nunca se descoordinan.
 */
public class Paciente {

    public static final int CARACTERES_IDENTIFICACION = 13;
    public static final int CARACTERES_NOMBRES = 50;
    public static final int CARACTERES_APELLIDOS = 50;
    public static final int CARACTERES_TELEFONO = 15;
    public static final int CARACTERES_CORREO = 60;

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String identificacion;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private Sexo sexo;
    private String telefono;
    private String correo;        // puede ser null o vacio, es opcional
    private TipoSangre tipoSangre;

    // Constructor vacio: lo necesita la interfaz para ir llenando el objeto
    // campo por campo desde el formulario.
    public Paciente() {
    }

    public Paciente(String identificacion, String nombres, String apellidos,
            LocalDate fechaNacimiento, Sexo sexo, String telefono,
            String correo, TipoSangre tipoSangre) {
        this.identificacion = identificacion;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.telefono = telefono;
        this.correo = correo;
        this.tipoSangre = tipoSangre;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
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

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
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

    public TipoSangre getTipoSangre() {
        return tipoSangre;
    }

    public void setTipoSangre(TipoSangre tipoSangre) {
        this.tipoSangre = tipoSangre;
    }

    // --- Metodos de ayuda para mostrar en la interfaz ---
    // Nombre completo, util para las tablas y los combos.
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public String getFechaNacimientoFormateada() {
        return fechaNacimiento == null ? "" : fechaNacimiento.format(FORMATO_FECHA);
    }

    // Calcula la edad a partir de la fecha de nacimiento. Se calcula al vuelo
    // en vez de guardarla, porque la edad cambia con el tiempo y guardarla
    // quedaria desactualizada.
    public int getEdad() {
        if (fechaNacimiento == null) {
            return 0;
        }
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + identificacion + ")";
    }
}
