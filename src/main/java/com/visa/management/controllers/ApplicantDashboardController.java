package com.visa.management.controllers;

import com.visa.management.VisaManagementApp;
import com.visa.management.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ApplicantDashboardController {
    
    @FXML private Label welcomeLabel;
    @FXML private Label applicationIdLabel;
    @FXML private Label statusLabel;
    @FXML private Label countryLabel;
    @FXML private Label visaTypeLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label nationalityLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label appliedDateLabel;
    @FXML private Label visaDurationLabel;
    @FXML private Label approvedByLabel;
    @FXML private Label approvalDateLabel;
    @FXML private VBox visaConditionsBox;
    @FXML private Button messageButton;
    
    private String applicantId;
    private String applicantName;
    private String currentStatus;
    private String visaType;
    
    @FXML
    public void initialize() {
        // Get applicant info from session
        VisaApplicationSession session = VisaApplicationSession.getInstance();
        applicantId = session.getApplicantId();
        applicantName = session.getApplicantName();
        
        // Set welcome message
        welcomeLabel.setText("Welcome, " + applicantName);
        
        // Load application data
        loadApplicationData();
        
        // Update message button visibility based on status (will be set after loadApplicationData)
    }
    
    private void loadApplicationData() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     LOADING APPLICANT DATA FROM DATABASE                   ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("Application ID: " + applicantId);
        
        DatabaseManager dbManager = DatabaseManager.getInstance();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM applicants WHERE application_id = ?")) {
            
            stmt.setString(1, applicantId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                applicationIdLabel.setText(rs.getString("application_id"));
                
                // Set status with color
                currentStatus = rs.getString("status");
                visaType = rs.getString("visa_type");
                
                System.out.println("✓ Found application in database");
                System.out.println("  - Current Status: " + currentStatus);
                System.out.println("  - Visa Type: " + visaType);
                
                String visaDuration = rs.getString("visa_duration");
                String approvedBy = rs.getString("approved_by");
                String approvalDate = rs.getString("approval_date");
                
                System.out.println("  - Visa Duration: " + (visaDuration != null ? visaDuration : "N/A"));
                System.out.println("  - Approved By: " + (approvedBy != null ? approvedBy : "N/A"));
                System.out.println("  - Approval Date: " + (approvalDate != null ? approvalDate : "N/A"));
                
                statusLabel.setText(currentStatus);
                switch (currentStatus) {
                    case "Processing":
                        statusLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                        break;
                    case "Approved":
                        statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                        break;
                    case "Rejected":
                        statusLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                        break;
                }
                
                countryLabel.setText(rs.getString("country"));
                visaTypeLabel.setText(visaType);
                fullNameLabel.setText(rs.getString("first_name") + " " + rs.getString("last_name"));
                nationalityLabel.setText(rs.getString("nationality"));
                emailLabel.setText(rs.getString("email"));
                phoneLabel.setText(rs.getString("phone"));
                appliedDateLabel.setText(rs.getString("created_at"));
                
                // Show visa duration and approval info if approved
                if ("Approved".equals(currentStatus)) {
                    System.out.println("✓ Application is APPROVED - showing approval details");
                    
                    if (visaDurationLabel != null && visaDuration != null) {
                        visaDurationLabel.setText(visaDuration);
                        visaDurationLabel.setVisible(true);
                        System.out.println("  ✓ Visa duration label visible: " + visaDuration);
                    }
                    if (approvedByLabel != null && approvedBy != null) {
                        approvedByLabel.setText(approvedBy);
                        approvedByLabel.setVisible(true);
                        System.out.println("  ✓ Approved by label visible: " + approvedBy);
                    }
                    if (approvalDateLabel != null && approvalDate != null) {
                        approvalDateLabel.setText(approvalDate);
                        approvalDateLabel.setVisible(true);
                        System.out.println("  ✓ Approval date label visible: " + approvalDate);
                    }
                    
                    // Show visa conditions box
                    if (visaConditionsBox != null) {
                        visaConditionsBox.setVisible(true);
                        visaConditionsBox.setManaged(true);
                        System.out.println("  ✓ Visa conditions box visible");
                    }
                } else {
                    System.out.println("⚠ Application status is: " + currentStatus + " - hiding approval details");
                    
                    // Hide approval details if not approved
                    if (visaDurationLabel != null) visaDurationLabel.setVisible(false);
                    if (approvedByLabel != null) approvedByLabel.setVisible(false);
                    if (approvalDateLabel != null) approvalDateLabel.setVisible(false);
                    if (visaConditionsBox != null) {
                        visaConditionsBox.setVisible(false);
                        visaConditionsBox.setManaged(false);
                    }
                }
                
                // Update message button visibility
                updateMessageButtonVisibility();
                
                System.out.println("╚════════════════════════════════════════════════════════════╝");
            } else {
                System.err.println("✗ Application not found in database!");
                System.out.println("╚════════════════════════════════════════════════════════════╝");
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Database error loading application data: " + e.getMessage());
            e.printStackTrace();
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            showError("Failed to load application data: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRefresh() {
        System.out.println("Applicant dashboard refresh triggered for: " + applicantId);
        loadApplicationData();
        System.out.println("Applicant dashboard refresh completed. Current status: " + currentStatus);
    }
    
    private void updateMessageButtonVisibility() {
        // Only show message button when status is "Processing"
        if (messageButton != null) {
            boolean isProcessing = "Processing".equals(currentStatus);
            messageButton.setVisible(isProcessing);
            messageButton.setManaged(isProcessing);
            System.out.println("Message button visibility: " + (isProcessing ? "VISIBLE" : "HIDDEN") + " (Status: " + currentStatus + ")");
        }
    }
    
    @FXML
    private void handleSendMessage() {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Send Message to Admin");
        dialog.setHeaderText("Contact Admin for " + countryLabel.getText());
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label infoLabel = new Label("Send a message to the admin regarding your application:");
        infoLabel.setWrapText(true);
        
        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Enter your message here...");
        messageArea.setPrefRowCount(6);
        messageArea.setWrapText(true);
        
        content.getChildren().addAll(infoLabel, messageArea);
        dialog.getDialogPane().setContent(content);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String message = messageArea.getText().trim();
            if (message.isEmpty()) {
                showError("Message cannot be empty");
                return;
            }
            
            DatabaseManager dbManager = DatabaseManager.getInstance();
            if (dbManager.sendApplicantMessage(applicantId, message)) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Message Sent");
                successAlert.setContentText("Your message has been sent to the admin successfully.\n\nYou will be notified once the admin responds.");
                successAlert.showAndWait();
            } else {
                showError("Failed to send message. Please try again.");
            }
        }
    }
    
    @FXML
    private void handlePrint() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Print Application");
        alert.setHeaderText("Print Feature");
        alert.setContentText("Print functionality will be implemented soon.\n\n" +
                            "Your Application ID: " + applicantId + "\n" +
                            "Status: " + statusLabel.getText());
        alert.showAndWait();
    }
    
    @FXML
    private void handleLogout() {
        VisaApplicationSession.getInstance().clear();
        VisaManagementApp.changeScene("/fxml/home.fxml", "Visa Management & Processing System");
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
