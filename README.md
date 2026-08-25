# Sistema de Gestión de Clínica Médica

Aplicación de escritorio en **Java 21 + Swing** para administrar pacientes, médicos y
citas de una clínica médica, con persistencia de datos en archivos mediante
`RandomAccessFile` (sin bases de datos ni librerías externas de manejo de archivos).

## Características

- **Módulo de Pacientes:** registro, búsqueda, modificación y eliminación.
- **Módulo de Médicos:** registro, catálogo de especialidades, estados activo/inactivo y filtros.
- **Módulo de Citas:** programación con validaciones cruzadas (disponibilidad, horario del
  médico, sin traslapes), cambio de estado y búsqueda.
- **Módulo de Reportes:** 14 reportes distintos, todos exportables a CSV y TXT.
- **Bitácora:** registro automático de todas las operaciones realizadas.

## Requisitos

- **Java 21** (JDK o JRE) instalado.

Para verificar tu versión de Java:

```
java -version
```

## Cómo ejecutar

### Opción 1: desde el archivo JAR (recomendado para usar la aplicación)

```
java -jar SistemaGestionClinicaMedica-v1.0.jar
```

También, en la mayoría de los sistemas, se puede ejecutar haciendo doble clic sobre el
archivo `.jar`.

### Opción 2: desde el código fuente

El proyecto usa Maven:

```
mvn clean package
java -jar target/SistemaGestionClinicaMedica.jar
```

## Almacenamiento de datos

La aplicación crea automáticamente una carpeta `datos/` (junto al lugar desde donde se
ejecuta) con los archivos de persistencia:

- `pacientes.dat`, `medicos.dat`, `citas.dat`, `especialidades.dat`, `logs.dat`

Los reportes exportados se guardan donde el usuario elija al momento de exportarlos.

## Arquitectura

El sistema sigue una **arquitectura en capas** con separación estricta de responsabilidades:

```
Vista (Swing)  →  Servicio (lógica de negocio)  →  Persistencia (RandomAccessFile)
```

- **Modelo:** entidades del dominio (Paciente, Medico, Cita, etc.).
- **Persistencia:** `ArchivoBase<T>` genérico (registros de longitud fija, índices en
  memoria, borrado lógico) y un archivo por entidad.
- **Servicio:** validaciones y reglas de negocio.
- **Vista:** interfaz gráfica en Swing.

La documentación técnica completa (diagramas UML) está en la carpeta `docs/`.

## Documentación

- `docs/` — diagramas de clases (por capa y completo) y de casos de uso.
- Manual de usuario y documento de casos de uso (según la entrega del proyecto).

## Tecnologías

- Java 21 LTS
- Swing (interfaz gráfica)
- RandomAccessFile (persistencia)
- Maven (construcción)
