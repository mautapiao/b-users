package com.function;

import java.sql.SQLException;
import java.util.Optional;

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

public class UsuarioPutFunction {
  
@FunctionName("UpdateUsuario")
public HttpResponseMessage updateUsuario(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.PUT},
            route = "usuarios/{id:int}",
            authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
        @BindingName("id") int id,
        final ExecutionContext context) {

    // Aquí informo que voy a actualizar un usuario por su id
    context.getLogger().info("Actualizando usuario con id: " + id);

    try {
        // Aquí obtengo el body de la petición
        String body = request.getBody().orElse("");

        // Aquí valido si el body viene vacío
        if (body.isBlank()) {
            String errorJson = "{\"error\":\"El body de la petición está vacío\"}";

            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body(errorJson)
                    .build();
        }

        // Aquí preparo Gson para convertir el JSON recibido a objeto Usuario
        Gson gson = new Gson();
        Usuario usuario = gson.fromJson(body, Usuario.class);

        // Aquí valido si no pude leer correctamente el usuario enviado
        if (usuario == null) {
            String errorJson = "{\"error\":\"No se pudo leer el usuario enviado\"}";

            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body(errorJson)
                    .build();
        }

        // Aquí valido que el nombre venga informado
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            String errorJson = "{\"error\":\"El nombre es obligatorio\"}";

            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body(errorJson)
                    .build();
        }

        // Aquí valido que el email venga informado
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            String errorJson = "{\"error\":\"El email es obligatorio\"}";

            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body(errorJson)
                    .build();
        }

        // Aquí creo el repositorio para actualizar el usuario
        UsuarioRepository repository = new UsuarioRepository();

        // Aquí actualizo el usuario usando el id recibido por la URL
        Usuario usuarioActualizado = repository.actualizarUsuario(id, usuario);

        // Aquí valido si no existía un usuario con ese id
        if (usuarioActualizado == null) {
            String notFoundJson = "{\"error\":\"Usuario no encontrado\",\"id\":" + id + "}";

            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json")
                    .body(notFoundJson)
                    .build();
        }

        // Aquí convierto el usuario actualizado a JSON
        String json = gson.toJson(usuarioActualizado);

        // Aquí devuelvo una respuesta correcta con el usuario actualizado
        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(json)
                .build();

    } catch (JsonSyntaxException e) {
        // Aquí controlo el caso en que el JSON venga mal escrito
        context.getLogger().severe("JSON inválido al actualizar usuario: " + e.getMessage());

        String errorJson = "{\"error\":\"El JSON enviado no es válido\"}";

        return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body(errorJson)
                .build();

    } catch (SQLException e) {
        // Aquí controlo errores de base de datos como email duplicado
        context.getLogger().severe("Error SQL al actualizar usuario: " + e.getMessage());

        String errorJson = "{\"error\":\"Error al actualizar el usuario\",\"detalle\":\""
                + e.getMessage().replace("\"", "'") + "\"}";

        return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(errorJson)
                .build();

    } catch (Exception e) {
        // Aquí controlo cualquier otro error general
        context.getLogger().severe("Error general al actualizar usuario: " + e.getMessage());

        String errorJson = "{\"error\":\"Error interno al actualizar usuario\",\"detalle\":\""
                + e.getMessage().replace("\"", "'") + "\"}";

        return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(errorJson)
                .build();
    }
}

}
