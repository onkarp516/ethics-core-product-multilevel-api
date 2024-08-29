package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoiceDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurOrderDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoiceDetailsUnits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxSalesInvoiceDetailsUnitRepository extends JpaRepository<TranxSalesInvoiceDetailsUnits, Long> {

    TranxSalesInvoiceDetailsUnits findByIdAndStatus(Long details_id, boolean b);

    List<TranxSalesInvoiceDetailsUnits> findBySalesInvoiceIdAndStatus(long transactionId, boolean b);


    @Query(
            value = " SELECT product_id FROM tranx_sales_invoice_details_units_tbl " +
                    "WHERE sales_invoice_id=?1 AND status=?2 GROUP BY product_id ", nativeQuery = true)
    List<Object[]> findByTranxPurId(Long id, boolean b);

    List<TranxSalesInvoiceDetailsUnits> findByProductIdAndStatusOrderByIdDesc(Long productId, boolean b);

    @Query(
            value = " SELECT IFNULL(avg(rate),0.0) FROM tranx_sales_invoice_details_units_tbl WHERE product_id=?1 and unit_id=?2", nativeQuery = true
    )
    Double findTotalValue(Long productId, Long id);

    @Query(
            value = "SELECT IFNULL(SUM(qty),0.0) FROM `tranx_sales_invoice_details_units_tbl` " +
                    "WHERE product_id=?1 AND batch_id=?2 AND DATE(created_at) BETWEEN ?3 AND ?4", nativeQuery = true
    )
    Double findExpiredSalesSumQty(Long productId, Long batchId, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);

    @Query(
            value = " SELECT IFNULL(avg(total_amount),0.0) FROM tranx_sales_invoice_details_units_tbl " +
                    "WHERE product_id=?1 and batch_id=?2", nativeQuery = true
    )
    Double findTotalBatchValue(Long productId, Long batchId);

    TranxSalesInvoiceDetailsUnits findBySalesInvoiceIdAndProductIdAndProductBatchNoId(Long id, Long productId, Long batchId);

    @Query(
            value = "SELECT * FROM `tranx_sales_invoice_details_units_tbl` WHERE sales_invoice_id=?1 AND status=?2", nativeQuery = true)
    List<TranxSalesInvoiceDetailsUnits> findSalesInvoicesDetails(Long id, boolean b);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_sales_invoice_details_units_tbl WHERE "+
                    "batch_id=?1 AND status=?2", nativeQuery = true
    )
    Long countBatchExists(Long batch_id, Boolean status);

    TranxSalesInvoiceDetailsUnits findBySalesInvoiceIdAndStatusAndProductId(Long id, boolean b, Long id1);

    @Query(
            value = "SELECT * FROM tranx_sales_invoice_details_units_tbl LEFT JOIN tranx_sales_invoice_tbl ON " +
                    "tranx_sales_invoice_details_units_tbl.sales_invoice_id=tranx_sales_invoice_tbl.id WHERE " +
                    "product_id=?2 AND tranx_sales_invoice_tbl.sundry_debtors_id=?1 AND return_qty<qty", nativeQuery = true
    )
    List<TranxSalesInvoiceDetailsUnits> findProductList(Long sundryCreditorId, Long productId, boolean b);

    @Query(
            value = "SELECT tranx_sales_invoice_details_units_tbl.id FROM `tranx_sales_invoice_details_units_tbl` LEFT JOIN tranx_sales_invoice_tbl ON" +
                    " tranx_sales_invoice_details_units_tbl.sales_invoice_id=tranx_sales_invoice_tbl.id" +
                    " WHERE product_id=?1 AND batch_id=?2 AND tranx_sales_invoice_tbl.sundry_debtors_id=?3 AND tranx_sales_invoice_details_units_tbl.status=?4",
            nativeQuery = true
    )
    List<Long> findBatchList(Long productId, Long BatchId, Long sundryDebtorsId, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(qty),0.0) FROM `tranx_sales_invoice_details_units_tbl` LEFT JOIN tranx_sales_invoice_tbl " +
                    "ON tranx_sales_invoice_details_units_tbl.sales_invoice_id=tranx_sales_invoice_tbl.id" +
                    " WHERE product_id=?1 AND batch_id=?2 AND rate=?3 AND tranx_sales_invoice_details_units_tbl.status=?4 AND " +
                    "tranx_sales_invoice_tbl.sundry_debtors_id=?5", nativeQuery = true
    )
    Double findQuantity(Long productId, Long batchId, Double rate, boolean b,Long sundryDebtorsId);

    @Query(
            value = "SELECT * FROM tranx_sales_invoice_details_units_tbl LEFT JOIN tranx_sales_invoice_tbl ON " +
                    "tranx_sales_invoice_details_units_tbl.sales_invoice_id=tranx_sales_invoice_tbl.id WHERE " +
                    "product_id=?1 AND batch_id=?2 AND tranx_sales_invoice_tbl.sundry_debtors_id=?3",nativeQuery = true
    )
    List<TranxSalesInvoiceDetailsUnits> findProductTotalQty(Long productId, Long productBatchNoId, Long sdId);

    @Query(
            value = "SELECT * FROM tranx_sales_invoice_details_units_tbl as a LEFT join tranx_sales_invoice_tbl on " +
                    "a.sales_invoice_id=tranx_sales_invoice_tbl.id where " +
                    "a.product_id=?1 and tranx_sales_invoice_tbl.bill_date between ?2 AND ?3 AND a.status=?4", nativeQuery = true
    )
    List<TranxSalesInvoiceDetailsUnits> findProductTransactions(Long productId, LocalDate startDatep, LocalDate endDatep, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(qty),0.0),IFNULL(rate*qty,0.0) FROM tranx_sales_invoice_details_units_tbl as a LEFT join tranx_sales_invoice_tbl on " +
                    "a.sales_invoice_id=tranx_sales_invoice_tbl.id where " +
                    "a.product_id=?1 and tranx_sales_invoice_tbl.bill_date=?2 AND a.status=?3", nativeQuery = true
    )
    String findRateAndQty(Long productId, LocalDate billDate, boolean b);
    @Query(
            value = "SELECT IFNULL(SUM(a.qty),0.0),IFNULL(SUM(a.rate),0.0) FROM tranx_sales_invoice_details_units_tbl AS a LEFT JOIN" +
                    " tranx_sales_invoice_tbl ON a.sales_invoice_id=tranx_sales_invoice_tbl.id WHERE product_id=?1" +
                    " AND a.status=?2 AND tranx_sales_invoice_tbl.bill_date BETWEEN ?3 AND ?4",nativeQuery = true
    )
    String findSalesQtyByProductIdAndStatus(Long productId, boolean b, String toString, String toString1);

    @Query(
            value = "SELECT IFNULL(SUM(a.qty),0.0),IFNULL(SUM(a.rate),0.0), a.unit_id FROM tranx_sales_return_details_units_tbl AS a " +
                    "LEFT JOIN tranx_sales_return_invoice_tbl ON a.sales_return_id=tranx_sales_return_invoice_tbl.id WHERE product_id=?1 AND a.status=?2 AND " +
                    "tranx_sales_return_invoice_tbl.transaction_date BETWEEN ?3 AND ?4",nativeQuery = true
    )
    String findSalesReturnQtyByProductIdAndStatus(Long productId, boolean b, String toString, String toString1);

    TranxSalesInvoiceDetailsUnits findBySalesInvoiceIdAndProductIdAndStatus(Long id, Long productId, boolean b);

}