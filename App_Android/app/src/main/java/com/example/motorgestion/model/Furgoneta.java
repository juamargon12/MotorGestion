package com.example.motorgestion.model;

import com.google.gson.annotations.SerializedName;

public class Furgoneta {
    private long num;
    private String modelo;
    private String matricula;
    private String combustible;

    @SerializedName("carga_maxima")
    private double cargaMaxima;

    private String zona;
    private String foto;

    public Furgoneta() {}

    public long getNum() { return num; }
    public void setNum(long num) { this.num = num; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getCombustible() { return combustible; }
    public void setCombustible(String combustible) { this.combustible = combustible; }

    public double getCargaMaxima() { return cargaMaxima; }
    public void setCargaMaxima(double cargaMaxima) { this.cargaMaxima = cargaMaxima; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }
}
