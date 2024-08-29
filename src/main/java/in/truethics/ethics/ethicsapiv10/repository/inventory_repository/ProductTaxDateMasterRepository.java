package in.truethics.ethics.ethicsapiv10.repository.inventory_repository;

import in.truethics.ethics.ethicsapiv10.model.inventory.ProductTaxDateMaster;
import in.truethics.ethics.ethicsapiv10.model.master.TaxMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ProductTaxDateMasterRepository extends JpaRepository<ProductTaxDateMaster, Long> {

    @Query(
            value = " SELECT * FROM product_tax_date_master_tbl WHERE product_id=?1 AND status=?2 " +
                    "ORDER BY ID DESC LIMIT 1 ", nativeQuery = true
    )
    ProductTaxDateMaster findLastRecords(Long id, Boolean status);

    @Query(
            value = " SELECT * FROM product_tax_date_master_tbl WHERE product_id=?1 AND status=?2 AND " +
                    "applicable_date<=?3 ORDER BY id DESC limit 1 ", nativeQuery = true
    )
    ProductTaxDateMaster findTax(Long id, boolean b);

    @Query(
            value = " SELECT * FROM product_tax_date_master_tbl WHERE tax_master_id=?1 AND status=?2 AND " +
                    "applicable_date=?3 ORDER BY id DESC limit 1 ", nativeQuery = true
    )
    ProductTaxDateMaster findRecord(TaxMaster taxMaster, Boolean status,LocalDate applicableDate);

    List<ProductTaxDateMaster> findByProductIdAndOutletIdAndBranchIdAndStatus(Long productId, Long id, Long id1, boolean b);

    List<ProductTaxDateMaster> findByProductIdAndOutletIdAndBranchIdIsNullAndStatus(Long productId, Long id, boolean b);
}
