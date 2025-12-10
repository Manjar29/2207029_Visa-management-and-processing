package com.visa.management.controllers;

import com.visa.management.VisaManagementApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class CheckStatusController {

    @FXML
    private TextField applicationIdField;

    @FXML
    private TextField passportField;

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
    private void handleCheckStatus() {
        String applicationId = applicationIdField.getText();
        String passport = passportField.getText();

        if (applicationId.isEmpty() || passport.isEmpty()) {
            showAlert("Error", "Please fill in all fields", Alert.AlertType.ERROR);
            return;
        }

        // Show the status display
        statusContainer.setVisible(true);
        statusContainer.setManaged(true);
        
        // Update with mock data
        displayMockStatus(applicationId);
    }

    @FXML
    private void handleBackToHome() {
        VisaManagementApp.changeScene("/fxml/home.fxml", "Visa Management & Processing System");
    }

    private void displayMockStatus(String applicationId) {
        statusBadge.setText("Processing");
        appIdLabel.setText(applicationId);
        nameLabel.setText("Manjar Hossan");
        visaTypeLabel.setText("Tourist Visa");
        submissionDateLabel.setText("Dec 1, 2025");
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
        System.out.println("Check Status screen initialized");
    }
}
