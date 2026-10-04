package com.inventario.repository;

import com.inventario.model.ServicioTecnicoHistorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicioTecnicoHistorialRepository extends JpaRepository<ServicioTecnicoHistorial, Long> {

    List<ServicioTecnicoHistorial> findByServicioTecnicoIdOrderByFechaHoraDesc(Long servicioTecnicoId);
}
