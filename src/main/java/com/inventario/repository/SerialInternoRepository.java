package com.inventario.repository;

import com.inventario.model.SerialInterno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SerialInternoRepository extends JpaRepository<SerialInterno, Long> {
    Optional<SerialInterno> findByEquipoIdEquipo(Long equipoId);
    Optional<SerialInterno> findByEquipoSerialIgnoreCase(String serial);
}
