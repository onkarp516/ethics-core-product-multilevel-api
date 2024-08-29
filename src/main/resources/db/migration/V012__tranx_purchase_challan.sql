SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS tranx_purchase_challan_tbl;
CREATE TABLE tranx_purchase_challan_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   sundry_creditors_id BIGINT NULL,
   purchase_account_ledger_id BIGINT NULL,
   purchase_roundoff_id BIGINT NULL,
   transaction_status_id BIGINT NOT NULL,
   fiscal_year_id BIGINT NULL,
   purchase_discount_ledger_id BIGINT NULL,
   pur_challan_srno BIGINT NULL,
   vendor_invoice_no VARCHAR(255) NULL,
   order_reference VARCHAR(255) NULL,
   reference_type VARCHAR(255) NULL,
   transaction_date date NULL,
   invoice_date datetime NULL,
   transport_name VARCHAR(255) NULL,
   reference VARCHAR(255) NULL,
   round_off DOUBLE NULL,
   total_base_amount DOUBLE NULL,
   purchase_discount_amount DOUBLE NULL,
   purchase_discount_per DOUBLE NULL,
   total_purchase_discount_amt DOUBLE NULL,
   additional_charges_total DOUBLE NULL,
   total_amount DOUBLE NULL,
   totalcgst DOUBLE NULL,
   totalqty BIGINT NULL,
   totalsgst DOUBLE NULL,
   totaligst DOUBLE NULL,
   taxable_amount DOUBLE NULL,
   tcs DOUBLE NULL,
   status BIT(1) NULL,
   financial_year VARCHAR(255) NULL,
   narration VARCHAR(255) NULL,
   operations VARCHAR(255) NULL,
   gst_number VARCHAR(255) NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_by BIGINT NULL,
   updated_at datetime NULL,
   additional_ledger_id1 BIGINT NULL,
   additional_ledger_id2 BIGINT NULL,
   additional_ledger_id3 BIGINT NULL,
   addition_ledger_amt1 DOUBLE NULL,
   addition_ledger_amt2 DOUBLE NULL,
   addition_ledger_amt3 DOUBLE NULL,
   free_qty DOUBLE NULL,
   gross_amount DOUBLE NULL,
   total_tax DOUBLE NULL,
   is_round_off BIT(1) NULL,
   CONSTRAINT pk_tranx_purchase_challan_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_purchase_challan_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_TBL_ON_ADDITIONAL_LEDGER_ID1 FOREIGN KEY (additional_ledger_id1) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_purchase_challan_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_TBL_ON_ADDITIONAL_LEDGER_ID2 FOREIGN KEY (additional_ledger_id2) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_purchase_challan_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_TBL_ON_ADDITIONAL_LEDGER_ID3 FOREIGN KEY (additional_ledger_id3) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_purchase_challan_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tranx_purchase_challan_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_TBL_ON_FISCAL_YEAR FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_year_tbl (id);

