package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.PackingMaster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_sales_order_details_tbl")
public class
TranxSalesOrderDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sales_order_invoice_id")
    @JsonManagedReference
    private TranxSalesOrder salesTransaction;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonManagedReference
    private Product product;

    @ManyToOne
    @JoinColumn(name = "packaging_id")
    @JsonManagedReference
    private PackingMaster packingMaster;

    //private Double qty;
    private Double base_amt;
    //private Double rate;
    private Double totalAmount;
    private Double discountAmount;
    private String referenceId;
    private String referenceType;
    private Double discountPer;
    private Double discountAmountCal;
    private Double discountPerCal;
    private Double igst;
    private Double sgst;
    private Double cgst;
    private Double totalIgst;
    private Double totalSgst;
    private Double totalCgst;
    private Double finalAmount;
    private Double qtyHigh;
    private Double rateHigh;
    private Double qtyMedium;
    private Double rateMedium;
    private Double qtyLow;
    private Double rateLow;
    private Double baseAmtHigh;
    private Double baseAmtLow;
    private Double baseAmtMedium;
    private Boolean status;
    private String operations;
    @CreationTimestamp
    private LocalDateTime createdDate;
    private Long createdBy;
    @UpdateTimestamp
    private LocalDateTime updatedDate;
    private Long updatedBy;
}