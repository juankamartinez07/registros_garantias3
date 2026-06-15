package com.inventario.controller;

import com.inventario.model.Sede;

import com.inventario.repository.SedeRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sedes")
public class SedeController {

    @Autowired
    private SedeRepository repository;


    // =========================
    // LISTAR
    // =========================

    @GetMapping
    public List<Sede> listar(){

        return repository.findAll();

    }


    // =========================
    // GUARDAR
    // =========================

    @PostMapping
    public Sede guardar(
            @RequestBody Sede sede){

        return repository.save(sede);

    }


    // =========================
    // ACTUALIZAR
    // =========================

    @PutMapping("/{id}")
    public Sede actualizar(
            @PathVariable Long id,
            @RequestBody Sede sede){

        Sede existente = repository
                .findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Sede no encontrada"));

        existente.setNombre(sede.getNombre());

        return repository.save(existente);

    }


    // =========================
    // ELIMINAR
    // =========================

    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Long id){

        repository.deleteById(id);

    }

}
