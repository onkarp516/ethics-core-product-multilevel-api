package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_sales_order_duties_taxes_tbl")
public class TranxSalesOrderDutiesTaxes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sales_order_invoice_id", nullable = false)
    @JsonManagedReference
    private TranxSalesOrder salesTransaction;

    @ManyToOne
    @JoinColumn(name = "sundry_debtors_id", nullable = false)
    @JsonManagedReference
    private LedgerMaster sundryDebtors;

    @ManyToOne
    @JoinColumn(name = "duties_taxes_ledger_id", nullable = false)
    @JsonManagedReference
    private LedgerMaster dutiesTaxes;

    private Boolean intra; //intra is for within state i.e cgst and sgst and inter is for outside state ie. igst
    private Double amount;
    private Boolean status;
    @CreationTimestamp
    private LocalDateTime createdDate;
}