module StudiSave {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires de.mkammerer.argon2.nolibs;

    opens core to javafx.graphics, javafx.fxml, de.mkammerer.argon2.nolibs;
    opens core.ui to javafx.graphics, javafx.fxml;
    opens core.tables to javafx.base;
}