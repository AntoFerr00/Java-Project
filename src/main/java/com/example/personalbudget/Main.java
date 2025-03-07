package com.example.personalbudget;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;

public class Main extends Application {

    private BudgetManager budgetManager = new BudgetManager();
    private ListView<String> transactionsList;
    private Label balanceLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Personal Budgeting Tool");

        // Create UI elements for the form
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
                updateTransactionList();
                updateBalance();

                // Clear input fields
                descriptionField.clear();
                amountField.clear();
            } catch (NumberFormatException ex) {
                showAlert("Invalid amount. Please enter a valid number.");
            } catch (Exception ex) {
                showAlert("Error: " + ex.getMessage());
            }
        });

        // ListView for transactions
        transactionsList = new ListView<>();

        // Label for displaying the current balance
        balanceLabel = new Label("Current Balance: " + String.format("%.2f", budgetManager.calculateBalance()));

        // Organize UI layout
        VBox formBox = new VBox(10);
        formBox.getChildren().addAll(
                new Label("Date:"), datePicker,
                new Label("Description:"), descriptionField,
                new Label("Amount:"), amountField,
                addButton
        );

        VBox mainBox = new VBox(15);
        mainBox.setPadding(new Insets(10));
        mainBox.getChildren().addAll(
                formBox,
                new Label("Transactions:"),
                transactionsList,
                balanceLabel
        );

        Scene scene = new Scene(mainBox, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateTransactionList() {
        transactionsList.getItems().clear();
        for (Transaction t : budgetManager.getTransactions()) {
            transactionsList.getItems().add(t.toString());
        }
    }

    private void updateBalance() {
        double balance = budgetManager.calculateBalance();
        balanceLabel.setText("Current Balance: " + String.format("%.2f", balance));
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
         launch(args);
    }
}
