package com.visa.management.controllers;

import com.visa.management.VisaManagementApp;
import com.visa.management.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private RadioButton adminRadio;
    
    @FXML
    private RadioButton applicantRadio;
    
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
        
        // Add listener to change label based on login type
        loginTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == adminRadio) {
                usernameLabel.setText("Username *");
                usernameField.setPromptText("Enter admin username");
            } else {
                usernameLabel.setText("Application ID *");
                usernameField.setPromptText("Enter application ID");
            }
        });
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

