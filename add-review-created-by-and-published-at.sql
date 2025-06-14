-- Migration script to add createdBy and publishedAt fields to review table
-- Run this script to update the existing review table structure

-- Add the new columns (allow NULL initially for existing reviews)
ALTER TABLE review 
ADD COLUMN created_by_id UUID NULL,
ADD COLUMN published_at TIMESTAMP NULL;

-- Add foreign key constraint for created_by_id
ALTER TABLE review 
ADD CONSTRAINT fk_review_created_by 
FOREIGN KEY (created_by_id) REFERENCES "user"(id);

-- Create indexes for better performance on queries filtering by creator
CREATE INDEX idx_review_created_by ON review(created_by_id);
CREATE INDEX idx_review_published_at ON review(published_at);

-- OPTIONAL: Set a default creator for existing reviews
-- You can run this to assign all existing reviews to a specific admin/manager
-- Replace 'your-admin-user-uuid-here' with an actual admin or manager user UUID from your system

-- Example queries to find admin users:
-- SELECT id, name, email, role FROM "user" WHERE role = 'ADMIN' OR role = 'MANAGER';

-- Then update existing reviews:
-- UPDATE review SET created_by_id = 'your-admin-user-uuid-here' WHERE created_by_id IS NULL;

-- IMPORTANT: After setting default values, you can make the field NOT NULL if desired:
-- ALTER TABLE review ALTER COLUMN created_by_id SET NOT NULL;

-- Note: The application now handles NULL created_by gracefully, so this is optional.
