package com.visa.management.controllers;

import com.visa.management.VisaManagementApp;
import com.visa.management.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ApplicationDetailsController {

    @FXML private Label applicationIdLabel;
    @FXML private Label statusLabel;
    @FXML private Label firstNameLabel;
    @FXML private Label lastNameLabel;
    @FXML private Label nationalIdLabel;
    @FXML private Label nationalityLabel;
    @FXML private Label passportLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label addressLabel;
    @FXML private Label countryLabel;
    @FXML private Label visaTypeLabel;
    @FXML private Label appliedDateLabel;
    @FXML private Label visaDurationLabel;
    @FXML private Label approvedByLabel;
    @FXML private Label approvalDateLabel;
    @FXML private VBox visaSpecificDetailsBox;
    @FXML private VBox documentsBox;
    
    private String applicationId;
    
    public void setApplicationId(String applicationId) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     APPLICATION DETAILS CONTROLLER                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("Setting Application ID: " + applicationId);
        this.applicationId = applicationId;
        loadApplicationDetails();
    }
    
    @FXML
    public void initialize() {
        System.out.println("ApplicationDetailsController initialized");
        // Will be called before setApplicationId
    }
    
    private void loadApplicationDetails() {
        System.out.println("Loading application details for: " + applicationId);
        DatabaseManager dbManager = DatabaseManager.getInstance();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM applicants WHERE application_id = ?")) {
            
            stmt.setString(1, applicationId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("✓ Application found in database");
                
                applicationIdLabel.setText(rs.getString("application_id"));
                firstNameLabel.setText(rs.getString("first_name"));
                lastNameLabel.setText(rs.getString("last_name"));
                nationalIdLabel.setText(rs.getString("national_id"));
                nationalityLabel.setText(rs.getString("nationality"));
                passportLabel.setText(rs.getString("passport"));
                emailLabel.setText(rs.getString("email"));
                phoneLabel.setText(rs.getString("phone"));
                addressLabel.setText(rs.getString("address"));
                countryLabel.setText(rs.getString("country"));
                visaTypeLabel.setText(rs.getString("visa_type"));
                appliedDateLabel.setText(rs.getString("created_at"));
                
                System.out.println("✓ Basic info loaded");
                
                String status = rs.getString("status");
                statusLabel.setText(status);
                
                System.out.println("✓ Status: " + status);
                
                // Set status color
                switch (status) {
                    case "Processing":
                        statusLabel.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 5; -fx-font-weight: bold;");
                        break;
                    case "Approved":
                        statusLabel.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 5; -fx-font-weight: bold;");
                        
                        // Show approval details
                        String duration = rs.getString("visa_duration");
                        String approvedBy = rs.getString("approved_by");
                        String approvalDate = rs.getString("approval_date");
                        
                        if (duration != null) {
                            visaDurationLabel.setText(duration);
                            visaDurationLabel.setVisible(true);
                        }
                        if (approvedBy != null) {
                            approvedByLabel.setText(approvedBy);
                            approvedByLabel.setVisible(true);
                        }
                        if (approvalDate != null) {
                            approvalDateLabel.setText(approvalDate);
                            approvalDateLabel.setVisible(true);
                        }
                        break;
                    case "Rejected":
                        statusLabel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 5; -fx-font-weight: bold;");
                        break;
                }
                
                System.out.println("✓ Loading visa-specific details...");
                // Load visa-specific details
                loadVisaSpecificDetails();
                
                System.out.println("✓ Application details loaded successfully");
                
            } else {
                System.err.println("✗ Application not found in database!");
                showError("Application not found!");
            }
            
        } catch (SQLException e) {
            System.err.println("✗ SQL Error: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load application details: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("✗ Unexpected Error: " + e.getMessage());
            e.printStackTrace();
            showError("Unexpected error: " + e.getMessage());
        }
    }
    
    private void loadVisaSpecificDetails() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT field_name, field_value FROM visa_details WHERE application_id = ? ORDER BY field_name")) {
            
            stmt.setString(1, applicationId);
            ResultSet rs = stmt.executeQuery();
            
            // Clear existing content
            if (visaSpecificDetailsBox != null) {
                visaSpecificDetailsBox.getChildren().clear();
                
                boolean hasDetails = false;
                FlowPane flowPane = new FlowPane();
                flowPane.setHgap(25);
                flowPane.setVgap(18);
                flowPane.setPrefWrapLength(1300); // Wrap at 1300px
                
                while (rs.next()) {
                    hasDetails = true;
                    String fieldName = rs.getString("field_name");
                    String fieldValue = rs.getString("field_value");
                    
                    if (fieldValue != null && !fieldValue.trim().isEmpty()) {
                        VBox fieldBox = createFieldBox(formatFieldName(fieldName), fieldValue);
                        fieldBox.setMinWidth(300);
                        fieldBox.setMaxWidth(350);
                        flowPane.getChildren().add(fieldBox);
                    }
                }
                
                if (hasDetails) {
                    visaSpecificDetailsBox.getChildren().add(flowPane);
                    visaSpecificDetailsBox.setVisible(true);
                    visaSpecificDetailsBox.setManaged(true);
                } else {
                    visaSpecificDetailsBox.setVisible(false);
                    visaSpecificDetailsBox.setManaged(false);
                }
            }
            
            // Load documents info
            loadDocumentsInfo();
            
        } catch (SQLException e) {
            System.err.println("Error loading visa-specific details: " + e.getMessage());
        }
    }
    
    private void loadDocumentsInfo() {
        if (documentsBox != null) {
            documentsBox.getChildren().clear();
            
            // Get documents from database
            DatabaseManager dbManager = DatabaseManager.getInstance();
            java.util.List<DatabaseManager.DocumentInfo> documents = dbManager.getDocuments(applicationId);
            
            if (documents.isEmpty()) {
                // No documents uploaded
                Label noDocLabel = new Label("📎 No Documents Uploaded");
                noDocLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #999;");
                documentsBox.getChildren().add(noDocLabel);
            } else {
                // Add header
                Label headerLabel = new Label("📎 Uploaded Documents (" + documents.size() + ")");
                headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #4CAF50; -fx-padding: 0 0 10 0;");
                documentsBox.getChildren().add(headerLabel);
                
                // Add each document
                for (DatabaseManager.DocumentInfo doc : documents) {
                    VBox docBox = createDocumentBox(doc);
                    documentsBox.getChildren().add(docBox);
                }
            }
            
            documentsBox.setVisible(true);
            documentsBox.setManaged(true);
        }
    }
    
    private VBox createDocumentBox(DatabaseManager.DocumentInfo doc) {
        VBox box = new VBox(5);
        box.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        // Filename with icon
        Label filenameLabel = new Label("📄 " + doc.getFilename());
        filenameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #333;");
        
        // File info (type and size)
        String sizeStr = formatFileSize(doc.getFileSize());
        Label infoLabel = new Label("Type: " + doc.getFileType().toUpperCase() + " | Size: " + sizeStr);
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        
        // Upload date
        Label dateLabel = new Label("Uploaded: " + doc.getUploadedAt());
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        
        // View button
        javafx.scene.control.Button viewBtn = new javafx.scene.control.Button("View File");
        viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 15;");
        viewBtn.setOnAction(e -> openDocument(doc.getFilePath()));
        
        box.getChildren().addAll(filenameLabel, infoLabel, dateLabel, viewBtn);
        return box;
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
    
    private void openDocument(String filePath) {
        try {
            java.io.File file = new java.io.File(filePath);
            if (file.exists()) {
                // Open file with default system application
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(file);
                } else {
                    showAlert("Error", "Cannot open file on this system.", javafx.scene.control.Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Error", "File not found: " + filePath, javafx.scene.control.Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to open document: " + e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    private VBox createFieldBox(String label, String value) {
        VBox box = new VBox(5);
        box.setStyle("-fx-background-color: #fafafa; -fx-padding: 12; -fx-background-radius: 5;");
        
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #666;");
        
        Label valueNode = new Label(value != null ? value : "-");
        valueNode.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        valueNode.setWrapText(true);
        valueNode.setMaxWidth(320);
        
        box.getChildren().addAll(labelNode, valueNode);
        return box;
    }
    
    private String formatFieldName(String fieldName) {
        // Convert snake_case to Title Case
        String[] words = fieldName.split("_");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (word.length() > 0) {
                formatted.append(Character.toUpperCase(word.charAt(0)))
                         .append(word.substring(1).toLowerCase())
                         .append(" ");
            }
        }
        
        return formatted.toString().trim();
    }
    
    @FXML
    private void handleBack() {
        System.out.println("Going back to Admin Dashboard...");
        // Session should still have admin info, so just change scene
        VisaManagementApp.changeScene("/fxml/admin-dashboard.fxml", "Admin Dashboard");
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
