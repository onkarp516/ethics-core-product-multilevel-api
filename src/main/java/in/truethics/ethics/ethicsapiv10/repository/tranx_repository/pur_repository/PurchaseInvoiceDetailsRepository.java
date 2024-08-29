package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoiceDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface PurchaseInvoiceDetailsRepository extends JpaRepository<TranxPurInvoiceDetails, Long> {
   

    List<TranxPurInvoiceDetails> findByPurchaseTransactionIdAndStatus(Long id, boolean b);

    TranxPurInvoiceDetails findByIdAndStatus(Long detailsId, boolean b);

    @Query(
            value = " SELECT * FROM tranx_purchase_invoice_details_tbl " +
                    "WHERE purchase_invoice_id=?1 AND status=?2 AND id IN(?3) "
            , nativeQuery = true
    )
    List<TranxPurInvoiceDetails> findInvoiceByIdWithProductsId(Long id, boolean state, String str);

    Set<TranxPurInvoiceDetails> findByProductId(Long id);

    TranxPurInvoiceDetails findTop1ByProductIdOrderByIdDesc(Long id);


}
