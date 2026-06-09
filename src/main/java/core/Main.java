package core;

// authors: Nicole Muzayyin, Felix D'Cruz

import javafx.application.Application;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Objects;


public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        loadCustomFonts();

        Pane root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/core/ui/login.fxml")));
        stage.setTitle("StudiSave");
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void loadCustomFonts() {
        try {
            Font.loadFont(Objects.requireNonNull(getClass().getResourceAsStream("/core/fonts/Onest-VariableFont_wght.ttf")), 12);
        } catch (Exception ignored) {
            // If font file is missing, JavaFX will use fallback system fonts.
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}


