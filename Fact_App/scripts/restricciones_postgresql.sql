-- ============================================================================
-- SCRIPT DE INTEGRIDAD Y RESTRICCIONES PARA POSTGRESQL
-- (Motor actualmente configurado en el proyecto Fact_App)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- PASO 1: CONSULTAS DE DIAGNÓSTICO PARA IDENTIFICAR DATOS INCOMPATIBLES
-- ----------------------------------------------------------------------------

-- 1.1. Categorías con nombres duplicados (ignorando mayúsculas y espacios)
SELECT LOWER(TRIM(nombre)) AS nombre_duplicado, COUNT(*) AS cantidad
FROM categoria
GROUP BY LOWER(TRIM(nombre))
HAVING COUNT(*) > 1;

-- 1.2. Productos con códigos duplicados (ignorando mayúsculas y espacios)
SELECT LOWER(TRIM(codigo)) AS codigo_duplicado, COUNT(*) AS cantidad
FROM producto
GROUP BY LOWER(TRIM(codigo))
HAVING COUNT(*) > 1;

-- 1.3. Productos con categoría inexistente
SELECT p.id, p.codigo, p.nombre, p.categoria_id
FROM producto p
LEFT JOIN categoria c ON p.categoria_id = c.id
WHERE c.id IS NULL;

-- 1.4. Productos con precio menor o igual a cero
SELECT id, codigo, nombre, precio_venta
FROM producto
WHERE precio_venta <= 0;

-- 1.5. Productos con existencia negativa
SELECT id, codigo, nombre, existencia
FROM producto
WHERE existencia < 0;


-- ----------------------------------------------------------------------------
-- PASO 2: APLICACIÓN DE RESTRICCIONES DE INTEGRIDAD EN POSTGRESQL
-- ----------------------------------------------------------------------------

-- 2.1. Índice único insensible a mayúsculas para Categoria
CREATE UNIQUE INDEX IF NOT EXISTS uq_categoria_nombre_lower 
ON categoria (LOWER(TRIM(nombre)));

-- 2.2. Unicidad del código de Producto (si no existe ya)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_producto_codigo' OR conname = 'producto_codigo_key'
    ) THEN
        ALTER TABLE producto ADD CONSTRAINT uq_producto_codigo UNIQUE (codigo);
    END IF;
END $$;

-- 2.3. Llave foránea de Producto hacia Categoria con ON DELETE RESTRICT / NO ACTION
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_producto_categoria'
    ) THEN
        ALTER TABLE producto 
        ADD CONSTRAINT fk_producto_categoria 
        FOREIGN KEY (categoria_id) REFERENCES categoria(id) 
        ON DELETE RESTRICT;
    END IF;
END $$;

-- 2.4. Restricción CHECK de precio estrictamente mayor que cero
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_producto_precio_positivo'
    ) THEN
        ALTER TABLE producto 
        ADD CONSTRAINT chk_producto_precio_positivo 
        CHECK (precio_venta > 0);
    END IF;
END $$;

-- 2.5. Restricción CHECK de existencia mayor o igual a cero
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_producto_existencia_no_negativa'
    ) THEN
        ALTER TABLE producto 
        ADD CONSTRAINT chk_producto_existencia_no_negativa 
        CHECK (existencia >= 0);
    END IF;
END $$;
