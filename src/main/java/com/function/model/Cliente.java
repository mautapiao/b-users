package com.function.model;
public class Cliente {
    private int id;
    private int rut;
    private String dv;
    private String nombre;

    public Cliente() {
    }

    public Cliente(int id, int rut, String dv, String nombre) {
        this.id = id;
        this.rut = rut;
        this.dv = dv;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRut() {  // ❌ era String, debe ser int para coincidir con el atributo
        return rut;
    }

    public void setRut(int rut) {  // ❌ era String, debe ser int
        this.rut = rut;
    }

    public String getDv() {
        return dv;
    }

    public void setDv(String dv) {
        this.dv = dv;
    }

    public String getNombre() {  
        return nombre;
    }

    public void setNombre(String nombre) { 
        this.nombre = nombre;
    }
}