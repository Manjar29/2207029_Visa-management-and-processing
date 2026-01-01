# Visa Management System - Login Functionality Implementation

## Overview
Complete login system has been implemented with database support for both administrators and applicants.

## Database Setup

### H2 Database
- **Location**: `./visadb` (in project root)
- **Access**: Embedded database, auto-created on first run
- **Tables**: 
  - `admins` - Administrator accounts
  - `applicants` - Visa applicant records
  - `visa_details` - Additional visa-specific information

### Admin Accounts (Pre-configured)
The system creates 6 admin accounts automatically:

| Country        | Username              | Password          |
|----------------|-----------------------|-------------------|
| India          | admin_india           | india123          |
| Bangladesh     | admin_bangladesh      | bangladesh123     |
| Japan          | admin_japan           | japan123          |
| Malaysia       | admin_malaysia        | malaysia123       |
| New Zealand    | admin_new_zealand     | new_zealand123    |
| Singapore      | admin_singapore       | singapore123      |

### Applicant Accounts
- **Auto-generated** when user submits visa application
- **Application ID Format**: VSA-YYYY-NNNNN (e.g., VSA-2025-12345)
- **Password**: 8-character random alphanumeric string
- Credentials are displayed to user after successful application submission

## Features Implemented

### 1. Login Screen ([login.fxml](src/main/resources/fxml/login.fxml))
- Radio buttons to select login type (Admin/Applicant)
- Dynamic label changes based on selection
- Fields for username/Application ID and password
- Input validation and error handling

### 2. Admin Dashboard ([AdminDashboardController.java](src/main/java/com/visa/management/controllers/AdminDashboardController.java))
**Features:**
- View all applications for their assigned country
- Statistics: Total, Processing, Approved, Rejected counts
- Filter applications by status
- Approve/Reject applications
- View detailed application information
- Real-time table updates

**Access**: Country-specific - each admin sees only their country's applications

### 3. Applicant Dashboard ([ApplicantDashboardController.java](src/main/java/com/visa/management/controllers/ApplicantDashboardController.java))
**Features:**
- View application status
- Application details display
- Status tracking (Submitted → Processing → Decision)
- Refresh status functionality
- Print application (placeholder)

**Access**: Personal - applicants see only their own application

### 4. Database Manager ([DatabaseManager.java](src/main/java/com/visa/management/database/DatabaseManager.java))
**Key Methods:**
- `initializeDatabase()` - Creates all tables
- `insertDefaultAdmins()` - Creates 6 admin accounts
- `verifyAdmin(username, password)` - Admin authentication
- `verifyApplicant(applicationId, password)` - Applicant authentication
- `createApplicant(...)` - Saves application, returns credentials
- `saveVisaDetail(...)` - Stores visa-specific form data
- `hashPassword(password)` - SHA-256 password hashing
- `generateApplicationId()` - Creates unique application ID
- `generatePassword()` - Creates random 8-char password

**Security:**
- All passwords hashed with SHA-256
- Prepared statements prevent SQL injection
- Singleton pattern ensures single DB connection manager

### 5. Session Management ([VisaApplicationSession.java](src/main/java/com/visa/management/controllers/VisaApplicationSession.java))
**Enhanced with:**
- `adminUsername` - Logged-in admin username
- `adminCountry` - Admin's assigned country
- `applicantId` - Logged-in applicant's ID
- `applicantName` - Applicant's full name
- `clear()` - Clears all session data on logout

### 6. Updated Application Form ([ApplyVisaController.java](src/main/java/com/visa/management/controllers/ApplyVisaController.java))
**New functionality:**
- Saves applicant data to database
- Generates unique Application ID and password
- Displays credentials to user in alert
- Saves visa-type specific details to `visa_details` table
- Stores all form data for admin review

## User Workflows

### Admin Workflow
1. Navigate to Login from Home screen
2. Select "Admin" radio button
3. Enter admin credentials (e.g., admin_india / india123)
4. Click Login → Redirected to Admin Dashboard
5. View applications, filter by status
6. Review, Approve, or Reject applications
7. Logout returns to Home screen

