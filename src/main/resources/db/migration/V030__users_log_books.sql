SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS user_log_books_tbl;
CREATE TABLE user_log_books_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   status BIT(1) NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   ledger_master_id BIGINT NULL,
   products VARCHAR(255) NULL,
   users_id BIGINT NULL,
   log_type VARCHAR(255) NULL,
   tranx_id BIGINT NULL,
   voucher_type VARCHAR(255) NULL,
   voucher_no VARCHAR(255) NULL,
   tranx_date datetime NULL,
   invoice_date datetime NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   tranx_value DOUBLE NULL,
   modify_value DOUBLE NULL,
   difference DOUBLE NULL,
   pc_name VARCHAR(255) NULL,
   CONSTRAINT pk_user_log_books_tbl PRIMARY KEY (id)
);

ALTER TABLE user_log_books_tbl ADD CONSTRAINT FK_USER_LOG_BOOKS_TBL_ON_LEDGER_MASTER FOREIGN KEY (ledger_master_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE user_log_books_tbl ADD CONSTRAINT FK_USER_LOG_BOOKS_TBL_ON_USERS FOREIGN KEY (users_id) REFERENCES users_tbl (id);
SET FOREIGN_KEY_CHECKS = 1;