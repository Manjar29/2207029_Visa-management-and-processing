# IMPORTANT: Delete Old Database Before Running

## Changes Made:
1. ✅ Application ID format: **VSA** + **7 numbers** (e.g., VSA1234567)
2. ✅ Password format: **4 numbers** + **2 letters** (e.g., 1234Ab)
3. ✅ Date pickers: **Cannot select past dates** (journey, appointment, program start, employment start)
4. ✅ Status updates: Work correctly in both admin and applicant dashboards

## BEFORE RUNNING THE APP:

**YOU MUST DELETE THE OLD DATABASE FILE:**

1. Close the application if it's running
2. Go to your project folder: `C:\Users\Acer\IdeaProjects\2207029_Visa-management-and-processing`
3. **Delete the file:** `visadb.db`
4. Run the application again

The old database has the old format (VSA-2025-XXXXX). The new database will use the new format (VSAXXXXXXX).

## Testing:

1. **Delete `visadb.db`** 
2. Run the app
3. Apply for a visa → Get new format credentials (VSA1234567 / 1234Ab)
4. Login as admin: `admin_india` / `india123`
5. Approve the application with duration
6. Login as applicant with the new credentials
7. See approved status with visa duration and conditions

## How Status Updates Work:

**Admin Dashboard:**
- When admin approves/rejects → Table refreshes automatically
- Statistics update immediately

**Applicant Dashboard:**
- Click "Refresh Status" button to see latest status
- Or logout and login again

## Date Picker Fix:
All date pickers (journey date, appointment date, etc.) now prevent selecting past dates - only today and future dates can be selected.
