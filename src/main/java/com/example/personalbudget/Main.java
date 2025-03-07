package com.example.personalbudget;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends Application {

    private BudgetManager budgetManager = new BudgetManager();
    private ObservableList<Transaction> transactionData;
    private TableView<Transaction> transactionTable;
    private Label balanceLabel;
    private Label incomeLabel;
    private Label expenseLabel;
    private PieChart expensePieChart;
    private PieChart incomePieChart;

    // ComboBoxes for transaction type and category
    private ComboBox<String> typeCombo;
    private ComboBox<String> categoryCombo;

    // Predefined categories for expenses and incomes
    private ObservableList<String> expenseCategories = FXCollections.observableArrayList(
            "Food", "Clothing", "Entertainment", "Bills", "Travel", "Shopping");
    private ObservableList<String> incomeCategories = FXCollections.observableArrayList(
            "Salary", "Freelance", "Investment", "Other Income");

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Personal Budgeting Tool");

        // Transaction form components
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");
        TextField amountField = new TextField();
        amountField.setPromptText("Amount (enter positive number)");
        
        // ComboBox for selecting transaction type
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Expense", "Income");
        typeCombo.setValue("Expense");  // default selection

        // ComboBox for selecting category; initialize with expense categories
        categoryCombo = new ComboBox<>();
        categoryCombo.setItems(expenseCategories);
        categoryCombo.setValue(expenseCategories.get(0));

        // Update category options when the type changes
        typeCombo.setOnAction(e -> {
            String selectedType = typeCombo.getValue();
            if ("Expense".equals(selectedType)) {
                categoryCombo.setItems(expenseCategories);
                categoryCombo.setValue(expenseCategories.get(0));
            } else {
                categoryCombo.setItems(incomeCategories);
                categoryCombo.setValue(incomeCategories.get(0));
            }
        });

        Button addButton = new Button("Add Transaction");
        addButton.setOnAction(e -> {
            try {
                LocalDate date = datePicker.getValue();
                String description = descriptionField.getText();
                double amountInput = Double.parseDouble(amountField.getText());
                String type = typeCombo.getValue();
                String category = categoryCombo.getValue();

                // Ensure expense amounts are negative and incomes are positive
                double amount = ("Expense".equals(type)) ? -Math.abs(amountInput) : Math.abs(amountInput);

                Transaction transaction = new Transaction(date, description, amount, category);
                budgetManager.addTransaction(transaction);
                updateTransactionTable();
                updateSummary();
                updateExpensePieChart();
                updateIncomePieChart();
                // Clear input fields after adding
                descriptionField.clear();
                amountField.clear();
            } catch (NumberFormatException ex) {
                showAlert("Invalid amount. Please enter a valid number.");
            } catch (Exception ex) {
                showAlert("Error: " + ex.getMessage());
            }
        });
        
        // Form layout combining all input elements
        HBox formBox = new HBox(10);
        formBox.getChildren().addAll(
                new Label("Date:"), datePicker,
                new Label("Description:"), descriptionField,
                new Label("Amount:"), amountField,
                new Label("Type:"), typeCombo,
                new Label("Category:"), categoryCombo,
                addButton);
        formBox.setPadding(new Insets(10));

        // TableView for displaying transactions
        transactionTable = new TableView<>();
        transactionData = FXCollections.observableArrayList(budgetManager.getTransactions());
        transactionTable.setItems(transactionData);

        TableColumn<Transaction, LocalDate> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<Transaction, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<Transaction, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        TableColumn<Transaction, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        transactionTable.getColumns().addAll(dateColumn, descriptionColumn, categoryColumn, amountColumn);
        transactionTable.setPrefHeight(300);

        // Summary labels for income, expense, and balance
        incomeLabel = new Label("Total Income: 0.00");
        expenseLabel = new Label("Total Expense: 0.00");
        balanceLabel = new Label("Current Balance: 0.00");
        
        HBox summaryBox = new HBox(20);
        summaryBox.getChildren().addAll(incomeLabel, expenseLabel, balanceLabel);
        summaryBox.setPadding(new Insets(10));

        // PieChart for expense distribution by category
        expensePieChart = new PieChart();
        expensePieChart.setTitle("Expense Distribution by Category");
        updateExpensePieChart();
        
        // PieChart for income distribution by category
        incomePieChart = new PieChart();
        incomePieChart.setTitle("Income Distribution by Category");
        updateIncomePieChart();
        
        // Container for both pie charts
        HBox pieChartBox = new HBox(20);
        pieChartBox.getChildren().addAll(expensePieChart, incomePieChart);
        pieChartBox.setPadding(new Insets(10));

        // Buttons for deletion, saving, and loading transactions
        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> {
            Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                budgetManager.removeTransaction(selected);
                updateTransactionTable();
                updateSummary();
                updateExpensePieChart();
                updateIncomePieChart();
            } else {
                showAlert("Please select a transaction to delete.");
            }
        });
        
        Button saveButton = new Button("Save Transactions");
        saveButton.setOnAction(e -> {
            try {
                saveTransactionsToFile("transactions.csv", budgetManager.getTransactions());
                showAlertInfo("Transactions saved successfully.");
            } catch (IOException ex) {
                showAlert("Error saving transactions: " + ex.getMessage());
            }
        });
        
        Button loadButton = new Button("Load Transactions");
        loadButton.setOnAction(e -> {
            try {
                List<Transaction> loaded = loadTransactionsFromFile("transactions.csv");
                budgetManager.getTransactions().clear();
                budgetManager.getTransactions().addAll(loaded);
                updateTransactionTable();
                updateSummary();
                updateExpensePieChart();
                updateIncomePieChart();
                showAlertInfo("Transactions loaded successfully.");
            } catch (IOException ex) {
                showAlert("Error loading transactions: " + ex.getMessage());
            }
        });
        
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(deleteButton, saveButton, loadButton);
        buttonBox.setPadding(new Insets(10));

        // Main layout combining all components
        VBox mainLayout = new VBox(10);
        mainLayout.getChildren().addAll(formBox, transactionTable, summaryBox, buttonBox, pieChartBox);
        mainLayout.setPadding(new Insets(10));

        Scene scene = new Scene(mainLayout, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateTransactionTable() {
        transactionData.setAll(budgetManager.getTransactions());
    }

    private void updateSummary() {
        double income = budgetManager.calculateTotalIncome();
        double expense = budgetManager.calculateTotalExpense();
        double balance = budgetManager.calculateBalance();
        incomeLabel.setText("Total Income: " + String.format("%.2f", income));
        expenseLabel.setText("Total Expense: " + String.format("%.2f", expense));
        balanceLabel.setText("Current Balance: " + String.format("%.2f", balance));
    }
    
    private void updateExpensePieChart() {
        // Aggregate expense transactions by category (only consider negative amounts)
        Map<String, Double> expenseByCategory = new HashMap<>();
        for (Transaction t : budgetManager.getTransactions()) {
            if (t.getAmount() < 0) {
                expenseByCategory.merge(t.getCategory(), Math.abs(t.getAmount()), Double::sum);
            }
        }
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : expenseByCategory.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        expensePieChart.setData(pieChartData);
    }
    
    private void updateIncomePieChart() {
        // Aggregate income transactions by category (only consider positive amounts)
        Map<String, Double> incomeByCategory = new HashMap<>();
        for (Transaction t : budgetManager.getTransactions()) {
            if (t.getAmount() > 0) {
                incomeByCategory.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : incomeByCategory.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        incomePieChart.setData(pieChartData);
    }
    
    private void saveTransactionsToFile(String filename, List<Transaction> transactions) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // CSV header including the new category field
            writer.println("date,description,amount,category");
            for (Transaction t : transactions) {
                writer.println(t.getDate() + "," + escapeCsv(t.getDescription()) + "," + t.getAmount() + "," + escapeCsv(t.getCategory()));
            }
        }
    }
    
    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"")) {
            value = value.replace("\"", "\"\"");
            value = "\"" + value + "\"";
        }
        return value;
    }
    
    private List<Transaction> loadTransactionsFromFile(String filename) throws IOException {
        ObservableList<Transaction> loadedTransactions = FXCollections.observableArrayList();
        File file = new File(filename);
        if (!file.exists()) {
            return loadedTransactions;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length == 4) {
                    LocalDate date = LocalDate.parse(parts[0]);
                    String description = parts[1];
                    double amount = Double.parseDouble(parts[2]);
                    String category = parts[3];
                    loadedTransactions.add(new Transaction(date, description, amount, category));
                }
            }
        }
        return loadedTransactions;
    }
    
    private String[] parseCsvLine(String line) {
        // Splitting by comma while accounting for commas inside quoted strings.
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Alert");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showAlertInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
         launch(args);
    }
}
