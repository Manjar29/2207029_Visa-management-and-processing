package com.visa.management.controllers;

import com.visa.management.VisaManagementApp;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class HomeController {

    @FXML
    private Button loginButton;

    @FXML
    private Button applyVisaButton;

    @FXML
    private Button checkStatusButton;

    @FXML
    private void handleLogin() {
        VisaManagementApp.changeScene("/fxml/login.fxml", "Login - Visa Management System");
    }

    @FXML
    private void handleApplyVisa() {
        VisaManagementApp.changeScene("/fxml/select-country.fxml", "Select Destination Country");
    }

    @FXML
    private void handleCheckStatus() {
        VisaManagementApp.changeScene("/fxml/check-status.fxml", "Check Visa Status - Visa Management System");
    }

    @FXML
    private void initialize() {
        System.out.println("Home screen initialized");
    }
}
