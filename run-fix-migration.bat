@echo off
echo Applying database fix for is_published column...

REM You can modify these connection details as needed
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=devlabs
set DB_USER=postgres

echo Enter PostgreSQL password for user %DB_USER%:
set /p DB_PASSWORD=

REM Run the migration SQL
psql -h %DB_HOST% -p %DB_PORT% -d %DB_NAME% -U %DB_USER% -f fix-is-published-null-values.sql

if %ERRORLEVEL% equ 0 (
    echo ✅ Database migration completed successfully!
    echo You can now restart your application and test the endpoints.
) else (
    echo ❌ Database migration failed. Please check your connection details and try again.
    echo Alternative: Copy the SQL from fix-is-published-null-values.sql and run it manually in your database client.
)

pause
