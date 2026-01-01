package com.visa.management.database;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.io.File;

public class DatabaseManager {
    // Use absolute path to ensure all connections use the same database file
    private static final String DB_FILE = new File(System.getProperty("user.dir"), "visadb.db").getAbsolutePath();
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;
    private static final String DB_USER = null;
    private static final String DB_PASSWORD = null;
    
    private static DatabaseManager instance;
    
    private DatabaseManager() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║          INITIALIZING DATABASE MANAGER                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("Database File: " + DB_FILE);
        System.out.println("Database URL: " + DB_URL);
        System.out.println("Working directory: " + System.getProperty("user.dir"));
        initializeDatabase();
    }
    
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            // Set SQLite to synchronous mode for immediate writes
            stmt.execute("PRAGMA synchronous = FULL");
            stmt.execute("PRAGMA journal_mode = DELETE");
            stmt.execute("PRAGMA foreign_keys = ON");
            
            System.out.println("SQLite PRAGMA settings applied");
            
            // Create admins table
            stmt.execute("CREATE TABLE IF NOT EXISTS admins (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "country TEXT NOT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
            
            // Create applicants table
            stmt.execute("CREATE TABLE IF NOT EXISTS applicants (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "application_id TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "first_name TEXT NOT NULL, " +
                    "last_name TEXT NOT NULL, " +
                    "national_id TEXT NOT NULL, " +
                    "nationality TEXT NOT NULL, " +
                    "passport TEXT NOT NULL, " +
                    "email TEXT NOT NULL, " +
                    "phone TEXT NOT NULL, " +
                    "address TEXT NOT NULL, " +
                    "country TEXT NOT NULL, " +
                    "visa_type TEXT NOT NULL, " +
                    "status TEXT DEFAULT 'Processing', " +
                    "visa_duration TEXT, " +
                    "approved_by TEXT, " +
                    "approval_date DATETIME, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
            
            // Create visa_details table for additional visa-specific information
            stmt.execute("CREATE TABLE IF NOT EXISTS visa_details (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "application_id TEXT NOT NULL, " +
                    "field_name TEXT NOT NULL, " +
                    "field_value TEXT, " +
                    "FOREIGN KEY (application_id) REFERENCES applicants(application_id))");
            
            // Insert default admin accounts (one for each country) if not exists
            insertDefaultAdmins();
            
            System.out.println("Database initialized successfully");
            
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void insertDefaultAdmins() {
        String[] countries = {"India", "Bangladesh", "Japan", "Malaysia", "New Zealand", "Singapore"};
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String checkSql = "SELECT COUNT(*) FROM admins WHERE username = ?";
            String insertSql = "INSERT INTO admins (username, password, country) VALUES (?, ?, ?)";
            
            for (String country : countries) {
                String username = "admin_" + country.toLowerCase().replace(" ", "_");
                String password = hashPassword(country.toLowerCase() + "123"); // Default password
                
                // Check if admin already exists
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, username);
                    ResultSet rs = checkStmt.executeQuery();
                    rs.next();
                    
                    if (rs.getInt(1) == 0) {
                        // Insert new admin
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setString(1, username);
                            insertStmt.setString(2, password);
                            insertStmt.setString(3, country);
                            insertStmt.executeUpdate();
                            System.out.println("Created admin: " + username + " (password: " + country.toLowerCase() + "123)");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting default admins: " + e.getMessage());
        }
    }
    
    // Hash password using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    // Generate random password
    private String generatePassword() {
        Random random = new Random();
        // 4 numbers
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            password.append(random.nextInt(10));
        }
        // 2 letters
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < 2; i++) {
            password.append(letters.charAt(random.nextInt(letters.length())));
        }
        return password.toString();
    }
    
    // Verify admin login
    public Admin verifyAdmin(String username, String password) {
        String sql = "SELECT * FROM admins WHERE username = ? AND password = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Admin(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("country")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error verifying admin: " + e.getMessage());
        }
        
        return null;
    }
    
    // Verify applicant login
    public Applicant verifyApplicant(String applicationId, String password) {
        String sql = "SELECT * FROM applicants WHERE application_id = ? AND password = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, applicationId);
            stmt.setString(2, hashPassword(password));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Applicant(
                    rs.getInt("id"),
                    rs.getString("application_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("country"),
                    rs.getString("visa_type"),
                    rs.getString("status")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error verifying applicant: " + e.getMessage());
        }
        
        return null;
    }
    
    // Create new applicant and return application ID and password
    public ApplicationCredentials createApplicant(
            String firstName, String lastName, String nationalId, String nationality,
            String passport, String email, String phone, String address,
            String country, String visaType) {
        
        String applicationId = generateApplicationId();
        String password = generatePassword();
        
        String sql = "INSERT INTO applicants (application_id, password, first_name, last_name, " +
                "national_id, nationality, passport, email, phone, address, country, visa_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, applicationId);
            stmt.setString(2, hashPassword(password));
            stmt.setString(3, firstName);
            stmt.setString(4, lastName);
            stmt.setString(5, nationalId);
            stmt.setString(6, nationality);
            stmt.setString(7, passport);
            stmt.setString(8, email);
            stmt.setString(9, phone);
            stmt.setString(10, address);
            stmt.setString(11, country);
            stmt.setString(12, visaType);
            
            stmt.executeUpdate();
            
            return new ApplicationCredentials(applicationId, password);
            
        } catch (SQLException e) {
            System.err.println("Error creating applicant: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Save additional visa details
    public void saveVisaDetail(String applicationId, String fieldName, String fieldValue) {
        String sql = "INSERT INTO visa_details (application_id, field_name, field_value) VALUES (?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, applicationId);
            stmt.setString(2, fieldName);
            stmt.setString(3, fieldValue);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error saving visa detail: " + e.getMessage());
        }
    }
    
    // Generate application ID
    private String generateApplicationId() {
        Random random = new Random();
        int randomNum = random.nextInt(9000000) + 1000000; // 7-digit number (1000000-9999999)
        return "VSA" + randomNum;
    }
    
    // Get database connection (for dashboard controllers)
    public Connection getConnection() throws SQLException {
        // Log every connection to ensure we're using the same database file
        if (Math.random() < 0.1) { // Log 10% of connections to avoid spam
            System.out.println("[DB Connection] Using database: " + DB_FILE);
        }
        
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        
        // Set SQLite to synchronous mode for immediate writes
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA synchronous = FULL");
            stmt.execute("PRAGMA journal_mode = DELETE");
            stmt.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            System.err.println("Warning: Could not set PRAGMA settings: " + e.getMessage());
        }
        
        return conn;
    }
    
    // Update application status with visa duration (for approval)
    public boolean updateApplicationStatus(String applicationId, String status, String visaDuration, String approvedBy) {
        System.out.println("\n======== DATABASE UPDATE STARTING ========");
        System.out.println("Application ID: " + applicationId);
        System.out.println("New Status: " + status);
        System.out.println("Duration: " + visaDuration);
        System.out.println("Approved By: " + approvedBy);
        
        String sql = "UPDATE applicants SET status = ?, visa_duration = ?, approved_by = ?, approval_date = CURRENT_TIMESTAMP WHERE application_id = ?";
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status);
                stmt.setString(2, visaDuration);
                stmt.setString(3, approvedBy);
                stmt.setString(4, applicationId);
                
                System.out.println("Executing UPDATE query...");
                int rowsUpdated = stmt.executeUpdate();
                System.out.println("Rows updated: " + rowsUpdated);
                
                if (rowsUpdated == 0) {
                    System.err.println("✗ ERROR: No rows updated! Application ID '" + applicationId + "' not found in database!");
                    conn.rollback();
                    return false;
                }
                
                conn.commit(); // Explicitly commit the transaction
                System.out.println("✓ Transaction committed");
                
                // Force database to flush to disk
                try (Statement checkpointStmt = conn.createStatement()) {
                    checkpointStmt.execute("PRAGMA wal_checkpoint(FULL)");
                    System.out.println("✓ Database checkpoint executed");
                } catch (SQLException e) {
                    // Ignore if WAL mode is not active
                }
            }
            
            // Verify with fresh connection
            System.out.println("Opening new connection to verify...");
            try (Connection verifyConn = getConnection();
                 PreparedStatement verifyStmt = verifyConn.prepareStatement(
                     "SELECT status, visa_duration, approved_by FROM applicants WHERE application_id = ?")) {
                
                verifyStmt.setString(1, applicationId);
                ResultSet rs = verifyStmt.executeQuery();
                
                if (rs.next()) {
                    String dbStatus = rs.getString("status");
                    String dbDuration = rs.getString("visa_duration");
                    String dbApprovedBy = rs.getString("approved_by");
                    
                    System.out.println("✓ VERIFIED in DB:");
                    System.out.println("  - Status: " + dbStatus);
                    System.out.println("  - Duration: " + dbDuration);
                    System.out.println("  - Approved By: " + dbApprovedBy);
                    
                    if (dbStatus.equals(status)) {
                        System.out.println("✓✓✓ DATABASE UPDATE SUCCESSFUL ✓✓✓");
                        System.out.println("========================================\n");
                        return true;
                    } else {
                        System.err.println("✗ ERROR: Status mismatch! Expected: " + status + ", Got: " + dbStatus);
                        return false;
                    }
                } else {
                    System.err.println("✗ ERROR: Application not found in verification query!");
                    return false;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("✗ SQL ERROR: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================\n");
            System.err.println("✗ SQL ERROR: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================\n");
            return false;
        }
    }
    
    // Get visa duration options based on visa type
    public static String[] getVisaDurationOptions(String visaType) {
        if (visaType.contains("Tourist")) {
            return new String[]{"30 days", "60 days", "90 days", "6 months", "1 year"};
        } else if (visaType.contains("Medical")) {
            return new String[]{"30 days", "60 days", "90 days", "6 months"};
        } else if (visaType.contains("Student")) {
            return new String[]{"1 year", "2 years", "3 years", "4 years", "5 years"};
        } else if (visaType.contains("Work")) {
            return new String[]{"1 year", "2 years", "3 years", "5 years"};
        }
        return new String[]{"30 days", "90 days", "1 year"};
    }
    
    // Inner classes for return types
    public static class Admin {
        private final int id;
        private final String username;
        private final String country;
        
        public Admin(int id, String username, String country) {
            this.id = id;
            this.username = username;
            this.country = country;
        }
        
        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getCountry() { return country; }
    }
    
    public static class Applicant {
        private final int id;
        private final String applicationId;
        private final String firstName;
        private final String lastName;
        private final String email;
        private final String country;
        private final String visaType;
        private final String status;
        
        public Applicant(int id, String applicationId, String firstName, String lastName,
                        String email, String country, String visaType, String status) {
            this.id = id;
            this.applicationId = applicationId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.country = country;
            this.visaType = visaType;
            this.status = status;
        }
        
        public int getId() { return id; }
        public String getApplicationId() { return applicationId; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public String getCountry() { return country; }
        public String getVisaType() { return visaType; }
        public String getStatus() { return status; }
    }
    
    public static class ApplicationCredentials {
        private final String applicationId;
        private final String password;
        
        public ApplicationCredentials(String applicationId, String password) {
            this.applicationId = applicationId;
            this.password = password;
        }
        
        public String getApplicationId() { return applicationId; }
        public String getPassword() { return password; }
    }
}
