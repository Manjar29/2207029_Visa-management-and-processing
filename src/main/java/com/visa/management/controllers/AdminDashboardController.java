package com.visa.management.controllers;

import com.visa.management.VisaManagementApp;
import com.visa.management.database.DatabaseManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.layout.Priority;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.List;

public class AdminDashboardController {
    
    @FXML private Label adminLabel;
    @FXML private Label countryLabel;
    @FXML private Label totalApplicationsLabel;
    @FXML private Label processingLabel;
    @FXML private Label approvedLabel;
    @FXML private Label rejectedLabel;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<ApplicationData> applicationsTable;
    @FXML private TableColumn<ApplicationData, String> applicationIdColumn;
    @FXML private TableColumn<ApplicationData, String> applicantNameColumn;
    @FXML private TableColumn<ApplicationData, String> visaTypeColumn;
    @FXML private TableColumn<ApplicationData, String> nationalityColumn;
    @FXML private TableColumn<ApplicationData, String> statusColumn;
    @FXML private TableColumn<ApplicationData, String> dateColumn;
    @FXML private TableColumn<ApplicationData, Void> actionsColumn;
    
    private String adminCountry;
    private String adminUsername;
    
    @FXML
    public void initialize() {
        // Get admin info from session
        VisaApplicationSession session = VisaApplicationSession.getInstance();
        adminUsername = session.getAdminUsername();
        adminCountry = session.getAdminCountry();
        
        // Set labels
        adminLabel.setText("Admin: " + adminUsername);
        countryLabel.setText("Managing Applications for: " + adminCountry);
        
        // Setup table columns
        applicationIdColumn.setCellValueFactory(data -> data.getValue().applicationIdProperty());
        applicantNameColumn.setCellValueFactory(data -> data.getValue().applicantNameProperty());
        visaTypeColumn.setCellValueFactory(data -> data.getValue().visaTypeProperty());
        nationalityColumn.setCellValueFactory(data -> data.getValue().nationalityProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        dateColumn.setCellValueFactory(data -> data.getValue().dateProperty());
        
        // Setup actions column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            private final Button viewBtn = new Button("View");
            private final HBox buttons = new HBox(5, viewBtn, approveBtn, rejectBtn);
            
            {
                System.out.println("Creating action buttons for table cell...");
                approveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");
                rejectBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");
                viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");
                buttons.setAlignment(Pos.CENTER);
                
                approveBtn.setOnAction(event -> {
                    ApplicationData app = getTableView().getItems().get(getIndex());
                    handleApprove(app);
                });
                
                rejectBtn.setOnAction(event -> {
                    ApplicationData app = getTableView().getItems().get(getIndex());
                    handleReject(app);
                });
                
                viewBtn.setOnAction(event -> {
                    try {
                        System.out.println("\n>>> VIEW BUTTON CLICKED IN TABLE <<<");
                        ApplicationData app = getTableView().getItems().get(getIndex());
                        System.out.println(">>> App data retrieved: " + app.getApplicationId());
                        handleView(app);
                    } catch (Exception e) {
                        System.err.println("!!! ERROR IN VIEW BUTTON HANDLER !!!");
                        e.printStackTrace();
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ApplicationData app = getTableView().getItems().get(getIndex());
                    System.out.println("Rendering buttons for: " + app.getApplicationId() + " (Status: " + app.getStatus() + ")");
                    
                    if ("Approved".equals(app.getStatus()) || "Rejected".equals(app.getStatus())) {
                        approveBtn.setDisable(true);
                        rejectBtn.setDisable(true);
                    } else {
                        approveBtn.setDisable(false);
                        rejectBtn.setDisable(false);
                    }
                    setGraphic(buttons);
                    System.out.println("Buttons rendered and visible");
                }
            }
        });
        
        // Set status filter default and populate items
        statusFilter.getItems().addAll("All", "Processing", "Approved", "Rejected");
        statusFilter.setValue("All");
        statusFilter.setOnAction(event -> loadApplications());
        
        // Load data
        loadApplications();
        updateStatistics();
    }
    
    private void loadApplications() {
        System.out.println("\n📋 ========== LOADING APPLICATIONS ==========");
        System.out.println("Country: " + adminCountry);
        System.out.println("Filter: " + statusFilter.getValue());
        
        ObservableList<ApplicationData> applications = FXCollections.observableArrayList();
        
        DatabaseManager dbManager = DatabaseManager.getInstance();
        String query = "SELECT application_id, first_name, last_name, visa_type, nationality, status, created_at " +
                       "FROM applicants WHERE country = ?";
        
        String filterStatus = statusFilter.getValue();
        if (filterStatus != null && !"All".equals(filterStatus)) {
            query += " AND status = ?";
        }
        query += " ORDER BY created_at DESC";
        
        System.out.println("SQL Query: " + query);
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, adminCountry);
            if (filterStatus != null && !"All".equals(filterStatus)) {
                stmt.setString(2, filterStatus);
            }
            
            ResultSet rs = stmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                count++;
                String appId = rs.getString("application_id");
                String status = rs.getString("status");
                String name = rs.getString("first_name") + " " + rs.getString("last_name");
                
                applications.add(new ApplicationData(
                    appId,
                    name,
                    rs.getString("visa_type"),
                    rs.getString("nationality"),
                    status,
                    rs.getString("created_at")
                ));
                
                System.out.println("[" + count + "] " + appId + " | " + name + " | Status: " + status);
            }
            
