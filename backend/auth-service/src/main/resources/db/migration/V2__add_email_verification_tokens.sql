ALTER TABLE customer_profiles
    ADD COLUMN email_verification_token_hash VARCHAR(64) NULL;

ALTER TABLE customer_profiles
    ADD COLUMN email_verification_token_expires_at TIMESTAMP(6) NULL;
