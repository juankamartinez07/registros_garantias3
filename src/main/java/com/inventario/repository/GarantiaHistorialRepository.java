package com.inventario.repository;

import com.inventario.model.GarantiaHistorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GarantiaHistorialRepository extends JpaRepository<GarantiaHistorial, Long> {

    List<GarantiaHistorial> findByGarantiaIdOrderByFechaHoraDesc(Long garantiaId);
}
