package com.function.prestamo;

import java.util.List;
import java.util.Optional;

import com.function.model.Prestamo;
import com.function.model.PrestamoDetalle;
import com.function.repository.PrestamoRepository;
import com.google.gson.Gson;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

public class PrestamoFunction {

        // GET /prestamos todos los préstamos con detalle
        @FunctionName("GetPrestamos")
        public HttpResponseMessage getPrestamos(
                        @HttpTrigger(name = "req", methods = {
                                        HttpMethod.GET }, route = "prestamos", authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
                        final ExecutionContext context) {

                context.getLogger().info("Listando todos los préstamos...");
                try {
                        PrestamoRepository repository = new PrestamoRepository();
                        List<PrestamoDetalle> prestamos = repository.listarPrestamos();
                        String json = new Gson().toJson(prestamos);
                        return request.createResponseBuilder(HttpStatus.OK)
                                        .header("Content-Type", "application/json")
                                        .body(json).build();
                } catch (Exception e) {
                        context.getLogger().severe("Error al listar préstamos: " + e.getMessage());
                        String errorJson = "{\"error\":\"Error al listar préstamos\",\"detalle\":\""
                                        + e.getMessage().replace("\"", "'") + "\"}";
                        return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .header("Content-Type", "application/json")
                                        .body(errorJson).build();
                }
        }

        // GET /prestamos/pendientes solo los que tienen FECHA_ENTREGA null
        @FunctionName("GetPrestamosPendientes")
        public HttpResponseMessage getPrestamosPendientes(
                        @HttpTrigger(name = "req", methods = {
                                        HttpMethod.GET }, route = "prestamos/pendientes", authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
                        final ExecutionContext context) {

                context.getLogger().info("Listando préstamos pendientes...");
                try {
                        PrestamoRepository repository = new PrestamoRepository();
                        List<PrestamoDetalle> pendientes = repository.listarPrestamosPendientes();
                        String json = new Gson().toJson(pendientes);
                        return request.createResponseBuilder(HttpStatus.OK)
                                        .header("Content-Type", "application/json")
                                        .body(json).build();
                } catch (Exception e) {
                        context.getLogger().severe("Error al listar pendientes: " + e.getMessage());
                        String errorJson = "{\"error\":\"Error al listar pendientes\",\"detalle\":\""
                                        + e.getMessage().replace("\"", "'") + "\"}";
                        return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .header("Content-Type", "application/json")
                                        .body(errorJson).build();
                }
        }

        @FunctionName("InsertarPrestamo")
public HttpResponseMessage insertarPrestamo(
    @HttpTrigger(name = "req", methods = {
        HttpMethod.POST }, route = "prestamos", authLevel = AuthorizationLevel.ANONYMOUS)
    HttpRequestMessage<Optional<String>> request,
    final ExecutionContext context) {

    context.getLogger().info("Insertando nuevo préstamo...");

    try {
        Prestamo prestamo = new Gson().fromJson(request.getBody().orElse("{}"), Prestamo.class);

        // Valido que vengan los ids necesarios
        if (prestamo.getUsuarioId() == 0) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body("{\"error\":\"El usuarioId es obligatorio\"}").build();
        }
        if (prestamo.getLibroId() == 0) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body("{\"error\":\"El libroId es obligatorio\"}").build();
        }
        if (prestamo.getClienteId() == 0) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body("{\"error\":\"El clienteId es obligatorio\"}").build();
        }

        PrestamoRepository repository = new PrestamoRepository();
        Optional<Prestamo> resultado = repository.insertarPrestamo(prestamo);

        // Resultado de negocio válido — libro no disponible no es un error
        if (resultado.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body("{\"disponible\": false, \"mensaje\": \"Libro no disponible o no existe\"}")
                .build();
        }

        String json = new Gson().toJson(resultado.get());
        return request.createResponseBuilder(HttpStatus.CREATED)
            .header("Content-Type", "application/json")
            .body(json).build();

    } catch (Exception e) {
        // Solo errores reales del sistema llegan acá
        context.getLogger().severe("Error al insertar préstamo: " + e.getMessage());
        String errorJson = "{\"error\":\"Error al insertar préstamo\",\"detalle\":\""
            + e.getMessage().replace("\"", "'") + "\"}";
        return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
            .header("Content-Type", "application/json")
            .body(errorJson).build();
    }
}
        

        // POST /prestamos inserta un nuevo préstamo
        /* original
        @FunctionName("InsertarPrestamo")
        public HttpResponseMessage insertarPrestamo(
                        @HttpTrigger(name = "req", methods = {
                                        HttpMethod.POST }, route = "prestamos", authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
                        final ExecutionContext context) {

                context.getLogger().info("Insertando nuevo préstamo...");
                try {
                        Prestamo prestamo = new Gson().fromJson(request.getBody().orElse("{}"), Prestamo.class);

                        // Valido que vengan los ids necesarios
                        if (prestamo.getUsuarioId() == 0) {
                                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                                                .header("Content-Type", "application/json")
                                                .body("{\"error\":\"El usuarioId es obligatorio\"}").build();
                        }
                        if (prestamo.getLibroId() == 0) {
                                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                                                .header("Content-Type", "application/json")
                                                .body("{\"error\":\"El libroId es obligatorio\"}").build();
                        }
                        if (prestamo.getClienteId() == 0) {
                                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                                                .header("Content-Type", "application/json")
                                                .body("{\"error\":\"El clienteId es obligatorio\"}").build();
                        }

                        PrestamoRepository repository = new PrestamoRepository();
                        Prestamo nuevo = repository.insertarPrestamo(prestamo);
                        String json = new Gson().toJson(nuevo);
                        return request.createResponseBuilder(HttpStatus.CREATED)
                                        .header("Content-Type", "application/json")
                                        .body(json).build();
                } catch (IllegalStateException e) {
                        // Error de negocio — libro no disponible → HTTP 409 Conflict
                        context.getLogger().warning("Libro no disponible: " + e.getMessage());
                        String errorJson = "{\"error\":\"" + e.getMessage() + "\"}";
                        return request.createResponseBuilder(HttpStatus.CONFLICT)
                                        .header("Content-Type", "application/json")
                                        .body(errorJson).build();

                } catch (Exception e) {
                        context.getLogger().severe("Error al insertar préstamo: " + e.getMessage());
                        String errorJson = "{\"error\":\"Error al insertar préstamo\",\"detalle\":\""
                                        + e.getMessage().replace("\"", "'") + "\"}";
                        return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .header("Content-Type", "application/json")
                                        .body(errorJson).build();
                }
        }

        */
        // PATCH /prestamos/{id}/devolucion registra la fecha de entrega de hoy
        @FunctionName("RegistrarDevolucion")
        public HttpResponseMessage registrarDevolucion(
                        @HttpTrigger(name = "req", methods = {
                                        HttpMethod.PATCH }, route = "prestamos/{id:int}/devolucion", authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
                        @BindingName("id") int id,
                        final ExecutionContext context) {

                context.getLogger().info("Registrando devolución del préstamo id: " + id);
                try {
                        PrestamoRepository repository = new PrestamoRepository();
                        Prestamo actualizado = repository.registrarDevolucion(id);

                        if (actualizado == null) {
                                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                                                .header("Content-Type", "application/json")
                                                .body("{\"error\":\"Préstamo no encontrado o ya fue devuelto\",\"id\":"
                                                                + id + "}")
                                                .build();
                        }

                        String json = new Gson().toJson(actualizado);
                        return request.createResponseBuilder(HttpStatus.OK)
                                        .header("Content-Type", "application/json")
                                        .body(json).build();
                } catch (Exception e) {
                        context.getLogger().severe("Error al registrar devolución: " + e.getMessage());
                        String errorJson = "{\"error\":\"Error al registrar devolución\",\"detalle\":\""
                                        + e.getMessage().replace("\"", "'") + "\"}";
                        return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .header("Content-Type", "application/json")
                                        .body(errorJson).build();
                }
        }

        // GET /prestamos/libros/{id}/disponibilidad
        @FunctionName("LibroDisponible")
        public HttpResponseMessage libroDisponible(
                        @HttpTrigger(name = "req", methods = {
                                        HttpMethod.GET }, route = "prestamos/libros/{id:int}/disponibilidad", authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
                        @BindingName("id") int id,
                        final ExecutionContext context) {

                context.getLogger().info("Consultando disponibilidad del libro id: " + id);
                try {
                        PrestamoRepository repository = new PrestamoRepository();
                        Boolean disponible = repository.libroDisponible(id);

                        if (disponible == null) {
    return request.createResponseBuilder(HttpStatus.NOT_FOUND)
        .header("Content-Type", "application/json")
        .body("{\"error\":\"Libro no encontrado\"}")
        .build();
} 
                        String json = "{\"libroId\":" + id + ",\"disponible\":" + disponible + "}";

                        return request.createResponseBuilder(HttpStatus.OK)
                                        .header("Content-Type", "application/json")
                                        .body(json).build();

                } catch (Exception e) {
                        context.getLogger().severe("Error al consultar disponibilidad: " + e.getMessage());
                        String errorJson = "{\"error\":\"Error al consultar disponibilidad\",\"detalle\":\""
                                        + e.getMessage().replace("\"", "'") + "\"}";
                        return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .header("Content-Type", "application/json")
                                        .body(errorJson).build();
                }
        }
}