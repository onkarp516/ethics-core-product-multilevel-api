package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tranx_counter_sales_tbl")
public class TranxCounterSales {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxCounterSalesDetails> tranxCounterSalesDetails;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxCounterSalesProdSrNo> tranxCounterSalesProdSrNos;

    private Long counterSaleSrNo; // it is used for counterNo
    private String counterSaleNo;
    private Date transactionDate;
    private String customerName;
    private Long mobileNumber;
    private Double totalBill;
    private Double totalBaseAmt;
    private Double taxableAmt;
    private Double roundoff;
    private Boolean status;
    private Boolean isBillConverted;// 1: Converted  0: Not Converted
    private Long createdBy;
    private String financialYear;
    private Double totalDiscount;
    private String narrations;
    private Double freeQty; // free qty
    private Double totalqty;
    private Double totalcgst;
    private Double totalsgst;
    private Double totaligst;
    private String paymentMode;
    private Double paymentAmount;
    private Double cash;
    private Double digital;
    private Double cardPayment;
    private Double advancedAmount;
    private String operations;
    private Double discountAmt;//Discount In Amt
    @CreationTimestamp
    private LocalDateTime createdAt;
    private Long updatedBy;
    /*** only for Upahar Manufacturing Unit ***/
    private LocalDate counterSalesDate;
    private Long transactionStatus;
    private String tranxCode;//Transaction unique code of each transaction performed


}
