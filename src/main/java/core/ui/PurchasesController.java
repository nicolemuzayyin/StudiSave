package core.ui;

// author: Felix D'Cruz

import core.db_functions.Action;
import core.db_library.Query;
import core.tables.Category;
import core.tables.Purchase;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PurchasesController implements Initializable {

    @FXML private ComboBox<Category>   categoryBox;
    @FXML private DatePicker           datePicker;
    @FXML private TextField            costField;
    @FXML private TextField            noteField;
    @FXML private TableView<Purchase>  purchaseTable;
    @FXML private ComboBox<Category> subCategoryBox;
    private final ObservableList<Category> subCategoryItems = FXCollections.observableArrayList();
    private final ObservableList<Category> categoryItems = FXCollections.observableArrayList();
    private final ObservableList<Purchase> purchaseItems = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        categoryBox.setItems(categoryItems);
        subCategoryBox.setItems(subCategoryItems);
        subCategoryBox.setDisable(true);
        purchaseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        purchaseTable.setItems(purchaseItems);

        TableColumn<Purchase, String> catCol  = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        TableColumn<Purchase, String> subCatCol  = new TableColumn<>("Subcategory");
        subCatCol.setCellValueFactory(new PropertyValueFactory<>("subCategoryName"));

        TableColumn<Purchase, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(110);

        TableColumn<Purchase, Double> costCol = new TableColumn<>("Cost (£)");
        costCol.setCellValueFactory(new PropertyValueFactory<>("cost"));
        costCol.setPrefWidth(50);
        costCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });

        TableColumn<Purchase, String> noteCol = new TableColumn<>("Note");
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));
        noteCol.setPrefWidth(200);

        purchaseTable.getColumns().addAll(catCol, subCatCol, dateCol, costCol, noteCol);
        datePicker.setValue(LocalDate.now());

        loadData();
    }

    @FXML
    private void addPurchase(ActionEvent event) {
        Category cat    = (Category) categoryBox.getValue();
        Category subCat = (Category) subCategoryBox.getValue();
        if (cat == null) { showAlert("Please select a category."); return; }

        double cost;
        try {
            cost = Double.parseDouble(costField.getText().trim());
            if (cost <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid positive amount.");
            return;
        }

        LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();

        Purchase p = new Purchase();
        p.fromUserInput(
                cat.getCategoryID(),
                subCat != null ? subCat.getCategoryID() : "",
                date.toString(), cost, noteField.getText().trim()
        );

        costField.clear();
        noteField.clear();
        datePicker.setValue(LocalDate.now());
        categoryBox.setValue(null);
        subCategoryBox.setValue(null);
        subCategoryBox.setDisable(true);
        subCategoryItems.clear();

        Action.addPurchase(p);
        loadData();

    }

    private void loadData() {
        ArrayList<Query> results = Action.purchasePageData();
        purchaseItems.clear();
        for (String[] row : results.get(0).getResults()) {
            Purchase p = new Purchase();
            p.fromDisplayRow(row[0], row[1], row[2], parseDouble(row[3]), row[4]);
            purchaseItems.add(p);
        }

        categoryItems.clear();
        for (String[] row : results.get(1).getResults()) {
            categoryItems.add(new Category(row[0], row[1]));
        }
    }

    @FXML
    private void categorySelected(ActionEvent event) {
        Category selected = (Category) categoryBox.getValue();
        subCategoryItems.clear();
        subCategoryBox.setValue(null);


        if (selected == null) {
            subCategoryBox.setDisable(true);
            return;
        }

        ArrayList<Query> results = Action.subCategoriesForCategory(selected.getCategoryID());
        for (String[] row : results.get(0).getResults()) {
            subCategoryItems.add(new Category(row[0], row[1]));
        }
        subCategoryBox.setDisable(subCategoryItems.isEmpty());
    }

    private void showAlert(String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait();
    }

    private double parseDouble(String s) {
        if (s == null) return 0.0;
        try { return Double.parseDouble(s); }
        catch (NumberFormatException e) { return 0.0; }
    }

}

