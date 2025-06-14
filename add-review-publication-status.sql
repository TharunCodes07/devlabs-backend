-- Add isPublished column to review table
ALTER TABLE review ADD COLUMN is_published BOOLEAN NOT NULL DEFAULT false;
