package org.example.fact_app;

import org.example.fact_app.dao.CategoriaDAO;
import org.example.fact_app.dao.ProductoDAO;
import org.example.fact_app.model.Categoria;
import org.example.fact_app.model.Producto;

import java.math.BigDecimal;
import java.sql.SQLException;

public class PracticaValidacionesTest {

    private static final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private static final ProductoDAO productoDAO = new ProductoDAO();

    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        System.out.println("=================================================");
        System.out.println("EJECUTANDO PRUEBAS DE VALIDACIÓN Y EXCEPCIONES");
        System.out.println("=================================================");

        // Test 1: Categoría vacía y con espacios
        try {
            validarNombreCategoria("");
            System.err.println("FALLO Test 1: Debería rechazar nombre vacío");
            failed++;
        } catch (IllegalArgumentException e) {
            System.out.println("ÉXITO Test 1.1: Nombre vacío rechazado -> " + e.getMessage());
            passed++;
        }

        try {
            validarNombreCategoria("    ");
            System.err.println("FALLO Test 1.2: Debería rechazar nombre con puros espacios");
            failed++;
        } catch (IllegalArgumentException e) {
            System.out.println("ÉXITO Test 1.2: Nombre con espacios rechazado -> " + e.getMessage());
            passed++;
        }

        // Test 2: Categorías duplicadas con diferencias de mayúsculas
        try {
            boolean existeUpper = categoriaDAO.existeNombre("ALIMENTOS", null);
            boolean existeLower = categoriaDAO.existeNombre("alimentos", null);
            boolean existeMixed = categoriaDAO.existeNombre("  aLiMenToS  ", null);
            if (existeUpper && existeLower && existeMixed) {
                System.out.println("ÉXITO Test 2: Categoría duplicada detectada sin distinguir mayúsculas ni espacios");
                passed++;
            } else {
                System.err.println("FALLO Test 2: existeNombre no detectó duplicados en diferentes mayúsculas");
                failed++;
            }
        } catch (SQLException e) {
            System.err.println("FALLO Test 2 con SQLException: " + e.getMessage());
            failed++;
        }

        // Test 3: Actualizar y eliminar sin selección
        try {
            validarSeleccion(null, "modificar");
            System.err.println("FALLO Test 3.1: Debería exigir selección al modificar");
            failed++;
        } catch (IllegalArgumentException e) {
            System.out.println("ÉXITO Test 3.1: Modificar sin selección rechazado -> " + e.getMessage());
            passed++;
        }

        try {
            validarSeleccion(null, "eliminar");
            System.err.println("FALLO Test 3.2: Debería exigir selección al eliminar");
            failed++;
        } catch (IllegalArgumentException e) {
            System.out.println("ÉXITO Test 3.2: Eliminar sin selección rechazado -> " + e.getMessage());
            passed++;
        }

        // Test 4: Editar conservando el propio nombre o código
        try {
            boolean propioNombreDuplicado = categoriaDAO.existeNombre("Alimentos", 1);
            boolean propioCodigoDuplicado = productoDAO.existeCodigo("01", 1);
            if (!propioNombreDuplicado && !propioCodigoDuplicado) {
                System.out.println("ÉXITO Test 4: Se permite editar conservando el propio nombre/código");
                passed++;
            } else {
                System.err.println("FALLO Test 4: Marcó como duplicado el propio registro");
                failed++;
            }
        } catch (SQLException e) {
            System.err.println("FALLO Test 4 con SQLException: " + e.getMessage());
            failed++;
        }

        // Test 5: Editar usando nombre o código perteneciente a otro registro
        try {
            boolean nombreOtroRegistro = categoriaDAO.existeNombre("Alimentos", 2);
            boolean codigoOtroRegistro = productoDAO.existeCodigo("01", 2);
            if (nombreOtroRegistro && codigoOtroRegistro) {
                System.out.println("ÉXITO Test 5: Bloquea uso de nombre/código perteneciente a otro registro");
                passed++;
            } else {
                System.err.println("FALLO Test 5: No detectó colisión con otro registro");
                failed++;
            }
        } catch (SQLException e) {
            System.err.println("FALLO Test 5 con SQLException: " + e.getMessage());
            failed++;
        }

