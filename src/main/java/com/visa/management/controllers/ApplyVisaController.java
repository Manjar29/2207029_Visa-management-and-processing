package com.visa.management.controllers;

import com.visa.management.VisaManagementApp;
import com.visa.management.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class ApplyVisaController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField nationalIdField;

    @FXML
    private ComboBox<String> nationalityCombo;

    @FXML
    private TextField passportField;

    @FXML
    private Label fileLabel;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextArea addressArea;

    @FXML
    private ComboBox<String> travelHistoryCombo;

    @FXML
    private TextArea previousTravelArea;

    @FXML
    private Label countryInfoLabel;

    @FXML
    private Label visaTypeInfoLabel;

    // Tourist Visa Fields
    @FXML
    private VBox touristSection;
    @FXML
    private TextArea touristPlaceArea;
    @FXML
    private TextField hotelPnrField;
    @FXML
    private TextField flightNoField;
    @FXML
    private DatePicker journeyDatePicker;
    @FXML
    private TextField bankNameField;
    @FXML
    private TextField accountNumberField;
    @FXML
    private TextArea referencesArea;
    @FXML
    private TextArea familyMembersArea;

    // Medical Visa Fields
    @FXML
    private VBox medicalSection;
    @FXML
    private TextArea diagnosisArea;
    @FXML
    private TextArea hospitalDetailsArea;
    @FXML
    private TextField doctorNameField;
    @FXML
    private DatePicker appointmentDatePicker;

    // Student Visa Fields
    @FXML
    private VBox studentSection;
    @FXML
    private TextField universityField;
    @FXML
    private TextField programField;
    @FXML
    private TextField programDurationField;
    @FXML
    private DatePicker programStartDatePicker;
    @FXML
    private TextArea educationDetailsArea;

    // Work Visa Fields
    @FXML
    private VBox workSection;
    @FXML
    private TextField companyNameField;
    @FXML
    private TextArea companyAddressArea;
    @FXML
    private TextField jobPositionField;
    @FXML
    private TextField contractDurationField;
    @FXML
    private DatePicker employmentStartDatePicker;
    @FXML
    private TextField salaryField;

    private List<File> selectedFiles;

    @FXML
    private void handleSubmit() {
        if (!validateForm()) {
            showAlert("Validation Error", "Please fill in all required fields.", Alert.AlertType.ERROR);
            return;
        }
        
        // Get application details
        String country = VisaApplicationSession.getInstance().getSelectedCountry();
        String visaType = VisaApplicationSession.getInstance().getSelectedVisaType();
        String nationalId = nationalIdField.getText().trim();
        String passport = passportField.getText().trim();
        String nationality = nationalityCombo.getValue();
        
        // Validate formats
        String formatError = validateFormats(nationalId, passport, nationality);
        if (formatError != null) {
            showAlert("Validation Error", formatError, Alert.AlertType.ERROR);
            return;
        }
        
        DatabaseManager dbManager = DatabaseManager.getInstance();
        
        // Check for existing application (duplicate prevention)
        DatabaseManager.ApplicationInfo existingApp = dbManager.checkExistingApplication(nationalId, passport, country);
        if (existingApp != null) {
            String duplicateMessage = "⚠️ DUPLICATE APPLICATION DETECTED\n\n" +
                                    "You already have an active application for " + country + ".\n\n" +
                                    "Application ID: " + existingApp.getApplicationId() + "\n" +
                                    "Status: " + existingApp.getStatus() + "\n" +
                                    "Submitted: " + existingApp.getCreatedAt() + "\n\n" +
                                    "You cannot submit multiple applications to the same country while one is " +
                                    "still Processing or Approved.\n\n" +
                                    "Please wait for your current application to be finalized before reapplying.";
            
            showAlert("Duplicate Application", duplicateMessage, Alert.AlertType.WARNING);
            return;
        }
        
        // Check if applicant is banned from reapplying
        DatabaseManager.RejectionBan rejectionBan = dbManager.checkRejectionBan(nationalId, passport, country);
        
        if (rejectionBan.isBanned()) {
            String banMessage = "⛔ APPLICATION REJECTED\n\n" +
                              "You cannot apply for a visa to " + country + " at this time.\n\n" +
                              "Reason: " + rejectionBan.getReason() + "\n\n" +
                              "Rejection Date: " + rejectionBan.getRejectionDate() + "\n" +
                              "Ban Until: " + rejectionBan.getBanUntilDate() + "\n\n" +
                              "Your application is automatically rejected due to a previous rejection.\n" +
                              "Please wait until the ban period expires before reapplying.";
            
            showAlert("Application Banned", banMessage, Alert.AlertType.ERROR);
            return;
        }
        
        // Validate country-specific rules
        String validationError = validateCountryRules(country, visaType);
        if (validationError != null) {
            showAlert("Application Restriction", validationError, Alert.AlertType.ERROR);
            return;
        }

        // Save to database and get credentials
        DatabaseManager.ApplicationCredentials credentials = dbManager.createApplicant(
            firstNameField.getText().trim(),
            lastNameField.getText().trim(),
            nationalId,
            nationalityCombo.getValue(),
            passport,
            emailField.getText().trim(),
            phoneField.getText().trim(),
            addressArea.getText().trim(),
            country,
            visaType
        );
        
        if (credentials == null) {
            showAlert("Error", "Failed to submit application. Please try again.", Alert.AlertType.ERROR);
            return;
        }
        
        // Save additional visa-specific details
        saveVisaSpecificDetails(credentials.getApplicationId(), visaType);
        
        // Save uploaded documents
        saveDocuments(credentials.getApplicationId());
        
        // Show success message with credentials
        showAlert("Application Submitted Successfully!", 
                  "Your visa application has been submitted!\n\n" +
                  "═══════════════════════════════\n" +
                  "Application ID: " + credentials.getApplicationId() + "\n" +
                  "Password: " + credentials.getPassword() + "\n" +
                  "═══════════════════════════════\n\n" +
                  "⚠️ IMPORTANT: Please save these credentials!\n" +
                  "You can use them to login and check your application status.\n\n" +
                  "Status: Processing\n" +
                  "Email: " + emailField.getText(), 
                  Alert.AlertType.INFORMATION);
        
        clearForm();
        
        // Clear session and return to home
        VisaApplicationSession.getInstance().clear();
        VisaManagementApp.changeScene("/fxml/home.fxml", "Visa Management & Processing System");
    }
    
    private void saveVisaSpecificDetails(String applicationId, String visaType) {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        
        // Save common fields
        dbManager.saveVisaDetail(applicationId, "travel_history", travelHistoryCombo.getValue());
        dbManager.saveVisaDetail(applicationId, "previous_travel", previousTravelArea.getText());
        
        // Save visa-type specific fields
        if (visaType.contains("Tourist")) {
            dbManager.saveVisaDetail(applicationId, "tourist_places", touristPlaceArea.getText());
            dbManager.saveVisaDetail(applicationId, "hotel_pnr", hotelPnrField.getText());
            dbManager.saveVisaDetail(applicationId, "flight_no", flightNoField.getText());
            dbManager.saveVisaDetail(applicationId, "journey_date", journeyDatePicker.getValue() != null ? journeyDatePicker.getValue().toString() : "");
            dbManager.saveVisaDetail(applicationId, "bank_name", bankNameField.getText());
            dbManager.saveVisaDetail(applicationId, "account_number", accountNumberField.getText());
            dbManager.saveVisaDetail(applicationId, "references", referencesArea.getText());
            dbManager.saveVisaDetail(applicationId, "family_members", familyMembersArea.getText());
        } else if (visaType.contains("Medical")) {
            dbManager.saveVisaDetail(applicationId, "diagnosis", diagnosisArea.getText());
            dbManager.saveVisaDetail(applicationId, "hospital_details", hospitalDetailsArea.getText());
            dbManager.saveVisaDetail(applicationId, "doctor_name", doctorNameField.getText());
            dbManager.saveVisaDetail(applicationId, "appointment_date", appointmentDatePicker.getValue() != null ? appointmentDatePicker.getValue().toString() : "");
        } else if (visaType.contains("Student")) {
            dbManager.saveVisaDetail(applicationId, "university", universityField.getText());
            dbManager.saveVisaDetail(applicationId, "program", programField.getText());
            dbManager.saveVisaDetail(applicationId, "program_duration", programDurationField.getText());
            dbManager.saveVisaDetail(applicationId, "program_start_date", programStartDatePicker.getValue() != null ? programStartDatePicker.getValue().toString() : "");
            dbManager.saveVisaDetail(applicationId, "education_details", educationDetailsArea.getText());
        } else if (visaType.contains("Work")) {
            dbManager.saveVisaDetail(applicationId, "company_name", companyNameField.getText());
            dbManager.saveVisaDetail(applicationId, "company_address", companyAddressArea.getText());
            dbManager.saveVisaDetail(applicationId, "job_position", jobPositionField.getText());
            dbManager.saveVisaDetail(applicationId, "contract_duration", contractDurationField.getText());
            dbManager.saveVisaDetail(applicationId, "employment_start_date", employmentStartDatePicker.getValue() != null ? employmentStartDatePicker.getValue().toString() : "");
            dbManager.saveVisaDetail(applicationId, "salary", salaryField.getText());
        }
    }
    
    private void saveDocuments(String applicationId) {
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            System.out.println("No documents to save for application " + applicationId);
            return;
        }
        
        DatabaseManager dbManager = DatabaseManager.getInstance();
        
        // Create documents directory if it doesn't exist
        java.io.File documentsDir = new java.io.File("documents");
        if (!documentsDir.exists()) {
            documentsDir.mkdir();
        }
        
        // Create application-specific folder
        java.io.File appDocDir = new java.io.File(documentsDir, applicationId);
        if (!appDocDir.exists()) {
            appDocDir.mkdir();
        }
        
        // Copy and save each document
        for (java.io.File file : selectedFiles) {
            try {
                // Copy file to application folder
                java.io.File destFile = new java.io.File(appDocDir, file.getName());
                java.nio.file.Files.copy(file.toPath(), destFile.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                // Get file info
                String filename = file.getName();
                String filePath = destFile.getAbsolutePath();
                String fileType = getFileExtension(filename);
                long fileSize = file.length();
                
                // Save to database
                dbManager.saveDocument(applicationId, filename, filePath, fileType, fileSize);
                
                System.out.println("Saved document: " + filename + " (" + fileSize + " bytes)");
                
            } catch (java.io.IOException e) {
                System.err.println("Error saving document " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "unknown";
    }
    
    private String validateCountryRules(String country, String visaType) {
        switch (country) {
            case "India":
                // Check if previously traveled to Pakistan
                String travelHistory = previousTravelArea.getText().toLowerCase();
                if (travelHistory.contains("pakistan")) {
                    return "Sorry, applicants who have previously traveled to Pakistan cannot apply for India visa.";
                }
                break;
                
            case "Bangladesh":
                if (visaType.contains("Work")) {
                    return "Bangladesh does not provide Work Visa. Please select a different visa type.";
                }
                break;
                
            case "Japan":
                // This would normally check actual bank balance from financial documents
                // For now, we'll show a warning that will be verified
                // In real application, you'd validate uploaded bank statements
                break;
                
            case "New Zealand":
                // Check if traveled to at least 2 countries
                String travelHistoryType = travelHistoryCombo.getValue();
                if (travelHistoryType == null || travelHistoryType.contains("No - First Time") || 
                    travelHistoryType.contains("Single Country")) {
                    return "New Zealand requires applicants to have previously traveled to at least 2 different countries.";
                }
                break;
                
            case "Singapore":
                if (visaType.contains("Tourist") || visaType.contains("Work")) {
                    return "Singapore does not provide " + visaType + ". Please select a different visa type.";
                }
                break;
        }
        
        return null; // No validation errors
    }

    @FXML
    private void handleBackToHome() {
        VisaManagementApp.changeScene("/fxml/select-visa-type.fxml", "Select Visa Type");
    }

    @FXML
    private void handleUploadDocuments() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Documents");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Files", "*.*"),
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        selectedFiles = fileChooser.showOpenMultipleDialog(VisaManagementApp.getPrimaryStage());
        
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            fileLabel.setText(selectedFiles.size() + " file(s) selected");
        }
    }

    private String validateFormats(String nationalId, String passport, String nationality) {
        // Validate National ID format (alphanumeric, 8-20 characters)
        if (!nationalId.matches("^[A-Z0-9]{8,20}$")) {
            return "Invalid National ID format.\n" +
                   "Must be 8-20 alphanumeric characters (uppercase).\n" +
                   "Example: NID123456789";
        }
        
        // Validate Passport format (alphanumeric, 6-15 characters)
        if (!passport.matches("^[A-Z0-9]{6,15}$")) {
            return "Invalid Passport Number format.\n" +
                   "Must be 6-15 alphanumeric characters (uppercase).\n" +
                   "Example: AB1234567";
        }
        
        // Validate email format
        String email = emailField.getText().trim();
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return "Invalid email address format.\n" +
                   "Example: user@example.com";
        }
        
        // Validate phone format (digits, optional + prefix, 10-15 digits)
        String phone = phoneField.getText().trim();
        if (!phone.matches("^\\+?[0-9]{10,15}$")) {
            return "Invalid phone number format.\n" +
                   "Must be 10-15 digits (optional + prefix).\n" +
                   "Example: +1234567890";
        }
        
        // Basic nationality-passport consistency check
        // (This is a simplified check - real-world would need comprehensive country-passport prefix mapping)
        String passportPrefix = passport.substring(0, Math.min(2, passport.length()));
        if (nationality.equals("Bangladesh") && !passportPrefix.matches("^[AB].*")) {
            return "Passport number appears inconsistent with nationality.\n" +
                   "Bangladeshi passports typically start with 'A' or 'B'.\n" +
                   "Please verify your information.";
        }
        
        return null; // No validation errors
    }
    
    private boolean validateForm() {
        // Validate common fields
        boolean basicValid = !firstNameField.getText().isEmpty() &&
               !lastNameField.getText().isEmpty() &&
               !nationalIdField.getText().isEmpty() &&
               nationalityCombo.getValue() != null &&
               !passportField.getText().isEmpty() &&
               !emailField.getText().isEmpty() &&
               !phoneField.getText().isEmpty() &&
               !addressArea.getText().isEmpty() &&
               travelHistoryCombo.getValue() != null;
        
        if (!basicValid) return false;
        
        // Validate visa-type specific fields
        String visaType = VisaApplicationSession.getInstance().getSelectedVisaType();
        
        if (visaType.contains("Tourist")) {
            return !touristPlaceArea.getText().isEmpty() &&
                   !hotelPnrField.getText().isEmpty() &&
                   !flightNoField.getText().isEmpty() &&
                   journeyDatePicker.getValue() != null &&
                   !bankNameField.getText().isEmpty() &&
                   !accountNumberField.getText().isEmpty() &&
                   !referencesArea.getText().isEmpty();
        } else if (visaType.contains("Medical")) {
            return !diagnosisArea.getText().isEmpty() &&
                   !hospitalDetailsArea.getText().isEmpty() &&
                   !doctorNameField.getText().isEmpty() &&
                   appointmentDatePicker.getValue() != null;
        } else if (visaType.contains("Student")) {
            return !universityField.getText().isEmpty() &&
                   !programField.getText().isEmpty() &&
                   !programDurationField.getText().isEmpty() &&
                   programStartDatePicker.getValue() != null &&
                   !educationDetailsArea.getText().isEmpty();
        } else if (visaType.contains("Work")) {
            return !companyNameField.getText().isEmpty() &&
                   !companyAddressArea.getText().isEmpty() &&
                   !jobPositionField.getText().isEmpty() &&
                   !contractDurationField.getText().isEmpty() &&
                   employmentStartDatePicker.getValue() != null &&
                   !salaryField.getText().isEmpty();
        }
        
        return true;
    }

    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        nationalIdField.clear();
        nationalityCombo.setValue(null);
        passportField.clear();
        emailField.clear();
        phoneField.clear();
        addressArea.clear();
        travelHistoryCombo.setValue(null);
        previousTravelArea.clear();
        fileLabel.setText("Click to choose files");
        selectedFiles = null;
        
        // Clear Tourist fields
        if (touristPlaceArea != null) touristPlaceArea.clear();
        if (hotelPnrField != null) hotelPnrField.clear();
        if (flightNoField != null) flightNoField.clear();
        if (journeyDatePicker != null) journeyDatePicker.setValue(null);
        if (bankNameField != null) bankNameField.clear();
        if (accountNumberField != null) accountNumberField.clear();
        if (referencesArea != null) referencesArea.clear();
        if (familyMembersArea != null) familyMembersArea.clear();
        
        // Clear Medical fields
        if (diagnosisArea != null) diagnosisArea.clear();
        if (hospitalDetailsArea != null) hospitalDetailsArea.clear();
        if (doctorNameField != null) doctorNameField.clear();
        if (appointmentDatePicker != null) appointmentDatePicker.setValue(null);
        
        // Clear Student fields
        if (universityField != null) universityField.clear();
        if (programField != null) programField.clear();
        if (programDurationField != null) programDurationField.clear();
        if (programStartDatePicker != null) programStartDatePicker.setValue(null);
        if (educationDetailsArea != null) educationDetailsArea.clear();
        
        // Clear Work fields
        if (companyNameField != null) companyNameField.clear();
        if (companyAddressArea != null) companyAddressArea.clear();
        if (jobPositionField != null) jobPositionField.clear();
        if (contractDurationField != null) contractDurationField.clear();
        if (employmentStartDatePicker != null) employmentStartDatePicker.setValue(null);
        if (salaryField != null) salaryField.clear();
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
        System.out.println("ApplyVisaController - Initialize method called");
        
        // Get selected country and visa type from session
        String selectedCountry = VisaApplicationSession.getInstance().getSelectedCountry();
        String selectedVisaType = VisaApplicationSession.getInstance().getSelectedVisaType();
        
        System.out.println("Selected Country: " + selectedCountry);
        System.out.println("Selected Visa Type: " + selectedVisaType);
        
        // Update info labels if they exist
        if (countryInfoLabel != null && selectedCountry != null) {
            countryInfoLabel.setText("Destination: " + selectedCountry);
        }
        if (visaTypeInfoLabel != null && selectedVisaType != null) {
            visaTypeInfoLabel.setText("Visa Type: " + selectedVisaType);
        }
        
        // Show only the relevant section based on visa type
        showVisaTypeSection(selectedVisaType);
        
        // Populate nationality combo box
        nationalityCombo.getItems().addAll(
            "Afghanistan", "Australia", "Bangladesh", "Bhutan", "Brazil", "Canada", 
            "China", "Egypt", "France", "Germany", "India", "Indonesia", "Iran", 
            "Iraq", "Italy", "Japan", "Malaysia", "Maldives", "Nepal", "Netherlands",
            "New Zealand", "Nigeria", "Philippines", "Qatar", "Russia",
            "Saudi Arabia", "Singapore", "South Africa", "South Korea", "Spain", 
            "Sri Lanka", "Thailand", "Turkey", "United Arab Emirates", "United Kingdom", 
            "United States", "Vietnam"
        );
        
        // Populate travel history combo box
        travelHistoryCombo.getItems().addAll(
            "Yes - Multiple Countries", "Yes - Single Country", "No - First Time Travel"
        );
        
        // Disable past dates for journey date picker
        if (journeyDatePicker != null) {
            journeyDatePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(java.time.LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.isBefore(java.time.LocalDate.now()));
                }
            });
        }
        
        // Disable past dates for appointment date picker
        if (appointmentDatePicker != null) {
            appointmentDatePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(java.time.LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.isBefore(java.time.LocalDate.now()));
                }
            });
        }
        
        // Disable past dates for program start date picker
        if (programStartDatePicker != null) {
            programStartDatePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(java.time.LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.isBefore(java.time.LocalDate.now()));
                }
            });
        }
        
        // Disable past dates for employment start date picker
        if (employmentStartDatePicker != null) {
            employmentStartDatePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(java.time.LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.isBefore(java.time.LocalDate.now()));
                }
            });
        }

        System.out.println("Apply Visa screen initialized successfully");
    }
    
    private void showVisaTypeSection(String visaType) {
        // Hide all sections first
        if (touristSection != null) {
            touristSection.setVisible(false);
            touristSection.setManaged(false);
        }
        if (medicalSection != null) {
            medicalSection.setVisible(false);
            medicalSection.setManaged(false);
        }
        if (studentSection != null) {
            studentSection.setVisible(false);
            studentSection.setManaged(false);
        }
        if (workSection != null) {
            workSection.setVisible(false);
            workSection.setManaged(false);
        }
        
        // Show only the relevant section
        if (visaType == null) return;
        
        if (visaType.contains("Tourist")) {
            touristSection.setVisible(true);
            touristSection.setManaged(true);
        } else if (visaType.contains("Medical")) {
            medicalSection.setVisible(true);
            medicalSection.setManaged(true);
        } else if (visaType.contains("Student")) {
            studentSection.setVisible(true);
            studentSection.setManaged(true);
        } else if (visaType.contains("Work")) {
            workSection.setVisible(true);
            workSection.setManaged(true);
        }
    }
}
