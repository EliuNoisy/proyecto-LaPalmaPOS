-- 07: stock mínimo PERSONALIZADO por insumo (igual que el de los productos, sql/06).
-- El administrador fija el límite de cada insumo desde «Insumos y recetas» > «Stock mínimo».
-- Cuando las existencias llegan a ese límite (o bajan de él) el sistema avisa «por reponer».
-- Los insumos que ya existían quedan con el límite general de antes: 5 si se cuentan por pieza y 300 si se pesan en gramos.
-- Es seguro volver a correrlo.
IF COL_LENGTH('dbo.Insumos', 'StockMinimo') IS NULL
BEGIN
    ALTER TABLE dbo.Insumos ADD StockMinimo INT NOT NULL
        CONSTRAINT DF_Insumos_StockMinimo DEFAULT 5
        CONSTRAINT CK_Insumos_StockMinimo CHECK (StockMinimo >= 0 AND StockMinimo <= 99999);
    EXEC (N'UPDATE dbo.Insumos SET StockMinimo = 300 WHERE Unidad <> ''pieza''');
END
GO
