package in.truethics.ethics.ethicsapiv10.model.barcode;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.inventory.InventoryDetailsPostings;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductOpeningStocks;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductTaxDateMaster;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurChallanDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoiceDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurReturnDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxCounterSalesDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesChallanDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoiceDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesReturnDetailsUnits;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product_batchno_tbl")
public class ProductBatchNo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String batchNo;//
    private String serialNo;
    private Double mrp;//
    private Boolean status;
    private Integer qnty;
    private Double salesRate; // from rate_a, rate_b, rate_c depends on creditor
    private Double purchaseRate;//
    private LocalDate expiryDate;//
    private Double minRateA;
    private Double minRateB;
    private Double minRateC;
    private Double maxDiscount;
    private Double minDiscount;
    private Double minMargin;
    private LocalDate manufacturingDate;
    private Long createdBy;
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    @JsonManagedReference
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "outlet_id")
    @JsonManagedReference
    private Outlet outlet;

    @ManyToOne
    @JoinColumn(name = "fiscal_year_id")
    @JsonManagedReference
    private FiscalYear fiscalYear;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonManagedReference
    private Product product;

    @ManyToOne
    @JoinColumn(name = "packaging_id")
    @JsonManagedReference
    private PackingMaster packingMaster;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    @JsonManagedReference
    private Units units;

    @ManyToOne
    @JoinColumn(name = "flavour_master_id")
    @JsonManagedReference
    private FlavourMaster flavourMaster;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    @JsonManagedReference
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "group_id")
    @JsonManagedReference
    private Group group;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonManagedReference
    private Category category;

    @ManyToOne
    @JoinColumn(name = "subcategory_id")
    @JsonManagedReference
    private Subcategory subcategory;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<ProductBarcode> productBarcodes;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurInvoiceDetailsUnits> tranxPurInvoiceDetailsUnits;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurReturnDetailsUnits> tranxPurReturnDetailsUnits;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurChallanDetailsUnits> tranxPurChallanDetailsUnits;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesInvoiceDetailsUnits> tranxSalesInvoiceDetailsUnits;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesChallanDetailsUnits> tranxSalesChallanDetailsUnits;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesReturnDetailsUnits> tranxSalesReturnDetailsUnits;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxCounterSalesDetailsUnits> tranxCounterSalesDetailsUnits;

    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<ProductTaxDateMaster> productTaxDateMasters;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<LedgerTransactionPostings> ledgerTransactionPostings;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<ProductOpeningStocks> productOpeningStocks;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<InventoryDetailsPostings> inventoryDetailsPostings;

    /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
    private Double openingQty;
    private Double freeQty; // free_qty
    private Double costing; // net_amt / (qty+free_qty)
    private Double costingWithTax; // taxable_amt / (qty+free_qty)

    @ManyToOne
    @JoinColumn(name = "level_a_id")
    @JsonManagedReference
    private LevelA levelA;

    @ManyToOne
    @JoinColumn(name = "level_b_id")
    @JsonManagedReference
    private LevelB levelB;

    @ManyToOne
    @JoinColumn(name = "level_c_id")
    @JsonManagedReference
    private LevelC levelC;
    private Long supplierId;//Suppliers Id, maintain supplier wise batch numbers, if suppliers is changed while
    // creating purchase transactions create new batch number for that supplier

//    private Long lotNo; // if product doesn't have batch

    @Column(name = "dis_per")
    private Double disPer;
    @Column(name = "dis_amt")
    private Double disAmt;
    @Column(name = "cess_per")
    private Double cessPer;
    @Column(name = "cess_amt")
    private Double cessAmt;
    @Column(name = "pur_date")
    private LocalDate purchaseDate;
    @Column(name = "modify_date")
    private LocalDate modifyDate;

    @Column(name="barcode")
    private Double barcode;


    /**** END ****/

}