SELECT subCategoryID,
       subCatName
FROM SubCategories
WHERE categoryID = ?
ORDER BY subCatName