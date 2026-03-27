package com.function.libro;

import java.util.Optional;

import com.function.repository.LibroRepository;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

public class LibroDeleteFunction {
  
@FunctionName("DeleteLibro")
public HttpResponseMessage deleteUsuario(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.DELETE},
            route = "libros/{id:int}",
            authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
        @BindingName("id") int id,
        final ExecutionContext context) {

    // Aquí informo que voy a eliminar un libro por su id
    context.getLogger().info("Eliminando libro  con id: " + id);

    try {
        // aquí creo el repositorio para eliminar el registro
        LibroRepository repository = new LibroRepository();

        // aquí intento eliminar el libro usando el id de la URL
        boolean eliminado = repository.eliminarLibro(id);

        // aquí valido si no existía un lirbo con ese id
        if (!eliminado) {
            String notFoundJson = "{\"error\":\"Libro no encontrado\",\"id\":" + id + "}";

            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json")
                    .body(notFoundJson)
                    .build();
        }

        // aquí preparo un JSON confirmando la eliminación
        String okJson = "{\"mensaje\":\"Libro eliminado correctamente\",\"id\":" + id + "}";

        // aquí devuelvo una respuesta exitosa indicando que el libro fue eliminado
        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(okJson)
                .build();

    } catch (Exception e) {
        // aquí controlo cualquier error al eliminar 
        context.getLogger().severe("Error al eliminar libro: " + e.getMessage());

        String errorJson = "{\"error\":\"Error interno al eliminar libro\",\"detalle\":\""
                + e.getMessage().replace("\"", "'") + "\"}";

        return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(errorJson)
                .build();
    }
}
}
