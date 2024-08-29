package in.truethics.ethics.ethicsapiv10.service.reports_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.GenerateFiscalYear;
import in.truethics.ethics.ethicsapiv10.common.InventoryCommonPostings;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.InventoryDetailsPostings;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductUnitPacking;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.*;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.ProductBatchNoRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.InventoryDetailsPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductOpeningStocksRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.FiscalYearRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.*;
import in.truethics.ethics.ethicsapiv10.service.master_service.ProductService;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StockService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private TranxPurChallanRepository tranxPurChallanRepository;
    @Autowired
    private TranxPurChallanDetailsUnitRepository tranxPurChallanDetailsUnitRepository;
    @Autowired
    private TranxSalesChallanDetailsUnitsRepository tranxSalesChallanDetailsUnitsRepository;

    @Autowired
    private TranxSalesChallanRepository tranxSalesChallanRepository;
    @Autowired
    private ProductUnitRepository productUnitRepository;
    @Autowired
    private InventoryCommonPostings inventoryCommonPostings;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private ProductBatchNoRepository productBatchNoRepository;
    private static final Logger productLogger = LogManager.getLogger(ProductService.class);
    @Autowired
    private TranxPurInvoiceDetailsUnitsRepository tranxPurInvoiceDetailsUnitsRepository;
    @Autowired
    private TranxCSDetailsUnitsRepository tranxCSDetailsUnitsRepository;
    @Autowired
    private TranxSalesInvoiceDetailsUnitRepository tranxSalesInvoiceDetailsUnitRepository;
    @Autowired
    private TranxPurInvoiceRepository tranxPurInvoiceRepository;
    @Autowired
    private InventoryDetailsPostingsRepository inventoryDetailsPostingsRepository;

    @Autowired
    FiscalYearRepository fiscalYearRepository;

    @Autowired
    private TranxSalesInvoiceRepository tranxSalesInvoiceRepository;
    @Autowired
    private ProductOpeningStocksRepository productOpeningStocksRepository;

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private TranxPurReturnsRepository tranxPurReturnsRepository;
    @Autowired
    private TranxSalesReturnRepository tranxSalesReturnRepository;

    @Autowired
    private TranxPurReturnDetailsUnitRepository tranxPurReturnDetailsUnitRepository;

    @Autowired
    private TranxSalesReturnDetailsUnitsRepository tranxSalesReturnDetailsUnitsRepository;


    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    @Autowired
    private TranxCounterSalesRepository tranxCounterSalesRepository;

    /****** WholeStock and Available Stock and Batch Stock product details ********/
    public JsonObject getWholeStockProducts(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<Product> productList = new ArrayList<>();
        List<InventoryDetailsPostings> inventoryDetailsPostings = new ArrayList<>();
        List<Object[]> list = new ArrayList<>();
        Long branchId = null;
        if (users.getBranch() != null) {
            productList = productRepository.findByOutletIdAndBranchIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true);
            branchId = users.getBranch().getId();
        } else {
            productList = productRepository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        JsonObject finalResult = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        for (Product mProduct : productList) {
            JsonObject mObject = new JsonObject();
            JsonArray productunitarray = new JsonArray();
            mObject.addProperty("id", mProduct.getId());
            mObject.addProperty("product_name", mProduct.getProductName());
            mObject.addProperty("product_code", mProduct.getProductCode() != null ? mProduct.getProductCode():"");
            mObject.addProperty("brand_name", mProduct.getBrand().getBrandName());
            mObject.addProperty("packaging", mProduct.getPackingMaster() != null ? mProduct.getPackingMaster().getPackName() : "");
            mObject.addProperty("companyName", mProduct.getOutlet().getCompanyName());
            mObject.addProperty("tax_per", mProduct.getTaxMaster().getIgst());

            //inventoryDetailsPostings = inventoryDetailsPostingsRepository.findProductsGroupByUnits(mProduct.getId(), true);
            list = inventoryDetailsPostingsRepository.findProductsGroupByUnits(mProduct.getId(), true);
            for (int j = 0; j < list.size(); j++) {
                Object[] objects = list.get(j);
                Long inventoryId = Long.parseLong(objects[0].toString());
                InventoryDetailsPostings unitPacking = inventoryDetailsPostingsRepository.findByIdAndStatus(inventoryId, true);
                JsonObject productunitobject = new JsonObject();
                productunitobject.addProperty("row_id", unitPacking.getId());
                productunitobject.addProperty("unit_name", unitPacking.getUnits().getUnitName());
                productunitobject.addProperty("qty", unitPacking.getQty());
                productunitobject.addProperty("batchno", unitPacking.getUniqueBatchNo() != null ? unitPacking.getUniqueBatchNo() : "");
                productunitobject.addProperty("batchid", unitPacking.getProductBatch() != null ? unitPacking.getProductBatch().getId().toString() : "");
                productunitobject.addProperty("hsn", unitPacking.getProduct().getProductHsn().getHsnNumber());
                productunitobject.addProperty("group", unitPacking.getProduct().getGroup() != null ? unitPacking.getProduct().getGroup().getGroupName() : "");
                productunitobject.addProperty("subgroup", unitPacking.getProduct().getSubgroup() != null ? unitPacking.getProduct().getSubgroup().getSubgroupName() : "");
                productunitobject.addProperty("category", unitPacking.getProduct().getCategory() != null ? unitPacking.getProduct().getCategory().getCategoryName() : "");
                productunitobject.addProperty("tax_type", unitPacking.getProduct().getTaxType() != null ? unitPacking.getProduct().getTaxType() : "");
//                productunitobject.addProperty("tax_per", unitPacking.getProduct().getTaxMaster() != null ?unitPacking.getProduct().getTaxMaster().getIgst() : 0);
                productunitobject.addProperty("Shelf_id", unitPacking.getProduct().getShelfId() != null ? unitPacking.getProduct().getShelfId() : "");

                productunitobject.addProperty("purchase_rate", unitPacking.getProduct().getPurchaseRate());
                productunitobject.addProperty("Shelf_id", unitPacking.getProduct().getShelfId());
                productunitobject.addProperty("margin", unitPacking.getProduct().getMarginPer());
                productunitobject.addProperty("Cost", unitPacking.getProduct().getPurchaseRate());
//                productunitobject.addProperty("opening_qty",unitPacking.getProduct().get);

                //   ProductBatchNo productBatchNo = productBatchNoRepository.findByBatchNoAndStatus(unitPacking.getUniqueBatchNo(), true);
                productunitobject.addProperty("expiryDate", unitPacking.getProductBatch() != null ?
                        (unitPacking.getProductBatch().getExpiryDate() != null ? unitPacking.getProductBatch().getExpiryDate().toString() : "") : "");
                Long fiscalId = null;
                LocalDate currentDate = LocalDate.now();
                FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
                if (fiscalYear != null)
                    fiscalId = fiscalYear.getId();
                Double closing = inventoryCommonPostings.getClosingStockProductFilters(branchId,
                        users.getOutlet().getId(), mProduct.getId(), unitPacking.getLevelA() != null ?
                                unitPacking.getLevelA().getId() : null,
                        null, null, unitPacking.getUnits().getId(),
                        unitPacking.getProductBatch() != null ? unitPacking.getProductBatch().getId() : null, fiscalId);
                productunitobject.addProperty("closing_stock", closing);
                productunitarray.add(productunitobject);
            }

            mObject.add("product_unit_data", productunitarray);
            jsonArray.add(mObject);
        }
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("data", jsonArray);
        return finalResult;
    }

    /****** WholeStock and Available Stock Monthwise product details ********/
    public Object getMonthwiseWholeStockDetails(HttpServletRequest request) {
        JsonObject res = new JsonObject();
        Long id = 0L;
        Double total_month_sum = 0.0;
        Double credit_total = 0.0;
        List<Object[]> list = new ArrayList<>();
        try {
            Map<String, String[]> paramMap = request.getParameterMap();
            String endDate = null;
            LocalDate endDatep = null;
            String startDate = null;
            LocalDate startDatep = null;
            Double opening_bal = 0.0;
            Long productId = Long.valueOf(request.getParameter("productId"));
            LocalDate currentStartDate = null;
            LocalDate currentEndDate = null;
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            //****This Code For Users Dates Selection Between Start And End Date Manually****//
            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
                endDate = request.getParameter("end_date");
                endDatep = LocalDate.parse(endDate);
                startDate = request.getParameter("start_date");
                startDatep = LocalDate.parse(startDate);

            } else {
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }
            currentStartDate = startDatep;
            currentEndDate = endDatep;
            if (startDatep.isAfter(endDatep)) {
                System.out.println("Start Date Should not be After");
                return 0;
            }
            JsonArray innerArr = new JsonArray();
            while (startDatep.isBefore(endDatep)) {
                JsonObject monthObject = new JsonObject();
                JsonArray monthArray = new JsonArray();
                JsonObject mainObj = new JsonObject();
                JsonArray saleArray = new JsonArray();
                JsonArray closingArray = new JsonArray();
                String month = startDatep.getMonth().name();
                System.out.println();
                LocalDate startMonthDate = startDatep;
                LocalDate endMonthDate = startDatep.withDayOfMonth(startDatep.lengthOfMonth());
                System.out.println("Start Date:" + startMonthDate + "End Date " + endMonthDate); //**  If You Want To Print  All Start And End Date of each month  between Fiscal Year **//
                startDatep = endMonthDate.plusDays(1);
                List<ProductUnitPacking> productUnitPackings = productUnitRepository.findByProductIdAndStatus(productId, true);
                for (ProductUnitPacking mUnits : productUnitPackings) {
                    Double totalpValue = 0.0;
                    Double totalsValue = 0.0;
                    Double totalClosingqty = 0.0;
                    Double totalpQty = 0.0;
                    Double totalsQty = 0.0;
                    Double totalptrnQty = 0.0;
                    Double totalptrnValue = 0.0;
                    Double totalstrnValue = 0.0;
                    Double totalstrnQty = 0.0;

                    /*****  Purchase *******/
                    JsonObject inside = new JsonObject();
                    totalpQty = inventoryDetailsPostingsRepository.
                            findByTotalQty(productId, "CR", mUnits.getUnits().getId(), 1L, startMonthDate.toString(), endMonthDate.toString());
                    if (mUnits.getProduct().getIsBatchNumber())
                        totalpValue = productBatchNoRepository.findPurchaseTotalVale(productId, mUnits.getUnits().getId());
                    else
                        totalpValue = tranxPurInvoiceDetailsUnitsRepository.findTotalValue(productId, mUnits.getUnits().getId());
                    /*****  Purchase Return *******/
                    totalptrnQty = inventoryDetailsPostingsRepository.
                            findByTotalQty(productId, "DR", mUnits.getUnits().getId(), 2L,
                                    startMonthDate.toString(), endMonthDate.toString());
                    totalptrnValue = tranxPurReturnDetailsUnitRepository.findTotalValue(productId, mUnits.getUnits().getId());
                    inside.addProperty("unit", mUnits.getUnits().getUnitName());
                    inside.addProperty("qty", totalpQty - totalptrnQty);
                    inside.addProperty("value", totalpValue - totalptrnValue);
                    monthArray.add(inside);

                    /****** Sales ****/
                    JsonObject saleInside = new JsonObject();
                    totalsQty = inventoryDetailsPostingsRepository.
                            findByTotalQty(productId, "DR", mUnits.getUnits().getId(), 3L,
                                    startMonthDate.toString(), endMonthDate.toString());
                    if (mUnits.getProduct().getIsBatchNumber())
                        totalsValue = productBatchNoRepository.findTotalVale(productId, mUnits.getUnits().getId());
                    else
                        totalsValue = tranxSalesInvoiceDetailsUnitRepository.findTotalValue(productId, mUnits.getUnits().getId());
                    System.out.println("totalsValue" + totalsValue);
                    /***** Sales Return *****/
                    totalstrnQty = inventoryDetailsPostingsRepository.
                            findByTotalQty(productId, "CR", mUnits.getUnits().getId(), 4L,
                                    startMonthDate.toString(), endMonthDate.toString());
                    totalstrnValue = tranxSalesReturnDetailsUnitsRepository.findTotalValue(productId, mUnits.getUnits().getId());
                    System.out.println("totalstrnValue" + totalstrnValue);
                    saleInside.addProperty("qty", totalsQty + totalstrnQty);
                    saleInside.addProperty("unit", mUnits.getUnits().getUnitName());
                    saleInside.addProperty("value", totalsValue + totalstrnValue);
                    saleArray.add(saleInside);
                    JsonObject closingInside = new JsonObject();
                    totalClosingqty = totalpQty - totalsQty;
                    closingInside.addProperty("qty", totalClosingqty);
                    closingInside.addProperty("unit", mUnits.getUnits().getUnitName());
                    closingInside.addProperty("value", totalClosingqty * totalpValue);
                    closingArray.add(closingInside);
                }

                mainObj.add("purchase", monthArray);
                mainObj.add("sale", saleArray);
                mainObj.add("closing", closingArray);

                monthObject.addProperty("month_name", month);
                monthObject.addProperty("start_date", startMonthDate.toString());
                monthObject.addProperty("end_date", endMonthDate.toString());
                monthObject.add("responseObject", mainObj);
                innerArr.add(monthObject);
            }
            Double open_stk = productOpeningStocksRepository.findOpeningStockByProductId(productId);
            System.out.println("open_stk  " + open_stk);
            res.addProperty("d_start_date", currentStartDate.toString());
            res.addProperty("d_end_date", currentEndDate.toString());
            res.addProperty("id", productId);
            res.addProperty("opening_stock", open_stk != null ? open_stk : 0);
            res.add("response", innerArr);
            res.addProperty("opening_bal", opening_bal);
            res.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("message", "Failed To Load Data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;

    }

    /****** Batch wise Monthwise product details ********/
    public Object getMonthwiseBatchStockDetails(HttpServletRequest request) {
        JsonObject res = new JsonObject();
        Long id = 0L;
        Double total_month_sum = 0.0;
        Double credit_total = 0.0;

        List<Object[]> list = new ArrayList<>();
        try {
            Map<String, String[]> paramMap = request.getParameterMap();
            String endDate = null;
            LocalDate endDatep = null;
            String startDate = null;
            LocalDate startDatep = null;
            Double opening_bal = 0.0;
            Long productId = Long.valueOf(request.getParameter("productId"));
            String batchNo = request.getParameter("batchno");
            LocalDate currentStartDate = null;
            LocalDate currentEndDate = null;
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            //****This Code For Users Dates Selection Between Start And End Date Manually****//
            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
                endDate = request.getParameter("end_date");
                endDatep = LocalDate.parse(endDate);
                startDate = request.getParameter("start_date");
                startDatep = LocalDate.parse(startDate);

            } else {
                //****This Code For Load Data Default Current Year From Automatically load And Select Fiscal Year From Fiscal Year Table****//
                /*List<Object[]> nlist = new ArrayList<>();
                nlist = fiscalYearRepository.findByStartDateAndEndDateOutletIdAndBranchIdAndStatusLimit();
                for (int i = 0; i < nlist.size(); i++) {
                    Object obj[] = nlist.get(i);
                    System.out.println("start Date:" + obj[0].toString());
                    System.out.println("end Date:" + obj[1].toString());
                    startDatep = LocalDate.parse(obj[0].toString());
                    endDatep = LocalDate.parse(obj[1].toString());
                }*/
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }
            currentStartDate = startDatep;
            currentEndDate = endDatep;
            if (startDatep.isAfter(endDatep)) {
                System.out.println("Start Date Should not be After");
                return 0;
            }
            JsonArray mainArr = new JsonArray();
            JsonArray innerArr = new JsonArray();
            while (startDatep.isBefore(endDatep)) {
                JsonObject monthObject = new JsonObject();
                JsonArray monthArray = new JsonArray();
                JsonObject mainObj = new JsonObject();
                JsonArray saleArray = new JsonArray();
                JsonArray closingArray = new JsonArray();
                Double closing_bal = 0.0;
                String month = startDatep.getMonth().name();
                System.out.println();
                LocalDate startMonthDate = startDatep;
                LocalDate endMonthDate = startDatep.withDayOfMonth(startDatep.lengthOfMonth());
                System.out.println("Start Date:" + startMonthDate + "End Date " + endMonthDate); //**  If You Want To Print  All Start And End Date of each month  between Fiscal Year **//
                startDatep = endMonthDate.plusDays(1);
                System.out.println();

                List<ProductUnitPacking> productUnitPackings = productUnitRepository.findByProductIdAndStatus(productId, true);
                for (ProductUnitPacking mUnits : productUnitPackings) {
                    Double totalQty = 0.0;
                    Double totalpurBValue = 0.0;
                    Double totalsaleBValue = 0.0;
                    Double totalClosingqty = 0.0;
                    Double totalpurBQty = 0.0;
                    Double totalsaleBQty = 0.0;
                    /*****  Purchase *******/
                    JsonObject inside = new JsonObject();
                    totalpurBQty = inventoryDetailsPostingsRepository.
                            findByTotalBatchQty(productId, 1L, mUnits.getUnits().getId(), startMonthDate.toString(), endMonthDate.toString(), batchNo);
                    totalpurBValue = productBatchNoRepository.findPurchaseBatchTotalVale(productId, mUnits.getUnits().getId(), batchNo);
                    System.out.println("totalValue" + totalpurBValue);

                    inside.addProperty("qty", totalpurBQty);
                    inside.addProperty("unit", mUnits.getUnits().getUnitName());
                    inside.addProperty("value", totalpurBValue);
                    monthArray.add(inside);

                    /****** Sales ****/
                    JsonObject saleInside = new JsonObject();
                    totalsaleBQty = inventoryDetailsPostingsRepository.
                            findBySaleBatchQty(productId, 3L, mUnits.getUnits().getId(), startMonthDate.toString(), endMonthDate.toString(), batchNo);
                    totalsaleBValue = productBatchNoRepository.findSaleBatchTotalVale(productId, mUnits.getUnits().getId(), batchNo);
                    System.out.println("totalValue" + totalsaleBValue);

                    saleInside.addProperty("qty", totalsaleBQty);
                    saleInside.addProperty("unit", mUnits.getUnits().getUnitName());
                    saleInside.addProperty("value", totalsaleBValue);
                    saleArray.add(saleInside);

                    JsonObject closingInside = new JsonObject();
                    totalClosingqty = totalpurBQty - totalsaleBQty;

                    closingInside.addProperty("qty", totalClosingqty);
                    closingInside.addProperty("unit", mUnits.getUnits().getUnitName());
                    closingInside.addProperty("value", totalClosingqty * totalpurBValue);
                    closingArray.add(closingInside);
                }

                mainObj.add("purchase", monthArray);
                mainObj.add("sale", saleArray);
                mainObj.add("closing", closingArray);

                monthObject.addProperty("month_name", month);
                monthObject.addProperty("start_date", startMonthDate.toString());
                monthObject.addProperty("end_date", endMonthDate.toString());
                monthObject.add("responseObject", mainObj);
                innerArr.add(monthObject);
            }
            res.addProperty("d_start_date", currentStartDate.toString());
            res.addProperty("d_end_date", currentEndDate.toString());
            res.add("response", innerArr);
            res.addProperty("opening_bal", opening_bal);
            res.addProperty("responseStatus", HttpStatus.OK.value());

        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("message", "Failed To Load Data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;

    }

    public JsonObject getExpiryProducts(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalResult = new JsonObject();
        try {
            Long branchId = null;
            Long fiscalId = null;
            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
            if (fiscalYear != null) fiscalId = fiscalYear.getId();
            LocalDate currentDate = LocalDate.now().plusDays(30);
            String startDate = "";
            String endDate = currentDate.toString();
            if (request.getParameterMap().containsKey("start_date") && !request.getParameter("start_date").isEmpty())
                startDate = request.getParameter("start_date");
            if (request.getParameterMap().containsKey("end_date") && !request.getParameter("end_date").isEmpty())
                endDate = request.getParameter("end_date");

            JsonArray jsonArray = new JsonArray();
            List<ProductBatchNo> productBatchNos = new ArrayList<>();

            String basicQuery = "SELECT * FROM product_batchno_tbl WHERE  fiscal_year_id=" + fiscalId + " AND outlet_id=" + users.getOutlet().getId();
            if (users.getBranch() != null) {
                branchId = users.getBranch().getId();
                basicQuery += " AND branch_id=" + users.getBranch().getId();
            }

            if (startDate.isEmpty())
                basicQuery += " AND expiry_date<'" + currentDate + "'";
            else
                basicQuery += " AND (expiry_date BETWEEN '" + startDate + "' AND '" + endDate + "') ";
            Query q = entityManager.createNativeQuery(basicQuery, ProductBatchNo.class);
            productBatchNos = q.getResultList();
           /* if (startDate.isEmpty())
                productBatchNos = productBatchNoRepository.FindExpiryProduct(currentDate, users.getOutlet().getId());
            else
                productBatchNos = productBatchNoRepository.FindExpiredProductByDateRange(startDate, endDate, users.getOutlet().getId());*/
            System.out.println("productBatchNos size:" + productBatchNos.size());
            for (ProductBatchNo batchNo : productBatchNos) {
                Long level_a_id = null;
                Long level_b_id = null;
                Long level_c_id = null;

                if (batchNo.getLevelA() != null)
                    level_a_id = batchNo.getLevelA().getId();
                if (batchNo.getLevelB() != null)
                    level_b_id = batchNo.getLevelB().getId();
                if (batchNo.getLevelC() != null)
                    level_c_id = batchNo.getLevelC().getId();

                Double closing = inventoryCommonPostings.getClosingStockProductFilters(branchId, users.getOutlet().getId(),
                        batchNo.getProduct().getId(), level_a_id, level_b_id, level_c_id, batchNo.getUnits().getId(),
                        batchNo.getId(), fiscalId);

                JsonObject batchObject = new JsonObject();
                batchObject.addProperty("id", batchNo.getProduct().getId());
                batchObject.addProperty("product_name", batchNo.getProduct().getProductName());
                batchObject.addProperty("brand_name", batchNo.getProduct().getBrand().getBrandName());
                batchObject.addProperty("packaging", batchNo.getProduct().getPackingMaster() != null ? batchNo.getProduct().getPackingMaster().getPackName() : "");
                batchObject.addProperty("companyName", batchNo.getOutlet().getCompanyName());
                batchObject.addProperty("unit_name", batchNo.getUnits().getUnitName());
                batchObject.addProperty("qty", closing);
                batchObject.addProperty("batchno", batchNo.getBatchNo());
                batchObject.addProperty("batchid", batchNo.getId());
                batchObject.addProperty("mfgDate", batchNo.getManufacturingDate() != null ?
                        batchNo.getManufacturingDate().toString() : "");
                batchObject.addProperty("expiryDate", batchNo.getExpiryDate() != null ?
                        batchNo.getExpiryDate().toString() : "");
                jsonArray.add(batchObject);
            }
            finalResult.add("data", jsonArray);
            finalResult.addProperty("companyName", users.getOutlet().getCompanyName());
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            finalResult.addProperty("companyName", users.getOutlet().getCompanyName());
            finalResult.addProperty("message", "Failed to load data");
            finalResult.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return finalResult;
    }

    public Object getExpiryProductsMonthwise(HttpServletRequest request) {
        JsonObject res = new JsonObject();
        Long productId = Long.parseLong(request.getParameter("product_id"));
        Long batchId = Long.parseLong(request.getParameter("batch_id"));
        Double sumPurchase = 0.0;
        Double sumSales = 0.0;
        Double totalClosingqty = 0.0;
        Double totalPurValue = 0.0;
        Double totalSalesValue = 0.0;

        JsonArray purArray = new JsonArray();

        JsonArray salesArray = new JsonArray();

        JsonArray closingArray = new JsonArray();

        try {
            Map<String, String[]> paramMap = request.getParameterMap();
            LocalDate endDatep = null;
            LocalDate startDatep = null;
            LocalDate currentStartDate = null;
            LocalDate currentEndDate = null;
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
                endDatep = LocalDate.parse(request.getParameter("end_date"));
                startDatep = LocalDate.parse(request.getParameter("start_date"));
            } else {
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }
            currentStartDate = startDatep;
            currentEndDate = endDatep;
            if (startDatep.isAfter(endDatep)) {
                System.out.println("Start Date Should not be After");
                return 0;
            }
            JsonObject responseObjct = new JsonObject();
            JsonArray innerArr = new JsonArray();
//            JsonArray sumArr = new JsonArray();
            while (startDatep.isBefore(endDatep)) {
                JsonObject jsonObject = new JsonObject();
                JsonObject purObject = new JsonObject();
                JsonObject salesObject = new JsonObject();
                JsonObject closingObject = new JsonObject();
                JsonArray purchaseArr = new JsonArray();
                JsonArray saleArr = new JsonArray();
                JsonArray closingArr = new JsonArray();
                String month = startDatep.getMonth().name();
                System.out.println();
                LocalDate startMonthDate = startDatep;
                LocalDate endMonthDate = startDatep.withDayOfMonth(startDatep.lengthOfMonth());
                System.out.println("Start Date:" + startMonthDate + "End Date " + endMonthDate);
                /******  If You Want To Print  All Start And End Date of each month  between Fiscal Year ******/
                startDatep = endMonthDate.plusDays(1);
                System.out.println();
                /**** TranxPurchase Invoice  ****/
                sumPurchase = tranxPurInvoiceDetailsUnitsRepository.findExpiredPurSumQty(
                        productId, batchId, startMonthDate, endMonthDate, true);
                totalPurValue = tranxPurInvoiceDetailsUnitsRepository.findTotalBatchValue(productId, batchId);
                purObject.addProperty("qty", sumPurchase);
                purObject.addProperty("value", totalPurValue);
                purchaseArr.add(purObject);
//                purArray.add(purObject);
                /**** TranxSales Invoice  ****/
                sumSales = tranxSalesInvoiceDetailsUnitRepository.findExpiredSalesSumQty(
                        productId, batchId, startMonthDate, endMonthDate, true);
                totalSalesValue = tranxSalesInvoiceDetailsUnitRepository.findTotalBatchValue(productId, batchId);
                salesObject.addProperty("qty", sumSales);
                salesObject.addProperty("value", totalSalesValue);
                saleArr.add(salesObject);
//                salesArray.add(salesObject);
                /***** closing QTY ******/
                totalClosingqty = sumPurchase - sumSales;
                closingObject.addProperty("qty", totalClosingqty);
                closingObject.addProperty("value", totalClosingqty * totalPurValue);
                closingArr.add(closingObject);
//                closingArray.add(closingObject);
                jsonObject.addProperty("month", month);
                jsonObject.addProperty("start_date", startMonthDate.toString());
                jsonObject.addProperty("end_date", endMonthDate.toString());
//                sumArr.add(purObject);
//                sumArr.add(salesObject);
//                sumArr.add(closingObject);
                jsonObject.add("purchase", purchaseArr);
                jsonObject.add("sale", saleArr);
                jsonObject.add("closing", closingArr);
//                jsonObject.add("responseObject",sumArr);
                innerArr.add(jsonObject);
            }
            res.addProperty("d_start_date", currentStartDate.toString());
            res.addProperty("d_end_date", currentEndDate.toString());
            res.addProperty("d_end_date", currentEndDate.toString());
            res.addProperty("company_name", users.getOutlet().getCompanyName());
            res.add("response", innerArr);
            res.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("message", "Failed To Load Data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }

    public Object getExpiryProductsDetails(HttpServletRequest request) {
        JsonObject res = new JsonObject();
        JsonArray tranxInvoice = new JsonArray();
        List<InventoryDetailsPostings> tranxList = new ArrayList<>();
        LocalDate startDatep = LocalDate.parse(request.getParameter("start_date"));
        LocalDate endDatep = LocalDate.parse(request.getParameter("end_date"));
        Long productId = Long.parseLong(request.getParameter("product_id"));
        Long batchId = Long.parseLong(request.getParameter("batch_id"));
        tranxList = inventoryDetailsPostingsRepository.finProductsDetails(
                productId, batchId, startDatep, endDatep);
        for (InventoryDetailsPostings mDetails : tranxList) {
            TranxPurInvoice purVoucher = null;
            TranxSalesInvoice salesVoucher = null;
            JsonObject jsonObject = new JsonObject();
            if (mDetails.getTransactionType().getId() == 1L) {
                purVoucher = tranxPurInvoiceRepository.findByIdAndStatus(mDetails.getTranxId(), true);
                jsonObject.addProperty("particular", purVoucher != null ?
                        purVoucher.getSundryCreditors().getLedgerName() : "");
                jsonObject.addProperty("voucher_type", purVoucher != null ?
                        purVoucher.getPurchaseAccountLedger().getLedgerName() : "");
                jsonObject.addProperty("invoice_no", purVoucher != null ? purVoucher.getVendorInvoiceNo() : "");
                TranxPurInvoiceDetailsUnits purUnitDetails = tranxPurInvoiceDetailsUnitsRepository.
                        findByPurchaseTransactionIdAndProductIdAndProductBatchNoId(purVoucher.getId(), productId, batchId);
                jsonObject.addProperty("value", purUnitDetails != null ? purUnitDetails.getTotalAmount() : 0);
                jsonObject.addProperty("tranx_type", 1L);
            } else if (mDetails.getTransactionType().getId() == 3L) {
                salesVoucher = tranxSalesInvoiceRepository.findByIdAndStatus(mDetails.getTranxId(), true);
                jsonObject.addProperty("particular", salesVoucher != null ?
                        salesVoucher.getSundryDebtors().getLedgerName() : "");
                jsonObject.addProperty("voucher_type", salesVoucher != null ?
                        salesVoucher.getSalesAccountLedger().getLedgerName() : "");
                jsonObject.addProperty("invoice_no", salesVoucher != null ?
                        salesVoucher.getSalesInvoiceNo() : "");
                TranxSalesInvoiceDetailsUnits salesUnitDetails = tranxSalesInvoiceDetailsUnitRepository.
                        findBySalesInvoiceIdAndProductIdAndProductBatchNoId(salesVoucher.getId(), productId, batchId);
                jsonObject.addProperty("value", salesUnitDetails != null ?
                        salesUnitDetails.getTotalAmount() : 0);
                jsonObject.addProperty("tranx_type", 3L);

            }
            jsonObject.addProperty("date", mDetails.getTranxDate().toString());
            jsonObject.addProperty("qty", mDetails.getQty());
            jsonObject.addProperty("unit", mDetails.getUnits().getUnitName());
            tranxInvoice.add(jsonObject);
        }
        res.addProperty("message", "success");
        res.add("data", tranxInvoice);
        res.addProperty("responseStatus", HttpStatus.OK.value());
        return res;
    }

    public JsonObject getBatchwiseProductStocks(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonArray batchArray = new JsonArray();
        JsonObject result = new JsonObject();
        LocalDate endDatep = null;
        LocalDate startDatep = null;
        if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
            endDatep = LocalDate.parse(request.getParameter("end_date"));
            startDatep = LocalDate.parse(request.getParameter("start_date"));
        } else {
            List<Object[]> list = new ArrayList<>();
            list = fiscalYearRepository.findByStartDateAndEndDateOutletIdAndBranchIdAndStatus();
            Object obj[] = list.get(0);
            startDatep = LocalDate.parse(obj[0].toString());
            endDatep = LocalDate.parse(obj[1].toString());
        }
        Long level_a_id = null;
        Long level_b_id = null;
        Long level_c_id = null;
        Long unit_id = null;
        Long branchId = null;
        if (users.getBranch() != null) {
            branchId = users.getBranch().getId();
        }
        Long fiscalId = null;
        LocalDate currentDate = LocalDate.now();
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
        if (fiscalYear != null) fiscalId = fiscalYear.getId();
        Long product_id = Long.parseLong(request.getParameter("product_id"));
        List<ProductBatchNo> productbatch = productBatchNoRepository.findByBatchList(product_id, users.getOutlet().getId(), true, level_a_id, level_b_id, level_c_id, unit_id);
        if (productbatch != null && productbatch.size() > 0) {
            for (ProductBatchNo mBatch : productbatch) {
                Double opening = productOpeningStocksRepository.findSumProductOpeningStocksBatchwise(mBatch.getProduct().getId(), mBatch.getOutlet().getId(), mBatch.getBranch() != null ? mBatch.getBranch().getId() : null, fiscalId, mBatch.getId());
                Double free_qnt = productOpeningStocksRepository.findSumProductFreeQtyBatchwise(mBatch.getProduct().getId(), mBatch.getOutlet().getId(), mBatch.getBranch() != null ? mBatch.getBranch().getId() : null, fiscalId, mBatch.getId());
                Double closing = inventoryCommonPostings.getClosingStockProductFilters(branchId, users.getOutlet().getId(), product_id, level_a_id, level_b_id, level_c_id, unit_id, mBatch.getId(), fiscalId);
                JsonObject object = new JsonObject();
                object.addProperty("product_name", mBatch.getProduct().getProductName());
                object.addProperty("batch_no", mBatch.getBatchNo());
                object.addProperty("qty", mBatch.getQnty());
                object.addProperty("expiry_date", mBatch.getExpiryDate() != null ? mBatch.getExpiryDate().toString() : "");
                object.addProperty("manufacturing_date", mBatch.getManufacturingDate() != null ? mBatch.getManufacturingDate().toString() : "");
                batchArray.add(object);
            }
        }
        ProductBatchNo productBatchNo = productBatchNoRepository.getLastRecordByFilterForCosting(product_id,
                users.getOutlet().getId(), true, level_a_id, level_b_id, level_c_id, unit_id);
        if (productBatchNo != null) {
            result.addProperty("costing", productBatchNo.getCosting() != null ? productBatchNo.getCosting() : 0.0);
            result.addProperty("costingWithTax", productBatchNo.getCostingWithTax() != null ? productBatchNo.getCostingWithTax() : 0.0);
        } else {
            result.addProperty("costing", 0.0);
            result.addProperty("costingWithTax", 0.0);
        }
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.add("data", batchArray);
        return result;
    }

//    public Object getMonthwiseWholeStockDetailsPrdTranx(HttpServletRequest request) {
//
//        JsonObject res = new JsonObject();
//        JsonArray tranxInvoice = new JsonArray();
//        List<InventoryDetailsPostings> tranxList = new ArrayList<>();
//        LocalDate startDatep = LocalDate.parse(request.getParameter("start_date"));
//        LocalDate endDatep = LocalDate.parse(request.getParameter("end_date"));
//        Long productId = Long.parseLong(request.getParameter("productId"));
////        Long batchId = Long.parseLong(request.getParameter("batch_id"));
//        tranxList = inventoryDetailsPostingsRepository.findProductsDetails(
//                productId, startDatep, endDatep);
//        for (InventoryDetailsPostings mDetails : tranxList) {
//            TranxPurInvoice purVoucher = null;
//            TranxSalesInvoice salesVoucher = null;
//            TranxPurReturnInvoice purReturnVoucher = null;
//            TranxSalesReturnInvoice salesReturnVoucher = null;
//            JsonObject jsonObject = new JsonObject();
//            if (mDetails.getTransactionType().getId() == 1L) {
//                purVoucher = tranxPurInvoiceRepository.findByIdAndStatus(mDetails.getTranxId(), true);
//                if (purVoucher != null) {
//                    jsonObject.addProperty("particular", purVoucher != null ?
//                            purVoucher.getSundryCreditors().getLedgerName() : "");
//                    jsonObject.addProperty("voucher_type", purVoucher != null ?
//                            purVoucher.getPurchaseAccountLedger().getLedgerName() : "");
//                    jsonObject.addProperty("invoice_no", purVoucher != null ? purVoucher.getVendorInvoiceNo() : "");
//                    TranxPurInvoiceDetailsUnits purUnitDetails = tranxPurInvoiceDetailsUnitsRepository.
//                            findByPurchaseTransactionIdAndProductIdAndStatus(purVoucher.getId(), productId, true);
//                    jsonObject.addProperty("rate",purUnitDetails != null ?purUnitDetails.getRate() : 0);
//                    jsonObject.addProperty("value", purUnitDetails != null ? purUnitDetails.getTotalAmount() : 0);
//                    jsonObject.addProperty("tranx_type", 1L);
//                }
//            } else if (mDetails.getTransactionType().getId() == 3L) {
//                salesVoucher = tranxSalesInvoiceRepository.findByIdAndStatus(mDetails.getTranxId(), true);
//                if (salesVoucher != null) {
//                    jsonObject.addProperty("particular", salesVoucher != null ?
//                            salesVoucher.getSundryDebtors().getLedgerName() : "");
//                    jsonObject.addProperty("voucher_type", salesVoucher != null ?
//                            salesVoucher.getSalesAccountLedger().getLedgerName() : "");
//                    jsonObject.addProperty("invoice_no", salesVoucher != null ?
//                            salesVoucher.getSalesInvoiceNo() : "");
//                    TranxSalesInvoiceDetailsUnits salesUnitDetails = tranxSalesInvoiceDetailsUnitRepository.
//                            findBySalesInvoiceIdAndProductIdAndStatus(salesVoucher.getId(), productId, true);
//                    jsonObject.addProperty("rate",salesUnitDetails != null ?salesUnitDetails.getRate() : 0);
//                    jsonObject.addProperty("value", salesUnitDetails != null ?
//                            salesUnitDetails.getTotalAmount() : 0);
//                }
//                jsonObject.addProperty("tranx_type", 3L);
//            } else if (mDetails.getTransactionType().getId() == 2L) {
//                purReturnVoucher = tranxPurReturnsRepository.findByIdAndStatus(mDetails.getTranxId(), true);
//                if (purReturnVoucher != null) {
//                    jsonObject.addProperty("particular", purReturnVoucher != null ?
//                            purReturnVoucher.getSundryCreditors().getLedgerName() : "");
//                    jsonObject.addProperty("voucher_type", purReturnVoucher != null ?
//                            purReturnVoucher.getPurchaseAccountLedger().getLedgerName() : "");
//                    jsonObject.addProperty("invoice_no", purReturnVoucher != null ?
//                            purReturnVoucher.getPurRtnNo() : "");
//                    TranxPurReturnDetailsUnits purReturnUnitDetails = tranxPurReturnDetailsUnitRepository.
//                            findByTranxPurReturnInvoiceIdAndProductIdAndStatus(purReturnVoucher.getId(), productId, true);
//                    jsonObject.addProperty("value", purReturnUnitDetails != null ?
//                            purReturnUnitDetails.getTotalAmount() : 0);
//                    jsonObject.addProperty("tranx_type", 2L);
//                }
//
//            } else if (mDetails.getTransactionType().getId() == 4L) {
//                salesReturnVoucher = tranxSalesReturnRepository.findByIdAndStatus(mDetails.getTranxId(), true);
//                if (salesReturnVoucher != null) {
//                    jsonObject.addProperty("particular", salesReturnVoucher != null ?
//                            salesReturnVoucher.getSundryDebtors().getLedgerName() : "");
//                    jsonObject.addProperty("voucher_type", salesReturnVoucher != null ?
//                            salesReturnVoucher.getSalesAccountLedger().getLedgerName() : "");
//                    jsonObject.addProperty("invoice_no", salesReturnVoucher != null ?
//                            salesReturnVoucher.getSalesReturnNo() : "");
//                    TranxSalesReturnDetailsUnits salesReturnUnitDetails = tranxSalesReturnDetailsUnitsRepository.
//                            findBySalesReturnInvoiceIdAndProductIdAndStatus(salesReturnVoucher.getId(), productId, true);
//                    jsonObject.addProperty("value", salesReturnUnitDetails != null ?
//                            salesReturnUnitDetails.getTotalAmount() : 0);
//                    jsonObject.addProperty("tranx_type", 4L);
//                }
//            }
//            jsonObject.addProperty("tranx_unique_code", mDetails.getTransactionType().getTransactionCode());
//            jsonObject.addProperty("tranx_type", mDetails.getTransactionType().getTransactionName());
//            jsonObject.addProperty("date", mDetails.getTranxDate().toString());
//            jsonObject.addProperty("qty", mDetails.getQty());
//            jsonObject.addProperty("unit", mDetails.getUnits().getUnitName());
//            tranxInvoice.add(jsonObject);
//        }
//        res.addProperty("message", "success");
//        res.add("data", tranxInvoice);
//        res.addProperty("responseStatus", HttpStatus.OK.value());
//        return res;
//    }

//    public Object getMonthwiseWholeStockDetailsPrdTranx(HttpServletRequest request) {
//
//        JsonObject res = new JsonObject();
//        JsonArray tranxInvoice = new JsonArray();
//        List<InventoryDetailsPostings> tranxList = new ArrayList<>();
//        LocalDate startDatep = LocalDate.parse(request.getParameter("start_date"));
//        LocalDate endDatep = LocalDate.parse(request.getParameter("end_date"));
//        Long productId = Long.parseLong(request.getParameter("productId"));
////        Long batchId = Long.parseLong(request.getParameter("batch_id"));
//        tranxList = inventoryDetailsPostingsRepository.findProductsDetails(
//                productId, startDatep, endDatep);
//        for (InventoryDetailsPostings mDetails : tranxList) {
//            TranxPurInvoice purVoucher = null;
//            TranxSalesInvoice salesVoucher = null;
//            TranxPurReturnInvoice purReturnVoucher = null;
//            TranxSalesReturnInvoice salesReturnVoucher = null;
//            TranxPurChallan purChallanVoucher = null;
//            TranxSalesChallan salesChallanVoucher = null;
//            JsonObject jsonObject = new JsonObject();
//            if (mDetails.getTransactionType().getId() == 1L) {
//                purVoucher = tranxPurInvoiceRepository.findByIdAndStatus(mDetails.getTranxId(), true);
//                jsonObject.addProperty("particular", purVoucher != null ?
//                        purVoucher.getSundryCreditors().getLedgerName() : "");
//                jsonObject.addProperty("voucher_type", purVoucher != null ?
//                        purVoucher.getPurchaseAccountLedger().getLedgerName() : "");
//                jsonObject.addProperty("invoice_no", purVoucher != null ? purVoucher.getVendorInvoiceNo() : "");
//                TranxPurInvoiceDetailsUnits purUnitDetails = tranxPurInvoiceDetailsUnitsRepository.
//                        findByPurchaseTransactionIdAndProductIdAndStatus(purVoucher.getId(), productId, true);
//                jsonObject.addProperty("value", purUnitDetails != null ? purUnitDetails.getTotalAmount() : 0);
//                jsonObject.addProperty("tranx_type", 1L);
//
//            } else if (mDetails.getTransactionType().getId() == 3L) {
//                salesVoucher = tranxSalesInvoiceRepository.findByIdAndStatus(mDetails.getTranxId(), true);
//                jsonObject.addProperty("particular", salesVoucher != null ?
//                        salesVoucher.getSundryDebtors().getLedgerName() : "");
//                jsonObject.addProperty("voucher_type", salesVoucher != null ?
//                        salesVoucher.getSalesAccountLedger().getLedgerName() : "");
//                jsonObject.addProperty("invoice_no", salesVoucher != null ?
//                        salesVoucher.getSalesInvoiceNo() : "");
//                TranxSalesInvoiceDetailsUnits salesUnitDetails = tranxSalesInvoiceDetailsUnitRepository.
//                        findBySalesInvoiceIdAndProductIdAndStatus(salesVoucher.getId(), productId, true);
//                jsonObject.addProperty("value", salesUnitDetails != null ?
//                        salesUnitDetails.getTotalAmount() : 0);
//                jsonObject.addProperty("tranx_type", 3L);
//
//            } else if (mDetails.getTransactionType().getId() == 2L) {
//                purReturnVoucher = tranxPurReturnsRepository.findByIdAndStatus(mDetails.getTranxId(), true);
//                jsonObject.addProperty("particular", purReturnVoucher != null ?
//                        purReturnVoucher.getSundryCreditors().getLedgerName() : "");
//                jsonObject.addProperty("voucher_type", purReturnVoucher != null ?
//                        purReturnVoucher.getPurchaseAccountLedger().getLedgerName() : "");
//                jsonObject.addProperty("invoice_no", purReturnVoucher != null ?
//                        purReturnVoucher.getPurRtnNo() : "");
//                TranxPurReturnDetailsUnits purReturnUnitDetails = tranxPurReturnDetailsUnitRepository.
//                        findByTranxPurReturnInvoiceIdAndProductIdAndStatus(purReturnVoucher.getId(), productId, true);
//                jsonObject.addProperty("value", purReturnUnitDetails != null ?
//                        purReturnUnitDetails.getTotalAmount() : 0);
//                jsonObject.addProperty("tranx_type", 2L);
//
//            } else if (mDetails.getTransactionType().getId() == 4L) {
//                salesReturnVoucher = tranxSalesReturnRepository.findByIdAndStatus(mDetails.getTranxId(), true);
//                jsonObject.addProperty("particular", salesReturnVoucher != null ?
//                        salesReturnVoucher.getSundryDebtors().getLedgerName() : "");
//                jsonObject.addProperty("voucher_type", salesReturnVoucher != null ?
//                        salesReturnVoucher.getSalesAccountLedger().getLedgerName() : "");
//                jsonObject.addProperty("invoice_no", salesReturnVoucher != null ?
//                        salesReturnVoucher.getSalesReturnNo() : "");
//                TranxSalesReturnDetailsUnits salesReturnUnitDetails = tranxSalesReturnDetailsUnitsRepository.
//                        findBySalesReturnInvoiceIdAndProductIdAndStatus(salesReturnVoucher.getId(), productId, true);
//                jsonObject.addProperty("value", salesReturnUnitDetails != null ?
//                        salesReturnUnitDetails.getTotalAmount() : 0);
//                jsonObject.addProperty("tranx_type", 4L);
//            } else if (mDetails.getTransactionType().getId() == 12L) {
//                purChallanVoucher = tranxPurChallanRepository.findByIdAndStatus(mDetails.getTranxId(), true);
//                jsonObject.addProperty("particular", purChallanVoucher != null ?
//                        purChallanVoucher.getSundryCreditors().getLedgerName() : "");
//                jsonObject.addProperty("voucher_type", purChallanVoucher != null ?
//                        purChallanVoucher.getPurchaseAccountLedger().getLedgerName() : "");
//                jsonObject.addProperty("invoice_no", purChallanVoucher != null ? purChallanVoucher.getVendorInvoiceNo() : "");
//                TranxPurChallanDetailsUnits purChallanUnitDetails = tranxPurChallanDetailsUnitRepository.
//                        findByTranxPurChallanIdAndProductIdAndStatus(purChallanVoucher.getId(), productId, true);
//                jsonObject.addProperty("value", purChallanUnitDetails != null ? purChallanUnitDetails.getTotalAmount() : 0);
//                jsonObject.addProperty("tranx_type", 12L);
//
//            } else if (mDetails.getTransactionType().getId() == 15L) {
//
//                salesChallanVoucher = tranxSalesChallanRepository.findByIdAndStatus(mDetails.getTranxId(), true);
//                jsonObject.addProperty("particular", salesChallanVoucher != null ?
//                        salesChallanVoucher.getSundryDebtors().getLedgerName() : "");
//                jsonObject.addProperty("voucher_type", salesChallanVoucher != null ?
//                        salesChallanVoucher.getSalesAccountLedger().getLedgerName() : "");
//                jsonObject.addProperty("invoice_no", salesChallanVoucher != null ? salesChallanVoucher.getSc_bill_no() : "");
//                TranxSalesChallanDetailsUnits salesChallanDetails = tranxSalesChallanDetailsUnitsRepository.
//                        findBySalesChallanIdAndProductIdAndStatus(salesChallanVoucher.getId(), productId, true);
//                jsonObject.addProperty("value", salesChallanDetails != null ? salesChallanDetails.getTotalAmount() : 0);
//                jsonObject.addProperty("tranx_type", 15L);
//
//            }
//            jsonObject.addProperty("tranx_unique_code", mDetails.getTransactionType().getTransactionCode());
//            jsonObject.addProperty("tranx_type", mDetails.getTransactionType().getTransactionName());
//            jsonObject.addProperty("date", mDetails.getTranxDate().toString());
//            jsonObject.addProperty("qty", mDetails.getQty());
//            jsonObject.addProperty("unit", mDetails.getUnits().getUnitName());
//            tranxInvoice.add(jsonObject);
//        }
//        res.addProperty("message", "success");
//        res.add("data", tranxInvoice);
//        res.addProperty("responseStatus", HttpStatus.OK.value());
//        return res;
//    }
    public Object getMonthwiseWholeStockDetailsPrdTranx(HttpServletRequest request) {

        JsonObject res = new JsonObject();
        JsonArray tranxInvoice = new JsonArray();
        List<InventoryDetailsPostings> tranxList = new ArrayList<>();
        LocalDate startDatep = null;
        LocalDate endDatep = null;
        Long productId = Long.parseLong(request.getParameter("productId"));
        String durations = null;
        Long branchId = null;
        try {
            Map<String, String[]> paraMap = request.getParameterMap();
            if (paraMap.containsKey("start_date") && paraMap.containsKey("end_date")) {
                String stDay = request.getParameter("start_date");
                startDatep = LocalDate.parse(stDay);
                String endDay = request.getParameter("end_date");
                endDatep = LocalDate.parse(endDay);
            } else if (paraMap.containsKey("duration")) {
                durations = request.getParameter("duration");
                if (durations.equalsIgnoreCase("month")) {
                    //for finding first and last day of current month
                    LocalDate thisMonth = LocalDate.now();
                    String fDay = thisMonth.withDayOfMonth(1).toString();
                    String lDay = thisMonth.withDayOfMonth(thisMonth.lengthOfMonth()).toString();
                    startDatep = LocalDate.parse(fDay);
                    endDatep = LocalDate.parse(lDay);

                } else if (durations.equalsIgnoreCase("lastMonth")) {
                    //for finding first day and last day of previous month
                    Calendar aCalendar = Calendar.getInstance();
                    // add -1 month to current month
                    aCalendar.add(Calendar.MONTH, -1);
                    // set DATE to 1, so first date of previous month
                    aCalendar.set(Calendar.DATE, 1);
                    Date firstDateOfPreviousMonth = aCalendar.getTime();
                    // set actual maximum date of previous month
                    aCalendar.set(Calendar.DATE, aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    //read it
                    Date lastDateOfPreviousMonth = aCalendar.getTime();

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    String firstDay = df.format(firstDateOfPreviousMonth);  //here we get the first day of last month
                    String lastDay = df.format(lastDateOfPreviousMonth);    //here we get the last day of last month
                    startDatep = LocalDate.parse(firstDay);
                    endDatep = LocalDate.parse(lastDay);

                } else if (durations.equalsIgnoreCase("halfYear")) {
                    //for finding first and second half year start day and end day
                    LocalDate currentDate = LocalDate.now();
                    //for first half-year
                    LocalDate lastYear = currentDate.minusYears(1);
                    LocalDate firstDayOfFirstHalf = LocalDate.of(lastYear.getYear(), 1, 1);
                    LocalDate lastDayOfFirstHalf = LocalDate.of(lastYear.getYear(), 6, 30);

                    // Second half-year
                    LocalDate firstDayOfSecondHalf = LocalDate.of(lastYear.getYear(), 7, 1);
                    LocalDate lastDayOfSecondHalf = LocalDate.of(lastYear.getYear(), 12, 31);

                    // Format the dates in dd-MM-yyyy format
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                    String firstDayFirstHalfFormatted = firstDayOfFirstHalf.format(formatter);
                    String lastDayFirstHalfFormatted = lastDayOfFirstHalf.format(formatter);

                    String firstDaySecondHalfFormatted = firstDayOfSecondHalf.format(formatter);
                    String lastDaySecondHalfFormatted = lastDayOfSecondHalf.format(formatter);
                    System.out.println("firstDayFirstHalfFormatted " + firstDayFirstHalfFormatted + " lastDayFirstHalfFormatted " + lastDayFirstHalfFormatted);
                    System.out.println("firstDaySecondHalfFormatted " + firstDaySecondHalfFormatted + "  lastDaySecondHalfFormatted " + lastDaySecondHalfFormatted);
                    startDatep = LocalDate.parse(firstDaySecondHalfFormatted);
                    endDatep = LocalDate.parse(lastDaySecondHalfFormatted);
                } else if (durations.equalsIgnoreCase("fullYear")) {
                    List<Object[]> nlist = new ArrayList<>();
                    nlist = fiscalYearRepository.findByStartDateAndEndDateOutletIdAndBranchIdAndStatusLimit();
                    for (int i = 0; i < nlist.size(); i++) {
                        Object obj[] = nlist.get(i);
//                    System.out.println("start Date:" + obj[0].toString());
//                    System.out.println("end Date:" + obj[1].toString());
                        startDatep = LocalDate.parse(obj[0].toString());
                        endDatep = LocalDate.parse(obj[1].toString());
                    }
                }
            }
//        Long batchId = Long.parseLong(request.getParameter("batch_id"));
            tranxList = inventoryDetailsPostingsRepository.findProductsDetails(productId, startDatep, endDatep);
            for (InventoryDetailsPostings mDetails : tranxList) {
                TranxPurInvoice purVoucher = null;
                TranxSalesInvoice salesVoucher = null;
                TranxPurReturnInvoice purReturnVoucher = null;
                TranxSalesReturnInvoice salesReturnVoucher = null;
                TranxPurChallan purChallanVoucher = null;
                TranxSalesChallan salesChallanVoucher = null;
                TranxCounterSales counterSaleVoucher = null;
                JsonObject jsonObject = new JsonObject();
                if (mDetails.getTransactionType().getId() == 1L) {
                    purVoucher = tranxPurInvoiceRepository.findByIdAndStatus(mDetails.getTranxId(), true);
                    jsonObject.addProperty("particular", purVoucher != null ?
                            purVoucher.getSundryCreditors().getLedgerName() : "");
                    jsonObject.addProperty("voucher_type", purVoucher != null ?
                            purVoucher.getPurchaseAccountLedger().getLedgerName() : "");
                    jsonObject.addProperty("invoice_no", purVoucher != null ? purVoucher.getVendorInvoiceNo() : "");
                    List<TranxPurInvoiceDetailsUnits> purUnitDetails = tranxPurInvoiceDetailsUnitsRepository.
                            findByPurchaseInvoiceIdAndProductIdAndStatus1(purVoucher.getId(), productId, true);
                    jsonObject.addProperty("value", purUnitDetails != null ? purUnitDetails.get(0).getTotalAmount() : 0);
                    jsonObject.addProperty("tranx_type", 1L);
//                    jsonObject.addProperty("opening_qty");

                } else if (mDetails.getTransactionType().getId() == 3L) {
                    salesVoucher = tranxSalesInvoiceRepository.findByIdAndStatus(mDetails.getTranxId(), true);
                    jsonObject.addProperty("particular", salesVoucher != null ?
                            salesVoucher.getSundryDebtors().getLedgerName() : "");
                    jsonObject.addProperty("voucher_type", salesVoucher != null ?
                            salesVoucher.getSalesAccountLedger().getLedgerName() : "");
                    jsonObject.addProperty("invoice_no", salesVoucher != null ?
                            salesVoucher.getSalesInvoiceNo() : "");
                    TranxSalesInvoiceDetailsUnits salesUnitDetails = tranxSalesInvoiceDetailsUnitRepository.
                            findBySalesInvoiceIdAndProductIdAndStatus(salesVoucher.getId(), productId, true);
                    jsonObject.addProperty("value", salesUnitDetails != null ?
                            salesUnitDetails.getTotalAmount() : 0);
                    jsonObject.addProperty("tranx_type", 3L);

                } else if (mDetails.getTransactionType().getId() == 2L) {
                    purReturnVoucher = tranxPurReturnsRepository.findByIdAndStatus(mDetails.getTranxId(), true);
                    jsonObject.addProperty("particular", purReturnVoucher != null ?
                            purReturnVoucher.getSundryCreditors().getLedgerName() : "");
                    jsonObject.addProperty("voucher_type", purReturnVoucher != null ?
                            purReturnVoucher.getPurchaseAccountLedger().getLedgerName() : "");
                    jsonObject.addProperty("invoice_no", purReturnVoucher != null ?
                            purReturnVoucher.getPurRtnNo() : "");
                    TranxPurReturnDetailsUnits purReturnUnitDetails = tranxPurReturnDetailsUnitRepository.
                            findByTranxPurReturnInvoiceIdAndProductIdAndStatus(purReturnVoucher.getId(), productId, true);
                    jsonObject.addProperty("value", purReturnUnitDetails != null ?
                            purReturnUnitDetails.getTotalAmount() : 0);
                    jsonObject.addProperty("tranx_type", 2L);

                } else if (mDetails.getTransactionType().getId() == 4L) {
                    salesReturnVoucher = tranxSalesReturnRepository.findByIdAndStatus(mDetails.getTranxId(), true);
                    jsonObject.addProperty("particular", salesReturnVoucher != null ?
                            salesReturnVoucher.getSundryDebtors().getLedgerName() : "");
                    jsonObject.addProperty("voucher_type", salesReturnVoucher != null ?
                            salesReturnVoucher.getSalesAccountLedger().getLedgerName() : "");
                    jsonObject.addProperty("invoice_no", salesReturnVoucher != null ?
                            salesReturnVoucher.getSalesReturnNo() : "");
                    TranxSalesReturnDetailsUnits salesReturnUnitDetails = tranxSalesReturnDetailsUnitsRepository.
                            findBySalesReturnInvoiceIdAndProductIdAndStatus(salesReturnVoucher.getId(), productId, true);
                    jsonObject.addProperty("value", salesReturnUnitDetails != null ?
                            salesReturnUnitDetails.getTotalAmount() : 0);
                    jsonObject.addProperty("tranx_type", 4L);
                } else if (mDetails.getTransactionType().getId() == 12L) {
                    purChallanVoucher = tranxPurChallanRepository.findByIdAndStatus(mDetails.getTranxId(), true);
                    jsonObject.addProperty("particular", purChallanVoucher != null ?
                            purChallanVoucher.getSundryCreditors().getLedgerName() : "");
                    jsonObject.addProperty("voucher_type", purChallanVoucher != null ?
                            purChallanVoucher.getPurchaseAccountLedger().getLedgerName() : "");
                    jsonObject.addProperty("invoice_no", purChallanVoucher != null ? purChallanVoucher.getVendorInvoiceNo() : "");
                    TranxPurChallanDetailsUnits purChallanUnitDetails = tranxPurChallanDetailsUnitRepository.
                            findByTranxPurChallanIdAndProductIdAndStatus(purChallanVoucher.getId(), productId, true);
                    jsonObject.addProperty("value", purChallanUnitDetails != null ? purChallanUnitDetails.getTotalAmount() : 0);
                    jsonObject.addProperty("tranx_type", 12L);

                } else if (mDetails.getTransactionType().getId() == 15L) {

                    salesChallanVoucher = tranxSalesChallanRepository.findByIdAndStatus(mDetails.getTranxId(), true);
                    jsonObject.addProperty("particular", salesChallanVoucher != null ?
                            salesChallanVoucher.getSundryDebtors().getLedgerName() : "");
                    jsonObject.addProperty("voucher_type", salesChallanVoucher != null ?
                            salesChallanVoucher.getSalesAccountLedger().getLedgerName() : "");
                    jsonObject.addProperty("invoice_no", salesChallanVoucher != null ? salesChallanVoucher.getSc_bill_no() : "");
                    TranxSalesChallanDetailsUnits salesChallanDetails = tranxSalesChallanDetailsUnitsRepository.
                            findBySalesChallanIdAndProductIdAndStatus(salesChallanVoucher.getId(), productId, true);
                    jsonObject.addProperty("value", salesChallanDetails != null ? salesChallanDetails.getTotalAmount() : 0);
                    jsonObject.addProperty("tranx_type", 15L);

                } else if (mDetails.getTransactionType().getId() == 16L) {

                    counterSaleVoucher = tranxCounterSalesRepository.findByIdAndStatus(mDetails.getTranxId(), true);
                    jsonObject.addProperty("particular", counterSaleVoucher.getCustomerName() != null ? counterSaleVoucher.getCustomerName() : "");
                    jsonObject.addProperty("voucher_type","Sales Account");
                    jsonObject.addProperty("invoice_no", counterSaleVoucher != null ? counterSaleVoucher.getCounterSaleNo() : "");
                    TranxCounterSalesDetailsUnits salesChallanDetails = tranxCSDetailsUnitsRepository.
                            findByCounterSalesIdAndProductIdAndStatus(counterSaleVoucher.getId(), productId, true);
                    jsonObject.addProperty("value", salesChallanDetails != null ? salesChallanDetails.getNetAmount() : 0);
                    jsonObject.addProperty("tranx_type", 16L);

                }

                jsonObject.addProperty("tranx_unique_code", mDetails.getTransactionType().getTransactionCode());
                jsonObject.addProperty("tranx_type", mDetails.getTransactionType().getTransactionName());
                jsonObject.addProperty("date", mDetails.getTranxDate().toString());
                jsonObject.addProperty("qty", mDetails.getQty());
                jsonObject.addProperty("unit", mDetails.getUnits().getUnitName());
                tranxInvoice.add(jsonObject);
            }
            res.addProperty("message", "success");
            res.add("data", tranxInvoice);
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.addProperty("d_start_date", startDatep.toString());
            res.addProperty("d_end_date", endDatep.toString());
        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("Error", "Failed to load data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }


        return res;

    }

    public JsonObject getExpiredProducts(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalResult = new JsonObject();
        try {
            Long branchId = null;
            Long fiscalId = null;
            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
            if (fiscalYear != null) fiscalId = fiscalYear.getId();
            LocalDate currentDate = LocalDate.now();
            String startDate = "";
            String endDate = currentDate.toString();
            if (request.getParameterMap().containsKey("start_date") && !request.getParameter("start_date").isEmpty())
                startDate = request.getParameter("start_date");
            if (request.getParameterMap().containsKey("end_date") && !request.getParameter("end_date").isEmpty())
                endDate = request.getParameter("end_date");

            JsonArray jsonArray = new JsonArray();
            List<ProductBatchNo> productBatchNos = new ArrayList<>();

            String basicQuery = "SELECT * FROM product_batchno_tbl WHERE fiscal_year_id=" + fiscalId + " AND outlet_id=" + users.getOutlet().getId();
            if (users.getBranch() != null) {
                branchId = users.getBranch().getId();
                basicQuery += " AND branch_id=" + users.getBranch().getId();
            }

            if (startDate.isEmpty())
                basicQuery += " AND expiry_date<='" + currentDate + "'";
            else
                basicQuery += " AND (expiry_date BETWEEN '" + startDate + "' AND '" + endDate + "') ";
            Query q = entityManager.createNativeQuery(basicQuery, ProductBatchNo.class);
            productBatchNos = q.getResultList();
            for (ProductBatchNo batchNo : productBatchNos) {
                Long level_a_id = null;
                Long level_b_id = null;
                Long level_c_id = null;

                if (batchNo.getLevelA() != null)
                    level_a_id = batchNo.getLevelA().getId();
                if (batchNo.getLevelB() != null)
                    level_b_id = batchNo.getLevelB().getId();
                if (batchNo.getLevelC() != null)
                    level_c_id = batchNo.getLevelC().getId();

                Double closing = inventoryCommonPostings.getClosingStockProductFilters(branchId, users.getOutlet().getId(),
                        batchNo.getProduct().getId(), level_a_id, level_b_id, level_c_id, batchNo.getUnits().getId(),
                        batchNo.getId(), fiscalId);

                JsonObject batchObject = new JsonObject();
                batchObject.addProperty("id", batchNo.getProduct().getId());
                batchObject.addProperty("product_name", batchNo.getProduct().getProductName());
                batchObject.addProperty("brand_name", batchNo.getProduct().getBrand() != null ? batchNo.getProduct().getBrand().getBrandName() : "");
                batchObject.addProperty("packaging", batchNo.getProduct().getPackingMaster() != null ? batchNo.getProduct().getPackingMaster().getPackName() : "");
                batchObject.addProperty("companyName", batchNo.getOutlet() != null ? batchNo.getOutlet().getCompanyName() : "");
                batchObject.addProperty("unit_name", batchNo.getUnits() != null ? batchNo.getUnits().getUnitName() : "");
                batchObject.addProperty("qty", closing);
                batchObject.addProperty("batchno", batchNo.getBatchNo());
                batchObject.addProperty("batchid", batchNo.getId());
                batchObject.addProperty("mfgDate", batchNo.getManufacturingDate() != null ?
                        batchNo.getManufacturingDate().toString() : "");
                batchObject.addProperty("expiryDate", batchNo.getExpiryDate() != null ?
                        batchNo.getExpiryDate().toString() : "");
                jsonArray.add(batchObject);
            }
            finalResult.add("data", jsonArray);
            finalResult.addProperty("companyName", users.getOutlet().getCompanyName());
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            finalResult.addProperty("companyName", users.getOutlet().getCompanyName());
            finalResult.addProperty("message", "Failed to load data");
            finalResult.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return finalResult;
    }

    public JsonObject getExpiryProductsByBatch(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        try {
            Long batchId = Long.valueOf(request.getParameter("batchId"));

            ProductBatchNo batchNo = productBatchNoRepository.findByIdAndStatus(batchId, true);
            if (batchNo != null) {
                JsonObject data = new JsonObject();
                data.addProperty("costing", batchNo.getCosting());
                data.addProperty("purchaseRate", batchNo.getPurchaseRate());
                data.addProperty("groupName", batchNo.getProduct().getGroup() != null ?
                        batchNo.getProduct().getGroup().getGroupName() : "NA");
                data.addProperty("subGroupName", batchNo.getProduct().getSubgroup() != null ?
                        batchNo.getProduct().getSubgroup().getSubgroupName() : "NA");
                data.addProperty("categoryName", batchNo.getProduct().getCategory() != null ?
                        batchNo.getProduct().getCategory().getCategoryName() : "NA");
                data.addProperty("productHsn", batchNo.getProduct().getProductHsn() != null ?
                        batchNo.getProduct().getProductHsn().getHsnNumber() : "NA");
                data.addProperty("taxType", batchNo.getProduct().getTaxType() != null ?
                        batchNo.getProduct().getTaxType().toUpperCase() : "NA");
                data.addProperty("taxPer", batchNo.getProduct().getTaxMaster() != null ?
                        batchNo.getProduct().getTaxMaster().getIgst().toString() : "NA");
                data.addProperty("minStock", batchNo.getProduct().getMinStock() != null ?
                        batchNo.getProduct().getMinStock().toString() : "NA");
                data.addProperty("maxStock", batchNo.getProduct().getMaxStock() != null ?
                        (String) batchNo.getProduct().getMaxStock().toString() : "NA");
                data.addProperty("shelfId", batchNo.getProduct().getShelfId() != null ?
                        batchNo.getProduct().getShelfId() : "NA");
                data.addProperty("marginPer", batchNo.getMinMargin() != null ?
                        batchNo.getMinMargin().toString() : "NA");

                response.add("response", data);
                response.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                response.addProperty("message", "No data found");
                response.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.addProperty("message", "Failed to load data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public InputStream exportExcelNearExpiryProduct(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            System.out.println("productBatchNos size:" + productBatchNos.size());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"BRAND NAME", "PRODUCT NAME", "PACKING", "BATCH NO", "EXP.", "QTY", "UNIT"};
                    if (mfgShow)
                        headers = new String[]{"BRAND NAME", "PRODUCT NAME", "PACKING", "BATCH NO", "MFG.", "EXP.", "QTY", "UNIT"};
                    Sheet sheet = workbook.createSheet("nearly_expiry_products");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("brand_name").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("product_name").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("packaging").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("batchno").getAsString());
                        if (mfgShow) {
                            row.createCell(4).setCellValue(batchNo.get("mfgDate").getAsString());
                            row.createCell(5).setCellValue(batchNo.get("expiryDate").getAsString());
                            row.createCell(6).setCellValue(batchNo.get("qty").getAsDouble());
                            row.createCell(7).setCellValue(batchNo.get("unit_name").getAsString());
                        } else {
                            row.createCell(4).setCellValue(batchNo.get("expiryDate").getAsString());
                            row.createCell(5).setCellValue(batchNo.get("qty").getAsDouble());
                            row.createCell(6).setCellValue(batchNo.get("unit_name").getAsString());
                        }

                        sumOfQty += batchNo.get("qty").getAsDouble();
                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(5);
                    cell.setCellValue(sumOfQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    //API fo Near expiry csv screen-1
    public void exportCsvNearExpiry1(Map<String, String> jsonRequest, HttpServletRequest request, PrintWriter writer) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
            System.out.println("mfgShow" + jsonRequest);

            if (!mfgShow)
                printer.printRecord("BRAND NAME", "PRODUCT NAME", "PACKING", "BATCH NO", "MFG.", "EXP.", "QTY", "UNIT");
            else
                printer.printRecord("BRAND NAME", "PRODUCT NAME", "PACKING", "BATCH NO", "EXP.", "QTY", "UNIT");

            if (productBatchNos.size() > 0) {

                for (int j = 0; j < productBatchNos.size(); j++) {

                    JsonObject prodArr = productBatchNos.get(j).getAsJsonObject();
//                JsonArray prodArray = prodArr.get("product_unit_data").getAsJsonArray();

                    if (!mfgShow) {
//                    for (int i = 0; i < productBatchNos.size(); i++) {
//                        JsonObject prodArray1 = prodArr.get(i).getAsJsonObject();

                        printer.printRecord(prodArr.get(("brand_name")).getAsString(), prodArr.get("product_name").getAsString(), prodArr.get("packaging").getAsString(), prodArr.get("batchno").getAsString(), prodArr.get("mfgDate").getAsString(), prodArr.get("expiryDate").getAsString(), prodArr.get("qty").getAsString(), prodArr.get("unit_name").getAsString());

                    } else {
                        printer.printRecord(prodArr.get(("brand_name")).getAsString(), prodArr.get("product_name").getAsString(), prodArr.get("packaging").getAsString(), prodArr.get("batchno").getAsString(), prodArr.get("expiryDate").getAsString(), prodArr.get("qty").getAsString(), prodArr.get("unit_name").getAsString());
                    }
                }
            }


        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public InputStream exportCSVNearExpiryProduct(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            System.out.println("productBatchNos size:" + productBatchNos.size());
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            String[] headers = {"BRAND NAME", "PRODUCT NAME", "PACKING", "BATCH NO", "EXP.", "QTY", "UNIT"};
            if (mfgShow)
                headers = new String[]{"BRAND NAME", "PRODUCT NAME", "PACKING", "BATCH NO", "MFG.", "EXP.", "QTY", "UNIT"};

            CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(headers);
//                    CSVFormat.DEFAULT.
//                    .setHeader(headers)
//                    .build();
            CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT);

            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    Sheet sheet = workbook.createSheet("nearly_expiry_products");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("brand_name").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("product_name").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("packaging").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("batchno").getAsString());
                        if (mfgShow) {
                            row.createCell(4).setCellValue(batchNo.get("mfgDate").getAsString());
                            row.createCell(5).setCellValue(batchNo.get("expiryDate").getAsString());
                            row.createCell(6).setCellValue(batchNo.get("qty").getAsDouble());
                            row.createCell(7).setCellValue(batchNo.get("unit_name").getAsString());
                        } else {
                            row.createCell(4).setCellValue(batchNo.get("expiryDate").getAsString());
                            row.createCell(5).setCellValue(batchNo.get("qty").getAsDouble());
                            row.createCell(6).setCellValue(batchNo.get("unit_name").getAsString());
                        }

                        sumOfQty += batchNo.get("qty").getAsDouble();
                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(5);
                    cell.setCellValue(sumOfQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }


    public InputStream exportExcelMaximumLevelPdct(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            System.out.println("productBatchNos size:" + productBatchNos.size());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"BRAND NAME", "PRODUCT NAME", "PACKING", "MAXIMUM LEVEL", "CLOSING STOCK", "EXTRA STOCK"};
                    if (mfgShow)
                        headers = new String[]{"BRAND NAME", "PRODUCT NAME", "PACKING", "MAXIMUM LEVEL", "CLOSING STOCK", "EXTRA STOCK"};
                    Sheet sheet = workbook.createSheet("Maximum level_products");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("brand").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("product_name").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("packing").getAsString());
//                        row.createCell(3).setCellValue(batchNo.get("batchno").getAsString());
                        if (mfgShow) {
//                            row.createCell(4).setCellValue(batchNo.get("mfgDate").getAsString());
//                            row.createCell(5).setCellValue(batchNo.get("expiryDate").getAsString());
                            row.createCell(3).setCellValue(batchNo.get("maximumStock").getAsDouble());
                            row.createCell(4).setCellValue(batchNo.get("closing_stocks").getAsString());
                            row.createCell(5).setCellValue(batchNo.get("closing_stocks").getAsInt() - batchNo.get("maximumStock").getAsInt());
                        } else {
//                            row.createCell(4).setCellValue(batchNo.get("expiryDate").getAsString());
                            row.createCell(3).setCellValue(batchNo.get("maximumStock").getAsDouble());
                            row.createCell(4).setCellValue(batchNo.get("closing_stocks").getAsString());
                            row.createCell(5).setCellValue(batchNo.get("closing_stocks").getAsInt() - batchNo.get("maximumStock").getAsInt());
                        }

                        sumOfQty += batchNo.get("maximumStock").getAsDouble();
                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(6);
                    cell.setCellValue(sumOfQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public InputStream exportExcelMaximumLevel2Pdct(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            System.out.println("productBatchNos size:" + productBatchNos.size());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"MONTH", " P.QYT", "P.UNIT", " P.VALUE", "S.QTY", "S.UNIT", "S.VALUE", "C.QTY", "C.UNIT", "C.VALUE"};
//                    if (mfgShow)
//                        headers = new String[]{"PARTICULAR", " P.QYT", "P.UNIT", " P.VALUE", "S.QTY", "S.UNIT", "S.VALUE", "C.QTY", "C.UNIT", "C.VALUE"};
                    Sheet sheet = workbook.createSheet("Maximum level_products");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
                        JsonArray pur1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("purchase");
                        JsonArray sale1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("sale");
                        JsonArray closing1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("closing");
                        JsonObject pur = pur1.get(0).getAsJsonObject();
                        JsonObject sale = sale1.get(0).getAsJsonObject();
                        JsonObject closing = closing1.get(0).getAsJsonObject();
                        System.out.println("batcNO  " + batchNo);
                        System.out.println("pur  " + pur);
                        System.out.println("sale  " + sale);
                        System.out.println("clsoing  " + closing);
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("month_name").getAsString());
                        row.createCell(1).setCellValue(pur.get("qty").getAsString());
                        row.createCell(2).setCellValue(pur.get("unit").getAsString());
                        row.createCell(3).setCellValue(pur.get("value").getAsString());

                        row.createCell(4).setCellValue(sale.get("qty").getAsString());
                        row.createCell(5).setCellValue(sale.get("unit").getAsString());
                        row.createCell(6).setCellValue(sale.get("value").getAsString());


                        row.createCell(7).setCellValue(closing.get("qty").getAsString());
                        row.createCell(8).setCellValue(closing.get("unit").getAsString());
                        row.createCell(9).setCellValue(closing.get("value").getAsString());


                        sumOfQty += closing.get("qty").getAsDouble();

                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(7);
                    cell.setCellValue(sumOfQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                            System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }


                }


            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }

    }

    public InputStream exportExcelMaximumLevel3Pdct(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            System.out.println("productBatchNos size:" + productBatchNos.size());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"Date", "Invoice No", "Particular", "Vocher Type", " P.QYT", "P.UNIT", " P.VALUE", "S.QTY", "S.UNIT", "S.VALUE", "C.QTY", "C.UNIT", "C.VALUE"};
//                    if (mfgShow)
//                        headers = new String[]{"PARTICULAR", " P.QYT", "P.UNIT", " P.VALUE", "S.QTY", "S.UNIT", "S.VALUE", "C.QTY", "C.UNIT", "C.VALUE"};
                    Sheet sheet = workbook.createSheet("Maximum level_products");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
//
                        System.out.println("batcNO  " + batchNo);
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("date").getAsString());  //date
                        row.createCell(1).setCellValue(batchNo.get("invoice_no").getAsString());  //inoice
                        row.createCell(2).setCellValue(batchNo.get("particular").getAsString());   //name
                        row.createCell(3).setCellValue(batchNo.get("voucher_type").getAsString());  //type
                        if (batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Purchase A/C")) {
                            row.createCell(4).setCellValue(batchNo.get("qty").getAsString());  //pur
                            row.createCell(5).setCellValue(batchNo.get("unit").getAsString());  //pur
                            row.createCell(6).setCellValue(batchNo.get("value").getAsString());  //pur
                        } else {
                            row.createCell(4).setCellValue("");
                            row.createCell(5).setCellValue("");
                            row.createCell(6).setCellValue("");
                        }

                        if (batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Sales A/C")) {
                            row.createCell(7).setCellValue(batchNo.get("qty").getAsString());  //sale
                            row.createCell(8).setCellValue(batchNo.get("unit").getAsString());  //sale
                            row.createCell(9).setCellValue(batchNo.get("value").getAsString());  //sale
                        } else {
                            row.createCell(7).setCellValue("");
                            row.createCell(8).setCellValue("");
                            row.createCell(9).setCellValue("");
                        }

                        row.createCell(10).setCellValue(batchNo.get("closing_qty").getAsString());
                        row.createCell(11).setCellValue(batchNo.get("unit").getAsString());
                        row.createCell(12).setCellValue(batchNo.get("closing_rate").getAsString());


//                        sumOfQty += batchNo.get("closing_qty").getAsDouble();

                    }
                    Row prow = sheet.createRow(rowIdx++);
//                    prow.createCell(0).setCellValue("Total");
//                    Cell cell = prow.createCell(10);
//                    cell.setCellValue(sumOfQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                            System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }


                }


            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }

    }


    public InputStream exportExcelExpiredProduct(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"BRAND NAME", "PRODUCT NAME", "PACKING", "BATCH NO", "EXP.", "QTY", "UNIT"};
                    if (mfgShow)
                        headers = new String[]{"BRAND NAME", "PRODUCT NAME", "PACKING", "BATCH NO", "MFG.", "EXP.", "QTY", "UNIT"};
                    Sheet sheet = workbook.createSheet("expired_products");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("brand_name").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("product_name").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("packaging").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("batchno").getAsString());
                        if (mfgShow) {
                            row.createCell(4).setCellValue(batchNo.get("mfgDate").getAsString());
                            row.createCell(5).setCellValue(batchNo.get("expiryDate").getAsString());
                            row.createCell(6).setCellValue(batchNo.get("qty").getAsDouble());
                            row.createCell(7).setCellValue(batchNo.get("unit_name").getAsString());
                        } else {
                            row.createCell(4).setCellValue(batchNo.get("expiryDate").getAsString());
                            row.createCell(5).setCellValue(batchNo.get("qty").getAsDouble());
                            row.createCell(6).setCellValue(batchNo.get("unit_name").getAsString());
                        }

                        sumOfQty += batchNo.get("qty").getAsDouble();
                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(5);
                    cell.setCellValue(sumOfQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public InputStream exportExcelMinLevelProduct(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"BRAND NAME", "PRODUCT NAME", "PACKING", "MIN. LEVEL", "CLOSING STOCK", "OPENING STOCK"};
                    if (mfgShow)
                        headers = new String[]{"BRAND NAME", "PRODUCT NAME", "PACKING", "MIN. LEVEL", "CLOSING STOCK", "OPENING STOCK"};
                    Sheet sheet = workbook.createSheet("min_level_products");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("brand").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("product_name").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("packing").getAsString());
//                        row.createCell(3).setCellValue(batchNo.get("batchno").getAsString());
                        if (mfgShow) {
//                            row.createCell(4).setCellValue(batchNo.get("mfgDate").getAsString());
//                            row.createCell(5).setCellValue(batchNo.get("expiryDate").getAsString());
                            row.createCell(3).setCellValue(batchNo.get("minimumStock").getAsDouble());
                            row.createCell(4).setCellValue(batchNo.get("closing_stocks").getAsInt());
                            row.createCell(5).setCellValue(batchNo.get("opening_stocks").getAsString());
                        } else {
//                            row.createCell(4).setCellValue(batchNo.get("expiryDate").getAsString());
                            row.createCell(3).setCellValue(batchNo.get("minimumStock").getAsDouble());
                            row.createCell(4).setCellValue(batchNo.get("closing_stocks").getAsInt());
                            row.createCell(5).setCellValue(batchNo.get("opening_stocks").getAsString());
                        }

                        sumOfQty += batchNo.get("minimumStock").getAsDouble();
                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(6);
                    cell.setCellValue(sumOfQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    //API for min Level stock Screen -2
    public InputStream exportExcelMinLevel2Product(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            System.out.println("---->>>>" + productBatchNos.size());
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"MONTH", "PUR. QTY", "PUR. UNIT", "PUR. VALUE", "SALE QTY", "SALE UNIT", "SALE VALUE"};
                    if (mfgShow)
                        headers = new String[]{"MONTH", "PUR. QTY", "PUR. UNIT", "PUR. VALUE", "SALE QTY", "SALE UNIT", "SALE VALUE", "ClOSING QTY", "CLOSING UNIT", "CLOSING VALUE"};
                    Sheet sheet = workbook.createSheet("min_level_products");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {

                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
                        JsonArray pur1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("purchase");
                        JsonArray sale1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("sale");
                        JsonArray closing1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("closing");
                        JsonObject pur = pur1.get(0).getAsJsonObject();
                        JsonObject sale = sale1.get(0).getAsJsonObject();
                        JsonObject closing = closing1.get(0).getAsJsonObject();
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("month_name").getAsString());
                        row.createCell(1).setCellValue(pur.get("qty").getAsInt());
                        row.createCell(2).setCellValue(pur.get("unit").getAsString());
                        row.createCell(3).setCellValue(pur.get("value").getAsString());

                        if (mfgShow) {
                            row.createCell(4).setCellValue(sale.get("qty").getAsString());
                            row.createCell(5).setCellValue(sale.get("unit").getAsString());
                            row.createCell(6).setCellValue(sale.get("value").getAsString());
                        } else {
                            row.createCell(4).setCellValue(sale.get("qty").getAsString());
                            row.createCell(5).setCellValue(sale.get("unit").getAsString());
                            row.createCell(6).setCellValue(sale.get("value").getAsString());
                        }

                        sumOfQty += sale.get("qty").getAsDouble();
                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(4);
                    cell.setCellValue(sumOfQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public InputStream exportExcelMinLevel3Product(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//
            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            System.out.println("---->>>>" + productBatchNos.size());
            System.out.println("productBatchNos " + productBatchNos);
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"DATE", "INVOICE NO.", "PARTICULARS", "VOUCHER TYPE", "PUR. QTY", "PUR. UNIT", "PUR. VALUE",
                            "SALE QTY", "SALE UNIT", "SALE VALUE",};

                    Sheet sheet = workbook.createSheet("min_level_products");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int sumOfSaleQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {

                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("date").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("invoice_no").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("particular").getAsString());

                        row.createCell(3).setCellValue(batchNo.get("tranx_type").getAsString());

                        if (batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Purchase A/C")) {
                            row.createCell(4).setCellValue(batchNo.get("qty").getAsInt());
                            row.createCell(5).setCellValue(batchNo.get("unit").getAsString());
                            row.createCell(6).setCellValue(batchNo.get("value").getAsDouble());
                        } else {
                            row.createCell(4).setCellValue("");
                            row.createCell(5).setCellValue("");
                            row.createCell(6).setCellValue("");
                        }


                        if (batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Sales A/C")) {
                            row.createCell(7).setCellValue(batchNo.get("qty").getAsString());
                            row.createCell(8).setCellValue(batchNo.get("unit").getAsString());
                            row.createCell(9).setCellValue(batchNo.get("value").getAsString());
                        } else {
                            row.createCell(7).setCellValue("");
                            row.createCell(8).setCellValue("");
                            row.createCell(9).setCellValue("");
                        }

                        if (batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Purchase A/C"))
                            sumOfQty += batchNo.get("qty").getAsDouble();   //for purchase
                        else {
                            sumOfSaleQty += batchNo.get("qty").getAsInt();
                        }
                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(4);
                    Cell cell1 = prow.createCell(7);
                    cell.setCellValue(sumOfQty);
                    cell1.setCellValue(sumOfSaleQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }


    //API for whole stock screen 1
    public InputStream exportExcelWholeStock1(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"BRAND NAME", "PRODCUT NAME", "PACKING", "CLOSING QUANTITY", "UNIT"};

                    Sheet sheet = workbook.createSheet("whole_stock_products");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int sumOfSaleQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {

                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
                        JsonArray prodArray = batchNo.get("product_unit_data").getAsJsonArray();
                        JsonObject prodUnit = null;

                        Row row = sheet.createRow(rowIdx++);
                        if (prodArray.size() == 0) {
                            prodUnit = null;
                            row.createCell(0).setCellValue(batchNo.get("brand_name").getAsString());
                            row.createCell(1).setCellValue(batchNo.get("product_name").getAsString());
                            row.createCell(2).setCellValue(batchNo.get("packaging").getAsString());
                            row.createCell(3).setCellValue("");
                            row.createCell(4).setCellValue("");

                        } else {
                            prodUnit = prodArray.get(0).getAsJsonObject();
                            row.createCell(0).setCellValue(batchNo.get("brand_name").getAsString());
                            row.createCell(1).setCellValue(batchNo.get("product_name").getAsString());
                            row.createCell(2).setCellValue(batchNo.get("packaging").getAsString());
                            row.createCell(3).setCellValue(prodUnit.get("qty").getAsInt());
                            row.createCell(4).setCellValue(prodUnit.get(("unit_name")).getAsString());
                        }


                        if (prodArray.size() != 0)
                            sumOfQty += prodUnit.get("qty").getAsDouble();
                        else sumOfQty = sumOfQty + 0;

                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(3);
                    cell.setCellValue(sumOfQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    //API for whole stock screen 1 CSV
    public void exportCsvWholeStock1(Map<String, String> jsonRequest, HttpServletRequest request, PrintWriter writer) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
            printer.printRecord("BRAND NAME", "PRODCUT NAME", "PACKING", "CLOSING QUANTITY", "UNIT");

//            int totalqty = 0;
            for (int j = 0; j < productBatchNos.size(); j++) {

                JsonObject prodArr = productBatchNos.get(j).getAsJsonObject();
                JsonArray prodArray = prodArr.get("product_unit_data").getAsJsonArray();


                if (prodArray.size() > 0) {
                    for (int i = 0; i < prodArray.size(); i++) {
                        JsonObject prodArray1 = prodArray.get(i).getAsJsonObject();

//                        totalqty = totalqty+ prodArray1.get("closing_stock").getAsInt();


                        printer.printRecord(prodArr.get(("brand_name")).getAsString(), prodArr.get("product_name").getAsString(), prodArr.get("packaging").getAsString(), prodArray1.get("closing_stock").getAsString(), prodArray1.get("unit_name").getAsString());

                    }
                } else {
                    printer.printRecord(prodArr.get("brand_name").getAsString(), prodArr.get("product_name").getAsString(), prodArr.get("packaging").getAsString(), "", "");
                }
            }

//            printer.printRecord("total ", totalqty);


        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    //API for whole stock screen 2
    public InputStream exportExcelWholeStock2(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            System.out.println("---->>>>" + productBatchNos.size());
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"MONTH", "PUR. QTY", "PUR. UNIT", "PUR. VALUE", "SALE QTY", "SALE UNIT", "SALE VALUE"};
                    Sheet sheet = workbook.createSheet("whole_stock_products");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {

                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
                        JsonArray pur1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("purchase");
                        JsonArray sale1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("sale");
                        JsonArray closing1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("closing");
                        JsonObject pur = pur1.get(0).getAsJsonObject();
                        JsonObject sale = sale1.get(0).getAsJsonObject();
                        JsonObject closing = closing1.get(0).getAsJsonObject();
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("month_name").getAsString());
                        row.createCell(1).setCellValue(pur.get("qty").getAsInt());
                        row.createCell(2).setCellValue(pur.get("unit").getAsString());
                        row.createCell(3).setCellValue(pur.get("value").getAsString());

                        row.createCell(4).setCellValue(sale.get("qty").getAsString());
                        row.createCell(5).setCellValue(sale.get("unit").getAsString());
                        row.createCell(6).setCellValue(sale.get("value").getAsString());

                        sumOfQty += sale.get("qty").getAsDouble();
                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(4);
                    cell.setCellValue(sumOfQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    //API for whole stock screen 2 csv
    public void exportCsvWholeStock2(Map<String, String> jsonRequest, HttpServletRequest request, PrintWriter writer) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
            printer.printRecord("MONTH", "PUR. QTY", "PUR. UNIT", "PUR. VALUE", "SALE QTY", "SALE UNIT", "SALE VALUE", "CLOSING QTY", "CLOSING UNIT", "CLOSING VALUE");
            System.out.println("---->>>>" + productBatchNos.size());

            for (int i = 0; i < productBatchNos.size(); i++) {
                JsonObject prodArr = productBatchNos.get(i).getAsJsonObject();
//                        System.out.println("prodArr  "+prodArr);

                JsonArray purArr = prodArr.get("responseObject").getAsJsonObject().getAsJsonArray("purchase");
                JsonObject purArr1 = purArr.get(0).getAsJsonObject();
                JsonArray saleArr = prodArr.get("responseObject").getAsJsonObject().getAsJsonArray("sale");
                JsonObject saleArr1 = saleArr.get(0).getAsJsonObject();
                JsonArray closingArr = prodArr.get("responseObject").getAsJsonObject().getAsJsonArray("closing");
                JsonObject closingArr1 = closingArr.get(0).getAsJsonObject();

                if (prodArr.size() > 0) {
                    for (int j = 0; j < purArr.size(); j++) {
                        printer.printRecord(prodArr.get("month_name").getAsString(), purArr1.get("qty").getAsInt(), purArr1.get("unit").getAsString(), purArr1.get("value").getAsDouble(),
                                saleArr1.get("qty").getAsInt(), saleArr1.get("unit").getAsString(), saleArr1.get("value").getAsDouble(),
                                closingArr1.get("qty").getAsInt(), closingArr1.get("unit").getAsString(), closingArr1.get("value").getAsDouble());
                    }
                }

            }

        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    //API for whole stock screen -3
    public InputStream exportExcelWholeStock3(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//
            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            System.out.println("---->>>>" + productBatchNos.size());
            System.out.println("productBatchNos " + productBatchNos);
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"DATE", "INVOICE NO.", "PARTICULARS", "VOUCHER TYPE", "PUR. QTY", "PUR. UNIT", "PUR. VALUE",
                            "SALE QTY", "SALE UNIT", "SALE VALUE",};

                    Sheet sheet = workbook.createSheet("whole_stock_products");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int sumOfSaleQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {

                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("date").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("invoice_no").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("particular").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("tranx_type").getAsString());
                        if (batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Purchase A/C")) {
                            row.createCell(4).setCellValue(batchNo.get("qty").getAsInt());
                            row.createCell(5).setCellValue(batchNo.get("unit").getAsString());
                            row.createCell(6).setCellValue(batchNo.get("value").getAsDouble());
                        } else {
                            row.createCell(4).setCellValue("");
                            row.createCell(5).setCellValue("");
                            row.createCell(6).setCellValue("");
                        }


                        if (batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Sales A/C")) {
                            row.createCell(7).setCellValue(batchNo.get("qty").getAsString());
                            row.createCell(8).setCellValue(batchNo.get("unit").getAsString());
                            row.createCell(9).setCellValue(batchNo.get("value").getAsString());
                        } else {
                            row.createCell(7).setCellValue("");
                            row.createCell(8).setCellValue("");
                            row.createCell(9).setCellValue("");
                        }

                        if (batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Purchase A/C"))
                            sumOfQty += batchNo.get("qty").getAsDouble();   //for purchase
                        else {
                            sumOfSaleQty += batchNo.get("qty").getAsInt();
                        }
                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(4);
                    Cell cell1 = prow.createCell(7);
                    cell.setCellValue(sumOfQty);
                    cell1.setCellValue(sumOfSaleQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    //API for whole stock screen 3 csv
    public void exportCsvWholeStock3(Map<String, String> jsonRequest, HttpServletRequest request, PrintWriter writer) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos " + productBatchNos);
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
            printer.printRecord("DATE", "INVOICE NO.", "PARTICULARS", "VOUCHER TYPE", "PUR. QTY", "PUR. UNIT", "PUR. VALUE",
                    "SALE QTY", "SALE UNIT", "SALE VALUE", "CLOSING QTY", "CLOSING UNIT", "CLOSING VALUE");

            for (int i = 0; i < productBatchNos.size(); i++) {
                JsonObject consumerArr = productBatchNos.get(i).getAsJsonObject();
                System.out.println("consumerArr  " + consumerArr);
                if (consumerArr.size() > 0) {
                    printer.printRecord(consumerArr.get("date").getAsString(),
                            consumerArr.keySet().contains("invoice_no") ? consumerArr.get("invoice_no").getAsString() : " ",
                            consumerArr.keySet().contains("particular") ? consumerArr.get("particular").getAsString() : " ",
                            consumerArr.keySet().contains("voucher_type") ? consumerArr.get("voucher_type").getAsString() : " ",
                            consumerArr.keySet().contains("voucher_type") && consumerArr.get("voucher_type").getAsString().equals("Purchase A/C") ? consumerArr.get("qty").getAsInt() : " ",  //for purchase
                            consumerArr.keySet().contains("voucher_type") && consumerArr.get("voucher_type").getAsString().equals("Purchase A/C") ? consumerArr.get("unit").getAsString() : " ",   //for purchase
                            consumerArr.keySet().contains("voucher_type") && consumerArr.keySet().contains("value") && consumerArr.get("voucher_type").getAsString().equals("Purchase A/C") ? consumerArr.get("value").getAsInt() : " ", //for purchase
                            consumerArr.keySet().contains("voucher_type") && consumerArr.get("voucher_type").getAsString().equals("Sales A/C") ? consumerArr.get("qty").getAsInt() : " ", //for Sales
                            consumerArr.keySet().contains("voucher_type") && consumerArr.get("voucher_type").getAsString().equals("Sales A/C") ? consumerArr.get("unit").getAsString() : " ",//for Sales
                            consumerArr.keySet().contains("value") && consumerArr.get("voucher_type").getAsString().equals("Sales A/C") ? consumerArr.get("value").getAsDouble() : " ",//for Sales
                            consumerArr.get("closing_qty").getAsInt(), consumerArr.get("unit").getAsString(), consumerArr.get("closing_rate").getAsDouble()
                    );
                }


            }

        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }


    //API for Near Expiry Screen -2
    public InputStream exportExcelNearExpiry2(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {

            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"MONTH", "PUR. QTY", "PUR. UNIT", "PUR. VALUE", "SALE QTY", "SALE UNIT", "SALE VALUE", "ClOSING QTY", "CLOSING UNIT", "CLOSING VALUE"};
                    Sheet sheet = workbook.createSheet("near_expiry_products");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int sumOfPur = 0;
                    int sumOfClosing = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {

                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
                        JsonArray pur1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("purchase");
                        JsonArray sale1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("sale");
                        JsonArray closing1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("closing");
                        JsonObject pur = pur1.get(0).getAsJsonObject();
                        JsonObject sale = sale1.get(0).getAsJsonObject();
                        JsonObject closing = closing1.get(0).getAsJsonObject();
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("month_name").getAsString());
                        row.createCell(1).setCellValue(pur.get("qty").getAsInt());
                        row.createCell(2).setCellValue(pur.get("unit").getAsString());
                        row.createCell(3).setCellValue(pur.get("value").getAsString());
                        //for sale stock
                        row.createCell(4).setCellValue(sale.get("qty").getAsString());
                        row.createCell(5).setCellValue(sale.get("unit").getAsString());
                        row.createCell(6).setCellValue(sale.get("value").getAsString());
                        //for closing stock
                        row.createCell(7).setCellValue(batchNo.get("closing_stock").getAsString());
                        row.createCell(8).setCellValue(closing.get("unit").getAsString());
                        row.createCell(9).setCellValue(closing.get("value").getAsString());


                        sumOfQty += sale.get("qty").getAsDouble();
                        sumOfPur += pur.get("qty").getAsInt();

                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(4);
                    Cell cell1 = prow.createCell(1);

                    cell.setCellValue(sumOfQty);
                    cell1.setCellValue(sumOfPur);


                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);

                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    //API for Near Expiry stock screen 2 csv

    public void exportCsvNearExpiry2(Map<String, String> jsonRequest, HttpServletRequest request, PrintWriter writer) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
            printer.printRecord("MONTH", "PUR. QTY", "PUR. UNIT", "PUR. VALUE", "SALE QTY", "SALE UNIT", "SALE VALUE", "CLOSING QTY", "CLOSING UNIT", "CLOSING VALUE");
            System.out.println("---->>>>" + productBatchNos.size());

            for (int i = 0; i < productBatchNos.size(); i++) {
                JsonObject prodArr = productBatchNos.get(i).getAsJsonObject();
                System.out.println("prodArr  " + prodArr);

                JsonArray purArr = prodArr.get("responseObject").getAsJsonObject().getAsJsonArray("purchase");
                JsonObject purArr1 = purArr.get(0).getAsJsonObject();
                JsonArray saleArr = prodArr.get("responseObject").getAsJsonObject().getAsJsonArray("sale");
                JsonObject saleArr1 = saleArr.get(0).getAsJsonObject();
                JsonArray closingArr = prodArr.get("responseObject").getAsJsonObject().getAsJsonArray("closing");
                JsonObject closingArr1 = closingArr.get(0).getAsJsonObject();


                if (prodArr.size() > 0) {
                    for (int j = 0; j < purArr.size(); j++) {
                        printer.printRecord(prodArr.get("month_name").getAsString(), purArr1.get("qty").getAsInt(), purArr1.get("unit").getAsString(), purArr1.get("value").getAsDouble(),
                                saleArr1.get("qty").getAsInt(), saleArr1.get("unit").getAsString(), saleArr1.get("value").getAsDouble(),
                                closingArr1.get("qty").getAsInt(), closingArr1.get("unit").getAsString(), closingArr1.get("value").getAsDouble());
                    }
                }

            }

        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    //API for Near Expiry -3
    public InputStream exportExcelNearExpiry3(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//
            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            System.out.println("---->>>>" + productBatchNos.size());
            System.out.println("productBatchNos " + productBatchNos);
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"DATE", "INVOICE NO.", "PARTICULARS", "VOUCHER TYPE", "PUR. QTY", "PUR. UNIT", "PUR. VALUE",
                            "SALE QTY", "SALE UNIT", "SALE VALUE",};

                    Sheet sheet = workbook.createSheet("whole_stock_products");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int sumOfSaleQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {

                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("date").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("invoice_no").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("particular").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("tranx_type").getAsString());
                        if (batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Purchase A/C")) {
                            row.createCell(4).setCellValue(batchNo.get("qty").getAsInt());
                            row.createCell(5).setCellValue(batchNo.get("unit").getAsString());
                            row.createCell(6).setCellValue(batchNo.get("value").getAsDouble());
                        } else {
                            row.createCell(4).setCellValue("");
                            row.createCell(5).setCellValue("");
                            row.createCell(6).setCellValue("");
                        }


                        if (batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Sales A/C")) {
                            row.createCell(7).setCellValue(batchNo.get("qty").getAsString());
                            row.createCell(8).setCellValue(batchNo.get("unit").getAsString());
                            row.createCell(9).setCellValue(batchNo.get("value").getAsString());
                        } else {
                            row.createCell(7).setCellValue("");
                            row.createCell(8).setCellValue("");
                            row.createCell(9).setCellValue("");
                        }

                        if (batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Purchase A/C"))
                            sumOfQty += batchNo.get("qty").getAsDouble();   //for purchase
                        else {
                            sumOfSaleQty += batchNo.get("qty").getAsInt();
                        }
                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(4);
                    Cell cell1 = prow.createCell(7);
                    cell.setCellValue(sumOfQty);
                    cell1.setCellValue(sumOfSaleQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    //API for whole stock screen 3 csv
//    public void exportCsvNearExpiry3(Map<String, String> jsonRequest, HttpServletRequest request, PrintWriter writer) {
//        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//        try {
//            String JsonToStr = jsonRequest.get("list");
//
//            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
//            System.out.println("productBatchNos "+productBatchNos);
//            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
//            printer.printRecord("DATE", "INVOICE NO.", "PARTICULARS", "VOUCHER TYPE", "PUR. QTY","PUR. UNIT","PUR. VALUE",
//                    "SALE QTY","SALE UNIT", "SALE VALUE","CLOSING QTY","CLOSING UNIT","CLOSING VALUE");
//
//            for (int i = 0; i < productBatchNos.size(); i++) {
//                JsonObject consumerArr=productBatchNos.get(i).getAsJsonObject();
//                System.out.println("consumerArr  "+consumerArr);
//                if(consumerArr.size() >0){
//                    printer.printRecord(consumerArr.get("date").getAsString(),
//                            consumerArr.keySet().contains("invoice_no")? consumerArr.get("invoice_no").getAsString(): " ",
//                            consumerArr.keySet().contains("particular") ? consumerArr.get("particular").getAsString() : " ",
//                            consumerArr.keySet().contains("voucher_type") ? consumerArr.get("voucher_type").getAsString() : " ",
//                            consumerArr.keySet().contains("voucher_type") && consumerArr.get("voucher_type").getAsString().equals("Purchase A/C") ? consumerArr.get("qty").getAsInt() :" ",  //for purchase
//                            consumerArr.keySet().contains("voucher_type") && consumerArr.get("voucher_type").getAsString().equals("Purchase A/C") ? consumerArr.get("unit").getAsString() : " ",   //for purchase
//                            consumerArr.keySet().contains("voucher_type") && consumerArr.keySet().contains("value") && consumerArr.get("voucher_type").getAsString().equals("Purchase A/C") ? consumerArr.get("value").getAsInt():" ", //for purchase
//                            consumerArr.keySet().contains("voucher_type") && consumerArr.get("voucher_type").getAsString().equals("Sales A/C") ? consumerArr.get("qty").getAsInt():" ", //for Sales
//                            consumerArr.keySet().contains("voucher_type") && consumerArr.get("voucher_type").getAsString().equals("Sales A/C") ? consumerArr.get("unit").getAsString():" ",//for Sales
//                            consumerArr.keySet().contains("value") && consumerArr.get("voucher_type").getAsString().equals("Sales A/C") ? consumerArr.get("value").getAsDouble():" ",//for Sales
//                            consumerArr.get("closing_qty").getAsInt(),consumerArr.get("unit").getAsString(),consumerArr.get("closing_rate").getAsDouble()
//                    );
//                }
//
//
//            }
//
//        } catch (Exception e) {
//            productLogger.error("Failed to load near expiry of products data in excel " + e);
//            e.printStackTrace();
//            System.out.println("Exception " + e.getMessage());
//            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
//        }
//    }


    public InputStream exportExcelExpiredProduct2(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            System.out.println("productBatchNos size:" + productBatchNos.size());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"MONTH", " P.QYT", "P.UNIT", " P.VALUE", "S.QTY", "S.UNIT", "S.VALUE", "C.QTY", "C.UNIT", "C.VALUE"};
//                    if (mfgShow)
//                        headers = new String[]{"PARTICULAR", " P.QYT", "P.UNIT", " P.VALUE", "S.QTY", "S.UNIT", "S.VALUE", "C.QTY", "C.UNIT", "C.VALUE"};
                    Sheet sheet = workbook.createSheet(" expired_products");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
                        JsonArray pur1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("purchase");
                        JsonArray sale1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("sale");
                        JsonArray closing1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("closing");
                        JsonObject pur = pur1.get(0).getAsJsonObject();
                        JsonObject sale = sale1.get(0).getAsJsonObject();
                        JsonObject closing = closing1.get(0).getAsJsonObject();
                        System.out.println("batcNO  " + batchNo);
                        System.out.println("pur  " + pur);
                        System.out.println("sale  " + sale);
                        System.out.println("clsoing  " + closing);
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("month_name").getAsString());
                        row.createCell(1).setCellValue(pur.get("qty").getAsString());
                        row.createCell(2).setCellValue(pur.get("unit").getAsString());
                        row.createCell(3).setCellValue(pur.get("value").getAsString());

                        row.createCell(4).setCellValue(sale.get("qty").getAsString());
                        row.createCell(5).setCellValue(sale.get("unit").getAsString());
                        row.createCell(6).setCellValue(sale.get("value").getAsString());


                        row.createCell(7).setCellValue(closing.get("qty").getAsString());
                        row.createCell(8).setCellValue(closing.get("unit").getAsString());
                        row.createCell(9).setCellValue(closing.get("value").getAsString());


                        sumOfQty += closing.get("qty").getAsDouble();

                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(7);
                    cell.setCellValue(sumOfQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                            System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }


                }


            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }

    }


    public InputStream exportExcelExpiredProduct3(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            System.out.println("productBatchNos size:" + productBatchNos.size());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"Date", "Invoice No", "Particular", "Vocher Type", " P.QYT", "P.UNIT", " P.VALUE", "S.QTY", "S.UNIT", "S.VALUE", "C.QTY", "C.UNIT", "C.VALUE"};
//                    if (mfgShow)
//                        headers = new String[]{"PARTICULAR", " P.QYT", "P.UNIT", " P.VALUE", "S.QTY", "S.UNIT", "S.VALUE", "C.QTY", "C.UNIT", "C.VALUE"};
                    Sheet sheet = workbook.createSheet(" expired_products");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
//
                        System.out.println("batcNO  " + batchNo);
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("date").getAsString());  //date
                        row.createCell(1).setCellValue(batchNo.get("invoice_no").getAsString());  //inoice
                        row.createCell(2).setCellValue(batchNo.get("particular").getAsString());   //name
                        row.createCell(3).setCellValue(batchNo.get("voucher_type").getAsString());  //type
                        if (batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Purchase A/C")) {
                            row.createCell(4).setCellValue(batchNo.get("qty").getAsString());  //pur
                            row.createCell(5).setCellValue(batchNo.get("unit").getAsString());  //pur
                            row.createCell(6).setCellValue(batchNo.get("value").getAsString());  //pur
                        } else {
                            row.createCell(4).setCellValue("");
                            row.createCell(5).setCellValue("");
                            row.createCell(6).setCellValue("");
                        }

                        if (batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Sales A/C")) {
                            row.createCell(7).setCellValue(batchNo.get("qty").getAsString());  //sale
                            row.createCell(8).setCellValue(batchNo.get("unit").getAsString());  //sale
                            row.createCell(9).setCellValue(batchNo.get("value").getAsString());  //sale
                        } else {
                            row.createCell(7).setCellValue("");
                            row.createCell(8).setCellValue("");
                            row.createCell(9).setCellValue("");
                        }

                        row.createCell(10).setCellValue(batchNo.get("qty").getAsString());
                        row.createCell(11).setCellValue(batchNo.get("unit").getAsString());
                        row.createCell(12).setCellValue(batchNo.get("value").getAsString());


//                        sumOfQty += batchNo.get("qty").getAsDouble();

                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(10);
                    cell.setCellValue(sumOfQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                            System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }


                }


            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }

    }


    //APi for export excel available stock-1
    public InputStream exportExcelAvailableProduct(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            System.out.println("---->>>>" + productBatchNos.size());
            System.out.println("productBatchNos " + productBatchNos);
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"BRAND NAME", "PRODCUT NAME", "PACKING", "CLOSING QUANTITY", "UNIT"};

                    Sheet sheet = workbook.createSheet("available_stock_products");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int sumOfSaleQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {

                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
                        JsonArray prodArray = batchNo.get("product_unit_data").getAsJsonArray();
                        JsonObject prodUnit = null;

                        System.out.println("prodArray size " + prodArray.size());
                        System.out.println("prodUnit Data " + prodArray);
                        Row row = sheet.createRow(rowIdx++);
                        if (prodArray.size() == 0) {
                            prodUnit = null;
                            row.createCell(0).setCellValue(batchNo.get("brand_name").getAsString());
                            row.createCell(1).setCellValue(batchNo.get("product_name").getAsString());
                            row.createCell(2).setCellValue(batchNo.get("packaging").getAsString());
                            row.createCell(3).setCellValue("");
                            row.createCell(4).setCellValue("");

                        } else {
                            prodUnit = prodArray.get(0).getAsJsonObject();
                            row.createCell(0).setCellValue(batchNo.get("brand_name").getAsString());
                            row.createCell(1).setCellValue(batchNo.get("product_name").getAsString());
                            row.createCell(2).setCellValue(batchNo.get("packaging").getAsString());
                            row.createCell(3).setCellValue(prodUnit.get("closing_stock").getAsInt());
                            row.createCell(4).setCellValue(prodUnit.get(("unit_name")).getAsString());
                        }


                        if (prodArray.size() != 0)
                            sumOfQty += prodUnit.get("closing_stock").getAsDouble();
                        else sumOfQty = sumOfQty + 0;

                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(3);
                    cell.setCellValue(sumOfQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    //API for Available stock screen 1 CSV
    public void exportCsvAvailableStock1(Map<String, String> jsonRequest, HttpServletRequest request, PrintWriter writer) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
            printer.printRecord("BRAND NAME", "PRODCUT NAME", "PACKING", "CLOSING QUANTITY", "UNIT");


            for (int j = 0; j < productBatchNos.size(); j++) {

                JsonObject prodArr = productBatchNos.get(j).getAsJsonObject();
//                System.out.println("prodArr "+prodArr);
                JsonArray prodArray = prodArr.get("product_unit_data").getAsJsonArray();
//                System.out.println("productBatchNos "+productBatchNos);
                if (prodArray.size() > 0) {
                    for (int i = 0; i < prodArray.size(); i++) {
                        JsonObject prodArray1 = prodArray.get(i).getAsJsonObject();
//                        System.out.println("prodArray1  "+prodArray1);
                        printer.printRecord(prodArr.get(("brand_name")).getAsString(), prodArr.get("product_name").getAsString(), prodArr.get("packaging").getAsString(), prodArray1.get("closing_stock").getAsString(), prodArray1.get("unit_name").getAsString());
                    }
                } else {
                    printer.printRecord(prodArr.get("brand_name").getAsString(), prodArr.get("product_name").getAsString(), prodArr.get("packaging").getAsString(), "", "");
                }
            }


        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    //APi for export excel available stock-2
    public InputStream exportExcelAvailableProduct2(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            System.out.println("productBatchNos size:" + productBatchNos.size());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"MONTH", " P.QYT", "P.UNIT", " P.VALUE", "S.QTY", "S.UNIT", "S.VALUE", "C.QTY", "C.UNIT", "C.VALUE"};
//                    if (mfgShow)
//                        headers = new String[]{"PARTICULAR", " P.QYT", "P.UNIT", " P.VALUE", "S.QTY", "S.UNIT", "S.VALUE", "C.QTY", "C.UNIT", "C.VALUE"};
                    Sheet sheet = workbook.createSheet("Maximum level_products");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
                        JsonArray pur1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("purchase");
                        JsonArray sale1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("sale");
                        JsonArray closing1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("closing");
                        JsonObject pur = pur1.get(0).getAsJsonObject();
                        JsonObject sale = sale1.get(0).getAsJsonObject();
                        JsonObject closing = closing1.get(0).getAsJsonObject();
                        System.out.println("batcNO  " + batchNo);
                        System.out.println("pur  " + pur);
                        System.out.println("sale  " + sale);
                        System.out.println("clsoing  " + closing);
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("month_name").getAsString());
                        row.createCell(1).setCellValue(pur.get("qty").getAsString());
                        row.createCell(2).setCellValue(pur.get("unit").getAsString());
                        row.createCell(3).setCellValue(pur.get("value").getAsString());

                        row.createCell(4).setCellValue(sale.get("qty").getAsString());
                        row.createCell(5).setCellValue(sale.get("unit").getAsString());
                        row.createCell(6).setCellValue(sale.get("value").getAsString());


                        row.createCell(7).setCellValue(closing.get("qty").getAsString());
                        row.createCell(8).setCellValue(closing.get("unit").getAsString());
                        row.createCell(9).setCellValue(closing.get("value").getAsString());


                        sumOfQty += closing.get("qty").getAsDouble();

                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(7);
                    cell.setCellValue(sumOfQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                            System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }


                }


            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }

    }

    //API for Available stock screen 2 csv
    public void exportCsvAvailableStock2(Map<String, String> jsonRequest, HttpServletRequest request, PrintWriter writer) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
            printer.printRecord("MONTH", "PUR. QTY", "PUR. UNIT", "PUR. VALUE", "SALE QTY", "SALE UNIT", "SALE VALUE", "CLOSING QTY", "CLOSING UNIT", "CLOSING VALUE");
