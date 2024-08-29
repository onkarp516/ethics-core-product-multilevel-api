package in.truethics.ethics.ethicsapiv10.service.Gstr_Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.NumFormat;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerGstDetails;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerGstDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.FiscalYearRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesInvoiceRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.service.master_service.ProductService;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

@Service
public class GSTR3Service {

    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    FiscalYearRepository fiscalYearRepository;
    @Autowired
    EntityManager entityManager;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    @Autowired
    private LedgerGstDetailsRepository gstDetailsRepository;
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    @Autowired
    private TranxSalesInvoiceRepository tranxSalesInvoiceRepository;
    @Autowired
    private NumFormat numFormat;
    private static final Logger productLogger = LogManager.getLogger(ProductService.class);
    public JsonObject getGSTR3(HttpServletRequest request){

        JsonObject finalObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        LocalDate startDate=null;
        LocalDate endDate=null;
        ResponseMessage message = new ResponseMessage();

        if(paramMap.containsKey("start_date") && paramMap.containsKey("end_date")){
            startDate = LocalDate.parse(request.getParameter("start_date"));
            endDate= LocalDate.parse(request.getParameter("end_date"));
        }else{
            FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
            System.out.println("fiscalYear  "+fiscalYear);
            if (fiscalYear != null){
              startDate = fiscalYear.getDateStart();
              endDate = fiscalYear.getDateEnd();
            }
        }
        if(startDate.isAfter(endDate)){
            System.out.println("End Date should be after start date");
            return null;
        }
       try{
        //start code from here
           JsonArray array=new JsonArray();
           List<LedgerTransactionPostings> ledTranxPost = new ArrayList<>();
           String query = "SELECT ledger_transaction_postings_tbl.ledger_master_id FROM ledger_transaction_postings_tbl";
       }catch (Exception e){

       }



        return finalObject;
    }

    public JsonObject getGSTR3BOutwardTaxSuplierData(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalObject = new JsonObject();
//        String searchText = request.getParameter("searchText");
//        String startDate = request.getParameter("startDate");
//        String endDate = request.getParameter("endDate");
//        LocalDate endDatep = null;
//        LocalDate startDatep = null;
        Boolean flag = false;
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
            endDate = request.getParameter("end_date");
            startDate = request.getParameter("start_date");
            if (endDate != null && !endDate.isEmpty() &&
                    startDate != null && !startDate.isEmpty()) {
                endDatep = LocalDate.parse(endDate);
                startDatep = LocalDate.parse(startDate);
            }
            else{
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }

        } else {

            FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
            if (fiscalYear != null) {
                startDatep = fiscalYear.getDateStart();
                endDatep = fiscalYear.getDateEnd();
            }

        }
        currentStartDate = startDatep;
        currentEndDate = endDatep;

        if ( startDatep != null && startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");

        }

//        List sundryDebtorsData = new ArrayList<String>();
        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();

        // sales invoice
        String query = "SELECT DATE(tsit.bill_date), tsit.sales_invoice_no, lmt.ledger_name,lgdt.gstin,tsit.total_base_amount, " +
                "tsit.totaligst, tsit.totalcgst, tsit.totalsgst, tsit.total_tax, tsit.total_amount, tsit.id from tranx_sales_invoice_tbl" +
                " AS tsit LEFT JOIN ledger_master_tbl AS lmt ON tsit.sundry_debtors_id =lmt.id LEFT JOIN ledger_gst_details_tbl " +
                "AS lgdt ON lmt.id = lgdt.ledger_id WHERE tsit.status=1 AND DATE(tsit.bill_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "'";

        Query q = entityManager.createNativeQuery(query);

       List<Object[]> sundryDebtorsData = q.getResultList();

        for (int j = 0; j < sundryDebtorsData.size(); j++) {
            JsonObject inspObj = new JsonObject();
//            System.out.println("----->>>."+sundryDebtorsData.get(j)[4]);
            inspObj.addProperty("bill_date", sundryDebtorsData.get(j)[0].toString());
            inspObj.addProperty("sales_invoice", sundryDebtorsData.get(j)[1].toString());
            inspObj.addProperty("ledger_name", sundryDebtorsData.get(j)[2].toString());
            inspObj.addProperty("gstin", sundryDebtorsData.get(j)[3] !=null ?  sundryDebtorsData.get(j)[3].toString(): "");
            inspObj.addProperty("taxable_amt",  sundryDebtorsData.get(j)[4] !=null ? parseDouble(sundryDebtorsData.get(j)[4].toString()) :  0.0);
            inspObj.addProperty("totaligst", sundryDebtorsData.get(j)[5] != null ? parseDouble(sundryDebtorsData.get(j)[5].toString()) :  0.0);
            inspObj.addProperty("totalcgst", sundryDebtorsData.get(j)[6] != null ? parseDouble(sundryDebtorsData.get(j)[6].toString()) :  0.0);
            inspObj.addProperty("totalsgst", sundryDebtorsData.get(j)[7] != null ? parseDouble(sundryDebtorsData.get(j)[7].toString()) :  0.0);
            inspObj.addProperty("total_tax", sundryDebtorsData.get(j)[8] != null ? parseDouble(sundryDebtorsData.get(j)[8].toString()) :  0.0);
            inspObj.addProperty("total_amount", sundryDebtorsData.get(j)[9] != null ? parseDouble(sundryDebtorsData.get(j)[9].toString()) :  0.0);
            inspObj.addProperty("id", sundryDebtorsData.get(j)[10] != null ? parseLong(sundryDebtorsData.get(j)[10].toString()) :  0);
            inspObj.addProperty("voucher_type", "Sales Invoice");
            mArray.add(inspObj);
        }

