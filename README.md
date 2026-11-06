# La Palma POS

Sistema de punto de venta e inventario para **Refresquería La Palma**. Aplicación de escritorio en Java (Swing) con base de datos SQL Server. Permite iniciar sesión por rol, administrar empleados, consultar el inventario y dar de alta productos; en los siguientes sprints se agregan insumos, recetas, cobro con ticket, historial y reportes.

## Estado del proyecto

| Sprint | Periodo | Contenido | Estado |
|---|---|---|---|
| Sprint 1 | 26 oct – 8 nov 2026 | HU1 login · HU2–HU5 empleados · HU6 inventario · HU9 alta de productos | ✅ Terminado (28 pts) |
| Sprint 2 | 9 – 22 nov 2026 | Edición de productos, insumos, recetas, captura de venta y cobro | Por hacer |
| Sprint 3 | 23 nov – 6 dic 2026 | Baja de productos, ajuste de existencias, alertas, historial y reportes | Por hacer |

## Funcionalidad del Sprint 1

- **Inicio de sesión (HU1):** entra con usuario o correo y contraseña. Tras 5 intentos fallidos, se bloquea 30 segundos.
- **Empleados (HU2–HU5, solo administrador):** consultar con filtro por puesto, registrar, editar y eliminar con confirmación. El administrador no puede quitarse su puesto ni eliminar su propia cuenta, y siempre debe quedar al menos un administrador.
- **Inventario (HU6, solo administrador):** nombre, precio y existencias, con filtros por categoría (Comida, Bebidas, Snacks), por pieza o por reponer, y búsqueda por nombre.
- **Alta de productos (HU9):** nombre, precio mayor a $0, existencias, categoría y tipo (Directo, Preparado o Nieve), con imagen opcional y aviso si el nombre ya existe.

## Requisitos

- Java **JDK 22** (el `pom.xml` compila con la versión 22)
- Maven o NetBeans
- Microsoft SQL Server (se usa SQL Server Express)
- Windows

## Instalación

### 1. Base de datos

1. En SQL Server Management Studio abre `sql/00_instalar_todo.sql` y presiona **Execute (F5)**.
2. Trabaja sobre la base `RefresqueríaLaPalma`. Solo agrega tablas, columnas y procedimientos; no borra productos, empleados ni ventas.
3. Al final aparece una tabla de verificación: todas las filas deben decir **OK**.
4. El script se puede ejecutar varias veces sin problema. Los archivos `01` a `05` son las mismas partes por separado y no se necesitan si corres el `00`.

| Script | Para qué sirve |
|---|---|
| `sql/00_instalar_todo.sql` | Instala todo |
| `sql/99_desinstalar_todo.sql` | Quita lo nuevo (primero simula; cambia `@CONFIRMAR` a `1` para borrar) |

Para reinstalar desde cero: corre `99_desinstalar_todo.sql` con `@CONFIRMAR = 1` y luego `00_instalar_todo.sql`.

### 2. Conexión a la base de datos

La contraseña no está en el código. Antes de abrir el programa define la variable de entorno:

```
LAPALMA_DB_PASSWORD=<contraseña de SQL Server>
```

Opcionales: `LAPALMA_DB_USUARIO` y `LAPALMA_DB_SERVIDOR`. En NetBeans también sirve `-DLAPALMA_DB_PASSWORD=...`.

### 3. Compilar y ejecutar

- **NetBeans:** abre el proyecto, haz *Clean and Build* y ejecútalo.
- **Maven:** `mvn clean package`
- **JAR:** `SoftwareTerminadoPalma.jar` (el de la raíz es una versión anterior; vuelve a compilar antes de probar).

Entra como administrador. Si falta algo en la base de datos, la pantalla de Inicio muestra un aviso rojo con lo que falta.

## Pruebas

Las pruebas automáticas (JUnit 4) están en `src/test/java/Login`.

```
mvn test                         # todas
mvn -Dtest="Sprint1*Test" test   # solo las del Sprint 1
```

- No necesitan SQL Server, salvo `ConsultarPersonalTest`, que consulta la base real.
- Detalle en `LEEME_PRUEBAS.txt` y `LEEME_PRUEBAS_SPRINT1.txt`.
- Las pruebas manuales (P01–P32) y su evidencia están en el Excel de seguimiento.

## Estructura del repositorio

```
├── src/main/java/Login/   Pantallas (Swing), servicios y reglas
├── src/main/java/capa/    Conexión a la base de datos
├── src/main/resources/    Imágenes del menú
├── src/test/java/Login/   Pruebas JUnit
├── sql/                   Scripts T-SQL de instalación
├── respaldo/              Evidencia (capturas de issues)
├── LEEME_INSTALACION.txt  Notas de instalación detalladas
└── pom.xml
```

## Arquitectura

Arquitectura en capas: presentación (Swing), lógica de negocio y servicios (`TicketService`, `InventarioService`, `HistorialService`, `Exportar`), acceso a datos (JDBC) y persistencia en SQL Server.

## Notas

- Las imágenes de productos se guardan en la carpeta `imagenes_productos`, junto al programa. Consérvala al mover o actualizar el programa.
- Los archivos `.form` de NetBeans de las pantallas modificadas ya no coinciden con el código; no las abras en el diseñador.
- Para guardar un reporte en PDF desde el cuadro de impresión, usa la impresora **Microsoft Print to PDF**.

## Equipo 1

| Integrante | Historia del Sprint 1 |
|---|---|
| Martin Eliu Ayón Osuna | HU1, HU6 |
| Jorge Omar Zazueta Huerta | HU3, HU4 |
| Miguel Obed Navarro Mora | HU2 |
| Juan Antonio Valencia Sánchez | HU5 |
| Karol Guadalupe López Navarro | HU9 |
