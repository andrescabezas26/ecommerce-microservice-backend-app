CREATE TABLE verification_tokens (
  verification_token_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  credential_id INT,
  verif_token VARCHAR(255),
  expire_date DATE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
);