        // sales return
        String queryy = "SELECT DATE(tsrit.transaction_date), tsrit.sales_return_no, lmt.ledger_name,lgdt.gstin,tsrit.total_base_amount," +
                " tsrit.totaligst, tsrit.totalcgst, tsrit.totalsgst, tsrit.total_tax, tsrit.total_amount, tsrit.id from tranx_sales_return_invoice_tbl " +
                " AS tsrit LEFT JOIN ledger_master_tbl AS lmt ON tsrit.sundry_debtors_id =lmt.id LEFT JOIN ledger_gst_details_tbl" +
                " AS lgdt ON lmt.id = lgdt.ledger_id WHERE tsrit.status=1 AND DATE(tsrit.transaction_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "'";

        Query qq = entityManager.createNativeQuery(queryy);

        List<Object[]> sundryDebtorsDataR = qq.getResultList();

        for (int j = 0; j < sundryDebtorsDataR.size(); j++) {
            JsonObject inspObj = new JsonObject();
//            System.out.println("----->>>."+sundryDebtorsData.get(j)[4]);
            inspObj.addProperty("bill_date", sundryDebtorsDataR.get(j)[0].toString());
            inspObj.addProperty("sales_invoice", sundryDebtorsDataR.get(j)[1].toString());
            inspObj.addProperty("ledger_name", sundryDebtorsDataR.get(j)[2].toString());
            inspObj.addProperty("gstin", sundryDebtorsDataR.get(j)[3] !=null ?  sundryDebtorsDataR.get(j)[3].toString(): "");
            inspObj.addProperty("taxable_amt",  sundryDebtorsDataR.get(j)[4] !=null ? parseDouble(sundryDebtorsDataR.get(j)[4].toString()) :  0.0);
            inspObj.addProperty("totaligst", sundryDebtorsDataR.get(j)[5] != null ? parseDouble(sundryDebtorsDataR.get(j)[5].toString()) :  0.0);
            inspObj.addProperty("totalcgst", sundryDebtorsDataR.get(j)[6] != null ? parseDouble(sundryDebtorsDataR.get(j)[6].toString()) :  0.0);
            inspObj.addProperty("totalsgst", sundryDebtorsDataR.get(j)[7] != null ? parseDouble(sundryDebtorsDataR.get(j)[7].toString()) :  0.0);
            inspObj.addProperty("total_tax", sundryDebtorsDataR.get(j)[8] != null ? parseDouble(sundryDebtorsDataR.get(j)[8].toString()) :  0.0);
            inspObj.addProperty("total_amount", sundryDebtorsDataR.get(j)[9] != null ? parseDouble(sundryDebtorsDataR.get(j)[9].toString()) :  0.0);
            inspObj.addProperty("id", sundryDebtorsData.get(j)[10] != null ? parseLong(sundryDebtorsData.get(j)[10].toString()) :  0);
            inspObj.addProperty("voucher_type", "Sales Return");
            mArray.add(inspObj);
        }

        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }


    public InputStream ExportExcelGSTR3BOutwardTaxSuplierData(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            System.out.println("productBatchNos size:" + productBatchNos.size());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"Sr.No", "Dates","Invoice No.","Particulars", "GSTIN/UIN", "Voucher Type", "Taxable Amt.", "IGST Amt", "CGST Amt", "SGST Amt", "Cess Amt", "Tax Amt", "Invoice Amt"};
//                    if(mfgShow)
//                        headers = new String[]{"BRAND NAME", "PRODUCT NAME", "PACKING", "BATCH NO", "MFG.", "EXP.", "QTY", "UNIT"};
                    Sheet sheet = workbook.createSheet("GSTR3B_Outward_Tax_Suplier_ExcelSheet");

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

//                    int sumOfVoucherCount = 0;
                    Double sumOfTaxableAmt = 0.0, sumOfIGSTAmt = 0.0, sumOfCGSTAmt = 0.0, sumOfSGSTAmt = 0.0, sumOfTaxAmt = 0.0, sumOfInvoiceAmt = 0.0;

                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
