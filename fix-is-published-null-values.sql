-- Migration script to fix null values in is_published column
-- This handles the case where the column exists but has null values

DO $$
BEGIN
    -- First, check if the column exists
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
        
        RAISE NOTICE 'Updated null values in is_published column and set NOT NULL constraint';
    ELSE
        -- Column doesn't exist, create it
        ALTER TABLE public.review 
        ADD COLUMN is_published BOOLEAN NOT NULL DEFAULT false;
        
        RAISE NOTICE 'Added is_published column with NOT NULL constraint';
    END IF;
    
    -- Add comment for documentation
    COMMENT ON COLUMN public.review.is_published IS 'Indicates whether the review results are published to students';
    
END $$;
