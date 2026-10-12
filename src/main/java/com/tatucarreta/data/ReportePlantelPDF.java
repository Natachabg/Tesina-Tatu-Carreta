package com.tatucarreta.data;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Generador de reportes PDF del Plantel Permanente.
 *
 * Incluye:
 *  - Inventario mensual general del Plantel.
 *  - Reportes individuales de nacimientos, fallecimientos y traslados.
 *
 * El formato está pensado para impresión y lectura humana: encabezados claros,
 * resumen ejecutivo, indicadores, tablas agrupadas y espacio de firma.
 */
public final class ReportePlantelPDF {

    private static final Color VERDE = new Color(36, 108, 61);
    private static final Color VERDE_OSCURO = new Color(24, 73, 42);
    private static final Color VERDE_SUAVE = new Color(232, 243, 235);
    private static final Color VERDE_MUY_SUAVE = new Color(246, 250, 247);
    private static final Color GRIS_TEXTO = new Color(78, 91, 83);
    private static final Color GRIS_LINEA = new Color(210, 220, 214);
    private static final Color GRIS_FONDO = new Color(242, 245, 243);
    private static final Color BLANCO = Color.WHITE;

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter PERIODO = DateTimeFormatter.ofPattern("MM-yyyy");
    private static final DateTimeFormatter FECHA_HORA_ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private ReportePlantelPDF() {}

    // =========================================================
    // REPORTE GENERAL / MENSUAL
    // =========================================================

    public static File generar() throws Exception {
        List<SistemaDAO.Fila> filas = SistemaDAO.reportePlantelMensual();
        LocalDate hoy = LocalDate.now();
        YearMonth ym = YearMonth.from(hoy);
        String periodo = ym.format(PERIODO);
        String selloArchivo = LocalDateTime.now().format(FECHA_HORA_ARCHIVO);

        File carpeta = prepararCarpeta();
        File salida = new File(carpeta, "Inventario_Plantel_Fauna_" + periodo + "_" + selloArchivo + ".pdf");

        Document doc = new Document(PageSize.A4.rotate(), 28, 28, 42, 38);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(salida));
        writer.setPageEvent(new PiePagina());
        doc.open();

        Fuentes f = new Fuentes();
        agregarEncabezado(doc,
                "INVENTARIO GENERAL",
                "PLANTEL PERMANENTE DE FAUNA SILVESTRE",
                "Reporte mensual · " + periodo,
                f);

        PdfPTable datos = tablaDatosGenerales(f,
                "RESERVA NATURAL TATÚ CARRETA",
                "Período", periodo,
                "Fecha de generación", hoy.format(FECHA),
                "Tipo de reporte", "Inventario vivo actual");
        doc.add(datos);
        espacio(doc, 8);

        int totalActual = 0;
        int totalAltas = 0;
        int totalNacimientos = 0;
        int totalBajas = 0;
        int totalFallecimientos = 0;
        int totalTraslados = 0;
        Set<String> especies = new LinkedHashSet<>();
        Set<String> ubicaciones = new LinkedHashSet<>();

        for (SistemaDAO.Fila fila : filas) {
            List<String> c = fila.c();
            int actual = toInt(get(c, 6));
            int altas = movimientos(fila.id(), ym, "Ingreso");
            int nacimientos = movimientos(fila.id(), ym, "Nacimiento");
            int bajasEgreso = movimientos(fila.id(), ym, "Egreso");
            int fallecimientos = movimientos(fila.id(), ym, "Fallecimiento");
            int traslados = movimientos(fila.id(), ym, "Traslado");

            totalActual += actual;
            totalAltas += altas;
            totalNacimientos += nacimientos;
            totalBajas += bajasEgreso + fallecimientos + traslados;
            totalFallecimientos += fallecimientos;
            totalTraslados += traslados;

            String cientifico = get(c, 2).trim();
            if (!cientifico.isBlank()) especies.add(cientifico.toLowerCase(Locale.ROOT));

            String ubicacion = get(c, 8).trim();
            if (!ubicacion.isBlank()) ubicaciones.add(ubicacion.toLowerCase(Locale.ROOT));
        }

        PdfPTable indicadores = new PdfPTable(6);
        indicadores.setWidthPercentage(100);
        indicadores.setWidths(new float[]{1, 1, 1, 1, 1, 1});
        agregarIndicador(indicadores, String.valueOf(totalActual), "EJEMPLARES VIVOS", VERDE, f);
        agregarIndicador(indicadores, String.valueOf(especies.size()), "ESPECIES", VERDE_OSCURO, f);
        agregarIndicador(indicadores, String.valueOf(ubicaciones.size()), "UBICACIONES", VERDE, f);
        agregarIndicador(indicadores, String.valueOf(totalAltas + totalNacimientos), "ALTAS DEL MES", new Color(67, 117, 79), f);
        agregarIndicador(indicadores, String.valueOf(totalFallecimientos), "FALLECIMIENTOS", new Color(145, 75, 75), f);
        agregarIndicador(indicadores, String.valueOf(totalTraslados), "TRASLADOS", new Color(76, 103, 137), f);
        doc.add(indicadores);
        espacio(doc, 12);

