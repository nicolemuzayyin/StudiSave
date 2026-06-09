package core.ui;

// authors: Felix D'Cruz, Stephen Hu

// CHANGES:
// loads data from querylibrary not CSV file
// uses stackedbarchart
// changed year to be selectable
// pre-set months so that all months are shown even if empty

import core.db_functions.Action;
import core.db_library.Query;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.Node;

import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChartController {

    @FXML private StackedBarChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private ComboBox<String> yearBox;

    // Summary card labels added for dashboard-style UI
    @FXML private Label totalSpendLabel;
    @FXML private Label topCategoryLabel;
    @FXML private Label averageMonthLabel;

    // Overlay label used for empty-state messaging (instead of chart title)
    @FXML private Label emptyStateLabel;

    private static final List<String> MONTH_LABELS = List.of(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    );

        // Default-like chart cycle, but assigned deterministically by category name.
        private static final List<String> CHART_PALETTE = List.of(
            "#f3622d", "#fba71b", "#57b757", "#41a9c9", "#4258c9",
            "#9a42c8", "#c84164", "#888888", "#4e8cff", "#00b894",
            "#ff7675", "#fdcb6e"
        );

    @FXML
    public void initialize() {
        int currentYear = Year.now().getValue();

        yearBox.getItems().clear();
        for (int y = currentYear - 2; y <= currentYear + 1; y++) {
            yearBox.getItems().add(String.valueOf(y));
        }
        yearBox.setValue(String.valueOf(currentYear));

        xAxis.setCategories(javafx.collections.FXCollections.observableArrayList(MONTH_LABELS));
        xAxis.setLabel("Month");
        yAxis.setLabel("Amount (£)");

        barChart.setAnimated(false);

        // Title is now static; empty-state is handled separately via overlay label
        barChart.setTitle("Monthly spending");

        // Initialize summary cards with default values
        resetSummaryCards();

        // Ensure empty-state overlay is hidden on startup
        hideEmptyState();

        loadData();
    }

    @FXML
    public void loadData(ActionEvent event) {
        loadData();
    }

    private void loadData() {
        String year = yearBox.getValue();
        if (year == null || year.isBlank()) {
            year = String.valueOf(Year.now().getValue());
            yearBox.setValue(year);
        }

        ArrayList<Query> results = Action.overviewPageData(year);
        Query q = results.getFirst();

        Map<String, Map<Integer, Double>> byCategory = new LinkedHashMap<>();
        Map<String, Double> categoryTotals = new LinkedHashMap<>();

        // Track total yearly spend for summary cards
        double yearlyTotal = 0.0;

        for (String[] row : q.getResults()) {
            String categoryName = safe(row, 0, "Unknown");
            int month = parseIntSafe(safe(row, 1, "0"), 0);
            double total = parseDoubleSafe(safe(row, 2, "0"), 0.0);

            if (month < 1 || month > 12) {
                continue;
            }

            byCategory
                    .computeIfAbsent(categoryName, k -> new LinkedHashMap<>())
                    .put(month, total);

            categoryTotals.merge(categoryName, total, Double::sum);

            // Accumulate yearly total
            yearlyTotal += total;
        }

        // Populate summary cards (total, top category, average)
        renderSummaryCards(yearlyTotal, categoryTotals);

        renderChart(byCategory, year);

        // Handle empty-state display separately from chart title
        updateEmptyState(q.getResults().isEmpty(), year);
    }

    private void renderChart(Map<String, Map<Integer, Double>> byCategory, String year) {
        barChart.getData().clear();
        Map<String, String> colorByCategory = buildCategoryColors(byCategory.keySet());

        for (Map.Entry<String, Map<Integer, Double>> categoryEntry : byCategory.entrySet()) {
            String categoryName = categoryEntry.getKey();
            Map<Integer, Double> monthMap = categoryEntry.getValue();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(categoryName);

            for (int month = 1; month <= 12; month++) {
                double value = monthMap.getOrDefault(month, 0.0);
                series.getData().add(new XYChart.Data<>(MONTH_LABELS.get(month - 1), value));
            }

            barChart.getData().add(series);
        }

        // Chart title always stays consistent (year shown but no error messaging)
        barChart.setTitle("Monthly spending • " + year);

        applySeriesColors(colorByCategory);
    }

    private void applySeriesColors(Map<String, String> colorByCategory) {
        // Nodes are created during CSS/layout pass; defer styling until then.
        Platform.runLater(() -> {
            for (XYChart.Series<String, Number> series : barChart.getData()) {
                String color = colorByCategory.getOrDefault(series.getName(), "#8b8f97");

                Node seriesNode = series.getNode();
                if (seriesNode != null) {
                    seriesNode.setStyle("-fx-bar-fill: " + color + ";");
                }

                for (XYChart.Data<String, Number> data : series.getData()) {
                    Node barNode = data.getNode();
                    if (barNode != null) {
                        barNode.setStyle("-fx-bar-fill: " + color + ";");
                    }
                }
            }

            // Keep legend key colors in sync with bar colors.
            for (Node legendItem : barChart.lookupAll(".chart-legend-item")) {
                if (legendItem instanceof Label legendLabel) {
                    String color = colorByCategory.get(legendLabel.getText());
                    if (color == null) {
                        continue;
                    }

                    Node symbol = legendLabel.lookup(".chart-legend-item-symbol");
                    if (symbol != null) {
                        symbol.setStyle("-fx-background-color: " + color + ";");
                    }
                }
            }
        });
    }

    private Map<String, String> buildCategoryColors(Iterable<String> categories) {
        List<String> sorted = new ArrayList<>();
        for (String category : categories) {
            if (category != null && !category.isBlank()) {
                sorted.add(category);
            }
        }
        Collections.sort(sorted, String.CASE_INSENSITIVE_ORDER);

        Map<String, String> colorByCategory = new HashMap<>();
        for (int i = 0; i < sorted.size(); i++) {
            colorByCategory.put(sorted.get(i), CHART_PALETTE.get(i % CHART_PALETTE.size()));
        }
        return colorByCategory;
    }

    // Method to compute and display summary statistics
    private void renderSummaryCards(double yearlyTotal, Map<String, Double> categoryTotals) {
        totalSpendLabel.setText(formatCurrency(yearlyTotal));
        averageMonthLabel.setText(formatCurrency(yearlyTotal / 12.0));

        if (categoryTotals.isEmpty()) {
            topCategoryLabel.setText("No data");
            return;
        }

        String topCategory = null;
        double topValue = -1.0;

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > topValue) {
                topValue = entry.getValue();
                topCategory = entry.getKey();
            }
        }

        if (topCategory == null || topCategory.isBlank()) {
            topCategoryLabel.setText("No data");
        } else {
            topCategoryLabel.setText(topCategory);
        }
    }

    // Dedicated empty-state handling (UI overlay instead of title changes)
    private void updateEmptyState(boolean isEmpty, String year) {
        if (isEmpty) {
            emptyStateLabel.setText(
                    "No spending data for " + year + " yet.\n" +
                    "Add purchases in the Purchases tab to populate your overview."
            );
            emptyStateLabel.setVisible(true);
            emptyStateLabel.setManaged(true);
        } else {
            hideEmptyState();
        }
    }

    // Helper to hide empty-state overlay
    private void hideEmptyState() {
        emptyStateLabel.setVisible(false);
        emptyStateLabel.setManaged(false);
    }

    // Reset summary cards to default values before data loads
    private void resetSummaryCards() {
        totalSpendLabel.setText("£0.00");
        topCategoryLabel.setText("No data");
        averageMonthLabel.setText("£0.00");
    }

    private String formatCurrency(double value) {
        return String.format("£%.2f", value);
    }

    private static String safe(String[] row, int index, String fallback) {
        if (row == null || index < 0 || index >= row.length || row[index] == null || row[index].isBlank()) {
            return fallback;
        }
        return row[index];
    }

    private static int parseIntSafe(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static double parseDoubleSafe(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return fallback;
        }
    }
}