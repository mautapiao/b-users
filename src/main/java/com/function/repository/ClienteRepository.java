package com.function.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.function.OracleConnection;
import com.function.model.Cliente;

public class ClienteRepository {

    public List<Cliente> listarClientes() throws Exception {
        List<Cliente> clientes = new ArrayList<>();

        String sql = "SELECT ID, RUT, DV,NOMBRE FROM B6_CLIENTES ORDER BY NOMBRE";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setId(rs.getInt("ID"));
                cliente.setRut(rs.getInt("RUT"));
                cliente.setDv(rs.getString("DV"));
                cliente.setNombre(rs.getString("NOMBRE"));
                clientes.add(cliente);
            }
        }

        return clientes;
    }

    public Cliente buscarPorId(int id) throws Exception {
        String sql = "SELECT ID, RUT, DV,NOMBRE FROM B6_CLIENTES WHERE ID = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setId(rs.getInt("ID"));
                    cliente.setRut(rs.getInt("RUT"));
                    cliente.setDv(rs.getString("DV"));
                    cliente.setNombre(rs.getString("NOMBRE"));

                    return cliente;
                }
            }
        }

        return null;
    }


    public Cliente buscarPorRut(String rutCompleto) throws Exception {

    // Separo la parte entera y el dv del formato "12333444-K"
    if (rutCompleto == null || !rutCompleto.contains("-")) {
        throw new IllegalArgumentException("Formato de RUT inválido se espera: 12333444-K");
    }

    String[] partes = rutCompleto.split("-");
    int rutNumero = Integer.parseInt(partes[0]);
    String dv = partes[1].toUpperCase();

    String sql = "SELECT ID, RUT, DV, NOMBRE FROM B6_CLIENTES WHERE RUT = ? AND DV = ?";

    try (Connection conn = OracleConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, rutNumero);
        stmt.setString(2, dv);

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setId(rs.getInt("ID"));
                cliente.setRut(rs.getInt("RUT"));
                cliente.setDv(rs.getString("DV"));
                cliente.setNombre(rs.getString("NOMBRE")); 
                return cliente;
            }
        }
    }
    return null;
            }


    public Cliente insertarCliente(Cliente cliente) throws Exception {

        String sql = "INSERT INTO B6_CLIENTES (RUT,DV, NOMBRE) VALUES ( ?, ?,?)";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // aquí envío los datos delcliente  al INSERT
            stmt.setInt(1, cliente.getRut());
            stmt.setString(2, cliente.getDv());
            stmt.setString(2, cliente.getNombre());
            // aquí ejecuto el INSERT en Oracle
            stmt.executeUpdate();
        }

        // aquí vuelvo a buscar elregistro  para devolverlo con su id
        return buscarPorId(cliente.getId());
    }

    public Cliente actualizarCliente(int id, Cliente cliente) throws Exception {
    

    String sql = "UPDATE B6_CLIENTES SET RUT = ?, DV=?, NOMBRE = ? WHERE ID = ?";

    try (Connection conn = OracleConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        //envío los nuevos datos al UPDATE
        stmt.setInt(1, cliente.getRut());
        stmt.setString(2, cliente.getDv());
                stmt.setString(3, cliente.getNombre());

        stmt.setInt(4, id);

        // ejecuto el UPDATE..guardo
        int filasActualizadas = stmt.executeUpdate();

        // valido si no encontré ningún registro  con ese id
        if (filasActualizadas == 0) {
            return null;
        }
    }

    // Aquí vuelvo a consultar el Libro actualizado para devolverlo con sus datos finales
    return buscarPorId(id);
}

public boolean eliminarCliente(int id) throws Exception {
    String sql = "DELETE FROM B6_CLIENTES WHERE ID = ?";

    try (Connection conn = OracleConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        //envío el id  que quiero eliminar
        stmt.setInt(1, id);

        //  ejecuto el DELETE y guardo cuántas filas eliminé
        int filasEliminadas = stmt.executeUpdate();

        // devuelvo true si sí eliminé un registro
        return filasEliminadas > 0;
    }
}


}
