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
@Table(name = "tranx_sales_invoice_details_units_tbl")
public class TranxSalesInvoiceDetailsUnits {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sales_invoice_id")
    @JsonManagedReference
    private TranxSalesInvoice salesInvoice;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonManagedReference
    private Product product;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    @JsonManagedReference
    private Units units;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    @JsonManagedReference
    private ProductBatchNo productBatchNo;

    @Column(name = "transaction_status_id")
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

    @Column(name = "level_a_id")
    private Long levelAId;

    @Column(name = "level_b_id")
    private Long levelBId;

    @Column(name = "level_c_id")
    private Long levelCId;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesInvoiceProductSrNumber> tranxSalesInvoiceProductSrNumbers;
    private Double returnQty; //maintain the quantity of the invoice while return the invoice


}
