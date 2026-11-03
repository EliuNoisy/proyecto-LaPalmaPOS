-- 05: permite ELIMINAR cualquier producto, incluso los que ya tienen ventas.
-- Un producto con ventas no se borra de la base (para no perder reportes ni tickets):
-- se marca como Eliminado y desaparece del inventario, de la caja y de las recetas.
-- Es seguro volver a correrlo.
IF COL_LENGTH('dbo.Productos', 'Eliminado') IS NULL
    ALTER TABLE dbo.Productos ADD Eliminado BIT NOT NULL CONSTRAINT DF_Productos_Eliminado DEFAULT 0;
GO
