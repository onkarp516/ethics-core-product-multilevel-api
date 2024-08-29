package in.truethics.ethics.ethicsapiv10.model.master;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBarcode;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.*;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurChallanDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoiceDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurOrderDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurReturnDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoiceDetailsUnits;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "sub_category_tbl")
public class Subcategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subcategoryName;
    private Boolean status;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    @JsonManagedReference
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "outlet_id")
    @JsonManagedReference
    private Outlet outlet;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<Product> product;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<ProductBarcode> productBarcodes;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<ProductBatchNo> productBatchNos;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<ProductOpeningStocks> productOpeningStocks;

    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<ProductTaxDateMaster> productTaxDateMasters;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurInvoiceDetailsUnits> tranxPurInvoiceDetailsUnits;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesInvoiceDetailsUnits> tranxSalesInvoiceDetailsUnits;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurChallanDetailsUnits> tranxPurChallanDetailsUnits;


    private Long createdBy;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private Long updatedBy;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<ProductUnitPacking> productUnitPackings;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<LedgerTransactionPostings> ledgerTransactionPostings;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurReturnDetailsUnits> tranxPurReturnDetailsUnits;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurOrderDetailsUnits> tranxPurOrderDetailsUnits;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<InventoryDetailsPostings> inventoryDetailsPostings;

    /****** Modification after PK Visits at Solapur 25th to 30th January 2023 ******/
    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<Product> products;
}
