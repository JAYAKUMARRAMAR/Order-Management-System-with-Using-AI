CREATE TABLE customer_profiles (
    customer_id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_customer_profiles PRIMARY KEY (customer_id),
    CONSTRAINT uk_customer_profiles_username UNIQUE (username),
    CONSTRAINT uk_customer_profiles_email UNIQUE (email)
);

CREATE INDEX idx_customer_profiles_email_verified
    ON customer_profiles (email_verified);
