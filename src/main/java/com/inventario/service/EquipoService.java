package com.inventario.service;

import com.inventario.dto.EquipoDTO;
import com.inventario.model.Equipo;
import com.inventario.model.Producto;
import com.inventario.model.Proveedor;
import com.inventario.model.TipoProducto;
import com.inventario.model.Usuario;
import com.inventario.repository.EquipoRepository;
import com.inventario.repository.ProductoRepository;
import com.inventario.repository.ProveedorRepository;
import com.inventario.repository.TipoProductoRepository;
import com.inventario.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final TipoProductoRepository tipoProductoRepository;
    private final UsuarioRepository usuarioRepository;

    public EquipoService(
            EquipoRepository equipoRepository,
            ProductoRepository productoRepository,
            ProveedorRepository proveedorRepository,
            TipoProductoRepository tipoProductoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.equipoRepository = equipoRepository;
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
        this.tipoProductoRepository = tipoProductoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Equipo> listar() {
        return equipoRepository.findAll();
    }

    public Equipo buscarPorSerial(String serial) {
        return equipoRepository.findBySerial(serial).orElse(null);
    }

    public Equipo obtenerPorId(Long id) {
        return equipoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
    }

    @Transactional
    public Equipo guardarCompleto(EquipoDTO dto) {
        String serial = limpiar(dto.getSerial());
        if (serial == null) {
            throw new RuntimeException("El serial es obligatorio");
        }

        if (equipoRepository.findBySerial(serial).isPresent()) {
            throw new RuntimeException("El serial " + serial + " ya se encuentra registrado");
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
        equipo.setUsuarioRegistro(usuario != null ? usuario.getUsername() : nombreUsuarioActual());

        if (usuario != null) {
            equipo.setSede(usuario.getSede());
        }

        return equipoRepository.save(equipo);
    }

    @Transactional
    public void guardarLote(EquipoDTO dto) {
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

        if (!repetidos.isEmpty()) {
            throw new RuntimeException("Hay seriales repetidos en el lote: " + String.join(", ", repetidos));
        }

        Set<String> existentes = new LinkedHashSet<>();
        for (String serial : serialesUnicos) {
            if (equipoRepository.findBySerial(serial).isPresent()) {
                existentes.add(serial);
            }
        }

        if (!existentes.isEmpty()) {
            throw new RuntimeException("Estos seriales ya se encuentran registrados: " + String.join(", ", existentes));
        }

        for (String serial : serialesUnicos) {
            EquipoDTO item = new EquipoDTO();
            item.setSerial(serial);
            item.setProducto(dto.getProducto());
            item.setTipo(dto.getTipo());
            item.setProveedor(dto.getProveedor());
            item.setFactura(dto.getFactura());
            item.setFecha(dto.getFecha());
            item.setEstado(dto.getEstado());
            guardarCompleto(item);
        }
    }

    @Transactional
    public Equipo actualizarCompleto(Long id, EquipoDTO dto) {
        Equipo equipo = obtenerPorId(id);
        TipoProducto tipo = obtenerTipo(dto.getTipo());
        Proveedor proveedor = obtenerProveedor(dto.getProveedor());
        Producto producto = obtenerOCrearProducto(dto.getProducto(), tipo);
        String serial = limpiar(dto.getSerial());

        if (serial != null && !serial.equals(equipo.getSerial())) {
            if (equipoRepository.findBySerial(serial).isPresent()) {
                throw new RuntimeException("El serial " + serial + " ya se encuentra registrado");
            }
            equipo.setSerial(serial);
        }

        equipo.setProducto(producto);
        equipo.setTipoProducto(tipo);
        equipo.setProveedor(proveedor);
        equipo.setFactura(limpiar(dto.getFactura()));
        equipo.setFecha(limpiar(dto.getFecha()));

        if (limpiar(dto.getEstado()) != null) {
            equipo.setEstado(limpiar(dto.getEstado()));
        }

        return equipoRepository.save(equipo);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!equipoRepository.existsById(id)) {
            throw new RuntimeException("Equipo no encontrado");
        }
        equipoRepository.deleteById(id);
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
}
