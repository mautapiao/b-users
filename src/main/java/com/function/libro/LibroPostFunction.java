package com.function.libro;

import java.sql.SQLException;
import java.util.Optional;

import com.function.model.Libro;
import com.function.repository.LibroRepository;
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

public class LibroPostFunction {
  
@FunctionName("CreateLibro")
    public HttpResponseMessage createUsuario(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                route = "libros",
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        // Aquí informo que voy a crear un nuevo usuario
        context.getLogger().info("Creando un nuevo libro");

        try {
            // Aquí obtengo el body de la petición
            String body = request.getBody().orElse("");

            // Aquí valido si el body viene vacío
            if (body.isBlank()) {
                // Aquí preparo un JSON indicando que faltan datos
                String errorJson = "{\"error\":\"El body de la petición está vacío\"}";

                // Aquí devuelvo una respuesta 400 porque faltó enviar información
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(errorJson)
                        .build();
            }

            // Aquí preparo Gson para convertir el JSON recibido a un objeto Usuario
            Gson gson = new Gson();
            Libro libro = gson.fromJson(body, Libro.class);

            // Aquí valido si el objeto no se pudo construir correctamente
            if (libro == null) {
                String errorJson = "{\"error\":\"No se pudo leer el libro enviado\"}";

                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(errorJson)
                        .build();
            }

            // Aquí valido que el nombre venga informado
            if (libro.getTitulo() == null || libro.getTitulo().isBlank()) {
                String errorJson = "{\"error\":\"El titulo es obligatorio\"}";

                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(errorJson)
                        .build();
            }

            // Aquí valido que el email venga informado
            if (libro.getAutor() == null || libro.getAutor().isBlank()) {
                String errorJson = "{\"error\":\"El autor es obligatorio\"}";

                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(errorJson)
                        .build();
            }

            // Aquí creo el repositorio para guardar el nuevo usuario
            LibroRepository repository = new LibroRepository();

            // Aquí inserto el usuario en la base de datos
            Libro libroCreado = repository.insertarLibro(libro);

            // Aquí convierto el usuario creado a JSON
            String json = gson.toJson(libroCreado);

            // Aquí devuelvo una respuesta 201 porque el usuario fue creado correctamente
            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(json)
                    .build();

        } catch (JsonSyntaxException e) {
            // Aquí controlo el caso en que el JSON esté mal escrito
            context.getLogger().severe("JSON inválido: " + e.getMessage());

            String errorJson = "{\"error\":\"El JSON enviado no es válido\"}";

            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body(errorJson)
                    .build();

        } catch (SQLException e) {
            // Aquí controlo errores de base de datos como email duplicado
            context.getLogger().severe("Error SQL al crear libro: " + e.getMessage());

            String errorJson = "{\"error\":\"Error al guardar el libro\",\"detalle\":\""
                    + e.getMessage().replace("\"", "'") + "\"}";

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body(errorJson)
                    .build();

        } catch (Exception e) {
            // Aquí controlo cualquier otro error no esperado
            context.getLogger().severe("Error general al crear libro: " + e.getMessage());

            String errorJson = "{\"error\":\"Error interno al crear libro\",\"detalle\":\""
                    + e.getMessage().replace("\"", "'") + "\"}";

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body(errorJson)
                    .build();
        }
    }
}