        // Test 6: Eliminar una categoría con productos asociados
        try {
            boolean tieneProds = categoriaDAO.tieneProductos(1);
            if (tieneProds) {
                System.out.println("ÉXITO Test 6: Categoría 1 tiene productos asociados; eliminación bloqueada correctamente");
                passed++;
            } else {
                System.err.println("FALLO Test 6: No detectó productos asociados en categoría 1");
                failed++;
            }
        } catch (SQLException e) {
            System.err.println("FALLO Test 6 con SQLException: " + e.getMessage());
            failed++;
        }

        // Test 7: Producto sin código, sin nombre o sin categoría
        try {
            validarProductoFormulario("", "Producto Test", new Categoria(1, "Alimentos", true), "10.0", "5");
            System.err.println("FALLO Test 7.1: Debería rechazar código vacío");
            failed++;
        } catch (IllegalArgumentException e) {
            System.out.println("ÉXITO Test 7.1: Código vacío rechazado -> " + e.getMessage());
            passed++;
        }

        try {
            validarProductoFormulario("P99", "   ", new Categoria(1, "Alimentos", true), "10.0", "5");
            System.err.println("FALLO Test 7.2: Debería rechazar nombre en blanco");
            failed++;
        } catch (IllegalArgumentException e) {
            System.out.println("ÉXITO Test 7.2: Nombre en blanco rechazado -> " + e.getMessage());
            passed++;
        }

        try {
            validarProductoFormulario("P99", "Producto Test", null, "10.0", "5");
            System.err.println("FALLO Test 7.3: Debería rechazar categoría nula");
            failed++;
        } catch (IllegalArgumentException e) {
            System.out.println("ÉXITO Test 7.3: Categoría nula rechazada -> " + e.getMessage());
            passed++;
        }

        // Test 8: Código duplicado
        try {
            boolean codigoDuplicado = productoDAO.existeCodigo("01", null);
            if (codigoDuplicado) {
                System.out.println("ÉXITO Test 8: Código duplicado detectado correctamente");
                passed++;
            } else {
                System.err.println("FALLO Test 8: No detectó código existente");
                failed++;
            }
        } catch (SQLException e) {
            System.err.println("FALLO Test 8 con SQLException: " + e.getMessage());
            failed++;
        }

        // Test 9: Precio vacío, "abc", cero y negativo
        try {
            validarProductoFormulario("P99", "Producto Test", new Categoria(1, "Alimentos", true), "", "5");
            System.err.println("FALLO Test 9.1: Debería rechazar precio vacío");
            failed++;
        } catch (IllegalArgumentException e) {
            System.out.println("ÉXITO Test 9.1: Precio vacío rechazado -> " + e.getMessage());
            passed++;
        }

        try {
            validarProductoFormulario("P99", "Producto Test", new Categoria(1, "Alimentos", true), "abc", "5");
            System.err.println("FALLO Test 9.2: Debería rechazar precio 'abc'");
            failed++;
        } catch (NumberFormatException e) {
            System.out.println("ÉXITO Test 9.2: Precio 'abc' rechazado con NumberFormatException -> " + e.getMessage());
            passed++;
        }

        try {
            validarProductoFormulario("P99", "Producto Test", new Categoria(1, "Alimentos", true), "0", "5");
            System.err.println("FALLO Test 9.3: Debería rechazar precio cero");
            failed++;
        } catch (IllegalArgumentException e) {
            System.out.println("ÉXITO Test 9.3: Precio cero rechazado -> " + e.getMessage());
            passed++;
        }

