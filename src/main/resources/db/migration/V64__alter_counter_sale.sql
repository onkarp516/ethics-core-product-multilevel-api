ALTER TABLE tranx_counter_sales_details_units_tbl
ADD COLUMN transaction_status BIGINT NULL,
ADD COLUMN igst DOUBLE NULL,
ADD COLUMN sgst DOUBLE NULL,
ADD COLUMN cgst DOUBLE NULL,
ADD COLUMN total_igst DOUBLE NULL,
ADD COLUMN total_sgst DOUBLE NULL,
ADD COLUMN total_cgst DOUBLE NULL;

ALTER TABLE tranx_sales_comp_invoice_tbl
ADD COLUMN doctor_id BIGINT NULL,
ADD COLUMN client_name VARCHAR(255) NULL,
ADD COLUMN client_address VARCHAR(255) NULL,
ADD COLUMN mobile_number VARCHAR(255) NULL,
ADD COLUMN transaction_tracking_no VARCHAR(255) NULL,
ADD COLUMN patient_name VARCHAR(255) NULL,
ADD COLUMN doctor_address VARCHAR(255) NULL,
ADD COLUMN image_upload VARCHAR(255) NULL;