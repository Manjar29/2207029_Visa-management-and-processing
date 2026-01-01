@echo off
echo ========================================
echo    VISA DATABASE RESET SCRIPT
echo ========================================
echo.
echo This will delete the existing database file
echo and create a fresh database when you run the app.
echo.
echo Press CTRL+C to cancel, or
pause

if exist visadb.db (
    del visadb.db
    echo.
    echo ✓ Database file deleted successfully!
    echo.
    echo The database will be recreated with:
    echo - New Application ID format: VSA1234567
    echo - New Password format: 1234Ab
    echo - Fresh admin accounts
    echo.
) else (
    echo.
    echo Database file not found. It will be created when you run the app.
    echo.
)

echo Now you can run the application.
pause
