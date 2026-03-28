package com.function.usuario;

import java.util.Optional;

import com.function.repository.UsuarioRepository;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

public class UsuarioDeleteFunction {

    @FunctionName("DeleteUsuario")
    public HttpResponseMessage deleteUsuario(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.DELETE }, route = "usuarios/{id:int}", authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") int id,
            final ExecutionContext context) {

        // Aquí informo que voy a eliminar un usuario por su id
        context.getLogger().info("Eliminando usuario con id: " + id);

        try {
            // Aquí creo el repositorio para eliminar el usuario
            UsuarioRepository repository = new UsuarioRepository();

            // Aquí intento eliminar el usuario usando el id de la URL
            boolean eliminado = repository.eliminarUsuario(id);

            // Aquí valido si no existía un usuario con ese id
            if (!eliminado) {
                String notFoundJson = "{\"error\":\"Usuario no encontrado\",\"id\":" + id + "}";

                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(notFoundJson)
                        .build();
            }

            // Aquí preparo un JSON confirmando la eliminación
            String okJson = "{\"mensaje\":\"Usuario eliminado correctamente\",\"id\":" + id + "}";

            // Aquí devuelvo una respuesta exitosa indicando que el usuario fue eliminado
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(okJson)
                    .build();

        } catch (Exception e) {
            // Aquí controlo cualquier error al eliminar el usuario
            context.getLogger().severe("Error al eliminar usuario: " + e.getMessage());

            String errorJson = "{\"error\":\"Error interno al eliminar usuario\",\"detalle\":\""
                    + e.getMessage().replace("\"", "'") + "\"}";

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body(errorJson)
                    .build();
        }
    }
}
