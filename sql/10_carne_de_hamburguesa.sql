-- 10: Separa la «Carne de hamburguesa» de la «Carne» (la de sabritas, tortas, nachos, chalupa, etc.).
-- Crea el insumo «Carne de hamburguesa» y pasa a él la receta de todas las hamburguesas (sencilla, con tocino, BBQ...).
-- La «Carne» de siempre se queda para los demás productos. Es seguro volver a correrlo.
-- La carne de hamburguesa nace con 0 g: después de correrlo, usa «Entrada rápida» en Insumos y recetas para ponerle lo que hay.
IF NOT EXISTS (SELECT 1 FROM dbo.Insumos WHERE Nombre = N'Carne de hamburguesa')
    INSERT dbo.Insumos (Nombre, Unidad, Existencias) VALUES (N'Carne de hamburguesa', 'g', 0);
GO
IF COL_LENGTH('dbo.Insumos', 'StockMinimo') IS NOT NULL
    EXEC (N'UPDATE dbo.Insumos SET StockMinimo = 300 WHERE Nombre = N''Carne de hamburguesa'' AND StockMinimo <> 300');
GO
DECLARE @Vieja INT = (SELECT InsumoID FROM dbo.Insumos WHERE Nombre = N'Carne');
DECLARE @Nueva INT = (SELECT InsumoID FROM dbo.Insumos WHERE Nombre = N'Carne de hamburguesa');
IF @Vieja IS NOT NULL AND @Nueva IS NOT NULL
    UPDATE r SET InsumoID = @Nueva
    FROM dbo.Recetas r
    JOIN dbo.Productos p ON p.id = r.ProductoID
    WHERE r.InsumoID = @Vieja
      AND p.Nombre_Producto COLLATE Latin1_General_CI_AI LIKE N'Hamburguesa%'
      AND NOT EXISTS (SELECT 1 FROM dbo.Recetas x WHERE x.ProductoID = r.ProductoID AND x.InsumoID = @Nueva);
GO
