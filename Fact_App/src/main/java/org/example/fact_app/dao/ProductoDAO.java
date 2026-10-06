package org.example.fact_app.dao;

import org.example.fact_app.model.Categoria;
import org.example.fact_app.model.Producto;
import org.example.fact_app.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> listar() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = """
            SELECT p.id, p.codigo, p.nombre, p.categoria_id, p.precio_venta, p.existencia, p.ruta_imagen, p.activo,
                   c.nombre AS categoria_nombre, c.activa AS categoria_activa
            FROM producto p
            INNER JOIN categoria c ON p.categoria_id = c.id
            ORDER BY p.id ASC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Categoria categoria = new Categoria();
                categoria.setId(rs.getInt("categoria_id"));
                categoria.setNombre(rs.getString("categoria_nombre"));
                categoria.setActiva(rs.getBoolean("categoria_activa"));

                Producto producto = new Producto();
                producto.setId(rs.getInt("id"));
                producto.setCodigo(rs.getString("codigo"));
                producto.setNombre(rs.getString("nombre"));
                producto.setCategoria(categoria);
                producto.setPrecioVenta(rs.getBigDecimal("precio_venta"));
                producto.setExistencia(rs.getInt("existencia"));
                producto.setRutaImagen(rs.getString("ruta_imagen"));
                producto.setActivo(rs.getBoolean("activo"));

                lista.add(producto);
            }
        }
        return lista;
    }

    public boolean guardar(Producto producto) throws SQLException {
        String sql = """
            INSERT INTO producto 
            (codigo, nombre, categoria_id, precio_venta, existencia, ruta_imagen, activo) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setInt(3, producto.getCategoria().getId());
            ps.setBigDecimal(4, producto.getPrecioVenta());
            ps.setInt(5, producto.getExistencia());
            ps.setString(6, producto.getRutaImagen());
            ps.setBoolean(7, producto.isActivo());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    public boolean actualizar(Producto producto) throws SQLException {
        String sql = """
            UPDATE producto 
            SET codigo = ?, nombre = ?, categoria_id = ?, precio_venta = ?, existencia = ?, ruta_imagen = ?, activo = ? 
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setInt(3, producto.getCategoria().getId());
            ps.setBigDecimal(4, producto.getPrecioVenta());
            ps.setInt(5, producto.getExistencia());
            ps.setString(6, producto.getRutaImagen());
            ps.setBoolean(7, producto.isActivo());
            ps.setInt(8, producto.getId());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM producto WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    public boolean existeCodigo(String codigo, Integer excluirId) throws SQLException {
        String sql;
        if (excluirId == null) {
            sql = "SELECT COUNT(*) FROM producto WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(?))";
        } else {
            sql = "SELECT COUNT(*) FROM producto WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(?)) AND id <> ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codigo.trim());
            if (excluirId != null) {
                ps.setInt(2, excluirId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}