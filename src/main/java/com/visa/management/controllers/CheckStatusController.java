package com.visa.management.controllers;

import com.visa.management.VisaManagementApp;
import com.visa.management.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
    private Label approvedByLabel;
    
    @FXML
    private VBox approvedByBox;
    
    @FXML
    private Label approvalDateLabel;
    
    @FXML
    private VBox approvalDateBox;
    
    // Timeline elements
    @FXML
    private VBox timelineBox;
    
    @FXML
    private HBox timeline1;
    @FXML
    private Label timeline1Marker;
    @FXML
    private Label timeline1Title;
    @FXML
    private Label timeline1Date;
    
    @FXML
    private HBox timeline2;
    @FXML
    private Label timeline2Marker;
    @FXML
    private Label timeline2Title;
    @FXML
    private Label timeline2Date;
    
    @FXML
    private HBox timeline3;
    @FXML
    private Label timeline3Marker;
    @FXML
    private Label timeline3Title;
    @FXML
    private Label timeline3Date;

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
                String submittedDate = rs.getString("created_at");
                String duration = rs.getString("visa_duration");
                String approvedBy = rs.getString("approved_by");
                String approvalDate = rs.getString("approval_date");
                
                statusBadge.setText(status);
                
                // Hide optional fields by default
                hideOptionalFields();
                
                // Update timeline and display based on status
                updateTimeline(status, submittedDate, approvalDate);
                
                // Set status color and show relevant information
                switch (status) {
                    case "Processing":
                        statusBadge.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 5; -fx-font-weight: bold;");
                        break;
                        
                    case "Approved":
                        statusBadge.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 5; -fx-font-weight: bold;");
                        
                        // Show visa duration
                        if (duration != null && visaDurationLabel != null && visaDurationBox != null) {
                            visaDurationLabel.setText("Visa Duration: " + duration);
                            visaDurationBox.setVisible(true);
                            visaDurationBox.setManaged(true);
                        }
                        
                        // Show approved by
                        if (approvedBy != null && approvedByLabel != null && approvedByBox != null) {
                            approvedByLabel.setText(approvedBy);
                            approvedByBox.setVisible(true);
                            approvedByBox.setManaged(true);
                        }
                        
                        // Show approval date
                        if (approvalDate != null && approvalDateLabel != null && approvalDateBox != null) {
                            approvalDateLabel.setText(approvalDate);
                            approvalDateBox.setVisible(true);
                            approvalDateBox.setManaged(true);
                        }
                        break;
                        
                    case "Rejected":
                        statusBadge.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 5; -fx-font-weight: bold;");
                        
                        // Show who rejected
                        if (approvedBy != null && approvedByLabel != null && approvedByBox != null) {
                            approvedByLabel.setText(approvedBy);
                            approvedByLabel.setStyle("-fx-text-fill: #f44336;");
                            approvedByBox.setVisible(true);
                            approvedByBox.setManaged(true);
                        }
                        
                        // Show rejection date
                        if (approvalDate != null && approvalDateLabel != null && approvalDateBox != null) {
                            approvalDateLabel.setText(approvalDate);
                            approvalDateLabel.setStyle("-fx-text-fill: #f44336;");
                            approvalDateBox.setVisible(true);
                            approvalDateBox.setManaged(true);
                        }
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
    
    private void hideOptionalFields() {
        if (visaDurationBox != null) {
            visaDurationBox.setVisible(false);
            visaDurationBox.setManaged(false);
        }
        if (approvedByBox != null) {
            approvedByBox.setVisible(false);
            approvedByBox.setManaged(false);
        }
        if (approvalDateBox != null) {
            approvalDateBox.setVisible(false);
            approvalDateBox.setManaged(false);
        }
    }
    
    private void updateTimeline(String status, String submittedDate, String approvalDate) {
        // Timeline Item 1: Submitted (always completed)
        timeline1Marker.setText("✓");
        timeline1Marker.setStyle("");
        timeline1.getStyleClass().clear();
        timeline1.getStyleClass().add("timeline-item-completed");
        timeline1Title.setText("Application Submitted");
        timeline1Date.setText(submittedDate != null ? submittedDate : "");
        
        // Timeline Item 2: Processing
        if (status.equals("Processing")) {
            // Currently processing
            timeline2Marker.setText("⟳");
            timeline2Marker.setStyle("");
            timeline2.getStyleClass().clear();
            timeline2.getStyleClass().add("timeline-item-active");
            timeline2Title.setText("Under Review");
            timeline2Date.setText("In Progress");
            
            // Timeline Item 3: Decision pending
            timeline3Marker.setText("○");
            timeline3Marker.setStyle("");
            timeline3.getStyleClass().clear();
            timeline3.getStyleClass().add("timeline-item-pending");
            timeline3Title.setText("Decision Pending");
            timeline3Date.setText("Awaiting");
            
        } else if (status.equals("Approved")) {
            // Processing completed
            timeline2Marker.setText("✓");
            timeline2Marker.setStyle("");
            timeline2.getStyleClass().clear();
            timeline2.getStyleClass().add("timeline-item-completed");
            timeline2Title.setText("Review Completed");
            timeline2Date.setText("Completed");
            
            // Approved
            timeline3Marker.setText("✓");
            timeline3Marker.setStyle("-fx-text-fill: #4CAF50;");
            timeline3.getStyleClass().clear();
            timeline3.getStyleClass().add("timeline-item-completed");
            timeline3Title.setText("✓ Visa Approved");
            timeline3Title.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            timeline3Date.setText(approvalDate != null ? approvalDate : "");
            timeline3Date.setStyle("-fx-text-fill: #4CAF50;");
            
        } else if (status.equals("Rejected")) {
            // Processing completed
            timeline2Marker.setText("✓");
            timeline2Marker.setStyle("");
            timeline2.getStyleClass().clear();
            timeline2.getStyleClass().add("timeline-item-completed");
            timeline2Title.setText("Review Completed");
            timeline2Date.setText("Completed");
            
            // Rejected
            timeline3Marker.setText("✗");
            timeline3Marker.setStyle("-fx-text-fill: #f44336;");
            timeline3.getStyleClass().clear();
            timeline3.getStyleClass().add("timeline-item-completed");
            timeline3Title.setText("✗ Visa Rejected");
            timeline3Title.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
            timeline3Date.setText(approvalDate != null ? approvalDate : "");
            timeline3Date.setStyle("-fx-text-fill: #f44336;");
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
        hideOptionalFields();
        System.out.println("Check Status screen initialized");
    }
}
