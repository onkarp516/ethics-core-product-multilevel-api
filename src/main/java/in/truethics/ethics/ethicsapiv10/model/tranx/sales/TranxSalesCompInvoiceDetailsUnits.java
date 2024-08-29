package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_sales_comp_details_units_tbl")
public class TranxSalesCompInvoiceDetailsUnits {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long salesInvoiceId;

    private Long salesInvoiceDetailsId;

    private Long productId;

    private Long unitsId;

    private Long packingMasterId;

    private Long flavourMasterId;

    private Long productBatchNoId;
    private Long brandId;

    private Long groupId;

    private Long categoryId;

    private Long subcategoryId;
    private Long transactionStatusId;


    private Double unitConversions;
    private Double qty;
    private Double rate;
    private Double baseAmt; // rate * qty
    private Double totalAmount; // total_amt OR taxable_amt
    private Double discountAmount; // dis_amt
    private Double discountPer; // dis_per
    private Double discountAmountCal;
    private Double discountPerCal;
    private Double igst; // tax_per
    private Double sgst;
    private Double cgst;
    private Double totalIgst; // tax_amount
    private Double totalSgst;
    private Double totalCgst;
    private Double finalAmount; // net_amount
    @CreationTimestamp
    private LocalDateTime createdAt;
    private Long createdBy;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private Long updatedBy;
    private Boolean status;

    /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
    private Double freeQty;
    private Double discountBInPer; // dis_per2
    private Double totalDiscountInAmt; // row_dis_amt
    private Double grossAmt; // gross_amt
    private Double additionChargesAmt; // add_chg_amt
    private Double grossAmt1; // gross_amt1 = gross_amt - add_chg_amt
    private Double invoiceDisAmt; // invoice_dis_amt

    private Long levelAId;

    private Long levelBId;

    private Long levelCId;

    private Double returnQty; //maintain the quantity of the invoice while return the invoice

}
