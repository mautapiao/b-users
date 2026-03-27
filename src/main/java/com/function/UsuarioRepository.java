package com.function;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UsuarioRepository {

    public List<Usuario> listarUsuarios() throws Exception {
        List<Usuario> usuarios = new ArrayList<>();

        String sql = "SELECT ID, NOMBRE, EMAIL, ROL FROM B6_USUARIOS ORDER BY ID";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setId(rs.getInt("ID"));
                usuario.setNombre(rs.getString("NOMBRE"));
                usuario.setEmail(rs.getString("EMAIL"));
                usuario.setRol(rs.getString("ROL"));

                usuarios.add(usuario);
            }
        }

        return usuarios;
    }

    public Usuario buscarPorId(int id) throws Exception {
        String sql = "SELECT ID, NOMBRE, EMAIL, ROL FROM B6_USUARIOS WHERE ID = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setId(rs.getInt("ID"));
                    usuario.setNombre(rs.getString("NOMBRE"));
                    usuario.setEmail(rs.getString("EMAIL"));
                    usuario.setRol(rs.getString("ROL"));

                    return usuario;
                }
            }
        }

        return null;
    }
    public Usuario buscarPorEmail(String email) throws Exception {
        String sql = "SELECT ID, NOMBRE, EMAIL, ROL FROM B6_USUARIOS WHERE EMAIL = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setId(rs.getInt("ID"));
                    usuario.setNombre(rs.getString("NOMBRE"));
                    usuario.setEmail(rs.getString("EMAIL"));
                    usuario.setRol(rs.getString("ROL"));

                    return usuario;
                }
            }
        }

        return null;
    }

    public Usuario insertarUsuario(Usuario usuario) throws Exception {
        // Aquí valido el rol y si viene vacío lo dejo como USER
        if (usuario.getRol() == null || usuario.getRol().isBlank()) {
            usuario.setRol("USER");
        }

        String sql = "INSERT INTO B6_USUARIOS (NOMBRE, EMAIL, ROL) VALUES (?, ?, ?)";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Aquí envío los datos del usuario al INSERT
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getRol());

            // Aquí ejecuto el INSERT en Oracle
            stmt.executeUpdate();
        }

        // Aquí vuelvo a buscar el usuario por email para devolverlo con su id
        return buscarPorEmail(usuario.getEmail());
    }

    public Usuario actualizarUsuario(int id, Usuario usuario) throws Exception {
    // Aquí valido el rol y si viene vacío lo dejo como USER
    if (usuario.getRol() == null || usuario.getRol().isBlank()) {
        usuario.setRol("USER");
    }

    String sql = "UPDATE B6_USUARIOS SET NOMBRE = ?, EMAIL = ?, ROL = ? WHERE ID = ?";

    try (Connection conn = OracleConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        // Aquí envío los nuevos datos al UPDATE
        stmt.setString(1, usuario.getNombre());
        stmt.setString(2, usuario.getEmail());
        stmt.setString(3, usuario.getRol());
        stmt.setInt(4, id);

        // Aquí ejecuto el UPDATE y guardo cuántas filas cambié
        int filasActualizadas = stmt.executeUpdate();

        // Aquí valido si no encontré ningún usuario con ese id
        if (filasActualizadas == 0) {
            return null;
        }
    }

    // Aquí vuelvo a consultar el usuario actualizado para devolverlo con sus datos finales
    return buscarPorId(id);
}

public boolean eliminarUsuario(int id) throws Exception {
    String sql = "DELETE FROM B6_USUARIOS WHERE ID = ?";

    try (Connection conn = OracleConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        // Aquí envío el id del usuario que quiero eliminar
        stmt.setInt(1, id);

        // Aquí ejecuto el DELETE y guardo cuántas filas eliminé
        int filasEliminadas = stmt.executeUpdate();

        // Aquí devuelvo true si sí eliminé un registro
        return filasEliminadas > 0;
    }
}


}
