package com.function.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.function.OracleConnection;
import com.function.model.Prestamo;
import com.function.model.PrestamoDetalle;

public class PrestamoRepository {

    // Listo todos los préstamos con libro, autor y cliente (todos, incluso
    // devueltos)
    public List<PrestamoDetalle> listarPrestamos() throws Exception {
        List<PrestamoDetalle> lista = new ArrayList<>();

        // podriamos agrefgar la relacion b6_usuarios quien hace el prestamo etc

        String sql = """
                SELECT P.ID,
                       TO_CHAR(P.FECHA_PRESTAMO, 'YYYY-MM-DD') AS FECHA_PRESTAMO,
                       TO_CHAR(P.FECHA_ENTREGA,  'YYYY-MM-DD') AS FECHA_ENTREGA,
                       L.TITULO,
                       L.AUTOR,
                       C.NOMBRE AS NOMBRE_CLIENTE
                  FROM B6_PRESTAMOS P
                  JOIN B6_LIBROS    L ON L.ID = P.B6_LIBROS_ID
                  JOIN B6_CLIENTES  C ON C.ID = P.B6_CLIENTES_ID
                 ORDER BY P.ID
                """;

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PrestamoDetalle p = new PrestamoDetalle();
                p.setId(rs.getInt("ID"));
                p.setFechaPrestamo(rs.getString("FECHA_PRESTAMO"));
                p.setFechaEntrega(rs.getString("FECHA_ENTREGA")); // null si no fue devuelto
                p.setTitulo(rs.getString("TITULO"));
                p.setAutor(rs.getString("AUTOR"));
                p.setNombreCliente(rs.getString("NOMBRE_CLIENTE"));
                lista.add(p);
            }
        }
        return lista;
    }

    // Listo solo los préstamos pendientes, es decir con FECHA_ENTREGA en null
    public List<PrestamoDetalle> listarPrestamosPendientes() throws Exception {
        List<PrestamoDetalle> lista = new ArrayList<>();

        String sql = """
                SELECT P.ID,
                       TO_CHAR(P.FECHA_PRESTAMO, 'YYYY-MM-DD') AS FECHA_PRESTAMO,
                       L.TITULO,
                       L.AUTOR,
                       C.NOMBRE AS NOMBRE_CLIENTE
                  FROM B6_PRESTAMOS P
                  JOIN B6_LIBROS    L ON L.ID = P.B6_LIBROS_ID
                  JOIN B6_CLIENTES  C ON C.ID = P.B6_CLIENTES_ID
                 WHERE P.FECHA_ENTREGA IS NULL
                 ORDER BY P.FECHA_PRESTAMO
                """;

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PrestamoDetalle p = new PrestamoDetalle();
                p.setId(rs.getInt("ID"));
                p.setFechaPrestamo(rs.getString("FECHA_PRESTAMO"));
                p.setFechaEntrega(null); // siempre null aquí, lo dejo explícito
                p.setTitulo(rs.getString("TITULO"));
                p.setAutor(rs.getString("AUTOR"));
                p.setNombreCliente(rs.getString("NOMBRE_CLIENTE"));
                lista.add(p);
            }
        }
        return lista;
    }

    // Busco un préstamo por id para devolverlo después del INSERT o UPDATE
    public Prestamo buscarPorId(int id) throws Exception {
        String sql = """
                SELECT ID,
                       TO_CHAR(FECHA_PRESTAMO, 'YYYY-MM-DD') AS FECHA_PRESTAMO,
                       TO_CHAR(FECHA_ENTREGA,  'YYYY-MM-DD') AS FECHA_ENTREGA,
                       B6_USUARIOS_ID, B6_LIBROS_ID, B6_CLIENTES_ID
                  FROM B6_PRESTAMOS
                 WHERE ID = ?
                """;

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Prestamo p = new Prestamo();
                    p.setId(rs.getInt("ID"));
                    p.setFechaPrestamo(rs.getString("FECHA_PRESTAMO"));
                    p.setFechaEntrega(rs.getString("FECHA_ENTREGA"));
                    p.setUsuarioId(rs.getInt("B6_USUARIOS_ID"));
                    p.setLibroId(rs.getInt("B6_LIBROS_ID"));
                    p.setClienteId(rs.getInt("B6_CLIENTES_ID"));
                    return p;
                }
            }
        }
        return null;
    }

    public Optional<Prestamo> insertarPrestamo(Prestamo prestamo) throws SQLException {

    // Verifico disponibilidad ANTES de insertar — resultado de negocio, no excepción
    if (!libroDisponible(prestamo.getLibroId())) {
        return Optional.empty();
    }

    String sql = """
        INSERT INTO B6_PRESTAMOS
            (FECHA_PRESTAMO, FECHA_ENTREGA, B6_USUARIOS_ID, B6_LIBROS_ID, B6_CLIENTES_ID)
        VALUES
            (SYSDATE, NULL, ?, ?, ?)
        """;

    try (Connection conn = OracleConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID"})) {

        stmt.setInt(1, prestamo.getUsuarioId());
        stmt.setInt(2, prestamo.getLibroId());
        stmt.setInt(3, prestamo.getClienteId());
        stmt.executeUpdate();

        try (ResultSet rs = stmt.getGeneratedKeys()) {
    if (rs.next()) {
        int idGenerado = rs.getInt(1);
        try {
            return Optional.ofNullable(buscarPorId(idGenerado));
        } catch (Exception e) {
            // El INSERT fue exitoso pero no pudo recuperar el registro
            throw new SQLException("Préstamo insertado pero no se pudo recuperar. ID: " + idGenerado, e);
        }
    }
}
    }

    return Optional.empty();
}

    // Inserto un nuevo préstamo con fecha de hoy y sin fecha de entrega
    public Prestamo _insertarPrestamo(Prestamo prestamo) throws Exception {

        // verifico disponibilidad ANTES de insertar
        if (!libroDisponible(prestamo.getLibroId())) {
            throw new IllegalStateException("El libro con id " + prestamo.getLibroId() + " no está disponible");
        }

        String sql = """
                INSERT INTO B6_PRESTAMOS
                    (FECHA_PRESTAMO, FECHA_ENTREGA, B6_USUARIOS_ID, B6_LIBROS_ID, B6_CLIENTES_ID)
                VALUES
                    (SYSDATE, NULL, ?, ?, ?)
                """;

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql,
                        new String[] { "ID" })) { // Aquí pido que Oracle me devuelva el ID generado

            stmt.setInt(1, prestamo.getUsuarioId());
            stmt.setInt(2, prestamo.getLibroId());
            stmt.setInt(3, prestamo.getClienteId());
            stmt.executeUpdate();

            // Aquí recupero el ID que Oracle generó automáticamente con IDENTITY
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    return buscarPorId(idGenerado);
                }
            }
        }
        return null;
    }

    // Actualizo la fecha de entrega de un préstamo para marcarlo como devuelto
    public Prestamo registrarDevolucion(int id) throws Exception {
        String sql = "UPDATE B6_PRESTAMOS SET FECHA_ENTREGA = SYSDATE WHERE ID = ? AND FECHA_ENTREGA IS NULL";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int filasActualizadas = stmt.executeUpdate();

            // Aquí valido si el préstamo no existía o ya tenía fecha de entrega
            if (filasActualizadas == 0) {
                return null;
            }
        }
        // Aquí devuelvo el préstamo ya con la fecha de entrega registrada
        return buscarPorId(id);
    }

    // verifico si un libro está disponible: disponible = todos sus préstamos tienen
    // fecha_entrega
    /* 
    public boolean libroDisponible(int libroId) throws Exception {
        String sql = """
                SELECT COUNT(*) AS PENDIENTES
                  FROM B6_PRESTAMOS
                 WHERE B6_LIBROS_ID = ?
                   AND FECHA_ENTREGA IS NULL
                """;

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, libroId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("PENDIENTES") == 0; // true = disponible, false = prestado
                }
            }
        }
        return true; // si no tiene préstamos, está disponible
    }
   */
