package com.example.personalbudget;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.*;
import java.time.LocalDate;
import java.util.List;

public class Main extends Application {

    private BudgetManager budgetManager = new BudgetManager();
    private ObservableList<Transaction> transactionData;
    private TableView<Transaction> transactionTable;
    private Label balanceLabel;
    private Label incomeLabel;
    private Label expenseLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Personal Budgeting Tool");

        // Transaction form
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");
        TextField amountField = new TextField();
        amountField.setPromptText("Amount (e.g., 100.0 or -50.0)");
        Button addButton = new Button("Add Transaction");
        addButton.setOnAction(e -> {
            try {
                LocalDate date = datePicker.getValue();
                String description = descriptionField.getText();
                double amount = Double.parseDouble(amountField.getText());
                Transaction transaction = new Transaction(date, description, amount);
                budgetManager.addTransaction(transaction);
                updateTransactionTable();
                updateSummary();
                // Clear input fields
                descriptionField.clear();
                amountField.clear();
            } catch (NumberFormatException ex) {
                showAlert("Invalid amount. Please enter a valid number.");
            } catch (Exception ex) {
                showAlert("Error: " + ex.getMessage());
            }
        });
        
        HBox formBox = new HBox(10);
        formBox.getChildren().addAll(new Label("Date:"), datePicker,
                                     new Label("Description:"), descriptionField,
                                     new Label("Amount:"), amountField, addButton);
        formBox.setPadding(new Insets(10));

        // Transaction Table using TableView
        transactionTable = new TableView<>();
        transactionData = FXCollections.observableArrayList(budgetManager.getTransactions());
        transactionTable.setItems(transactionData);

        TableColumn<Transaction, LocalDate> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<Transaction, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<Transaction, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        transactionTable.getColumns().addAll(dateColumn, descriptionColumn, amountColumn);
        transactionTable.setPrefHeight(300);

        // Summary Labels for income, expense, and balance
        incomeLabel = new Label("Total Income: 0.00");
        expenseLabel = new Label("Total Expense: 0.00");
        balanceLabel = new Label("Current Balance: 0.00");
        
        HBox summaryBox = new HBox(20);
        summaryBox.getChildren().addAll(incomeLabel, expenseLabel, balanceLabel);
        summaryBox.setPadding(new Insets(10));

        // Buttons for deleting, saving, and loading transactions
        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> {
            Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                budgetManager.removeTransaction(selected);
                updateTransactionTable();
                updateSummary();
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
                showAlertInfo("Transactions loaded successfully.");
            } catch (IOException ex) {
                showAlert("Error loading transactions: " + ex.getMessage());
            }
        });
        
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(deleteButton, saveButton, loadButton);
        buttonBox.setPadding(new Insets(10));

        // Main layout
        VBox mainLayout = new VBox(10);
        mainLayout.getChildren().addAll(formBox, transactionTable, summaryBox, buttonBox);
        mainLayout.setPadding(new Insets(10));

        Scene scene = new Scene(mainLayout, 800, 500);
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
    
    private void saveTransactionsToFile(String filename, List<Transaction> transactions) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // CSV header
            writer.println("date,description,amount");
            for (Transaction t : transactions) {
                writer.println(t.getDate() + "," + escapeCsv(t.getDescription()) + "," + t.getAmount());
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
        List<Transaction> loadedTransactions = FXCollections.observableArrayList();
        File file = new File(filename);
        if (!file.exists()) {
            return loadedTransactions;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length == 3) {
                    LocalDate date = LocalDate.parse(parts[0]);
                    String description = parts[1];
                    double amount = Double.parseDouble(parts[2]);
                    loadedTransactions.add(new Transaction(date, description, amount));
                }
            }
        }
        return loadedTransactions;
    }
    
    private String[] parseCsvLine(String line) {
        // Splitting by comma, accounting for commas inside quoted strings.
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
