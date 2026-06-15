package com.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.inventario.model.Ingreso;

public interface IngresoRepository extends JpaRepository<Ingreso, Long> {
}