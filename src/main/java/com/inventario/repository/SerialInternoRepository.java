package com.inventario.repository;

import com.inventario.model.SerialInterno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;

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
}
