package com.inventario.controller;

import com.inventario.model.Proveedor;

import com.inventario.repository.ProveedorRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorRepository repository;


    @GetMapping
    public List<Proveedor> listar(){

        return repository.findAll();

    }


    @PostMapping
    public Proveedor guardar(
            @RequestBody Proveedor proveedor){

        return repository.save(proveedor);

    }


    @PutMapping("/{id}")
    public Proveedor actualizar(
            @PathVariable Long id,
            @RequestBody Proveedor proveedor){

        Proveedor existente = repository
                .findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Proveedor no encontrado"));

        existente.setNombre(proveedor.getNombre());

        return repository.save(existente);

    }


    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Long id){

        repository.deleteById(id);

    }

}
