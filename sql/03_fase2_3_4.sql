-- FASES 2-4: historial, empleado en ventas, recetas iniciales por nombre de producto y restauración.
-- Ejecutar DESPUÉS de 01_pago_mixto.sql y 02_fase1_insumos_recetas.sql. Se puede volver a correr.

-- 1) Historial de cambios (no se puede modificar ni borrar)
IF OBJECT_ID('dbo.Historial') IS NULL
CREATE TABLE dbo.Historial (
    HistorialID INT IDENTITY(1,1) PRIMARY KEY,
    Fecha       DATETIME2(0)  NOT NULL DEFAULT SYSDATETIME(),
    Usuario     NVARCHAR(120) NOT NULL,
    Puesto      NVARCHAR(20)  NULL,
    Accion      NVARCHAR(20)  NOT NULL,
    Objeto      NVARCHAR(120) NOT NULL,
    Detalle     NVARCHAR(600) NULL
);
GO
IF OBJECT_ID('dbo.TR_Historial_Inmutable') IS NULL
EXEC('CREATE TRIGGER dbo.TR_Historial_Inmutable ON dbo.Historial INSTEAD OF UPDATE, DELETE AS
BEGIN RAISERROR(N''El historial no se puede modificar ni borrar.'',16,1); END');
GO

-- 2) Quién atendió la venta (para tickets y reportes)
IF COL_LENGTH('dbo.Ventas', 'Empleado') IS NULL
    ALTER TABLE dbo.Ventas ADD Empleado NVARCHAR(120) NULL;
GO

-- 3) Insumos iniciales del prototipo
CREATE OR ALTER PROCEDURE dbo.sp_SembrarInsumos AS
BEGIN
    SET NOCOUNT ON;
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
        ) v(Nombre, Unidad, Existencias))
    INSERT INTO dbo.Insumos (Nombre, Unidad, Existencias)
    SELECT s.Nombre, s.Unidad, s.Existencias FROM s
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Insumos i WHERE i.Nombre = s.Nombre);
END
GO

