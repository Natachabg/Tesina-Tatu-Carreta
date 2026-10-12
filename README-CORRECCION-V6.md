# Tatú Carreta 4G — Corrección V6

Esta versión parte de la V5 entregada y corrige los errores de compilación informados en `ReportePlantelPDF.java` y `VistaMovimientos.java`.

## Correcciones
- `java.util.List` y `com.lowagie.text.List` ya no generan ambigüedad.
- `SistemaDAO.Fila` se utiliza correctamente mediante `id()` en lugar de acceder al campo privado.
- Se mantiene Java 25 / Maven / JavaFX.
- Se mantiene el PDF mensual del Plantel.
- Se incorporan controles para evitar registros activos duplicados.
- Los duplicados históricos detectados se conservan como registros inactivos; no se eliminan físicamente.
- Se crean índices únicos parciales para impedir nuevas duplicaciones activas de actas, animales de catálogo, habitáculos e identificaciones.
- El detalle de un mismo animal dentro de una misma acta no puede volver a cargarse como un segundo detalle activo.
- La base incluida contiene un único archivo `tatu_carreta.db` y fue normalizada para esta versión.

## Prueba
Desde la carpeta que contiene `pom.xml` ejecutar:

```powershell
mvn clean javafx:run
```

No ejecutes Maven desde `Downloads` si allí no está `pom.xml`.
