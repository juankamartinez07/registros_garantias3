package com.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.inventario.model.TipoProducto;

public interface TipoProductoRepository extends JpaRepository<TipoProducto, Long> {

    Optional<TipoProducto> findByNombre(String nombre);

}