# Status Update Testing Guide

## CRITICAL: Before Testing

### 1. Close the Application
Make sure the Visa Management application is completely closed.

### 2. Delete ALL Database Files
Navigate to: `C:\Users\Acer\IdeaProjects\2207029_Visa-management-and-processing\`

Delete these files if they exist:
- `visadb.db`
- `visadb.db-journal`
- `visadb.db-wal`
- `visadb.db-shm`

**Why?** Old database files may conflict with the new absolute path implementation.

### 3. Rebuild the Project
In VS Code terminal, run:
```bash
mvn clean package
```

---

## Testing Steps

### Step 1: Start Application & Check Database Path
1. Run the application
2. **CHECK CONSOLE** - You should see:
   ```
   ╔════════════════════════════════════════════════════════════╗
   ║          INITIALIZING DATABASE MANAGER                     ║
   ╚════════════════════════════════════════════════════════════╝
   Database File: C:\Users\Acer\IdeaProjects\2207029_Visa-management-and-processing\visadb.db
   ```
3. **VERIFY** the path is absolute (starts with C:\)

### Step 2: Apply for a Visa
1. Click "Apply for Visa"
2. Select any country (e.g., India)
3. Select any visa type (e.g., Tourist Visa)
4. Fill in the form with test data
5. Submit the application
6. **SAVE THE CREDENTIALS** that are displayed:
   - Application ID: VSA1234567 (example)
   - Password: 1234Ab (example)

### Step 3: Login as Admin
1. Go back to home page
2. Click "Admin Login"
3. Use credentials:
   - Username: `admin_india` (or admin for the country you selected)
   - Password: `india123`

### Step 4: Approve Application (WATCH CONSOLE!)
1. Find the application in the table
2. Click "Approve" button
3. Select a visa duration (e.g., "90 days")
4. Click "Approve"

**CONSOLE SHOULD SHOW:**
```
=================================================
⏳ APPROVING APPLICATION
  - Application ID: VSA1234567
  - Duration: 90 days
  - Admin: admin_india
=================================================

✓ Successfully updated application: VSA1234567 to status: Approved
  - Visa Duration: 90 days
  - Approved By: admin_india
  - Rows affected: 1
  - Database checkpoint executed (forced write to disk)
  - Verified in DB - Status: Approved, Duration: 90 days

🔄 REFRESHING UI...

📋 ========== LOADING APPLICATIONS ==========
Country: India
Filter: All
SQL Query: SELECT application_id, first_name, last_name, visa_type, nationality, status, created_at FROM applicants WHERE country = ? ORDER BY created_at DESC
[1] VSA1234567 | John Doe | Status: Approved

✓ Total applications loaded: 1
===========================================

Table updated with 1 items

✓ UI REFRESH COMPLETED
=================================================
```

### Step 5: Verify in Admin Dashboard
**IMMEDIATELY CHECK:**
- Does the table show status as "Approved"?
- Are the Approve/Reject buttons disabled for this application?
- Do the statistics update to show 1 Approved application?

**IF THE STATUS STILL SHOWS "Processing":**
1. Check the console output carefully
2. Look for the line: `Verified in DB - Status: Approved`
3. If it says "Approved" in console but NOT in table → UI refresh issue
4. If it says "Processing" in console → Database update failed

### Step 6: Click Refresh Button
1. Click the "Refresh" button in admin dashboard
2. **CONSOLE SHOULD SHOW:**
   ```
   📋 ========== LOADING APPLICATIONS ==========
   [1] VSA1234567 | John Doe | Status: Approved
   ```
3. Check if the table now shows "Approved"

### Step 7: Login as Applicant
1. Logout from admin
2. Click "Applicant Login"
3. Use the saved credentials (Application ID + Password)

**CONSOLE SHOULD SHOW:**
```
╔════════════════════════════════════════════════════════════╗
║         LOADING APPLICANT DATA FROM DATABASE               ║
╚════════════════════════════════════════════════════════════╝
  - Status: Approved
  - Visa Duration: 90 days
  - Approved By: admin_india
✓ Application is APPROVED - showing approval details
```

**CHECK APPLICANT DASHBOARD:**
- Status should be "Approved" (green)
- Visa duration should be visible
- Approved by should be visible
- Approval date should be visible

---

## Troubleshooting

### Problem: "No rows updated" in console
**Cause:** Application ID doesn't exist in database
**Solution:** Check the Application ID is correct

### Problem: Console shows "Approved" but table shows "Processing"
**Cause:** UI not refreshing properly OR reading from different database file
**Solution:** 
1. Check if database path is the same in all console messages
2. Try clicking Refresh button
3. Logout and login again

### Problem: Multiple database files exist
**Cause:** Old relative path created db files in different locations
**Solution:** 
1. Search entire project folder for `visadb.db` files
2. Delete ALL of them
3. Restart application

### Problem: Application won't start after deleting database
**Cause:** Normal - database will be recreated
**Solution:** Just wait for it to initialize

---

## Window Maximization Test

**NEW FEATURE:** The window should stay maximized when changing scenes.

1. Start the application
2. Maximize the window (or it should auto-maximize)
3. Click any button (Apply for Visa, Login, etc.)
4. **VERIFY:** Window stays maximized
5. Navigate through multiple screens
6. **VERIFY:** Window remains maximized throughout

If the window shrinks back to default size, report this issue.

---

## What to Report Back

If the status update **STILL DOESN'T WORK**, please provide:

1. **Complete console output** from startup to after clicking approve
2. Screenshot of admin dashboard showing the status
3. Answer: Does the console say "Verified in DB - Status: Approved"?
4. Answer: Does clicking Refresh button update the table?
5. Answer: What happens when you login as applicant?

---

## Expected Success Indicators

✅ Console shows "Verified in DB - Status: Approved"
✅ Admin table shows "Approved" status with disabled buttons
✅ Statistics update to show 1 approved
✅ Applicant dashboard shows green "Approved" badge
✅ Visa duration and approval details visible
✅ Window stays maximized when changing scenes

If ALL of the above are true → **PROBLEM IS FIXED!** 🎉
