package in.truethics.ethics.ethicsapiv10.model.inventory;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.TaxMaster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_units_level_tbl")
public class ProductUnitsLevelMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double unitConversion;
    private Double unitConvMargn;
    private Double purchaseRate;
    private Double mrp;
    private Boolean isNegativeStocks;
    private Double minSalesRateA;
    private Double minSalesRateB;
    private Double minSalesRateC;
    private LocalDate taxApplicableDate;
    private Double maxDiscount;
    private Double minDiscount;
    private Double minMargin;
    private Boolean status;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonManagedReference
    private Product product;
    private String subFilterMasters; //multiple level comma seperated ids
    @ManyToOne
    @JoinColumn(name = "hsn_id")
    @JsonManagedReference
    private ProductHsn productHsn;

    @ManyToOne
    @JoinColumn(name = "taxmaster_id")
    @JsonManagedReference
    private TaxMaster taxMaster;



}
