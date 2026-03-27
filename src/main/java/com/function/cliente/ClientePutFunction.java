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
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

// las secuencias \" dentro del string permiten incluir comillas dobles literales
// en el texto sin cerrar el string ejemplo: "{\"error\":\"mensaje\"}"
// produce el JSON: {"error":"mensaje"}
// como se esta creandio el JSON manualmente como String
//  el JSON requiere comillas dobles alrededor de las claves y valores

public class ClientePutFunction {
  
@FunctionName("UpdateCliente")
public HttpResponseMessage updateCliente(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.PUT},
            route = "clientes/{id:int}",
            authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
        @BindingName("id") int id,
        final ExecutionContext context) {

    // informo que voy a actualizar reg por su id
    context.getLogger().info("Actualizando con id: " + id);

    try {
        // obtengo el body de la petición
        String body = request.getBody().orElse("");

        // Aquí valido si el body viene vacío
        if (body.isBlank()) {
            String errorJson = "{\"error\":\"El body de la petición está vacío\"}";

            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body(errorJson)
                    .build();
        }

        // preparo Gson para convertir el JSON recibido a objetolibro
        Gson gson = new Gson();
        Cliente cliente = gson.fromJson(body, Cliente.class);

        // valido si no pude leer correctamente el libro enviado
        if (cliente == null) {
            String errorJson = "{\"error\":\"No se pudo leer el registro  enviado\"}";

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

        // aquí creo el repositorio para actualizar 
        ClienteRepository repository = new ClienteRepository();

        // actualizo el usuario usando el id recibido por la URL
        Cliente clienteActualizado = repository.actualizarCliente(id, cliente);

        // Aquí valido si no existía un usuario con ese id
        if (clienteActualizado == null) {
            String notFoundJson = "{\"error\":\"Libro no encontrado\",\"id\":" + id + "}";

            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json")
                    .body(notFoundJson)
                    .build();
        }


        // Aquí convierto el libro actualizado a JSON
        String json = gson.toJson(clienteActualizado);

        // Aquí devuelvo una respuesta correcta con ellibro actualizado
        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(json)
                .build();

    } catch (JsonSyntaxException e) {
        // Aquí controlo el caso en que el JSON venga mal escrito
        context.getLogger().severe("JSON inválido al actualizar libro: " + e.getMessage());

        String errorJson = "{\"error\":\"El JSON enviado no es válido\"}";

        return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body(errorJson)
                .build();

    } catch (SQLException e) {
        // Aquí controlo errores de base de datos como libro duplicado
        context.getLogger().severe("Error SQL al actualizar : " + e.getMessage());

        String errorJson = "{\"error\":\"Error al actualizar\",\"detalle\":\""
                + e.getMessage().replace("\"", "'") + "\"}";

        return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(errorJson)
                .build();

    } catch (Exception e) {
        // Aquí controlo cualquier otro error general
        context.getLogger().severe("Error general al actualizar lirbo: " + e.getMessage());

        String errorJson = "{\"error\":\"Error interno al actualizar\",\"detalle\":\""
                + e.getMessage().replace("\"", "'") + "\"}";

        return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(errorJson)
                .build();
    }
}

}
