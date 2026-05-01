package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

public class EnviarCorreoDevolucion {
  
  @FunctionName("EnviarCorreoDevolucion")
public void enviarCorreo(
    @EventGridTrigger(name = "event") String event,
    final ExecutionContext context) {

    context.getLogger().info("Evento recibido: " + event);

    // Aquí enviatr correo
    context.getLogger().info("Enviando correo por devolución...");
}

}
