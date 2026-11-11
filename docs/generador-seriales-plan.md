# Plan tecnico: generador de seriales internos

## Alcance y base analizada

Analisis de la rama `modulo_generado_seriales`, creada desde `codex/opcion-filtros-garantias-sede` en `b658892`. Esta fase no implementa el modulo ni ejecuta migraciones. La rama base contiene cambios posteriores a `main`; confirmar el destino de integracion antes de fusionar en el futuro.

## Arquitectura actual

- Maven configura Java 17 y Spring Boot 4.0.6; usa Spring MVC, Security, Data JPA, Thymeleaf, MariaDB/MySQL, Apache POI 5.4.1 y OpenPDF 1.3.39. No se declara una biblioteca especifica de codigos de barras en `pom.xml`.
- `Equipo` representa un serial en `equipos`: `id_equipo`, `serial`, relaciones con `Producto`, `Proveedor`, `TipoProducto` y `Sede`, factura, fecha, estado, observaciones y `usuarioRegistro`. La fecha es `String`; `estado` ya tiene significado propio y no debe reutilizarse como estado de activacion.
- En JPA las columnas del equipo son opcionales salvo el identificador; `EquipoService.guardarCompleto` exige serial, comprueba duplicados ignorando mayusculas y normaliza entradas a mayusculas. Tipo y proveedor, si se proporcionan, deben existir; producto puede crearse mediante `obtenerOCrearProducto`. Revisar las restricciones reales de la base y la validacion de formularios antes de fijar campos obligatorios para el nuevo flujo.
- `EquipoRepository` ofrece `existsBySerialIgnoreCase`, `findBySerialIgnoreCase`, paginacion por sede y conteos. `EquipoService.guardarLote` llama a `guardarCompleto` y omite existentes; `actualizarCompleto` permite cambiar serial y `eliminar` borra registros. Ambas rutas necesitan proteger especificamente seriales internos y sus reservas.
- `EquipoController` expone `/equipos/completo`, `/equipos/lote`, `/{serial}`, `/editar/{id}`, `/excel` y dashboard. `buscarPorSerial` es global; los listados y conteos filtran por sede. Excel usa POI y el mismo registro completo. Los templates principales son `index.html` y `editar.html`; `sistema-ui.js` aplica mayusculas a entradas.
- `UsuarioContextService` obtiene el usuario del contexto de Spring Security y valida sede. `SecurityConfig` restringe Garantias a SUPER_ADMIN, ADMIN y TECNICO; Servicio tecnico a SUPER_ADMIN y TECNICO. Las demas rutas requieren autenticacion. `DemoInterceptor` tambien interviene. No alterar esos permisos existentes.
- `GarantiaService.obtenerEquipoApto` busca el serial y valida garantia por fecha, sin verificar activacion; `ServicioTecnicoService` acepta un serial como texto y no requiere que exista en `equipos`. Los comprobantes actuales usan templates de impresion y `ComprobantePdfService` con OpenPDF; son reutilizables como referencia, no como formato de etiquetas.
- `TiempoColombiaConfig` centraliza `America/Bogota`. `spring.jpa.hibernate.ddl-auto=none`; hay scripts en `db/migration-manual` y `DatabaseSchemaInitializer` ejecuta DDL condicional bajo ciertas variables de entorno. No se encontro en estos scripts una creacion verificable de un indice unico para `equipos.serial`. `@Column(unique=true)` expresa intencion JPA, no prueba que exista en una base ya desplegada. La base real, sus indices, colacion y duplicados no se consultaron.

## Modelo propuesto

Mantener un `Equipo` por identificador, como fuente general de datos y consulta. Agregar una tabla `serial_interno` con relacion uno a uno unica y obligatoria a `equipos.id_equipo`, y claves a un nuevo `lote_serial_interno`. La existencia del registro asociado identifica `GENERADO_SMD`; su ausencia significa serial convencional de fabrica. No requiere modificar masivamente los registros historicos ni sobrecargar `Equipo.estado`.