-- 4) Tipos y recetas iniciales según el prototipo, buscando los productos por nombre
--    (ignora acentos y mayúsculas). Solo toca productos 'directo' que aún no tienen receta,
--    así que no pisa lo que ya configuraste. Los productos que no coincidan quedan como están.
CREATE OR ALTER PROCEDURE dbo.sp_SembrarRecetas AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @Rec TABLE (Base VARCHAR(4), Insumo NVARCHAR(30), Cant DECIMAL(10,2));
    INSERT @Rec VALUES
    ('TJ',N'Pan de torta',1),('TJ',N'Jamón',2),('TJ',N'Lechuga',15),('TJ',N'Tomate',20),
    ('TP',N'Pan de torta',1),('TP',N'Carne',120),('TP',N'Lechuga',15),('TP',N'Tomate',20),
    ('TE',N'Pan de torta',1),('TE',N'Jamón',2),('TE',N'Carne',100),('TE',N'Queso',30),('TE',N'Lechuga',15),('TE',N'Tomate',20),
    ('HS',N'Pan de hamburguesa',1),('HS',N'Carne',150),('HS',N'Lechuga',30),('HS',N'Tomate',20),('HS',N'Queso',20),
    ('HT',N'Pan de hamburguesa',1),('HT',N'Carne',150),('HT',N'Tocino',30),('HT',N'Queso',20),('HT',N'Lechuga',30),
    ('HB',N'Pan de hamburguesa',1),('HB',N'Carne',150),('HB',N'Salsa BBQ',30),('HB',N'Queso',20),('HB',N'Lechuga',30),
    ('NA',N'Totopos',80),('NA',N'Carne',100),('NA',N'Queso',30),
    ('PA',N'Papas',150),
    ('CH',N'Tortilla para chalupa',1),('CH',N'Frijoles',30),('CH',N'Carne',40),('CH',N'Lechuga',10),('CH',N'Queso',15),('CH',N'Salsa roja',10),
    ('PE',N'Clamato',150),('PE',N'Cacahuate',20),('PE',N'Pepino',60),('PE',N'Tomate',30),
    ('FG',N'Fresa',160),('FG',N'Crema',80),('FG',N'Vaso grande',1),
    ('FC',N'Fresa',80),('FC',N'Crema',40),('FC',N'Vaso chico',1),
    ('FNG',N'Fresa',160),('FNG',N'Crema',80),('FNG',N'Vaso grande',1),('FNG',N'Nieve',120),
    ('FNC',N'Fresa',80),('FNC',N'Crema',40),('FNC',N'Vaso chico',1),('FNC',N'Nieve',60),
    ('RA',N'Hielo',200),('RA',N'Jarabe',50),
    ('TO',N'Totopos',80),('TO',N'Cacahuate',20),('TO',N'Pepino',40),('TO',N'Jícama',40),('TO',N'Salsa roja',20),
    ('CL',N'Churritos',80),('CL',N'Pepino',40),('CL',N'Jícama',40),('CL',N'Cacahuate',20),('CL',N'Salsa roja',20),
    ('SC',N'Bolsa de Sabritas',1),('SC',N'Carne',100),('SC',N'Queso',20),('SC',N'Salsa roja',15),
    ('SQ',N'Bolsa de Sabritas',1),('SQ',N'Queso',50),('SQ',N'Salsa roja',15),
    ('OB',N'Tortilla para chalupa',1),('OB',N'Carne',80),('OB',N'Queso',30),('OB',N'Lechuga',10),('OB',N'Salsa roja',10);

    DECLARE @Pat TABLE (Orden INT IDENTITY(1,1), Pat NVARCHAR(80), Tipo VARCHAR(10), Base VARCHAR(4),
                        Papas BIT, Mult DECIMAL(4,2), Bolas INT, Cont NVARCHAR(30));
    INSERT @Pat (Pat,Tipo,Base,Papas,Mult,Bolas,Cont) VALUES
    (N'Torta de jam%papas%','preparado','TJ',1,1,NULL,NULL),(N'Torta de jam%','preparado','TJ',0,1,NULL,NULL),
    (N'Torta de pierna%papas%','preparado','TP',1,1,NULL,NULL),(N'Torta de pierna%','preparado','TP',0,1,NULL,NULL),
    (N'Torta especial%papas%','preparado','TE',1,1,NULL,NULL),(N'Torta especial%','preparado','TE',0,1,NULL,NULL),
    (N'Hamburguesa sencilla%papas%','preparado','HS',1,1,NULL,NULL),(N'Hamburguesa sencilla%','preparado','HS',0,1,NULL,NULL),
    (N'Hamburguesa con tocino%papas%','preparado','HT',1,1,NULL,NULL),(N'Hamburguesa con tocino%','preparado','HT',0,1,NULL,NULL),
    (N'Hamburguesa BBQ%papas%','preparado','HB',1,1,NULL,NULL),(N'Hamburguesa BBQ%','preparado','HB',0,1,NULL,NULL),
    (N'Nachos%grande%','preparado','NA',0,1.5,NULL,NULL),(N'Nachos%','preparado','NA',0,1,NULL,NULL),
    (N'Orden de papas%','preparado','PA',0,1,NULL,NULL),(N'Papas','preparado','PA',0,1,NULL,NULL),
    (N'Chalupa%','preparado','CH',0,1,NULL,NULL),(N'Pepihuate%','preparado','PE',0,1,NULL,NULL),
    (N'Fresas con crema y nieve grande%','preparado','FNG',0,1,NULL,NULL),
    (N'Fresas con crema y nieve chica%','preparado','FNC',0,1,NULL,NULL),
    (N'Fresas con crema grande%','preparado','FG',0,1,NULL,NULL),
    (N'Fresas con crema chica%','preparado','FC',0,1,NULL,NULL),
    (N'Fresas con crema%','preparado','FG',0,1,NULL,NULL),
    (N'Nieve%vaso%chic%','nieve',NULL,0,1,1,N'Vaso chico'),
    (N'Nieve%vaso%median%','nieve',NULL,0,1,2,N'Vaso mediano'),
    (N'Nieve%vaso%grande%','nieve',NULL,0,1,3,N'Vaso grande'),
    (N'Nieve%cono%galleta%','nieve',NULL,0,1,2,N'Cono de galleta'),
    (N'Nieve%cono%oblea%','nieve',NULL,0,1,1,N'Cono de oblea'),
    (N'Raspado%','preparado','RA',0,1,NULL,NULL),
    (N'Tostilocos%','preparado','TO',0,1,NULL,NULL),
    (N'Churros%','preparado','CL',0,1,NULL,NULL),
    (N'Sabritas%carne%','preparado','SC',0,1,NULL,NULL),
    (N'Sabritas%salchicha%','preparado','SC',0,1,NULL,NULL),
    (N'Sabritas%queso%','preparado','SQ',0,1,NULL,NULL),
    (N'Obispos%','preparado','OB',0,1,NULL,NULL);

    DECLARE @M TABLE (ProductoID INT PRIMARY KEY, Orden INT);
    INSERT @M (ProductoID, Orden)
    SELECT p.id, MIN(pt.Orden)
    FROM dbo.Productos p
    JOIN @Pat pt ON p.Nombre_Producto COLLATE Latin1_General_CI_AI LIKE pt.Pat COLLATE Latin1_General_CI_AI
    WHERE p.Tipo = 'directo' AND NOT EXISTS (SELECT 1 FROM dbo.Recetas r WHERE r.ProductoID = p.id)
    GROUP BY p.id;

    -- nieve: bolas + vaso/cono
    UPDATE p SET Tipo = 'nieve', Bolas = pt.Bolas, ContenedorID = i.InsumoID
    FROM dbo.Productos p
    JOIN @M m ON m.ProductoID = p.id
    JOIN @Pat pt ON pt.Orden = m.Orden AND pt.Tipo = 'nieve'
    JOIN dbo.Insumos i ON i.Nombre = pt.Cont;

    -- preparados: receta base (x multiplicador)
    INSERT dbo.Recetas (ProductoID, InsumoID, Cantidad)
    SELECT m.ProductoID, i.InsumoID, CAST(ROUND(r.Cant * pt.Mult, 2) AS DECIMAL(10,2))
    FROM @M m
    JOIN @Pat pt ON pt.Orden = m.Orden AND pt.Tipo = 'preparado'
    JOIN @Rec r ON r.Base = pt.Base
    JOIN dbo.Insumos i ON i.Nombre = r.Insumo;

    -- complemento de papas (100 g por pieza)
    INSERT dbo.Recetas (ProductoID, InsumoID, Cantidad)
    SELECT m.ProductoID, i.InsumoID, 100
    FROM @M m
    JOIN @Pat pt ON pt.Orden = m.Orden AND pt.Papas = 1
    JOIN dbo.Insumos i ON i.Nombre = N'Papas'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Recetas x WHERE x.ProductoID = m.ProductoID AND x.InsumoID = i.InsumoID);

    UPDATE p SET Tipo = 'preparado'
    FROM dbo.Productos p
    WHERE p.Tipo = 'directo' AND EXISTS (SELECT 1 FROM dbo.Recetas r WHERE r.ProductoID = p.id);

    -- Productos que quedaron sin configurar (para revisar en la pantalla Recetas)
    SELECT p.id, p.Nombre_Producto AS [Producto sin receta ni nivel (queda como directo)]
    FROM dbo.Productos p WHERE p.Tipo = 'directo' ORDER BY p.Nombre_Producto;
