package com.example.motorgestion.model;

import com.google.gson.annotations.SerializedName;

public class Moto {
    private long num;
    private String modelo;

    @SerializedName("n_bastidor")
    private String nBastidor;

    private String matricula;

    @SerializedName("anio_fabricacion")
    private String anioFabricacion;

    private String zona;
    private String foto;

    public Moto() {}

    public long getNum() { return num; }
    public void setNum(long num) { this.num = num; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getNBastidor() { return nBastidor; }
    public void setNBastidor(String nBastidor) { this.nBastidor = nBastidor; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getAnioFabricacion() { return anioFabricacion; }
    public void setAnioFabricacion(String anioFabricacion) { this.anioFabricacion = anioFabricacion; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }
}
