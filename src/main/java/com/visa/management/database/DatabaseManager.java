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
            
            // Create notices table for country-specific notices from admin
            stmt.execute("CREATE TABLE IF NOT EXISTS notices (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "country TEXT NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "content TEXT NOT NULL, " +
                    "notice_type TEXT NOT NULL, " + // 'RULES', 'REJECTION_REASONS', 'GENERAL'
                    "created_by TEXT NOT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
            
            // Create applicant_messages table for messages from applicants to admin
            stmt.execute("CREATE TABLE IF NOT EXISTS applicant_messages (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "application_id TEXT NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "status TEXT DEFAULT 'UNREAD', " + // 'UNREAD', 'READ', 'REPLIED'
                    "admin_reply TEXT, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "replied_at DATETIME, " +
                    "FOREIGN KEY (application_id) REFERENCES applicants(application_id))");
            
            // Create rejection_history table to track rejections and prevent re-application
            stmt.execute("CREATE TABLE IF NOT EXISTS rejection_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "application_id TEXT NOT NULL, " +
                    "national_id TEXT NOT NULL, " +
                    "passport TEXT NOT NULL, " +
                    "country TEXT NOT NULL, " +
                    "rejection_reason TEXT, " +
                    "rejection_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "ban_duration_months INTEGER DEFAULT 6, " + // Default 6 months ban
                    "ban_until_date DATETIME, " +
                    "rejected_by TEXT NOT NULL, " +
                    "FOREIGN KEY (application_id) REFERENCES applicants(application_id))");
            
            // Create documents table for uploaded files
            stmt.execute("CREATE TABLE IF NOT EXISTS documents (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "application_id TEXT NOT NULL, " +
                    "filename TEXT NOT NULL, " +
                    "file_path TEXT NOT NULL, " +
                    "file_type TEXT, " +
                    "file_size INTEGER, " +
                    "uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (application_id) REFERENCES applicants(application_id))");
            
            // Insert default admin accounts (one for each country) if not exists
            insertDefaultAdmins();
            
            // Insert default notices for all countries
            insertDefaultNotices();
            
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
    
    private void insertDefaultNotices() {
        String[] countries = {"India", "Bangladesh", "Japan", "Malaysia", "New Zealand", "Singapore"};
        
        try (Connection conn = getConnection()) {
            String checkSql = "SELECT COUNT(*) FROM notices WHERE country = ? AND notice_type = ?";
            String insertSql = "INSERT INTO notices (country, title, content, notice_type, created_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, datetime('now'), datetime('now'))";
            
            for (String country : countries) {
                // Check if default notices already exist for this country
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, country);
                    checkStmt.setString(2, "RULES");
                    ResultSet rs = checkStmt.executeQuery();
                    rs.next();
                    
                    if (rs.getInt(1) == 0) {
                        // Insert default rules notice
                        String rulesContent = "General Visa Requirements:\n" +
                            "• Valid passport \n" +
                            "• Completed application form\n" +
                            "• Recent passport-size photographs\n" +
                            "• Proof of financial means\n" +
                            "• Return flight tickets\n" +
                            "• Accommodation proof\n" +
                            "• Travel insurance (recommended)\n\n" +
                            "Processing Time: 10-15 business days\n" +
                            "Please ensure all documents are authentic and up-to-date.";
                        
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setString(1, country);
                            insertStmt.setString(2, "Visa Application Requirements");
                            insertStmt.setString(3, rulesContent);
                            insertStmt.setString(4, "RULES");
                            insertStmt.setString(5, "system");
                            insertStmt.executeUpdate();
                        }
                        
                        // Insert default rejection reasons notice
                        String rejectionContent = "Common Reasons for Visa Rejection:\n" +
                            "• Incomplete or incorrect application form\n" +
                            "• Insufficient financial proof\n" +
                            "• Invalid or expired documents\n" +
                            "• Previous visa violations\n" +
                            "• Suspicious travel history\n" +
                            "• Missing required documents\n" +
                            "• False information or fraud\n\n" +
                            "To avoid rejection, please ensure all information is accurate and complete.";
                        
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setString(1, country);
                            insertStmt.setString(2, "Common Rejection Reasons");
                            insertStmt.setString(3, rejectionContent);
                            insertStmt.setString(4, "REJECTION_REASONS");
                            insertStmt.setString(5, "system");
                            insertStmt.executeUpdate();
                        }
                        
                        System.out.println("Created default notices for: " + country);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting default notices: " + e.getMessage());
            e.printStackTrace();
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
    
    public static class DocumentInfo {
        private final int id;
        private final String applicationId;
        private final String filename;
        private final String filePath;
        private final String fileType;
        private final long fileSize;
        private final String uploadedAt;
        
        public DocumentInfo(int id, String applicationId, String filename, String filePath, 
                          String fileType, long fileSize, String uploadedAt) {
            this.id = id;
            this.applicationId = applicationId;
            this.filename = filename;
            this.filePath = filePath;
            this.fileType = fileType;
            this.fileSize = fileSize;
            this.uploadedAt = uploadedAt;
        }
        
        public int getId() { return id; }
        public String getApplicationId() { return applicationId; }
        public String getFilename() { return filename; }
        public String getFilePath() { return filePath; }
        public String getFileType() { return fileType; }
        public long getFileSize() { return fileSize; }
        public String getUploadedAt() { return uploadedAt; }
    }
    
    // Save document information
    public boolean saveDocument(String applicationId, String filename, String filePath, 
                                String fileType, long fileSize) {
        String sql = "INSERT INTO documents (application_id, filename, file_path, file_type, file_size) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, applicationId);
            pstmt.setString(2, filename);
            pstmt.setString(3, filePath);
            pstmt.setString(4, fileType);
            pstmt.setLong(5, fileSize);
            
            int affected = pstmt.executeUpdate();
            System.out.println("Document saved: " + filename + " for application " + applicationId);
            return affected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error saving document: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Get all documents for an application
    public java.util.List<DocumentInfo> getDocuments(String applicationId) {
        java.util.List<DocumentInfo> documents = new java.util.ArrayList<>();
        String sql = "SELECT id, application_id, filename, file_path, file_type, file_size, uploaded_at " +
                    "FROM documents WHERE application_id = ? ORDER BY uploaded_at DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, applicationId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                documents.add(new DocumentInfo(
                    rs.getInt("id"),
                    rs.getString("application_id"),
                    rs.getString("filename"),
                    rs.getString("file_path"),
                    rs.getString("file_type"),
                    rs.getLong("file_size"),
                    rs.getString("uploaded_at")
                ));
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving documents: " + e.getMessage());
            e.printStackTrace();
        }
        
        return documents;
    }
    
    // ==================== NOTICE MANAGEMENT ====================
    
    // Add or update notice
    public boolean saveNotice(String country, String title, String content, String noticeType, String createdBy) {
        String checkSql = "SELECT id FROM notices WHERE country = ? AND notice_type = ?";
        String insertSql = "INSERT INTO notices (country, title, content, notice_type, created_by) VALUES (?, ?, ?, ?, ?)";
        String updateSql = "UPDATE notices SET title = ?, content = ?, updated_at = CURRENT_TIMESTAMP WHERE country = ? AND notice_type = ?";
        
        try (Connection conn = getConnection()) {
            // Check if notice exists
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, country);
                checkStmt.setString(2, noticeType);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    // Update existing notice
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, title);
                        updateStmt.setString(2, content);
                        updateStmt.setString(3, country);
                        updateStmt.setString(4, noticeType);
                        return updateStmt.executeUpdate() > 0;
                    }
                } else {
                    // Insert new notice
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, country);
                        insertStmt.setString(2, title);
                        insertStmt.setString(3, content);
                        insertStmt.setString(4, noticeType);
                        insertStmt.setString(5, createdBy);
                        return insertStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving notice: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Get notices for a country
    public java.util.List<Notice> getNotices(String country) {
        java.util.List<Notice> notices = new java.util.ArrayList<>();
        String sql = "SELECT * FROM notices WHERE country = ? ORDER BY notice_type, updated_at DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, country);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                notices.add(new Notice(
                    rs.getInt("id"),
                    rs.getString("country"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getString("notice_type"),
                    rs.getString("created_by"),
                    rs.getString("created_at"),
                    rs.getString("updated_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving notices: " + e.getMessage());
        }
        return notices;
    }
    
    // Delete notice
    public boolean deleteNotice(int noticeId) {
        String sql = "DELETE FROM notices WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, noticeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting notice: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== MESSAGE MANAGEMENT ====================
    
    // Send message from applicant
    public boolean sendApplicantMessage(String applicationId, String message) {
        String sql = "INSERT INTO applicant_messages (application_id, message) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, applicationId);
            stmt.setString(2, message);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error sending message: " + e.getMessage());
            return false;
        }
    }
    
    // Get messages for an application
    public java.util.List<ApplicantMessage> getMessagesForApplication(String applicationId) {
        java.util.List<ApplicantMessage> messages = new java.util.ArrayList<>();
        String sql = "SELECT * FROM applicant_messages WHERE application_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, applicationId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                messages.add(new ApplicantMessage(
                    rs.getInt("id"),
                    rs.getString("application_id"),
                    rs.getString("message"),
                    rs.getString("status"),
                    rs.getString("admin_reply"),
                    rs.getString("created_at"),
                    rs.getString("replied_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving messages: " + e.getMessage());
        }
        return messages;
    }
    
    // Get all messages for admin (by country)
    public java.util.List<ApplicantMessage> getAllMessagesForCountry(String country) {
        java.util.List<ApplicantMessage> messages = new java.util.ArrayList<>();
        String sql = "SELECT m.* FROM applicant_messages m " +
                    "JOIN applicants a ON m.application_id = a.application_id " +
                    "WHERE a.country = ? ORDER BY m.created_at DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, country);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                messages.add(new ApplicantMessage(
                    rs.getInt("id"),
                    rs.getString("application_id"),
                    rs.getString("message"),
                    rs.getString("status"),
                    rs.getString("admin_reply"),
                    rs.getString("created_at"),
                    rs.getString("replied_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving messages: " + e.getMessage());
        }
        return messages;
    }
    
    // Mark message as read
    public boolean markMessageAsRead(int messageId) {
        String sql = "UPDATE applicant_messages SET status = 'READ' WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, messageId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error marking message as read: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== REJECTION TRACKING ====================
    
    // Check if applicant is banned from reapplying
    public RejectionBan checkRejectionBan(String nationalId, String passport, String country) {
        String sql = "SELECT * FROM rejection_history WHERE national_id = ? AND passport = ? AND country = ? " +
                    "AND ban_until_date > datetime('now') ORDER BY ban_until_date DESC LIMIT 1";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nationalId);
            stmt.setString(2, passport);
            stmt.setString(3, country);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new RejectionBan(
                    true,
                    rs.getString("rejection_reason"),
                    rs.getString("rejection_date"),
                    rs.getString("ban_until_date"),
                    rs.getInt("ban_duration_months")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error checking rejection ban: " + e.getMessage());
        }
        return new RejectionBan(false, null, null, null, 0);
    }
    
    // Add rejection to history
    public boolean addRejectionHistory(String applicationId, String nationalId, String passport, 
                                      String country, String rejectionReason, int banDurationMonths, 
                                      String rejectedBy) {
        String sql = "INSERT INTO rejection_history (application_id, national_id, passport, country, " +
                    "rejection_reason, ban_duration_months, ban_until_date, rejected_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, datetime('now', '+' || ? || ' months'), ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, applicationId);
            stmt.setString(2, nationalId);
            stmt.setString(3, passport);
            stmt.setString(4, country);
            stmt.setString(5, rejectionReason);
            stmt.setInt(6, banDurationMonths);
            stmt.setInt(7, banDurationMonths);
            stmt.setString(8, rejectedBy);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding rejection history: " + e.getMessage());
            return false;
        }
    }
    
    // Check for duplicate/existing application (Processing or Approved)
    public ApplicationInfo checkExistingApplication(String nationalId, String passport, String country) {
        String sql = "SELECT application_id, status, country, created_at FROM applicants " +
                    "WHERE national_id = ? AND passport = ? AND country = ? " +
                    "AND (status = 'Processing' OR status = 'Approved') " +
                    "ORDER BY created_at DESC LIMIT 1";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nationalId);
            stmt.setString(2, passport);
            stmt.setString(3, country);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new ApplicationInfo(
                    rs.getString("application_id"),
                    rs.getString("status"),
                    rs.getString("country"),
                    rs.getString("created_at")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error checking existing application: " + e.getMessage());
        }
        return null;
    }
    
    // Get travel/rejection history for an applicant (all countries, filtered by nationality)
    public java.util.List<TravelHistory> getTravelHistory(String nationalId, String passport, String nationality) {
        java.util.List<TravelHistory> history = new java.util.ArrayList<>();
        String sql = "SELECT a.application_id, a.country, a.visa_type, a.status, a.created_at, " +
                    "r.rejection_reason, r.rejection_date, r.ban_until_date " +
                    "FROM applicants a " +
                    "LEFT JOIN rejection_history r ON a.application_id = r.application_id " +
                    "WHERE a.national_id = ? AND a.passport = ? AND a.nationality = ? " +
                    "ORDER BY a.created_at DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nationalId);
            stmt.setString(2, passport);
            stmt.setString(3, nationality);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                history.add(new TravelHistory(
                    rs.getString("application_id"),
                    rs.getString("country"),
                    rs.getString("visa_type"),
                    rs.getString("status"),
                    rs.getString("created_at"),
                    rs.getString("rejection_reason"),
                    rs.getString("rejection_date"),
                    rs.getString("ban_until_date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving travel history: " + e.getMessage());
        }
        return history;
    }
    
    // ==================== INNER CLASSES ====================
    
    public static class Notice {
        private final int id;
        private final String country;
        private final String title;
        private final String content;
        private final String noticeType;
        private final String createdBy;
        private final String createdAt;
        private final String updatedAt;
        
        public Notice(int id, String country, String title, String content, String noticeType,
                     String createdBy, String createdAt, String updatedAt) {
            this.id = id;
            this.country = country;
            this.title = title;
            this.content = content;
            this.noticeType = noticeType;
            this.createdBy = createdBy;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
        
        public int getId() { return id; }
        public String getCountry() { return country; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getNoticeType() { return noticeType; }
        public String getCreatedBy() { return createdBy; }
        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
    }
    
    public static class ApplicantMessage {
        private final int id;
        private final String applicationId;
        private final String message;
        private String status;  // Changed from final to allow updates
        private final String adminReply;
        private final String createdAt;
        private final String repliedAt;
        
        public ApplicantMessage(int id, String applicationId, String message, String status,
                              String adminReply, String createdAt, String repliedAt) {
            this.id = id;
            this.applicationId = applicationId;
            this.message = message;
            this.status = status;
            this.adminReply = adminReply;
            this.createdAt = createdAt;
            this.repliedAt = repliedAt;
        }
        
        public int getId() { return id; }
        public String getApplicationId() { return applicationId; }
        public String getMessage() { return message; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }  // Added setter
        public String getAdminReply() { return adminReply; }
        public String getCreatedAt() { return createdAt; }
        public String getRepliedAt() { return repliedAt; }
    }
    
    // Inner class for existing application information
    public static class ApplicationInfo {
        private final String applicationId;
        private final String status;
        private final String country;
        private final String createdAt;
        
        public ApplicationInfo(String applicationId, String status, String country, String createdAt) {
            this.applicationId = applicationId;
            this.status = status;
            this.country = country;
            this.createdAt = createdAt;
        }
        
        public String getApplicationId() { return applicationId; }
        public String getStatus() { return status; }
        public String getCountry() { return country; }
        public String getCreatedAt() { return createdAt; }
    }
    
    public static class RejectionBan {
        private final boolean isBanned;
        private final String reason;
        private final String rejectionDate;
        private final String banUntilDate;
        private final int banDurationMonths;
        
        public RejectionBan(boolean isBanned, String reason, String rejectionDate, 
                          String banUntilDate, int banDurationMonths) {
            this.isBanned = isBanned;
            this.reason = reason;
            this.rejectionDate = rejectionDate;
            this.banUntilDate = banUntilDate;
            this.banDurationMonths = banDurationMonths;
        }
        
        public boolean isBanned() { return isBanned; }
        public String getReason() { return reason; }
        public String getRejectionDate() { return rejectionDate; }
        public String getBanUntilDate() { return banUntilDate; }
        public int getBanDurationMonths() { return banDurationMonths; }
    }
    
    public static class TravelHistory {
        private final String applicationId;
        private final String country;
        private final String visaType;
        private final String status;
        private final String appliedDate;
        private final String rejectionReason;
        private final String rejectionDate;
        private final String banUntilDate;
        
        public TravelHistory(String applicationId, String country, String visaType, String status,
                           String appliedDate, String rejectionReason, String rejectionDate, String banUntilDate) {
            this.applicationId = applicationId;
            this.country = country;
            this.visaType = visaType;
            this.status = status;
            this.appliedDate = appliedDate;
            this.rejectionReason = rejectionReason;
            this.rejectionDate = rejectionDate;
            this.banUntilDate = banUntilDate;
        }
        
        public String getApplicationId() { return applicationId; }
        public String getCountry() { return country; }
        public String getVisaType() { return visaType; }
        public String getStatus() { return status; }
        public String getAppliedDate() { return appliedDate; }
        public String getRejectionReason() { return rejectionReason; }
        public String getRejectionDate() { return rejectionDate; }
        public String getBanUntilDate() { return banUntilDate; }
    }
}
