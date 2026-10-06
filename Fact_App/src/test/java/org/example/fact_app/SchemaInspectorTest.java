package org.example.fact_app;

import org.example.fact_app.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SchemaInspectorTest {

    public static void main(String[] args) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("=== TABLAS Y COLUMNAS ===");
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT table_name, column_name, data_type, is_nullable " +
                    "FROM information_schema.columns " +
                    "WHERE table_schema = 'public' " +
                    "ORDER BY table_name, ordinal_position")) {
                while (rs.next()) {
                    System.out.printf("%s.%s : %s (nullable=%s)%n",
                            rs.getString("table_name"),
                            rs.getString("column_name"),
                            rs.getString("data_type"),
                            rs.getString("is_nullable"));
                }
            }

            System.out.println("\n=== RESTRICCIONES (CONSTRAINTS) ===");
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT conname, contype, relname " +
                    "FROM pg_constraint c " +
                    "JOIN pg_class cl ON c.conrelid = cl.oid " +
                    "JOIN pg_namespace n ON cl.relnamespace = n.oid " +
                    "WHERE n.nspname = 'public'")) {
                while (rs.next()) {
                    System.out.printf("Tabla %s: %s (tipo=%s)%n",
                            rs.getString("relname"),
                            rs.getString("conname"),
                            rs.getString("contype"));
                }
            }

            System.out.println("\n=== DATOS EN CATEGORIA ===");
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM categoria")) {
                while (rs.next()) {
                    System.out.printf("Categoria: id=%d, nombre='%s', activa=%s%n",
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getBoolean("activa"));
                }
            }

            System.out.println("\n=== DATOS EN PRODUCTO ===");
            try (ResultSet rs = stmt.executeQuery("SELECT id, codigo, nombre, categoria_id, precio_venta, existencia, activo FROM producto")) {
                while (rs.next()) {
                    System.out.printf("Producto: id=%d, codigo='%s', nombre='%s', cat_id=%d, precio=%s, existencia=%d, activo=%s%n",
                            rs.getInt("id"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getInt("categoria_id"),
                            rs.getBigDecimal("precio_venta"),
                            rs.getInt("existencia"),
                            rs.getBoolean("activo"));
                }
            }
        }
    }
}
