-- Migration to add course_id column to individual_score table
-- This script adds course-specific evaluation support to the individual score system

-- Add course_id column to individual_score table
ALTER TABLE individual_score 
ADD COLUMN course_id UUID;

-- Add foreign key constraint to course table
ALTER TABLE individual_score 
ADD CONSTRAINT fk_individual_score_course 
FOREIGN KEY (course_id) REFERENCES course(id);

-- Create index for better query performance
CREATE INDEX idx_individual_score_course_id ON individual_score(course_id);

-- Create composite index for the new course-specific queries
CREATE INDEX idx_individual_score_participant_review_project_course 
ON individual_score(participant_id, review_id, project_id, course_id);

-- Create index for finding scores by review, project, and course
CREATE INDEX idx_individual_score_review_project_course 
ON individual_score(review_id, project_id, course_id);

-- Comments explaining the changes
COMMENT ON COLUMN individual_score.course_id IS 'Course-specific evaluation support - allows faculty to evaluate projects per course they teach';

-- Note: The course_id column is nullable to maintain backward compatibility
-- Existing scores without course association will have NULL course_id
-- New course-specific evaluations will have the course_id populated
