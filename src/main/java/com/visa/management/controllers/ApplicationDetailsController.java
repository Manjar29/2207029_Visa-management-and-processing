package com.visa.management.controllers;

import com.visa.management.VisaManagementApp;
import com.visa.management.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;

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
    
    private String applicationId;
    
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        loadApplicationDetails();
    }
    
    @FXML
    public void initialize() {
        // Will be called before setApplicationId
    }
    
    private void loadApplicationDetails() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM applicants WHERE application_id = ?")) {
            
            stmt.setString(1, applicationId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
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
                
                String status = rs.getString("status");
                statusLabel.setText(status);
                
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
                
                // Load visa-specific details
                loadVisaSpecificDetails();
                
            } else {
                showError("Application not found!");
            }
            
        } catch (SQLException e) {
            showError("Failed to load application details: " + e.getMessage());
        }
    }
    
    private void loadVisaSpecificDetails() {
        // This can be expanded to show visa-specific fields from visa_details table
        // For now, basic info is shown
    }
    
    @FXML
    private void handleBack() {
        VisaManagementApp.changeScene("/fxml/admin-dashboard.fxml", "Admin Dashboard");
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
