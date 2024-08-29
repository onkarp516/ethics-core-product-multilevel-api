package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurChallan;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoice;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tranx_sales_return_adj_bills_tbl")
public class TranxSalesReturnAdjustmentBills {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "tranx_sales_invoice_id")
    private TranxSalesInvoice tranxSalesInvoice;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "tranx_sales_challan_id")
    private TranxSalesChallan tranxSalesChallan;

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
    private Long tranxSalesReturnId; //refernce Id

}
