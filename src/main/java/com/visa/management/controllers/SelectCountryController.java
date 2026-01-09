package com.visa.management.controllers;

import com.visa.management.VisaManagementApp;
import com.visa.management.database.DatabaseManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.util.List;

public class SelectCountryController {

    @FXML
    private void handleCountrySelection(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String country = clickedButton.getText();
        
        // Store selected country first
        VisaApplicationSession.getInstance().setSelectedCountry(country);
        
        // Show country notices in background thread to prevent UI freeze
        new Thread(() -> {
            try {
                DatabaseManager dbManager = DatabaseManager.getInstance();
                List<DatabaseManager.Notice> notices = dbManager.getNotices(country);
                
                if (notices != null && !notices.isEmpty()) {
                    // Show notices on JavaFX thread
                    Platform.runLater(() -> {
                        try {
                            showCountryNoticesDialog(country, notices);
                            // Navigate after notices shown
                            VisaManagementApp.changeScene("/fxml/select-visa-type.fxml", "Select Visa Type - " + country);
                        } catch (Exception e) {
                            System.err.println("Error showing notices dialog: " + e.getMessage());
                            e.printStackTrace();
                            // Navigate anyway even if dialog fails
                            VisaManagementApp.changeScene("/fxml/select-visa-type.fxml", "Select Visa Type - " + country);
                        }
                    });
                } else {
                    // No notices, navigate directly
                    Platform.runLater(() -> {
                        VisaManagementApp.changeScene("/fxml/select-visa-type.fxml", "Select Visa Type - " + country);
                    });
                }
            } catch (Exception e) {
                System.err.println("Error loading notices: " + e.getMessage());
                e.printStackTrace();
                // Navigate anyway even if loading fails
                Platform.runLater(() -> {
                    VisaManagementApp.changeScene("/fxml/select-visa-type.fxml", "Select Visa Type - " + country);
                });
            }
        }).start();
    }
    
    private void showCountryNoticesDialog(String country, List<DatabaseManager.Notice> notices) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Important Information - " + country);
        alert.setHeaderText("Visa Application Rules & Notices");
        
        // Create a single text area instead of multiple
        StringBuilder allNotices = new StringBuilder();
        
        for (DatabaseManager.Notice notice : notices) {
            if (allNotices.length() > 0) {
                allNotices.append("\n\n");
            }
            allNotices.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            allNotices.append(getNoticeTypeLabel(notice.getNoticeType())).append("\n");
            allNotices.append("Title: ").append(notice.getTitle()).append("\n");
            allNotices.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            allNotices.append(notice.getContent());
            allNotices.append("\n\nLast updated: ").append(notice.getUpdatedAt());
        }
        
        TextArea noticeArea = new TextArea(allNotices.toString());
        noticeArea.setEditable(false);
        noticeArea.setWrapText(true);
        noticeArea.setPrefSize(580, 400);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().add(noticeArea);
        
        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefWidth(600);
        alert.getDialogPane().setPrefHeight(500);
        alert.showAndWait();
    }
    
    private String getNoticeTypeLabel(String type) {
        switch (type) {
            case "RULES": return "📋 VISA APPLICATION RULES & REQUIREMENTS";
            case "REJECTION_REASONS": return "⚠️  COMMON REJECTION REASONS";
            case "GENERAL": return "ℹ️  GENERAL NOTICE";
            default: return "NOTICE";
        }
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
