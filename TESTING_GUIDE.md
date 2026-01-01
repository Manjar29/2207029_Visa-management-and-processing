# COMPLETE TESTING GUIDE - Visa Management System

## ⚠️ IMPORTANT: First Steps

### 1. Reset the Database
Before testing, you MUST reset the database to use the new format:

**Option A - Using the Script (Recommended):**
```bash
# Double-click or run:
RESET_DATABASE.bat
```

**Option B - Manual:**
1. Close the application if running
2. Delete the file `visadb.db` from your project root folder
3. The database will be recreated automatically when you run the application

### 2. New Credential Formats
- **Application ID**: `VSA1234567` (VSA + 7 digits)
- **Password**: `1234Ab` (4 numbers + 2 letters)

---

## 🧪 Complete Test Workflow

### TEST 1: Apply for Visa ✅

1. **Start Application**
   - Run the VisaManagementApp
   - Home page should appear

2. **Apply for Visa**
   - Click "Apply for Visa" button
   - Select Country (e.g., India)
   - Click "Next"

3. **Select Visa Type**
   - Choose visa type (Tourist/Medical/Student/Work)
   - Click "Next"

4. **Fill Application Form**
   - Enter all required details:
     - First Name: John
     - Last Name: Doe
     - National ID: 123456789
     - Nationality: American
     - Passport Number: US123456789
     - Email: john.doe@email.com
     - Phone: +1234567890
     - Address: 123 Main St
   - Fill visa-specific fields based on type
   - **IMPORTANT**: Journey Date, Appointment Date, etc. - past dates are disabled
   - Click "Submit Application"

5. **Save Credentials**
   - Alert will show:
     - Application ID: **VSA1234567** (example - save this!)
     - Password: **1234Ab** (example - save this!)
   - **Write these down** - you need them for login!

### TEST 2: Check Status (Guest) ✅

1. **From Home Page**
   - Click "Check Status" button

2. **Enter Application ID Only**
   - Enter the Application ID from TEST 1 (e.g., VSA1234567)
   - **NO password required for check status**
   - Click "Check Status"