        try {
            validarProductoFormulario("P99", "Producto Test", new Categoria(1, "Alimentos", true), "-15.5", "5");
            System.err.println("FALLO Test 9.4: Debería rechazar precio negativo");
            failed++;
        } catch (IllegalArgumentException e) {
            System.out.println("ÉXITO Test 9.4: Precio negativo rechazado -> " + e.getMessage());
            passed++;
        }

        // Test 10: Existencia vacía, "diez", "10.5", negativa y fuera de rango int
        try {
            validarProductoFormulario("P99", "Producto Test", new Categoria(1, "Alimentos", true), "20.0", "");
            System.err.println("FALLO Test 10.1: Debería rechazar existencia vacía");
            failed++;
        } catch (IllegalArgumentException e) {
            System.out.println("ÉXITO Test 10.1: Existencia vacía rechazada -> " + e.getMessage());
            passed++;
        }

        try {
            validarProductoFormulario("P99", "Producto Test", new Categoria(1, "Alimentos", true), "20.0", "diez");
            System.err.println("FALLO Test 10.2: Debería rechazar existencia 'diez'");
            failed++;
        } catch (NumberFormatException e) {
            System.out.println("ÉXITO Test 10.2: Existencia 'diez' rechazada con NumberFormatException -> " + e.getMessage());
            passed++;
        }

        try {
            validarProductoFormulario("P99", "Producto Test", new Categoria(1, "Alimentos", true), "20.0", "10.5");
            System.err.println("FALLO Test 10.3: Debería rechazar existencia '10.5'");
            failed++;
        } catch (NumberFormatException e) {
            System.out.println("ÉXITO Test 10.3: Existencia decimal rechazada con NumberFormatException -> " + e.getMessage());
            passed++;
        }

        try {
            validarProductoFormulario("P99", "Producto Test", new Categoria(1, "Alimentos", true), "20.0", "-5");
            System.err.println("FALLO Test 10.4: Debería rechazar existencia negativa");
            failed++;
        } catch (IllegalArgumentException e) {
            System.out.println("ÉXITO Test 10.4: Existencia negativa rechazada -> " + e.getMessage());
            passed++;
        }

        try {
            validarProductoFormulario("P99", "Producto Test", new Categoria(1, "Alimentos", true), "20.0", "9999999999999999");
            System.err.println("FALLO Test 10.5: Debería rechazar número fuera de rango de int");
            failed++;
        } catch (NumberFormatException e) {
            System.out.println("ÉXITO Test 10.5: Existencia fuera de rango rechazada con NumberFormatException -> " + e.getMessage());
            passed++;
        }

