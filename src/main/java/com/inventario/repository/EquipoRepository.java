package com.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.inventario.model.Equipo;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    Optional<Equipo> findBySerial(String serial);


    
}