//            System.out.println("---->>>>"+productBatchNos.size());

            for (int i = 0; i < productBatchNos.size(); i++) {
                JsonObject prodArr = productBatchNos.get(i).getAsJsonObject();
                System.out.println("prodArr  " + prodArr);

                JsonArray purArr = prodArr.get("responseObject").getAsJsonObject().getAsJsonArray("purchase");
                JsonObject purArr1 = purArr.get(0).getAsJsonObject();
                JsonArray saleArr = prodArr.get("responseObject").getAsJsonObject().getAsJsonArray("sale");
                JsonObject saleArr1 = saleArr.get(0).getAsJsonObject();
                JsonArray closingArr = prodArr.get("responseObject").getAsJsonObject().getAsJsonArray("closing");
                JsonObject closingArr1 = closingArr.get(0).getAsJsonObject();

                if (prodArr.size() > 0) {
                    for (int j = 0; j < purArr.size(); j++) {
                        printer.printRecord(prodArr.get("month_name").getAsString(), purArr1.get("qty").getAsInt(), purArr1.get("unit").getAsString(), purArr1.get("value").getAsDouble(),
                                saleArr1.get("qty").getAsInt(), saleArr1.get("unit").getAsString(), saleArr1.get("value").getAsDouble(),
                                closingArr1.get("qty").getAsInt(), closingArr1.get("unit").getAsString(), closingArr1.get("value").getAsDouble());
                    }
                }

            }

        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    //APi for export excel available stock-3
    public InputStream exportExcelAvailableProduct3(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            System.out.println("productBatchNos size:" + productBatchNos.size());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"Date", "Invoice No", "Particular", "Vocher Type", " P.QYT", "P.UNIT", " P.VALUE", "S.QTY", "S.UNIT", "S.VALUE", "C.QTY", "C.UNIT", "C.VALUE"};
