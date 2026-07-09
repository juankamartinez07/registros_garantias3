package com.inventario.repository;

import com.inventario.model.Garantia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

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
              and (:estadoGeneral is null or g.estadoGeneral = :estadoGeneral)
              and (:estadoEspecifico is null or g.estadoEspecifico = :estadoEspecifico)
              and (:ingresadasMesActual = false or g.fechaIngresoGarantia between :inicioMes and :finMes)
              and (:sinCasoProveedor = false or (g.estadoGeneral = 'Abierto' and (g.numeroCasoProveedor is null or trim(g.numeroCasoProveedor) = '')))
              and (:abiertas10Dias = false or (g.estadoGeneral = 'Abierto' and g.fechaIngresoGarantia <= :fechaLimite10Dias))
              and (:tiempoAbiertas = false or g.estadoGeneral = 'Abierto')
            """)
    Page<Garantia> buscar(
            @Param("serial") String serial,
            @Param("estado") String estado,
            @Param("estadoGeneral") String estadoGeneral,
            @Param("estadoEspecifico") String estadoEspecifico,
            @Param("ingresadasMesActual") boolean ingresadasMesActual,
            @Param("sinCasoProveedor") boolean sinCasoProveedor,
            @Param("abiertas10Dias") boolean abiertas10Dias,
            @Param("tiempoAbiertas") boolean tiempoAbiertas,
            @Param("inicioMes") LocalDate inicioMes,
            @Param("finMes") LocalDate finMes,
            @Param("fechaLimite10Dias") LocalDate fechaLimite10Dias,
            Pageable pageable);

    long countByEstadoGeneral(String estadoGeneral);

    long countByEstadoEspecifico(String estadoEspecifico);

    long countByFechaIngresoGarantiaBetween(LocalDate inicio, LocalDate fin);

    @Query("""
            select count(g)
            from Garantia g
            where g.estadoGeneral = 'Abierto'
              and (g.numeroCasoProveedor is null or trim(g.numeroCasoProveedor) = '')
            """)
    long contarAbiertasSinCasoProveedor();

    @Query("""
            select count(g)
            from Garantia g
            where g.estadoGeneral = 'Abierto'
              and g.fechaIngresoGarantia <= :fechaLimite
            """)
    long contarAbiertasMas10Dias(@Param("fechaLimite") LocalDate fechaLimite);

    @Query("""
            select min(g.fechaIngresoGarantia)
            from Garantia g
            where g.estadoGeneral = 'Abierto'
              and g.fechaIngresoGarantia is not null
            """)
    LocalDate fechaAbiertaMasAntigua();
}
