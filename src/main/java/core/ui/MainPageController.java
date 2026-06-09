package core.ui;

import core.Session;
import core.db_functions.Action;
import core.db_library.Query;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainPageController {

    @FXML private Label welcomeLabel;
    @FXML private Button purchasesWidget;
    @FXML private Button overviewWidget;
    @FXML private Button goalsWidget;
    @FXML private Label purchasesMonthHeader;
    @FXML private Label purchasesMonthTotal;
    @FXML private ProgressBar purchasesProgressBar;
    @FXML private VBox purchasesListBox;
    @FXML private Label overviewMonthHeader;
    @FXML private PieChart overviewMiniChart;
    @FXML private Region overviewDot1;
    @FXML private Region overviewDot2;
    @FXML private Region overviewDot3;
    @FXML private Label overviewCat1Name;
    @FXML private Label overviewCat2Name;
    @FXML private Label overviewCat3Name;
    @FXML private Label overviewCat1Amount;
    @FXML private Label overviewCat2Amount;
    @FXML private Label overviewCat3Amount;
    @FXML private Label goalsMonthHeader;
    @FXML private VBox goalsWishlistBox;

    private TabPane tabPane;
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMMM uuuu", Locale.UK);
    private static final String[] OVERVIEW_COLORS = {"#E56B67", "#EAC86C", "#9FD8F4"};

    @FXML
    public void initialize() {
        String name = Session.getUser() != null ? Session.getUser().getName() : "there";
        welcomeLabel.setText("Welcome back, " + name + "!");
        loadWidgetPreviews();
    }

    public void setTabPane(TabPane tabPane) {
        this.tabPane = tabPane;
    }

    @FXML
    private void openPurchases(ActionEvent event) {
        selectTabByTitle("Purchases");
    }

    @FXML
    private void openOverview(ActionEvent event) {
        selectTabByTitle("Overview");
    }

    @FXML
    private void openGoals(ActionEvent event) {
        selectTabByTitle("Savings Goals");
    }

    private void selectTabByTitle(String title) {
        if (tabPane == null) return;
        for (Tab tab : tabPane.getTabs()) {
            if (title.equals(tab.getText())) {
                tabPane.getSelectionModel().select(tab);
                break;
            }
        }
    }

    private void loadWidgetPreviews() {
        loadPurchasesPreview();
        loadOverviewPreview();
        loadGoalsPreview();
    }

    private void loadPurchasesPreview() {
        try {
            ArrayList<Query> results = Action.purchasePageData();
            ArrayList<String[]> historyRows = results.getFirst().getResults();

            if (historyRows.isEmpty()) {
                purchasesMonthHeader.setText(currentMonthLabel());
                purchasesMonthTotal.setText("£0");
                purchasesProgressBar.setProgress(0.18);
                populatePurchasesList(new ArrayList<>());
                return;
            }

            applyPurchasesMonthSummary(historyRows);
            populatePurchasesList(buildRecentPurchasesForCurrentMonth(historyRows));
        } catch (Exception e) {
            purchasesMonthHeader.setText(currentMonthLabel());
            purchasesMonthTotal.setText("£0");
            purchasesProgressBar.setProgress(0.18);
            populatePurchasesList(new ArrayList<>());
        }
    }

    private void applyPurchasesMonthSummary(ArrayList<String[]> historyRows) {
        YearMonth focusMonth = YearMonth.now();

        YearMonth previousMonth = focusMonth.minusMonths(1);
        double focusTotal = 0.0;
        double previousTotal = 0.0;

        for (String[] row : historyRows) {
            LocalDate parsedDate = parseDateSafe(safe(row, 2, ""));
            if (parsedDate == null) continue;

            YearMonth rowMonth = YearMonth.from(parsedDate);
            double cost = parseDoubleSafe(safe(row, 3, "0"));

            if (rowMonth.equals(focusMonth)) {
                focusTotal += cost;
            } else if (rowMonth.equals(previousMonth)) {
                previousTotal += cost;
            }
        }

        purchasesMonthHeader.setText(currentMonthLabel());
        purchasesMonthTotal.setText("£" + String.format("%.0f", focusTotal));
        purchasesProgressBar.setProgress(computePurchasesProgress(focusTotal, previousTotal));
    }

    private double computePurchasesProgress(double currentMonth, double previousMonth) {
        if (currentMonth <= 0.0) {
            return 0.12;
        }

        if (previousMonth <= 0.0) {
            return 0.22;
        }

        double ratio = currentMonth / previousMonth;
        return Math.max(0.08, Math.min(ratio, 1.0));
    }

    private void loadOverviewPreview() {
        try {
            YearMonth currentMonth = YearMonth.now();
            String year = String.valueOf(currentMonth.getYear());
            String currentMonthKey = String.format("%02d", currentMonth.getMonthValue());
            ArrayList<Query> results = Action.overviewPageData(year);
            ArrayList<String[]> rows = results.getFirst().getResults();
            overviewMonthHeader.setText(currentMonthLabel());

            if (rows.isEmpty()) {
                overviewMiniChart.setData(FXCollections.observableArrayList());
                applyOverviewLegend(List.of());
                return;
            }

            Map<String, Double> monthTotals = new LinkedHashMap<>();

            for (String[] row : rows) {
                String category = safe(row, 0, "Unknown");
                String month = safe(row, 1, "");
                double value = parseDoubleSafe(safe(row, 2, "0"));
                if (!currentMonthKey.equals(month)) continue;
                monthTotals.merge(category, value, Double::sum);
            }

            if (monthTotals.isEmpty()) {
                overviewMiniChart.setData(FXCollections.observableArrayList());
                applyOverviewLegend(List.of());
                return;
            }

            List<Map.Entry<String, Double>> ordered = monthTotals.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                    .toList();

            double monthTotal = monthTotals.values().stream().mapToDouble(Double::doubleValue).sum();
            ArrayList<OverviewSlice> topSlices = new ArrayList<>();
            for (int i = 0; i < Math.min(3, ordered.size()); i++) {
                Map.Entry<String, Double> entry = ordered.get(i);
                topSlices.add(new OverviewSlice(entry.getKey(), entry.getValue(), OVERVIEW_COLORS[i]));
            }

            applyMiniChart(topSlices, monthTotal);
            applyOverviewLegend(topSlices);
        } catch (Exception e) {
            overviewMonthHeader.setText(currentMonthLabel());
            overviewMiniChart.setData(FXCollections.observableArrayList());
            applyOverviewLegend(List.of());
        }
    }

    private String currentMonthLabel() {
        return MONTH_LABEL.format(YearMonth.now().atDay(1));
    }

    private void loadGoalsPreview() {
        try {
            ArrayList<Query> results = Action.goalsPageData();
            ArrayList<String[]> rows = results.getFirst().getResults();

            goalsMonthHeader.setText(currentMonthLabel());
            goalsWishlistBox.getChildren().clear();

            if (rows.isEmpty()) {
                goalsWishlistBox.getChildren().add(createEmptyGoalItem("No goals yet", "Create one in Savings Goals"));
                return;
            }

            int maxItems = Math.min(4, rows.size());
            for (int i = 0; i < maxItems; i++) {
                goalsWishlistBox.getChildren().add(createGoalItem(rows.get(i)));
            }
        } catch (Exception e) {
            goalsMonthHeader.setText(currentMonthLabel());
            goalsWishlistBox.getChildren().clear();
            goalsWishlistBox.getChildren().add(createEmptyGoalItem("Preview unavailable", "Open Savings Goals tab"));
        }
    }

    private VBox createGoalItem(String[] row) {
        String name = safe(row, 2, "Goal");
        double amount = parseDoubleSafe(safe(row, 3, "0"));
        double saved = parseDoubleSafe(safe(row, 9, "0"));
        double progress = amount > 0 ? Math.min(saved / amount, 1.0) : 0.0;

        Label icon = new Label(buildGoalIcon(name));
        icon.getStyleClass().add("goals-item-icon");

        Label title = new Label(trimGoalTitle(name) + " (£" + formatMoney(amount) + ")");
        title.getStyleClass().add("goals-item-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amountText = new Label("£" + formatMoney(saved) + "/£" + formatMoney(amount));
        amountText.getStyleClass().add("goals-item-amount");

        HBox topRow = new HBox(12, icon, title, spacer, amountText);
        topRow.setFillHeight(true);

        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.getStyleClass().add("goals-item-progress");
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        HBox bottomRow = new HBox(progressBar);
        HBox.setHgrow(bottomRow, Priority.ALWAYS);
        bottomRow.setFillHeight(true);

        VBox item = new VBox(6, topRow, bottomRow);
        item.getStyleClass().add("goals-wishlist-item");

        return item;
    }

    private VBox createEmptyGoalItem(String line1, String line2) {
        Label a = new Label(line1);
        a.getStyleClass().add("goals-item-title");
        Label b = new Label(line2);
        b.getStyleClass().add("goals-item-amount");
        VBox box = new VBox(3, a, b);
        box.getStyleClass().add("goals-wishlist-item");
        return box;
    }

    private String buildGoalIcon(String name) {
        if (name == null || name.isBlank()) return "G";
        return String.valueOf(Character.toUpperCase(name.charAt(0)));
    }

    private String trimGoalTitle(String name) {
        if (name == null) return "Goal";
        return name.length() > 16 ? name.substring(0, 15) + "..." : name;
    }

    private String formatMoney(double value) {
        if (Math.abs(value - Math.round(value)) < 0.01) {
            return String.format("%.0f", value);
        }
        return String.format("%.2f", value);
    }

    private void applyMiniChart(List<OverviewSlice> topSlices, double monthTotal) {
        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();
        for (OverviewSlice slice : topSlices) {
            chartData.add(new PieChart.Data(formatPercent(slice.amount, monthTotal), slice.amount));
        }
        overviewMiniChart.setData(chartData);

        Platform.runLater(() -> {
            for (int i = 0; i < topSlices.size() && i < chartData.size(); i++) {
                Node node = chartData.get(i).getNode();
                if (node != null) {
                    node.setStyle("-fx-pie-color: " + topSlices.get(i).color + ";");
                }
            }
        });
    }

    private void applyOverviewLegend(List<OverviewSlice> topSlices) {
        Region[] dots = {overviewDot1, overviewDot2, overviewDot3};
        Label[] names = {overviewCat1Name, overviewCat2Name, overviewCat3Name};
        Label[] amounts = {overviewCat1Amount, overviewCat2Amount, overviewCat3Amount};

        for (int i = 0; i < 3; i++) {
            if (i < topSlices.size()) {
                OverviewSlice slice = topSlices.get(i);
                names[i].setText(slice.name);
                amounts[i].setText("£" + String.format("%.0f", slice.amount));
                dots[i].setStyle("-fx-background-color: " + slice.color + ";");
                names[i].setOpacity(1.0);
                amounts[i].setOpacity(1.0);
                dots[i].setOpacity(1.0);
            } else {
                names[i].setText("-");
                amounts[i].setText("£0");
                dots[i].setStyle("-fx-background-color: #3A3D45;");
                names[i].setOpacity(0.6);
                amounts[i].setOpacity(0.6);
                dots[i].setOpacity(0.6);
            }
        }
    }

    private String formatPercent(double value, double total) {
        if (total <= 0.0) return "0%";
        double pct = (value / total) * 100.0;
        return String.format("%.2f%%", pct);
    }

    private static class OverviewSlice {
        private final String name;
        private final double amount;
        private final String color;

        private OverviewSlice(String name, double amount, String color) {
            this.name = name;
            this.amount = amount;
            this.color = color;
        }
    }

    private ArrayList<String> buildRecentPurchasesForCurrentMonth(ArrayList<String[]> historyRows) {
        YearMonth currentMonth = YearMonth.now();
        int maxItems = 8;
        ArrayList<String> output = new ArrayList<>();

        for (String[] row : historyRows) {
            LocalDate date = parseDateSafe(safe(row, 2, ""));
            if (date == null || !YearMonth.from(date).equals(currentMonth)) {
                continue;
            }

            output.add(formatPurchaseLine(row));

            if (output.size() >= maxItems) {
                break;
            }
        }

        if (output.size() < maxItems) {
            for (String[] row : historyRows) {
                LocalDate date = parseDateSafe(safe(row, 2, ""));
                if (date == null || YearMonth.from(date).equals(currentMonth)) {
                    continue;
                }

                output.add(formatPurchaseLine(row));
                if (output.size() >= maxItems) {
                    break;
                }
            }
        }

        return output;
    }

    private String formatPurchaseLine(String[] row) {
        String category = safe(row, 0, "Unknown");
        String subCategory = safe(row, 1, "");
        String cost = formatMoney(parseDoubleSafe(safe(row, 3, "0")));

        return subCategory.isBlank()
                ? category + " - £" + cost
                : category + " / " + subCategory + " - £" + cost;
    }

    private void populatePurchasesList(ArrayList<String> purchases) {
        purchasesListBox.getChildren().clear();

        if (purchases.isEmpty()) {
            HBox row = createPurchaseRow("No purchases yet this month");
            purchasesListBox.getChildren().add(row);
            return;
        }

        for (int i = 0; i < purchases.size(); i++) {
            purchasesListBox.getChildren().add(createPurchaseRow(purchases.get(i)));
            if (i < purchases.size() - 1) {
                Region divider = new Region();
                divider.getStyleClass().add("main-mini-separator");
                purchasesListBox.getChildren().add(divider);
            }
        }
    }

    private HBox createPurchaseRow(String text) {
        Label name = new Label(text);
        name.getStyleClass().add("main-mini-name");

        HBox row = new HBox(name);
        row.getStyleClass().add("main-mini-row");
        return row;
    }

    private static String safe(String[] row, int index, String fallback) {
        if (row == null || index < 0 || index >= row.length || row[index] == null || row[index].isBlank()) {
            return fallback;
        }
        return row[index];
    }

    private static double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private static LocalDate parseDateSafe(String value) {
        if (value == null || value.isBlank()) return null;

        try {
            return LocalDate.parse(value);
        } catch (Exception ignored) {
        }

        try {
            return LocalDate.parse(value, DateTimeFormatter.ofPattern("d/M/uuuu"));
        } catch (Exception ignored) {
        }

        try {
            return LocalDate.parse(value, DateTimeFormatter.ofPattern("d-M-uuuu"));
        } catch (Exception ignored) {
        }

        return null;
    }
}
