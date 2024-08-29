package in.truethics.ethics.ethicsapiv10.repository.barcode_repository;

import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBarcode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BarcodeRepository extends JpaRepository<ProductBarcode, Long> {

    List<ProductBarcode> findByTransactionIdAndStatus(Long id, boolean b);
}