            System.out.println("\n✓ Total applications loaded: " + count);
            System.out.println("===========================================\n");
            
        } catch (SQLException e) {
            System.err.println("✗ ERROR loading applications: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load applications: " + e.getMessage());
        }
        
        // Use setAll() to properly update existing observable list and trigger cell updates
        applicationsTable.getItems().setAll(applications);
        System.out.println("Table updated with " + applications.size() + " items\n");
    }
    
    private void updateStatistics() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        
        try (Connection conn = dbManager.getConnection()) {
            // Total applications
            String totalQuery = "SELECT COUNT(*) FROM applicants WHERE country = ?";
            try (PreparedStatement stmt = conn.prepareStatement(totalQuery)) {
                stmt.setString(1, adminCountry);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    totalApplicationsLabel.setText(String.valueOf(rs.getInt(1)));
                }
            }
            
            // Processing
            String processingQuery = "SELECT COUNT(*) FROM applicants WHERE country = ? AND status = 'Processing'";
            try (PreparedStatement stmt = conn.prepareStatement(processingQuery)) {
                stmt.setString(1, adminCountry);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    processingLabel.setText(String.valueOf(rs.getInt(1)));
                }
            }
            
            // Approved
            String approvedQuery = "SELECT COUNT(*) FROM applicants WHERE country = ? AND status = 'Approved'";
            try (PreparedStatement stmt = conn.prepareStatement(approvedQuery)) {
                stmt.setString(1, adminCountry);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    approvedLabel.setText(String.valueOf(rs.getInt(1)));
                }
            }
            
            // Rejected
            String rejectedQuery = "SELECT COUNT(*) FROM applicants WHERE country = ? AND status = 'Rejected'";
            try (PreparedStatement stmt = conn.prepareStatement(rejectedQuery)) {
                stmt.setString(1, adminCountry);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    rejectedLabel.setText(String.valueOf(rs.getInt(1)));
                }
            }
            
        } catch (SQLException e) {
            showError("Failed to load statistics: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleApprove(ApplicationData app) {
        // Create dialog for visa duration selection
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Approve Application");
        dialog.setHeaderText("Approve " + app.getApplicationId() + " - " + app.getApplicantName());
        
        // Get duration options based on visa type
        String[] durationOptions = DatabaseManager.getVisaDurationOptions(app.getVisaType());
        
        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(20));
        
        Label label = new Label("Select Visa Duration:");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        ComboBox<String> durationCombo = new ComboBox<>();
        durationCombo.getItems().addAll(durationOptions);
        durationCombo.setValue(durationOptions[0]);
        durationCombo.setPrefWidth(200);
        
        content.getChildren().addAll(label, durationCombo);
        dialog.getDialogPane().setContent(content);
        
        ButtonType approveButton = new ButtonType("Approve", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(approveButton, cancelButton);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == approveButton) {
                return durationCombo.getValue();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(duration -> {
            System.out.println("\n=================================================");
            System.out.println("⏳ APPROVING APPLICATION");
            System.out.println("  - Application ID: " + app.getApplicationId());
            System.out.println("  - Duration: " + duration);
            System.out.println("  - Admin: " + adminUsername);
            System.out.println("=================================================");
            
            // Update database
            DatabaseManager dbManager = DatabaseManager.getInstance();
            boolean updateSuccess = dbManager.updateApplicationStatus(app.getApplicationId(), "Approved", duration, adminUsername);
            
            if (!updateSuccess) {
                System.err.println("✗ DATABASE UPDATE FAILED - NOT REFRESHING UI");
                System.err.println("Check console output above for detailed error information");
                showError("Failed to update application status!\n\n" +
                         "Application ID: " + app.getApplicationId() + "\n" +
                         "Check console for details.");
                return;
            }
            
            System.out.println("\n🔄 REFRESHING UI...");
            
            // Use Platform.runLater to ensure UI update happens on JavaFX thread
            Platform.runLater(() -> {
                // Reload data from database (setAll in loadApplications clears automatically)
                loadApplications();
                updateStatistics();
                
                // Force complete table refresh including all columns
                applicationsTable.refresh();
                for (int i = 0; i < applicationsTable.getColumns().size(); i++) {
                    applicationsTable.getColumns().get(i).setVisible(false);
                    applicationsTable.getColumns().get(i).setVisible(true);
                }
                
                System.out.println("✓ UI REFRESH COMPLETED");
                System.out.println("=================================================\n");
                
                showSuccess("Application approved successfully!\nVisa Duration: " + duration);
            });
        });
    }
    
    @FXML
    private void handleReject(ApplicationData app) {
        // Create custom dialog with rejection reason and ban duration
        Dialog<ButtonType> rejectDialog = new Dialog<>();
        rejectDialog.setTitle("Reject Application");
        rejectDialog.setHeaderText("Reject " + app.getApplicationId());
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);
        
        Label reasonLabel = new Label("Rejection Reason:");
        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Enter the reason for rejection...");
        reasonArea.setPrefRowCount(4);
        reasonArea.setWrapText(true);
        
        Label banLabel = new Label("Ban Duration:");
        ComboBox<String> banDurationCombo = new ComboBox<>();
        banDurationCombo.getItems().addAll(
            "No Ban",
            "1 Month",
            "3 Months", 
            "6 Months",
            "1 Year",
            "2 Years",
            "5 Years",
            "Permanent"
        );
        banDurationCombo.setValue("No Ban");
        
        Label warningLabel = new Label("⚠️ Ban will prevent re-application based on NID, Passport, and Country");
        warningLabel.setStyle("-fx-text-fill: #f44336; -fx-font-size: 10px;");
        warningLabel.setWrapText(true);
        
        content.getChildren().addAll(reasonLabel, reasonArea, banLabel, banDurationCombo, warningLabel);
        rejectDialog.getDialogPane().setContent(content);
        rejectDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = rejectDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String reason = reasonArea.getText().trim();
            if (reason.isEmpty()) {
                showError("Rejection reason cannot be empty");
                return;
            }
            
            String banDuration = banDurationCombo.getValue();
            final int banMonths;
            
            switch (banDuration) {
                case "1 Month": banMonths = 1; break;
                case "3 Months": banMonths = 3; break;
                case "6 Months": banMonths = 6; break;
                case "1 Year": banMonths = 12; break;
                case "2 Years": banMonths = 24; break;
                case "5 Years": banMonths = 60; break;
                case "Permanent": banMonths = 999; break;
                default: banMonths = 0;
            }
            
            System.out.println("\n=================================================");
            System.out.println("⛔ REJECTING APPLICATION");
            System.out.println("  - Application ID: " + app.getApplicationId());
            System.out.println("  - Admin: " + adminUsername);
            System.out.println("  - Reason: " + reason);
            System.out.println("  - Ban Duration: " + banDuration + " (" + banMonths + " months)");
            System.out.println("=================================================");
            
            // Get applicant details for rejection history
            DatabaseManager dbManager = DatabaseManager.getInstance();
            
            String nationalId = null;
            String passport = null;
            String country = null;
            
            // First, get applicant details and close the connection
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT national_id, passport, country FROM applicants WHERE application_id = ?")) {
                
                stmt.setString(1, app.getApplicationId());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    nationalId = rs.getString("national_id");
                    passport = rs.getString("passport");
                    country = rs.getString("country");
                }
            } catch (SQLException e) {
                System.err.println("✗ DATABASE ERROR: " + e.getMessage());
                e.printStackTrace();
                showError("Failed to retrieve applicant details: " + e.getMessage());
                return;
            }
            
            if (nationalId == null || passport == null || country == null) {
                showError("Failed to retrieve applicant information");
                return;
            }
            
            // Now update application status with connection properly closed
            boolean updateSuccess = dbManager.updateApplicationStatus(app.getApplicationId(), "Rejected", null, adminUsername);
            
            if (!updateSuccess) {
                showError("Failed to update application status");
                return;
            }
            
            // Add to rejection history if ban duration is set
            if (banMonths > 0) {
                boolean historyAdded = dbManager.addRejectionHistory(
                    app.getApplicationId(), 
                    nationalId, 
                    passport, 
                    country, 
                    reason, 
                    banMonths, 
                    adminUsername
                );
                
                if (!historyAdded) {
                    System.err.println("⚠️ Failed to add rejection history, but application was rejected");
                }
            }
            
            System.out.println("\n🔄 REFRESHING UI...");
            
            final String finalCountry = country;
            Platform.runLater(() -> {
                loadApplications();
                updateStatistics();
                applicationsTable.refresh();
                for (int i = 0; i < applicationsTable.getColumns().size(); i++) {
                    applicationsTable.getColumns().get(i).setVisible(false);
                    applicationsTable.getColumns().get(i).setVisible(true);
                }
                
                System.out.println("✓ UI REFRESH COMPLETED");
                System.out.println("=================================================\n");
                
                String message = "Application rejected.\n\nReason: " + reason;
                if (banMonths > 0) {
                    message += "\n\nBan Duration: " + banDuration;
                    message += "\nApplicant cannot reapply with same NID/Passport to " + finalCountry;
                }
                showSuccess(message);
            });
        }
    }
    
    private void handleView(ApplicationData app) {
        System.out.println("===========================================");
        System.out.println("VIEW BUTTON CLICKED");
        System.out.println("Application ID: " + app.getApplicationId());
        System.out.println("===========================================");
        
        try {
            // Navigate to application details page
            ApplicationDetailsController controller = VisaManagementApp.changeSceneWithController(
                "/fxml/application-details.fxml", 
                "Application Details - " + app.getApplicationId()
            );
            
            System.out.println("Controller retrieved: " + (controller != null ? "SUCCESS" : "NULL"));
            
            if (controller != null) {
                controller.setApplicationId(app.getApplicationId());
                System.out.println("Application ID set on controller");
            } else {
                System.err.println("ERROR: Controller is null!");
            }
        } catch (Exception e) {
            System.err.println("ERROR in handleView: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to open application details: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleViewMessages() {
        System.out.println("=== VIEWING MESSAGES ===");
        System.out.println("Admin Country: " + adminCountry);
        
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Applicant Messages - " + adminCountry);
        dialog.setHeaderText("Messages from applicants in " + adminCountry);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefSize(800, 600);
        
        // Get messages from database
        DatabaseManager dbManager = DatabaseManager.getInstance();
        List<DatabaseManager.ApplicantMessage> messages = dbManager.getAllMessagesForCountry(adminCountry);
        
        System.out.println("Messages found: " + (messages != null ? messages.size() : "null"));
        
        if (messages == null || messages.isEmpty()) {
            Label noMessages = new Label("📭 No messages yet from applicants in " + adminCountry);
            noMessages.setStyle("-fx-font-size: 16px; -fx-text-fill: gray; -fx-padding: 50;");
            
            Label infoLabel = new Label("Messages will appear here when applicants with 'Processing' status send messages.");
            infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-padding: 10;");
            infoLabel.setWrapText(true);
            infoLabel.setMaxWidth(600);
            
            VBox emptyBox = new VBox(20, noMessages, infoLabel);
            emptyBox.setAlignment(Pos.CENTER);
            content.getChildren().add(emptyBox);
        } else {
            // Create table for messages
            TableView<DatabaseManager.ApplicantMessage> messageTable = new TableView<>();
            messageTable.setPrefHeight(400);
            
            TableColumn<DatabaseManager.ApplicantMessage, String> appIdCol = new TableColumn<>("Application ID");
            appIdCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApplicationId()));
            appIdCol.setPrefWidth(150);
            
            TableColumn<DatabaseManager.ApplicantMessage, String> messageCol = new TableColumn<>("Message");
            messageCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMessage()));
            messageCol.setPrefWidth(350);
            
            TableColumn<DatabaseManager.ApplicantMessage, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
            statusCol.setPrefWidth(80);
            
            TableColumn<DatabaseManager.ApplicantMessage, String> dateCol = new TableColumn<>("Date");
            dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreatedAt()));
            dateCol.setPrefWidth(150);
            
            @SuppressWarnings("unchecked")
            TableColumn<DatabaseManager.ApplicantMessage, ?>[] messageCols = new TableColumn[]{appIdCol, messageCol, statusCol, dateCol};
            messageTable.getColumns().addAll(messageCols);
            messageTable.setItems(FXCollections.observableArrayList(messages));
            
            // Buttons for message actions
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER);
            
            Button viewButton = new Button("View Full Message");
            viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            viewButton.setOnAction(e -> {
                DatabaseManager.ApplicantMessage selected = messageTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showFullMessage(selected);
                } else {
                    showAlert("No Selection", "Please select a message to view", Alert.AlertType.WARNING);
                }
            });
            
            Button markReadButton = new Button("Mark as Read");
            markReadButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            markReadButton.setOnAction(e -> {
                DatabaseManager.ApplicantMessage selected = messageTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    if (dbManager.markMessageAsRead(selected.getId())) {
                        selected.setStatus("READ");
                        messageTable.refresh();
                        showAlert("Success", "Message marked as read", Alert.AlertType.INFORMATION);
                    }
                } else {
                    showAlert("No Selection", "Please select a message", Alert.AlertType.WARNING);
                }
            });
            
            Button historyButton = new Button("View Applicant History");
            historyButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
            historyButton.setOnAction(e -> {
                DatabaseManager.ApplicantMessage selected = messageTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showApplicantHistory(selected.getApplicationId());
                } else {
                    showAlert("No Selection", "Please select a message", Alert.AlertType.WARNING);
                }
            });
            
            buttonBox.getChildren().addAll(viewButton, markReadButton, historyButton);
            content.getChildren().addAll(messageTable, buttonBox);
        }
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    private void showFullMessage(DatabaseManager.ApplicantMessage message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Message Details");
        alert.setHeaderText("From Application: " + message.getApplicationId());
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label dateLabel = new Label("Date: " + message.getCreatedAt());
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");
        
        Label messageLabel = new Label("Message:");
        messageLabel.setStyle("-fx-font-weight: bold;");
        
        TextArea messageArea = new TextArea(message.getMessage());
        messageArea.setWrapText(true);
        messageArea.setEditable(false);
        messageArea.setPrefRowCount(6);
        
        content.getChildren().addAll(dateLabel, messageLabel, messageArea);
        
        if (message.getAdminReply() != null && !message.getAdminReply().isEmpty()) {
            Label replyLabel = new Label("Admin Reply:");
            replyLabel.setStyle("-fx-font-weight: bold;");
            
            TextArea replyArea = new TextArea(message.getAdminReply());
            replyArea.setWrapText(true);
            replyArea.setEditable(false);
            replyArea.setPrefRowCount(4);
            
            content.getChildren().addAll(replyLabel, replyArea);
        }
        
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
    
    private void showApplicantHistory(String applicationId) {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT national_id, passport, nationality FROM applicants WHERE application_id = ?")) {
            
            stmt.setString(1, applicationId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String nationalId = rs.getString("national_id");
                String passport = rs.getString("passport");
                String nationality = rs.getString("nationality");
                
                showTravelHistory(nationalId, passport, nationality);
            } else {
                showError("Application not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to retrieve applicant details: " + e.getMessage());
        }
    }
    
    private void showTravelHistory(String nationalId, String passport, String nationality) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Complete Travel History");
        dialog.setHeaderText("All visa applications | Nationality: " + nationality + " | NID: " + nationalId + " | Passport: " + passport);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefSize(900, 500);
        
        DatabaseManager dbManager = DatabaseManager.getInstance();
        List<DatabaseManager.TravelHistory> history = dbManager.getTravelHistory(nationalId, passport, nationality);
        
        if (history.isEmpty()) {
            Label noHistory = new Label("No travel history found.");
            noHistory.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
            content.getChildren().add(noHistory);
        } else {
            TableView<DatabaseManager.TravelHistory> historyTable = new TableView<>();
            historyTable.setPrefHeight(400);
            
            TableColumn<DatabaseManager.TravelHistory, String> appIdCol = new TableColumn<>("App ID");
            appIdCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApplicationId()));
            appIdCol.setPrefWidth(120);
            
            TableColumn<DatabaseManager.TravelHistory, String> countryCol = new TableColumn<>("Country");
            countryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCountry()));
            countryCol.setPrefWidth(100);
            
            TableColumn<DatabaseManager.TravelHistory, String> visaTypeCol = new TableColumn<>("Visa Type");
            visaTypeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVisaType()));
            visaTypeCol.setPrefWidth(120);
            
            TableColumn<DatabaseManager.TravelHistory, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
            statusCol.setPrefWidth(100);
            
            TableColumn<DatabaseManager.TravelHistory, String> dateCol = new TableColumn<>("Date");
            dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAppliedDate()));
            dateCol.setPrefWidth(150);
            
            TableColumn<DatabaseManager.TravelHistory, String> rejectionCol = new TableColumn<>("Rejection Reason");
            rejectionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRejectionReason() != null ? data.getValue().getRejectionReason() : ""));
            rejectionCol.setPrefWidth(200);
            
            TableColumn<DatabaseManager.TravelHistory, String> banCol = new TableColumn<>("Ban Until");
            banCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBanUntilDate() != null ? data.getValue().getBanUntilDate() : ""));
            banCol.setPrefWidth(110);
            
            @SuppressWarnings("unchecked")
            TableColumn<DatabaseManager.TravelHistory, ?>[] historyCols = new TableColumn[]{appIdCol, countryCol, visaTypeCol, statusCol, dateCol, rejectionCol, banCol};
            historyTable.getColumns().addAll(historyCols);
            historyTable.setItems(FXCollections.observableArrayList(history));
            
            content.getChildren().add(historyTable);
        }
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    @FXML
    private void handleRefresh() {
        System.out.println("Manual refresh triggered...");
        loadApplications();
        updateStatistics();
        applicationsTable.refresh();
        System.out.println("Refresh completed.");
    }
    
    @FXML
    private void handleLogout() {
        VisaApplicationSession.getInstance().clear();
        VisaManagementApp.changeScene("/fxml/home.fxml", "Visa Management & Processing System");
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void handleManageNotices() {
        showNoticeManagementDialog();
    }
    
    private void showNoticeManagementDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Manage Notices - " + adminCountry);
        dialog.setHeaderText("Add, Edit, or Delete notices for applicants in " + adminCountry);
        
        // Create main content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefSize(700, 500);
        
        // Notice list
        ListView<DatabaseManager.Notice> noticeListView = new ListView<>();
        noticeListView.setPrefHeight(250);
        noticeListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(DatabaseManager.Notice notice, boolean empty) {
                super.updateItem(notice, empty);
                if (empty || notice == null) {
                    setText(null);
                } else {
                    String icon = notice.getNoticeType().equals("RULES") ? "📋" : 
                                 notice.getNoticeType().equals("REJECTION_REASONS") ? "⚠️" : "ℹ️";
                    setText(icon + " " + notice.getTitle() + " [" + notice.getNoticeType() + "]");
                }
            }
        });
        
        // Load existing notices
        loadNotices(noticeListView);
        
        // Buttons for notice management
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button addButton = new Button("➕ Add Notice");
        addButton.setOnAction(e -> showAddEditNoticeDialog(null, noticeListView));
        
        Button editButton = new Button("✏️ Edit Notice");
        editButton.setOnAction(e -> {
            DatabaseManager.Notice selected = noticeListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showAddEditNoticeDialog(selected, noticeListView);
            } else {
                showAlert("No Selection", "Please select a notice to edit", Alert.AlertType.WARNING);
            }
        });
        
        Button deleteButton = new Button("🗑️ Delete Notice");
        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> {
            DatabaseManager.Notice selected = noticeListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirm Deletion");
                confirmAlert.setHeaderText("Delete Notice");
                confirmAlert.setContentText("Are you sure you want to delete this notice?\n\n" + selected.getTitle());
                
                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    DatabaseManager dbManager = DatabaseManager.getInstance();
                    if (dbManager.deleteNotice(selected.getId())) {
                        loadNotices(noticeListView);
                        showAlert("Success", "Notice deleted successfully", Alert.AlertType.INFORMATION);
                    } else {
                        showError("Failed to delete notice");
                    }
                }
            } else {
                showAlert("No Selection", "Please select a notice to delete", Alert.AlertType.WARNING);
            }
        });
        
        buttonBox.getChildren().addAll(addButton, editButton, deleteButton);
        
        content.getChildren().addAll(
            new Label("Existing Notices:"),
            noticeListView,
            buttonBox
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    private void loadNotices(ListView<DatabaseManager.Notice> listView) {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        List<DatabaseManager.Notice> notices = dbManager.getNotices(adminCountry);
        ObservableList<DatabaseManager.Notice> items = FXCollections.observableArrayList(notices);
        listView.setItems(items);
    }
    
    private void showAddEditNoticeDialog(DatabaseManager.Notice existingNotice, ListView<DatabaseManager.Notice> parentListView) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(existingNotice == null ? "Add New Notice" : "Edit Notice");
        dialog.setHeaderText(existingNotice == null ? "Create a new notice for " + adminCountry : "Modify the notice");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(600);
        
        // Title field
        TextField titleField = new TextField();
        titleField.setPromptText("Enter notice title");
        if (existingNotice != null) titleField.setText(existingNotice.getTitle());
        
        // Notice type selector
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("RULES", "REJECTION_REASONS", "GENERAL");
        typeCombo.setValue(existingNotice != null ? existingNotice.getNoticeType() : "GENERAL");
        
        // Content area
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Enter notice content");
        contentArea.setPrefRowCount(10);
        contentArea.setWrapText(true);
        if (existingNotice != null) contentArea.setText(existingNotice.getContent());
        
        content.getChildren().addAll(
            new Label("Title:"),
            titleField,
            new Label("Type:"),
            typeCombo,
            new Label("Content:"),
            contentArea
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String title = titleField.getText().trim();
                String noticeType = typeCombo.getValue();
                String contentText = contentArea.getText().trim();
                
                if (title.isEmpty() || contentText.isEmpty()) {
                    showAlert("Validation Error", "Title and content cannot be empty", Alert.AlertType.ERROR);
                    return null;
                }
                
                DatabaseManager dbManager = DatabaseManager.getInstance();
                boolean success;
                
                if (existingNotice == null) {
                    // Add new notice
                    success = dbManager.saveNotice(adminCountry, title, contentText, noticeType, adminUsername);
                } else {
                    // Update existing notice
                    success = dbManager.saveNotice(adminCountry, title, contentText, noticeType, adminUsername);
                }
                
                if (success) {
                    loadNotices(parentListView);
                    showAlert("Success", 
                        existingNotice == null ? "Notice added successfully" : "Notice updated successfully", 
                        Alert.AlertType.INFORMATION);
                } else {
                    showError("Failed to save notice");
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Inner class for table data
    public static class ApplicationData {
        private final SimpleStringProperty applicationId;
        private final SimpleStringProperty applicantName;
        private final SimpleStringProperty visaType;
        private final SimpleStringProperty nationality;
        private final SimpleStringProperty status;
        private final SimpleStringProperty date;
        
        public ApplicationData(String applicationId, String applicantName, String visaType, 
                             String nationality, String status, String date) {
            this.applicationId = new SimpleStringProperty(applicationId);
            this.applicantName = new SimpleStringProperty(applicantName);
            this.visaType = new SimpleStringProperty(visaType);
            this.nationality = new SimpleStringProperty(nationality);
            this.status = new SimpleStringProperty(status);
            this.date = new SimpleStringProperty(date);
        }
        
        public String getApplicationId() { return applicationId.get(); }
        public SimpleStringProperty applicationIdProperty() { return applicationId; }
        
        public String getApplicantName() { return applicantName.get(); }
        public SimpleStringProperty applicantNameProperty() { return applicantName; }
        
        public String getVisaType() { return visaType.get(); }
        public SimpleStringProperty visaTypeProperty() { return visaType; }
        
        public String getNationality() { return nationality.get(); }
        public SimpleStringProperty nationalityProperty() { return nationality; }
        
        public String getStatus() { return status.get(); }
        public SimpleStringProperty statusProperty() { return status; }
        
        public String getDate() { return date.get(); }
        public SimpleStringProperty dateProperty() { return date; }
    }
}