`serial_interno`: equipo_id unico, lote_id, estado (`PENDIENTE_ACTIVACION`, `ACTIVO`, `ANULADO`), fecha limite vigente, fecha y usuario de activacion, fecha y usuario de anulacion, motivo y observacion de anulacion. Guardar sede y usuario historicos en el lote y, si se requiere conservar cambios futuros de sede, guardar ademas identificador y nombre historico de sede en cada registro. El serial sigue en `equipos.serial` y es inmutable.

`lote_serial_interno`: id, codigo unico, fecha/hora de generacion, sede historica, usuario creador y cantidad solicitada. `serial_interno_historial`: id, serial_interno_id, tipo de evento, fecha/hora, usuario, observacion y cambios anteriores/nuevos estructurados. Registrar generacion, activacion, edicion, anulacion y prorrogas. El vencimiento se calcula: estado pendiente y fecha limite anterior al dia de negocio; no se persiste como estado. Una prorroga actualiza la fecha limite y se audita por serial.

## Unicidad y transacciones

Generar 15 caracteres con `SecureRandom` y un alfabeto acordado sin caracteres ambiguos; verificar contra `equipos` ignorando mayusculas y reservar inmediatamente. La consulta previa no evita carreras: auditar primero duplicados normalizados y colacion, luego crear un indice unico que imponga la misma equivalencia de serial en la base real. Si colacion o espacios impiden esa equivalencia, disenar una clave normalizada compatible con MariaDB/MySQL y todos los caminos de escritura, previa limpieza acordada; no modificar historicos automaticamente. Confirmar longitud, valores nulos y usuarios con permisos DDL.

Crear lote, equipos, metadatos e historial en transaccion, sin resultados parciales. Un conflicto de unicidad concurrente debe producir rollback y reintento acotado en una transaccion nueva; no continuar dentro de una transaccion marcada para rollback. No capturar errores ajenos como colisiones. Mantener anulados y bloquear borrado y cambio de serial incluso desde rutas existentes, Excel y edicion general; probar carreras con escrituras convencionales.

## Servicios, rutas y activacion

Proponer `SerialInternoService` y repositorios para lote, metadatos e historial; extraer o reutilizar validaciones de catalogos de `EquipoService` de forma minima sin duplicar ni alterar semantica convencional. Endpoints bajo `/seriales-internos`: generacion, consulta y filtrado, detalle/historial, activacion individual, anular, prorrogar y editar seleccionados. Validar autorizacion y sede en backend en cada operacion.

El lector USB funciona como teclado: campo enfocado recibe codigo y Enter envia una solicitud de activacion individual; teclado manual es tecnicamente indistinguible del lector, por lo que no prueba colocacion fisica. Buscar serial normalizado, verificar origen, sede y estado; actualizacion condicional `WHERE estado = PENDIENTE_ACTIVACION` o bloqueo de fila dentro de transaccion, con historial en la misma transaccion. Informar con claridad inexistente, convencional, anulado o ya activo. Ningun endpoint normal de activacion masiva.

## Impresion

Reutilizar el patron de vista previa y `window.print()` de comprobantes, con CSS `@page` especifico de etiquetas 50 x 25 o 50 x 30 mm; evitar reutilizar sus margenes POS. Generar Code 128 con una biblioteca probada compatible con Java 17 o JavaScript, evaluando dependencias y soporte de impresion de lote en Fase 3. La cadena codificada debe ser exactamente el serial almacenado. Reimpresion consulta el mismo registro, sin generacion nueva. Verificar tamaño real, margenes del driver y lectura fisica con impresora y lector; navegador y controladores pueden modificar escala.

## Permisos propuestos para revision

| Operacion | SUPER_ADMIN | ADMIN | TECNICO | USER |
| --- | --- | --- | --- | --- |
| Generar, activar, consultar, imprimir, reimprimir | Todas las sedes | Su sede | Su sede | Su sede |
| Edicion individual y por lote | Todas las sedes | Su sede | No | No |
| Anular o prorrogar | Todas las sedes | Su sede | No | No |
| Consultar historial | Todas las sedes | Su sede | Su sede | Su sede |

