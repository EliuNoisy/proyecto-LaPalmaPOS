-- OPCIONAL: tu tabla dbo.Productos solo tiene id, Nombre_Producto, Precio y Cantidad,
-- pero la pantalla "Agregar producto" del Java también guarda Categoria e Imagen.
-- Este script agrega esas dos columnas si no existen (no cambia datos).
IF COL_LENGTH('dbo.Productos', 'Categoria') IS NULL
    ALTER TABLE dbo.Productos ADD Categoria NVARCHAR(30) NOT NULL CONSTRAINT DF_Productos_Categoria DEFAULT 'Comida';
GO
IF COL_LENGTH('dbo.Productos', 'Imagen') IS NULL
    ALTER TABLE dbo.Productos ADD Imagen NVARCHAR(260) NULL;
GO
