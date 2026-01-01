@echo off
echo ====================================================
echo VISA MANAGEMENT SYSTEM - RESET AND REBUILD
echo ====================================================
echo.

echo Step 1: Deleting old database files...
del /F /Q visadb.db 2>nul
del /F /Q visadb.db-journal 2>nul
del /F /Q visadb.db-wal 2>nul
del /F /Q visadb.db-shm 2>nul
echo   [DONE] Database files deleted
echo.

echo Step 2: Cleaning Maven build...
call mvn clean
echo   [DONE] Maven clean completed
echo.

echo Step 3: Compiling project...
call mvn compile
echo   [DONE] Compilation completed
echo.

echo Step 4: Packaging application...
call mvn package
echo   [DONE] Package created
echo.

echo ====================================================
echo READY TO TEST!
echo ====================================================
echo.
echo Next steps:
echo 1. Run the application (mvn javafx:run)
echo 2. Watch the console for database path output
echo 3. Apply for a visa and save credentials
echo 4. Login as admin and approve the application
echo 5. Check if status updates in the table
echo.
echo Press any key to run the application...
pause >nul

echo.
echo Starting application...
call mvn javafx:run
