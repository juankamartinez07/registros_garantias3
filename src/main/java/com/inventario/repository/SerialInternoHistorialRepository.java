package com.inventario.repository;

import com.inventario.model.SerialInternoHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SerialInternoHistorialRepository extends JpaRepository<SerialInternoHistorial, Long> {
    List<SerialInternoHistorial> findBySerialInternoIdOrderByFechaHoraDesc(Long serialInternoId);
    Page<SerialInternoHistorial> findBySerialInternoId(Long serialInternoId, Pageable pageable);
}