//                    if (mfgShow)
//                        headers = new String[]{"PARTICULAR", " P.QYT", "P.UNIT", " P.VALUE", "S.QTY", "S.UNIT", "S.VALUE", "C.QTY", "C.UNIT", "C.VALUE"};
                    Sheet sheet = workbook.createSheet(" expired_products");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
//
                        System.out.println("batcNO  " + batchNo);
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("date").getAsString());  //date
                        row.createCell(1).setCellValue(batchNo.get("invoice_no").getAsString());  //inoice
                        row.createCell(2).setCellValue(batchNo.get("particular").getAsString());   //name
                        row.createCell(3).setCellValue(batchNo.get("voucher_type").getAsString());  //type
                        if (batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Purchase A/C")) {
                            row.createCell(4).setCellValue(batchNo.get("qty").getAsString());  //pur
                            row.createCell(5).setCellValue(batchNo.get("unit").getAsString());  //pur
                            row.createCell(6).setCellValue(batchNo.get("value").getAsString());  //pur
                        } else {
                            row.createCell(4).setCellValue("");
                            row.createCell(5).setCellValue("");
                            row.createCell(6).setCellValue("");
                        }

                        if (batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Sales A/C")) {
                            row.createCell(7).setCellValue(batchNo.get("qty").getAsString());  //sale
                            row.createCell(8).setCellValue(batchNo.get("unit").getAsString());  //sale
                            row.createCell(9).setCellValue(batchNo.get("value").getAsString());  //sale
                        } else {
                            row.createCell(7).setCellValue("");
                            row.createCell(8).setCellValue("");
                            row.createCell(9).setCellValue("");
                        }

                        row.createCell(10).setCellValue(batchNo.get("closing_qty").getAsString());
                        row.createCell(11).setCellValue(batchNo.get("unit").getAsString());
                        row.createCell(12).setCellValue(batchNo.get("closing_rate").getAsString());


