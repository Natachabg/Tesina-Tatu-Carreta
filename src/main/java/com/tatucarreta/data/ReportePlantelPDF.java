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
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Reporte mensual inspirado en la planilla de Inventario de Fauna entregada por la Reserva. */
public final class ReportePlantelPDF {
    private ReportePlantelPDF() {}

    public static File generar() throws Exception {
        List<SistemaDAO.Fila> filas=SistemaDAO.reportePlantelMensual();
        LocalDate hoy=LocalDate.now(); YearMonth ym=YearMonth.from(hoy); String periodo=ym.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        File carpeta=new File("reportes");if(!carpeta.exists()&&!carpeta.mkdirs())throw new IllegalStateException("No se pudo crear la carpeta de reportes.");
        File salida=new File(carpeta,"Inventario_Plantel_Fauna_"+periodo+".pdf");
        Document doc=new Document(PageSize.LETTER.rotate(),28,28,30,30);PdfWriter.getInstance(doc,new FileOutputStream(salida));doc.open();
        Font verde=FontFactory.getFont(FontFactory.HELVETICA_BOLD,16,new Color(25,76,43));Font sub=FontFactory.getFont(FontFactory.HELVETICA,9,new Color(90,105,96));Font blanco=FontFactory.getFont(FontFactory.HELVETICA_BOLD,7,Color.WHITE);Font cuerpo=FontFactory.getFont(FontFactory.HELVETICA,7,Color.DARK_GRAY);Font cuerpoBold=FontFactory.getFont(FontFactory.HELVETICA_BOLD,7,new Color(30,55,38));
        try(InputStream in=ReportePlantelPDF.class.getResourceAsStream("/images/logo.png")){Image logo=Image.getInstance(in.readAllBytes());logo.scaleToFit(72,72);PdfPTable head=new PdfPTable(2);head.setWidthPercentage(100);head.setWidths(new float[]{18,82});PdfPCell lc=new PdfPCell(logo,true);lc.setBorder(Rectangle.NO_BORDER);lc.setHorizontalAlignment(Element.ALIGN_LEFT);head.addCell(lc);PdfPCell tc=new PdfPCell();tc.setBorder(Rectangle.NO_BORDER);Paragraph p=new Paragraph("INVENTARIO · PLANTEL FAUNA SILVESTRE",verde);p.setSpacingAfter(4);tc.addElement(p);tc.addElement(new Paragraph("RESERVA NATURAL TATÚ CARRETA · REPORTE MENSUAL",sub));head.addCell(tc);doc.add(head);}catch(Exception ignored){Paragraph p=new Paragraph("INVENTARIO · PLANTEL FAUNA SILVESTRE",verde);p.setAlignment(Element.ALIGN_CENTER);doc.add(p);}
        Paragraph info=new Paragraph("Establecimiento: Reserva Natural Tatú Carreta     Período: "+periodo+"     Provincia: Córdoba",sub);info.setSpacingBefore(4);info.setSpacingAfter(8);doc.add(info);

        Map<String,List<SistemaDAO.Fila>> grupos=new LinkedHashMap<>();
        for(SistemaDAO.Fila f:filas){String clas=get(f.c(),5).isBlank()?"Autóctonos":get(f.c(),5);String clase=get(f.c(),4).isBlank()?"Sin clasificar":get(f.c(),4);grupos.computeIfAbsent(clas+" · "+clase,k->new ArrayList<>()).add(f);}
        for(var entry:grupos.entrySet()){
            Paragraph sec=new Paragraph(entry.getKey().toUpperCase(Locale.ROOT),FontFactory.getFont(FontFactory.HELVETICA_BOLD,9,new Color(25,76,43)));sec.setSpacingBefore(7);sec.setSpacingAfter(4);doc.add(sec);
            PdfPTable t=new PdfPTable(15);t.setWidthPercentage(100);t.setWidths(new float[]{3,15,18,5,4,4,5,4,4,5,5,5,12,6,12});
            String[] hs={"N°","Nombre común","Nombre científico","Prev.","M","H","Ind","Altas","M","H","Ind","Bajas","Total final","Propietario","Observaciones"};
            for(String h:hs){PdfPCell c=new PdfPCell(new Phrase(h,blanco));c.setBackgroundColor(new Color(47,122,70));c.setHorizontalAlignment(Element.ALIGN_CENTER);c.setPadding(3);t.addCell(c);}
            int n=1;
            for(SistemaDAO.Fila f:entry.getValue()){
                List<String> c=f.c();int actual=toInt(get(c,6));int altas=SistemaDAO.movimientosPeriodo(f.id(),ym.atDay(1).toString(),ym.atEndOfMonth().toString(),"Ingreso")+SistemaDAO.movimientosPeriodo(f.id(),ym.atDay(1).toString(),ym.atEndOfMonth().toString(),"Nacimiento");int bajas=SistemaDAO.movimientosPeriodo(f.id(),ym.atDay(1).toString(),ym.atEndOfMonth().toString(),"Egreso")+SistemaDAO.movimientosPeriodo(f.id(),ym.atDay(1).toString(),ym.atEndOfMonth().toString(),"Fallecimiento");int prev=SistemaDAO.snapshotAnterior(f.id());if(prev<0)prev=Math.max(0,actual-altas+bajas);
                String obs=get(c,10);String propietario=get(c,11).isBlank()?"Establecimiento":get(c,11);String[] vals={String.valueOf(n++),get(c,1),get(c,2),String.valueOf(prev),"","",String.valueOf(prev),String.valueOf(altas),"","",String.valueOf(altas),String.valueOf(bajas),String.valueOf(actual),propietario,obs};
                for(String v:vals){PdfPCell cell=new PdfPCell(new Phrase(v,cuerpo));cell.setPadding(3);t.addCell(cell);}
            }
            doc.add(t);
        }
        if(filas.isEmpty())doc.add(new Paragraph("No hay ejemplares activos en el Plantel Permanente para este período.",cuerpoBold));
        Paragraph ref=new Paragraph("REFERENCIAS: M = Macho · H = Hembra · Ind = Indeterminado. ALTAS / BAJAS corresponden a movimientos registrados durante el período. TOTAL FINAL refleja el Plantel Permanente activo al generar el reporte.",sub);ref.setSpacingBefore(10);doc.add(ref);
        Paragraph pie=new Paragraph("Generado por el Sistema de Gestión de la Reserva Natural Tatú Carreta · "+hoy,sub);pie.setSpacingBefore(8);doc.add(pie);doc.close();
        SistemaDAO.guardarSnapshot(periodo,hoy.toString(),filas);return salida;
    }
    private static String get(List<String> c,int i){return c.size()>i&&c.get(i)!=null?c.get(i):"";}
    private static int toInt(String s){try{return Integer.parseInt(s);}catch(Exception e){return 0;}}
}
