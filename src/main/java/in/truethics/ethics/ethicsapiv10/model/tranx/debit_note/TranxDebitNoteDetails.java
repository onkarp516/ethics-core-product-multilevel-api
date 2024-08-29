package in.truethics.ethics.ethicsapiv10.model.tranx.debit_note;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import in.truethics.ethics.ethicsapiv10.model.master.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_debit_note_details_tbl")
public class TranxDebitNoteDetails {
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
    @JoinColumn(name = "sundry_creditor_id")
    @JsonManagedReference
    private LedgerMaster sundryCreditor;

    @ManyToOne
    @JoinColumn(name = "transaction_status_id")
    @JsonManagedReference
    private TransactionStatus transactionStatus;

    @ManyToOne
    @JoinColumn(name = "tranx_debitnote_master_id")
    @JsonManagedReference
    private TranxDebitNoteNewReferenceMaster tranxDebitNoteMaster;

    @ManyToOne
    @JoinColumn(name = "ledger_id")
    @JsonManagedReference
    private LedgerMaster ledgerMaster;

    private String type; //Cr or Dr
    private String ledgerType;
    private Double totalAmount;
    private double balance;
    private Long adjustedId; //adjusted this debit note against purhcase invoice
    private String adjustedSource; // purchase invoice, payment or receipt
    @CreationTimestamp
    private LocalDateTime createdAt;
    private Long createdBy;
    private boolean status;
    private String adjustmentStatus; // immediate , future or refund
    private String operations;  // create or adjust
    private Double paidAmt;
    private String source;

    private double dr;
    private double cr;
    private String paymentMethod;
    private String paymentTranxNo;
    private LocalDate transactionDate;
    private Double payableAmt;
    private Double selectedAmt;
    private Double remainingAmt;
    private Boolean isAdvance;
    private String bankName;
    private Date paymentDate;

}
