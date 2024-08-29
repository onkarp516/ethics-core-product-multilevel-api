package in.truethics.ethics.ethicsapiv10.service.reports_service.dashboard;

import ch.qos.logback.classic.Level;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.GenerateFiscalYear;
import in.truethics.ethics.ethicsapiv10.common.InventoryCommonPostings;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.InventoryDetailsPostings;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductOpeningStocks;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductUnitPacking;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoiceDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurReturnInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoiceDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesReturnInvoice;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.ProductBatchNoRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.InventoryDetailsPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductOpeningStocksRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.FiscalYearRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurInvoiceDetailsUnitsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurInvoiceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurReturnDetailsUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesInvoiceDetailsUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesInvoiceRepository;
import in.truethics.ethics.ethicsapiv10.service.master_service.ProductService;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static ch.qos.logback.classic.Level.valueOf;
import static javax.xml.bind.DatatypeConverter.parseLong;
import static org.hibernate.criterion.Projections.sum;

@Service
public class StockValuationService {

    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    private static final Logger productLogger = LogManager.getLogger(ProductService.class);
    @Autowired
    private TranxPurInvoiceRepository tranxPurInvoiceRepository;
    @Autowired
    private ProductOpeningStocksRepository openingStocksRepository;
    @Autowired
    private TranxSalesInvoiceRepository tranxSalesInvoiceRepository;
    @Autowired
    FiscalYearRepository fiscalYearRepository;
    @Autowired
    private ProductUnitRepository productUnitRepository;
    @Autowired
    private InventoryDetailsPostingsRepository inventoryDetailsPostingsRepository;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private InventoryCommonPostings inventoryCommonPostings;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductBatchNoRepository productBatchNoRepository;
    @Autowired
    private TranxSalesInvoiceDetailsUnitRepository tranxSalesInvoiceDetailsUnitRepository;
    @Autowired
    private TranxPurReturnDetailsUnitRepository tranxPurReturnDetailsUnitRepository;
    @Autowired
    private TranxPurInvoiceDetailsUnitsRepository tranxPurInvoiceDetailsUnitsRepository;
    @Autowired
    private ProductOpeningStocksRepository productOpeningStocksRepository;


