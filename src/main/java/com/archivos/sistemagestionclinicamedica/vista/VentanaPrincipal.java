package com.archivos.sistemagestionclinicamedica.vista;

import com.archivos.sistemagestionclinicamedica.excepcion.PersistenciaException;
import com.archivos.sistemagestionclinicamedica.persistencia.ArchivoCitas;
import com.archivos.sistemagestionclinicamedica.persistencia.ArchivoEspecialidades;
import com.archivos.sistemagestionclinicamedica.persistencia.ArchivoLogs;
import com.archivos.sistemagestionclinicamedica.persistencia.ArchivoMedicos;
import com.archivos.sistemagestionclinicamedica.persistencia.ArchivoPacientes;
import com.archivos.sistemagestionclinicamedica.servicio.CitaServicio;
import com.archivos.sistemagestionclinicamedica.servicio.EspecialidadServicio;
import com.archivos.sistemagestionclinicamedica.servicio.LogServicio;
import com.archivos.sistemagestionclinicamedica.servicio.MedicoServicio;
import com.archivos.sistemagestionclinicamedica.servicio.PacienteServicio;
import com.archivos.sistemagestionclinicamedica.util.RutasDatos;
import com.archivos.sistemagestionclinicamedica.vista.estilo.Tema;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * Ventana principal de la aplicacion.
 *
 * Se encarga de: - abrir los archivos de datos una sola vez al iniciar - crear
 * los servicios y pasarselos a los paneles - mostrar cada modulo en una pestana
 * - cerrar los archivos bien cuando el usuario cierra la ventana
 *
 * Los archivos se abren aca (y no en cada panel) para que haya una sola
 * instancia de cada uno; asi el indice en memoria se comparte y no hay dos
 * partes del programa escribiendo el mismo archivo a la vez.
 */
@SuppressWarnings("this-escape")
public class VentanaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;

    private final transient ArchivoLogs archivoLogs;
    private final transient ArchivoPacientes archivoPacientes;
    private final transient ArchivoMedicos archivoMedicos;
    private final transient ArchivoEspecialidades archivoEspecialidades;
    private final transient ArchivoCitas archivoCitas;

    public VentanaPrincipal() throws PersistenciaException {
        setTitle("Sistema de Gestion de Clinica Medica");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);   // cerramos a mano

        // Tamanio que tendra la ventana si el usuario la restaura desde
        // maximizada. Tambien es un minimo razonable para que no se deforme.
        setSize(1000, 640);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);   // centrar por si se restaura

        // Abrir la ventana maximizada, ocupando toda la pantalla pero
        // conservando la barra de titulo y la de tareas. Es lo esperable en una
        // aplicacion de gestion, a diferencia del modo exclusivo que oculta todo.
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Abrir archivos y armar servicios.
        archivoLogs = new ArchivoLogs(RutasDatos.archivo(ArchivoLogs.NOMBRE_ARCHIVO));
        archivoPacientes = new ArchivoPacientes(RutasDatos.archivo(ArchivoPacientes.NOMBRE_ARCHIVO));
        archivoMedicos = new ArchivoMedicos(RutasDatos.archivo(ArchivoMedicos.NOMBRE_ARCHIVO));
        archivoEspecialidades = new ArchivoEspecialidades(
                RutasDatos.archivo(ArchivoEspecialidades.NOMBRE_ARCHIVO));
        archivoCitas = new ArchivoCitas(RutasDatos.archivo(ArchivoCitas.NOMBRE_ARCHIVO));

        LogServicio logServicio = new LogServicio(archivoLogs);
        // Los servicios de paciente y medico reciben archivoCitas para poder
        // bloquear el borrado cuando hay citas programadas.
        PacienteServicio pacienteServicio
                = new PacienteServicio(archivoPacientes, archivoCitas, logServicio);
        EspecialidadServicio especialidadServicio
                = new EspecialidadServicio(archivoEspecialidades, logServicio);
        MedicoServicio medicoServicio
                = new MedicoServicio(archivoMedicos, especialidadServicio, archivoCitas, logServicio);
        CitaServicio citaServicio
                = new CitaServicio(archivoCitas, pacienteServicio, medicoServicio, logServicio);

        // Fondo general de la ventana.
        getContentPane().setBackground(Tema.colores().fondo);

        // Encabezado con el nombre de la app.
        add(construirEncabezado(), BorderLayout.NORTH);

        // Armar las pestanas. Se van agregando modulos en cada fase.
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Pacientes", new PanelPacientes(pacienteServicio));
        pestanas.addTab("Medicos", new PanelMedicos(medicoServicio, especialidadServicio));
        pestanas.addTab("Citas", new PanelCitas(citaServicio, pacienteServicio, medicoServicio));
        pestanas.setBorder(BorderFactory.createEmptyBorder(
                Tema.ESPACIO, Tema.ESPACIO, Tema.ESPACIO, Tema.ESPACIO));

        add(pestanas, BorderLayout.CENTER);

        // Cerrar los archivos al salir, para no perder datos.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarYSalir();
            }
        });
    }

    // Barra de arriba con el titulo de la app. Fondo blanco, titulo en teal y
    // una linea de color abajo como acento. Sobrio pero con identidad.
    private JPanel construirEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Tema.colores().superficie);
        // Relleno interno + linea teal de 2px abajo.
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Tema.colores().primario),
                BorderFactory.createEmptyBorder(Tema.ESPACIO, Tema.ESPACIO_GRANDE,
                        Tema.ESPACIO, Tema.ESPACIO_GRANDE)));

        JLabel titulo = new JLabel("Clinica Medica");
        titulo.setFont(Tema.fuenteTitulo());
        titulo.setForeground(Tema.colores().primario);

        JLabel subtitulo = new JLabel("Sistema de gestion");
        subtitulo.setFont(Tema.fuenteSubtitulo());
        subtitulo.setForeground(Tema.colores().textoTenue);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BorderLayout());
        textos.add(titulo, BorderLayout.NORTH);
        textos.add(subtitulo, BorderLayout.SOUTH);

        encabezado.add(textos, BorderLayout.WEST);
        encabezado.setPreferredSize(new Dimension(0, 72));
        return encabezado;
    }

    // Cierra los archivos y termina el programa.
    private void cerrarYSalir() {
        int respuesta = JOptionPane.showConfirmDialog(this,
                "Desea salir de la aplicacion?", "Salir",
                JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        cerrarArchivos();
        dispose();
        System.exit(0);
    }

    private void cerrarArchivos() {
        try {
            archivoCitas.close();
            archivoEspecialidades.close();
            archivoMedicos.close();
            archivoPacientes.close();
            archivoLogs.close();
        } catch (IOException e) {
            System.err.println("Error al cerrar archivos: " + e.getMessage());
        }
    }
}
