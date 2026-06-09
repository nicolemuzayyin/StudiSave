SELECT sg.savingID,
       sg.userID,
       sg.name,
       sg.amount,
       sg.startDate,
       sg.endDate,
       sg.note,
       sg.perMonth,
       CASE
           WHEN COALESCE((
               SELECT SUM(p.cost)
               FROM Purchases p
               INNER JOIN SubCategories sc ON p.subCategoryID = sc.subCategoryID
               WHERE sc.subCatName = sg.name
           ), 0) >= sg.amount THEN 1
           ELSE 0
       END AS completed,
       COALESCE((
           SELECT SUM(p.cost)
           FROM Purchases p
           INNER JOIN SubCategories sc ON p.subCategoryID = sc.subCategoryID
           WHERE sc.subCatName = sg.name
       ), 0) AS amountSaved
FROM SavingsGoals sg
ORDER BY sg.startDate DESC
