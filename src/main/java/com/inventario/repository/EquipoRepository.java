package com.inventario.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import com.inventario.model.Equipo;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    Optional<Equipo> findBySerial(String serial);

    boolean existsBySerial(String serial);

    Page<Equipo> findBySerialContainingIgnoreCase(String serial, Pageable pageable);

    Page<Equipo> findBySedeId(Long sedeId, Pageable pageable);

    Page<Equipo> findBySedeIdAndSerialContainingIgnoreCase(Long sedeId, String serial, Pageable pageable);

    @Query("""
            select e
            from Equipo e
            where e.observaciones is not null
              and trim(e.observaciones) <> ''
            """)
    Page<Equipo> findConObservaciones(Pageable pageable);

    @Query("""
            select e
            from Equipo e
            where e.sede.id = :sedeId
              and e.observaciones is not null
              and trim(e.observaciones) <> ''
            """)
    Page<Equipo> findConObservacionesPorSede(@Param("sedeId") Long sedeId, Pageable pageable);

    @Query("""
            select e
            from Equipo e
            where lower(e.serial) like lower(concat('%', :serial, '%'))
              and e.observaciones is not null
              and trim(e.observaciones) <> ''
            """)
    Page<Equipo> findBySerialContainingIgnoreCaseConObservaciones(@Param("serial") String serial, Pageable pageable);

    @Query("""
            select e
            from Equipo e
            where e.sede.id = :sedeId
              and lower(e.serial) like lower(concat('%', :serial, '%'))
              and e.observaciones is not null
              and trim(e.observaciones) <> ''
            """)
    Page<Equipo> findBySedeIdAndSerialContainingIgnoreCaseConObservaciones(@Param("sedeId") Long sedeId, @Param("serial") String serial, Pageable pageable);

    long countByFechaBetween(String fechaInicio, String fechaFin);

    long countBySedeId(Long sedeId);

    long countBySedeIdAndFechaBetween(Long sedeId, String fechaInicio, String fechaFin);

    @Query("select count(e) from Equipo e where e.observaciones is not null and trim(e.observaciones) <> ''")
    long countConObservaciones();

    @Query("select count(e) from Equipo e where e.sede.id = :sedeId and e.observaciones is not null and trim(e.observaciones) <> ''")
    long countConObservacionesPorSede(@Param("sedeId") Long sedeId);

    @Query("select e from Equipo e where e.sede.id = :sedeId")
    java.util.List<Equipo> listarPorSede(@Param("sedeId") Long sedeId);
}
