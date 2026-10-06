package org.example.fact_app;

import org.example.fact_app.util.DatabaseConnection;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

public class ApplySqlScript {
    public static void main(String[] args) throws Exception {
        Path path = Path.of("scripts/restricciones_postgresql.sql");
        if (!Files.exists(path)) {
            path = Path.of("Fact_App/scripts/restricciones_postgresql.sql");
        }
        String sql = Files.readString(path);

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Execute the script
            stmt.execute(sql);
            System.out.println("¡Script PostgreSQL ejecutado con éxito en la base de datos!");
        } catch (Exception e) {
            System.err.println("Error al ejecutar script: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
