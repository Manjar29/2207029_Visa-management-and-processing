package com.visa.management.controllers;

import com.visa.management.VisaManagementApp;
import com.visa.management.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private RadioButton adminRadio;
    
    @FXML
    private RadioButton applicantRadio;
    
    @FXML
    private VBox applicantHelpBox;
    
    private ToggleGroup loginTypeGroup;
    
    @FXML
    private Label usernameLabel;
    
    @FXML
    private void initialize() {
        // Create toggle group manually
        loginTypeGroup = new ToggleGroup();
        adminRadio.setToggleGroup(loginTypeGroup);
        applicantRadio.setToggleGroup(loginTypeGroup);
        
        // Set default to applicant
        applicantRadio.setSelected(true);
        usernameLabel.setText("Application ID *");
        
        // Show help box for applicants by default
        applicantHelpBox.setVisible(true);
        applicantHelpBox.setManaged(true);
        
        // Add listener to change label based on login type
        loginTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == adminRadio) {
                usernameLabel.setText("Username *");
                usernameField.setPromptText("Enter admin username");
                applicantHelpBox.setVisible(false);
                applicantHelpBox.setManaged(false);
            } else {
                usernameLabel.setText("Application ID *");
                usernameField.setPromptText("Enter application ID");
                applicantHelpBox.setVisible(true);
                applicantHelpBox.setManaged(true);
            }
        });
        
        // Show info on startup if applicant is selected
        if (applicantRadio.isSelected()) {
            showCredentialRecoveryInfo();
        }
    }
    
    private void showCredentialRecoveryInfo() {
        // Show message once when applicant login is selected
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Applicant Login Information");
        info.setHeaderText("Credential Recovery");
        info.setContentText("⚠️  Important Notice:\n\n" +
                          "If you forget your password or Application ID, please contact the " +
                          "AHC (Assistant High Commission) office with your passport and national ID.\n\n" +
                          "You will need these credentials to check your visa application status.");
        info.getDialogPane().setPrefWidth(450);
        
        // Don't block - just show briefly
        info.show();
        
        // Auto-close after 5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                javafx.application.Platform.runLater(() -> info.close());
            } catch (InterruptedException e) {
                // Ignore
            }
        }).start();
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username/Application ID and password", Alert.AlertType.ERROR);
            return;
        }
        
        DatabaseManager dbManager = DatabaseManager.getInstance();
        
        if (adminRadio.isSelected()) {
            // Admin login
            DatabaseManager.Admin admin = dbManager.verifyAdmin(username, password);
            if (admin != null) {
                // Store admin info in session
                VisaApplicationSession.getInstance().setAdminCountry(admin.getCountry());
                VisaApplicationSession.getInstance().setAdminUsername(admin.getUsername());
                
                // Navigate to admin dashboard directly
                VisaManagementApp.changeScene("/fxml/admin-dashboard.fxml", "Admin Dashboard - " + admin.getCountry());
            } else {
                showAlert("Login Failed", "Invalid username or password", Alert.AlertType.ERROR);
            }
        } else {
            // Applicant login
            DatabaseManager.Applicant applicant = dbManager.verifyApplicant(username, password);
            if (applicant != null) {
                // Store applicant info in session
                VisaApplicationSession.getInstance().setApplicantId(applicant.getApplicationId());
                VisaApplicationSession.getInstance().setApplicantName(applicant.getFirstName() + " " + applicant.getLastName());
                
                // Navigate to applicant dashboard directly
                VisaManagementApp.changeScene("/fxml/applicant-dashboard.fxml", "My Application");
            } else {
                showAlert("Login Failed", "Invalid Application ID or password", Alert.AlertType.ERROR);
            }
        }
    }
    
    @FXML
    private void handleBack() {
        VisaManagementApp.changeScene("/fxml/home.fxml", "Visa Management & Processing System");
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

