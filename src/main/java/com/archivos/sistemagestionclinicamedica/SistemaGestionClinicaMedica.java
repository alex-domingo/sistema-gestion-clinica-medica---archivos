package com.archivos.sistemagestionclinicamedica;

import com.archivos.sistemagestionclinicamedica.excepcion.ClinicaException;
import com.archivos.sistemagestionclinicamedica.modelo.RegistroLog;
import com.archivos.sistemagestionclinicamedica.modelo.enums.Accion;
import com.archivos.sistemagestionclinicamedica.modelo.enums.Modulo;
import com.archivos.sistemagestionclinicamedica.persistencia.ArchivoLogs;
import com.archivos.sistemagestionclinicamedica.servicio.LogServicio;
import com.archivos.sistemagestionclinicamedica.util.RutasDatos;

import java.io.IOException;
import java.util.List;

/**
 * Clase principal.
 *
 * Por ahora solo verifica que el nucleo de persistencia (fase 1) funcione: abre
 * la bitacora, escribe un par de registros y los vuelve a leer. Cuando empiece
 * la fase 2 esta clase solo va a abrir la ventana principal.
 */
public class SistemaGestionClinicaMedica {

    public static void main(String[] args) {
        System.out.println("Sistema de Gestion de Clinica Medica");
        System.out.println("Verificacion del nucleo de persistencia (Fase 1)");
        System.out.println("Carpeta de datos: " + RutasDatos.directorioDatos());
        System.out.println();

        // try-with-resources: cierra el archivo solo al terminar, aunque falle.
        try (ArchivoLogs archivoLogs = new ArchivoLogs(
                RutasDatos.archivo(ArchivoLogs.NOMBRE_ARCHIVO))) {

            System.out.println("Tamanio de cada registro: " + archivoLogs.tamanioRegistro() + " bytes");
            System.out.println("Registros al abrir: " + archivoLogs.contar());

            LogServicio log = new LogServicio(archivoLogs);
            log.registrar(Modulo.SISTEMA, Accion.CONSULTA, "Inicio de la aplicacion");
            log.registrar(Modulo.PACIENTES, Accion.CREACION,
                    "Prueba con acentos: José Rudy Muñoz , cedula 1234-56789-0101");

            List<RegistroLog> bitacora = log.listarTodos();
            System.out.println("Registros despues de escribir: " + bitacora.size());
            System.out.println();
            System.out.println("Ultimas entradas:");
            bitacora.stream().limit(5).forEach(r -> System.out.println("  " + r));

            System.out.println();
            System.out.println("Listo. Los datos quedaron guardados en " + archivoLogs.ruta());
            System.out.println("Si vuelve a ejecutar, el conteo inicial deberia subir.");

        } catch (ClinicaException e) {
            System.err.println("Error de la aplicacion: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error al cerrar el archivo: " + e.getMessage());
        }
    }
}