ALTER TABLE tranx_purchase_challan_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE tranx_purchase_challan_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_TBL_ON_PURCHASE_ACCOUNT_LEDGER FOREIGN KEY (purchase_account_ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_purchase_challan_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_TBL_ON_PURCHASE_DISCOUNT_LEDGER FOREIGN KEY (purchase_discount_ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_purchase_challan_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_TBL_ON_PURCHASE_ROUNDOFF FOREIGN KEY (purchase_roundoff_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_purchase_challan_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_TBL_ON_SUNDRY_CREDITORS FOREIGN KEY (sundry_creditors_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_purchase_challan_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_TBL_ON_TRANSACTION_STATUS FOREIGN KEY (transaction_status_id) REFERENCES transaction_status_tbl (id);


DROP TABLE if EXISTS tranx_purchase_challan_details_tbl;
CREATE TABLE tranx_purchase_challan_details_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   tranx_pur_challan_id BIGINT NULL,
   product_id BIGINT NULL,
   transaction_type_id BIGINT NULL,
   packaging_id BIGINT NULL,
   base_amt DOUBLE NULL,
   order_reference VARCHAR(255) NULL,
   is_purchase_order BIT(1) NULL,
   purchase_order_id VARCHAR(255) NULL,
   total_amount DOUBLE NULL,
   discount_amount DOUBLE NULL,
   discount_per DOUBLE NULL,
   discount_amount_cal DOUBLE NULL,
   discount_per_cal DOUBLE NULL,
   igst DOUBLE NULL,
   sgst DOUBLE NULL,
   cgst DOUBLE NULL,
   total_igst DOUBLE NULL,
   total_sgst DOUBLE NULL,
   total_cgst DOUBLE NULL,
   final_amount DOUBLE NULL,
   qty_high DOUBLE NULL,
   rate_high DOUBLE NULL,
   qty_medium DOUBLE NULL,
   rate_medium DOUBLE NULL,
   qty_low DOUBLE NULL,
   rate_low DOUBLE NULL,
   base_amt_high DOUBLE NULL,
   base_amt_low DOUBLE NULL,
   base_amt_medium DOUBLE NULL,
   status BIT(1) NULL,
   operations VARCHAR(255) NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_by BIGINT NULL,
   updated_at datetime NULL,
   CONSTRAINT pk_tranx_purchase_challan_details_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_purchase_challan_details_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_DETAILS_TBL_ON_PACKAGING FOREIGN KEY (packaging_id) REFERENCES packing_master_tbl (id);

ALTER TABLE tranx_purchase_challan_details_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_DETAILS_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);

ALTER TABLE tranx_purchase_challan_details_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_DETAILS_TBL_ON_TRANSACTION_TYPE FOREIGN KEY (transaction_type_id) REFERENCES transaction_type_master_tbl (id);

ALTER TABLE tranx_purchase_challan_details_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_DETAILS_TBL_ON_TRANX_PUR_CHALLAN FOREIGN KEY (tranx_pur_challan_id) REFERENCES tranx_purchase_challan_tbl (id);

DROP TABLE if EXISTS tranx_purchase_challan_duties_taxes_tbl;
CREATE TABLE tranx_purchase_challan_duties_taxes_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
  tranx_pur_challan_id BIGINT NOT NULL,
  sundry_creditors_id BIGINT NOT NULL,
  duties_taxes_ledger_id BIGINT NOT NULL,
  intra BIT(1) NULL,
  amount DOUBLE NULL,
  status BIT(1) NULL,
  created_by BIGINT NULL,
  created_at datetime NULL,
  updated_by BIGINT NULL,
  updated_at datetime NULL,
  CONSTRAINT pk_tranx_purchase_challan_duties_taxes_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_purchase_challan_duties_taxes_tbl ADD CONSTRAINT FK_TRANXPURCHASECHALLANDUTIESTAXESTBL_ON_DUTIESTAXESLEDGER FOREIGN KEY (duties_taxes_ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_purchase_challan_duties_taxes_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_DUTIES_TAXES_TBL_ON_SUNDRY_CREDITORS FOREIGN KEY (sundry_creditors_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_purchase_challan_duties_taxes_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_DUTIES_TAXES_TBL_ON_TRANX_PUR_CHALLAN FOREIGN KEY (tranx_pur_challan_id) REFERENCES tranx_purchase_challan_tbl (id);

DROP TABLE if EXISTS tranx_purchase_challan_details_units_tbl;
CREATE TABLE tranx_purchase_challan_details_units_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   purchase_challan_id BIGINT NULL,
   purchase_challan_details_id BIGINT NULL,
   product_id BIGINT NULL,
   unit_id BIGINT NULL,
   packaging_id BIGINT NULL,
   flavour_master_id BIGINT NULL,
   batch_id BIGINT NULL,
   brand_id BIGINT NULL,
   group_id BIGINT NULL,
   category_id BIGINT NULL,
   subcategory_id BIGINT NULL,
   unit_conversions DOUBLE NULL,
   qty DOUBLE NULL,
   rate DOUBLE NULL,
   base_amt DOUBLE NULL,
   total_amount DOUBLE NULL,
   discount_amount DOUBLE NULL,
   discount_per DOUBLE NULL,
   discount_amount_cal DOUBLE NULL,
   discount_per_cal DOUBLE NULL,
   igst DOUBLE NULL,
   sgst DOUBLE NULL,
   cgst DOUBLE NULL,
   total_igst DOUBLE NULL,
   total_sgst DOUBLE NULL,
   total_cgst DOUBLE NULL,
   final_amount DOUBLE NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   status BIT(1) NULL,
   free_qty DOUBLE NULL,
   discountbin_per DOUBLE NULL,
   total_discount_in_amt DOUBLE NULL,
   gross_amt DOUBLE NULL,
   addition_charges_amt DOUBLE NULL,
   gross_amt1 DOUBLE NULL,
   invoice_dis_amt DOUBLE NULL,
   level_a_id BIGINT NULL,
   level_b_id BIGINT NULL,
   level_c_id BIGINT NULL,
   transaction_status BIGINT NULL,
   CONSTRAINT pk_tranx_purchase_challan_details_units_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_purchase_challan_details_units_tbl ADD CONSTRAINT FK_TRANXPURCHASECHALLANDETAILSUNITSTB_ON_PURCHASECHALLANDETAILS FOREIGN KEY (purchase_challan_details_id) REFERENCES tranx_purchase_challan_details_tbl (id);

ALTER TABLE tranx_purchase_challan_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_DETAILS_UNITS_TBL_ON_BATCH FOREIGN KEY (batch_id) REFERENCES product_batchno_tbl (id);

ALTER TABLE tranx_purchase_challan_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_DETAILS_UNITS_TBL_ON_BRAND FOREIGN KEY (brand_id) REFERENCES brand_tbl (id);

ALTER TABLE tranx_purchase_challan_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_DETAILS_UNITS_TBL_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES category_tbl (id);

ALTER TABLE tranx_purchase_challan_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_DETAILS_UNITS_TBL_ON_FLAVOUR_MASTER FOREIGN KEY (flavour_master_id) REFERENCES flavour_master_tbl (id);

ALTER TABLE tranx_purchase_challan_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_DETAILS_UNITS_TBL_ON_GROUP FOREIGN KEY (group_id) REFERENCES group_tbl (id);

ALTER TABLE tranx_purchase_challan_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_DETAILS_UNITS_TBL_ON_LEVEL_A FOREIGN KEY (level_a_id) REFERENCES level_a_tbl (id);

ALTER TABLE tranx_purchase_challan_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_DETAILS_UNITS_TBL_ON_LEVEL_B FOREIGN KEY (level_b_id) REFERENCES level_b_tbl (id);

ALTER TABLE tranx_purchase_challan_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_DETAILS_UNITS_TBL_ON_LEVEL_C FOREIGN KEY (level_c_id) REFERENCES level_c_tbl (id);

ALTER TABLE tranx_purchase_challan_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_DETAILS_UNITS_TBL_ON_PACKAGING FOREIGN KEY (packaging_id) REFERENCES packing_master_tbl (id);

ALTER TABLE tranx_purchase_challan_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_DETAILS_UNITS_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);

ALTER TABLE tranx_purchase_challan_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_DETAILS_UNITS_TBL_ON_PURCHASE_CHALLAN FOREIGN KEY (purchase_challan_id) REFERENCES tranx_purchase_challan_tbl (id);

ALTER TABLE tranx_purchase_challan_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_DETAILS_UNITS_TBL_ON_SUBCATEGORY FOREIGN KEY (subcategory_id) REFERENCES sub_category_tbl (id);

ALTER TABLE tranx_purchase_challan_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_DETAILS_UNITS_TBL_ON_UNIT FOREIGN KEY (unit_id) REFERENCES units_tbl (id);

DROP TABLE if EXISTS tranx_purchase_challan_additional_charges_tbl;
CREATE TABLE tranx_purchase_challan_additional_charges_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   purchase_challan_id BIGINT NULL,
   additional_charges_id BIGINT NULL,
   amount DOUBLE NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   status BIT(1) NULL,
   operation VARCHAR(255) NULL,
   CONSTRAINT pk_tranx_purchase_challan_additional_charges_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_purchase_challan_additional_charges_tbl ADD CONSTRAINT FK_TRANXPURCHASECHALLANADDITIONALCHARGESTBL_ON_PURCHASECHALLAN FOREIGN KEY (purchase_challan_id) REFERENCES tranx_purchase_challan_tbl (id);

ALTER TABLE tranx_purchase_challan_additional_charges_tbl ADD CONSTRAINT FK_TRANXPURCHASECHALLANADDITIONALCHARGESTB_ON_ADDITIONALCHARGES FOREIGN KEY (additional_charges_id) REFERENCES ledger_master_tbl (id);

DROP TABLE if EXISTS tranx_purchase_challan_product_sr_no_tbl;
CREATE TABLE tranx_purchase_challan_product_sr_no_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NOT NULL,
   product_id BIGINT NOT NULL,
   transaction_type_master_id BIGINT NULL,
   serial_no VARCHAR(255) NULL,
   purchase_created_at datetime NULL,
   sale_created_at datetime NULL,
   transaction_status VARCHAR(255) NULL,
   operations VARCHAR(255) NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_by BIGINT NULL,
   updated_at datetime NULL,
   status BIT(1) NULL,
   level_a_id BIGINT NULL,
   level_b_id BIGINT NULL,
   level_c_id BIGINT NULL,
   pur_challan_unit_details_id BIGINT NULL,
   units_id BIGINT NULL,
   CONSTRAINT pk_tranx_purchase_challan_product_sr_no_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_purchase_challan_product_sr_no_tbl ADD CONSTRAINT FK_TRANXPURCHASECHALLANPRODUCTSRNOTBL_ON_PURCHALLANUNITDETAILS FOREIGN KEY (pur_challan_unit_details_id) REFERENCES tranx_purchase_challan_details_units_tbl (id);

ALTER TABLE tranx_purchase_challan_product_sr_no_tbl ADD CONSTRAINT FK_TRANXPURCHASECHALLANPRODUCTSRNOTBL_ON_TRANSACTIONTYPEMASTER FOREIGN KEY (transaction_type_master_id) REFERENCES transaction_type_master_tbl (id);

ALTER TABLE tranx_purchase_challan_product_sr_no_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_PRODUCT_SR_NO_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tranx_purchase_challan_product_sr_no_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_PRODUCT_SR_NO_TBL_ON_LEVEL_A FOREIGN KEY (level_a_id) REFERENCES level_a_tbl (id);

ALTER TABLE tranx_purchase_challan_product_sr_no_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_PRODUCT_SR_NO_TBL_ON_LEVEL_B FOREIGN KEY (level_b_id) REFERENCES level_b_tbl (id);

ALTER TABLE tranx_purchase_challan_product_sr_no_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_PRODUCT_SR_NO_TBL_ON_LEVEL_C FOREIGN KEY (level_c_id) REFERENCES level_c_tbl (id);

ALTER TABLE tranx_purchase_challan_product_sr_no_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_PRODUCT_SR_NO_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE tranx_purchase_challan_product_sr_no_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_PRODUCT_SR_NO_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);

ALTER TABLE tranx_purchase_challan_product_sr_no_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_CHALLAN_PRODUCT_SR_NO_TBL_ON_UNITS FOREIGN KEY (units_id) REFERENCES units_tbl (id);
SET FOREIGN_KEY_CHECKS = 1;




