package in.truethics.ethics.ethicsapiv10.model.tranx.purchase;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_purchase_order_tbl")
public class TranxPurOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "outlet_id")
    private Outlet outlet;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "sundry_creditors_id")
    private LedgerMaster sundryCreditors;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "purchase_account_ledger_id")
    private LedgerMaster purchaseAccountLedger;



    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "purchase_roundoff_id")
    private LedgerMaster purchaseRoundOff;

    @ManyToOne
    @JoinColumn(name = "fiscal_year_id")
    @JsonManagedReference
    private FiscalYear fiscalYear;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "transaction_status_id", nullable = false)
    private TransactionStatus transactionStatus;

    @JsonBackReference
    @OneToMany
    private List<TranxPurOrderDetails> purchaseOrderDetails;

    @JsonBackReference
    @OneToMany
    private List<TranxPurOrderDutiesTaxes> tranxPurOrderDutiesTaxes;

    @JsonBackReference
    @OneToMany
    private List<TranxPurOrderDetailsUnits> tranxPurOrderDetailsUnits;

    private Long purOrdSrno;
    private String vendorInvoiceNo;// purchase Order number
    private String orderReference;
    private LocalDate transactionDate;
    private Date invoiceDate;//purchase order date
    private String transportName;
    private String reference;
    private Double roundOff;
    private Double totalBaseAmount;  //qty*base_amount
    private Double totalAmount;
    private Double totalcgst;
    private Long totalqty;
    private Double totalsgst;
    private Double totaligst;
    private Double taxableAmount;
    private Double tcs;
    private Boolean status;
    private String financialYear;
    private String narration;
    private String operations;
    private Boolean isChallanConverted;
    private Long createdBy;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private Long updatedBy;
    /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
    @ManyToOne
    @JoinColumn(name = "ledger_id")
    @JsonManagedReference
    private LedgerMaster ledgerMaster;
    private Double ledgerAmt;
    private Double freeQty;
    private Double purchaseDiscountAmount; // purchase_discount
    private Double purchaseDiscountPer; // purchase_discount_amt
    private Double totalPurchaseDiscountAmt; // discount
    private Double grossAmount; // gross total
    private Double totalTax; // tax
    private String gstNumber;//gstNo;
    private String tranxCode;//Transaction unique code of each transaction performed

}
