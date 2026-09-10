package org.example.fact_app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    private Integer id;
    private String nombre;
    private boolean activa;
    private String descripcion;

    public Categoria(Integer id, String nombre, boolean activa) {
        this(id, nombre, activa, "");
    }

    @Override
    public String toString() {
        return nombre;
    }
}