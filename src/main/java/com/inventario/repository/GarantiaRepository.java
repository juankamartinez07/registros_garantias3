package com.inventario.repository;

import com.inventario.model.Garantia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GarantiaRepository extends JpaRepository<Garantia, Long> {

    boolean existsBySerialIgnoreCaseAndEstado(String serial, String estado);

    boolean existsByNumeroTicket(String numeroTicket);

    @Query("""
            select g
            from Garantia g
            where (:serial is null
                or lower(g.serial) like lower(concat('%', :serial, '%'))
                or lower(g.numeroTicket) like lower(concat('%', :serial, '%')))
              and (:estado is null or g.estado = :estado)
            """)
    Page<Garantia> buscar(
            @Param("serial") String serial,
            @Param("estado") String estado,
            Pageable pageable);
}
