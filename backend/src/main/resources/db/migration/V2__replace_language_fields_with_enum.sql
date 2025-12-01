-- Delete all vocabulary cards and related data
DELETE FROM card_tags;
DELETE FROM review_history;
DELETE FROM vocabulary_cards;

-- Remove old columns
ALTER TABLE vocabulary_cards DROP COLUMN source_language;
ALTER TABLE vocabulary_cards DROP COLUMN target_language;

-- Add new enum column
ALTER TABLE vocabulary_cards ADD COLUMN language_selection VARCHAR(20);
