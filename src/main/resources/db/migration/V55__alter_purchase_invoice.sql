 ALTER TABLE  tranx_purchase_invoice_tbl
 ADD COLUMN payment_mode VARCHAR(255) NULL;

 ALTER TABLE tranx_purchase_challan_additional_charges_tbl
 ADD percent DOUBLE NULL;


 ALTER TABLE tranx_sales_challan_additional_charges_tbl
 ADD percent DOUBLE NULL;