-- =====================================================================
-- LA PALMA POS - QUITAR TODO LO NUEVO (deshace sql/00_instalar_todo.sql)
-- =====================================================================
-- QUÉ BORRA:
--   * Tablas: Insumos, Recetas, Configuracion, Historial
--   * Procedimientos: sp_SembrarInsumos, sp_SembrarRecetas, sp_RestaurarSistema
--   * Columnas de Productos: Tipo, Bolas, ContenedorID, Eliminado
--   * Columnas de Ventas: MetodoPago, MontoTarjeta, Empleado
-- QUÉ SE PIERDE: insumos, recetas, historial de cambios y, en las ventas ya registradas,
--   el método de pago (tarjeta/mixto) y quién atendió. Los productos, empleados, totales,
--   fechas y detalles de venta NO se tocan.
-- RECOMENDADO ANTES: un respaldo. Quita los dos guiones de la línea siguiente y cambia la ruta:
-- BACKUP DATABASE [RefresqueríaLaPalma] TO DISK = N'C:\Respaldos\RefresqueriaLaPalma_antes.bak' WITH INIT;
--
-- CÓMO USARLO:
--   1) Ejecútalo tal cual (F5): es una SIMULACIÓN, no borra nada y muestra la lista de pasos.
--   2) Si estás de acuerdo, cambia  @CONFIRMAR = 0  por  @CONFIRMAR = 1  y ejecútalo otra vez.
--   3) Todo corre en una sola transacción: si algo falla, no se cambia nada.
-- Después de quitarlo, el programa Java sigue funcionando como antes (solo efectivo, sin insumos).
-- Para volver a instalarlo: ejecuta sql/00_instalar_todo.sql.
-- =====================================================================
USE [RefresqueríaLaPalma];
GO
SET NOCOUNT ON;

DECLARE @CONFIRMAR BIT = 0;                  -- 0 = solo simular, 1 = borrar de verdad
DECLARE @QUITAR_CATEGORIA_IMAGEN BIT = 0;    -- 1 = también quita Categoria e Imagen de Productos (las agregó el script 04)

DECLARE @acc TABLE (id INT IDENTITY(1,1), txt NVARCHAR(500));

-- 1) Procedimientos
INSERT @acc (txt)
SELECT N'DROP PROCEDURE dbo.' + QUOTENAME(name)
FROM sys.procedures
WHERE schema_id = SCHEMA_ID('dbo') AND name IN (N'sp_RestaurarSistema', N'sp_SembrarRecetas', N'sp_SembrarInsumos');

-- 2) Historial (su trigger se borra con la tabla)
INSERT @acc (txt) SELECT N'DROP TABLE dbo.Historial' WHERE OBJECT_ID('dbo.Historial') IS NOT NULL;

-- 3) Restricciones (default, check, llaves foráneas) que dependen de las columnas nuevas
INSERT @acc (txt)
SELECT DISTINCT N'ALTER TABLE dbo.' + QUOTENAME(OBJECT_NAME(c.t)) + N' DROP CONSTRAINT ' + QUOTENAME(c.n)
FROM (
    SELECT parent_object_id AS t, parent_column_id AS col, name AS n FROM sys.default_constraints
    UNION ALL
    SELECT parent_object_id, parent_column_id, name FROM sys.check_constraints WHERE parent_column_id <> 0
    UNION ALL
    SELECT fk.parent_object_id, fkc.parent_column_id, fk.name
    FROM sys.foreign_keys fk JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id = fk.object_id
) c
JOIN sys.columns k ON k.object_id = c.t AND k.column_id = c.col
WHERE OBJECT_SCHEMA_NAME(c.t) = N'dbo' AND (
       (OBJECT_NAME(c.t) = N'Productos' AND k.name IN (N'Tipo', N'Bolas', N'ContenedorID', N'Eliminado'))
    OR (OBJECT_NAME(c.t) = N'Ventas'    AND k.name IN (N'MetodoPago', N'MontoTarjeta', N'Empleado'))
    OR (@QUITAR_CATEGORIA_IMAGEN = 1 AND OBJECT_NAME(c.t) = N'Productos' AND k.name IN (N'Categoria', N'Imagen')));

-- 4) Tablas nuevas (Recetas primero porque depende de Insumos y Productos)
INSERT @acc (txt)
SELECT N'DROP TABLE dbo.' + QUOTENAME(v.t)
FROM (VALUES (N'Recetas', 1), (N'Configuracion', 2), (N'Insumos', 3)) v(t, o)
WHERE OBJECT_ID(N'dbo.' + v.t) IS NOT NULL
ORDER BY v.o;

