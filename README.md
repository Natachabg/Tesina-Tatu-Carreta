# Tatú Carreta — Gestión de Reserva Natural

Versión reorganizada y modernizada de la tesina.

## Arquitectura

- `src/main/java/com/tatucarreta/entidades` — entidades del dominio.
- `src/main/java/com/tatucarreta/data` — acceso a datos y consultas.
- `src/main/java/com/tatucarreta/config` — conexión SQLite y creación/migración de tablas.
- `src/main/java/com/tatucarreta/ui` — estilos y componentes visuales reutilizables.
- `src/main/java/com/tatucarreta/vistas` — login, dashboard y pantallas del sistema.
- `src/main/resources/images` — recursos gráficos.

## Principales cambios

- Navegación lateral única y consistente entre todas las vistas.
- Interfaz orientada a usuarios no técnicos: botones claros, búsquedas visibles y formularios simples.
- Todas las pantallas de gestión tienen tabla + buscador + botón Agregar.
- Doble click sobre una fila abre el formulario de modificación.
- Desactivación lógica: los registros conservan su historial y pasan a estado `Inactivo`.
- Se retiró `Documentación` de la interfaz de Ingresos; la columna histórica de la base no se elimina para no perder datos existentes.
- Plantel Permanente incorpora registro de nacimientos y fallecimientos.
- Un fallecimiento se registra también en `movimientos` y actualiza las cantidades del plantel y del animal sin borrar el historial.
- Un nacimiento queda en `nacimientos` y aumenta la cantidad del animal y del plantel activo.
- Dashboard con indicadores principales.
- SQLite se mantiene como base local/offline.
- La inicialización ejecuta una migración segura: crea tablas faltantes y agrega columnas de estado sin borrar información existente.

## Inicio

1. Tener Java 25 y Maven configurados.
2. Abrir el proyecto.
3. Ejecutar:

```bash
mvn clean javafx:run
```

## Acceso inicial

- Usuario: `admin`
- Contraseña: `admin`

Si la base ya tenía usuarios, la aplicación conserva esos usuarios y solo crea el administrador inicial cuando la tabla estaba vacía.
