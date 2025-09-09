-- This migration adds a 'position' column to the 'chapters' table for explicit ordering.
ALTER TABLE chapters ADD COLUMN IF NOT EXISTS "position" INTEGER;