package com.function.model;

public class Prestamo {
  private int id;
  private String fechaPrestamo;
  private String fechaEntrega;
  private int usuarioId;
  private int libroId;
  private int clienteId;

  // Getters y Setters
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getFechaPrestamo() {
    return fechaPrestamo;
  }

  public void setFechaPrestamo(String fechaPrestamo) {
    this.fechaPrestamo = fechaPrestamo;
  }

  public String getFechaEntrega() {
    return fechaEntrega;
  }

  public void setFechaEntrega(String fechaEntrega) {
    this.fechaEntrega = fechaEntrega;
  }

  public int getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(int usuarioId) {
    this.usuarioId = usuarioId;
  }

  public int getLibroId() {
    return libroId;
  }

  public void setLibroId(int libroId) {
    this.libroId = libroId;
  }

  public int getClienteId() {
    return clienteId;
  }

  public void setClienteId(int clienteId) {
    this.clienteId = clienteId;
  }
}