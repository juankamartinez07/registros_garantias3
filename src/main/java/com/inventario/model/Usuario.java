package com.inventario.model;

import jakarta.persistence.*;

@Entity


@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    private String rol;
    @ManyToOne

@JoinColumn(name = "sede_id")

private Sede sede;


    // GETTERS Y SETTERS

    public Sede getSede() {
    return sede;
}

public void setSede(Sede sede) {
    this.sede = sede;
}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

}