        agregarTituloSeccion(doc, "RESUMEN DEL PERÍODO", f);
        PdfPTable resumen = new PdfPTable(4);
        resumen.setWidthPercentage(100);
        resumen.setWidths(new float[]{34, 22, 22, 22});
        encabezado(resumen, new String[]{"Movimiento", "Cantidad", "Tipo", "Impacto"}, f);
        agregarFilaResumen(resumen, "Ingresos al plantel", totalAltas, "ALTA", "+ Inventario", f);
        agregarFilaResumen(resumen, "Nacimientos", totalNacimientos, "ALTA", "+ Inventario", f);
        agregarFilaResumen(resumen, "Fallecimientos", totalFallecimientos, "BAJA", "- Inventario", f);
        agregarFilaResumen(resumen, "Traslados", totalTraslados, "BAJA / MOVIMIENTO", "Según destino", f);
        agregarFilaResumen(resumen, "Otras bajas registradas", Math.max(0, totalBajas - totalFallecimientos - totalTraslados), "BAJA", "- Inventario", f);
        doc.add(resumen);
        espacio(doc, 12);

        agregarTituloSeccion(doc, "INVENTARIO ACTUAL", f);
        Paragraph ayuda = new Paragraph(
                "El total final representa únicamente ejemplares vivos que permanecen actualmente en el Plantel Permanente. " +
                "Las altas y bajas se informan como movimientos del período.", f.pequena);
        ayuda.setSpacingAfter(7);
        doc.add(ayuda);

        Map<String, List<SistemaDAO.Fila>> grupos = agrupar(filas);
        int numero = 1;
        for (Map.Entry<String, List<SistemaDAO.Fila>> entry : grupos.entrySet()) {
            agregarSubtitulo(doc, entry.getKey(), f);

            PdfPTable tabla = new PdfPTable(12);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{4, 14, 18, 12, 12, 7, 7, 7, 7, 7, 7, 10});
            encabezado(tabla, new String[]{
                    "N°", "Nombre común", "Nombre científico", "Ubicación", "Habitáculo",
                    "Anterior", "Ingresos", "Nac.", "Fallec.", "Trasl.", "Final", "Observaciones"
            }, f);

