package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;


import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxSalesInvoiceRepository extends JpaRepository<TranxSalesInvoice, Long> {
    List<TranxSalesInvoice> findByOutletIdAndStatus(Long id, boolean b);

    @Query(
            value = " SELECT * FROM tranx_sales_invoice_tbl as a WHERE a.outlet_id=?1 And a.status=?2 " +
                    "And a.sundry_debtors_id=?3 AND date(a.invoice_date) BETWEEN ?4 AND ?5 AND" +
                    "branch_id IS NULL",
            nativeQuery = true
    )
    List<TranxSalesInvoice> findBySuppliersWithDates(
            Long id, boolean status, long sundry_debtors_id, String dateFrom, String dateTo);

    @Query(
            value = " SELECT * FROM tranx_sales_invoice_tbl as a WHERE a.outlet_id=?1 And a.status=?2 " +
                    "And a.sundry_debtors_id=?3 AND date(a.invoice_date) BETWEEN ?4 AND ?5 AND branch_id=?6 ",
            nativeQuery = true
    )
    List<TranxSalesInvoice> findByBranchSuppliersWithDates(
            Long id, boolean status, long sundry_debtors_id, String dateFrom, String dateTo, Long branchId);


    @Query(
            value = " SELECT COUNT(*) FROM tranx_sales_invoice_tbl WHERE outlet_id=?1 AND " +
                    "branch_id=?2", nativeQuery = true
    )
    Long findLastRecordWithBranch(Long id, Long branchId);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_sales_invoice_tbl WHERE outlet_id=?1 AND " +
                    "branch_id IS NULL", nativeQuery = true
    )
    Long findLastRecord(Long id);


    List<TranxSalesInvoice> findByOutletId(Long id);

    List<TranxSalesInvoice> findByOutletIdAndStatusAndSundryDebtorsId(Long id,
                                                                      boolean b, long sundry_debtors_id);

    @Query(
            value = " SELECT * FROM tranx_sales_invoice_tbl as a WHERE a.outlet_id=?1 And a.status=?2 " +
                    "And a.sundry_debtors_id =?3 AND date(a.bill_date) BETWEEN ?4 AND ?5",
            nativeQuery = true
    )
    List<TranxSalesInvoice> findByClientsWithDates(Long id, boolean b,
                                                   long sundry_debtors_id, String dateFrom, String dateTo);

    TranxSalesInvoice findByIdAndStatus(long sales_invoice_id, boolean b);

    TranxSalesInvoice findByIdAndOutletIdAndStatus(Long id, Long id1, boolean b);


    List<TranxSalesInvoice> findByOutletIdAndStatusOrderByIdDesc(Long id, boolean b);

    @Query(
            value = " SELECT * FROM  tranx_sales_invoice_tbl as a WHERE a.outlet_id=?1 And a.status=?2 " +
                    "And a.sundry_debtors_id=?3 And balance>0 And a.branch_id IS NULL",
            nativeQuery = true
    )
    List<TranxSalesInvoice> findPendingBills(Long outletId, boolean b, Long ledgerId);

    @Query(
            value = " SELECT * FROM tranx_sales_invoice_tbl as a WHERE a.outlet_id=?1 And a.branch_id=?2 And a.status=?3 " +
                    "And a.sundry_debtors_id=?4 And balance>0",
            nativeQuery = true
    )
    List<TranxSalesInvoice> findPendingBillsByBranchId(Long outletId, Long branchId, boolean b, Long ledgerId);


   /* @Query(
            value = " SELECT COUNT(*) FROM tranx_sales_invoice_tbl WHERE outlet_id=?1 AND status =1 AND branch_id=?2", nativeQuery = true
    )
    Long findBranchLastRecord(Long id, Long id1);*/

    List<TranxSalesInvoice> findByOutletIdAndBranchIdAndStatusOrderByIdDesc(Long id, Long id1, boolean b);

    List<TranxSalesInvoice> findByOutletIdAndBranchIdAndStatusAndSundryDebtorsId(Long id, Long id1, boolean b, Long ledgerId);

    TranxSalesInvoice findByIdAndOutletIdAndBranchIdAndStatus(Long id, Long id1, Long id2, boolean b);

    List<TranxSalesInvoice> findByOutletIdAndBranchIdAndIsCounterSaleAndStatusOrderByIdDesc(Long id, Long id1, boolean b, boolean b1);

    List<TranxSalesInvoice> findByOutletIdAndIsCounterSaleAndStatusOrderByIdDesc(Long id, boolean b, boolean b1);


    @Query(
            value = " SELECT COUNT(*) FROM tranx_sales_invoice_tbl WHERE outlet_id=?1 AND status =1 AND branch_id=?2 AND is_counter_sale=?3", nativeQuery = true
    )
    Long findBranchLastRecordCounterSales(Long id, Long id1, Boolean isCounterCustomer);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_sales_invoice_tbl WHERE outlet_id=?1 AND " +
                    "branch_id=?2 AND status=1 AND is_counter_sale=?3", nativeQuery = true
    )
    Long findLastRecordCounterSalesWithBranch(Long id, Long branch, Boolean isCounterCustomer);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_sales_invoice_tbl WHERE outlet_id=?1 AND " +
                    "branch_id IS NULL AND status=1 AND is_counter_sale=?2", nativeQuery = true
    )
    Long findLastRecordCounterSales(Long id, Boolean isCounterCustomer);

    TranxSalesInvoice findBySalesInvoiceNoAndOutletIdAndBranchIdAndStatus(String id, Long id1, Long id2, boolean b);

    TranxSalesInvoice findByOutletIdAndBranchIdAndSalesInvoiceNoIgnoreCase(Long id, Long id1, String bill_no);

    TranxSalesInvoice findByOutletIdAndSalesInvoiceNoIgnoreCaseAndBranchIsNull(Long id, String bill_no);

    List<TranxSalesInvoice> findByOutletIdAndIsCounterSaleAndStatusAndBranchIsNullOrderByIdDesc(Long id, boolean b, boolean b1);


    TranxSalesInvoice findByIdAndOutletIdAndStatusAndBranchIsNull(Long id, Long id1, boolean b);

    List<TranxSalesInvoice> findByOutletIdAndStatusAndSundryDebtorsIdAndBranchIsNull(Long id, boolean b, Long ledgerId);

    @Query(
            value = "SELECT * FROM `tranx_sales_invoice_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND " +
                    "DATE(bill_date) BETWEEN ?3 AND ?4 ORDER by bill_date",
            nativeQuery = true)
    List<TranxSalesInvoice> findInvoices(Long outletId, Long branchId,
                                         LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT * FROM `tranx_sales_invoice_tbl` WHERE outlet_id=?1 AND " +
                    "DATE(bill_date) BETWEEN ?2 AND ?3 AND branch_id IS NULL ORDER by bill_date",
            nativeQuery = true)
    List<TranxSalesInvoice> findInvoicesNoBr(Long id, LocalDate startDate, LocalDate endDate);


    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_sales_invoice_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND DATE(bill_date) " +
                    "BETWEEN ?3 AND ?4 AND status=?5", nativeQuery = true
    )
    Double findTotalInvoiceAmtwithBr(Long id, Long id1, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_sales_invoice_tbl` WHERE outlet_id=?1 AND branch_id IS NULL AND DATE(bill_date) " +
                    "BETWEEN ?2 AND ?3 AND status=?4", nativeQuery = true
    )
    Double findTotalInvoicesAmtNoBranch(Long id, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);

    @Query(
            value = "SELECT * FROM tranx_sales_invoice_tbl WHERE outlet_id=?1 AND branch_id=?2 AND " +
                    "sundry_debtors_id=?3 AND bill_date<=?4 AND fiscal_year_id=?5 AND balance>0 " +
                    "order by bil_date desc limit 1", nativeQuery = true
    )
    TranxSalesInvoice validateCreditdaysWBr(Long id, Long id1, Long debtorsId, LocalDate compareDate, FiscalYear fiscalYear);

    @Query(
            value = "SELECT * FROM tranx_sales_invoice_tbl WHERE outlet_id=?1 AND branch_id IS NULL AND " +
                    "sundry_debtors_id=?2 AND bill_date<=?3 AND fiscal_year_id=?4 AND balance>0 " +
                    "order by bil_date DESC limit 1", nativeQuery = true
    )
    TranxSalesInvoice validateCreditdaysWtBr(Long id, Long debtorsId, LocalDate compareDate, FiscalYear fiscalYear);

    @Query(
            value = "SELECT COUNT(*) FROM tranx_sales_invoice_tbl WHERE outlet_id=?1 AND branch_id=?2 AND " +
                    "sundry_debtors_id=?3 AND bill_date<=?4 AND fiscal_year_id=?5 AND balance>0 ", nativeQuery = true
    )
    Long validateCreditbillsWBr(Long id, Long id1, Long debtorsId, LocalDate compareDate, FiscalYear fiscalYear);

    @Query(
            value = "SELECT COUNT(*) FROM tranx_sales_invoice_tbl WHERE outlet_id=?1 AND branch_id IS NULL AND " +
                    "sundry_debtors_id=?2 AND bill_date<=?3 AND fiscal_year_id=?4 AND balance>0 ", nativeQuery = true
    )
    Long validateCreditbillsWtBr(Long id, Long debtorsId, LocalDate currentDate, FiscalYear fiscalYear);

    @Query(
            value = "SELECT IFNULL(SUM(balance),0.0) FROM tranx_sales_invoice_tbl WHERE outlet_id=?1 AND branch_id=?2 AND " +
                    "sundry_debtors_id=?3 AND bill_date<=?4 AND fiscal_year_id=?5 AND balance>0 ", nativeQuery = true
    )
    Double validateCreditvaluesWBr(Long id, Long id1, Long debtorsId, LocalDate currentDate, FiscalYear fiscalYear);

    @Query(
            value = "SELECT IFNULL(SUM(balance),0.0) FROM tranx_sales_invoice_tbl WHERE outlet_id=?1 AND " +
                    "branch_id IS NULL AND sundry_debtors_id=?2 AND bill_date<=?3 AND fiscal_year_id=?4 AND balance>0 ", nativeQuery = true
    )
    Double validateCreditvaluesWtBr(Long id, Long debtorsId, LocalDate currentDate, FiscalYear fiscalYear);

    @Query(
            value = "SELECT * FROM `tranx_sales_invoice_tbl` WHERE sundry_debtors_id=?1 AND DATE(bill_date) BETWEEN ?2 AND ?3 AND status=?4 ORDER by bill_date DESC", nativeQuery = true)
    List<TranxSalesInvoice> findSaleListForMobile(Long id, LocalDate startDatep, LocalDate endDatep, boolean b);

    @Query(
            value = "SELECT * FROM `tranx_sales_invoice_tbl` WHERE outlet_id=?1 AND DATE(bill_date) BETWEEN ?2 AND ?3 AND status=?4 ORDER by bill_date DESC", nativeQuery = true)
    List<TranxSalesInvoice> findSaleListWithDate(Long id, LocalDate startDatep, LocalDate endDatep, boolean b);


    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0),IFNULL(SUM(total_base_amount),0.0),IFNULL(SUM(total_tax),0.0),COUNT(*) FROM `tranx_sales_invoice_tbl` WHERE sundry_debtors_id=?1 AND DATE(bill_date) BETWEEN ?2 AND ?3 AND status=?4", nativeQuery = true)
    List<Object[]> findmobilesumTotalAmt(Long ledgerId, LocalDate startDatep, LocalDate endDatep, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM `tranx_sales_invoice_tbl` WHERE DATE(bill_date) BETWEEN ?1 " +
                    "AND ?2 AND status=?3", nativeQuery = true)
    Double findTotalAmtByStatus(LocalDate startDatep, LocalDate endDatep, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM `tranx_sales_invoice_tbl` WHERE DATE(bill_date) BETWEEN ?1 " +
                    "AND ?2 AND status=?3 AND outlet_id=?4", nativeQuery = true)
    Double findTotalAmtByStatusOutlet(LocalDate startDatep, LocalDate endDatep, boolean b, Long id);    //for web

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM `tranx_sales_invoice_tbl` WHERE DATE(bill_date)=?1 AND status=?2", nativeQuery = true)
    Double findTodayTotalAmtByStatus(LocalDate startDatep, boolean b);   //for mobile app

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM `tranx_sales_invoice_tbl` WHERE DATE(bill_date)=?1 AND status=?2 AND outlet_id=?3 ", nativeQuery = true)
    Double findTodayTotalAmtByStatusOutlet(LocalDate startDatep, boolean b, Long id);  //for web app

    @Query(
            value = "SELECT IFNULL(SUM(balance),0.0),COUNT(*) FROM `tranx_sales_invoice_tbl` WHERE sundry_debtors_id=?1 AND DATE(bill_date) BETWEEN ?2 AND ?3 AND balance>0 AND status=?4", nativeQuery = true)
    List<Object[]> findReceivableTotalAmt(Long ledgerId, LocalDate startDate, LocalDate endDate, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_sales_invoice_tbl` WHERE DATE(bill_date)=?1 AND status=?2", nativeQuery = true
    )
    Double findSaleTotalAmtByStatus(LocalDate startOfWeek, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_sales_invoice_tbl` WHERE DATE(bill_date)=?1 AND status=?2 AND outlet_id=?3", nativeQuery = true
    )
    Double findSaleTotalAmtByStatusOutlet(LocalDate startOfWeek, boolean b, Long id); //for web

    @Query(
            value = " SELECT * FROM tranx_sales_invoice_tbl as a WHERE outlet_id=?1 And branch_id=?2 And status=?3 " +
                    "And sundry_debtors_id=?4 And balance>0 AND DATE(bill_date) BETWEEN ?5 AND ?6",
            nativeQuery = true
    )
    List<TranxSalesInvoice> findPendingBillsByBranchIdWithDates(Long id, Long id1, boolean b, Long ledgerId,
                                                                LocalDate startDate, LocalDate endDate);

    @Query(
            value = " SELECT * FROM tranx_sales_invoice_tbl WHERE outlet_id=?1 And status=?2 " +
                    "And sundry_debtors_id=?3 And balance>0 And branch_id IS NULL AND DATE(bill_date) BETWEEN ?4 AND ?5",
            nativeQuery = true
    )
    List<TranxSalesInvoice> findPendingBillsWithDate(Long id, boolean b, Long ledgerId, LocalDate startDate,
                                                     LocalDate endDate);


@Query(
     value ="SELECT IFNULL(sum(total_amount),0.0),IFNULL(sum(total_base_amount),0.0), IFNULL(sum(total_tax),0.0), " +
             "IFNULL(sum(totaligst),0.0),  IFNULL(sum(totalsgst),0.0),  IFNULL(sum(totalcgst),0.0) " +
             "FROM tranx_sales_invoice_tbl WHERE tranx_sales_invoice_tbl.status=1 AND sundry_debtors_id=?1 ",nativeQuery = true
)
    List<String> findTotal(LedgerMaster mLedger);

    @Query(
    value= "SELECT * FROM core_product_multilevel_db.ledger_master_tbl WHERE unique_code='SUDR' order by state_code",
            nativeQuery = true
    )
    List<String> findLedgerId();
    @Query(
            value = " SELECT id FROM tranx_sales_invoice_tbl WHERE sundry_debtors_id=?1 AND outlet_id=?2 And status=?3 " +
                    "And total_amount>=250000",
            nativeQuery = true
    )
    List<String> findLedgerId(Long id, Long aLong, boolean status);
}