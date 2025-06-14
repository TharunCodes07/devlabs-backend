-- Migration script to add is_published column to review table
-- This script checks if the column exists before adding it

DO $$
BEGIN
    -- Check if the column already exists
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'review' 
        AND column_name = 'is_published'
        AND table_schema = 'public'
    ) THEN
        -- Add the column if it doesn't exist
        ALTER TABLE public.review ADD COLUMN is_published BOOLEAN NOT NULL DEFAULT false;
        
        -- Add comment for documentation
        COMMENT ON COLUMN public.review.is_published IS 'Indicates whether the review results are published to students';
        
        RAISE NOTICE 'Column is_published added to review table successfully';
    ELSE
        RAISE NOTICE 'Column is_published already exists in review table';
    END IF;
END
$$;
