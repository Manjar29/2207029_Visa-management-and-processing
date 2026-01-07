package com.visa.management.debug;

import java.sql.*;

public class DatabaseInspector {
    public static void main(String[] args) {
        String dbPath = System.getProperty("user.dir") + "\\visadb.db";
        String dbUrl = "jdbc:sqlite:" + dbPath;
        
        System.out.println("╔════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                     DATABASE INSPECTOR                                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");
        System.out.println("Database Location: " + dbPath);
        System.out.println();
        
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            
            // Show all admins
            showAdmins(conn);
            
            // Show all applicants
            showApplicants(conn);
            
            // Show visa details
            showVisaDetails(conn);
            
            // Show documents
            showDocuments(conn);
            
        } catch (SQLException e) {
            System.err.println("❌ Error reading database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void showAdmins(Connection conn) throws SQLException {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                          ADMINS TABLE                                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");
        
        String query = "SELECT id, username, country, created_at FROM admins ORDER BY country";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            System.out.println(String.format("%-5s %-25s %-20s %-20s", "ID", "Username", "Country", "Created At"));
            System.out.println("─".repeat(75));
            
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println(String.format("%-5d %-25s %-20s %-20s", 
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("country"),
                    rs.getString("created_at")
                ));
            }
            
            if (count == 0) {
                System.out.println("No admins found.");
            } else {
                System.out.println("─".repeat(75));
                System.out.println("Total Admins: " + count);
            }
        }
    }
    
    private static void showApplicants(Connection conn) throws SQLException {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                       APPLICANTS TABLE                                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");
        
        String query = "SELECT * FROM applicants ORDER BY created_at DESC";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("\n┌─ Application #" + count + " ─────────────────────────────────────────────────");
                System.out.println("│ Application ID    : " + rs.getString("application_id"));
                System.out.println("│ Name              : " + rs.getString("first_name") + " " + rs.getString("last_name"));
                System.out.println("│ National ID       : " + rs.getString("national_id"));
                System.out.println("│ Nationality       : " + rs.getString("nationality"));
                System.out.println("│ Passport          : " + rs.getString("passport"));
                System.out.println("│ Email             : " + rs.getString("email"));
                System.out.println("│ Phone             : " + rs.getString("phone"));
                System.out.println("│ Address           : " + rs.getString("address"));
                System.out.println("│ Country           : " + rs.getString("country"));
                System.out.println("│ Visa Type         : " + rs.getString("visa_type"));
                
                String status = rs.getString("status");
                String statusIcon = status.equals("Approved") ? "✓" : status.equals("Rejected") ? "✗" : "⏳";
                System.out.println("│ Status            : " + statusIcon + " " + status);
                
                String duration = rs.getString("visa_duration");
                String approvedBy = rs.getString("approved_by");
                String approvalDate = rs.getString("approval_date");
                
                if (duration != null) {
                    System.out.println("│ Visa Duration     : " + duration);
                }
                if (approvedBy != null) {
                    System.out.println("│ Approved By       : " + approvedBy);
                }
                if (approvalDate != null) {
                    System.out.println("│ Approval Date     : " + approvalDate);
                }
                
                System.out.println("│ Created At        : " + rs.getString("created_at"));
                System.out.println("└──────────────────────────────────────────────────────────────────────");
            }
            
            if (count == 0) {
                System.out.println("No applications found in database.");
            } else {
                System.out.println("\n📊 Total Applications: " + count);
            }
        }
    }
    
    private static void showVisaDetails(Connection conn) throws SQLException {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                      VISA DETAILS TABLE                                ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");
        
        String query = "SELECT application_id, field_name, field_value FROM visa_details ORDER BY application_id, field_name";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            String currentAppId = "";
            int count = 0;
            int totalRecords = 0;
            
            while (rs.next()) {
                totalRecords++;
                String appId = rs.getString("application_id");
                
                if (!appId.equals(currentAppId)) {
                    if (!currentAppId.isEmpty()) {
                        System.out.println("└──────────────────────────────────────────────────────────────────────");
                    }
                    count++;
                    currentAppId = appId;
                    System.out.println("\n┌─ Application: " + appId + " ─────────────────────────────────────");
                }
                
                System.out.println(String.format("│ %-30s : %s", 
                    rs.getString("field_name"),
                    rs.getString("field_value")
                ));
            }
            
            if (count > 0) {
                System.out.println("└──────────────────────────────────────────────────────────────────────");
                System.out.println("\n📋 Total Detail Records: " + totalRecords + " (for " + count + " applications)");
            } else {
                System.out.println("No visa details found.");
            }
        }
    }
    
    private static void showDocuments(Connection conn) throws SQLException {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                      DOCUMENTS TABLE                                   ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");
        
        String query = "SELECT application_id, filename, file_path, file_type, file_size, uploaded_at " +
                      "FROM documents ORDER BY application_id, uploaded_at";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            String currentAppId = "";
            int count = 0;
            int totalDocs = 0;
            
            while (rs.next()) {
                totalDocs++;
                String appId = rs.getString("application_id");
                
                if (!appId.equals(currentAppId)) {
                    if (!currentAppId.isEmpty()) {
                        System.out.println("└──────────────────────────────────────────────────────────────────────");
                    }
                    count++;
                    currentAppId = appId;
                    System.out.println("\n┌─ Application: " + appId + " ─────────────────────────────────────");
                }
                
                String filename = rs.getString("filename");
                String fileType = rs.getString("file_type");
                long fileSize = rs.getLong("file_size");
                String uploadedAt = rs.getString("uploaded_at");
                
                System.out.println("│ 📄 Filename       : " + filename);
                System.out.println("│    Type           : " + fileType);
                System.out.println("│    Size           : " + formatFileSize(fileSize));
                System.out.println("│    Uploaded       : " + uploadedAt);
                System.out.println("│    Path           : " + rs.getString("file_path"));
                System.out.println("│");
            }
            
            if (count > 0) {
                System.out.println("└──────────────────────────────────────────────────────────────────────");
                System.out.println("\n📎 Total Documents: " + totalDocs + " (for " + count + " applications)");
            } else {
                System.out.println("No documents found.");
            }
        }
        
        System.out.println("\n╔════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                      END OF DATABASE REPORT                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");
    }
    
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}