public boolean libroDisponible(int libroId) throws SQLException {
    
    String sql = """
        SELECT 
            COUNT(*) AS TOTAL_LIBRO,
            SUM(CASE WHEN p.FECHA_ENTREGA IS NULL THEN 1 ELSE 0 END) AS PRESTAMOS_PENDIENTES
        FROM B6_LIBROS l
        LEFT JOIN B6_PRESTAMOS p ON p.B6_LIBROS_ID = l.ID
        WHERE l.ID = ?
    """;

    try (Connection conn = OracleConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, libroId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            // Si no existe el libro, retorna false (no null)
            if (rs.getInt("TOTAL_LIBRO") == 0) {
                return false;
            }
            // Disponible si no tiene préstamos pendientes
            return rs.getInt("PRESTAMOS_PENDIENTES") == 0;
        }

        return false;
    }
}

    public Boolean _libroDisponible(int libroId) throws Exception {

    // 1. Verificar existencia
    String sqlLibro = "SELECT COUNT(*) FROM B6_LIBROS WHERE ID = ?";

    try (Connection conn = OracleConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sqlLibro)) {

        stmt.setInt(1, libroId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next() && rs.getInt(1) == 0) {
            return null; // libro no existe
        }
    }

    // 2. Verificar préstamos
    String sqlPrestamo = """
        SELECT COUNT(*) AS PENDIENTES
        FROM B6_PRESTAMOS
        WHERE B6_LIBROS_ID = ?
        AND FECHA_ENTREGA IS NULL
    """;

    try (Connection conn = OracleConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sqlPrestamo)) {

        stmt.setInt(1, libroId);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("PENDIENTES") == 0;
        }
    }

    return true;
}


}
