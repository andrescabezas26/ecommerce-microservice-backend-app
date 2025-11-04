CREATE TABLE credentials (
  credential_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id INT,
  username VARCHAR(255),
  password VARCHAR(255),
  role VARCHAR(255),
  is_enabled BOOLEAN DEFAULT FALSE,
  is_account_non_expired BOOLEAN DEFAULT TRUE,
  is_account_non_locked BOOLEAN DEFAULT TRUE,
  is_credentials_non_expired BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
);
