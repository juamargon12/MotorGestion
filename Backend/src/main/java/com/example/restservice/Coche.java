package com.example.restservice;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Coche {
    private long num;
    private String modelo;
    private String nBastidor;
    private String matricula;
    private String anioFabricacion;
    private String zona;
    private String foto;

    public Coche() {}

    public Coche(long num, String modelo, String nBastidor, 
               String matricula, String anioFabricacion, 
               String zona, String foto) {
        this.num = num;
        this.modelo = modelo;
        this.nBastidor = nBastidor;
        this.matricula = matricula;
        this.anioFabricacion = anioFabricacion;
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

    @JsonProperty("n_bastidor")
    public String getNBastidor() { return nBastidor; }
    @JsonProperty("n_bastidor")
    public void setNBastidor(String nBastidor) { this.nBastidor = nBastidor; }

    @JsonProperty("matricula")
    public String getMatricula() { return matricula; }
    @JsonProperty("matricula")
    public void setMatricula(String matricula) { this.matricula = matricula; }

    @JsonProperty("anio_fabricacion")
    public String getAnioFabricacion() { return anioFabricacion; }
    @JsonProperty("anio_fabricacion")
    public void setAnioFabricacion(String anioFabricacion) { this.anioFabricacion = anioFabricacion; }

    @JsonProperty
    public String getZona() { return zona; }
    @JsonProperty
    public void setZona(String zona) { this.zona = zona; }

    @JsonProperty
    public String getFoto() { return foto; }
    @JsonProperty
    public void setFoto(String foto) { this.foto = foto; }
}
