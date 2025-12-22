package com.visa.management.controllers;

/**
 * Singleton class to store visa application session data
 */
public class VisaApplicationSession {
    private static VisaApplicationSession instance;
    
    private String selectedCountry;
    private String selectedVisaType;
    
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
    
    public void clear() {
        selectedCountry = null;
        selectedVisaType = null;
    }
}