            for (SistemaDAO.Fila fila : entry.getValue()) {
                List<String> c = fila.c();
                int actual = toInt(get(c, 6));
                int ingresos = movimientos(fila.id(), ym, "Ingreso");
                int nacimientos = movimientos(fila.id(), ym, "Nacimiento");
                int fallecimientos = movimientos(fila.id(), ym, "Fallecimiento");
                int traslados = movimientos(fila.id(), ym, "Traslado");
                int egresos = movimientos(fila.id(), ym, "Egreso");

                int anterior = SistemaDAO.snapshotAnterior(fila.id());
                if (anterior < 0) {
                    anterior = Math.max(0, actual - ingresos - nacimientos + fallecimientos + traslados + egresos);
                }

                String ubicacion = get(c, 8);
                String habitaculo = get(c, 7);
                if (habitaculo.isBlank()) habitaculo = "—";

                String obs = get(c, 10);
                if (obs.isBlank()) obs = "—";

                String[] vals = {
                        String.valueOf(numero++),
                        get(c, 1),
                        get(c, 2),
                        ubicacion,
                        habitaculo,
                        String.valueOf(anterior),
                        String.valueOf(ingresos),
                        String.valueOf(nacimientos),
                        String.valueOf(fallecimientos),
                        String.valueOf(traslados),
                        String.valueOf(actual),
                        obs
                };

                for (int i = 0; i < vals.length; i++) {
                    PdfPCell cell = celda(vals[i], f.cuerpo);
                    cell.setPadding(4);
                    if (i == 0 || i >= 5 && i <= 10) cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    if (i == 10) {
                        cell.setBackgroundColor(VERDE_SUAVE);
                        cell.setPhrase(new Phrase(vals[i], f.negritaVerde));
                    }
                    tabla.addCell(cell);
                }
            }
            doc.add(tabla);
            espacio(doc, 7);
        }

        if (filas.isEmpty()) {
            PdfPTable vacio = cajaMensaje("No hay ejemplares activos en el Plantel Permanente para este período.", f);
            doc.add(vacio);
        }

        espacio(doc, 8);
        agregarTituloSeccion(doc, "REFERENCIAS", f);
        doc.add(new Paragraph(
                "Anterior = existencia registrada al cierre del período anterior.  " +
                "Ingresos = ejemplares incorporados.  Nac. = nacimientos.  " +
                "Fallec. = fallecimientos.  Trasl. = traslados.  Final = existencia viva actual.",
                f.pequena));

        agregarFirma(doc, f);
        doc.close();

        SistemaDAO.guardarSnapshot(periodo, hoy.toString(), filas);
        return salida;
    }

    // =========================================================
    // RESUMEN TOTAL
    // =========================================================

    public static File generarResumenTotal() throws Exception {
        return generarResumenTotal(YearMonth.now());
    }

    public static File generarResumenTotal(YearMonth ym) throws Exception {
        String desde = ym.atDay(1).toString();
        String hasta = ym.atEndOfMonth().toString();
        List<SistemaDAO.Fila> vivos = SistemaDAO.reportePlantelMensual(desde, hasta);
        List<SistemaDAO.Fila> ingresos = SistemaDAO.movimientosPlantelPorTipo("Ingreso a Plantel", desde, hasta);
        List<SistemaDAO.Fila> nacimientos = SistemaDAO.movimientosPlantelPorTipo("Nacimiento", desde, hasta);
        List<SistemaDAO.Fila> fallecimientos = SistemaDAO.movimientosPlantelPorTipo("Fallecimiento", desde, hasta);
        List<SistemaDAO.Fila> traslados = SistemaDAO.movimientosPlantelPorTipo("Traslado", desde, hasta);

        LocalDate hoy = LocalDate.now();
        String periodo = ym.format(PERIODO);
        File salida = new File(prepararCarpeta(),
                "Resumen_Total_Plantel_Permanente_" + periodo + "_" + LocalDateTime.now().format(FECHA_HORA_ARCHIVO) + ".pdf");

        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 44, 40);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(salida));
        writer.setPageEvent(new PiePagina());
        doc.open();

        Fuentes f = new Fuentes();
        agregarEncabezado(doc,
                "RESUMEN TOTAL",
                "PLANTEL PERMANENTE DE FAUNA SILVESTRE",
                "Resumen ejecutivo · información acumulada y existencia actual · " + periodo,
                f);

        doc.add(tablaDatosGenerales(f,
                "RESERVA NATURAL TATÚ CARRETA",
                "Fecha de generación", hoy.format(FECHA),
                "Estado", "Información consolidada",
                "Alcance", "Plantel Permanente"));
        espacio(doc, 10);

        int totalVivos = sumarCantidadVivos(vivos);
        int totalIngresos = sumarCantidadMovimientos(ingresos);
        int totalNacimientos = sumarCantidadMovimientos(nacimientos);
        int totalFallecimientos = sumarCantidadMovimientos(fallecimientos);
        int totalTraslados = sumarCantidadMovimientos(traslados);

        PdfPTable indicadores = new PdfPTable(6);
        indicadores.setWidthPercentage(100);
        indicadores.setWidths(new float[]{1, 1, 1, 1, 1, 1});
        agregarIndicador(indicadores, String.valueOf(totalVivos), "ANIMALES VIVOS", VERDE, f);
        agregarIndicador(indicadores, String.valueOf(totalIngresos), "INGRESOS", VERDE_OSCURO, f);
        agregarIndicador(indicadores, String.valueOf(totalNacimientos), "NACIMIENTOS", new Color(67, 117, 79), f);
        agregarIndicador(indicadores, String.valueOf(totalFallecimientos), "FALLECIDOS", new Color(145, 75, 75), f);
        agregarIndicador(indicadores, String.valueOf(totalTraslados), "TRASLADADOS", new Color(76, 103, 137), f);
        agregarIndicador(indicadores, String.valueOf(
                ingresos.size() + nacimientos.size() + fallecimientos.size() + traslados.size()),
                "MOVIMIENTOS", new Color(112, 93, 55), f);
        doc.add(indicadores);
        espacio(doc, 12);

        agregarTituloSeccion(doc, "SITUACIÓN GENERAL", f);
        PdfPTable resumen = new PdfPTable(4);
        resumen.setWidthPercentage(100);
        resumen.setWidths(new float[]{38, 20, 20, 22});
        encabezado(resumen, new String[]{"Concepto", "Cantidad", "Estado", "Interpretación"}, f);
        agregarFilaResumen(resumen, "Animales vivos actuales", totalVivos, "ACTIVO", "Permanecen en reserva", f);
        agregarFilaResumen(resumen, "Ingresos al Plantel", totalIngresos, "HISTÓRICO", "Altas registradas", f);
        agregarFilaResumen(resumen, "Nacimientos", totalNacimientos, "HISTÓRICO", "Nuevos ejemplares", f);
        agregarFilaResumen(resumen, "Fallecimientos", totalFallecimientos, "HISTÓRICO", "Bajas por fallecimiento", f);
        agregarFilaResumen(resumen, "Traslados", totalTraslados, "HISTÓRICO", "Movimientos de traslado", f);
        doc.add(resumen);
        espacio(doc, 12);

        agregarTituloSeccion(doc, "EXISTENCIA VIVA ACTUAL", f);
        doc.add(new Paragraph(
                "Este listado representa únicamente los ejemplares que actualmente permanecen activos en el Plantel Permanente. " +
                "Los animales que fallecieron o fueron trasladados ya no forman parte de esta existencia.",
                f.pequena));
        espacio(doc, 6);

        PdfPTable tablaVivos = new PdfPTable(7);
        tablaVivos.setWidthPercentage(100);
        tablaVivos.setWidths(new float[]{5, 21, 22, 13, 13, 13, 8});
        encabezado(tablaVivos, new String[]{
                "N°", "Animal", "Nombre científico", "Ubicación", "Habitáculo", "Fecha ingreso", "Vivos"
        }, f);

        int n = 1;
        for (SistemaDAO.Fila fila : vivos) {
            List<String> c = fila.c();
            String[] vals = {
                    String.valueOf(n++),
                    get(c, 1),
                    get(c, 2).isBlank() ? "—" : get(c, 2),
                    get(c, 8).isBlank() ? "—" : get(c, 8),
                    get(c, 7).isBlank() ? "Campo abierto" : get(c, 7),
                    get(c, 9).isBlank() ? "—" : get(c, 9),
                    get(c, 6)
            };
            for (int i = 0; i < vals.length; i++) {
                PdfPCell cell = celda(vals[i], i == 6 ? f.negritaVerde : f.cuerpo);
                cell.setPadding(4);
                if (i == 0 || i == 6) cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                if ((n % 2) == 0) cell.setBackgroundColor(VERDE_MUY_SUAVE);
                tablaVivos.addCell(cell);
            }
        }

        if (vivos.isEmpty()) {
            doc.add(cajaMensaje("No hay ejemplares vivos activos en el Plantel Permanente.", f));
        } else {
            doc.add(tablaVivos);
        }

        espacio(doc, 12);
        agregarTituloSeccion(doc, "CRITERIO DE LECTURA", f);
        doc.add(new Paragraph(
                "Animales vivos = existencia actual del Plantel.  " +
                "Ingresos, nacimientos, fallecimientos y traslados = movimientos históricos registrados en el Plantel Permanente. " +
                "Los movimientos inactivos o revertidos no se contabilizan.",
                f.pequena));

        agregarFirma(doc, f);
        doc.close();
        return salida;
    }

    // =========================================================
    // INGRESOS GENERALES / VIVOS
    // =========================================================

    public static File generarIngresosGenerales() throws Exception {
        return generarIngresosGenerales(YearMonth.now());
    }

    public static File generarIngresosGenerales(YearMonth ym) throws Exception {
        List<SistemaDAO.Fila> filas = SistemaDAO.reportePlantelMensual(
                ym.atDay(1).toString(), ym.atEndOfMonth().toString());
        LocalDate hoy = LocalDate.now();
        String periodo = ym.format(PERIODO);
        File salida = new File(prepararCarpeta(),
                "Ingresos_Generales_Plantel_Vivos_" + periodo + "_" + LocalDateTime.now().format(FECHA_HORA_ARCHIVO) + ".pdf");

        Document doc = new Document(PageSize.A4.rotate(), 28, 28, 42, 38);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(salida));
        writer.setPageEvent(new PiePagina());
        doc.open();

        Fuentes f = new Fuentes();
        agregarEncabezado(doc,
                "INGRESOS GENERALES / VIVOS",
                "INVENTARIO ACTUAL DEL PLANTEL PERMANENTE",
                "Ejemplares activos que permanecen actualmente en la Reserva · " + periodo,
                f);

        int total = sumarCantidadVivos(filas);
        Set<String> animales = new LinkedHashSet<>();
        Set<String> ubicaciones = new LinkedHashSet<>();
        for (SistemaDAO.Fila fila : filas) {
            String animal = get(fila.c(), 1).trim();
            if (!animal.isBlank()) animales.add(animal.toLowerCase(Locale.ROOT));
            String ubicacion = get(fila.c(), 8).trim();
            if (!ubicacion.isBlank()) ubicaciones.add(ubicacion.toLowerCase(Locale.ROOT));
        }

        doc.add(tablaDatosGenerales(f,
                "RESERVA NATURAL TATÚ CARRETA",
                "Fecha", hoy.format(FECHA),
                "Estado", "Ejemplares activos",
                "Criterio", "Existencia viva actual"));
        espacio(doc, 8);

        PdfPTable indicadores = new PdfPTable(3);
        indicadores.setWidthPercentage(100);
        indicadores.setWidths(new float[]{1, 1, 1});
        agregarIndicador(indicadores, String.valueOf(total), "EJEMPLARES VIVOS", VERDE, f);
        agregarIndicador(indicadores, String.valueOf(animales.size()), "REGISTROS / ANIMALES", VERDE_OSCURO, f);
        agregarIndicador(indicadores, String.valueOf(ubicaciones.size()), "UBICACIONES", new Color(76, 103, 137), f);
        doc.add(indicadores);
        espacio(doc, 12);

        agregarTituloSeccion(doc, "DETALLE DEL PLANTEL ACTIVO", f);
        doc.add(new Paragraph(
                "Listado oficial de los ejemplares que actualmente permanecen en el Plantel Permanente. " +
                "No incluye fallecimientos ni traslados ya finalizados.",
                f.pequena));
        espacio(doc, 6);

        PdfPTable tabla = new PdfPTable(9);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{4, 17, 19, 11, 12, 11, 9, 8, 14});
        encabezado(tabla, new String[]{
                "N°", "Animal", "Nombre científico", "Especie", "Ubicación",
                "Habitáculo", "Ingreso", "Vivos", "Origen / Observaciones"
        }, f);

        int n = 1;
        for (SistemaDAO.Fila fila : filas) {
            List<String> c = fila.c();
            String obs = get(c, 11);
            String origen = get(c, 12);
            String extra = origen;
            if (!obs.isBlank()) {
                if (!extra.isBlank()) extra += " · ";
                extra += obs;
            }
            if (extra.isBlank()) extra = "—";

            String[] vals = {
                    String.valueOf(n++),
                    get(c, 1),
                    get(c, 2).isBlank() ? "—" : get(c, 2),
                    get(c, 3).isBlank() ? "—" : get(c, 3),
                    get(c, 8).isBlank() ? "—" : get(c, 8),
                    get(c, 7).isBlank() ? "Campo abierto" : get(c, 7),
                    get(c, 9).isBlank() ? "—" : get(c, 9),
                    get(c, 6),
                    extra
            };

            for (int i = 0; i < vals.length; i++) {
                PdfPCell cell = celda(vals[i], i == 7 ? f.negritaVerde : f.cuerpo);
                cell.setPadding(4);
                if (i == 0 || i == 7) cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                if ((n % 2) == 0) cell.setBackgroundColor(VERDE_MUY_SUAVE);
                tabla.addCell(cell);
            }
        }

        if (filas.isEmpty()) {
            doc.add(cajaMensaje("No existen ejemplares activos en el Plantel Permanente.", f));
        } else {
            doc.add(tabla);
        }

        espacio(doc, 10);
        agregarTituloSeccion(doc, "ACLARACIÓN", f);
        doc.add(new Paragraph(
                "Este reporte es una fotografía del estado actual del Plantel Permanente. " +
                "Para consultar movimientos históricos utilice los reportes específicos de nacimientos, fallecimientos y traslados.",
                f.pequena));

        agregarFirma(doc, f);
        doc.close();
        return salida;
    }

    // =========================================================
    // REPORTES DE MOVIMIENTOS
    // =========================================================

    public static File generarMovimientos(String tipo) throws Exception {
        return generarMovimientos(tipo, YearMonth.now());
    }

    public static File generarMovimientos(String tipo, YearMonth ym) throws Exception {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("El tipo de movimiento es obligatorio.");
        }

        String tipoNormalizado = tipo.trim();
        List<SistemaDAO.Fila> filas = SistemaDAO.movimientosPlantelPorTipo(
                tipoNormalizado, ym.atDay(1).toString(), ym.atEndOfMonth().toString());
        LocalDate hoy = LocalDate.now();
        String periodo = ym.format(PERIODO);

        File carpeta = prepararCarpeta();
        String tipoArchivo = tipoNormalizado
                .replace("ó", "o")
                .replace("é", "e")
                .replace("í", "i")
                .replace(" ", "_");
        File salida = new File(carpeta,
                "Reporte_" + tipoArchivo + "_Plantel_" + periodo + "_" + LocalDateTime.now().format(FECHA_HORA_ARCHIVO) + ".pdf");

        Document doc = new Document(PageSize.A4.rotate(), 28, 28, 42, 38);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(salida));
        writer.setPageEvent(new PiePagina());
        doc.open();

        Fuentes f = new Fuentes();
        agregarEncabezado(doc,
                "REPORTE DE " + tipoNormalizado.toUpperCase(Locale.ROOT),
                "PLANTEL PERMANENTE",
                "Trazabilidad de movimientos · " + periodo,
                f);

        doc.add(tablaDatosGenerales(f,
                "RESERVA NATURAL TATÚ CARRETA",
                "Tipo de movimiento", tipoNormalizado,
                "Fecha de generación", hoy.format(FECHA),
                "Estado informado", "Movimientos activos"));
        espacio(doc, 8);

        int cantidadMovimientos = filas.size();
        int totalEjemplares = 0;
        Set<String> animales = new LinkedHashSet<>();
        Set<String> destinos = new LinkedHashSet<>();
        Set<String> habitaculos = new LinkedHashSet<>();

        for (SistemaDAO.Fila fila : filas) {
            List<String> c = fila.c();
            totalEjemplares += toInt(get(c, 3));
            String animal = get(c, 2).trim();
            if (!animal.isBlank()) animales.add(animal.toLowerCase(Locale.ROOT));
            String destino = get(c, 5).trim();
            if (!destino.isBlank()) destinos.add(destino.toLowerCase(Locale.ROOT));
            String habitaculo = get(c, 8).trim();
            if (!habitaculo.isBlank()) habitaculos.add(habitaculo.toLowerCase(Locale.ROOT));
        }

        PdfPTable indicadores = new PdfPTable(4);
        indicadores.setWidthPercentage(100);
        indicadores.setWidths(new float[]{1, 1, 1, 1});
        agregarIndicador(indicadores, String.valueOf(cantidadMovimientos), "MOVIMIENTOS", VERDE, f);
        agregarIndicador(indicadores, String.valueOf(totalEjemplares), "EJEMPLARES", VERDE_OSCURO, f);
        agregarIndicador(indicadores, String.valueOf(animales.size()), "ESPECIES / ANIMALES", new Color(67, 117, 79), f);
        agregarIndicador(indicadores,
                String.valueOf(tipoNormalizado.equalsIgnoreCase("Fallecimiento") ? habitaculos.size() : destinos.size()),
                tipoNormalizado.equalsIgnoreCase("Fallecimiento") ? "HABITÁCULOS INVOLUCRADOS" : "DESTINOS",
                new Color(76, 103, 137), f);
        doc.add(indicadores);
        espacio(doc, 12);

        agregarTituloSeccion(doc, "DETALLE DE MOVIMIENTOS", f);
        doc.add(new Paragraph(
                "Cada fila corresponde a un movimiento activo. El identificador " +
                "del movimiento se conserva dentro de las observaciones para permitir su posterior localización y modificación.",
                f.pequena));
        espacio(doc, 6);

        PdfPTable tabla = new PdfPTable(9);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{9, 8, 15, 8, 8, 17, 12, 13, 25});
        encabezado(tabla, new String[]{
                "Fecha", "Movimiento", "Animal", "Cantidad", "Sexo", "Destino / Acción", "Ubicación", "Habitáculo", "Observaciones"
        }, f);

        int filaN = 0;
        for (SistemaDAO.Fila fila : filas) {
            List<String> c = fila.c();
            String idMovimiento = String.valueOf(fila.id());
            String destino = get(c, 5);
            if (destino.isBlank()) destino = "—";

            String ubicacion = get(c, 7);
            if (ubicacion.isBlank()) ubicacion = "—";

            String habitaculo = get(c, 8);
            if (habitaculo.isBlank()) habitaculo = "—";

            String observaciones = get(c, 6);
            if (observaciones.isBlank()) observaciones = "Movimiento #" + idMovimiento;

            String[] vals = {
                    get(c, 1),
                    "#" + idMovimiento,
                    get(c, 2),
                    get(c, 3),
                    get(c, 4).isBlank() ? "—" : get(c, 4),
                    destino,
                    ubicacion,
                    habitaculo,
                    observaciones
            };

            for (int i = 0; i < vals.length; i++) {
                PdfPCell cell = celda(vals[i], i == 1 ? f.negritaVerde : f.cuerpo);
                cell.setPadding(4);
                if (i == 1 || i == 3 || i == 4) cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                if ((filaN % 2) == 1) cell.setBackgroundColor(VERDE_MUY_SUAVE);
                tabla.addCell(cell);
            }
            filaN++;
        }

        if (filas.isEmpty()) {
            doc.add(cajaMensaje("No hay movimientos activos de tipo " + tipoNormalizado + " para mostrar.", f));
        } else {
            doc.add(tabla);
        }

        espacio(doc, 10);
        agregarTituloSeccion(doc, "NOTAS DE TRAZABILIDAD", f);
        doc.add(new Paragraph(
                "• Fecha: día en que se registró la acción.\n" +
                "• Movimiento: identificador único utilizado para localizar la operación.\n" +
                "• Destino / Acción: destino informado cuando corresponde al movimiento.\n" +
                "• Habitáculo: ubicación asociada al ejemplar en el Plantel Permanente.\n" +
                "• Observaciones: información histórica y referencia del movimiento.",
                f.pequena));

        agregarFirma(doc, f);
        doc.close();
        return salida;
    }

    // =========================================================
    // COMPONENTES VISUALES
    // =========================================================

    private static void agregarEncabezado(Document doc, String titulo, String subtitulo,
                                           String linea, Fuentes f) throws Exception {
        PdfPTable head = new PdfPTable(3);
        head.setWidthPercentage(100);
        head.setWidths(new float[]{16, 64, 20});

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        try (InputStream in = ReportePlantelPDF.class.getResourceAsStream("/images/logo.png")) {
            if (in != null) {
                Image logo = Image.getInstance(in.readAllBytes());
                logo.scaleToFit(74, 58);
                logoCell.addElement(logo);
            }
        } catch (Exception ignored) {}
        head.addCell(logoCell);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph p = new Paragraph(titulo, f.titulo);
        p.setSpacingAfter(2);
        titleCell.addElement(p);
        Paragraph p2 = new Paragraph(subtitulo, f.subtitulo);
        p2.setSpacingAfter(2);
        titleCell.addElement(p2);
        titleCell.addElement(new Paragraph(linea, f.pequena));
        head.addCell(titleCell);

        PdfPCell badge = new PdfPCell();
        badge.setBorder(Rectangle.NO_BORDER);
        badge.setBackgroundColor(VERDE_SUAVE);
        badge.setPadding(9);
        Paragraph b1 = new Paragraph("DOCUMENTO OFICIAL", f.negritaVerde);
        b1.setAlignment(Element.ALIGN_CENTER);
        badge.addElement(b1);
        Paragraph b2 = new Paragraph("PLANTEL\nPERMANENTE", f.badge);
        b2.setAlignment(Element.ALIGN_CENTER);
        badge.addElement(b2);
        head.addCell(badge);

        doc.add(head);

        PdfPTable lineaVerde = new PdfPTable(1);
        lineaVerde.setWidthPercentage(100);
        PdfPCell lc = new PdfPCell(new Phrase(""));
        lc.setBorder(Rectangle.NO_BORDER);
        lc.setFixedHeight(4);
        lc.setBackgroundColor(VERDE);
        lineaVerde.addCell(lc);
        doc.add(lineaVerde);
        espacio(doc, 8);
    }

    private static PdfPTable tablaDatosGenerales(Fuentes f,
                                                  String organismo,
                                                  String etiqueta1, String valor1,
                                                  String etiqueta2, String valor2,
                                                  String etiqueta3, String valor3) {
        PdfPTable t = new PdfPTable(8);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{12, 18, 12, 14, 12, 14, 12, 18});

        agregarDato(t, "Institución", organismo, f);
        agregarDato(t, etiqueta1, valor1, f);
        agregarDato(t, etiqueta2, valor2, f);
        agregarDato(t, etiqueta3, valor3, f);
        return t;
    }

    private static void agregarDato(PdfPTable t, String etiqueta, String valor, Fuentes f) {
        PdfPCell a = celda(etiqueta.toUpperCase(Locale.ROOT), f.etiqueta);
        a.setBackgroundColor(GRIS_FONDO);
        a.setPadding(5);
        t.addCell(a);
        PdfPCell b = celda(valor, f.cuerpoBold);
        b.setPadding(5);
        t.addCell(b);
    }

    private static void agregarIndicador(PdfPTable tabla, String valor, String titulo,
                                         Color color, Fuentes f) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(7);
        cell.setBackgroundColor(VERDE_MUY_SUAVE);
        cell.setBorderColor(GRIS_LINEA);

        Paragraph numero = new Paragraph(valor, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17, color));
        numero.setAlignment(Element.ALIGN_CENTER);
        numero.setSpacingAfter(2);
        cell.addElement(numero);

        Paragraph label = new Paragraph(titulo, f.etiqueta);
        label.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(label);
        tabla.addCell(cell);
    }

    private static void agregarTituloSeccion(Document doc, String titulo, Fuentes f) throws Exception {
        Paragraph p = new Paragraph(titulo, f.seccion);
        p.setSpacingBefore(2);
        p.setSpacingAfter(5);
        doc.add(p);
    }

    private static void agregarSubtitulo(Document doc, String texto, Fuentes f) throws Exception {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        PdfPCell c = new PdfPCell(new Phrase(texto.toUpperCase(Locale.ROOT), f.negritaVerde));
        c.setBackgroundColor(VERDE_SUAVE);
        c.setBorderColor(GRIS_LINEA);
        c.setPadding(5);
        t.addCell(c);
        doc.add(t);
        espacio(doc, 4);
    }

    private static void encabezado(PdfPTable tabla, String[] nombres, Fuentes f) {
        for (String nombre : nombres) {
            PdfPCell c = new PdfPCell(new Phrase(nombre, f.blancoBold));
            c.setBackgroundColor(VERDE);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            c.setPadding(5);
            c.setBorderColor(BLANCO);
            tabla.addCell(c);
        }
    }

    private static void agregarFilaResumen(PdfPTable tabla, String movimiento, int cantidad,
                                           String tipo, String impacto, Fuentes f) {
        PdfPCell a = celda(movimiento, f.cuerpo);
        PdfPCell b = celda(String.valueOf(cantidad), f.negritaVerde);
        PdfPCell c = celda(tipo, f.cuerpo);
        PdfPCell d = celda(impacto, f.cuerpo);
        b.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        d.setHorizontalAlignment(Element.ALIGN_CENTER);
        a.setPadding(5); b.setPadding(5); c.setPadding(5); d.setPadding(5);
        tabla.addCell(a); tabla.addCell(b); tabla.addCell(c); tabla.addCell(d);
    }

    private static PdfPCell celda(String texto, Font fuente) {
        PdfPCell c = new PdfPCell(new Phrase(texto == null ? "" : texto, fuente));
        c.setBorderColor(GRIS_LINEA);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return c;
    }

    private static PdfPTable cajaMensaje(String texto, Fuentes f) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        PdfPCell c = new PdfPCell(new Phrase(texto, f.cuerpoBold));
        c.setPadding(10);
        c.setBackgroundColor(VERDE_MUY_SUAVE);
        c.setBorderColor(GRIS_LINEA);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.addCell(c);
        return t;
    }

    private static void agregarFirma(Document doc, Fuentes f) throws Exception {
        espacio(doc, 18);
        PdfPTable firmas = new PdfPTable(3);
        firmas.setWidthPercentage(100);
        firmas.setWidths(new float[]{1, 1, 1});
        String[] textos = {
                "________________________________\nResponsable del Plantel",
                "________________________________\nResponsable de Registros",
                "________________________________\nFirma / Sello"
        };
        for (String texto : textos) {
            PdfPCell c = new PdfPCell(new Phrase(texto, f.pequena));
            c.setBorder(Rectangle.NO_BORDER);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setPaddingTop(8);
            firmas.addCell(c);
        }
        doc.add(firmas);
    }

    private static void espacio(Document doc, float alto) throws Exception {
        Paragraph p = new Paragraph(" ");
        p.setLeading(alto);
        p.setSpacingAfter(0);
        p.setSpacingBefore(0);
        doc.add(p);
    }

    // =========================================================
    // DATOS
    // =========================================================

    private static Map<String, List<SistemaDAO.Fila>> agrupar(List<SistemaDAO.Fila> filas) {
        Map<String, List<SistemaDAO.Fila>> grupos = new LinkedHashMap<>();
        for (SistemaDAO.Fila fila : filas) {
            String clasificacion = get(fila.c(), 5).isBlank() ? "Autóctonos" : get(fila.c(), 5);
            String clase = get(fila.c(), 4).isBlank() ? "Sin clasificar" : get(fila.c(), 4);
            grupos.computeIfAbsent(clasificacion + " · " + clase, k -> new ArrayList<>()).add(fila);
        }
        return grupos;
    }

    private static int movimientos(int animalId, YearMonth ym, String tipo) {
        return SistemaDAO.movimientosPeriodo(
                animalId,
                ym.atDay(1).toString(),
                ym.atEndOfMonth().toString(),
                tipo);
    }

    private static int sumarCantidadMovimientos(List<SistemaDAO.Fila> filas) {
        int total = 0;
        for (SistemaDAO.Fila fila : filas) {
            total += toInt(get(fila.c(), 3));
        }
        return total;
    }

    private static int sumarCantidadVivos(List<SistemaDAO.Fila> filas) {
        int total = 0;
        for (SistemaDAO.Fila fila : filas) {
            total += toInt(get(fila.c(), 6));
        }
        return total;
    }

    private static File prepararCarpeta() {
        File carpeta = new File("reportes");
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            throw new IllegalStateException("No se pudo crear la carpeta de reportes.");
        }
        return carpeta;
    }

    private static String get(List<String> c, int i) {
        return c.size() > i && c.get(i) != null ? c.get(i) : "";
    }

    private static int toInt(String s) {
        try {
            return Integer.parseInt(s == null ? "0" : s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    // =========================================================
    // FUENTES / PIE DE PÁGINA
    // =========================================================

    private static final class Fuentes {
        final Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, VERDE_OSCURO);
        final Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, VERDE);
        final Font seccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, VERDE_OSCURO);
        final Font negritaVerde = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, VERDE_OSCURO);
        final Font cuerpoBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, new Color(45, 58, 49));
        final Font cuerpo = FontFactory.getFont(FontFactory.HELVETICA, 7.2f, GRIS_TEXTO);
        final Font pequena = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, GRIS_TEXTO);
        final Font etiqueta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6.5f, GRIS_TEXTO);
        final Font blancoBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.2f, BLANCO);
        final Font badge = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, VERDE_OSCURO);
    }

    private static final class PiePagina extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable pie = new PdfPTable(2);
            pie.setTotalWidth(document.right() - document.left());
            pie.setWidths(new float[]{75, 25});

            PdfPCell izquierda = new PdfPCell(new Phrase(
                    "Sistema de Gestión · Reserva Natural Tatú Carreta",
                    FontFactory.getFont(FontFactory.HELVETICA, 6.5f, GRIS_TEXTO)));
            izquierda.setBorder(Rectangle.TOP);
            izquierda.setBorderColor(GRIS_LINEA);
            izquierda.setPaddingTop(4);

            PdfPCell derecha = new PdfPCell(new Phrase(
                    "Página " + writer.getPageNumber(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6.5f, VERDE_OSCURO)));
            derecha.setBorder(Rectangle.TOP);
            derecha.setBorderColor(GRIS_LINEA);
            derecha.setHorizontalAlignment(Element.ALIGN_RIGHT);
            derecha.setPaddingTop(4);

            pie.addCell(izquierda);
            pie.addCell(derecha);
            pie.writeSelectedRows(0, -1, document.left(), document.bottom() - 8, writer.getDirectContent());
        }
    }
}
