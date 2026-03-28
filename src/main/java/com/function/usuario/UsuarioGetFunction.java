package com.function.usuario;

import java.util.List;
import java.util.Optional;

import com.function.model.Usuario;
import com.function.repository.UsuarioRepository;
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

public class UsuarioGetFunction {
  @FunctionName("GetUsuarios")
  public HttpResponseMessage getUsuarios(
      @HttpTrigger(name = "req", methods = {
          HttpMethod.GET }, route = "usuarios", authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
      final ExecutionContext context) {

    // recibo una petición GET para listar todos los usuarios
    context.getLogger().info("Obteniendo usuarios desde Oracle...");

    try {
      // creo el repositorio para consultar la base de datos
      UsuarioRepository repository = new UsuarioRepository();

      // obtengo la lista completa de usuarios desde oracle
      List<Usuario> usuarios = repository.listarUsuarios();

      // preparo gson para convertir la lista de usuarios a JSON
      Gson gson = new Gson();

      // convierto la lista de usuarios a formato JSON
      String json = gson.toJson(usuarios);

      // devuelvo una respuesta 200 con la lista de usuarios en JSON
      return request.createResponseBuilder(HttpStatus.OK)
          .header("Content-Type", "application/json")
          .body(json)
          .build();

    } catch (Exception e) {
      // registro el error para revisar qué ocurrió
      context.getLogger().severe("Error al obtener usuarios: " + e.getMessage());

      // construyo un JSON con el detalle del error
      String errorJson = "{\"error\":\"Error al obtener usuarios\",\"detalle\":\""
          + e.getMessage().replace("\"", "'") + "\"}";

      // devuelvo una respuesta 500 porque ocurrió un error interno
      return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
          .header("Content-Type", "application/json")
          .body(errorJson)
          .build();
    }
  }

  @FunctionName("GetUsuarioById")
  public HttpResponseMessage getUsuarioById(
      @HttpTrigger(name = "req", methods = {
          HttpMethod.GET }, route = "usuarios/{id:int}", authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
      @BindingName("id") int id,
      final ExecutionContext context) {

    // recibo una petición GET y tomo el id que viene en la URL
    context.getLogger().info("Buscando usuario con id: " + id);

    try {
      // creo el repositorio para consultar la base de datos
      UsuarioRepository repository = new UsuarioRepository();

      // busco un usuario por su id
      Usuario usuario = repository.buscarPorId(id);

      // preparo Gson para convertir objetos Java a JSON
      Gson gson = new Gson();

      // valido si no encontre ningún usuario con ese id
      if (usuario == null) {
        // construyo un mensaje JSON indicando que no existe el usuario
        String notFoundJson = "{\"error\":\"Usuario no encontrado\",\"id\":" + id + "}";

        // devuelvo una respuesta 404 con el mensaje en formato JSON
        return request.createResponseBuilder(HttpStatus.NOT_FOUND)
            .header("Content-Type", "application/json")
            .body(notFoundJson)
            .build();
      }

      // convierto el usuario encontrado a formato JSON
      String json = gson.toJson(usuario);

      // devuelvo una respuesta 200 con el usuario encontrado
      return request.createResponseBuilder(HttpStatus.OK)
          .header("Content-Type", "application/json")
          .body(json)
          .build();

    } catch (Exception e) {
      // registro el error en consola para saber qué falló
      context.getLogger().severe("Error al buscar usuario por id: " + e.getMessage());

      // construyo un JSON con el detalle del error
      String errorJson = "{\"error\":\"Error al buscar usuario\",\"detalle\":\""
          + e.getMessage().replace("\"", "'") + "\"}";

      // devuelvo una respuesta 500 indicando que ocurrió un error interno
      return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
          .header("Content-Type", "application/json")
          .body(errorJson)
          .build();
    }
  }
}
