package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNote;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptMaster;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_sales_comp_invoice_tbl")
public class TranxSalesCompInvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long branchId;

    private Long outletId;

    private Long sundryDebtorsId;

    private Long salesAccountLedgerId;

    private Long salesDiscountLedgerId;

    private Long salesRoundOffId;

    private Long associateGroupsId;

    private Long fiscalYearId;

    private Long salesSerialNumber;
    private String salesInvoiceNo;
    private Date billDate;
    //    private LocalDate invoice_date;
    private String transportName;
    private String reference;
    private Double roundOff;
    private Double totalBaseAmount;  //qty*base_amount
    private Double totalAmount;
    private Double totalcgst;
    private Long totalqty;
    private Double totalsgst;
    private Double totaligst;
    private Double salesDiscountAmount; // purchase_discount
    private Double salesDiscountPer; // purchase_discount_amt
    private Double totalSalesDiscountAmt; // discount
    private Double additionalChargesTotal;
    private Double taxableAmount; // total
    private Double tcs;
    private Boolean isCounterSale;
    private String counterSaleId;
    private Boolean status;
    private String financialYear;
    private String narration;
    private String operations;
    private String referenceSqId;//Reference of Sales Quatations Ids
    private String referenceSoId;//Reference of Sales Order Ids
    private String referenceScId;//Reference of Sales Challan Ids
    private Long createdBy;
    private String paymentMode;
    private Double paymentAmount;
    private Double cash;
    private Double digital;
    private Double cardPayment;
    private Double advancedAmount;
    private String gstNumber;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private Long updatedBy;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private Double balance;

    /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
    private Long additionLedger1Id;
    private Long additionLedger2Id;
    private Long additionLedger3Id;
    private Double additionLedgerAmt1;
    private Double additionLedgerAmt2;
    private Double additionLedgerAmt3;
    private Double freeQty; // free qty
    private Double grossAmount; // gross total
    private Double totalTax; // tax
    private Long salesmanId;
    private String barcode;
    private Long salesmanUser;
    private Long transactionStatus; // maitaining return products while selecting bills, dont allow same bill next time for return
    private Boolean isSelected; // check whether this debitnote is selected or not while adjusting against the purchase invoice
    private Boolean isCreditNoteRef; //check for the debit note reference while creating purchase invoice
    private Boolean isRoundOff; // check if round of is applicable or not
    private Double tcsAmt;
    private String tcsMode;
    private Double tdsAmt;
    private Double tdsPer;//TDS Per
    private String imagePath;//uploading image of bill
    private String tranxCode;//Transaction unique code of each transaction performed


    //-----------------------------------------------------------------------------------------------------------

    @Column (name = "doctor_id")
    private Long doctorId;
    @Column (name = "client_name")
    private String clientName;
    @Column (name = "client_address")
    private String clientAddress;
    @Column (name = "mobile_number")
    private String mobileNumber;
    @Column(name = "transaction_tracking_no")
    private String transactionTrackingNo;//transactionTrackingNo;

    @Column(name = "patient_name")
    private String patientName;

    @Column(name = "doctor_address")
    private String doctorAddress;

    @Column(name = "image_upload")
    private String imageUpload;

}
