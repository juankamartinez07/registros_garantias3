package com.inventario.controller;

import com.inventario.dto.EquipoDTO;
import com.inventario.model.Equipo;
import com.inventario.model.Producto;
import com.inventario.model.Proveedor;
import com.inventario.model.TipoProducto;
import com.inventario.repository.ProductoRepository;
import com.inventario.repository.ProveedorRepository;
import com.inventario.repository.TipoProductoRepository;
import com.inventario.service.EquipoService;
import com.inventario.service.EquipoService.DashboardSeriales;
import com.inventario.service.EquipoService.ResultadoImportacionExcel;
import com.inventario.service.EquipoService.ResultadoLote;
import com.inventario.service.EquipoService.SedeExcel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/equipos")
public class EquipoController {

    private final EquipoService equipoService;
    private final TipoProductoRepository tipoProductoRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;

    public EquipoController(
            EquipoService equipoService,
            TipoProductoRepository tipoProductoRepository,
            ProductoRepository productoRepository,
            ProveedorRepository proveedorRepository
    ) {
        this.equipoService = equipoService;
        this.tipoProductoRepository = tipoProductoRepository;
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
    }

    @GetMapping
    public Page<Equipo> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String serial,
            @RequestParam(defaultValue = "false") boolean conObservaciones) {

        int pagina = Math.max(page, 0);
        int tamano = Math.min(Math.max(size, 10), 50);
        Pageable pageable = PageRequest.of(
                pagina,
                tamano,
                Sort.by(Sort.Direction.DESC, "idEquipo"));

        return equipoService.listarPaginado(serial, conObservaciones, pageable);
    }

    @GetMapping("/dashboard")
    public DashboardSeriales dashboard() {
        return equipoService.obtenerDashboard();
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) Long sedeId) {

        byte[] archivo = equipoService.exportarExcel(sedeId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=inventario.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(archivo);
    }

    @GetMapping("/sedes-excel")
    public List<SedeExcel> listarSedesExcel() {
        return equipoService.listarSedesExcel();
    }

    @PostMapping("/excel")
    public ResultadoImportacionExcel importarExcel(
            @RequestParam("archivo") MultipartFile archivo) {

        return equipoService.importarExcel(archivo);

    }

    @GetMapping("/id/{id}")
    public Equipo obtenerPorId(@PathVariable Long id) {
        return equipoService.obtenerPorId(id);
    }

    @GetMapping("/{serial}")
    public Equipo buscarPorSerial(@PathVariable String serial) {
        return equipoService.buscarPorSerial(serial);
    }

    @PostMapping("/completo")
    public Equipo guardarCompleto(@RequestBody EquipoDTO dto) {
        return equipoService.guardarCompleto(dto);
    }

    @PostMapping("/lote")
    public ResultadoLote guardarLote(@RequestBody EquipoDTO dto) {
        return equipoService.guardarLote(dto);
    }

    @PutMapping("/editar/{id}")
    public Equipo actualizarCompleto(@PathVariable Long id, @RequestBody EquipoDTO dto) {
        return equipoService.actualizarCompleto(id, dto);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        equipoService.eliminar(id);
    }

    @GetMapping("/tipos")
    public List<TipoProducto> obtenerTipos() {
        return tipoProductoRepository.findAll();
    }

    @GetMapping("/productos")
    public List<Producto> obtenerProductos() {
        return productoRepository.findAll();
    }

    @GetMapping("/proveedores")
    public List<Proveedor> obtenerProveedores() {
        return proveedorRepository.findAll();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String manejarError(RuntimeException exception) {
        return exception.getMessage();
    }
}
