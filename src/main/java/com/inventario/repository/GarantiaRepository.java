package com.inventario.repository;

import com.inventario.model.Garantia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GarantiaRepository extends JpaRepository<Garantia, Long> {

    boolean existsBySerialIgnoreCaseAndEstadoGeneral(String serial, String estadoGeneral);

    boolean existsByNumeroTicket(String numeroTicket);

    @Query("select max(g.numeroTicket) from Garantia g where length(g.numeroTicket) = 5")
    String maxNumeroTicketCorto();

    @Query("""
            select g
            from Garantia g
            where (:serial is null
                or lower(g.serial) like lower(concat('%', :serial, '%'))
                or lower(g.numeroTicket) like lower(concat('%', :serial, '%')))
              and (:estado is null or g.estadoGeneral = :estado or g.estadoEspecifico = :estado)
            """)
    Page<Garantia> buscar(
            @Param("serial") String serial,
            @Param("estado") String estado,
            Pageable pageable);
}