//                        1st row serial no.
                        row.createCell(0).setCellValue(i + 1);
                        row.createCell(1).setCellValue(batchNo.get("bill_date").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("sales_invoice").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("ledger_name").getAsString());
                        row.createCell(4).setCellValue(batchNo.get("gstin").getAsString());
                        row.createCell(5).setCellValue(batchNo.get("voucher_type").getAsString());
                        row.createCell(6).setCellValue(batchNo.get("taxable_amt").getAsDouble());

                        row.createCell(7).setCellValue(batchNo.get("totaligst").getAsDouble());
                        row.createCell(8).setCellValue(batchNo.get("totalcgst").getAsDouble());
                        row.createCell(9).setCellValue(batchNo.get("totalsgst").getAsDouble());
                        row.createCell(11).setCellValue(batchNo.get("total_tax").getAsDouble());
                        row.createCell(12).setCellValue(batchNo.get("total_amount").getAsDouble());

                        if(batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Sales Invoice")){
                            sumOfTaxableAmt += batchNo.get("taxable_amt").getAsDouble();
                            sumOfIGSTAmt += batchNo.get("totaligst").getAsDouble();
                            sumOfCGSTAmt += batchNo.get("totalcgst").getAsDouble();
                            sumOfSGSTAmt += batchNo.get("totalsgst").getAsDouble();
                            sumOfTaxAmt += batchNo.get("total_tax").getAsDouble();
                            sumOfInvoiceAmt += batchNo.get("total_amount").getAsDouble();
                        }
                        else {
                            sumOfTaxableAmt -= batchNo.get("taxable_amt").getAsDouble();
                            sumOfIGSTAmt -= batchNo.get("totaligst").getAsDouble();
                            sumOfCGSTAmt -= batchNo.get("totalcgst").getAsDouble();
                            sumOfSGSTAmt -= batchNo.get("totalsgst").getAsDouble();
                            sumOfTaxAmt -= batchNo.get("total_tax").getAsDouble();
                            sumOfInvoiceAmt -= batchNo.get("total_amount").getAsDouble();
                        }



                    }

                    Row prow = sheet.createRow(rowIdx++);
//                    for (int i = 0; i < headers.length; i++) {
                    prow.createCell(0).setCellValue("Total");
