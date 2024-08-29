package in.truethics.ethics.ethicsapiv10.repository.master_repository;


import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;

import java.util.List;

public interface OutletRepository extends JpaRepository<Outlet, Long> {

    List<Outlet> findByCreatedByAndStatus(Long id, boolean b);

    Outlet findByIdAndStatus(long outletId, boolean b);

    @Procedure("default_ledgers")
    void createDefaultLedgers(Long branchId, Long outletId, Long createdBy);

    @Procedure("duties_taxes_registered_outlet")
    void createDefaultGST(Long branchId, Long outletId, Long createdBy);

    @Procedure("duties_taxes_unregistered_outlet")
    void createDefaultGSTUnRegistered(Long branchId, Long outletId, Long createdBy);

    List<Outlet> findByStatus(boolean b);

    Outlet findByCompanyNameIgnoreCaseAndStatus(String companyName, boolean b);

    @Procedure("create_counter_customer_ledger")
    void createCounterCustomer(Long outletId,  Long createdBy);

    @Procedure("default_pur_sales_ac")
    void createLedgersPurSaleAc(Long branchId, Long outletId, Long createdBy);

    @Procedure("tcstds_registered_outlet")
    void createDefaultRegisteredTCS(Long branchId, Long outletId, Long createdBy);

    @Procedure("tcstds_unregistered_outlet")
    void createDefaultTCSUnRegistered(Long branchId, Long outletId, Long createdBy);
    @Procedure("default_tax_masters")
    void createLedgersTax(Long branchId, Long outletId, Long createdBy);


}