### Applicant Workflow
1. Navigate to "Apply for Visa" from Home screen
2. Select country (e.g., India)
3. Select visa type (e.g., Tourist)
4. Fill out application form
5. Submit → Application saved to database
6. **Credentials displayed**: Application ID (VSA-2025-XXXXX) and Password
7. User notes down credentials
8. Navigate to Login from Home screen
9. Select "Applicant" radio button
10. Enter Application ID and Password
11. Click Login → Redirected to Applicant Dashboard
12. View application status and details
13. Logout returns to Home screen

## Database Schema

### `admins` Table
```sql
CREATE TABLE admins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(64) NOT NULL,
    country VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
```

### `applicants` Table
```sql
CREATE TABLE applicants (
    id INT AUTO_INCREMENT PRIMARY KEY,
    application_id VARCHAR(20) UNIQUE NOT NULL,
    password VARCHAR(64) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    national_id VARCHAR(50) NOT NULL,
    nationality VARCHAR(50) NOT NULL,
    passport_number VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address TEXT NOT NULL,
    country VARCHAR(50) NOT NULL,
    visa_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'Processing',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
```

### `visa_details` Table
```sql
CREATE TABLE visa_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    application_id VARCHAR(20) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    field_value TEXT,
    FOREIGN KEY (application_id) REFERENCES applicants(application_id)
)
```

## Testing Instructions

### Test Admin Login:
1. Run the application
2. Click "Login" button
3. Select "Admin" radio button
4. Enter:
   - Username: `admin_india`
   - Password: `india123`
5. Click "Login"
6. Should see Admin Dashboard with statistics

### Test Applicant Flow:
1. Click "Apply for Visa"
2. Select any country
3. Select visa type (e.g., Tourist)
4. Fill out all required fields:
   - First Name, Last Name
   - National ID
   - Nationality (from dropdown)
   - Passport Number
   - Email, Phone, Address
   - Travel History
   - Tourist-specific fields (if Tourist selected)
5. Click "Submit"
6. **Note down the Application ID and Password** shown in the alert
7. Go back to Home
8. Click "Login"
9. Select "Applicant" radio button
10. Enter the Application ID and Password
11. Click "Login"
12. Should see Applicant Dashboard with your application details

## Files Created/Modified

### New Files:
- `src/main/java/com/visa/management/database/DatabaseManager.java`
- `src/main/java/com/visa/management/controllers/AdminDashboardController.java`
- `src/main/java/com/visa/management/controllers/ApplicantDashboardController.java`
- `src/main/resources/fxml/admin-dashboard.fxml`
- `src/main/resources/fxml/applicant-dashboard.fxml`

### Modified Files:
- `pom.xml` - Added H2 database dependency
- `src/main/java/com/visa/management/controllers/LoginController.java` - Complete rewrite
- `src/main/java/com/visa/management/controllers/VisaApplicationSession.java` - Added admin/applicant fields
- `src/main/java/com/visa/management/controllers/ApplyVisaController.java` - Added database integration
- `src/main/resources/fxml/login.fxml` - Updated UI for admin/applicant selection

## Next Steps (Future Enhancements)

1. **Email Notifications**: Send credentials via email instead of showing in alert
2. **Forgot Password**: Implement password recovery mechanism
3. **Advanced Filtering**: Add date range filters, search by name/ID
4. **Export Reports**: Generate PDF reports of applications
5. **File Uploads**: Allow document uploads (passport copy, photos, etc.)
6. **Status History**: Track all status changes with timestamps
7. **Admin Comments**: Allow admins to add notes to applications
8. **Multi-language Support**: Translate UI to multiple languages

## Security Features

✅ **Password Hashing**: SHA-256 encryption for all passwords  
✅ **SQL Injection Protection**: Prepared statements used throughout  
✅ **Session Management**: Secure session data storage  
✅ **Role-based Access**: Admins and applicants have separate dashboards  
✅ **Country-based Filtering**: Admins only see their country's applications  

## Dependencies

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
</dependency>
```

---

**Implementation Date**: January 2025  
**Status**: ✅ Complete and Ready for Testing
