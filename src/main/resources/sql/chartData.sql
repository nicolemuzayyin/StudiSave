SELECT c.categoryName,
       strftime('%m', p.date) AS month,
       SUM(p.cost)            AS total
FROM Purchases p
INNER JOIN Categories c ON p.categoryID = c.categoryID
WHERE strftime('%Y', p.date) = ? AND c.categoryName != "Savings"
GROUP BY c.categoryName,
         strftime('%m', p.date)
ORDER BY month, c.categoryName
