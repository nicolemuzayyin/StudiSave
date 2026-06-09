package core.tables;

// author: Felix D'Cruz

import core.Session;
import core.db_functions.CalcField;

public class Purchase {
    private String purchaseID;
    private String userID;
    private String categoryID;
    private String categoryName;
    private String subCategoryID;
    private String subCategoryName;
    private String date;
    private double cost;
    private String note;
    private int frequency;

    // get data from user form input
    public void fromUserInput(String categoryID, String subCategoryID, String date, double cost, String note) {
        this.purchaseID = CalcField.newUUID();
        this.userID     = Session.getUser().getUserID();
        this.categoryID   = categoryID;
        this.subCategoryID = subCategoryID != null ? subCategoryID : "";
        this.date         = (date == null || date.isBlank()) ? CalcField.nowDate() : date;
        this.cost         = cost;
        this.note         = (note != null) ? note : "";
        this.frequency = 1; // Default to 1 for now
    }

    // get data from purchaseHistory query result row (for display).
    // purchaseHistory returns: categoryName, date, cost, note.
    public void fromDisplayRow(String categoryName, String subCategoryName, String date, double cost, String note) {
        this.categoryName = categoryName;
        this.subCategoryName = subCategoryName;
        this.date         = date;
        this.cost         = cost;
        this.note         = note;
    }

    public String getPurchaseID() { return purchaseID; }
    public String getUserID() { return userID; }
    public String getCategoryID()   { return categoryID;   }
    public String getCategoryName() { return categoryName; }
    public String getSubCategoryID() { return subCategoryID; }
    public String getSubCategoryName() { return subCategoryName; }
    public String getDate()         { return date;         }
    public double getCost()         { return cost;         }
    public String getNote()         { return note;         }
    public int getFrequency() { return frequency; }
}

