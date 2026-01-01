package com.visa.management.debug;

import java.sql.*;

public class DatabaseInspector {
    public static void main(String[] args) {
        String dbPath = System.getProperty("user.dir") + "\\visadb.db";
        String dbUrl = "jdbc:sqlite:" + dbPath;
        
        System.out.println("========================================");
        System.out.println("DATABASE INSPECTOR");
        System.out.println("========================================");
        System.out.println("Database: " + dbPath);
        System.out.println();
        
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            String query = "SELECT application_id, first_name, last_name, country, visa_type, status, visa_duration, approved_by FROM applicants ORDER BY created_at DESC";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                
                System.out.println("CURRENT DATABASE CONTENTS:");
                System.out.println("========================================");
                
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("\n[" + count + "] Application Details:");
                    System.out.println("  Application ID: " + rs.getString("application_id"));
                    System.out.println("  Name: " + rs.getString("first_name") + " " + rs.getString("last_name"));
                    System.out.println("  Country: " + rs.getString("country"));
                    System.out.println("  Visa Type: " + rs.getString("visa_type"));
                    System.out.println("  STATUS: " + rs.getString("status"));
                    System.out.println("  Visa Duration: " + rs.getString("visa_duration"));
                    System.out.println("  Approved By: " + rs.getString("approved_by"));
                }
                
                if (count == 0) {
                    System.out.println("No applications found in database.");
                } else {
                    System.out.println("\n========================================");
                    System.out.println("Total applications: " + count);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error reading database: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("========================================");
    }
}
