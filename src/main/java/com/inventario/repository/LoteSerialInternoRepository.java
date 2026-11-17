package com.inventario.repository;

import com.inventario.model.LoteSerialInterno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoteSerialInternoRepository extends JpaRepository<LoteSerialInterno, Long> {
    boolean existsByCodigo(String codigo);
    @Query("select l from LoteSerialInterno l where (:sedeId is null or l.sede.id = :sedeId)")
    Page<LoteSerialInterno> consultarPorSede(@Param("sedeId") Long sedeId, Pageable pageable);
}
