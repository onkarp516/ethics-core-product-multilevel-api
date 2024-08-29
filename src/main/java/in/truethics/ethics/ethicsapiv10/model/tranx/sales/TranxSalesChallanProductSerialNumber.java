package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.*;
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
@Table(name = "tranx_sales_challan_product_sr_no_tbl")
public class TranxSalesChallanProductSerialNumber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    @JsonManagedReference
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "outlet_id", nullable = false)
    @JsonManagedReference
    private Outlet outlet;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonManagedReference
    private Product product;


    @ManyToOne
    @JoinColumn(name = "transaction_type_master_id")
    @JsonManagedReference
    private TransactionTypeMaster transactionTypeMaster;


    private String serialNo;
    private LocalDateTime purchaseCreatedAt;
    private LocalDateTime saleCreatedAt;
    private String transactionStatus;
    private String operations;
    private Long createdBy;
    @CreationTimestamp
    private LocalDateTime createdDate;
    private Long updatedBy;
    @UpdateTimestamp
    private LocalDateTime updatedDate;
    private Boolean status;

    /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
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


    @ManyToOne
    @JoinColumn(name = "sales_challan_unit_details_id")
    @JsonManagedReference
    private TranxSalesChallanDetailsUnits tranxSalesChallanDetailsUnits;

    @ManyToOne
    @JoinColumn(name = "units_id")
    @JsonManagedReference
    private Units units;
}
