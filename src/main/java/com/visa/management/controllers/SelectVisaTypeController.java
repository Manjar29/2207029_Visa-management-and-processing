package com.visa.management.controllers;

import com.visa.management.VisaManagementApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class SelectVisaTypeController {

    @FXML
    private Label countryLabel;
    
    @FXML
    private Button touristBtn;
    
    @FXML
    private Button medicalBtn;
    
    @FXML
    private Button studentBtn;
    
    @FXML
    private Button workBtn;

    @FXML
    private void handleVisaTypeSelection(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        
        // Determine visa type based on which button was clicked
        String visaType = "";
        if (clickedButton == touristBtn) {
            visaType = "Tourist Visa";
        } else if (clickedButton == medicalBtn) {
            visaType = "Medical Visa";
        } else if (clickedButton == studentBtn) {
            visaType = "Student Visa";
        } else if (clickedButton == workBtn) {
            visaType = "Work Visa";
        }
        
        String country = VisaApplicationSession.getInstance().getSelectedCountry();
        
        // Check country-specific visa restrictions
        if (!isVisaTypeAllowed(country, visaType)) {
            showAlert("Visa Type Not Available", 
                     getRestrictionMessage(country, visaType), 
                     Alert.AlertType.WARNING);
            return;
        }
        
        // Store selected visa type and navigate to application form
        VisaApplicationSession.getInstance().setSelectedVisaType(visaType);
        VisaManagementApp.changeScene("/fxml/apply-visa.fxml", "Apply for Visa");
    }
    
    private boolean isVisaTypeAllowed(String country, String visaType) {
        switch (country) {
            case "Bangladesh":
                return !visaType.contains("Work");
            case "Singapore":
                return !visaType.contains("Tourist") && !visaType.contains("Work");
            case "India":
            case "Japan":
            case "New Zealand":
            case "Malaysia":
                return true; // All visa types allowed
            default:
                return true;
        }
    }
    
    private String getRestrictionMessage(String country, String visaType) {
        if (country.equals("Bangladesh") && visaType.contains("Work")) {
            return "Bangladesh does not provide Work Visa. Please select another visa type.";
        } else if (country.equals("Singapore") && visaType.contains("Tourist")) {
            return "Singapore does not provide Tourist Visa. Please select another visa type.";
        } else if (country.equals("Singapore") && visaType.contains("Work")) {
            return "Singapore does not provide Work Visa. Please select another visa type.";
        }
        return "This visa type is not available for " + country + ".";
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        VisaManagementApp.changeScene("/fxml/select-country.fxml", "Select Destination Country");
    }

    @FXML
    private void initialize() {
        String country = VisaApplicationSession.getInstance().getSelectedCountry();
        if (country != null && countryLabel != null) {
            countryLabel.setText("Visa Types for " + country);
        }
        System.out.println("Visa type selection screen initialized for: " + country);
        
        // Show restriction message and disable restricted visa types
        applyCountryRestrictions(country);
    }
    
    private void applyCountryRestrictions(String country) {
        if (country == null) return;
        
        String restrictionMsg = "";
        
        // Disable buttons based on country restrictions
        if (country.equals("Singapore")) {
            restrictionMsg = "Note: Singapore does NOT provide Tourist Visa and Work Visa.";
            // Disable Tourist and Work visas for Singapore
            if (touristBtn != null) {
                touristBtn.setDisable(true);
                touristBtn.setStyle("-fx-opacity: 0.4;");
            }
            if (workBtn != null) {
                workBtn.setDisable(true);
                workBtn.setStyle("-fx-opacity: 0.4;");
            }
        } else if (country.equals("Bangladesh")) {
            restrictionMsg = "Note: Bangladesh does NOT provide Work Visa.";
            // Disable Work visa for Bangladesh
            if (workBtn != null) {
                workBtn.setDisable(true);
                workBtn.setStyle("-fx-opacity: 0.4;");
            }
        }
        
        if (!restrictionMsg.isEmpty() && countryLabel != null) {
            countryLabel.setText("Visa Types for " + country + "\n" + restrictionMsg);
        }
    }
}
