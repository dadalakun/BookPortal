package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import model.ClientService;
import model.ClientItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainController {
    private static final Logger logger = LogManager.getLogger(MainController.class);

    // Left part
    // Login components
    @FXML private VBox loginVBox;
    @FXML private TextField inputUsernameField;
    @FXML private TextField inputPasswordField;
    // Setting components
    @FXML private VBox userCenterVBox;
    @FXML private TextField newPasswordField;
    // Logging components
    @FXML private TextFlow logTextFlow;

    // Middle part
    @FXML private VBox middleVBox;
    @FXML private ListView<ClientItem> borrowedItemsListView;

    // Right part
    @FXML private VBox rightVBox;
    // Sort
    @FXML private RadioButton sortByItemType;
    @FXML private RadioButton sortByTitle;
    @FXML private RadioButton sortByLastCheckedOutDate;
    // Filter
    @FXML private RadioButton filterByAll;
    @FXML private RadioButton filterByBook;
    @FXML private RadioButton filterByAudiobook;
    @FXML private RadioButton filterByGame;
    @FXML private RadioButton filterByDVD;
    @FXML private RadioButton filterByAvailability;
    // Search
    @FXML private TextField searchField;

    @FXML private ListView<ClientItem> libraryItemsListView;
    // An immutable source
    private final ObservableList<ClientItem> allItems = FXCollections.observableArrayList();

    private ClientService client;

    public void initialize() {
        client = ClientService.getInstance();
        // Change to localhost to test locally
        client.startConnection("localhost", 6667);
        setupLibraryItemFactory();
        setupBorrowedItemFactory();
        // Disable the middle and right components until login
        middleVBox.setDisable(true);
        rightVBox.setDisable(true);
        middleVBox.setOpacity(0.5);
        rightVBox.setOpacity(0.5);
    }
    @FXML
    private void handleLogin() {
        String userName = inputUsernameField.getText().trim();
        String password = inputPasswordField.getText().trim();
        if (!userName.isEmpty() && !password.isEmpty()) {
            logger.info("Attempting to login for user: {}", userName);
            client.login(userName, password, (success, message) -> {
                // Call back function which will be executed after login
                Platform.runLater(() -> {
                    if (success) {
                        logger.info("Login successful for user: {}", userName);
                        logMessage(message, MessageType.SUCCESS);
                        handleLoginSuccess();
                    } else {
                        logger.warn("Login failed for user: {}", userName);
                        logMessage(message, MessageType.ERROR);
                    }
                });
            });
        } else {
            logMessage("Username and password cannot be empty.", MessageType.WARNING);
            logger.warn("Username and password cannot be empty.");
        }
    }

    private void handleLoginSuccess() {
        // Hide the login VBox
        loginVBox.setVisible(false);
        loginVBox.setManaged(false);
        inputUsernameField.setText("");
        inputPasswordField.setText("");

        // Show and manage userInfo VBox
        userCenterVBox.setVisible(true);
        userCenterVBox.setManaged(true);

        // Enable ListView components and update their contents
        middleVBox.setDisable(false);
        rightVBox.setDisable(false);
        middleVBox.setOpacity(1);
        rightVBox.setOpacity(1);
        loginVBox.setVisible(false);
        updateItemList();
        updateBorrowedItemList();
        applySort();
    }

    @FXML
    private void handleSignup() {
        String userName = inputUsernameField.getText().trim();
        String password = inputPasswordField.getText().trim();
        if (!userName.isEmpty() && !password.isEmpty()) {
            logger.info("Attempting to signup for user: {}", userName);
            client.signup(userName, password, (success, message) -> {
                Platform.runLater(() -> {
                    if (success) {
                        logger.info("Signup successful for user: {}", userName);
                        logMessage("Signup successful: " + message, MessageType.SUCCESS);
                    } else {
                        logger.warn("Signup failed for user: {} - {}", userName, message);
                        logMessage("Signup failed: " + message, MessageType.ERROR);
                    }
                });
            });
        } else {
            logMessage("Username and password cannot be empty.", MessageType.WARNING);
            logger.warn("Username and password cannot be empty.");
        }
    }

    @FXML
    private void handleLogout() {
        // Reset the visibility and management of UI components
        loginVBox.setVisible(true);
        loginVBox.setManaged(true);
        userCenterVBox.setVisible(false);
        userCenterVBox.setManaged(false);

        // Clear and disable the middle and right part
        middleVBox.setDisable(true);
        rightVBox.setDisable(true);
        middleVBox.setOpacity(0.5);
        rightVBox.setOpacity(0.5);

        // Call the logout method in ClientService
        client.logout();

        logger.info("User logged out successfully.");
        logMessage("Logged out successfully.", MessageType.SUCCESS);
    }

    @FXML
    private void handleResetPassword() {
        String newPassword = newPasswordField.getText().trim();
        if (!newPassword.isEmpty()) {
            if (isValidPassword(newPassword)) {
                client.resetPassword(newPassword, success -> {
                    Platform.runLater(() -> {
                        newPasswordField.setText(""); // Clear the TextField regardless of success
                        if (success) {
                            logger.info("Password reset successfully.");
                            logMessage("Password has been reset.", MessageType.SUCCESS);
                        } else {
                            logger.info("Failed to reset password.");
                            logMessage("Failed to reset password.", MessageType.ERROR);
                        }
                    });
                });
            } else {
                // Handle invalid password format
                logger.warn("Password does not meet the requirements.");
                logMessage("Password does not meet the requirements.", MessageType.WARNING);
                newPasswordField.setText("");
            }
        } else {
            logger.warn("Password field cannot be empty.");
            logMessage("Password field cannot be empty.", MessageType.WARNING);
            newPasswordField.setText("");
        }
    }

    // Right now there is no real checking criteria for the password
    private boolean isValidPassword(String password) {
        return true;
    }

    private void setupLibraryItemFactory() {
        libraryItemsListView.setCellFactory(param -> new ListCell<ClientItem>(){
            @Override
            protected void updateItem(ClientItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hBox = new HBox(10);
                    Label label = new Label(String.format("[%s] %s [%d/%d]",
                            item.getItemType(),
                            item.getTitle(),
                            item.getRemained(),
                            item.getTotal()));
                    Button borrowButton = new Button("Borrow");
                    borrowButton.setDisable(item.getRemained() == 0);

                    borrowButton.setOnAction(event -> borrowItem(item));

                    hBox.getChildren().addAll(label, borrowButton);
                    setGraphic(hBox);
                }
            }
        });
    }

    private void setupBorrowedItemFactory() {
        borrowedItemsListView.setCellFactory(param -> new ListCell<ClientItem>() {
            @Override
            protected void updateItem(ClientItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hBox = new HBox(10);
                    Label label = new Label(String.format("[%s] %s [%d]",
                            item.getItemType(),
                            item.getTitle(),
                            item.getTotal()));
                    Button returnButton = new Button("Return");

                    returnButton.setOnAction(event -> returnItem(item));

                    hBox.getChildren().addAll(label, returnButton);
                    setGraphic(hBox);
                }
            }
        });
    }

    private void borrowItem(ClientItem item) {
        client.borrowItem(item.getId(), (success, message) -> {
            Platform.runLater(() -> {
                if (success) {
                    updateItemList();
                    updateBorrowedItemList();
                    // logger.info("Item borrowed successfully: {}", item.getTitle());
                    logMessage(message, MessageType.SUCCESS);
                } else {
                    // logger.warn("Failed to borrow item: {}", item.getTitle());
                    updateItemList();
                    // Same user connects to the server at the same time
                    updateBorrowedItemList();
                    logMessage(message, MessageType.ERROR);
                }
            });
        });
    }

    private void returnItem(ClientItem item) {
        client.returnItem(item.getId(), (success, message) -> {
            Platform.runLater(() -> {
                if (success) {
                    updateItemList();
                    updateBorrowedItemList();
                    // logger.info("Item returned successfully: {}", item.getTitle());
                    logMessage(message, MessageType.SUCCESS);
                } else {
                    // logger.error("Failed to return item: {}", item.getTitle());
                    updateItemList();
                    updateBorrowedItemList();
                    logMessage(message, MessageType.ERROR);
                }
            });
        });
    }

    public void updateItemList() {
        List<ClientItem> items = client.fetchAllItems();
        Platform.runLater(() -> {
            allItems.setAll(items);
            applyFilters("");
            applySort();
            logger.info("Item list updated with {} items.", items.size());
        });
    }

    public void updateBorrowedItemList() {
        List<ClientItem> borrowedItems = client.fetchBorrowedItems();
        Platform.runLater(() -> {
            borrowedItemsListView.setItems(FXCollections.observableArrayList(borrowedItems));
            logger.info("Borrowed item list updated with {} items.", borrowedItems.size());
        });
    }

    @FXML
    private void handleSort() {
        applySort();
    }

    @FXML
    private void handleFilter() {
        applyFilters("");
        applySort();
    }

    @FXML
    private void handleSearch() {
        applyFilters(searchField.getText().toLowerCase().trim());
        applySort();
    }

    private void applyFilters(String searchText) {
        Stream<ClientItem> filteredStream = allItems.stream();

        // Apply search filter
        if (!searchText.isEmpty()) {
            filteredStream = filteredStream.filter(item -> item.getTitle().toLowerCase().contains(searchText));
        }

        // Apply item type filter
        if (filterByAll.isSelected()) {
            // No need to filter by type if "All" is selected
        } else if (filterByBook.isSelected()) {
            filteredStream = filteredStream.filter(item -> "Book".equals(item.getItemType()));
        } else if (filterByAudiobook.isSelected()) {
            filteredStream = filteredStream.filter(item -> "Audiobook".equals(item.getItemType()));
        } else if (filterByGame.isSelected()) {
            filteredStream = filteredStream.filter(item -> "Game".equals(item.getItemType()));
        } else if (filterByDVD.isSelected()) {
            filteredStream = filteredStream.filter(item -> "DVD".equals(item.getItemType()));
        }

        // Apply availability filter
        if (filterByAvailability.isSelected()) {
            filteredStream = filteredStream.filter(item -> item.getRemained() > 0);
        }

        // Update the ListView with the filtered list
        libraryItemsListView.setItems(filteredStream.collect(Collectors.toCollection(FXCollections::observableArrayList)));
    }

    private void applySort() {
        if (sortByItemType.isSelected()) {
            libraryItemsListView.getItems().sort(Comparator.comparing(ClientItem::getItemType, Comparator.nullsFirst(Comparator.naturalOrder())));
        } else if (sortByTitle.isSelected()) {
            libraryItemsListView.getItems().sort(Comparator.comparing(ClientItem::getTitle, Comparator.nullsFirst(Comparator.naturalOrder())));
        } else if (sortByLastCheckedOutDate.isSelected()) {
            libraryItemsListView.getItems().sort(Comparator.comparing(ClientItem::getLastCheckedOutDate, Comparator.nullsFirst(Comparator.naturalOrder())));
        }
    }

    private enum MessageType {
        INFO, SUCCESS, ERROR, WARNING
    }

    private void logMessage(String message, MessageType type) {
        Text text = new Text(message + "\n");
        text.setFont(Font.font("Monospaced", FontWeight.NORMAL, 15));
        switch (type) {
            case INFO:
                text.setFill(Color.BLUE);
                break;
            case SUCCESS:
                text.setFill(Color.GREEN);
                break;
            case ERROR:
                text.setFill(Color.RED);
                break;
            case WARNING:
                text.setFill(Color.ORANGE);
                break;
        }
        Platform.runLater(() -> logTextFlow.getChildren().add(text));
    }

    @FXML
    private void handleQuit() {
        logger.info("User requested to quit the application.");
        client.stopConnection();
        Platform.exit();
        System.exit(0);
    }

    public void closeConnection() {
        if (client != null) {
            client.stopConnection();
        }
    }
}
