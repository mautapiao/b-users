package com.function.cliente;

import java.sql.SQLException;
import java.util.Optional;

import com.function.model.Cliente;
import com.function.repository.ClienteRepository;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

public class ClientePostFunction {
  
@FunctionName("CreateCliente")
    public HttpResponseMessage createUsuario(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                route = "clientes",
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        // informo que voy a crear un nuevo usuario
        context.getLogger().info("Creando un nuevo cliente");

        try {
            // obtengo el body de la petición
            String body = request.getBody().orElse("");

            // valido si el body viene vacío
            if (body.isBlank()) {
                //  preparo un JSON indicando que faltan datos
                String errorJson = "{\"error\":\"El body de la petición está vacío\"}";

                // devuelvo una respuesta 400 porque faltó enviar información
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(errorJson)
                        .build();
            }

            // preparo Gson para convertir el JSON recibido a un objeto Usuario
            Gson gson = new Gson();

            
            Cliente cliente = gson.fromJson(body, Cliente.class);

            //  valido si el objeto no se pudo construir correctamente
            if (cliente == null) {
                String errorJson = "{\"error\":\"No se pudo leer el registro enviado\"}";

                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(errorJson)
                        .build();
            }

            // Valido que el rut venga informado
if (cliente.getRut() == 0) {
    String errorJson = "{\"error\":\"El rut es obligatorio\"}";
    return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
            .header("Content-Type", "application/json")
            .body(errorJson)
            .build();
}

// Valido que el dv venga informado
if (cliente.getDv() == null || cliente.getDv().isBlank()) {
    String errorJson = "{\"error\":\"El dv es obligatorio\"}";
    return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
            .header("Content-Type", "application/json")
            .body(errorJson)
            .build();
}

// Valido que el nombre venga informado
if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
    String errorJson = "{\"error\":\"El nombre es obligatorio\"}";
    return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
            .header("Content-Type", "application/json")
            .body(errorJson)
            .build();
}

            //  creo el repositorio para guardar el nuevo usuario
            ClienteRepository repository = new ClienteRepository();

            // Aquí inserto el usuario en la base de datos
            Cliente clienteCreado = repository.insertarCliente(cliente);

            // Aquí convierto el usuario creado a JSON
            String json = gson.toJson(clienteCreado);

            // devuelvo una respuesta 201 porque el regiostro fue creado correctamente
            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(json)
                    .build();

        } catch (JsonSyntaxException e) {
            //  controlo el caso en que el JSON esté mal escrito
            context.getLogger().severe("JSON inválido: " + e.getMessage());

            String errorJson = "{\"error\":\"El JSON enviado no es válido\"}";

            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body(errorJson)
                    .build();

        } catch (SQLException e) {
            // Aquí controlo errores de base de datos como email duplicado
            context.getLogger().severe("Error SQL al crear regsirto: " + e.getMessage());

            String errorJson = "{\"error\":\"Error al guardar el registro\",\"detalle\":\""
                    + e.getMessage().replace("\"", "'") + "\"}";

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body(errorJson)
                    .build();

        } catch (Exception e) {
            // Aquí controlo cualquier otro error no esperado
            context.getLogger().severe("Error general al crear registro: " + e.getMessage());

            String errorJson = "{\"error\":\"Error interno al crear registro\",\"detalle\":\""
                    + e.getMessage().replace("\"", "'") + "\"}";

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body(errorJson)
                    .build();
        }
    }
}