//                    prow.createCell(3).setCellValue(sumOfVoucherCount);

                    prow.createCell(6).setCellValue(sumOfTaxableAmt);
                    prow.createCell(7).setCellValue(sumOfIGSTAmt);
                    prow.createCell(8).setCellValue(sumOfCGSTAmt);
                    prow.createCell(9).setCellValue(sumOfSGSTAmt);
                    prow.createCell(10).setCellValue("");
                    prow.createCell(11).setCellValue(sumOfTaxAmt);
                    prow.createCell(12).setCellValue(sumOfInvoiceAmt);

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
            productLogger.error("Failed to data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    //    GSTR3B Purchase Itc Api
    public JsonObject getGSTR3BAllOtherITCData(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalObject = new JsonObject();
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
            endDate = request.getParameter("end_date");
            startDate = request.getParameter("start_date");
            if (endDate != null && !endDate.isEmpty() &&
                    startDate != null && !startDate.isEmpty()) {
                endDatep = LocalDate.parse(endDate);
                startDatep = LocalDate.parse(startDate);
            }
            else{
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }

        } else {

            FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
            if (fiscalYear != null) {
                startDatep = fiscalYear.getDateStart();
                endDatep = fiscalYear.getDateEnd();
            }

        }
        currentStartDate = startDatep;
        currentEndDate = endDatep;

        if ( startDatep != null && startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");

        }

//        List sundryDebtorsData = new ArrayList<String>();
        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();

        // purchase invoice
        String query = "SELECT DATE(tpit.transaction_date), tpit.vendor_invoice_no, lmt.ledger_name, lgdt.gstin, tpit.total_base_amount, " +
                "tpit.totaligst, tpit.totalcgst, tpit.totalsgst, tpit.total_tax, tpit.total_amount, tpit.id " +
                "FROM core_product_multilevel_db.tranx_purchase_invoice_tbl AS tpit LEFT JOIN ledger_master_tbl AS lmt ON " +
                "tpit.sundry_creditors_id = lmt.id LEFT JOIN ledger_gst_details_tbl AS lgdt ON lmt.id = lgdt.ledger_id" +
                " WHERE tpit.status=1 AND" +
                " DATE(tpit.transaction_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "'";

        Query q = entityManager.createNativeQuery(query);

        List<Object[]> sundryDebtorsData = q.getResultList();

        for (int j = 0; j < sundryDebtorsData.size(); j++) {
            JsonObject inspObj = new JsonObject();
//            System.out.println("----->>>."+sundryDebtorsData.get(j)[4]);
            inspObj.addProperty("bill_date", sundryDebtorsData.get(j)[0].toString());
            inspObj.addProperty("purchase_invoice", sundryDebtorsData.get(j)[1].toString());
            inspObj.addProperty("ledger_name", sundryDebtorsData.get(j)[2].toString());
            inspObj.addProperty("gstin", sundryDebtorsData.get(j)[3] !=null ?  sundryDebtorsData.get(j)[3].toString(): "");
            inspObj.addProperty("taxable_amt",  sundryDebtorsData.get(j)[4] !=null ? parseDouble(sundryDebtorsData.get(j)[4].toString()) :  0.0);
            inspObj.addProperty("totaligst", sundryDebtorsData.get(j)[5] != null ? parseDouble(sundryDebtorsData.get(j)[5].toString()) :  0.0);
            inspObj.addProperty("totalcgst", sundryDebtorsData.get(j)[6] != null ? parseDouble(sundryDebtorsData.get(j)[6].toString()) :  0.0);
            inspObj.addProperty("totalsgst", sundryDebtorsData.get(j)[7] != null ? parseDouble(sundryDebtorsData.get(j)[7].toString()) :  0.0);
            inspObj.addProperty("total_tax", sundryDebtorsData.get(j)[8] != null ? parseDouble(sundryDebtorsData.get(j)[8].toString()) :  0.0);
            inspObj.addProperty("total_amount", sundryDebtorsData.get(j)[9] != null ? parseDouble(sundryDebtorsData.get(j)[9].toString()) :  0.0);
            inspObj.addProperty("id", sundryDebtorsData.get(j)[10] != null ? parseLong(sundryDebtorsData.get(j)[10].toString()) :  0);
            inspObj.addProperty("voucher_type", "Purchase Invoice");
            mArray.add(inspObj);
        }

        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }

    //    GSTR3B Purchase Itc Export excel Api
    public InputStream exportExcelGSTR3BPurchaseData(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            System.out.println("productBatchNos size:" + productBatchNos.size());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"Sr.No", "Dates","Invoice No.","Particulars", "GSTIN/UIN", "Voucher Type", "Taxable Amt.", "IGST Amt", "CGST Amt", "SGST Amt", "Cess Amt", "Tax Amt", "Invoice Amt"};

                    Sheet sheet = workbook.createSheet("GSTR3B_purchase_ExcelSheet");

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

                    Double sumOfTaxableAmt = 0.0, sumOfIGSTAmt = 0.0, sumOfCGSTAmt = 0.0, sumOfSGSTAmt = 0.0, sumOfTaxAmt = 0.0, sumOfInvoiceAmt = 0.0;

                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
//                        1st row serial no.
                        row.createCell(0).setCellValue(i + 1);
                        row.createCell(1).setCellValue(batchNo.get("bill_date").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("purchase_invoice").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("ledger_name").getAsString());
                        row.createCell(4).setCellValue(batchNo.get("gstin").getAsString());
                        row.createCell(5).setCellValue(batchNo.get("voucher_type").getAsString());
                        row.createCell(6).setCellValue(batchNo.get("taxable_amt").getAsDouble());

                        row.createCell(7).setCellValue(batchNo.get("totaligst").getAsDouble());
                        row.createCell(8).setCellValue(batchNo.get("totalcgst").getAsDouble());
                        row.createCell(9).setCellValue(batchNo.get("totalsgst").getAsDouble());
                        row.createCell(11).setCellValue(batchNo.get("total_tax").getAsDouble());
                        row.createCell(12).setCellValue(batchNo.get("total_amount").getAsDouble());
                            sumOfTaxableAmt += batchNo.get("taxable_amt").getAsDouble();
                            sumOfIGSTAmt += batchNo.get("totaligst").getAsDouble();
                            sumOfCGSTAmt += batchNo.get("totalcgst").getAsDouble();
                            sumOfSGSTAmt += batchNo.get("totalsgst").getAsDouble();
                            sumOfTaxAmt += batchNo.get("total_tax").getAsDouble();
                            sumOfInvoiceAmt += batchNo.get("total_amount").getAsDouble();

                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    prow.createCell(6).setCellValue(sumOfTaxableAmt);
                    prow.createCell(7).setCellValue(sumOfIGSTAmt);
                    prow.createCell(8).setCellValue(sumOfCGSTAmt);
                    prow.createCell(9).setCellValue(sumOfSGSTAmt);
                    prow.createCell(10).setCellValue("");
                    prow.createCell(11).setCellValue(sumOfTaxAmt);
                    prow.createCell(12).setCellValue(sumOfInvoiceAmt);

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
            productLogger.error("Failed to data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }
}
