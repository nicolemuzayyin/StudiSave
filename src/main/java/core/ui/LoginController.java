package core.ui;

// author: Felix D'Cruz

import core.Session;
import core.db_functions.Action;

import core.tables.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Objects;


public class LoginController {
    @FXML private PasswordField     passwordField;
    @FXML private TextField         emailField;

    @FXML
    private void tryLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || email.isEmpty()) {
            showAlert("Please enter an email.");
            return;
        }

        try {
            // UserManager: call process to execute SQL, check result, and return either null or a valid user
            User login = Action.tryLogin(email, password);
            if (login == null) {
                throw new Exception();
            }

            // save the current user
            Session.setUser(login);
            Stage stage = (Stage) passwordField.getScene().getWindow();
            stage.close();
            login();

        } catch (Exception e) {
            showAlert("Email or password incorrect. ");
        }
    }

    private void showAlert(String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait();
    }

    public void login() throws Exception {
        Stage stage = new Stage();
        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("theme-tabs");
        tabs.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/core/ui/app.css")).toExternalForm());
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabs.getTabs().add(loadTab("Main Page",     "/core/ui/mainpage.fxml", tabs));
        tabs.getTabs().add(loadTab("Purchases",     "/core/ui/purchases.fxml", tabs));
        tabs.getTabs().add(loadTab("Overview",      "/core/ui/chart.fxml", tabs));
        tabs.getTabs().add(loadTab("Savings Goals", "/core/ui/goals.fxml", tabs));
        tabs.getTabs().add(createInactiveTab("Profile"));

        stage.setTitle( Session.getUser().getName() +"'s StudiSave");
        stage.setScene(new Scene(tabs, 960, 800));
        stage.setMaximized(true);
        stage.show();
    }

    private Tab loadTab(String title, String fxmlPath, TabPane tabs) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        Object controller = loader.getController();
        if (controller instanceof MainPageController mainPageController) {
            mainPageController.setTabPane(tabs);
        }

        Tab tab = new Tab(title, root);
        tab.setClosable(false);
        return tab;
    }

    private Tab createInactiveTab(String title) {
        Tab tab = new Tab(title);
        tab.setClosable(false);
        tab.setDisable(true);
        return tab;
    }

}
