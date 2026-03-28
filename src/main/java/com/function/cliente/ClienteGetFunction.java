package com.function.cliente;

import java.util.List;
import java.util.Optional;

import com.function.model.Cliente;
import com.function.repository.ClienteRepository;
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

public class ClienteGetFunction {
  @FunctionName("GetClientes")
  public HttpResponseMessage getClientes(
      @HttpTrigger(name = "req", methods = {
          HttpMethod.GET }, route = "clientes", authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
      final ExecutionContext context) {

    // recibo una petición GET para listar todos los registros
    context.getLogger().info("Obteniendo registros desde Oracle...");

    try {
      // creo el repositorio para consultar la base de datos
      ClienteRepository repository = new ClienteRepository();

      // obtengo la lista completa desde oracle
      List<Cliente> clientes = repository.listarClientes();

      // preparo gson para convertir la lista a JSON
      Gson gson = new Gson();

      // convierto la lista a formato JSON
      String json = gson.toJson(clientes);

      // devuelvo una respuesta 200 con la lista de registros en JSON
      return request.createResponseBuilder(HttpStatus.OK)
          .header("Content-Type", "application/json")
          .body(json)
          .build();

    } catch (Exception e) {
      // registro el error para revisar qué ocurrió
      context.getLogger().severe("Error al obtener registros: " + e.getMessage());

      // construyo un JSON con el detalle del error
      String errorJson = "{\"error\":\"Error al obtener registros\",\"detalle\":\""
          + e.getMessage().replace("\"", "'") + "\"}";

      // devuelvo una respuesta 500 porque ocurrió un error interno
      return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
          .header("Content-Type", "application/json")
          .body(errorJson)
          .build();
    }
  }

  @FunctionName("GetClienteById")
  public HttpResponseMessage getLibroById(
      @HttpTrigger(name = "req", methods = {
          HttpMethod.GET }, route = "Clientes/{id:int}", authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
      @BindingName("id") int id,
      final ExecutionContext context) {

    // recibo una petición GET y tomo el id que viene en la URL
    context.getLogger().info("Buscando registro con id: " + id);

    try {
      // creo el repositorio para consultar la base de datos
      ClienteRepository repository = new ClienteRepository();

      // busco por su id
      Cliente cliente = repository.buscarPorId(id);

      // preparo Gson para convertir objetos Java a JSON
      Gson gson = new Gson();

      // valido si no encontre ningún registro con ese id
      if (cliente == null) {
        // construyo un mensaje JSON indicando que no existe el regsitro
        String notFoundJson = "{\"error\":\"Registro no encontrado\",\"id\":" + id + "}";

        // devuelvo una respuesta 404 con el mensaje en formato JSON
        return request.createResponseBuilder(HttpStatus.NOT_FOUND)
            .header("Content-Type", "application/json")
            .body(notFoundJson)
            .build();
      }

      // convierto el registro encontrado a formato JSON
      String json = gson.toJson(cliente);

      // devuelvo una respuesta 200 con el registro encontrado
      return request.createResponseBuilder(HttpStatus.OK)
          .header("Content-Type", "application/json")
          .body(json)
          .build();

    } catch (Exception e) {
      // registro el error en consola para saber qué falló
      context.getLogger().severe("Error al buscar por id: " + e.getMessage());

      // construyo un JSON con el detalle del error
      String errorJson = "{\"error\":\"Error al buscar \",\"detalle\":\""
          + e.getMessage().replace("\"", "'") + "\"}";

      // devuelvo una respuesta 500 indicando que ocurrió un error interno
      return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
          .header("Content-Type", "application/json")
          .body(errorJson)
          .build();
    }
  }
}
