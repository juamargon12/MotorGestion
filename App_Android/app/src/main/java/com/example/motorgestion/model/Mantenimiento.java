package com.example.motorgestion.model;

public class Mantenimiento {
    private long num;
    private String texto;
    private boolean realizada;

    public Mantenimiento() {}

    public long getNum() { return num; }
    public void setNum(long num) { this.num = num; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public boolean isRealizada() { return realizada; }
    public void setRealizada(boolean realizada) { this.realizada = realizada; }
}
