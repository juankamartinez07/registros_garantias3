package com.inventario.repository;

import com.inventario.model.SerialInterno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface SerialInternoRepository extends JpaRepository<SerialInterno, Long> {
    Optional<SerialInterno> findByEquipoIdEquipo(Long equipoId);
    Optional<SerialInterno> findByEquipoSerialIgnoreCase(String serial);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SerialInterno s where s.equipo.idEquipo = :equipoId")
    Optional<SerialInterno> bloquearPorEquipo(@Param("equipoId") Long equipoId);
}
