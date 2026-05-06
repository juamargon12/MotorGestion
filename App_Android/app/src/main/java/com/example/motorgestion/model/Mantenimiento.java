package com.example.motorgestion.model;

public class Mantenimiento {
    private long num;
    private String texto;
    private boolean realizada;
    private String tipoVehiculo;  // "COCHE", "MOTO" o "FURGONETA"
    private Long vehiculoNum;     // ID del vehículo asignado (puede ser null)

    public Mantenimiento() {}

    public long getNum() { return num; }
    public void setNum(long num) { this.num = num; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public boolean isRealizada() { return realizada; }
    public void setRealizada(boolean realizada) { this.realizada = realizada; }

    public String getTipoVehiculo() { return tipoVehiculo; }
    public void setTipoVehiculo(String tipoVehiculo) { this.tipoVehiculo = tipoVehiculo; }

    public Long getVehiculoNum() { return vehiculoNum; }
    public void setVehiculoNum(Long vehiculoNum) { this.vehiculoNum = vehiculoNum; }
}
