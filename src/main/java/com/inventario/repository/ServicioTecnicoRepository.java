package com.inventario.repository;

import com.inventario.model.ServicioTecnico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServicioTecnicoRepository extends JpaRepository<ServicioTecnico, Long> {

    boolean existsByTicket(String ticket);

    @Query("select max(s.ticket) from ServicioTecnico s where length(s.ticket) = 5")
    String maxTicketCorto();

    @Query("""
            select s
            from ServicioTecnico s
            where (:busqueda is null
                or lower(s.serial) like lower(concat('%', :busqueda, '%'))
                or lower(s.ticket) like lower(concat('%', :busqueda, '%'))
                or lower(s.cliente) like lower(concat('%', :busqueda, '%')))
              and (:estadoServicio is null or s.estadoServicio = :estadoServicio)
              and (:sedeNombre is null or s.sede = :sedeNombre)
            """)
    Page<ServicioTecnico> buscar(
            @Param("busqueda") String busqueda,
            @Param("estadoServicio") String estadoServicio,
            @Param("sedeNombre") String sedeNombre,
            Pageable pageable);
}
