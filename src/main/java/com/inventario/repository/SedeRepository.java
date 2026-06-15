package com.inventario.repository;

import com.inventario.model.Sede;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SedeRepository
        extends JpaRepository<Sede, Long> {

}