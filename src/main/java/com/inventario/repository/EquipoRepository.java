package com.inventario.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import com.inventario.model.Equipo;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    Optional<Equipo> findBySerial(String serial);

    boolean existsBySerial(String serial);

    Page<Equipo> findBySerialContainingIgnoreCase(String serial, Pageable pageable);

    long countByFechaBetween(String fechaInicio, String fechaFin);

    @Query("select count(e) from Equipo e where e.observaciones is not null and trim(e.observaciones) <> ''")
    long countConObservaciones();
}
