package in.truethics.ethics.ethicsapiv10.repository.inventory_repository;

import in.truethics.ethics.ethicsapiv10.model.inventory.InventorySummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Date;

public interface StockSummaryRepository extends JpaRepository<InventorySummary,Long> {

    InventorySummary findByOutletIdAndBranchIdAndProductIdAndUnitsIdAndTranxDate(Long id, Long id1, Long id2, Long unitId, Date tranxDate);

    InventorySummary findByOutletIdAndBranchIdIsNullAndProductIdAndUnitsIdAndTranxDate(Long id, Long id1,  Long unitId, Date tranxDate);
}
