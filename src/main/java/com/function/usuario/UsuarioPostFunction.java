package com.function.usuario;

import java.sql.SQLException;
import java.util.Optional;

import com.function.model.Usuario;
import com.function.repository.UsuarioRepository;
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

public class UsuarioPostFunction {

    @FunctionName("CreateUsuario")
    public HttpResponseMessage createUsuario(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.POST }, route = "usuarios", authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        // informo que voy a crear un nuevo usuario
        context.getLogger().info("Creando un nuevo usuario");

        try {
            // obtengo el body de la petición
            String body = request.getBody().orElse("");

            // valido si el body viene vacío
            if (body.isBlank()) {
                // preparo un JSON indicando que faltan datos
                String errorJson = "{\"error\":\"El body de la petición está vacío\"}";

                // devuelvo una respuesta 400 porque faltó enviar información
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(errorJson)
                        .build();
            }

            // preparo Gson para convertir el JSON recibido a un objeto Usuario
            Gson gson = new Gson();
            Usuario usuario = gson.fromJson(body, Usuario.class);

            // valido si el objeto no se pudo construir correctamente
            if (usuario == null) {
                String errorJson = "{\"error\":\"No se pudo leer el usuario enviado\"}";

                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(errorJson)
                        .build();
            }

            // valido que el nombre venga informado
            if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
                String errorJson = "{\"error\":\"El nombre es obligatorio\"}";

                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(errorJson)
                        .build();
            }

            // valido que el email venga informado
            if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
                String errorJson = "{\"error\":\"El email es obligatorio\"}";

                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(errorJson)
                        .build();
            }

            // creo el repositorio para guardar el nuevo usuario
            UsuarioRepository repository = new UsuarioRepository();

            // inserto el usuario en la base de datos
            Usuario usuarioCreado = repository.insertarUsuario(usuario);

            // convierto el usuario creado a JSON
            String json = gson.toJson(usuarioCreado);

            // Aquí devuelvo una respuesta 201 porque el usuario fue creado correctamente
            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(json)
                    .build();

        } catch (JsonSyntaxException e) {
            // controlo el caso en que el JSON esté mal escrito
            context.getLogger().severe("JSON inválido: " + e.getMessage());

            String errorJson = "{\"error\":\"El JSON enviado no es válido\"}";

            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body(errorJson)
                    .build();

        } catch (SQLException e) {
            // controlo errores de base de datos como email duplicado
            context.getLogger().severe("Error SQL al crear usuario: " + e.getMessage());

            String errorJson = "{\"error\":\"Error al guardar el usuario\",\"detalle\":\""
                    + e.getMessage().replace("\"", "'") + "\"}";

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body(errorJson)
                    .build();

        } catch (Exception e) {
            // controlo cualquier otro error no esperado
            context.getLogger().severe("Error general al crear usuario: " + e.getMessage());

            String errorJson = "{\"error\":\"Error interno al crear usuario\",\"detalle\":\""
                    + e.getMessage().replace("\"", "'") + "\"}";

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body(errorJson)
                    .build();
        }
    }
}
