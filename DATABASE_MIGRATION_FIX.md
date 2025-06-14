# Database Migration Fix for DevLabs Backend

## Problem

Two related issues:

1. The error `column r1_1.is_published does not exist` indicates that the `is_published` column was not added to the `review` table.
2. The error `Null value was assigned to a property [class com.devlabs.devlabsbackend.review.domain.Review.isPublished]` indicates that existing records have null values in the `is_published` column.

## Quick Fix Options

### Option 1: Automatic Migration (Recommended)

The `DatabaseMigrationRunner` class has been updated to automatically:

- Add the missing column if it doesn't exist
- Fix null values in existing records
- Set proper constraints and defaults

1. **Restart your application**:

   ```bash
   cd /d/Coding/Devlabs/devlabs-backend
   ./gradlew bootRun
   ```

2. **Check the logs** for messages like:
   - "Successfully added 'is_published' column to 'review' table"
   - "Updated X records: set is_published = false for null values"
   - "Set NOT NULL constraint on is_published column"

### Option 2: Manual Database Migration

If Option 1 doesn't work, manually execute this comprehensive SQL:

```sql
-- Fix is_published column issues
DO $$
BEGIN
    -- Check if column exists
    IF EXISTS (
        SELECT column_name
        FROM information_schema.columns
        WHERE table_name = 'review'
        AND column_name = 'is_published'
        AND table_schema = 'public'
    ) THEN
        -- Column exists, update null values to false
        UPDATE public.review
        SET is_published = false
        WHERE is_published IS NULL;

        -- Ensure the column has NOT NULL constraint
        ALTER TABLE public.review
        ALTER COLUMN is_published SET NOT NULL;

        -- Set default value for future records
        ALTER TABLE public.review
        ALTER COLUMN is_published SET DEFAULT false;

        RAISE NOTICE 'Updated null values in is_published column and set constraints';
    ELSE
        -- Column doesn't exist, create it
        ALTER TABLE public.review
        ADD COLUMN is_published BOOLEAN NOT NULL DEFAULT false;

        RAISE NOTICE 'Added is_published column with proper constraints';
    END IF;

    -- Add comment for documentation
    COMMENT ON COLUMN public.review.is_published IS 'Indicates whether the review results are published to students';

END $$;
```

### Option 3: Force Schema Recreation (Use with caution - will lose data)

1. **Backup your data first**
2. **Temporarily change** `application-dev.properties`:
   ```properties
   spring.jpa.hibernate.ddl-auto=create-drop
   ```
3. **Run the application** (this will recreate all tables)
4. **Change back** to:
   ```properties
   spring.jpa.hibernate.ddl-auto=update
   ```

## Verification

After applying the fix, test both API endpoints that were failing:

1. **Test projects by course endpoint**:

   ```
   GET http://localhost:8090/projects/course/{courseId}
   ```

2. **Test project reviews endpoint**:
   ```
   GET http://localhost:8090/projects/{projectId}/reviews
   ```

Both should now work without errors.

## Files Modified

1. `Review.kt` - Already has proper `@Column(name = "is_published")` annotation
2. `DatabaseMigrationRunner.kt` - Enhanced to handle both missing column and null values
3. `fix-is-published-null-values.sql` - Comprehensive manual migration script

## Root Cause

The issue occurred because:

1. The `is_published` column was added to the database but without proper NOT NULL constraint
2. Existing records had null values in this column
3. The JPA entity expected a non-null Boolean value

The enhanced migration runner now ensures:

- Column exists with proper constraints
- No null values remain in existing data
- Future records have proper defaults
