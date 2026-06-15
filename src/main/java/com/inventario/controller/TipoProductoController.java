package com.inventario.controller;

import com.inventario.model.TipoProducto;

import com.inventario.repository.TipoProductoRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tipos")
public class TipoProductoController {

    @Autowired
    private TipoProductoRepository repository;


    @GetMapping
    public List<TipoProducto> listar(){

        return repository.findAll();

    }


  @PostMapping
public TipoProducto guardar(
        @RequestBody TipoProducto tipo){

    if(repository.findByNombre(
            tipo.getNombre()
    ).isPresent()){

        throw new RuntimeException(
                "El tipo ya existe"
        );

    }

    return repository.save(tipo);

}


@PutMapping("/{id}")
public TipoProducto actualizar(
        @PathVariable Long id,
        @RequestBody TipoProducto tipo){

    TipoProducto existente = repository
            .findById(id)
            .orElseThrow(() -> new RuntimeException(
                    "Tipo o marca no encontrado"));

    existente.setNombre(tipo.getNombre());

    return repository.save(existente);

}


@DeleteMapping("/{id}")
public void eliminar(
        @PathVariable Long id){

    repository.deleteById(id);

}

}
