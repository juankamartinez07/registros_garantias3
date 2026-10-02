package com.inventario.service;

import com.inventario.model.Garantia;
import com.inventario.model.ServicioTecnico;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class ComprobantePdfService {

    private static final String TELEFONO_ENCABEZADO = "318 0974067";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Font titulo = new Font(Font.HELVETICA, 16, Font.BOLD);
    private final Font subtitulo = new Font(Font.HELVETICA, 13, Font.BOLD);
    private final Font normal = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private final Font etiqueta = new Font(Font.HELVETICA, 10, Font.BOLD);
    private final Font pie = new Font(Font.HELVETICA, 9, Font.BOLD);

    public byte[] servicioTecnico(ServicioTecnico servicio, String sede) {
        return generar(documento -> {
            agregarEncabezado(documento, sede);
            agregarTitulo(documento, "COMPROBANTE DE INGRESO", "SERVICIO TECNICO");

            PdfPTable tabla = tablaDatos();
            agregarCampo(tabla, "Fecha ingreso", fecha(servicio.getFechaIngreso()));
            agregarCampo(tabla, "No. ticket", valor(servicio.getTicket()));
            agregarCampo(tabla, "Cliente", valor(servicio.getCliente()));
            agregarCampo(tabla, "Telefono", valor(servicio.getTelefono()));
            agregarCampo(tabla, "Serial", valor(servicio.getSerial()));
            agregarCampo(tabla, "Equipo", valor(servicio.getProductoReferencia()));
            agregarCampoAncho(tabla, "Motivo revision", valor(servicio.getMotivoRevision()));
            agregarCampoAncho(tabla, "Estado fisico", valor(servicio.getEstadoFisico()));
            agregarCampoAncho(tabla, "Observaciones", valor(servicio.getObservaciones()));
            documento.add(tabla);

            agregarFirmas(documento, "Firma recibe", "Firma cliente");
            agregarPie(documento, "Este comprobante certifica la recepcion del equipo para revision tecnica. No representa aprobacion de garantia.");
        });
    }

    public byte[] garantia(Garantia garantia, String sede) {
        return generar(documento -> {
            agregarEncabezado(documento, sede);
            agregarTitulo(documento, "COMPROBANTE DE INGRESO", "GARANTIA EN TRAMITE");

            PdfPTable tabla = tablaDatos();
            agregarCampo(tabla, "Fecha ingreso equipo", fecha(garantia.getFechaIngresoGarantia()));
            agregarCampo(tabla, "No. ticket", valor(garantia.getNumeroTicket()));
            agregarCampo(tabla, "Serial", valor(garantia.getSerial()));
            agregarCampo(tabla, "Producto / referencia", valor(garantia.getReferenciaProducto()));
            agregarCampoAncho(tabla, "Motivo de garantia", valor(garantia.getMotivosGarantia()));
            agregarCampoAncho(tabla, "Observaciones", valor(garantia.getObservaciones()));
            agregarCampoAncho(tabla, "Estado actual", valor(garantia.getEstadoEspecifico() == null ? garantia.getEstado() : garantia.getEstadoEspecifico()));
            documento.add(tabla);

            agregarFirmas(documento, "Firma recibe", "Firma responsable");
            agregarPie(documento, "Comprobante para control interno del proceso de garantia en tramite.");
        });
    }

    public String nombreArchivoServicioTecnico(ServicioTecnico servicio) {
        return nombreArchivo("servicio-tecnico-ticket-", servicio.getTicket(), servicio.getId());
    }

    public String nombreArchivoGarantia(Garantia garantia) {
        return nombreArchivo("garantia-ticket-", garantia.getNumeroTicket(), garantia.getId());
    }

    private byte[] generar(DocumentoPdfConstructor constructor) {
        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            Document documento = new Document(PageSize.A4, 42, 42, 46, 42);
            PdfWriter.getInstance(documento, salida);
            documento.open();
            constructor.construir(documento);
            documento.close();
            return salida.toByteArray();
        } catch (DocumentException exception) {
            throw new RuntimeException("No se pudo generar el PDF del comprobante.", exception);
        }
    }

    private void agregarEncabezado(Document documento, String sede) throws DocumentException {
        Paragraph encabezado = new Paragraph("SMD SEGURIDAD " + valor(sede), titulo);
        encabezado.setAlignment(Element.ALIGN_CENTER);
        documento.add(encabezado);

        Paragraph telefono = new Paragraph(TELEFONO_ENCABEZADO, subtitulo);
        telefono.setAlignment(Element.ALIGN_CENTER);
        telefono.setSpacingAfter(18);
        documento.add(telefono);
    }

    private void agregarTitulo(Document documento, String linea1, String linea2) throws DocumentException {
        Paragraph tituloComprobante = new Paragraph(linea1 + "\n" + linea2, subtitulo);
        tituloComprobante.setAlignment(Element.ALIGN_CENTER);
        tituloComprobante.setSpacingAfter(16);
        documento.add(tituloComprobante);
    }

    private PdfPTable tablaDatos() {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(22);
        return tabla;
    }

    private void agregarCampo(PdfPTable tabla, String nombre, String contenido) {
        PdfPCell celda = celdaCampo(nombre, contenido);
        tabla.addCell(celda);
    }

    private void agregarCampoAncho(PdfPTable tabla, String nombre, String contenido) {
        PdfPCell celda = celdaCampo(nombre, contenido);
        celda.setColspan(2);
        tabla.addCell(celda);
    }

    private PdfPCell celdaCampo(String nombre, String contenido) {
        Paragraph parrafo = new Paragraph();
        parrafo.add(new Phrase(nombre + "\n", etiqueta));
        parrafo.add(new Phrase(contenido, normal));

        PdfPCell celda = new PdfPCell(parrafo);
        celda.setPadding(9);
        celda.setBorderColor(new Color(209, 213, 219));
        return celda;
    }

    private void agregarFirmas(Document documento, String firma1, String firma2) throws DocumentException {
        PdfPTable firmas = new PdfPTable(2);
        firmas.setWidthPercentage(100);
        firmas.setSpacingBefore(28);
        firmas.setSpacingAfter(24);

        firmas.addCell(celdaFirma(firma1));
        firmas.addCell(celdaFirma(firma2));
        documento.add(firmas);
    }

    private PdfPCell celdaFirma(String texto) {
        PdfPCell celda = new PdfPCell(new Phrase("______________________________\n" + texto, etiqueta));
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setBorder(PdfPCell.NO_BORDER);
        celda.setPaddingTop(18);
        return celda;
    }

    private void agregarPie(Document documento, String texto) throws DocumentException {
        Paragraph parrafo = new Paragraph(texto, pie);
        parrafo.setAlignment(Element.ALIGN_CENTER);
        documento.add(parrafo);
    }

    private String fecha(LocalDate fecha) {
        return fecha == null ? "No registrado" : fecha.format(FORMATO_FECHA);
    }

    private String valor(String valor) {
        if (valor == null || valor.isBlank()) {
            return "No registrado";
        }
        return valor.trim();
    }

    private String nombreArchivo(String prefijo, String ticket, Long id) {
        String identificador = ticket == null || ticket.isBlank()
                ? String.valueOf(id == null ? "sin-id" : id)
                : ticket.trim();
        return sanitizar(prefijo + identificador + ".pdf");
    }

    private String sanitizar(String nombre) {
        return nombre
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]+", "-")
                .replaceAll("-+", "-");
    }

    @FunctionalInterface
    private interface DocumentoPdfConstructor {
        void construir(Document documento) throws DocumentException;
    }
}