-- 5) Columnas nuevas
INSERT @acc (txt)
SELECT N'ALTER TABLE dbo.' + QUOTENAME(v.t) + N' DROP COLUMN ' + QUOTENAME(v.c)
FROM (VALUES (N'Productos', N'Tipo'), (N'Productos', N'Bolas'), (N'Productos', N'ContenedorID'), (N'Productos', N'Eliminado'),
             (N'Ventas', N'MetodoPago'), (N'Ventas', N'MontoTarjeta'), (N'Ventas', N'Empleado')) v(t, c)
WHERE COL_LENGTH(N'dbo.' + v.t, v.c) IS NOT NULL;

IF @QUITAR_CATEGORIA_IMAGEN = 1
    INSERT @acc (txt)
    SELECT N'ALTER TABLE dbo.Productos DROP COLUMN ' + QUOTENAME(v.c)
    FROM (VALUES (N'Categoria'), (N'Imagen')) v(c)
    WHERE COL_LENGTH(N'dbo.Productos', v.c) IS NOT NULL;

-- 6) Ejecutar o simular
IF NOT EXISTS (SELECT 1 FROM @acc)
BEGIN
    PRINT N'No hay nada que quitar: la base ya está sin los cambios nuevos.';
END
ELSE IF @CONFIRMAR = 0
BEGIN
    PRINT N'SIMULACIÓN: no se borró nada. Cambia @CONFIRMAR a 1 para ejecutar estos pasos.';
    SELECT id AS Paso, txt AS [Se ejecutaría] FROM @acc ORDER BY id;
END
ELSE
BEGIN
    BEGIN TRY
        BEGIN TRAN;
        DECLARE @i INT = 0, @s NVARCHAR(500);
        WHILE 1 = 1
        BEGIN
            SELECT TOP 1 @i = id, @s = txt FROM @acc WHERE id > @i ORDER BY id;
            IF @@ROWCOUNT = 0 BREAK;
            EXEC (@s);
        END
        COMMIT;
        PRINT N'Listo: se quitaron los cambios nuevos. Productos, empleados y ventas siguen intactos.';
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK;
        PRINT N'ERROR: ' + ERROR_MESSAGE() + N' -- No se cambió nada.';
    END CATCH
END

-- 7) Estado final
SELECT Pieza, CASE WHEN Existe = 1 THEN 'AÚN EXISTE' ELSE 'QUITADO' END AS Estado FROM (VALUES
 ('Tabla Insumos',            CASE WHEN OBJECT_ID('dbo.Insumos')       IS NULL THEN 0 ELSE 1 END),
 ('Tabla Recetas',            CASE WHEN OBJECT_ID('dbo.Recetas')       IS NULL THEN 0 ELSE 1 END),
 ('Tabla Configuracion',      CASE WHEN OBJECT_ID('dbo.Configuracion') IS NULL THEN 0 ELSE 1 END),
 ('Tabla Historial',          CASE WHEN OBJECT_ID('dbo.Historial')     IS NULL THEN 0 ELSE 1 END),
 ('Productos.Tipo',           CASE WHEN COL_LENGTH('dbo.Productos','Tipo')         IS NULL THEN 0 ELSE 1 END),
 ('Productos.Bolas',          CASE WHEN COL_LENGTH('dbo.Productos','Bolas')        IS NULL THEN 0 ELSE 1 END),
 ('Productos.ContenedorID',   CASE WHEN COL_LENGTH('dbo.Productos','ContenedorID') IS NULL THEN 0 ELSE 1 END),
 ('Productos.Eliminado',      CASE WHEN COL_LENGTH('dbo.Productos','Eliminado')    IS NULL THEN 0 ELSE 1 END),
 ('Ventas.MetodoPago',        CASE WHEN COL_LENGTH('dbo.Ventas','MetodoPago')      IS NULL THEN 0 ELSE 1 END),
 ('Ventas.MontoTarjeta',      CASE WHEN COL_LENGTH('dbo.Ventas','MontoTarjeta')    IS NULL THEN 0 ELSE 1 END),
 ('Ventas.Empleado',          CASE WHEN COL_LENGTH('dbo.Ventas','Empleado')        IS NULL THEN 0 ELSE 1 END),
 ('Procedimiento Restaurar',  CASE WHEN OBJECT_ID('dbo.sp_RestaurarSistema') IS NULL THEN 0 ELSE 1 END)
) v(Pieza, Existe);
GO
