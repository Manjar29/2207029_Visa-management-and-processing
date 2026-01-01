package com.visa.management.controllers;

import com.visa.management.VisaManagementApp;
import com.visa.management.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CheckStatusController {

    @FXML
    private TextField applicationIdField;

    @FXML
    private VBox statusContainer;

    @FXML
    private Label statusBadge;

    @FXML
    private Label appIdLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label visaTypeLabel;

    @FXML
    private Label submissionDateLabel;
    
    @FXML
    private Label countryLabel;
    
    @FXML
    private Label visaDurationLabel;
    
    @FXML
    private VBox visaDurationBox;

    @FXML
    private void handleCheckStatus() {
        String applicationId = applicationIdField.getText().trim();

        if (applicationId.isEmpty()) {
            showAlert("Error", "Please enter Application ID", Alert.AlertType.ERROR);
            return;
        }

        // Query database for application
        DatabaseManager dbManager = DatabaseManager.getInstance();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM applicants WHERE application_id = ?")) {
            
            stmt.setString(1, applicationId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Application found - display details
                statusContainer.setVisible(true);
                statusContainer.setManaged(true);
                
                appIdLabel.setText(rs.getString("application_id"));
                nameLabel.setText(rs.getString("first_name") + " " + rs.getString("last_name"));
                visaTypeLabel.setText(rs.getString("visa_type"));
                submissionDateLabel.setText(rs.getString("created_at"));
                countryLabel.setText(rs.getString("country"));
                
                String status = rs.getString("status");
                statusBadge.setText(status);
                
                // Hide duration by default
                if (visaDurationBox != null) {
                    visaDurationBox.setVisible(false);
                    visaDurationBox.setManaged(false);
                }
                
                // Set status color
                switch (status) {
                    case "Processing":
                        statusBadge.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 5; -fx-font-weight: bold;");
                        break;
                    case "Approved":
                        statusBadge.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 5; -fx-font-weight: bold;");
                        String duration = rs.getString("visa_duration");
                        if (duration != null && visaDurationLabel != null && visaDurationBox != null) {
                            visaDurationLabel.setText("Visa Duration: " + duration);
                            visaDurationBox.setVisible(true);
                            visaDurationBox.setManaged(true);
                        }
                        break;
                    case "Rejected":
                        statusBadge.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 5; -fx-font-weight: bold;");
                        break;
                }
                
            } else {
                // Application not found
                statusContainer.setVisible(false);
                statusContainer.setManaged(false);
                showAlert("Not Found", "No application found with ID: " + applicationId, Alert.AlertType.WARNING);
            }
            
        } catch (SQLException e) {
            showAlert("Error", "Failed to check status: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBackToHome() {
        VisaManagementApp.changeScene("/fxml/home.fxml", "Visa Management & Processing System");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void initialize() {
        statusContainer.setVisible(false);
        statusContainer.setManaged(false);
        if (visaDurationBox != null) {
            visaDurationBox.setVisible(false);
            visaDurationBox.setManaged(false);
        }
        System.out.println("Check Status screen initialized");
    }
}
