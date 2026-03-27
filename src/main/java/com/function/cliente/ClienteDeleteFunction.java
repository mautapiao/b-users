package com.function.cliente;

import java.util.Optional;

import com.function.repository.ClienteRepository;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

public class ClienteDeleteFunction {
  
@FunctionName("DeleteCliente")
public HttpResponseMessage deleteCliente(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.DELETE},
            route = "clientes/{id:int}",
            authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
        @BindingName("id") int id,
        final ExecutionContext context) {

    // informo que voy a eliminar por su id
    context.getLogger().info("Eliminando registro con id: " + id);

    try {
        //  creo el repositorio para eliminar el registro
        ClienteRepository repository = new ClienteRepository();

        // aquí intento eliminar usando el id de la URL
        boolean eliminado = repository.eliminarCliente(id);

        // aquí valido si no existía con ese id
        if (!eliminado) {
            String notFoundJson = "{\"error\":\"Cliente no encontrado\",\"id\":" + id + "}";

            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json")
                    .body(notFoundJson)
                    .build();
        }

        // preparo un JSON confirmando la eliminación
        String okJson = "{\"mensaje\":\"Registro eliminado correctamente\",\"id\":" + id + "}";

        // devuelvo una respuesta exitosa indicando que fue eliminado
        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(okJson)
                .build();

    } catch (Exception e) {
        // controlo cualquier error al eliminar 
        context.getLogger().severe("Error al eliminar registro: " + e.getMessage());

        String errorJson = "{\"error\":\"Error interno al eliminar registro\",\"detalle\":\""
                + e.getMessage().replace("\"", "'") + "\"}";

        return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(errorJson)
                .build();
    }
}
}
