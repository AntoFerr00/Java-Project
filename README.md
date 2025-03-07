# Personal Budgeting Tool

A simple personal budgeting tool built with Java and JavaFX that allows you to track your financial transactions and view your current balance. This project is designed to demonstrate core Java skills, JavaFX GUI development, and basic financial management concepts.

## Features

- **Add Transactions:** Record transactions with a date, description, and amount (positive for income, negative for expenses).
- **Transaction List:** View all recorded transactions in a clear, user-friendly interface.
- **Balance Calculation:** Automatically calculate and display the current balance based on your transactions.
- **JavaFX Interface:** A clean and responsive UI built with JavaFX.

## Requirements

- **Java 11 or later**
- **Maven** for project management
- **JavaFX SDK** (set the `JAVAFX_HOME` environment variable to your JavaFX SDK path)

## Setup and Running

1. **Clone the repository:**

   ```bash
   git clone https://github.com/yourusername/personal-budgeting-tool.git
   cd personal-budgeting-tool
   ```

## Build the project:

   ```bash
    mvn compile
   ```

## Run the application:

Make sure your JAVAFX_HOME environment variable is set. Then, run:

   ```bash
    mvn exec:java
   ```

This will launch the JavaFX application.

## Customization
Feel free to expand the project with additional features such as:

Persistent storage (e.g., using a database or file system).
More advanced financial reporting.
Improved UI/UX with additional JavaFX components.

## Contributing
Contributions are welcome! Please fork this repository and submit a pull request with your improvements.