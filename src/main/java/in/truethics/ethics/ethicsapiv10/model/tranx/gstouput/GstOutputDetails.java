package in.truethics.ethics.ethicsapiv10.model.tranx.gstouput;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductHsn;
import in.truethics.ethics.ethicsapiv10.model.master.TaxMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.gstinput.GstInputMaster;
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
@Table(name = "tranx_gst_ouput_details_tbl")
public class GstOutputDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gst_ouput_id", nullable = false)
    @JsonManagedReference
    private GstOutputMaster gstOutputMaster;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonManagedReference
    private Product product;

    @ManyToOne
    @JoinColumn(name = "hsn_id")
    @JsonManagedReference
    private ProductHsn productHsn;

    @ManyToOne
    @JoinColumn(name = "tax_id")
    @JsonManagedReference
    private TaxMaster taxMaster;
    private String particular;
    private String hsnNo;
    private Double igst;
    private Double cgst;
    private Double sgst;
    private Double amount;
    private Double qty;
    private Double finalAmt;
    private Double baseAmount;
    private Boolean status;
    private Long createdBy;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private Long updatedBy;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
