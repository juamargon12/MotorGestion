package com.example.restservice;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Mantenimiento {
    private long num;
    private String texto;
    private boolean realizada;
    private String tipoVehiculo;  // "COCHE", "MOTO" o "FURGONETA"
    private Long vehiculoNum;     // ID del vehículo asignado (nullable)

    public Mantenimiento() {}

    public Mantenimiento(long num, String texto, boolean realizada) {
        this.num = num;
        this.texto = texto;
        this.realizada = realizada;
    }

    @JsonProperty
    public long getNum() { return num; }
    @JsonProperty
    public void setNum(long num) { this.num = num; }

    @JsonProperty
    public String getTexto() { return texto; }
    @JsonProperty
    public void setTexto(String texto) { this.texto = texto; }

    @JsonProperty
    public boolean isRealizada() { return realizada; }
    @JsonProperty
    public void setRealizada(boolean realizada) { this.realizada = realizada; }

    @JsonProperty
    public String getTipoVehiculo() { return tipoVehiculo; }
    @JsonProperty
    public void setTipoVehiculo(String tipoVehiculo) { this.tipoVehiculo = tipoVehiculo; }

    @JsonProperty
    public Long getVehiculoNum() { return vehiculoNum; }
    @JsonProperty
    public void setVehiculoNum(Long vehiculoNum) { this.vehiculoNum = vehiculoNum; }
}