        // Test 11: Registro válido con existencia cero
        try {
            Producto prodCero = validarProductoFormulario("P_CERO", "Galletas", new Categoria(1, "Alimentos", true), "12.50", "0");
            if (prodCero.getExistencia() == 0) {
                System.out.println("ÉXITO Test 11: Producto válido con existencia cero aceptado exitosamente");
                passed++;
            } else {
                System.err.println("FALLO Test 11: Existencia no es cero");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("FALLO Test 11 con excepción: " + e.getMessage());
            failed++;
        }

        // Test 12: Ciclo CRUD completo en BD (Guardar, Actualizar y Eliminar)
        try {
            // Guardar categoría temporal
            Categoria catTemp = new Categoria(null, "Categoria_Test_Temporal", true);
            boolean guardadoCat = categoriaDAO.guardar(catTemp);
            if (!guardadoCat) throw new RuntimeException("No se guardó categoría");

            // Buscar id generado
            Categoria catGuardada = categoriaDAO.listar().stream()
                    .filter(c -> c.getNombre().equals("Categoria_Test_Temporal"))
                    .findFirst()
                    .orElseThrow();

            // Guardar producto
            Producto prodTemp = new Producto(null, "TEMP_01", "Producto Temporal", catGuardada, new BigDecimal("45.50"), 15, null, true);
            boolean guardadoProd = productoDAO.guardar(prodTemp);
            if (!guardadoProd) throw new RuntimeException("No se guardó producto");

            Producto prodGuardado = productoDAO.listar().stream()
                    .filter(p -> p.getCodigo().equals("TEMP_01"))
                    .findFirst()
                    .orElseThrow();

            // Actualizar producto
            prodGuardado.setNombre("Producto Temporal Actualizado");
            prodGuardado.setExistencia(20);
            boolean actProd = productoDAO.actualizar(prodGuardado);
            if (!actProd) throw new RuntimeException("No se actualizó producto");

            // Intentar eliminar categoría que ahora tiene productos asociados -> debe bloquearse
            boolean tieneProds = categoriaDAO.tieneProductos(catGuardada.getId());
            if (!tieneProds) throw new RuntimeException("Debería tener productos asociados");

            // Eliminar producto
            boolean elimProd = productoDAO.eliminar(prodGuardado.getId());
            if (!elimProd) throw new RuntimeException("No se eliminó producto");

            // Ahora que no tiene productos, eliminar categoría
            boolean elimCat = categoriaDAO.eliminar(catGuardada.getId());
            if (!elimCat) throw new RuntimeException("No se eliminó categoría");

            System.out.println("ÉXITO Test 12: Ciclo CRUD completo (Guardar, Actualizar, Bloqueo referencial y Eliminar) verificado con éxito");
            passed++;

        } catch (Exception e) {
            System.err.println("FALLO Test 12: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        System.out.println("\n=================================================");
        System.out.printf("RESULTADOS: %d PASADAS, %d FALLADAS%n", passed, failed);
        System.out.println("=================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    // Funciones simuladoras idénticas a las implementadas en los controladores
    private static void validarNombreCategoria(String nombre) {
        String n = nombre != null ? nombre.trim() : "";
        if (n.isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio y no puede consistir únicamente en espacios en blanco.");
        }
    }

    private static void validarSeleccion(Object obj, String operacion) {
        if (obj == null) {
            throw new IllegalArgumentException("Debe seleccionar un registro de la tabla para " + operacion + ".");
        }
    }

    private static Producto validarProductoFormulario(String codigo, String nombre, Categoria categoria, String precioTexto, String existenciaTexto) {
        String c = codigo != null ? codigo.trim() : "";
        if (c.isEmpty()) {
            throw new IllegalArgumentException("El código del producto es obligatorio.");
        }
        String n = nombre != null ? nombre.trim() : "";
        if (n.isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio y no puede consistir únicamente en espacios en blanco.");
        }
        if (categoria == null) {
            throw new IllegalArgumentException("Debe seleccionar una categoría para el producto.");
        }
        if (!categoria.isActiva()) {
            throw new IllegalArgumentException("La categoría seleccionada está inactiva. Seleccione una categoría activa.");
        }
        String pt = precioTexto != null ? precioTexto.trim() : "";
        if (pt.isEmpty()) {
            throw new IllegalArgumentException("El precio de venta es obligatorio.");
        }
        BigDecimal precio;
        try {
            precio = new BigDecimal(pt);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("El precio de venta debe ser un número válido (ej. 150.00).");
        }
        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio de venta debe ser estrictamente mayor que cero.");
        }
        String et = existenciaTexto != null ? existenciaTexto.trim() : "";
        if (et.isEmpty()) {
            throw new IllegalArgumentException("La existencia es obligatoria.");
        }
        int existencia;
        try {
            existencia = Integer.parseInt(et);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("La existencia debe ser un número entero válido (sin decimales ni letras y dentro del rango permitido).");
        }
        if (existencia < 0) {
            throw new IllegalArgumentException("La existencia debe ser un número entero mayor o igual que cero.");
        }

        Producto p = new Producto();
        p.setCodigo(c);
        p.setNombre(n);
        p.setCategoria(categoria);
        p.setPrecioVenta(precio);
        p.setExistencia(existencia);
        p.setActivo(true);
        return p;
    }
}
