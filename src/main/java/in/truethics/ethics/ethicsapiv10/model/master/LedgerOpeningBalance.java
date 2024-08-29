package in.truethics.ethics.ethicsapiv10.model.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ledger_opening_balance_tbl")
public class LedgerOpeningBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String invoice_no;
    private LocalDate invoice_date;
    private Long due_days;
    private Double bill_amt;
    private Double invoice_paid_amt;
    private Double invoice_bal_amt;
    private String invoiceBalType;// CR or DR
    private Long ledgerId;
    private String balancingType; //Ledger Balancing Type
    private Boolean status;
    private Long createdBy;
    private Long updatedBy;
    @CreationTimestamp
    private LocalDate createdAt;
    @UpdateTimestamp
    private LocalDate updatedAt;
}
