package com.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.inventario.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByNombre(String nombre);
}