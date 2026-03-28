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
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

// las secuencias \" dentro del string permiten incluir comillas dobles literales
// en el texto sin cerrar el string ejemplo: "{\"error\":\"mensaje\"}"
// produce el JSON: {"error":"mensaje"}
// como se esta creandio el JSON manualmente como String
//  el JSON requiere comillas dobles alrededor de las claves y valores

public class LibroPutFunction {

        @FunctionName("UpdateLibro")
        public HttpResponseMessage updateUsuario(
                        @HttpTrigger(name = "req", methods = {
                                        HttpMethod.PUT }, route = "libros/{id:int}", authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
                        @BindingName("id") int id,
                        final ExecutionContext context) {

                // Aquí informo que voy a actualizar un usuario por su id
                context.getLogger().info("Actualizando libro con id: " + id);

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

                        // Aquí preparo Gson para convertir el JSON recibido a objetolibro
                        Gson gson = new Gson();
                        Libro libro = gson.fromJson(body, Libro.class);

                        // Aquí valido si no pude leer correctamente el libro enviado
                        if (libro == null) {
                                String errorJson = "{\"error\":\"No se pudo leer el libro  enviado\"}";

                                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                                                .header("Content-Type", "application/json")
                                                .body(errorJson)
                                                .build();
                        }

                        // Aquí valido que el titulo venga informado
                        if (libro.getTitulo() == null || libro.getTitulo().isBlank()) {
                                String errorJson = "{\"error\":\"El titulo es obligatorio\"}";

                                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                                                .header("Content-Type", "application/json")
                                                .body(errorJson)
                                                .build();
                        }

                        // Aquí valido que el autorvenga informado
                        if (libro.getAutor() == null || libro.getAutor().isBlank()) {
                                String errorJson = "{\"error\":\"El autor es obligatorio\"}";

                                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                                                .header("Content-Type", "application/json")
                                                .body(errorJson)
                                                .build();
                        }

                        // Aquí creo el repositorio para actualizar libro
                        LibroRepository repository = new LibroRepository();

                        // Aquí actualizo el usuario usando el id recibido por la URL
                        Libro libroActualizado = repository.actualizarLibro(id, libro);

                        // Aquí valido si no existía un usuario con ese id
                        if (libroActualizado == null) {
                                String notFoundJson = "{\"error\":\"Libro no encontrado\",\"id\":" + id + "}";

                                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                                                .header("Content-Type", "application/json")
                                                .body(notFoundJson)
                                                .build();
                        }

                        // Aquí convierto el libro actualizado a JSON
                        String json = gson.toJson(libroActualizado);

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
                        context.getLogger().severe("Error SQL al actualizar libro: " + e.getMessage());

                        String errorJson = "{\"error\":\"Error al actualizar el libro\",\"detalle\":\""
                                        + e.getMessage().replace("\"", "'") + "\"}";

                        return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .header("Content-Type", "application/json")
                                        .body(errorJson)
                                        .build();

                } catch (Exception e) {
                        // Aquí controlo cualquier otro error general
                        context.getLogger().severe("Error general al actualizar lirbo: " + e.getMessage());

                        String errorJson = "{\"error\":\"Error interno al actualizar libro\",\"detalle\":\""
                                        + e.getMessage().replace("\"", "'") + "\"}";

                        return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .header("Content-Type", "application/json")
                                        .body(errorJson)
                                        .build();
                }
        }

}
