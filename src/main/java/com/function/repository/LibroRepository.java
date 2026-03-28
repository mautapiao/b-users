package com.function.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.function.OracleConnection;
import com.function.model.Libro;

public class LibroRepository {

    public List<Libro> listarLibros() throws Exception {
        List<Libro> libros = new ArrayList<>();

        String sql = "SELECT ID, TITULO, AUTOR FROM B6_LIBROS ORDER BY TITULO";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Libro libro = new Libro();
                libro.setId(rs.getInt("ID"));
                libro.setTitulo(rs.getString("TITULO"));
                libro.setAutor(rs.getString("AUTOR"));

                libros.add(libro);
            }
        }

        return libros;
    }

    public Libro buscarPorId(int id) throws Exception {
        String sql = "SELECT ID, TITULO, AUTOR FROM B6_LIBROS WHERE ID = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Libro libro = new Libro();
                    libro.setId(rs.getInt("ID"));
                    libro.setTitulo(rs.getString("TITULO"));
                    libro.setAutor(rs.getString("AUTOR"));

                    return libro;
                }
            }
        }

        return null;
    }

    public Libro buscarPorTitulo(String titulo) throws Exception {
        String sql = "SELECT ID, TITULO, AUTOR FROM B6_LIBROS WHERE TITULO = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, titulo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Libro libro = new Libro();
                    libro.setId(rs.getInt("ID"));
                    libro.setTitulo(rs.getString("TITULO"));
                    libro.setAutor(rs.getString("AUTOR"));

                    return libro;
                }
            }
        }

        return null;
    }

    public Libro insertarLibro(Libro libro) throws Exception {

        String sql = "INSERT INTO B6_LIBROS (TITULO, AUTOR) VALUES ( ?, ?)";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Aquí envío los datos del Libro al INSERT
            stmt.setString(1, libro.getTitulo());
            stmt.setString(2, libro.getAutor());

            // Aquí ejecuto el INSERT en Oracle
            stmt.executeUpdate();
        }

        // Aquí vuelvo a buscar el Libro por totulo para devolverlo con su id
        return buscarPorTitulo(libro.getTitulo());
    }

    public Libro actualizarLibro(int id, Libro libro) throws Exception {

        String sql = "UPDATE B6_LIBROS SET TITULO = ?, AUTOR = ? WHERE ID = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Aquí envío los nuevos datos al UPDATE
            stmt.setString(1, libro.getTitulo());
            stmt.setString(2, libro.getAutor());
            stmt.setInt(3, id);

            // Aquí ejecuto el UPDATE y guardo cuántas filas cambié
            int filasActualizadas = stmt.executeUpdate();

            // Aquí valido si no encontré ningún Libro con ese id
            if (filasActualizadas == 0) {
                return null;
            }
        }

        // Aquí vuelvo a consultar el Libro actualizado para devolverlo con sus datos
        // finales
        return buscarPorId(id);
    }

    public boolean eliminarLibro(int id) throws Exception {
        String sql = "DELETE FROM B6_LIBROS WHERE ID = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Aquí envío el id del Libro que quiero eliminar
            stmt.setInt(1, id);

            // Aquí ejecuto el DELETE y guardo cuántas filas eliminé
            int filasEliminadas = stmt.executeUpdate();

            // Aquí devuelvo true si sí eliminé un registro
            return filasEliminadas > 0;
        }
    }

}
