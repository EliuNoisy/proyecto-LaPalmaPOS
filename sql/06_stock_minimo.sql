-- 06: stock mínimo PERSONALIZADO por producto.
-- El administrador fija el límite de cada producto desde Inventario > «Stock mínimo».
-- Cuando las existencias llegan a ese límite (o bajan de él) el sistema avisa «por reponer».
-- Los productos ya existentes quedan con 5, que era el límite general de antes. Es seguro volver a correrlo.
IF COL_LENGTH('dbo.Productos', 'StockMinimo') IS NULL
    ALTER TABLE dbo.Productos ADD StockMinimo INT NOT NULL
        CONSTRAINT DF_Productos_StockMinimo DEFAULT 5
        CONSTRAINT CK_Productos_StockMinimo CHECK (StockMinimo >= 0 AND StockMinimo <= 99999);
GO
