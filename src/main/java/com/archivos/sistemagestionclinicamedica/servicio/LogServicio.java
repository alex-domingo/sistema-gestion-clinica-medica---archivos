package com.archivos.sistemagestionclinicamedica.servicio;

import com.archivos.sistemagestionclinicamedica.excepcion.PersistenciaException;
import com.archivos.sistemagestionclinicamedica.modelo.RegistroLog;
import com.archivos.sistemagestionclinicamedica.modelo.enums.Accion;
import com.archivos.sistemagestionclinicamedica.modelo.enums.Modulo;
import com.archivos.sistemagestionclinicamedica.persistencia.ArchivoLogs;
import com.archivos.sistemagestionclinicamedica.util.UsuarioSistema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Servicio de la bitacora.
 *
 * Los demas modulos lo llaman despues de hacer una operacion, para dejar
 * registro de quien hizo que y cuando. Tambien arma las consultas para el
 * reporte de logs.
 *
 * Detalle importante: si falla el guardado en la bitacora, NO se corta la
 * operacion principal. Seria absurdo que una alta de paciente valida fallara
 * solo porque no se pudo escribir el log. Por eso el error se avisa por consola
 * y la operacion sigue.
 */
public class LogServicio {

    private final ArchivoLogs archivoLogs;

    public LogServicio(ArchivoLogs archivoLogs) {
        this.archivoLogs = archivoLogs;
    }

    /**
     * Registra una accion en la bitacora, poniendo automaticamente el usuario
     * del sistema y la fecha/hora actual.
     */
    public void registrar(Modulo modulo, Accion accion, String detalle) {
        RegistroLog registro = new RegistroLog(
                UsuarioSistema.nombre(),
                LocalDateTime.now(),
                modulo,
                accion,
                detalle);
        try {
            archivoLogs.agregar(registro);
        } catch (PersistenciaException e) {
            System.err.println("Aviso: no se pudo registrar en la bitacora. " + e.getMessage());
        }
    }

    /**
     * Devuelve toda la bitacora, de la entrada mas nueva a la mas vieja.
     */
    public List<RegistroLog> listarTodos() throws PersistenciaException {
        List<RegistroLog> registros = new ArrayList<>(archivoLogs.listarTodos());
        registros.sort(Comparator.comparing(
                RegistroLog::getMarcaTiempo,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return registros;
    }

    /**
     * Filtra la bitacora por modulo.
     */
    public List<RegistroLog> listarPorModulo(Modulo modulo) throws PersistenciaException {
        return listarTodos().stream()
                .filter(r -> r.getModulo() == modulo)
                .toList();
    }

    /**
     * Filtra la bitacora por tipo de accion.
     */
    public List<RegistroLog> listarPorAccion(Accion accion) throws PersistenciaException {
        return listarTodos().stream()
                .filter(r -> r.getAccion() == accion)
                .toList();
    }

    /**
     * Filtra la bitacora por rango de fechas (incluye los dos extremos).
     */
    public List<RegistroLog> listarPorRangoFechas(LocalDate desde, LocalDate hasta)
            throws PersistenciaException {
        return listarTodos().stream()
                .filter(r -> {
                    if (r.getMarcaTiempo() == null) {
                        return false;
                    }
                    LocalDate fecha = r.getMarcaTiempo().toLocalDate();
                    return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
                })
                .toList();
    }
}
