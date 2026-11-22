-- 08: Bolsas de botana «solas» y «preparadas» (Cheetos, Chips, Doritos, Tostitos y Sabritas).
-- En la caja salen DOS productos por marca con precio distinto (ej. «Doritos (solos)» y «Doritos preparados»),
-- pero los dos descuentan 1 pieza del MISMO insumo «Bolsa de Doritos». Así el inventario nunca se desfasa
-- por vender «solo» o «preparado». Las «Sabritas con carne / queso / salchicha...» también gastan 1 «Bolsa de Sabritas».
-- Ejecutar DESPUÉS de 02 y 03 (o todo junto con 00_instalar_todo.sql). Es seguro volver a correrlo:
-- no duplica productos ni recetas y no toca los precios que ya hayas puesto.
-- Los productos nuevos nacen con el precio de las bolsas «solas»: cámbialo en Inventario > Modificar producto.
IF COL_LENGTH('dbo.Productos', 'Categoria') IS NULL
    ALTER TABLE dbo.Productos ADD Categoria NVARCHAR(30) NOT NULL CONSTRAINT DF_Productos_Categoria DEFAULT 'Comida';
GO
IF COL_LENGTH('dbo.Productos', 'Eliminado') IS NULL
    ALTER TABLE dbo.Productos ADD Eliminado BIT NOT NULL CONSTRAINT DF_Productos_Eliminado DEFAULT 0;
GO

CREATE OR ALTER PROCEDURE dbo.sp_BolsasSolasPreparadas AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @C NVARCHAR(40) = N'Latin1_General_CI_AI';

    DECLARE @M TABLE (Marca NVARCHAR(20) PRIMARY KEY, Insumo NVARCHAR(30), Sola NVARCHAR(40), Prep NVARCHAR(40));
    INSERT @M VALUES
      (N'Cheetos', N'Bolsa de Cheetos', N'Cheetos (solos)',  N'Cheetos preparados'),
      (N'Chips',   N'Bolsa de Chips',   N'Chips (solos)',    N'Chips preparados'),
      (N'Doritos', N'Bolsa de Doritos', N'Doritos (solos)',  N'Doritos preparados'),
      (N'Tostitos',N'Bolsa de Tostitos',N'Tostitos (solos)', N'Tostitos preparados'),
      (N'Sabritas',N'Bolsa de Sabritas',N'Sabritas (solas)', N'Sabritas preparadas');

    -- 1) El insumo «Bolsa de ...» de cada marca (si ya lo tienes, no se toca)
    INSERT dbo.Insumos (Nombre, Unidad, Existencias)
    SELECT m.Insumo, 'pieza', 0 FROM @M m
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Insumos i WHERE i.Nombre = m.Insumo);

    -- 2) Los productos «solos» y «preparados» de cada marca (solo se crean los que falten)
    DECLARE @Base DECIMAL(10,2) = ISNULL((SELECT MAX(p.Precio) FROM dbo.Productos p
        WHERE p.Eliminado = 0 AND p.Nombre_Producto COLLATE Latin1_General_CI_AI LIKE N'%solo%'), 20);

    INSERT dbo.Productos (Nombre_Producto, Precio, Cantidad, Categoria)
    SELECT v.Nombre, ISNULL(v.PrecioSolo, @Base), 0, N'Snacks'
    FROM (
        SELECT m.Marca, m.Sola AS Nombre, N'sola' AS Clase,
               (SELECT MAX(p.Precio) FROM dbo.Productos p WHERE p.Eliminado = 0 AND p.Nombre_Producto COLLATE Latin1_General_CI_AI LIKE m.Marca + N'%sol[ao]%') AS PrecioSolo
        FROM @M m
        UNION ALL
        -- «preparados»: solo existe «Sabritas preparadas»; en la caja pregunta la marca o sabor de la bolsa
        -- (ya no se crean «Cheetos/Chips/Doritos/Tostitos preparados» por separado)
        SELECT m.Marca, m.Prep, N'prep',
               (SELECT MAX(p.Precio) FROM dbo.Productos p WHERE p.Eliminado = 0 AND p.Nombre_Producto COLLATE Latin1_General_CI_AI LIKE m.Marca + N'%sol[ao]%')
        FROM @M m WHERE m.Marca = N'Sabritas'
    ) v
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.Productos p
        WHERE p.Eliminado = 0 AND p.Nombre_Producto COLLATE Latin1_General_CI_AI LIKE
              v.Marca + CASE v.Clase WHEN N'sola' THEN N'%sol[ao]%' ELSE N'%preparad%' END);

    -- 3) Todos los productos de bolsa (solos / preparados) y las «Sabritas con ...» llevan la bolsa en su receta
    DECLARE @P TABLE (ProductoID INT PRIMARY KEY, InsumoID INT, EraDirecto BIT, Cant INT);
    INSERT @P (ProductoID, InsumoID, EraDirecto, Cant)
    SELECT p.id, i.InsumoID,
           CASE WHEN p.Tipo = 'directo' AND NOT EXISTS (SELECT 1 FROM dbo.Recetas r WHERE r.ProductoID = p.id) THEN 1 ELSE 0 END,
           p.Cantidad
    FROM dbo.Productos p
    JOIN @M m ON p.Nombre_Producto COLLATE Latin1_General_CI_AI LIKE m.Marca + N'%sol[ao]%'
              OR p.Nombre_Producto COLLATE Latin1_General_CI_AI LIKE m.Marca + N'%preparad%'
              OR (m.Marca = N'Sabritas' AND p.Nombre_Producto COLLATE Latin1_General_CI_AI LIKE N'Sabritas con%')
    JOIN dbo.Insumos i ON i.Nombre = m.Insumo
    WHERE p.Eliminado = 0 AND p.Tipo <> 'nieve';

    -- si el administrador ya tenía bolsas contadas en un producto «solo/preparado» directo, pasan al insumo (una sola vez)
    UPDATE i SET Existencias = CASE WHEN i.Existencias + x.Total > 99999 THEN 99999 ELSE i.Existencias + x.Total END
    FROM dbo.Insumos i
    JOIN (SELECT p.InsumoID, SUM(p.Cant) AS Total FROM @P p JOIN dbo.Productos pr ON pr.id = p.ProductoID
          WHERE p.EraDirecto = 1 AND pr.Nombre_Producto COLLATE Latin1_General_CI_AI NOT LIKE N'Sabritas con%'
          GROUP BY p.InsumoID) x ON x.InsumoID = i.InsumoID;
    UPDATE pr SET Cantidad = 0
    FROM dbo.Productos pr JOIN @P p ON p.ProductoID = pr.id
    WHERE p.EraDirecto = 1 AND pr.Nombre_Producto COLLATE Latin1_General_CI_AI NOT LIKE N'Sabritas con%';

    INSERT dbo.Recetas (ProductoID, InsumoID, Cantidad)
    SELECT p.ProductoID, p.InsumoID, 1 FROM @P p
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Recetas r WHERE r.ProductoID = p.ProductoID AND r.InsumoID = p.InsumoID);

    UPDATE pr SET Tipo = 'preparado'
    FROM dbo.Productos pr JOIN @P p ON p.ProductoID = pr.id
    WHERE pr.Tipo = 'directo';
END
GO

EXEC dbo.sp_BolsasSolasPreparadas;
GO
