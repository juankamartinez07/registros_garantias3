package com.inventario.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

@Service
public class ExportacionExcelService {
    private final GarantiaService garantias;
    private final ServicioTecnicoService servicios;

    public ExportacionExcelService(GarantiaService garantias, ServicioTecnicoService servicios) {
        this.garantias = garantias;
        this.servicios = servicios;
    }

    public byte[] garantias(String serial, List<String> estados, String general, String especifico,
            String filtro, String sede, String orden) {
        return generar("Garantias", new String[]{"Ticket", "Serial", "Producto", "Sede que gestiona", "Sede correspondiente", "Proveedor", "Factura proveedor", "Fecha ingreso garantia", "Fecha ingreso serial", "Estado general", "Estado especifico", "Caso proveedor", "Motivo garantia", "Motivo no aplica", "Observaciones", "Usuario creador", "Fecha creacion", "Fecha actualizacion"},
                pagina -> garantias.listar(serial, estados, general, especifico, filtro, sede, PageRequest.of(pagina, 500, orden(orden, "numeroTicket"))),
                g -> new Object[]{g.getNumeroTicket(), g.getSerial(), g.getReferenciaProducto(), g.getSede(), g.getSedeCorrespondiente(), g.getProveedor(), g.getFacturaProveedor(), g.getFechaIngresoGarantia(), g.getFechaIngresoSerial(), g.getEstadoGeneral(), g.getEstadoEspecifico(), g.getNumeroCasoProveedor(), g.getMotivosGarantia(), g.getMotivoNoAplicaGarantia(), g.getObservaciones(), g.getUsuarioCreacion(), g.getFechaCreacion(), g.getFechaActualizacion()});
    }

    public byte[] servicios(String busqueda, String estado, String orden) {
        return generar("Servicio tecnico", new String[]{"Ticket", "Fecha ingreso", "Sede", "Cliente", "Telefono", "Serial", "Producto", "Marca", "Motivo revision", "Estado fisico", "Observaciones", "Usuario recibe", "Estado servicio", "Fecha creacion", "Fecha actualizacion"},
                pagina -> servicios.listar(busqueda, estado, PageRequest.of(pagina, 500, orden(orden, "ticket"))),
                s -> new Object[]{s.getTicket(), s.getFechaIngreso(), s.getSede(), s.getCliente(), s.getTelefono(), s.getSerial(), s.getProductoReferencia(), s.getMarca(), s.getMotivoRevision(), s.getEstadoFisico(), s.getObservaciones(), s.getUsuarioRecibe(), s.getEstadoServicio(), s.getFechaCreacion(), s.getFechaActualizacion()});
    }

    private Sort orden(String valor, String campo) {
        return Sort.by("desc".equalsIgnoreCase(valor) ? Sort.Direction.DESC : Sort.Direction.ASC, campo);
    }

    private <T> byte[] generar(String nombre, String[] columnas, Function<Integer, Page<T>> consulta, Function<T, Object[]> valores) {
        try (SXSSFWorkbook libro = new SXSSFWorkbook(100); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            var hoja = libro.createSheet(nombre);
            Row cabecera = hoja.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                cabecera.createCell(i).setCellValue(columnas[i]);
                hoja.setColumnWidth(i, 24 * 256);
            }
            int fila = 1;
            int numeroPagina = 0;
            Page<T> pagina;
            do {
                pagina = consulta.apply(numeroPagina++);
                for (T registro : pagina.getContent()) {
                    if (fila >= 1048576) throw new IllegalStateException("La exportacion supera el limite de filas de Excel.");
                    Row destino = hoja.createRow(fila++);
                    Object[] datos = valores.apply(registro);
                    for (int i = 0; i < datos.length; i++) {
                        destino.createCell(i).setCellValue(datos[i] == null ? "" : datos[i].toString());
                    }
                }
            } while (pagina.hasNext());
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo generar el archivo Excel.", error);
        }
    }
}
