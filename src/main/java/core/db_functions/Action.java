package core.db_functions;

// authors: Nicole Muzayyin, Felix D'Cruz

// initiate the queries needed for a specific action
// make a list of Query objects and then execute them in one connection
// the order will be the order that the SQL is executed in - must do additions first, etc
// now an arraylist that is returned to the caller

// to add a new action, first construct the queries in QueryLibrary
// then, call that as part of a list here
// then where it is needed you can call Action.execute(Action.___())
// if it has results, must be ArrayList<Query> results = Action.execute(Action.___())

import core.db_library.Query;
import core.db_library.QueryLibrary;
import core.tables.Purchase;
import core.tables.SavingsGoal;
import core.tables.User;
import core.ui.ChartController;

import java.util.ArrayList;

public class Action {

    // gets the user entry for the given email
    public static User tryLogin(String email, String password) {
        ArrayList<Query> list = new ArrayList<>();
        list.add(QueryLibrary.tryLogin(email)); // index 0

        // UserManager: execute SQL query and get stored info
        ManageConnection.execute(list);

        // UserManager: attempt to make a user based on SQL query results
        // check if the password attempt is valid here and return null if not
        // avoids sending the actual retrieved info to the LoginController unless the login is valid
        return User.validate(list.getFirst().getResults().getFirst(), password);
    }

    // loads data for the Purchases page: purchase history + category list
    public static ArrayList<Query> purchasePageData() {
        ArrayList<Query> list = new ArrayList<>();
        list.add(QueryLibrary.purchaseHistory());  // index 0
        list.add(QueryLibrary.getCategories());    // index 1
        ManageConnection.execute(list);
        return list;
    }

    // loads data for the Overview/Chart page for the given year (e.g. "2026")
    public static ArrayList<Query> overviewPageData(String year) {
        ArrayList<Query> list = new ArrayList<>();
        list.add(QueryLibrary.chartData(year));    // index 0
        ManageConnection.execute(list);
        return list;
    }

    // loads all savings goals
    public static ArrayList<Query> goalsPageData() {
        ArrayList<Query> list = new ArrayList<>();
        list.add(QueryLibrary.getSavingsGoals());  // index 0
        ManageConnection.execute(list);
        return list;
    }

    // gets list of subCategories linked to the category the user has selected in drop down
    public static ArrayList<Query> subCategoriesForCategory(String categoryID) {
        ArrayList<Query> list = new ArrayList<>();
        list.add(QueryLibrary.getSubCategories(categoryID));
        ManageConnection.execute(list);
        return list;
    }

    // inserts a new purchase
    public static void addPurchase(Purchase purchase) {
        ArrayList<Query> list = new ArrayList<>();
        list.add(QueryLibrary.insertPurchase(purchase)); // index 0
        ManageConnection.execute(list);
    }

    // inserts a new savings goal
    public static void addSavingsGoal(SavingsGoal goal) {
        ArrayList<Query> list = new ArrayList<>();
        list.add(QueryLibrary.insertSavingsGoal(goal)); // index 0
        list.add(QueryLibrary.insertSubCategory(goal.getName())); // index 1
        ManageConnection.execute(list);
    }
}