    public JsonObject getAllstockValuation(HttpServletRequest request) {
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
        String AvgRate = "";
        Double t_qty = 0.0;
        Double t_rate = 0.0;
        Double[] rate = new Double[0];
        for (Product mProduct : productList) {
            JsonObject mObject = new JsonObject();
            JsonArray productunitarray = new JsonArray();
            mObject.addProperty("id", mProduct.getId());
            mObject.addProperty("product_name", mProduct.getProductName());
            mObject.addProperty("brand_name", mProduct.getBrand().getBrandName());
            mObject.addProperty("packaging", mProduct.getPackingMaster() != null ? mProduct.getPackingMaster().getPackName() : "");
            mObject.addProperty("companyName", mProduct.getOutlet().getCompanyName());
            mObject.addProperty("group", mProduct.getGroup() != null ? mProduct.getGroup().getGroupName() : "");
            mObject.addProperty("sub_group", mProduct.getSubgroup() != null ? mProduct.getSubgroup().getSubgroupName() : "");
            mObject.addProperty("category", mProduct.getCategory() != null ? mProduct.getCategory().getCategoryName() : "");
            mObject.addProperty("shelfId", mProduct.getShelfId() != null ? mProduct.getShelfId() : "");
            mObject.addProperty("sub_category", mProduct.getSubcategory() != null ? mProduct.getSubcategory().getSubcategoryName() : "");

            mObject.addProperty("HSN", mProduct.getProductHsn() != null ? mProduct.getProductHsn().getHsnNumber() : "");
            mObject.addProperty("tax_type", mProduct.getTaxType());
            mObject.addProperty("tax", mProduct.getTaxMaster().getGst_per() != null ? mProduct.getTaxMaster().getGst_per() : "");
            mObject.addProperty("margin_per", mProduct.getMarginPer() != null ? mProduct.getMarginPer().toString() : "");
            mObject.addProperty("min_stock", mProduct.getMinStock() != null ? mProduct.getMinStock().toString() : "");
            mObject.addProperty("max_stock", mProduct.getMaxStock() != null ? mProduct.getMaxStock().toString() : "");

//            >>>>>>>>>
            String productid = mProduct.getId() != null ? String.valueOf(mProduct.getId()) : "";
            System.out.println("productid " + productid);
            String sums = tranxPurInvoiceDetailsUnitsRepository.findSumByProductId(productid, true);

            if (sums.length() > 0) {
                String[] arrSum = sums.split(",");

//           for (int i=0;i<arrSum.length;i++){
                mObject.addProperty("Qsum", arrSum[0]);
                mObject.addProperty("total_AmountSum", arrSum[1]);
//           }
            } else {
                mObject.addProperty("Qsum", "");
                mObject.addProperty("total_AmountSum", "");
            }

//<<<<<<<<<<<

            list = inventoryDetailsPostingsRepository.findProductDatabyPurchaseId(mProduct.getId(), true);
            double sum_rate = 0.0;
            for (int j = 0; j < list.size(); j++) {
                Object[] objects = list.get(j);
                Long inventoryId = Long.parseLong(objects[0].toString());
                InventoryDetailsPostings unitPacking = inventoryDetailsPostingsRepository.findByIdAndStatus(inventoryId, true);
                JsonObject productunitobject = new JsonObject();
                productunitobject.addProperty("row_id", unitPacking.getId());
                productunitobject.addProperty("unit_name", unitPacking.getUnits().getUnitName());
                productunitobject.addProperty("qty", unitPacking.getQty());
                productunitobject.addProperty("batchno", unitPacking.getUniqueBatchNo() != null ? unitPacking.getUniqueBatchNo() : "");
                productunitobject.addProperty("ExpiryDate", unitPacking.getProductBatch().getExpiryDate() != null ? unitPacking.getProductBatch().getExpiryDate().toString(): "");
                productunitobject.addProperty("batchid", unitPacking.getProductBatch() != null ? unitPacking.getProductBatch().getId().toString() : "");
                productunitobject.addProperty("bat_pur_rate", unitPacking.getProductBatch() != null ?
                        unitPacking.getProductBatch().getPurchaseRate().toString() != null ? unitPacking.getProductBatch().getPurchaseRate().toString() : "" : "");
                sum_rate = sum_rate + Double.valueOf(unitPacking.getProductBatch() != null ?
                        unitPacking.getProductBatch().getPurchaseRate().toString() != null ? unitPacking.getProductBatch().getPurchaseRate().toString() : "" : "");
//                String batchid = unitPacking.getProductBatch() != null ? unitPacking.getProductBatch().getId().toString() : ""; ;
//                Double costing = productBatchNoRepository.findByBatchIdAndStatus(parseLong(batchid), true);
//
//                productunitobject.addProperty("costing", costing!=null?costing:0.0);

                productunitobject.addProperty("mfgDate", unitPacking.getProductBatch() != null ?
                        (unitPacking.getProductBatch().getManufacturingDate() != null ? unitPacking.getProductBatch().getManufacturingDate().toString() : "") : "");
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
                t_qty = t_qty + closing;
//                if(productunitarray.size()>1){
//                    AvgRate = String. join(AvgRate, ",", rate);
//                }
            }
            t_rate = t_rate + (sum_rate != 0.0 ? sum_rate / list.size() : 0);
//            t_rate = t_rate + closing;

//            System.out.println("rates" + str[] );
//            Double sum =0.0;
//            for(int i=0;i<AvgRate.length();i++){
//                String str [] = AvgRate.split(",");
//                Double num = Double.parseDouble(str[i]);
//;                sum = sum(sum, num);
//            }
//
//            Double Average = sum/AvgRate.length();
//
//
//            productunitarray.add("Average");
//            mObject.addProperty("total_quqntity", t_qty);
            mObject.add("product_unit_data", productunitarray);
//            mObject.add("total_quqntity", t_qty);;
            jsonArray.add(mObject);
        }
//        finalResult.addProperty("array ", rate);
        finalResult.addProperty("total_closing_qty", t_qty);
        finalResult.addProperty("total_Rate", t_rate);
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("data", jsonArray);
        return finalResult;
    }

//    private Double sum(Double average, Double num) {
//       Double result  = average +num;
//       return result;
//    }

