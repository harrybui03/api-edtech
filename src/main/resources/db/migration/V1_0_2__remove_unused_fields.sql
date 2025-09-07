-- This migration removes unused columns from the 'courses' and 'chapters' tables.
-- This migration removes unused columns and adds new fields to align with entity updates.

ALTER TABLE courses DROP COLUMN IF EXISTS upcoming;
ALTER TABLE courses DROP COLUMN IF EXISTS featured;
ALTER TABLE courses DROP COLUMN IF EXISTS disable_self_learning;
ALTER TABLE courses DROP COLUMN IF EXISTS paid_certificate;
ALTER TABLE courses DROP COLUMN IF EXISTS tags;

ALTER TABLE chapters DROP COLUMN IF EXISTS is_scorm_package;
ALTER TABLE chapters DROP COLUMN IF EXISTS scorm_package;
ALTER TABLE chapters DROP COLUMN IF EXISTS scorm_package_path;
ALTER TABLE chapters DROP COLUMN IF EXISTS manifest_file;
ALTER TABLE chapters DROP COLUMN IF EXISTS launch_file;

-- Add new fields to courses and chapters
ALTER TABLE courses ADD COLUMN IF NOT EXISTS selling_price DECIMAL(10, 2);
ALTER TABLE courses ADD COLUMN IF NOT EXISTS language VARCHAR(50);
ALTER TABLE courses ADD COLUMN IF NOT EXISTS slug VARCHAR(255);

ALTER TABLE chapters ADD COLUMN IF NOT EXISTS summary TEXT;

ALTER TABLE lessons ADD COLUMN IF NOT EXISTS slug VARCHAR(255);
ALTER TABLE lessons ADD COLUMN IF NOT EXISTS "position" INTEGER;

