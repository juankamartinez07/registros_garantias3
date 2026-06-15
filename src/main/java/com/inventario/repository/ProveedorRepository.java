package com.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.inventario.model.Proveedor;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    Optional<Proveedor> findByNombre(String nombre);
}