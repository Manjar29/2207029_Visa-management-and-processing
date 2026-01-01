package com.visa.management.controllers;

/**
 * Singleton class to store visa application session data
 */
public class VisaApplicationSession {
    private static VisaApplicationSession instance;
    
    private String selectedCountry;
    private String selectedVisaType;
    
    // Admin session data
    private String adminUsername;
    private String adminCountry;
    
    // Applicant session data
    private String applicantId;
    private String applicantName;
    
    private VisaApplicationSession() {}
    
    public static VisaApplicationSession getInstance() {
        if (instance == null) {
            instance = new VisaApplicationSession();
        }
        return instance;
    }
    
    public String getSelectedCountry() {
        return selectedCountry;
    }
    
    public void setSelectedCountry(String selectedCountry) {
        this.selectedCountry = selectedCountry;
    }
    
    public String getSelectedVisaType() {
        return selectedVisaType;
    }
    
    public void setSelectedVisaType(String selectedVisaType) {
        this.selectedVisaType = selectedVisaType;
    }
    
    public String getAdminUsername() {
        return adminUsername;
    }
    
    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }
    
    public String getAdminCountry() {
        return adminCountry;
    }
    
    public void setAdminCountry(String adminCountry) {
        this.adminCountry = adminCountry;
    }
    
    public String getApplicantId() {
        return applicantId;
    }
    
    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }
    
    public String getApplicantName() {
        return applicantName;
    }
    
    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }
    
    public void clear() {
        selectedCountry = null;
        selectedVisaType = null;
        adminUsername = null;
        adminCountry = null;
        applicantId = null;
        applicantName = null;
    }
}

