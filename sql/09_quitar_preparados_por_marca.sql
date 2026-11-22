-- 09_quitar_preparados_por_marca.sql
-- 09: Quita los «preparados» por marca (Cheetos, Chips, Doritos y Tostitos preparados).
-- Ahora «Sabritas preparadas» (y «Sabritas con ...») preguntan en la caja la marca o sabor de la bolsa.
-- Sin ventas se borran; con ventas quedan marcados como eliminados (las ventas viejas siguen completas en reportes y tickets).
-- Es seguro volver a correrlo.
DECLARE @Q TABLE (id INT PRIMARY KEY, Ventas BIT);
INSERT @Q (id, Ventas)
SELECT p.id, CASE WHEN EXISTS (SELECT 1 FROM dbo.DetalleVentas d WHERE d.ProductoID = p.id) THEN 1 ELSE 0 END
FROM dbo.Productos p
WHERE p.Eliminado = 0
  AND (p.Nombre_Producto COLLATE Latin1_General_CI_AI LIKE N'Cheetos%preparad%'
    OR p.Nombre_Producto COLLATE Latin1_General_CI_AI LIKE N'Chips%preparad%'
    OR p.Nombre_Producto COLLATE Latin1_General_CI_AI LIKE N'Doritos%preparad%'
    OR p.Nombre_Producto COLLATE Latin1_General_CI_AI LIKE N'Tostitos%preparad%');
DELETE FROM dbo.Recetas WHERE ProductoID IN (SELECT id FROM @Q);
UPDATE p SET Eliminado = 1, Tipo = 'directo', Bolas = NULL, ContenedorID = NULL
FROM dbo.Productos p JOIN @Q q ON q.id = p.id WHERE q.Ventas = 1;
DELETE FROM dbo.Productos WHERE id IN (SELECT id FROM @Q WHERE Ventas = 0);
GO
