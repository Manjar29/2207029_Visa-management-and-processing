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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

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
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Reject Application");
        confirmAlert.setHeaderText("Reject " + app.getApplicationId() + "?");
        confirmAlert.setContentText("Are you sure you want to reject this application?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("\n=================================================");
            System.out.println("⛔ REJECTING APPLICATION");
            System.out.println("  - Application ID: " + app.getApplicationId());
            System.out.println("  - Admin: " + adminUsername);
            System.out.println("=================================================");
            
            // Update database
            DatabaseManager dbManager = DatabaseManager.getInstance();
            boolean updateSuccess = dbManager.updateApplicationStatus(app.getApplicationId(), "Rejected", null, adminUsername);
            
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
                
                showSuccess("Application rejected.");
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
