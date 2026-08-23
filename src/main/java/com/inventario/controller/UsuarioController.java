package com.inventario.controller;

import com.inventario.model.Sede;
import com.inventario.model.Usuario;
import com.inventario.repository.SedeRepository;
import com.inventario.repository.UsuarioRepository;
import com.inventario.service.DemoService;
import com.inventario.service.UsuarioContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.time.LocalDate;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private static final Set<String> ROLES_VALIDOS =
            Set.of("SUPER_ADMIN", "ADMIN", "TECNICO", "USER");

    private static final Set<String> ROLES_SUPER =
            Set.of("SUPER_ADMIN");

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SedeRepository sedeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioContextService usuarioContextService;

    @Autowired
    private DemoService demoService;

    @GetMapping
    public List<UsuarioRespuesta> listar() {

        return usuarioRepository
                .findAll()
                .stream()
                .map(usuario -> new UsuarioRespuesta(usuario, demoService.estadoParaUsuario(usuario)))
                .toList();

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioRespuesta guardar(
            @RequestBody UsuarioSolicitud solicitud) {

        validarSolicitud(solicitud, true);

        if (usuarioRepository
                .findByUsername(solicitud.getUsername().trim())
                .isPresent()) {

            throw new RuntimeException(
                    "El usuario ya existe.");

        }

        Usuario usuario = new Usuario();

        usuario.setUsername(
                solicitud.getUsername().trim());

        usuario.setPassword(
                passwordEncoder.encode(
                        solicitud.getPassword()));

        usuario.setRol(normalizarRol(solicitud.getRol()));
        usuario.setActivo(solicitud.getActivo() == null || solicitud.getActivo());

        usuario.setSede(
                obtenerSede(solicitud.getSedeId()));

        Usuario guardado = usuarioRepository.save(usuario);

        return new UsuarioRespuesta(
                guardado,
                demoService.estadoParaUsuario(guardado));

    }

    @PutMapping("/{id}")
    public UsuarioRespuesta actualizar(
            @PathVariable Long id,
            @RequestBody UsuarioSolicitud solicitud) {

        validarSolicitud(solicitud, false);

        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado."));

        String username = solicitud.getUsername().trim();
        usuarioRepository
                .findByUsername(username)
                .filter(existente -> !existente.getId().equals(id))
                .ifPresent(existente -> {
                    throw new RuntimeException("El usuario ya existe.");
                });

        String rolAnterior = normalizarRol(usuario.getRol());
        String rolNuevo = normalizarRol(solicitud.getRol());
        Boolean activoNuevo = solicitud.getActivo() == null ? usuario.getActivo() : solicitud.getActivo();

        validarNoDesprotegerSuperUsuario(usuario, rolAnterior, rolNuevo, activoNuevo);

        usuario.setUsername(username);
        usuario.setRol(rolNuevo);
        usuario.setSede(obtenerSede(solicitud.getSedeId()));
        usuario.setActivo(activoNuevo);

        Usuario guardado = usuarioRepository.save(usuario);

        return new UsuarioRespuesta(
                guardado,
                demoService.estadoParaUsuario(guardado));

    }

    @PutMapping("/{id}/demo")
    public UsuarioRespuesta actualizarDemoUsuario(
            @PathVariable Long id,
            @RequestBody DemoUsuarioSolicitud solicitud) {

        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado."));

        if (usuarioContextService.esRolSuperUsuario(usuario.getRol())) {
            throw new RuntimeException("El SUPER_ADMIN no requiere configuracion demo individual.");
        }

        if (!Boolean.TRUE.equals(solicitud.getDemoIndividualActiva())) {
            usuario.setDemoIndividualActiva(null);
            usuario.setFechaInicioDemoIndividual(null);
            usuario.setDiasDemoIndividual(null);
        } else {
            LocalDate fechaInicio = solicitud.getFechaInicioDemoIndividual() == null
                    ? LocalDate.now()
                    : solicitud.getFechaInicioDemoIndividual();
            Integer diasDemo = solicitud.getDiasDemoIndividual();

            if (diasDemo == null || diasDemo < 1) {
                throw new RuntimeException("La duracion de la demo individual debe ser mayor a cero.");
            }

            usuario.setDemoIndividualActiva(true);
            usuario.setFechaInicioDemoIndividual(fechaInicio);
            usuario.setDiasDemoIndividual(diasDemo);
        }

        Usuario guardado = usuarioRepository.save(usuario);

        return new UsuarioRespuesta(
                guardado,
                demoService.estadoParaUsuario(guardado));

    }

    @PutMapping("/{id}/password")
    public UsuarioRespuesta cambiarPassword(
            @PathVariable Long id,
            @RequestBody UsuarioSolicitud solicitud) {

        if (solicitud.getPassword() == null ||
                solicitud.getPassword().isBlank()) {

            throw new RuntimeException(
                    "Ingrese la nueva contraseña.");

        }

        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado."));

        usuario.setPassword(
                passwordEncoder.encode(
                        solicitud.getPassword()));

        Usuario guardado = usuarioRepository.save(usuario);

        return new UsuarioRespuesta(
                guardado,
                demoService.estadoParaUsuario(guardado));

    }

    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Long id) {

        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado."));

        validarNoDesprotegerSuperUsuario(usuario, normalizarRol(usuario.getRol()), null, false);
        usuarioRepository.deleteById(id);

    }

    private void validarSolicitud(
            UsuarioSolicitud solicitud,
            boolean validarPassword) {

        if (solicitud.getUsername() == null ||
                solicitud.getUsername().isBlank()) {

            throw new RuntimeException(
                    "Ingrese el nombre de usuario.");

        }

        if (validarPassword &&
                (solicitud.getPassword() == null ||
                        solicitud.getPassword().isBlank())) {

            throw new RuntimeException(
                    "Ingrese la contraseña.");

        }

        if (solicitud.getRol() == null ||
                solicitud.getRol().isBlank()) {

            throw new RuntimeException(
                    "Seleccione el rol del usuario.");

        }

        if (!ROLES_VALIDOS.contains(normalizarRol(solicitud.getRol()))) {

            throw new RuntimeException(
                    "Seleccione un rol valido.");

        }

    }

    private String normalizarRol(String rol) {

        String rolLimpio = rol == null
                ? ""
                : rol.trim().toUpperCase(Locale.ROOT);

        if (rolLimpio.startsWith("ROLE_")) {

            rolLimpio = rolLimpio.substring("ROLE_".length());

        }

        if ("USUARIO".equals(rolLimpio)) {

            return "USER";

        }

        if ("SUPERUSUARIO".equals(rolLimpio) || "SUPERUSER".equals(rolLimpio) || "SUPERADMIN".equals(rolLimpio)) {

            return "SUPER_ADMIN";

        }

        return rolLimpio;

    }

    private void validarNoDesprotegerSuperUsuario(
            Usuario usuario,
            String rolAnterior,
            String rolNuevo,
            Boolean activoNuevo) {

        String usuarioActual = usuarioContextService.usernameActual();
        if (usuarioActual != null
                && usuarioActual.equals(usuario.getUsername())
                && Boolean.FALSE.equals(activoNuevo)) {
            throw new RuntimeException("No puede deshabilitar su propio usuario.");
        }

        boolean eraSuperActivo = ROLES_SUPER.contains(rolAnterior)
                && !Boolean.FALSE.equals(usuario.getActivo());
        boolean quedaSuperActivo = rolNuevo != null
                && ROLES_SUPER.contains(rolNuevo)
                && !Boolean.FALSE.equals(activoNuevo);

        if (eraSuperActivo && !quedaSuperActivo
                && usuarioRepository.countByRolInAndActivoTrue(ROLES_SUPER) <= 1) {
            throw new RuntimeException("No se puede deshabilitar o quitar el rol del ultimo SUPER_ADMIN activo.");
        }

    }

    private Sede obtenerSede(Long sedeId) {

        if (sedeId == null) {

            return null;

        }

        return sedeRepository
                .findById(sedeId)
                .orElse(null);

    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String manejarError(RuntimeException ex) {

        return ex.getMessage();

    }

    public static class UsuarioSolicitud {

        private String username;
        private String password;
        private String rol;
        private Long sedeId;
        private Boolean activo;

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

        public Long getSedeId() {
            return sedeId;
        }

        public void setSedeId(Long sedeId) {
            this.sedeId = sedeId;
        }

        public Boolean getActivo() {
            return activo;
        }

        public void setActivo(Boolean activo) {
            this.activo = activo;
        }

    }

    public static class DemoUsuarioSolicitud {

        private Boolean demoIndividualActiva;
        private LocalDate fechaInicioDemoIndividual;
        private Integer diasDemoIndividual;

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

    public static class UsuarioRespuesta {

        private Long id;
        private String username;
        private String rol;
        private String sede;
        private Long sedeId;
        private Boolean activo;
        private String passwordEstado;
        private Boolean demoIndividualActiva;
        private LocalDate fechaInicioDemoIndividual;
        private Integer diasDemoIndividual;
        private LocalDate demoFechaFinalizacion;
        private Long demoDiasRestantes;
        private Boolean demoExpirada;
        private String demoEstado;
        private String demoOrigen;

        public UsuarioRespuesta(Usuario usuario) {
            this(usuario, null);
        }

        public UsuarioRespuesta(Usuario usuario, DemoService.DemoEstado demoEstadoUsuario) {

            this.id = usuario.getId();
            this.username = usuario.getUsername();
            this.rol = usuario.getRol();
            this.sede = usuario.getSede() == null
                    ? ""
                    : usuario.getSede().getNombre();
            this.sedeId = usuario.getSede() == null
                    ? null
                    : usuario.getSede().getId();
            this.activo = !Boolean.FALSE.equals(usuario.getActivo());
            this.passwordEstado = "Protegida";
            this.demoIndividualActiva = Boolean.TRUE.equals(usuario.getDemoIndividualActiva());
            this.fechaInicioDemoIndividual = usuario.getFechaInicioDemoIndividual();
            this.diasDemoIndividual = usuario.getDiasDemoIndividual();

            if (demoEstadoUsuario != null) {
                this.demoFechaFinalizacion = demoEstadoUsuario.fechaFinalizacion();
                this.demoDiasRestantes = demoEstadoUsuario.diasRestantes();
                this.demoExpirada = demoEstadoUsuario.demoExpirada();
                this.demoEstado = demoEstadoUsuario.estado();
                this.demoOrigen = demoEstadoUsuario.origen();
            }

        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getRol() {
            return rol;
        }

        public String getSede() {
            return sede;
        }

        public Long getSedeId() {
            return sedeId;
        }

        public Boolean getActivo() {
            return activo;
        }

        public String getPasswordEstado() {
            return passwordEstado;
        }

        public Boolean getDemoIndividualActiva() {
            return demoIndividualActiva;
        }

        public LocalDate getFechaInicioDemoIndividual() {
            return fechaInicioDemoIndividual;
        }

        public Integer getDiasDemoIndividual() {
            return diasDemoIndividual;
        }

        public LocalDate getDemoFechaFinalizacion() {
            return demoFechaFinalizacion;
        }

        public Long getDemoDiasRestantes() {
            return demoDiasRestantes;
        }

        public Boolean getDemoExpirada() {
            return demoExpirada;
        }

        public String getDemoEstado() {
            return demoEstado;
        }

        public String getDemoOrigen() {
            return demoOrigen;
        }

    }

}
