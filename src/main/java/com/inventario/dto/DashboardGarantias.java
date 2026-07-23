package com.inventario.dto;

public class DashboardGarantias {

    private long totalGarantias;
    private long abiertas;
    private long cerradas;
    private long ingresadasMesActual;
    private long abiertasSinCasoProveedor;
    private long abiertasMas10Dias;
    private long enTramite;
    private long enRevisionInterna;
    private long enviadoAProveedor;
    private long reparado;
    private long noAplicoGarantia;
    private long cambioEquipoNuevo;
    private long notaCredito;
    private long maxDiasAbierta;
    private String estadoTiempoAbiertas;
    private String textoTiempoAbiertas;

    public long getTotalGarantias() {
        return totalGarantias;
    }

    public void setTotalGarantias(long totalGarantias) {
        this.totalGarantias = totalGarantias;
    }

    public long getAbiertas() {
        return abiertas;
    }

    public void setAbiertas(long abiertas) {
        this.abiertas = abiertas;
    }

    public long getCerradas() {
        return cerradas;
    }

    public void setCerradas(long cerradas) {
        this.cerradas = cerradas;
    }

    public long getIngresadasMesActual() {
        return ingresadasMesActual;
    }

    public void setIngresadasMesActual(long ingresadasMesActual) {
        this.ingresadasMesActual = ingresadasMesActual;
    }

    public long getAbiertasSinCasoProveedor() {
        return abiertasSinCasoProveedor;
    }

    public void setAbiertasSinCasoProveedor(long abiertasSinCasoProveedor) {
        this.abiertasSinCasoProveedor = abiertasSinCasoProveedor;
    }

    public long getAbiertasMas10Dias() {
        return abiertasMas10Dias;
    }

    public void setAbiertasMas10Dias(long abiertasMas10Dias) {
        this.abiertasMas10Dias = abiertasMas10Dias;
    }

    public long getEnTramite() {
        return enTramite;
    }

    public void setEnTramite(long enTramite) {
        this.enTramite = enTramite;
    }

    public long getEnRevisionInterna() {
        return enRevisionInterna;
    }

    public void setEnRevisionInterna(long enRevisionInterna) {
        this.enRevisionInterna = enRevisionInterna;
    }

    public long getEnviadoAProveedor() {
        return enviadoAProveedor;
    }

    public void setEnviadoAProveedor(long enviadoAProveedor) {
        this.enviadoAProveedor = enviadoAProveedor;
    }

    public long getReparado() {
        return reparado;
    }

    public void setReparado(long reparado) {
        this.reparado = reparado;
    }

    public long getNoAplicoGarantia() {
        return noAplicoGarantia;
    }

    public void setNoAplicoGarantia(long noAplicoGarantia) {
        this.noAplicoGarantia = noAplicoGarantia;
    }

    public long getCambioEquipoNuevo() {
        return cambioEquipoNuevo;
    }

    public void setCambioEquipoNuevo(long cambioEquipoNuevo) {
        this.cambioEquipoNuevo = cambioEquipoNuevo;
    }

    public long getNotaCredito() {
        return notaCredito;
    }

    public void setNotaCredito(long notaCredito) {
        this.notaCredito = notaCredito;
    }

    public long getMaxDiasAbierta() {
        return maxDiasAbierta;
    }

    public void setMaxDiasAbierta(long maxDiasAbierta) {
        this.maxDiasAbierta = maxDiasAbierta;
    }

    public String getEstadoTiempoAbiertas() {
        return estadoTiempoAbiertas;
    }

    public void setEstadoTiempoAbiertas(String estadoTiempoAbiertas) {
        this.estadoTiempoAbiertas = estadoTiempoAbiertas;
    }

    public String getTextoTiempoAbiertas() {
        return textoTiempoAbiertas;
    }

    public void setTextoTiempoAbiertas(String textoTiempoAbiertas) {
        this.textoTiempoAbiertas = textoTiempoAbiertas;
    }
}
