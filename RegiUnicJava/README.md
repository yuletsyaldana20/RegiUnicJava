# RegiUnic

Sistema de escritorio hecho en Java Swing para segundo semestre de Ingeniería de Software.

## Funciones

- Inicio de sesión y registro de usuarios.
- Gestión de inventario: agregar, editar, eliminar y consultar productos.
- Registro de pedidos/ventas.
- Edición y eliminación de pedidos.
- Estados: Pendiente, En Proceso, Completado y Cancelado.
- Historial de ventas.
- Búsqueda por cliente/producto y filtro por estado.
- Reportes básicos de ventas.
- Exportación de reportes a CSV.
- Base de datos SQLite.

## Ejecutar

Requiere JDK 21 y Maven.

Desde VS Code se puede abrir la carpeta del proyecto y ejecutar:

```text
mvn clean test
mvn package
```

Después:

```text
java -jar target/regiunic-1.0.0.jar
```

También se puede ejecutar directamente la clase:

```text
com.regiunic.App
```

La base de datos se crea automáticamente en `data/regiunic.db`.

## Nota

El proyecto mantiene el diseño original de RegiUnic: azul oscuro, blanco, botones con efecto hover y logo en `assets/Logo.png`.
