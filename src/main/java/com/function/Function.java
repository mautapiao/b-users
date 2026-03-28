package com.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */

public class Function {

        @FunctionName("test")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS
            )
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Function ejecutada correctamente");

        return request.createResponseBuilder(HttpStatus.OK)
                .body("OK FUNCIONANDO 🚀")
                .build();
    }


    @FunctionName("HttpExample")
    public HttpResponseMessage run_(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

if (System.getenv("ORACLE_URL") == null) {
    return request.createResponseBuilder(HttpStatus.OK)
            .body("Modo test OK")
            .build();
}

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) TOTAL FROM B6_USUARIOS");
             ResultSet rs = stmt.executeQuery()) {

            int total = 0;
            if (rs.next()) {
                total = rs.getInt("TOTAL");
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .body("Conexion OK. Total usuarios: " + total)
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error Oracle: " + e.getMessage());

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error conectando con Oracle: " + e.getMessage())
                    .build();
        }
    }
}
