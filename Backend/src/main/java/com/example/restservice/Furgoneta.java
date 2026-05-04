package com.example.restservice;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Furgoneta {
    private long num;
    private String modelo;
    private String matricula;
    private String combustible; // Antiguo sexo
    private double cargaMaxima; // Antiguo peso
    private String zona;
    private String foto;


    public Furgoneta() {}

    public Furgoneta(long num, String modelo, String matricula, 
                String combustible, double cargaMaxima, String zona, 
                String foto) {
        this.num = num;
        this.modelo = modelo;
        this.matricula = matricula;
        this.combustible = combustible;
        this.cargaMaxima = cargaMaxima;
        this.zona = zona;
        this.foto = foto;
    }

    @JsonProperty
    public long getNum() { return num; }
    @JsonProperty
    public void setNum(long num) { this.num = num; }

    @JsonProperty
    public String getModelo() { return modelo; }
    @JsonProperty
    public void setModelo(String modelo) { this.modelo = modelo; }

    @JsonProperty("matricula")
    public String getMatricula() { return matricula; }
    @JsonProperty("matricula")
    public void setMatricula(String matricula) { this.matricula = matricula; }

    @JsonProperty
    public String getCombustible() { return combustible; }
    @JsonProperty
    public void setCombustible(String combustible) { 
        if(combustible.equalsIgnoreCase("Diesel") || 
           combustible.equalsIgnoreCase("Gasolina") || 
           combustible.equalsIgnoreCase("Electrico") || 
           combustible.equalsIgnoreCase("Hibrido")) {

            // Estandariza la primera letra en mayuscula como tenías originalmente
            this.combustible = combustible.substring(0, 1).toUpperCase() + combustible.substring(1).toLowerCase();
        }
    }

    @JsonProperty("carga_maxima")
    public double getCargaMaxima() { return cargaMaxima; }
    @JsonProperty("carga_maxima")
    public void setCargaMaxima(double cargaMaxima) { this.cargaMaxima = cargaMaxima; }

    @JsonProperty
    public String getZona() { return zona; }
    @JsonProperty
    public void setZona(String zona) { this.zona = zona; }

    @JsonProperty
    public String getFoto() { return foto; }
    @JsonProperty
    public void setFoto(String foto) { this.foto = foto; }
    
}