END
GO

EXEC dbo.sp_SembrarInsumos;
EXEC dbo.sp_SembrarRecetas;
GO

-- 5) Restaurar sistema (botón del administrador): borra ventas y regresa insumos y recetas a los
--    valores iniciales. NO borra productos ni empleados y conserva el historial.
CREATE OR ALTER PROCEDURE dbo.sp_RestaurarSistema AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRAN;
        DECLARE @t TABLE (id INT IDENTITY, stmt NVARCHAR(400));
        INSERT @t (stmt)
        SELECT N'DELETE FROM ' + QUOTENAME(OBJECT_SCHEMA_NAME(fk.parent_object_id)) + N'.' + QUOTENAME(OBJECT_NAME(fk.parent_object_id))
        FROM sys.foreign_keys fk
        WHERE fk.referenced_object_id = OBJECT_ID('dbo.Ventas') AND fk.parent_object_id <> OBJECT_ID('dbo.Ventas');
        DECLARE @pass INT = 0, @i INT, @s NVARCHAR(400);
        WHILE @pass < 5
        BEGIN
            SET @pass += 1; SET @i = 0;
            WHILE 1 = 1
            BEGIN
                SELECT TOP 1 @i = id, @s = stmt FROM @t WHERE id > @i ORDER BY id;
                IF @@ROWCOUNT = 0 BREAK;
                BEGIN TRY EXEC (@s); END TRY BEGIN CATCH END CATCH
            END
        END
        DELETE FROM dbo.Ventas;

        DELETE FROM dbo.Recetas;
        UPDATE dbo.Productos SET Tipo = 'directo', Bolas = NULL, ContenedorID = NULL;
        DELETE FROM dbo.Insumos;
        UPDATE dbo.Configuracion SET Valor = 60 WHERE Clave = 'GramosBola';
        EXEC dbo.sp_SembrarInsumos;
        COMMIT;
        BEGIN TRY DBCC CHECKIDENT ('dbo.Ventas', RESEED, 0) WITH NO_INFOMSGS; END TRY BEGIN CATCH END CATCH
        EXEC dbo.sp_SembrarRecetas;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK;
        THROW;
    END CATCH
END
GO
