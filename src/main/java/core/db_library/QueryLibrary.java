package core.db_library;

// authors: Nicole Muzayyin, Felix D'Cruz
// class to initiate Query objects using sql files and manually set column names
// resultColumns MUST match expected output (test using db manager)
// sqlFile MUST match .sql file in /sql
// the Query objects are all named data as the actual name is determined when they are called

import core.Session;
import core.db_functions.CalcField;
import core.tables.Purchase;
import core.tables.SavingsGoal;

import java.util.List;


public class QueryLibrary {

    // UserManager: SQL string is written here
    // the params to insert into ? are List.of(inputEmail)
    public static Query tryLogin(String inputEmail){
        String sql = "SELECT userID, name, email, password FROM Users WHERE email = ?";
        String[] cols = {"userID", "name", "email", "password"};
        return new Query(sql, cols, List.of(inputEmail));
    }

    public static Query purchaseHistory() {
        String[] cols = { "categoryName", "subCatName", "date", "cost", "note" };
        return new Query(Query.fileToString("purchaseHistory"), cols);
    }

    public static Query getCategories() {
        String[] cols = { "categoryID", "categoryName" };
        return new Query(Query.fileToString("getCategories"), cols);
    }

    public static Query getSubCategories(String categoryID) {
        String[] cols = { "subCategoryID", "subCatName" };
        return new Query(Query.fileToString("getSubCategories"), cols, List.of(categoryID));
    }

    public static Query getSavingsGoals() {
        String[] cols = { "savingID", "userID", "name", "amount",
                "startDate", "endDate", "note", "perMonth", "completed", "amountSaved" };
        return new Query(Query.fileToString("getSavingsGoals"), cols);
    }

    // year should be a four-digit string, e.g. "2026".
    public static Query chartData(String year) {
        String[] cols = { "categoryName", "month", "total" };
        return new Query(Query.fileToString("chartData"), cols, List.of(year));
    }

    public static Query insertSubCategory(String n) {
        String sql = "INSERT INTO SubCategories (subCategoryID, categoryID,  userID, subCatName) "
                + "VALUES (?, ?, ?, ?)";
        // default category of "Savings" since this is the only category insertion we are implementing
        return new Query(sql, List.of(
                CalcField.newUUID(), "31bff236-fc1a-45f9-8254-533df572b610", Session.getUser().getUserID(), n
        ));
    }

    public static Query insertPurchase(Purchase p) {
        String sql = "INSERT INTO Purchases (purchaseID, userID, categoryID, subCategoryID, date, cost, note, frequency) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return new Query(sql, List.of(
                p.getPurchaseID(), p.getUserID(), p.getCategoryID(), p.getSubCategoryID(),
                p.getDate(), p.getCost(), p.getNote(), p.getFrequency()
        ));
    }

    public static Query insertSavingsGoal(SavingsGoal g) {
        String sql = "INSERT INTO SavingsGoals "
                + "(savingID, userID, name, amount, startDate, endDate, note, perMonth, completed) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return new Query(sql, List.of(
                g.getSavingID(), g.getUserID(), g.getName(), g.getAmount(),
                g.getStartDate(), g.getEndDate(), g.getNote(), g.getPerMonth(), g.getCompleted()
        ));
    }
}