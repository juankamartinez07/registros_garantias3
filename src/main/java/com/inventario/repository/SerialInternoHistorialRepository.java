package com.inventario.repository;

import com.inventario.model.SerialInternoHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SerialInternoHistorialRepository extends JpaRepository<SerialInternoHistorial, Long> {
    List<SerialInternoHistorial> findBySerialInternoIdOrderByFechaHoraDesc(Long serialInternoId);
}
