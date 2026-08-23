package com.inventario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "rol", nullable = false)
    private String rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_id")
    private Sede sede;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "demo_individual_activa")
    private Boolean demoIndividualActiva;

    @Column(name = "fecha_inicio_demo_individual")
    private LocalDate fechaInicioDemoIndividual;

    @Column(name = "dias_demo_individual")
    private Integer diasDemoIndividual;

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

    public Sede getSede() {
        return sede;
    }

    public void setSede(Sede sede) {
        this.sede = sede;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getDemoIndividualActiva() {
        return demoIndividualActiva;
    }

    public void setDemoIndividualActiva(Boolean demoIndividualActiva) {
        this.demoIndividualActiva = demoIndividualActiva;
    }

    public LocalDate getFechaInicioDemoIndividual() {
        return fechaInicioDemoIndividual;
    }

    public void setFechaInicioDemoIndividual(LocalDate fechaInicioDemoIndividual) {
        this.fechaInicioDemoIndividual = fechaInicioDemoIndividual;
    }

    public Integer getDiasDemoIndividual() {
        return diasDemoIndividual;
    }

    public void setDiasDemoIndividual(Integer diasDemoIndividual) {
        this.diasDemoIndividual = diasDemoIndividual;
    }

}

