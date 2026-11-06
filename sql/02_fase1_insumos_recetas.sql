-- FASE 1: insumos, recetas, tipos de producto y nieve (según prototipo HTML)
-- Ejecutar UNA vez en la base de datos de La Palma (se puede volver a correr).
-- Después de correrlo, todos los productos existentes quedan como 'directo'
-- (siguen descontando de Productos.Cantidad igual que antes).

IF OBJECT_ID('dbo.Insumos') IS NULL
CREATE TABLE dbo.Insumos (
    InsumoID    INT IDENTITY(1,1) PRIMARY KEY,
    Nombre      NVARCHAR(30)  NOT NULL UNIQUE,
    Unidad      NVARCHAR(6)   NOT NULL CHECK (Unidad IN ('pieza','g')),
    Existencias DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (Existencias >= 0 AND Existencias <= 99999)
);
GO

IF OBJECT_ID('dbo.Recetas') IS NULL
CREATE TABLE dbo.Recetas (
    ProductoID INT NOT NULL,
    InsumoID   INT NOT NULL,
    Cantidad   DECIMAL(10,2) NOT NULL CHECK (Cantidad > 0 AND Cantidad <= 9999),
    CONSTRAINT PK_Recetas PRIMARY KEY (ProductoID, InsumoID),
    CONSTRAINT FK_Recetas_Producto FOREIGN KEY (ProductoID) REFERENCES dbo.Productos(id) ON DELETE CASCADE,
    CONSTRAINT FK_Recetas_Insumo   FOREIGN KEY (InsumoID)   REFERENCES dbo.Insumos(InsumoID)
);
GO

IF OBJECT_ID('dbo.Configuracion') IS NULL
CREATE TABLE dbo.Configuracion (
    Clave NVARCHAR(30)  NOT NULL PRIMARY KEY,
    Valor DECIMAL(10,2) NOT NULL
);
GO
IF NOT EXISTS (SELECT 1 FROM dbo.Configuracion WHERE Clave = 'GramosBola')
    INSERT INTO dbo.Configuracion (Clave, Valor) VALUES ('GramosBola', 60);   -- peso de una bola de nieve
GO

-- Tipo de producto: directo | preparado | nieve
IF COL_LENGTH('dbo.Productos', 'Tipo') IS NULL
    ALTER TABLE dbo.Productos ADD Tipo NVARCHAR(10) NOT NULL
        CONSTRAINT DF_Productos_Tipo DEFAULT 'directo'
        CONSTRAINT CK_Productos_Tipo CHECK (Tipo IN ('directo','preparado','nieve'));
GO
IF COL_LENGTH('dbo.Productos', 'Bolas') IS NULL
    ALTER TABLE dbo.Productos ADD Bolas INT NULL;
GO
IF COL_LENGTH('dbo.Productos', 'ContenedorID') IS NULL
    ALTER TABLE dbo.Productos ADD ContenedorID INT NULL
        CONSTRAINT FK_Productos_Contenedor REFERENCES dbo.Insumos(InsumoID);
GO

-- Insumos iniciales del prototipo (solo se agregan si no existen por nombre)
;WITH s(Nombre, Unidad, Existencias) AS (
    SELECT * FROM (VALUES
    (N'Pan de torta','pieza',40),(N'Carne','g',3000),(N'Lechuga','g',800),(N'Tomate','g',600),
    (N'Queso','g',900),(N'Jamón','pieza',100),(N'Totopos','g',1200),(N'Pan de hamburguesa','pieza',30),
    (N'Tocino','g',600),(N'Salsa BBQ','g',500),(N'Papas','g',6000),(N'Tortilla para chalupa','pieza',60),
    (N'Frijoles','g',1500),(N'Salsa roja','g',800),(N'Cacahuate','g',1500),(N'Clamato','g',3000),
    (N'Nieve','g',8000),(N'Vaso chico','pieza',50),(N'Vaso mediano','pieza',50),(N'Vaso grande','pieza',40),
    (N'Cono de oblea','pieza',40),(N'Cono de galleta','pieza',40),(N'Pepino','g',1500),(N'Jícama','g',1500),
    (N'Fresa','g',2000),(N'Crema','g',1500),(N'Hielo','g',10000),(N'Jarabe','g',2000),
    (N'Churritos','g',1500),(N'Bolsa de Sabritas','pieza',40)
    ) v(Nombre, Unidad, Existencias)
)
INSERT INTO dbo.Insumos (Nombre, Unidad, Existencias)
SELECT s.Nombre, s.Unidad, s.Existencias FROM s
WHERE NOT EXISTS (SELECT 1 FROM dbo.Insumos i WHERE i.Nombre = s.Nombre);
GO
