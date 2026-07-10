package com.inventario.service;

import com.inventario.dto.EquipoDTO;
import com.inventario.model.Equipo;
import com.inventario.model.Producto;
import com.inventario.model.Proveedor;
import com.inventario.model.Sede;
import com.inventario.model.TipoProducto;
import com.inventario.model.Usuario;
import com.inventario.repository.EquipoRepository;
import com.inventario.repository.ProductoRepository;
import com.inventario.repository.ProveedorRepository;
import com.inventario.repository.SedeRepository;
import com.inventario.repository.TipoProductoRepository;
import com.inventario.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class EquipoService {

    private static final String[] COLUMNAS_EXCEL = {
            "Serial",
            "Producto",
            "Tipo / Marca",
            "Proveedor",
            "Factura",
            "Fecha",
            "Estado",
            "Observaciones / novedades",
            "Sede",
            "Registrado por"
    };

    private final EquipoRepository equipoRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final TipoProductoRepository tipoProductoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SedeRepository sedeRepository;
    private final UsuarioContextService usuarioContextService;

    public EquipoService(
            EquipoRepository equipoRepository,
            ProductoRepository productoRepository,
            ProveedorRepository proveedorRepository,
            TipoProductoRepository tipoProductoRepository,
            UsuarioRepository usuarioRepository,
            SedeRepository sedeRepository,
            UsuarioContextService usuarioContextService
    ) {
        this.equipoRepository = equipoRepository;
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
        this.tipoProductoRepository = tipoProductoRepository;
        this.usuarioRepository = usuarioRepository;
        this.sedeRepository = sedeRepository;
        this.usuarioContextService = usuarioContextService;
    }

    public List<Equipo> listar() {
        return equipoRepository.findAll();
    }

    public Page<Equipo> listarPaginado(String serial, boolean conObservaciones, Pageable pageable) {
        String filtro = limpiar(serial);
        boolean superUsuario = usuarioContextService.esSuperUsuario();
        Long sedeId = superUsuario ? null : usuarioContextService.sedeIdActual();

        if (!superUsuario && sedeId == null) {
            return Page.empty(pageable);
        }

        if (sedeId != null) {
            if (conObservaciones && filtro == null) {
                return equipoRepository.findConObservacionesPorSede(sedeId, pageable);
            }

            if (conObservaciones) {
                return equipoRepository.findBySedeIdAndSerialContainingIgnoreCaseConObservaciones(sedeId, filtro, pageable);
            }

            if (filtro == null) {
                return equipoRepository.findBySedeId(sedeId, pageable);
            }
            return equipoRepository.findBySedeIdAndSerialContainingIgnoreCase(sedeId, filtro, pageable);
        }

        if (conObservaciones && filtro == null) {
            return equipoRepository.findConObservaciones(pageable);
        }

        if (conObservaciones) {
            return equipoRepository.findBySerialContainingIgnoreCaseConObservaciones(filtro, pageable);
        }

        if (filtro == null) {
            return equipoRepository.findAll(pageable);
        }
        return equipoRepository.findBySerialContainingIgnoreCase(filtro, pageable);
    }

    public DashboardSeriales obtenerDashboard() {
        LocalDate hoy = LocalDate.now();
        String inicioMes = hoy.withDayOfMonth(1).toString();
        String finMes = hoy.withDayOfMonth(hoy.lengthOfMonth()).toString();

        return new DashboardSeriales(
                contarSerialesVisibles(),
                contarSerialesMesVisibles(inicioMes, finMes),
                contarObservacionesVisibles()
        );
    }

    public ContextoUsuario contextoUsuario() {
        return new ContextoUsuario(
                usuarioContextService.esSuperUsuario(),
                usuarioContextService.sedeIdActual(),
                usuarioContextService.sedeNombreActual()
        );
    }

    public Equipo buscarPorSerial(String serial) {
        return equipoRepository.findBySerial(serial).orElse(null);
    }

    public Equipo obtenerPorId(Long id) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
        usuarioContextService.validarMismaSede(equipo.getSede() == null ? null : equipo.getSede().getId());
        return equipo;
    }

    @Transactional
    public Equipo guardarCompleto(EquipoDTO dto) {
        String serial = limpiar(dto.getSerial());
        if (serial == null) {
            throw new RuntimeException("El serial es obligatorio");
        }

        if (equipoRepository.existsBySerial(serial)) {
            throw new RuntimeException("Este serial ya ha sido registrado, verifique la información.");
        }

        TipoProducto tipo = obtenerTipo(dto.getTipo());
        Proveedor proveedor = obtenerProveedor(dto.getProveedor());
        Producto producto = obtenerOCrearProducto(dto.getProducto(), tipo);
        Usuario usuario = obtenerUsuarioActual();

        Equipo equipo = new Equipo();
        equipo.setSerial(serial);
        equipo.setProducto(producto);
        equipo.setTipoProducto(tipo);
        equipo.setProveedor(proveedor);
        equipo.setFactura(limpiar(dto.getFactura()));
        equipo.setFecha(limpiar(dto.getFecha()));
        equipo.setEstado(limpiar(dto.getEstado()) != null ? limpiar(dto.getEstado()) : "ACTIVO");
        equipo.setObservaciones(limpiar(dto.getObservaciones()));
        equipo.setUsuarioRegistro(usuario != null ? usuario.getUsername() : nombreUsuarioActual());

        if (usuario != null) {
            equipo.setSede(usuario.getSede());
        }

        return equipoRepository.save(equipo);
    }

    @Transactional
    public ResultadoLote guardarLote(EquipoDTO dto) {
        if (dto.getSeriales() == null || dto.getSeriales().isEmpty()) {
            throw new RuntimeException("Debe ingresar al menos un serial");
        }

        Set<String> serialesUnicos = new LinkedHashSet<>();
        Set<String> repetidos = new LinkedHashSet<>();

        for (String valor : dto.getSeriales()) {
            String serial = limpiar(valor);
            if (serial == null) {
                continue;
            }
            if (!serialesUnicos.add(serial)) {
                repetidos.add(serial);
            }
        }

        if (serialesUnicos.isEmpty()) {
            throw new RuntimeException("Debe ingresar al menos un serial valido");
        }

        Set<String> existentes = new LinkedHashSet<>();
        for (String serial : serialesUnicos) {
            if (equipoRepository.existsBySerial(serial)) {
                existentes.add(serial);
            }
        }

        Set<String> duplicados = new LinkedHashSet<>();
        duplicados.addAll(repetidos);
        duplicados.addAll(existentes);

        int registrados = 0;

        for (String serial : serialesUnicos) {
            if (existentes.contains(serial)) {
                continue;
            }
            EquipoDTO item = new EquipoDTO();
            item.setSerial(serial);
            item.setProducto(dto.getProducto());
            item.setTipo(dto.getTipo());
            item.setProveedor(dto.getProveedor());
            item.setFactura(dto.getFactura());
            item.setFecha(dto.getFecha());
            item.setEstado(dto.getEstado());
            item.setObservaciones(dto.getObservaciones());
            guardarCompleto(item);
            registrados++;
        }

        return new ResultadoLote(serialesUnicos.size(), registrados, List.copyOf(duplicados));
    }

    @Transactional
    public Equipo actualizarCompleto(Long id, EquipoDTO dto) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
        usuarioContextService.validarMismaSede(equipo.getSede() == null ? null : equipo.getSede().getId());
        TipoProducto tipo = obtenerTipo(dto.getTipo());
        Proveedor proveedor = obtenerProveedor(dto.getProveedor());
        Producto producto = obtenerOCrearProducto(dto.getProducto(), tipo);
        String serial = limpiar(dto.getSerial());

        if (serial != null && !serial.equals(equipo.getSerial())) {
            if (equipoRepository.existsBySerial(serial)) {
                throw new RuntimeException("Este serial ya ha sido registrado, verifique la información.");
            }
            equipo.setSerial(serial);
        }

        equipo.setProducto(producto);
        equipo.setTipoProducto(tipo);
        equipo.setProveedor(proveedor);
        equipo.setFactura(limpiar(dto.getFactura()));
        equipo.setFecha(limpiar(dto.getFecha()));
        equipo.setObservaciones(limpiar(dto.getObservaciones()));

        if (limpiar(dto.getEstado()) != null) {
            equipo.setEstado(limpiar(dto.getEstado()));
        }

        return equipoRepository.save(equipo);
    }

    @Transactional
    public void eliminar(Long id) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
        usuarioContextService.validarMismaSede(equipo.getSede() == null ? null : equipo.getSede().getId());
        equipoRepository.deleteById(id);
    }

    public byte[] exportarExcel(Long sedeId) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Inventario");
            CellStyle estiloEncabezado = workbook.createCellStyle();
            Font fuenteEncabezado = workbook.createFont();
            fuenteEncabezado.setBold(true);
            estiloEncabezado.setFont(fuenteEncabezado);

            Row encabezado = sheet.createRow(0);
            for (int i = 0; i < COLUMNAS_EXCEL.length; i++) {
                Cell celda = encabezado.createCell(i);
                celda.setCellValue(COLUMNAS_EXCEL[i]);
                celda.setCellStyle(estiloEncabezado);
            }

            List<Equipo> equipos = equiposParaExportar(sedeId);
            int filaActual = 1;

            for (Equipo equipo : equipos) {
                Row fila = sheet.createRow(filaActual++);
                fila.createCell(0).setCellValue(valor(equipo.getSerial()));
                fila.createCell(1).setCellValue(equipo.getProducto() == null ? "" : valor(equipo.getProducto().getNombre()));
                fila.createCell(2).setCellValue(equipo.getTipoProducto() == null ? "" : valor(equipo.getTipoProducto().getNombre()));
                fila.createCell(3).setCellValue(equipo.getProveedor() == null ? "" : valor(equipo.getProveedor().getNombre()));
                fila.createCell(4).setCellValue(valor(equipo.getFactura()));
                fila.createCell(5).setCellValue(valor(equipo.getFecha()));
                fila.createCell(6).setCellValue(valor(equipo.getEstado()));
                fila.createCell(7).setCellValue(valor(equipo.getObservaciones()));
                fila.createCell(8).setCellValue(equipo.getSede() == null ? "" : valor(equipo.getSede().getNombre()));
                fila.createCell(9).setCellValue(valor(equipo.getUsuarioRegistro()));
            }

            for (int i = 0; i < COLUMNAS_EXCEL.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(salida);
            return salida.toByteArray();
        } catch (IOException exception) {
            throw new RuntimeException("No se pudo generar el archivo Excel");
        }
    }

    public List<SedeExcel> listarSedesExcel() {
        if (!usuarioContextService.esSuperUsuario()) {
            Long sedeId = usuarioContextService.sedeIdActual();
            String sedeNombre = usuarioContextService.sedeNombreActual();
            return sedeId == null ? List.of() : List.of(new SedeExcel(sedeId, valor(sedeNombre)));
        }

        return sedeRepository.findAll()
                .stream()
                .map(sede -> new SedeExcel(
                        sede.getId(),
                        valor(sede.getNombre())))
                .toList();
    }

    private List<Equipo> equiposParaExportar(Long sedeId) {
        if (!usuarioContextService.esSuperUsuario()) {
            Long sedeUsuario = usuarioContextService.sedeIdActual();
            return sedeUsuario == null ? List.of() : equipoRepository.listarPorSede(sedeUsuario);
        }

        List<Equipo> equipos = equipoRepository.findAll();

        if (sedeId == null) {
            return equipos;
        }

        return equipos
                .stream()
                .filter(equipo -> equipo.getSede() != null)
                .filter(equipo -> sedeId.equals(equipo.getSede().getId()))
                .toList();
    }

    private long contarSerialesVisibles() {
        boolean superUsuario = usuarioContextService.esSuperUsuario();
        Long sedeId = superUsuario ? null : usuarioContextService.sedeIdActual();
        if (!superUsuario && sedeId == null) {
            return 0;
        }
        return sedeId == null ? equipoRepository.count() : equipoRepository.countBySedeId(sedeId);
    }

    private long contarSerialesMesVisibles(String inicioMes, String finMes) {
        boolean superUsuario = usuarioContextService.esSuperUsuario();
        Long sedeId = superUsuario ? null : usuarioContextService.sedeIdActual();
        if (!superUsuario && sedeId == null) {
            return 0;
        }
        return sedeId == null
                ? equipoRepository.countByFechaBetween(inicioMes, finMes)
                : equipoRepository.countBySedeIdAndFechaBetween(sedeId, inicioMes, finMes);
    }

    private long contarObservacionesVisibles() {
        boolean superUsuario = usuarioContextService.esSuperUsuario();
        Long sedeId = superUsuario ? null : usuarioContextService.sedeIdActual();
        if (!superUsuario && sedeId == null) {
            return 0;
        }
        return sedeId == null
                ? equipoRepository.countConObservaciones()
                : equipoRepository.countConObservacionesPorSede(sedeId);
    }

    @Transactional
    public ResultadoImportacionExcel importarExcel(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException("Seleccione un archivo Excel.");
        }

        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null || !nombreArchivo.toLowerCase().endsWith(".xlsx")) {
            throw new RuntimeException("El archivo debe tener formato .xlsx");
        }

        List<FilaExcel> filas = leerFilasExcel(archivo);
        if (filas.isEmpty()) {
            throw new RuntimeException("El archivo no contiene registros para importar.");
        }

        validarSerialesExcel(filas);

        int registrados = 0;
        List<String> duplicados = new ArrayList<>();
        for (FilaExcel fila : filas) {
            try {
                String serial = limpiar(fila.dto().getSerial());
                if (serial != null && equipoRepository.existsBySerial(serial)) {
                    duplicados.add(serial);
                    continue;
                }
                guardarCompleto(fila.dto());
                registrados++;
            } catch (RuntimeException exception) {
                throw new RuntimeException("Fila " + fila.numero() + ": " + exception.getMessage());
            }
        }

        return new ResultadoImportacionExcel(filas.size(), registrados, duplicados);
    }

    private List<FilaExcel> leerFilasExcel(MultipartFile archivo) {
        try (Workbook workbook = WorkbookFactory.create(archivo.getInputStream())) {
            Sheet sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
            if (sheet == null) {
                return List.of();
            }

            DataFormatter formatter = new DataFormatter();
            List<FilaExcel> filas = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (filaVacia(row, formatter)) {
                    continue;
                }

                EquipoDTO dto = new EquipoDTO();
                dto.setSerial(textoCelda(row, 0, formatter));
                dto.setProducto(textoCelda(row, 1, formatter));
                dto.setTipo(textoCelda(row, 2, formatter));
                dto.setProveedor(textoCelda(row, 3, formatter));
                dto.setFactura(textoCelda(row, 4, formatter));
                dto.setFecha(fechaCelda(row, 5, formatter));
                dto.setEstado(textoCelda(row, 6, formatter));
                dto.setObservaciones(textoCelda(row, 7, formatter));

                filas.add(new FilaExcel(i + 1, dto));
            }

            return filas;
        } catch (IOException exception) {
            throw new RuntimeException("No se pudo leer el archivo Excel.");
        }
    }

    private void validarSerialesExcel(List<FilaExcel> filas) {
        Map<String, Integer> serialesArchivo = new HashMap<>();
        List<String> errores = new ArrayList<>();

        for (FilaExcel fila : filas) {
            String serial = limpiar(fila.dto().getSerial());
            if (serial == null) {
                errores.add("Fila " + fila.numero() + ": el serial es obligatorio");
                continue;
            }

            Integer filaRepetida = serialesArchivo.putIfAbsent(serial, fila.numero());
            if (filaRepetida != null) {
                errores.add("Fila " + fila.numero() + ": serial repetido en el archivo. Ya existe en la fila " + filaRepetida);
            }

        }

        if (!errores.isEmpty()) {
            throw new RuntimeException(String.join("\n", errores));
        }
    }

    private boolean filaVacia(Row row, DataFormatter formatter) {
        if (row == null) {
            return true;
        }

        for (int i = 0; i <= 7; i++) {
            if (limpiar(textoCelda(row, i, formatter)) != null) {
                return false;
            }
        }

        return true;
    }

    private String textoCelda(Row row, int indice, DataFormatter formatter) {
        if (row == null) {
            return null;
        }

        Cell cell = row.getCell(indice);
        if (cell == null) {
            return null;
        }

        return limpiar(formatter.formatCellValue(cell));
    }

    private String fechaCelda(Row row, int indice, DataFormatter formatter) {
        if (row == null) {
            return null;
        }

        Cell cell = row.getCell(indice);
        if (cell == null) {
            return null;
        }

        if (DateUtil.isCellDateFormatted(cell)) {
            LocalDate fecha = cell.getDateCellValue()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            return fecha.toString();
        }

        return textoCelda(row, indice, formatter);
    }

    private TipoProducto obtenerTipo(String nombre) {
        String valor = limpiar(nombre);
        if (valor == null) {
            return null;
        }

        return tipoProductoRepository.findByNombre(valor)
                .orElseThrow(() -> new RuntimeException("El tipo o marca no existe: " + valor));
    }

    private Proveedor obtenerProveedor(String nombre) {
        String valor = limpiar(nombre);
        if (valor == null) {
            return null;
        }

        return proveedorRepository.findByNombre(valor)
                .orElseThrow(() -> new RuntimeException("El proveedor no existe: " + valor));
    }

    private Producto obtenerOCrearProducto(String nombre, TipoProducto tipo) {
        String valor = limpiar(nombre);
        if (valor == null) {
            return null;
        }

        return productoRepository.findByNombre(valor)
                .map(producto -> {
                    if (producto.getTipo() == null && tipo != null) {
                        producto.setTipo(tipo);
                        return productoRepository.save(producto);
                    }
                    return producto;
                })
                .orElseGet(() -> {
                    Producto producto = new Producto();
                    producto.setNombre(valor);
                    producto.setTipo(tipo);
                    return productoRepository.save(producto);
                });
    }

    private Usuario obtenerUsuarioActual() {
        String username = nombreUsuarioActual();
        if (username == null || "SISTEMA".equals(username)) {
            return null;
        }
        return usuarioRepository.findByUsername(username).orElse(null);
    }

    private String nombreUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "SISTEMA";
        }
        return authentication.getName();
    }

    private String limpiar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    private String valor(String valor) {
        return valor == null ? "" : valor;
    }

    private record FilaExcel(int numero, EquipoDTO dto) {
    }

    public record DashboardSeriales(long totalSeriales, long serialesMesActual, long serialesConObservaciones) {
    }

    public record ResultadoLote(int total, int registrados, List<String> duplicados) {
    }

    public record ResultadoImportacionExcel(int total, int registrados, List<String> duplicados) {
    }

    public record SedeExcel(Long id, String nombre) {
    }

    public record ContextoUsuario(boolean superUsuario, Long sedeId, String sedeNombre) {
    }
}
