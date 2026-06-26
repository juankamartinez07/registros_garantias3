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
import com.inventario.service.EquipoService.ResultadoImportacionExcel;
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
    public List<Equipo> listar() {
        return equipoService.listar();
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportarExcel() {
        byte[] archivo = equipoService.exportarExcel();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=inventario.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(archivo);
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
    public void guardarLote(@RequestBody EquipoDTO dto) {
        equipoService.guardarLote(dto);
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
