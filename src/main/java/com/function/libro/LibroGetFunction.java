package com.function.libro;

import java.util.List;
import java.util.Optional;

import com.function.model.Libro;
import com.function.repository.LibroRepository;
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

public class LibroGetFunction {
  @FunctionName("GetLibros")
  public HttpResponseMessage getLibros(
      @HttpTrigger(name = "req", methods = {
          HttpMethod.GET }, route = "libros", authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
      final ExecutionContext context) {

    // recibo una petición GET para listar todos los Libros
    context.getLogger().info("Obteniendo Libros desde Oracle...");

    try {
      // creo el repositorio para consultar la base de datos
      LibroRepository repository = new LibroRepository();

      // obtengo la lista completa de Libros desde oracle
      List<Libro> libros = repository.listarLibros();

      // preparo gson para convertir la lista de Libros a JSON
      Gson gson = new Gson();

      // convierto la lista de Libros a formato JSON
      String json = gson.toJson(libros);

      // devuelvo una respuesta 200 con la lista de Libros en JSON
      return request.createResponseBuilder(HttpStatus.OK)
          .header("Content-Type", "application/json")
          .body(json)
          .build();

    } catch (Exception e) {
      //  registro el error para revisar qué ocurrió
      context.getLogger().severe("Error al obtener Libros: " + e.getMessage());

      //  construyo un JSON con el detalle del error
      String errorJson = "{\"error\":\"Error al obtener Libros\",\"detalle\":\""
          + e.getMessage().replace("\"", "'") + "\"}";

      // devuelvo una respuesta 500 porque ocurrió un error interno
      return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
          .header("Content-Type", "application/json")
          .body(errorJson)
          .build();
    }
  }

  @FunctionName("GetLibroById")
  public HttpResponseMessage getLibroById(
      @HttpTrigger(name = "req", methods = {
          HttpMethod.GET }, route = "Libros/{id:int}", authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
      @BindingName("id") int id,
      final ExecutionContext context) {

    // recibo una petición GET y tomo el id que viene en la URL
    context.getLogger().info("Buscando Libro con id: " + id);

    try {
      // creo el repositorio para consultar la base de datos
      LibroRepository repository = new LibroRepository();

      //  busco un Libro por su id
      Libro libro = repository.buscarPorId(id);

      // preparo Gson para convertir objetos Java a JSON
      Gson gson = new Gson();

      // valido si no encontre ningún Libro con ese id
      if (libro == null) {
        // construyo un mensaje JSON indicando que no existe el Libro
        String notFoundJson = "{\"error\":\"Libro no encontrado\",\"id\":" + id + "}";

        // devuelvo una respuesta 404 con el mensaje en formato JSON
        return request.createResponseBuilder(HttpStatus.NOT_FOUND)
            .header("Content-Type", "application/json")
            .body(notFoundJson)
            .build();
      }

      // convierto el Libro encontrado a formato JSON
      String json = gson.toJson(libro);

      // devuelvo una respuesta 200 con el Libro encontrado
      return request.createResponseBuilder(HttpStatus.OK)
          .header("Content-Type", "application/json")
          .body(json)
          .build();

    } catch (Exception e) {
      // registro el error en consola para saber qué falló
      context.getLogger().severe("Error al buscar Libro por id: " + e.getMessage());

      // construyo un JSON con el detalle del error
      String errorJson = "{\"error\":\"Error al buscar Libro\",\"detalle\":\""
          + e.getMessage().replace("\"", "'") + "\"}";

      // devuelvo una respuesta 500 indicando que ocurrió un error interno
      return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
          .header("Content-Type", "application/json")
          .body(errorJson)
          .build();
    }
  }
}