    /****** Stock Valuation and Available Stock Monthwise product details ********/
    public Object getMonthwiseStockValuationDetails(HttpServletRequest request) {
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
            JsonArray mainArr = new JsonArray();
            JsonArray innerArr = new JsonArray();
            while (startDatep.isBefore(endDatep)) {
                JsonObject monthObject = new JsonObject();
                JsonArray monthArray = new JsonArray();
                JsonObject mainObj = new JsonObject();
                JsonArray saleArray = new JsonArray();
                JsonArray closingArray = new JsonArray();
//                Double closing_bal = 0.0;
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
                    Double totalpValue = 0.0;
                    Double totalsValue = 0.0;
                    Double totalClosingqty = 0.0;
                    Double totalpQty = 0.0;
//                    Double totalpvalue = 0.0;
                    Double totalpurValuetotalpurRturnQty = 0.0;
                    Double totalsQty = 0.0;
                    String totalpurQty = "";
                    String totalPurRtrnQty = "";
                    String totalSaleQty = "";
                    String totalSaleRtrnQty = "";

                    /*****  Purchase *******/
                    JsonObject inside = new JsonObject();


//                    totalpQty = inventoryDetailsPostingsRepository.
//                            findByTotalQty(productId, "CR", mUnits.getUnits().getId(), startMonthDate.toString(), endMonthDate.toString());
                    totalpurQty = tranxPurInvoiceDetailsUnitsRepository.
                            findPurQtyByProductIdAndStatus(productId, true, startMonthDate.toString(), endMonthDate.toString());


                    totalPurRtrnQty = tranxPurReturnDetailsUnitRepository.
                            findPurReturnQtyByProductIdAndStatus(productId, true, startMonthDate.toString(), endMonthDate.toString());


                    if (mUnits.getProduct().getIsBatchNumber()) {

                        if (!totalpurQty.isEmpty()) {
                            String[] arr = totalpurQty.split(",");
                            if (!totalPurRtrnQty.isEmpty()) {
                                String[] arr1 = totalPurRtrnQty.split(",");
                                if (arr[0].length() > 0 && arr1[0].length() > 0) {
                                    totalpQty = Double.parseDouble(arr[0]) - Double.parseDouble(arr1[0]);
                                } else if (arr[0].length() > 0) {
                                    totalpQty = Double.parseDouble(arr[0]);
                                }

                                if (arr[1].length() > 0 && arr1[1].length() > 0) {
                                    totalpValue = Double.parseDouble(arr[1]) - Double.parseDouble(arr1[1]);
                                } else if (arr[1].length() > 0) {
                                    totalpValue = Double.parseDouble(arr[1]);
                                }
                            }
                        } else {
                            totalpQty = 0.0;
                            totalpValue = 0.0;
                        }
                    } else {
                        totalpValue = tranxPurInvoiceDetailsUnitsRepository.findTotalValue(productId, mUnits.getUnits().getId());
                    }
                    System.out.println("totalValue" + totalpValue);
                    Double totalpRate = 0.0;
                    if (totalpQty != 0) {
                        totalpRate = totalpValue / totalpQty;
                    } else {
                        totalpRate = 0.0;
                    }

                    inside.addProperty("qty", totalpQty);
                    inside.addProperty("unit", mUnits.getUnits().getUnitName());
                    inside.addProperty("value", totalpValue);
                    inside.addProperty("rate", totalpRate);
                    monthArray.add(inside);

                    /****** Sales ****/
                    JsonObject saleInside = new JsonObject();
//                    totalsQty = inventoryDetailsPostingsRepository.
//                            findByTotalQty(productId, "DR", mUnits.getUnits().getId(), startMonthDate.toString(), endMonthDate.toString());

                    totalSaleQty = tranxSalesInvoiceDetailsUnitRepository.
                            findSalesQtyByProductIdAndStatus(productId, true, startMonthDate.toString(), endMonthDate.toString());


                    totalSaleRtrnQty = tranxSalesInvoiceDetailsUnitRepository.
                            findSalesReturnQtyByProductIdAndStatus(productId, true, startMonthDate.toString(), endMonthDate.toString());

                    if (mUnits.getProduct().getIsBatchNumber()) {
                        if (!totalSaleQty.isEmpty()) {
                            String[] arr = totalSaleQty.split(",");
                            if (!totalSaleRtrnQty.isEmpty()) {
                                String[] arr1 = totalSaleRtrnQty.split(",");
                                if (arr[0].length() > 0 && arr1[0].length() > 0) {
                                    totalsQty = Double.parseDouble(arr[0]) - Double.parseDouble(arr1[0]);
                                } else if (arr[0].length() > 0) {
                                    totalsQty = Double.parseDouble(arr[0]);
                                }

                                if (arr[1].length() > 0 && arr1[1].length() > 0) {
                                    totalsValue = Double.parseDouble(arr[1]) - Double.parseDouble(arr1[1]);
                                } else if (arr[1].length() > 0) {
                                    totalsValue = Double.parseDouble(arr[1]);
                                }
                            }
                        } else {
                            totalsQty = 0.0;
                            totalsValue = 0.0;
                        }
                    }
                    else {

                        totalsValue = tranxSalesInvoiceDetailsUnitRepository.findTotalValue(productId, mUnits.getUnits().getId());
                    }
                    System.out.println("totalValue" + totalsValue);
                    Double totalsRate = 0.0;
                    if (totalsQty != 0) {
                        totalsRate = totalsValue / totalsQty;
                    } else {
                        totalsRate = 0.0;
                    }


                    saleInside.addProperty("qty", totalsQty);
                    saleInside.addProperty("unit", mUnits.getUnits().getUnitName());
                    saleInside.addProperty("value", totalsValue);
                    saleInside.addProperty("rate", totalsRate);
                    saleArray.add(saleInside);

                    JsonObject closingInside = new JsonObject();

                    closingInside.addProperty("qty", (totalpQty - totalsQty));
                    closingInside.addProperty("unit", mUnits.getUnits().getUnitName());
                    closingInside.addProperty("rate", Math.round((totalpRate - totalsRate)* 100) / 100);
                    closingInside.addProperty("value", (totalpValue - totalsValue));


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

//            >>>>>>>>>
           List<ProductOpeningStocks> openingStocks = openingStocksRepository.findOpeningStockByProductIdAndStatus(productId, true);
            for (ProductOpeningStocks mOpeningStocks : openingStocks) {
//                JsonObject mObject = new JsonObject();
                if(mOpeningStocks != null){
                    res.addProperty("unit", mOpeningStocks.getUnits().getUnitName());
//                mObject.addProperty("b_no", mOpeningStocks.getProductBatchNo() != null ? mOpeningStocks.getProductBatchNo().getBatchNo() : "");
//                mObject.addProperty("batch_id", mOpeningStocks.getProductBatchNo() != null ? mOpeningStocks.getProductBatchNo().getId().toString() : "");
                    res.addProperty("opening_qty",  mOpeningStocks.getOpeningStocks() );
                    String FreeOpeningQty = mOpeningStocks.getFreeOpeningQty() != null ? mOpeningStocks.getFreeOpeningQty().toString():"0";
                    res.addProperty("b_free_qty", Double.parseDouble(FreeOpeningQty));
                    res.addProperty("total_opening_qty",  mOpeningStocks.getOpeningStocks() + Double.parseDouble(FreeOpeningQty));
//                mObject.addProperty("b_mrp", mOpeningStocks.getMrp());
//                mObject.addProperty("b_sale_rate", mOpeningStocks.getSalesRate());
//                mObject.addProperty("b_purchase_rate", mOpeningStocks.getPurchaseRate());
                    String costing = mOpeningStocks.getCosting() != null ? mOpeningStocks.getCosting().toString():"0";
                    res.addProperty("costing",  Double.parseDouble(costing));
                }
                else{
                    res.addProperty("unit", "");
                    res.addProperty("opening_qty",  "");
                    res.addProperty("b_free_qty", "");
                    res.addProperty("total_opening_qty", "");
                    res.addProperty("costing","");
                }

            }
//           <<<<<<<<<<

            res.addProperty("responseStatus", HttpStatus.OK.value());

        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("message", "Failed To Load Data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;

    }

//   Api service for Export Stock Valuation screen-1 in Excel format
    public InputStream exportStockValuation1(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            Boolean batchFlag = Boolean.valueOf(jsonRequest.get("batchFlag"));
            int p_id = Integer.parseInt((jsonRequest.get("p_id")));
            Boolean mfg = Boolean.valueOf(jsonRequest.get("mfg"));
            Boolean exp = Boolean.valueOf(jsonRequest.get("exp"));
                    System.out.println("jsonRequest "+jsonRequest);
            Boolean Allbatchflag = Boolean.valueOf(jsonRequest.get("Allbatchflag"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            System.out.println("productBatchNos size:" + productBatchNos.size());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
//                    String[] header1 = {"", "", "", "Closing Balance", "", ""};
//                    String[] headers = new String[]{""};
                    String[] headers = {"BRAND NAME", "PRODUCT NAME", "PACKING", " CLOSING QTY", "UNIT", "CLOSING RATE", "CLOSING VALUE"};
//                    if(mfg == true && exp == true){
//                        headers = new String[]{"BRAND NAME", "PRODUCT NAME", "PACKING", "MFG. DATE","EXP.DATE"," CLOSING QTY", "UNIT", "CLOSING RATE", "CLOSING VALUE"};
//                    }
//                     else if(mfg == true){
//                        headers = new String[]{"BRAND NAME", "PRODUCT NAME", "PACKING","MFG. DATE"," CLOSING QTY", "UNIT", "CLOSING RATE", "CLOSING VALUE"};
//
//                    }
//                    else if(exp == true){
//                         headers = new String[]{"BRAND NAME", "PRODUCT NAME", "PACKING","EXP.DATE", " BATCH NO", "EXPIRY DATE", "QUANTITY", "UNIT"};
//                    }
//                    else{
//                        headers =new String[] {"BRAND NAME", "PRODUCT NAME", "PACKING"," CLOSING QTY", "UNIT", "CLOSING RATE", "CLOSING VALUE"};
//                    }
                    Sheet sheet = workbook.createSheet("stock_valuation_data");

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
                    double sumOfQty = 0;
                    Double sumOfRate = 0.0;
                    double sumOfValue = 0.0;
                    int rowIdx = 1;
                    Double sum_clo_qty = 0.0;
                    Double qty = 0.0;
                    Double sum_rate = 0.0;
                    Double rate = 0.0;
                    Double avgRate = 0.0;
                    Double value = 0.0;
                    String unit = "";
                    int total_size = productBatchNos.size();
                  int productSize = productBatchNos.size();
                    for (int i = 0; i < productSize; i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
                        JsonArray BatchArray = batchNo.get("product_unit_data").getAsJsonArray();
                        if(BatchArray.size()>0){
                            total_size = total_size + BatchArray.size();
                        }

                    }
                        System.out.println("total_size "+ total_size);

                    for (int i = 0; i < productSize; i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("brand_name").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("product_name").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("packaging").getAsString());
                        JsonArray BatchArray = batchNo.get("product_unit_data").getAsJsonArray();
                        sum_clo_qty = 0.0;
                        sum_rate = 0.0;
                        for (int j = 0; j < BatchArray.size(); j++){
                            System.out.println("databatch"+ batchNo.get("product_unit_data").getAsJsonArray() );
//                            for(int k = 0; k<BatchArray.size())
                            JsonObject BatchArray1 = BatchArray.get(j).getAsJsonObject();
                            if(BatchArray1.size()>0){
                                qty = BatchArray1.get("closing_stock").getAsDouble();
                                sum_clo_qty = sum_clo_qty + qty;
                                unit =  BatchArray1.get("unit_name").getAsString();

                                rate = BatchArray1.get("bat_pur_rate") != null ? BatchArray1.get("bat_pur_rate").getAsDouble(): 0.0;
                                sum_rate = sum_rate + rate;
                            }
                            else{
                                sum_rate =0.0;
                                sum_clo_qty = 0.0;
                            }

                        }
                        System.out.println("BatchArray1.get($(`bat_pur_rate)`).getAsDouble()" + sum_rate);
                        System.out.println("sum_clo_qty" + sum_clo_qty);

                        row.createCell(3).setCellValue(sum_clo_qty);
                        row.createCell(4).setCellValue(unit);
                        if(sum_rate!=0){
                            avgRate = sum_rate / BatchArray.size();
                        }
                        else{
                            avgRate = 0.0;
                        }

                        value = sum_clo_qty * avgRate;
                            row.createCell(5).setCellValue(avgRate != null && avgRate !=0 ?avgRate.doubleValue() : 0);
                        row.createCell(6).setCellValue(value != null  && value !=0? value.doubleValue() : 0 );
                                           sumOfQty = sumOfQty + sum_clo_qty ;
                        sumOfRate = sumOfRate + avgRate;
                        sumOfValue = sumOfValue + value;
                        System.out.println("Allbatchflag" + Allbatchflag);
                        if(Allbatchflag == true){

                            if(BatchArray.size()>0){
                              for(int j =0; j< BatchArray.size(); j++){
                                 Row batchRow = sheet.createRow(rowIdx++);
                                 JsonObject BatchArray1 = BatchArray.get(j).getAsJsonObject();
                                 Double Bvalue = (BatchArray1.get("closing_stock").getAsDouble() * BatchArray1.get("bat_pur_rate").getAsDouble());

                                 batchRow.createCell(1).setCellValue(BatchArray1.get("batchno").getAsLong());
                                 batchRow.createCell(3).setCellValue(BatchArray1.get("closing_stock").getAsDouble());
                                 batchRow.createCell(4).setCellValue(BatchArray1.get("unit_name").getAsString());
                                 batchRow.createCell(5).setCellValue(BatchArray1.get("bat_pur_rate").getAsDouble());
                                 batchRow.createCell(6).setCellValue(Bvalue);
                              }
                            }
                        }
                        if(batchFlag == true){
                            if(p_id==batchNo.get("id").getAsInt()){
                                if(BatchArray.size()>0){
                                    for(int j =0; j< BatchArray.size(); j++){
                                        Row batchRow = sheet.createRow(rowIdx++);
                                        JsonObject BatchArray1 = BatchArray.get(j).getAsJsonObject();
                                        Double Bvalue = (BatchArray1.get("closing_stock").getAsDouble() * BatchArray1.get("bat_pur_rate").getAsDouble());

                                        batchRow.createCell(1).setCellValue(BatchArray1.get("batchno").getAsLong());
                                        batchRow.createCell(3).setCellValue(BatchArray1.get("closing_stock").getAsDouble());
                                        batchRow.createCell(4).setCellValue(BatchArray1.get("unit_name").getAsString());
                                        batchRow.createCell(5).setCellValue(BatchArray1.get("bat_pur_rate").getAsDouble());
                                        batchRow.createCell(6).setCellValue(Bvalue);
                                    }
                                }
                            }
                        }
                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    prow.createCell(1).setCellValue(productBatchNos.size());
                    prow.createCell(3).setCellValue(sumOfQty);
                    prow.createCell(5).setCellValue(sumOfRate);
                    prow.createCell(6).setCellValue(sumOfValue);


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
            productLogger.error("Failed to load stock valuation1 data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public InputStream exportExcelStkValScreen2(Map<String, String> jsonRequest, HttpServletRequest request) {
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
                    Sheet sheet = workbook.createSheet("stockValuation2ExcelSheet");

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
            productLogger.error("Failed to load stock valuation2 data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public InputStream exportExcelStkValScreen3(Map<String, String> jsonRequest, HttpServletRequest request) {
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

                    Sheet sheet = workbook.createSheet("stockValuation3ExcelSheet");

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
            productLogger.error("Failed to load stock valuation3 data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

}
