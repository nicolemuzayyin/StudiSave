SELECT c.categoryName,
       s.subCatName,
       p.date,
       p.cost,
       p.note
FROM Purchases p
         INNER JOIN Categories c ON p.categoryID = c.categoryID
         LEFT JOIN SubCategories s ON p.subCategoryID = s.subCategoryID
ORDER BY p.date DESC