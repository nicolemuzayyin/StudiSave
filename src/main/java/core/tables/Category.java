package core.tables;

// author: Felix D'Cruz

public class Category {

    private final String categoryID;
    private final String categoryName;

    public Category(String categoryID, String categoryName) {
        this.categoryID   = categoryID;
        this.categoryName = categoryName;
    }

    public String getCategoryID()   { return categoryID;   }
    public String getCategoryName() { return categoryName; }

    // toString() controls what the ComboBox renders.
    @Override
    public String toString() { return categoryName; }
}

