# Testing View Button in Admin Dashboard

## Steps to Test:

1. **Run the Application** from your IDE (IntelliJ IDEA)

2. **Login as Admin:**
   - Click "Login"
   - Select "Admin" radio button
   - Username: `admin_india`
   - Password: `india123`

3. **In Admin Dashboard:**
   - You should see applications listed
   - Find application ID: `VSA4505919` (Approved)
   - Click the **blue "View" button**

4. **What Should Happen:**
   - Console will show:
     ```
     ===========================================
     VIEW BUTTON CLICKED
     Application ID: VSA4505919
     ===========================================
     Controller retrieved: SUCCESS
     Application ID set on controller
     ```
   - Screen changes to Application Details page
   - Shows all application information

5. **What to Check in Application Details:**
   - ✓ Personal Information section
   - ✓ Visa Information section
   - ✓ Approval Details section (Duration, Approved By, Date)
   - ✓ **Visa-Specific Details section** (NEW!)
     - Account Number: 7020654
     - Bank Name: DBBL
     - Flight No: 789654
     - Hotel PNR: 1020304
     - Journey Date: 2026-01-15
     - Previous Travel: saudi Arabia, Bahrain
     - References: Firoz shah
     - Tourist Places: Delhi
     - Travel History: Yes - Multiple Countries
   - ✓ **Documents section** (NEW!)

## If Nothing Shows:

### Check Console Output:
Look for error messages in the console/terminal

### Common Issues:

**Issue 1: No console output when clicking View**
- The button click event might not be firing
- Check if you're clicking the correct button (blue "View" button)

**Issue 2: "Controller is null" message**
- The FXML file might not be loading correctly
- Check that `application-details.fxml` exists in `src/main/resources/fxml/`

**Issue 3: "Application not found" error**
- Check database connection
- Run DatabaseInspector to verify data exists

**Issue 4: Scene changes but shows blank screen**
- Missing fx:id in FXML
- Check console for NullPointerException

## Run Database Inspector:

```powershell
java -cp "target/classes;$env:USERPROFILE\.m2\repository\org\xerial\sqlite-jdbc\3.42.0.0\sqlite-jdbc-3.42.0.0.jar" com.visa.management.debug.DatabaseInspector
```

This will show you all data in the database.

## Debug Mode:

The code now has extensive debug logging. When you click View, you should see detailed console output showing:
- Which application was clicked
- Controller loading status
- Database query results
- Each step of the loading process

**Share the console output if the issue persists!**
