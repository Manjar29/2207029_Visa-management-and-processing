package com.visa.management.controllers;

import com.visa.management.VisaManagementApp;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SelectCountryController {

    @FXML
    private void handleCountrySelection(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String country = clickedButton.getText();
        
        // Store selected country and navigate to visa type selection
        VisaApplicationSession.getInstance().setSelectedCountry(country);
        VisaManagementApp.changeScene("/fxml/select-visa-type.fxml", "Select Visa Type - " + country);
    }

    @FXML
    private void handleBackToHome() {
        VisaManagementApp.changeScene("/fxml/home.fxml", "Visa Management & Processing System");
    }

    @FXML
    private void initialize() {
        System.out.println("Country selection screen initialized");
    }
}
