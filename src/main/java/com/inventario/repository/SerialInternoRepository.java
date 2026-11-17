package com.inventario.repository;

import com.inventario.model.SerialInterno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.inventario.model.EstadoSerialInterno;

public interface SerialInternoRepository extends JpaRepository<SerialInterno, Long> {
    Optional<SerialInterno> findByEquipoIdEquipo(Long equipoId);
    Optional<SerialInterno> findByEquipoSerialIgnoreCase(String serial);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SerialInterno s where s.equipo.idEquipo = :equipoId")
    Optional<SerialInterno> bloquearPorEquipo(@Param("equipoId") Long equipoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SerialInterno s where s.id = :id")
    Optional<SerialInterno> bloquearPorId(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SerialInterno s where s.lote.id = :loteId and s.estado = com.inventario.model.EstadoSerialInterno.PENDIENTE_ACTIVACION order by s.id")
    List<SerialInterno> bloquearPendientesLote(@Param("loteId") Long loteId);

    @Query("""
            select s from SerialInterno s join s.lote l join s.equipo e
            where (:sedeId is null or l.sede.id = :sedeId)
              and (:estado is null or s.estado = :estado)
              and (:lote is null or lower(l.codigo) like lower(concat('%', :lote, '%')))
              and (:referencia is null or lower(e.producto.nombre) like lower(concat('%', :referencia, '%')))
              and (:vencidos = false or (s.estado = com.inventario.model.EstadoSerialInterno.PENDIENTE_ACTIVACION and s.fechaLimite < :hoy))
            """)
    Page<SerialInterno> consultar(@Param("sedeId") Long sedeId, @Param("estado") EstadoSerialInterno estado,
            @Param("lote") String lote, @Param("referencia") String referencia,
            @Param("vencidos") boolean vencidos, @Param("hoy") LocalDate hoy, Pageable pageable);
}