//                        sumOfQty += batchNo.get("closing_qty").getAsDouble();

                    }
                    Row prow = sheet.createRow(rowIdx++);
//                    prow.createCell(0).setCellValue("Total");
//                    Cell cell = prow.createCell(10);
//                    cell.setCellValue(sumOfQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                            System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }


                }


            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }

    }

    //API for whole stock screen 3 csv
    public void exportCsvAvailableStock3(Map<String, String> jsonRequest, HttpServletRequest request, PrintWriter writer) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos " + productBatchNos);
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
            printer.printRecord("DATE", "INVOICE NO.", "PARTICULARS", "VOUCHER TYPE", "PUR. QTY", "PUR. UNIT", "PUR. VALUE",
                    "SALE QTY", "SALE UNIT", "SALE VALUE", "CLOSING QTY", "CLOSING UNIT", "CLOSING VALUE");

            for (int i = 0; i < productBatchNos.size(); i++) {
                JsonObject consumerArr = productBatchNos.get(i).getAsJsonObject();
                System.out.println("consumerArr  " + consumerArr);
                if (consumerArr.size() > 0) {
                    printer.printRecord(consumerArr.get("date").getAsString(),
                            consumerArr.keySet().contains("invoice_no") ? consumerArr.get("invoice_no").getAsString() : " ",
                            consumerArr.keySet().contains("particular") ? consumerArr.get("particular").getAsString() : " ",
                            consumerArr.keySet().contains("voucher_type") ? consumerArr.get("voucher_type").getAsString() : " ",
                            consumerArr.keySet().contains("voucher_type") && consumerArr.get("voucher_type").getAsString().equals("Purchase A/C") ? consumerArr.get("qty").getAsInt() : " ",  //for purchase
                            consumerArr.keySet().contains("voucher_type") && consumerArr.get("voucher_type").getAsString().equals("Purchase A/C") ? consumerArr.get("unit").getAsString() : " ",   //for purchase
                            consumerArr.keySet().contains("voucher_type") && consumerArr.keySet().contains("value") && consumerArr.get("voucher_type").getAsString().equals("Purchase A/C") ? consumerArr.get("value").getAsInt() : " ", //for purchase
                            consumerArr.keySet().contains("voucher_type") && consumerArr.get("voucher_type").getAsString().equals("Sales A/C") ? consumerArr.get("qty").getAsInt() : " ", //for Sales
                            consumerArr.keySet().contains("voucher_type") && consumerArr.get("voucher_type").getAsString().equals("Sales A/C") ? consumerArr.get("unit").getAsString() : " ",//for Sales
                            consumerArr.keySet().contains("value") && consumerArr.get("voucher_type").getAsString().equals("Sales A/C") ? consumerArr.get("value").getAsDouble() : " ",//for Sales
                            consumerArr.get("closing_qty").getAsInt(), consumerArr.get("unit").getAsString(), consumerArr.get("closing_rate").getAsDouble()
                    );
                }


            }

        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    //API for Batch expiry Excel screen-1
    public InputStream exportExcelBatchStockScreen1(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            System.out.println("jsonRequest " + jsonRequest);
            String JsonToStr = jsonRequest.get("list");

            Boolean mfg = Boolean.valueOf(jsonRequest.get("mfg"));
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            String[] headers = new String[]{""};
            System.out.println("productBatchNos size:" + productBatchNos.size());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {

                try (Workbook workbook = new XSSFWorkbook()) {
                    Sheet sheet = workbook.createSheet("BatchStock1ExcelSheet");

                    if (mfg == true) {
                        headers = new String[]{"BRAND NAME", "PRODUCT NAME", "PACKING", " BATCH NO", "MANUFACTURED DATE", "EXPIRY DATE", "QUANTITY", "UNIT"};

                    } else {
                        headers = new String[]{"BRAND NAME", "PRODUCT NAME", "PACKING", " BATCH NO", "EXPIRY DATE", "QUANTITY", "UNIT"};
                    }

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int rowIdx = 1;
                    int sumOfQty = 0;
                    int productSize = productBatchNos.size();
                    for (int i = 0; i < productSize; i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
                        JsonArray BatchArray = batchNo.get("product_unit_data").getAsJsonArray();
                        if (BatchArray.size() > 0) {
                            System.out.println("BatchArray" + BatchArray);
                            for (int j = 0; j < BatchArray.size(); j++) {
                                JsonObject BatchArrayData = BatchArray.get(j).getAsJsonObject();

                                if (mfg == true) {
                                    Row row = sheet.createRow(rowIdx++);
                                    row.createCell(0).setCellValue(batchNo.get("brand_name").getAsString());
                                    row.createCell(1).setCellValue(batchNo.get("product_name").getAsString());
                                    row.createCell(2).setCellValue(batchNo.get("packaging").getAsString());
                                    row.createCell(3).setCellValue(BatchArrayData.get("batchno").getAsInt());
                                    row.createCell(4).setCellValue(BatchArrayData.get("mfgDate").getAsString());
                                    row.createCell(5).setCellValue(BatchArrayData.get("ExpiryDate").getAsString());
                                    row.createCell(6).setCellValue(BatchArrayData.get("qty").getAsInt());
                                    row.createCell(7).setCellValue(BatchArrayData.get("unit_name").getAsString());
                                    int qty = BatchArrayData.get("qty").getAsInt();
                                    sumOfQty += qty;
                                } else {
                                    Row row = sheet.createRow(rowIdx++);
                                    row.createCell(0).setCellValue(batchNo.get("brand_name").getAsString());
                                    row.createCell(1).setCellValue(batchNo.get("product_name").getAsString());
                                    row.createCell(2).setCellValue(batchNo.get("packaging").getAsString());
                                    row.createCell(3).setCellValue(BatchArrayData.get("batchno").getAsInt());
                                    row.createCell(4).setCellValue(BatchArrayData.get("ExpiryDate").getAsString());
                                    row.createCell(5).setCellValue(BatchArrayData.get("qty").getAsInt());
                                    row.createCell(6).setCellValue(BatchArrayData.get("unit_name").getAsString());
                                    int qty = BatchArrayData.get("qty").getAsInt();
                                    sumOfQty += qty;
                                }


                            }
                        } else {
                            Row row = sheet.createRow(rowIdx++);
                            row.createCell(0).setCellValue(batchNo.get("brand_name").getAsString());
                            row.createCell(1).setCellValue(batchNo.get("product_name").getAsString());
                            row.createCell(2).setCellValue(batchNo.get("packaging").getAsString());
                        }

                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    prow.createCell(5).setCellValue(sumOfQty);


                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load batch stock1 data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    //API for Batch expiry csv screen-1
    public void exportCsvBatchStockScreen1(Map<String, String> jsonRequest, HttpServletRequest request, PrintWriter writer) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");

            Boolean mfg = Boolean.valueOf(jsonRequest.get("mfg"));
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
            System.out.println("productBatchNos size:" + productBatchNos.size());

            if (productBatchNos.size() > 0) {

                if (mfg == true)
                    printer.printRecord("BRAND NAME", "PRODUCT NAME", "PACKING", " BATCH NO", "MANUFACTURED DATE", "EXPIRY DATE", "QUANTITY", "UNIT");
                else
                    printer.printRecord("BRAND NAME", "PRODUCT NAME", "PACKING", " BATCH NO", "EXPIRY DATE", "QUANTITY", "UNIT");

                for (int i = 0; i < productBatchNos.size(); i++) {
                    JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
                    JsonArray BatchArray = batchNo.get("product_unit_data").getAsJsonArray();
                    if (BatchArray.size() > 0) {
                        System.out.println("BatchArray" + BatchArray);
                        for (int j = 0; j < BatchArray.size(); j++) {
                            JsonObject BatchArrayData = BatchArray.get(j).getAsJsonObject();
                            if(mfg == true)
                                printer.printRecord(batchNo.get("brand_name").getAsString(), batchNo.get("product_name").getAsString(), batchNo.get("packaging").getAsString(), BatchArrayData.get("batchno").getAsString(),
                                        BatchArrayData.get("mfgDate").getAsString(), BatchArrayData.get("ExpiryDate").getAsString(), BatchArrayData.get("qty").getAsInt(),
                                        BatchArrayData.get("unit_name").getAsString());
                            else
                                printer.printRecord(batchNo.get("brand_name").getAsString(), batchNo.get("product_name").getAsString(), batchNo.get("packaging").getAsString(), BatchArrayData.get("batchno").getAsString(),
                                        BatchArrayData.get("ExpiryDate").getAsString(), BatchArrayData.get("qty").getAsInt(),
                                        BatchArrayData.get("unit_name").getAsString());

                        }
                    }
                    else{
                        printer.printRecord(batchNo.get("brand_name").getAsString(), batchNo.get("product_name").getAsString(), batchNo.get("packaging").getAsString());
                    }

                }



            }
        } catch (Exception e) {
            productLogger.error("Failed to load batch stock1 data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        } }



    //API for Batch expiry Excel screen-2
    public InputStream exportExcelBatchStockScreen2(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");
            int opening_stock = Integer.parseInt(jsonRequest.get("opening_stock"));
            Double costing = Double.valueOf(jsonRequest.get("costing"));

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            System.out.println("---->>>>"+productBatchNos.size());
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"MONTH", "PUR. QTY", "PUR. UNIT", "PUR. VALUE", "SALE QTY","SALE UNIT","SALE VALUE","CLOSING QTY","CLOSING UNIT","CLOSING VALUE"};
//                    if (mfgShow)
//                        headers = new String[]{"MONTH", "PUR. QTY", "PUR. UNIT", "PUR. VALUE", "SALE QTY","SALE UNIT","SALE VALUE","ClOSING QTY","CLOSING UNIT","CLOSING VALUE"};
                    Sheet sheet = workbook.createSheet("BatchStock1ExcelSheet");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int PurSumOfQty = 0, SaleSumOfQty = 0;
                    Double PurSumOfvalue = 0.0, saleSumOfvalue = 0.0, ClosingSumOfqty = 0.0, ClosingSumOfvalue =0.0;

                    int rowIdx = 1;
                    Row OProw = sheet.createRow(rowIdx++);
                    JsonObject batchNo1 = productBatchNos.get(0).getAsJsonObject();
                    JsonArray purch1 = batchNo1.get("responseObject").getAsJsonObject().getAsJsonArray("purchase");
                    JsonObject purch = purch1.get(0).getAsJsonObject();
                    OProw.createCell(0).setCellValue("Opening Quantity");
                    OProw.createCell(7).setCellValue(opening_stock);
                    OProw.createCell(8).setCellValue(purch.get(("unit")).getAsString());
                    OProw.createCell(9).setCellValue(costing);
                    for (int i = 0; i < productBatchNos.size(); i++) {

                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
                        JsonArray pur1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("purchase");
                        JsonArray sale1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("sale");
                        JsonArray closing1 = batchNo.get("responseObject").getAsJsonObject().getAsJsonArray("closing");
                        JsonObject pur = pur1.get(0).getAsJsonObject();
                        JsonObject sale = sale1.get(0).getAsJsonObject();
                        JsonObject closing = closing1.get(0).getAsJsonObject();
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("month_name").getAsString());
                        row.createCell(1).setCellValue(pur.get("qty").getAsInt());
                        row.createCell(2).setCellValue(pur.get("unit").getAsString());
                        row.createCell(3).setCellValue(pur.get("value").getAsString());
                        row.createCell(4).setCellValue(sale.get("qty").getAsString());
                        row.createCell(5).setCellValue(sale.get("unit").getAsString());
                        row.createCell(6).setCellValue(sale.get("value").getAsString());
                        row.createCell(7).setCellValue(batchNo.get("closing_stock").getAsString());
                        row.createCell(8).setCellValue(closing.get("unit").getAsString());
                        row.createCell(9).setCellValue(batchNo.get("closing_value").getAsString());
                        PurSumOfQty += pur.get("qty").getAsDouble();
                        SaleSumOfQty += sale.get("qty").getAsDouble();
                        PurSumOfvalue += pur.get("value").getAsDouble();
                        saleSumOfvalue += sale.get("value").getAsDouble();
                        ClosingSumOfqty = Double.valueOf(batchNo.get("closing_stock").getAsString());
                        ClosingSumOfvalue = Double.valueOf(batchNo.get("closing_value").getAsString());

                    }


                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    prow.createCell(1).setCellValue(PurSumOfQty);
                    prow.createCell(3).setCellValue(PurSumOfvalue);
                    prow.createCell(4).setCellValue(SaleSumOfQty);
                    prow.createCell(6).setCellValue(saleSumOfvalue);
                    prow.createCell(7).setCellValue(ClosingSumOfqty);
                    prow.createCell(9).setCellValue(ClosingSumOfvalue);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load batch stock2 data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    //API for whole stock screen 2 csv
    public void exportCsvBatchStockScreen2(Map<String, String> jsonRequest, HttpServletRequest request, PrintWriter writer) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
            printer.printRecord("MONTH", "PUR. QTY", "PUR. UNIT", "PUR. VALUE", "SALE QTY", "SALE UNIT", "SALE VALUE", "CLOSING QTY", "CLOSING UNIT", "CLOSING VALUE");
            System.out.println("---->>>>" + productBatchNos.size());

            for (int i = 0; i < productBatchNos.size(); i++) {
                JsonObject prodArr = productBatchNos.get(i).getAsJsonObject();
//                        System.out.println("prodArr  "+prodArr);

                JsonArray purArr = prodArr.get("responseObject").getAsJsonObject().getAsJsonArray("purchase");
                JsonObject purArr1 = purArr.get(0).getAsJsonObject();
                JsonArray saleArr = prodArr.get("responseObject").getAsJsonObject().getAsJsonArray("sale");
                JsonObject saleArr1 = saleArr.get(0).getAsJsonObject();
                JsonArray closingArr = prodArr.get("responseObject").getAsJsonObject().getAsJsonArray("closing");
                JsonObject closingArr1 = closingArr.get(0).getAsJsonObject();

                if (prodArr.size() > 0) {
                    for (int j = 0; j < purArr.size(); j++) {
                        printer.printRecord(prodArr.get("month_name").getAsString(), purArr1.get("qty").getAsInt(), purArr1.get("unit").getAsString(), purArr1.get("value").getAsDouble(),
                                saleArr1.get("qty").getAsInt(), saleArr1.get("unit").getAsString(), saleArr1.get("value").getAsDouble(),
                                closingArr1.get("qty").getAsInt(), closingArr1.get("unit").getAsString(), closingArr1.get("value").getAsDouble());
                    }
                }

            }

        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    //API for Batch expiry Excel screen-3
    public InputStream exportExcelBatchStockScreen3(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            System.out.println("---->>>>"+productBatchNos.size());
            System.out.println("productBatchNos "+productBatchNos);
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"DATE", "INVOICE NO.", "PARTICULARS", "VOUCHER TYPE", "PUR. QTY","PUR. UNIT","PUR. VALUE",
                            "SALE QTY","SALE UNIT", "SALE VALUE","CLOSING QTY","CLOSING UNIT", "CLOSING VALUE"};

                    Sheet sheet = workbook.createSheet("BatchStock3ExcelSheet");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfPurQty = 0, sumOfSaleQty =0, total_closing_stk = 0;
                    Double sumOfPurValue = 0.0, sumOfSaleValue = 0.0;
                    int rowIdx = 1;
                    String tran_type = "";
                    for (int i = 0; i < productBatchNos.size(); i++) {

                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
                        tran_type = batchNo.get("tranx_unique_code").getAsString();
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("date").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("invoice_no").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("particular").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("voucher_type").getAsString());


                        if(tran_type.equalsIgnoreCase("PRS") || tran_type.equalsIgnoreCase("PRSRT") || tran_type.equalsIgnoreCase("PRSCHN")){
                            row.createCell(4).setCellValue(batchNo.get("qty").getAsInt());
                            row.createCell(5).setCellValue(batchNo.get("unit").getAsString());
                            row.createCell(6).setCellValue(batchNo.get("value").getAsDouble());
                        }
                        else if(tran_type.equalsIgnoreCase("SLS") || tran_type.equalsIgnoreCase("SLSRT") || tran_type.equalsIgnoreCase("SLCHN")){
                            row.createCell(7).setCellValue(batchNo.get("qty").getAsInt());
                            row.createCell(8).setCellValue(batchNo.get("unit").getAsString());
                            row.createCell(9).setCellValue(batchNo.get("value").getAsDouble());
//                            row.createCell(7).setCellValue("");
//                            row.createCell(8).setCellValue("");
//                            row.createCell(9).setCellValue("");
                        }
                        else{

                        }


//                        if(batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Sales A/C")){
//                            row.createCell(7).setCellValue(batchNo.get("qty").getAsString());
//                            row.createCell(8).setCellValue(batchNo.get("unit").getAsString());
//                            row.createCell(9).setCellValue(batchNo.get("value").getAsString());
//                        }

//                        else {
//                            row.createCell(7).setCellValue("");
//                            row.createCell(8).setCellValue("");
//                            row.createCell(9).setCellValue("");
//                        }

                        if(tran_type.equalsIgnoreCase("PRS") || tran_type.equalsIgnoreCase( "PRSCHN"))
                            sumOfPurQty = sumOfPurQty + batchNo.get("qty").getAsInt();   //for All purchase QTY
                        else if(tran_type.equalsIgnoreCase("PRSRT")) {
                            sumOfPurQty = sumOfPurQty - batchNo.get("qty").getAsInt();
                        }
                        if(tran_type.equalsIgnoreCase( "PRS" )|| tran_type.equalsIgnoreCase("PRSCHN"))
                            sumOfPurValue = sumOfPurValue +  batchNo.get("value").getAsDouble();   //for All purchase value
                        else if(tran_type.equalsIgnoreCase("PRSRT")) {
                            sumOfPurValue = sumOfPurValue - batchNo.get("value").getAsInt();
                        }

                        if(tran_type.equalsIgnoreCase("SLS")|| tran_type.equalsIgnoreCase("SLSCHN"))
                            sumOfSaleQty += batchNo.get("qty").getAsInt();   //for all sales qty
                        else if(tran_type.equalsIgnoreCase("SLSRT" )) {
                            sumOfSaleQty -= batchNo.get("qty").getAsInt();
                        }
                        if(tran_type.equalsIgnoreCase("SLS") || tran_type.equalsIgnoreCase("SLSCHN"))
                            sumOfSaleValue += batchNo.get("value").getAsInt();  //for all sales value
                        else if(tran_type.equalsIgnoreCase("SLSRT" )) {
                            sumOfSaleValue -= batchNo.get("value").getAsInt();
                        }
                        if(tran_type.equalsIgnoreCase("PRS") || tran_type.equalsIgnoreCase("PRSRT") || tran_type.equalsIgnoreCase("PRSCHN")) {
                            total_closing_stk = total_closing_stk + sumOfPurQty;
                            row.createCell(10).setCellValue(total_closing_stk);
                        }
                        else if(tran_type.equalsIgnoreCase("SLS") || tran_type.equalsIgnoreCase("SLSRT") || tran_type.equalsIgnoreCase("SLCHN")){
                            total_closing_stk = total_closing_stk - sumOfSaleQty;
                            row.createCell(10).setCellValue(total_closing_stk);
                        }

                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    prow.createCell(4).setCellValue(sumOfPurQty);
                    prow.createCell(6).setCellValue(sumOfPurValue);
                    prow.createCell(7).setCellValue(sumOfSaleQty);
                    prow.createCell(9).setCellValue(sumOfSaleValue);
                    prow.createCell(10).setCellValue(total_closing_stk);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load batch stock3 data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }


    //API for Batch stock screen 3 csv
    public void exportCsvBatchStockScreen3(Map<String, String> jsonRequest, HttpServletRequest request, PrintWriter writer) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos " + productBatchNos);
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
            printer.printRecord("DATE", "INVOICE NO.", "PARTICULARS", "VOUCHER TYPE", "PUR. QTY", "PUR. UNIT", "PUR. VALUE",
                    "SALE QTY", "SALE UNIT", "SALE VALUE", "CLOSING QTY", "CLOSING UNIT", "CLOSING VALUE");

            for (int i = 0; i < productBatchNos.size(); i++) {
                JsonObject consumerArr = productBatchNos.get(i).getAsJsonObject();
                System.out.println("consumerArr  " + consumerArr);
                if (consumerArr.size() > 0) {
                    printer.printRecord(consumerArr.get("date").getAsString(),
                            consumerArr.keySet().contains("invoice_no") ? consumerArr.get("invoice_no").getAsString() : " ",
                            consumerArr.keySet().contains("particular") ? consumerArr.get("particular").getAsString() : " ",
                            consumerArr.keySet().contains("voucher_type") ? consumerArr.get("voucher_type").getAsString() : " ",
                            consumerArr.keySet().contains("voucher_type") && consumerArr.get("voucher_type").getAsString().equals("Purchase A/C") ? consumerArr.get("qty").getAsInt() : " ",  //for purchase
                            consumerArr.keySet().contains("voucher_type") && consumerArr.get("voucher_type").getAsString().equals("Purchase A/C") ? consumerArr.get("unit").getAsString() : " ",   //for purchase
                            consumerArr.keySet().contains("voucher_type") && consumerArr.keySet().contains("value") && consumerArr.get("voucher_type").getAsString().equals("Purchase A/C") ? consumerArr.get("value").getAsInt() : " ", //for purchase
                            consumerArr.keySet().contains("voucher_type") && consumerArr.get("voucher_type").getAsString().equals("Sales A/C") ? consumerArr.get("qty").getAsInt() : " ", //for Sales
                            consumerArr.keySet().contains("voucher_type") && consumerArr.get("voucher_type").getAsString().equals("Sales A/C") ? consumerArr.get("unit").getAsString() : " ",//for Sales
                            consumerArr.keySet().contains("value") && consumerArr.get("voucher_type").getAsString().equals("Sales A/C") ? consumerArr.get("value").getAsDouble() : " ",//for Sales
                            consumerArr.get("closing_qty").getAsInt(), consumerArr.get("unit").getAsString(), consumerArr.get("closing_rate").getAsDouble()
                    );
                }


            }

        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public InputStream exportToExcelPurchaseReg(Map<String, String> jsonRequest, HttpServletRequest request) throws IOException {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos "+productBatchNos);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};

//                    if (mfgShow)
//                        headers = new String[]{"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};
                    Sheet sheet = workbook.createSheet("PurchaseRegister");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int rowIdx = 1;
                    JsonObject batchNo = null;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("transaction_date").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("particulars").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("voucher_type").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("voucher_no").getAsString());
                        row.createCell(4).setCellValue(batchNo.get("debit").getAsDouble());
                        row.createCell(5).setCellValue(batchNo.get("credit").getAsDouble());
//                        if (mfgShow) {
//                            row.createCell(0).setCellValue(batchNo.get("transaction_date").getAsString());
//                            row.createCell(1).setCellValue(batchNo.get("particulars").getAsString());
//                            row.createCell(2).setCellValue(batchNo.get("voucher_type").getAsString());
//                            row.createCell(3).setCellValue(batchNo.get("voucher_no").getAsString());
//                            row.createCell(4).setCellValue(batchNo.get("credit").getAsString());
//                            row.createCell(5).setCellValue(batchNo.get("debit").getAsString());
//                        } else {
//                            row.createCell(0).setCellValue(batchNo.get("transaction_date").getAsString());
//                            row.createCell(1).setCellValue(batchNo.get("particulars").getAsString());
//                            row.createCell(2).setCellValue(batchNo.get("voucher_type").getAsString());
//                            row.createCell(3).setCellValue(batchNo.get("voucher_no").getAsString());
//                            row.createCell(4).setCellValue(batchNo.get("credit").getAsString());
//                            row.createCell(5).setCellValue(batchNo.get("debit").getAsString());
//
//
//                        }
                        sumOfQty += batchNo.get("credit").getAsDouble();

                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(5);
                    cell.setCellValue(sumOfQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }


                }
            }
            return new ByteArrayInputStream(out.toByteArray());

        } catch(Exception e){
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

}
