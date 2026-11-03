-- Pago mixto (Efectivo / Tarjeta / Mixto) - ejecutar UNA vez en la base de datos de La Palma
-- Es seguro volver a correrlo: solo agrega las columnas si no existen.
IF COL_LENGTH('dbo.Ventas', 'MetodoPago') IS NULL
    ALTER TABLE dbo.Ventas ADD MetodoPago NVARCHAR(10) NOT NULL
        CONSTRAINT DF_Ventas_MetodoPago DEFAULT 'Efectivo';
GO
IF COL_LENGTH('dbo.Ventas', 'MontoTarjeta') IS NULL
    ALTER TABLE dbo.Ventas ADD MontoTarjeta DECIMAL(10,2) NOT NULL
        CONSTRAINT DF_Ventas_MontoTarjeta DEFAULT 0;
GO
-- Las ventas anteriores quedan como 'Efectivo' con MontoTarjeta = 0.
