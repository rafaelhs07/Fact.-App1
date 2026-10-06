-- ============================================================================
-- SCRIPT DE INTEGRIDAD Y RESTRICCIONES PARA SQL SERVER
-- Práctica: Manejo de Excepciones y Validación de Operaciones
-- Módulos: Categoria y Producto
-- ============================================================================

-- ----------------------------------------------------------------------------
-- PASO 1: CONSULTAS DE DIAGNÓSTICO PARA IDENTIFICAR DATOS INCOMPATIBLES
-- (Ejecutar antes de aplicar las restricciones para no generar errores)
-- NOTA: No borre ni altere datos arbitrariamente. Si estas consultas devuelven
-- filas, deben corregirse manualmente antes del PASO 2.
-- ----------------------------------------------------------------------------

-- 1.1. Categorías con nombres duplicados (ignorando mayúsculas/minúsculas y espacios)
SELECT LOWER(LTRIM(RTRIM(nombre))) AS nombre_duplicado, COUNT(*) AS cantidad
FROM categoria
GROUP BY LOWER(LTRIM(RTRIM(nombre)))
HAVING COUNT(*) > 1;

-- 1.2. Productos con códigos duplicados (ignorando mayúsculas/minúsculas y espacios)
SELECT LOWER(LTRIM(RTRIM(codigo))) AS codigo_duplicado, COUNT(*) AS cantidad
FROM producto
GROUP BY LOWER(LTRIM(RTRIM(codigo)))
HAVING COUNT(*) > 1;

-- 1.3. Productos que hacen referencia a categorías inexistentes (violaciones de FK)
SELECT p.id, p.codigo, p.nombre, p.categoria_id
FROM producto p
LEFT JOIN categoria c ON p.categoria_id = c.id
WHERE c.id IS NULL;

-- 1.4. Productos con precio de venta menor o igual a cero
SELECT id, codigo, nombre, precio_venta
FROM producto
WHERE precio_venta <= 0;

-- 1.5. Productos con existencia negativa
SELECT id, codigo, nombre, existencia
FROM producto
WHERE existencia < 0;


-- ----------------------------------------------------------------------------
-- PASO 2: APLICACIÓN DE RESTRICCIONES DE INTEGRIDAD EN SQL SERVER
-- ----------------------------------------------------------------------------

-- 2.1. Unicidad del nombre de Categoria (insensible a mayúsculas/minúsculas)
-- Si la base de datos utiliza una colación insensible a mayúsculas (ej. SQL_Latin1_General_CP1_CI_AS):
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'uq_categoria_nombre' AND object_id = OBJECT_ID('categoria'))
BEGIN
    CREATE UNIQUE NONCLUSTERED INDEX uq_categoria_nombre
    ON categoria (nombre);
    PRINT 'Índice único uq_categoria_nombre creado exitosamente.';
END
GO

-- 2.2. Unicidad del código de Producto
IF NOT EXISTS (SELECT * FROM sys.key_constraints WHERE name = 'uq_producto_codigo' AND parent_object_id = OBJECT_ID('producto'))
BEGIN
    ALTER TABLE producto
    ADD CONSTRAINT uq_producto_codigo UNIQUE (codigo);
    PRINT 'Restricción de unicidad uq_producto_codigo agregada exitosamente.';
END
GO

-- 2.3. Llave foránea de Producto hacia Categoria que impida eliminación en cascada (ON DELETE NO ACTION)
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'fk_producto_categoria' AND parent_object_id = OBJECT_ID('producto'))
BEGIN
    ALTER TABLE producto
    ADD CONSTRAINT fk_producto_categoria
    FOREIGN KEY (categoria_id) REFERENCES categoria(id)
    ON DELETE NO ACTION;
    PRINT 'Llave foránea fk_producto_categoria agregada exitosamente.';
END
GO

-- 2.4. Restricción CHECK: precio de venta estrictamente mayor que cero
IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'chk_producto_precio_positivo' AND parent_object_id = OBJECT_ID('producto'))
BEGIN
    ALTER TABLE producto
    ADD CONSTRAINT chk_producto_precio_positivo
    CHECK (precio_venta > 0);
    PRINT 'Restricción CHECK chk_producto_precio_positivo agregada exitosamente.';
END
GO

-- 2.5. Restricción CHECK: existencia no negativa (mayor o igual que cero)
IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'chk_producto_existencia_no_negativa' AND parent_object_id = OBJECT_ID('producto'))
BEGIN
    ALTER TABLE producto
    ADD CONSTRAINT chk_producto_existencia_no_negativa
    CHECK (existencia >= 0);
    PRINT 'Restricción CHECK chk_producto_existencia_no_negativa agregada exitosamente.';
END
GO
