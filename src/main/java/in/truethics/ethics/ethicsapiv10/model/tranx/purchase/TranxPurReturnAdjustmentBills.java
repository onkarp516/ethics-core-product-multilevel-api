package in.truethics.ethics.ethicsapiv10.model.tranx.purchase;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tranx_pur_return_adj_bills_tbl")
public class TranxPurReturnAdjustmentBills {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "tranx_pur_invoice_id")
    private TranxPurInvoice tranxPurInvoice;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "tranx_pur_challan_id")
    private TranxPurChallan tranxPurChallan;

    private Double paidAmt;
    private Double remainingAmt;
    private Double totalAmt;
    private String source;
    private Boolean status;
    private Long createdBy;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private Long updatedBy;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private Long tranxPurReturnId; //refernce Id

}
