-- Rename or create join tables with correct names
-- First, back up existing data if tables exist

-- For course_review table
CREATE TABLE IF NOT EXISTS course_review_temp (
    course_id UUID NOT NULL,
    review_id UUID NOT NULL,
    PRIMARY KEY (course_id, review_id),
    CONSTRAINT fk_course_review_course FOREIGN KEY (course_id) REFERENCES course(id),
    CONSTRAINT fk_course_review_review FOREIGN KEY (review_id) REFERENCES review(id)
);

-- Copy data from old table if it exists
INSERT INTO course_review_temp (course_id, review_id)
SELECT course_id, review_id FROM course_review
ON CONFLICT DO NOTHING;

-- For review_project table
CREATE TABLE IF NOT EXISTS review_project_temp (
    review_id UUID NOT NULL,
    project_id UUID NOT NULL,
    PRIMARY KEY (review_id, project_id),
    CONSTRAINT fk_review_project_review FOREIGN KEY (review_id) REFERENCES review(id),
    CONSTRAINT fk_review_project_project FOREIGN KEY (project_id) REFERENCES project(id)
);

-- Copy data from old table if it exists
INSERT INTO review_project_temp (review_id, project_id)
SELECT review_id, project_id FROM review_projects
ON CONFLICT DO NOTHING;

-- Drop old tables if they exist
DROP TABLE IF EXISTS course_review;
DROP TABLE IF EXISTS review_projects;

-- Rename temp tables to final names
ALTER TABLE IF EXISTS course_review_temp RENAME TO course_review;
ALTER TABLE IF EXISTS review_project_temp RENAME TO review_project;

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_course_review_course_id ON course_review(course_id);
CREATE INDEX IF NOT EXISTS idx_course_review_review_id ON course_review(review_id);
CREATE INDEX IF NOT EXISTS idx_review_project_review_id ON review_project(review_id);
CREATE INDEX IF NOT EXISTS idx_review_project_project_id ON review_project(project_id);
