package core.ui;

// author: Felix D'Cruz

import core.db_functions.Action;
import core.db_library.Query;
import core.tables.SavingsGoal;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.Chart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class GoalsController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private TextField   nameField;
    @FXML private TextField   amountField;
    @FXML private DatePicker  startPicker;
    @FXML private DatePicker  endPicker;
    @FXML private TextField   noteField;
    @FXML private TableView<SavingsGoal> goalTable;

    private final ObservableList<SavingsGoal> goalItems = FXCollections.observableArrayList();


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        goalTable.setItems(goalItems);
        goalTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<SavingsGoal, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<SavingsGoal, Double> amtCol = new TableColumn<>("Target (£)");
        amtCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amtCol.setPrefWidth(75);
        amtCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });

        TableColumn<SavingsGoal, Double> pmCol = new TableColumn<>("Per month (£)");
        pmCol.setCellValueFactory(new PropertyValueFactory<>("perMonth"));
        pmCol.setPrefWidth(75);
        pmCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });

        TableColumn<SavingsGoal, String> startCol = new TableColumn<>("Start");
        startCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        startCol.setPrefWidth(100);

        TableColumn<SavingsGoal, String> endCol = new TableColumn<>("End");
        endCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        endCol.setPrefWidth(100);

        TableColumn<SavingsGoal, Integer> doneCol = new TableColumn<>("Done");
        doneCol.setCellValueFactory(new PropertyValueFactory<>("completed"));
        doneCol.setPrefWidth(50);
        doneCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item == 1 ? "Yes" : "No"));
            }
        });

        TableColumn<SavingsGoal, String> noteCol = new TableColumn<>("Note");
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));
        noteCol.setPrefWidth(200);

        TableColumn<SavingsGoal, Double> progressCol = new TableColumn<>("Progress");
        progressCol.setCellValueFactory(new PropertyValueFactory<>("amountSaved"));
        progressCol.setPrefWidth(150);
        progressCol.setCellFactory(col -> new TableCell<>() {
            private final ProgressBar bar = new ProgressBar(0);
            {
                bar.setPrefWidth(90);
                bar.setStyle("-fx-accent: #85FF72;");
            }
            @Override
            protected void updateItem(Double amountSaved, boolean empty) {
                super.updateItem(amountSaved, empty);
                if (empty || amountSaved == null || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    setText(null);
                } else {
                    SavingsGoal goal = getTableView().getItems().get(getIndex());
                    double progress = goal.getAmount() > 0
                            ? Math.min(amountSaved / goal.getAmount(), 1.0)
                            : 0.0;
                    bar.setProgress(progress);
                    setGraphic(bar);
                    setText(String.format("  %.0f%%", progress * 100));
                }
            }
        });

        goalTable.getColumns().addAll(nameCol, amtCol, pmCol, startCol, endCol, doneCol, noteCol, progressCol);
        startPicker.setValue(LocalDate.now());
        endPicker.setValue(LocalDate.now().plusMonths(1));

        refreshWhenTabIsSelected();
        loadData();
    }

    private void refreshWhenTabIsSelected() {
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) return;
            if (!(newScene.getRoot() instanceof TabPane tabs)) return;

            for (Tab tab : tabs.getTabs()) {
                if (tab.getContent() == rootPane) {
                    tab.selectedProperty().addListener((tabObs, wasSelected, isSelected) -> {
                        if (isSelected) {
                            loadData();
                        }
                    });
                    break;
                }
            }
        });
    }

    @FXML
    private void addGoal(ActionEvent event) {
        loadData();
        String name = nameField.getText().trim();
        if (name.isEmpty()) { showAlert("Please enter a goal name."); return; }

        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid positive amount.");
            return;
        }

        LocalDate start = startPicker.getValue() != null ? startPicker.getValue() : LocalDate.now();
        LocalDate end   = endPicker.getValue()   != null ? endPicker.getValue()   : start.plusMonths(6);

        SavingsGoal g = new SavingsGoal();
        g.fromUserInput(
                name, amount,
                start.toString(), end.toString(),
                noteField.getText().trim()
        );

        nameField.clear(); amountField.clear();
        noteField.clear();
        startPicker.setValue(LocalDate.now());
        endPicker.setValue(LocalDate.now().plusMonths(1));

        Action.addSavingsGoal(g);
        loadData();
    }

    private void loadData() {
        ArrayList<Query> results = Action.goalsPageData();
        goalItems.clear();
        for (String[] row : results.get(0).getResults()) {
            SavingsGoal g = new SavingsGoal();
            g.fromRow(
                    row[0], row[1], row[2],
                    parseDouble(row[3]),
                    row[4], row[5], row[6],
                    parseDouble(row[7]),
                    parseInt(row[8]),
                    parseDouble(row[9])
            );
            goalItems.add(g);
        }
    }

    private void showAlert(String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait();
    }

    private double parseDouble(String s) {
        if (s == null) return 0.0;
        try { return Double.parseDouble(s); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private int parseInt(String s) {
        if (s == null) return 0;
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return 0; }
    }
}