3. **Verify Display**
   - Application ID: VSA1234567
   - Applicant Name: John Doe
   - Visa Type: Tourist Visa
   - Country: India
   - Submission Date: (today's date)
   - Status Badge: **Processing** (orange color)
   - Visa Duration: *Not visible yet* (only shows when approved)

### TEST 3: Admin Login & Dashboard ✅

1. **Go to Login**
   - Click "← Back" to return home
   - Click "Login" button

2. **Admin Login**
   - Select "Admin" radio button
   - Username: `admin_india`
   - Password: `india123`
   - Click "Login"

**Available Admin Accounts:**
- India: `admin_india` / `india123`
- Bangladesh: `admin_bangladesh` / `bangladesh123`
- Japan: `admin_japan` / `japan123`
- Malaysia: `admin_malaysia` / `malaysia123`
- New Zealand: `admin_new_zealand` / `new_zealand123`
- Singapore: `admin_singapore` / `singapore123`

3. **Verify Admin Dashboard**
   - Should see applications table
   - Statistics cards show:
     - Total Applications
     - Processing (should be 1)
     - Approved (0)
     - Rejected (0)
   - Your test application should be in the table with "Processing" status

4. **Filter Applications**
   - Try the status filter dropdown
   - Select "Processing" - should see your application
   - Select "All" - should see all applications

### TEST 4: Approve Application ✅

1. **In Admin Dashboard**
   - Find the application (VSA1234567)
   - Click "Approve" button

2. **Select Visa Duration**
   - Dialog appears with duration options
   - Duration options vary by visa type:
     - **Tourist**: 30 days, 60 days, 90 days, 6 months, 1 year
     - **Medical**: 30 days, 60 days, 90 days, 6 months
     - **Student**: 1 year, 2 years, 3 years, 4 years, 5 years
     - **Work**: 1 year, 2 years, 3 years, 5 years
   - Select a duration (e.g., "90 days" for tourist)
   - Click "OK"

3. **Verify Table Updates**
   - Status should change to **"Approved"** (green)
   - Statistics should update:
     - Processing: 0
     - Approved: 1

### TEST 5: Applicant Login ✅

1. **Logout/Go to Login**
   - Click "Logout" if button exists, or restart app
   - Click "Login"

2. **Applicant Login**
   - Select "Applicant" radio button
   - Application ID: `VSA1234567` (from TEST 1)
   - Password: `1234Ab` (from TEST 1)
   - Click "Login"

3. **Verify Applicant Dashboard**
   - Application ID: VSA1234567
   - Status: **Approved** (green badge)
   - Full Name: John Doe
   - Nationality: American
   - Email: john.doe@email.com
   - Phone: +1234567890
   - Country: India
   - Visa Type: Tourist Visa
   - **Visa Duration**: 90 days ✓
   - **Approved By**: admin_india ✓
   - **Approval Date**: (today's date) ✓
   - **Visa Conditions Box**: Should be visible with green background showing requirements

4. **Test Refresh**
   - Click "Refresh Status" button
   - All data should reload from database

### TEST 6: Check Status Again (Approved) ✅

1. **Logout and Go to Home**
   - Return to home page

2. **Check Status**
   - Click "Check Status"
   - Enter Application ID: VSA1234567
   - Click "Check Status"

3. **Verify Updated Display**
   - Status Badge: **Approved** (green)
   - **Visa Duration**: 90 days ✓ (now visible!)
   - All other details should match

### TEST 7: Rejection Workflow ✅

1. **Create Another Application**
   - Apply for visa with different details (TEST 1 steps)
   - Save the new credentials (e.g., VSA2345678 / 5678Cd)

2. **Admin Login**
   - Login as admin_india / india123

3. **Reject Application**
   - Find the new application
   - Click "Reject" button
   - Confirm rejection

4. **Verify**
   - Status changes to **"Rejected"** (red)
   - Statistics update

5. **Applicant View**
   - Login with the new credentials
   - Status should show **Rejected** (red badge)
   - Visa Duration, Approved By fields should be hidden

### TEST 8: Multiple Applications ✅

1. **Create 3-4 Applications**
   - Apply for different visa types
   - Different countries
   - Save all credentials

2. **Admin Dashboard**
   - Login as different admins
   - Each admin should only see applications for their country
   - Try approving with different durations
   - Try rejecting some

3. **Filter Testing**
   - Use status filter to view:
     - All applications
     - Only Processing
     - Only Approved
     - Only Rejected

### TEST 9: Date Validation ✅

1. **Apply for Visa**
   - Go to application form
   - Try to select dates in the past
   - **Expected**: Past dates should be disabled/greyed out
   - Only today and future dates should be selectable

---

## 🔍 Expected Behaviors

### Application ID & Password Generation
- **Format**: Application ID = `VSA` + 7 random digits (1000000-9999999)
- **Format**: Password = 4 random digits + 2 random letters
- **Example**: `VSA1234567` / `1234Ab`

### Status Colors
- **Processing**: Orange background
- **Approved**: Green background
- **Rejected**: Red background

### Admin Country Access
- Each admin sees only applications for their assigned country
- Admin for India sees only India applications

### Visa Duration Options
Different options based on visa type:
- **Tourist Visa**: 30 days, 60 days, 90 days, 6 months, 1 year
- **Medical Visa**: 30 days, 60 days, 90 days, 6 months
- **Student Visa**: 1 year, 2 years, 3 years, 4 years, 5 years
- **Work Visa**: 1 year, 2 years, 3 years, 5 years

---

## ❌ Common Issues & Solutions

### Issue 1: "Login not working"
- **Solution**: Make sure database is reset using RESET_DATABASE.bat
- Old credentials won't work with new format

### Issue 2: "Application not found in Check Status"
- **Solution**: Verify you entered the exact Application ID (case-sensitive)
- Make sure database contains the application

### Issue 3: "Status not updating after approval"
- **Solution**: 
  - Refresh the page/dashboard
  - For applicant: click "Refresh Status" button
  - For admin: table should auto-update after approval

### Issue 4: "Can't select dates"
- **Solution**: Past dates are intentionally disabled
- Select today or future dates

### Issue 5: "Admin sees all applications"
- **Solution**: This might be a bug - admin should only see applications for their country
- Check the SQL query in AdminDashboardController.loadApplications()

---

## 📊 Database Verification

If you want to verify the database directly:

1. **Download SQLite Browser**: https://sqlitebrowser.org/
2. **Open**: visadb.db
3. **Check Tables**:
   - `admins` - should have 6 admin accounts
   - `applicants` - should have your test applications
   - `visa_details` - should have visa-specific details

---

## ✅ Success Criteria

All functionalities are working if:
- [x] Applications can be submitted with new ID format (VSA + 7 digits)
- [x] Passwords follow new format (4 nums + 2 letters)
- [x] Check Status works with only Application ID (no password)
- [x] Admin can login and see dashboard
- [x] Admin can approve with duration selection
- [x] Admin can reject applications
- [x] Table updates after approval/rejection
- [x] Applicant can login with Application ID + Password
- [x] Applicant dashboard shows all details correctly
- [x] When approved: visa duration, approved by, approval date visible
- [x] Check Status shows updated status (Approved/Rejected)
- [x] Date pickers prevent selecting past dates
- [x] Statistics cards show correct counts

---

## 🎯 Quick Test (5 minutes)

1. Run RESET_DATABASE.bat
2. Start application
3. Apply for visa → Save credentials
4. Check status with Application ID → See "Processing"
5. Login as admin_india → Approve with duration
6. Login as applicant → See "Approved" with duration
7. Check status again → See "Approved" status

If all 7 steps work, the system is functioning correctly! ✅