Esta matriz es propuesta para aprobacion funcional, no describe permisos ya implementados. Denegar por defecto sin sede, y validar identidad, sede y rol tambien en el servicio. Evitar revelar datos de otra sede por ID directo.

## Integraciones y riesgos

1. **Duplicados y concurrencia:** indice unico efectivo, normalizacion coherente y reintento transaccional. Verificar colacion y datos reales antes de migrar.
2. **Datos existentes:** tablas adicionales con FK y columnas opcionales; no reinterpretar `Equipo.estado` ni actualizar masivamente seriales de fabrica. Verificar compatibilidad con `ddl-auto=none` y respaldo antes de cualquier migracion futura.
3. **Edicion y eliminacion general:** impedir cambiar el identificador o borrar un interno por los endpoints existentes; anular solo pendientes con motivo y observacion, conservando reserva e historial.
4. **Sedes:** guardar sede de origen historica; aplicar filtros y comprobaciones a cada registro en backend, incluso en operaciones masivas y acceso directo. Respetar SUPER_ADMIN global.
5. **Activaciones simultaneas:** transaccion y cambio condicional atomico, con una sola entrada de historial ganadora. Rechazar anulados, convencionales e inexistentes.
6. **Edicion masiva:** seleccionar campos explicitamente, validar cada registro y sus permisos, mostrar confirmacion y aplicar de forma atomica con auditoria por serial. No afectar registros convencionales.
7. **Garantias y servicio tecnico:** mostrar origen y estado en consulta; validar en backend que un interno este activo antes de tramitar garantia. Para servicio tecnico definir el alcance de esa validacion, pues actualmente acepta seriales no registrados: preservar ese comportamiento para seriales convencionales o externos.
8. **Dashboard y Excel:** los conteos actuales incluyen todos los equipos; distinguir pendientes y anulados sin cambiar silenciosamente el significado historico. La importacion convencional no debe crear metadatos internos; evitar que sus rutas modifiquen reservas internas.
9. **Impresion y escaner:** prevenir etiquetas mal escaladas o codigos no legibles con pruebas fisicas; no asumir que una impresion o entrada de teclado confirma colocacion fisica.
10. **Fechas:** reutilizar `TiempoColombiaConfig`, definir fecha limite inclusiva y prorroga en calendario de Bogota; consultar pendientes vencidos por sede al abrir pantalla/dashboard, con indice adecuado, sin proceso programado ni anulacion automatica.

## Migraciones propuestas, no ejecutadas

Auditar esquema, motor, colacion, duplicados normalizados, longitud y restricciones de `equipos.serial` en entorno autorizado. Disenar migracion incremental para indice unico efectivo y tablas de lote, metadatos e historial, sus indices, FK y restricciones, compatible con la version exacta de MariaDB/MySQL. Documentar orden, permisos y respaldo; coordinar scripts manuales con `DatabaseSchemaInitializer` para evitar DDL duplicado. No ejecutar en Fase 1.

## Orden recomendado para Fase 2

1. Confirmar matriz de permisos, fecha limite inclusiva y auditoria de esquema real.
2. Preparar migracion incremental y comprobarla en base aislada con historicos y duplicados.
3. Crear entidades y repositorios para lotes, seriales internos e historial.
4. Implementar generacion, reserva unica, transacciones y concurrencia.
5. Implementar activacion individual, anulacion y prorrogas con auditoria.
6. Implementar edicion individual y masiva autorizada, protegiendo serial y rutas generales de edicion/eliminacion.
7. Integrar busqueda, reglas de garantia y servicio tecnico, dashboards y Excel sin alterar el flujo convencional.
8. Verificar permisos, colisiones, concurrencia, persistencia y compatibilidad en base aislada. La interfaz y etiquetas corresponden a Fase 3.
