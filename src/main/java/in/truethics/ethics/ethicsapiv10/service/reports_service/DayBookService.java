package in.truethics.ethics.ethicsapiv10.service.reports_service;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.model.master.TransactionTypeMaster;
import in.truethics.ethics.ethicsapiv10.model.report.DayBook;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.TransactionTypeMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.report_repository.DaybookRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DayBookService {

    @Autowired
    JwtTokenUtil jwtRequestFilter;

    @Autowired
    private DaybookRepository daybookRepository;
    private static final Logger productLogger = LogManager.getLogger(ProductService.class);

    public JsonObject getAllLedgersTransactions(HttpServletRequest request) {
        TransactionTypeMaster tranxType = null;
        Map<String, String[]> paramMap = request.getParameterMap();

        JsonArray result = new JsonArray();

        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        if (paramMap.containsKey("startDate")) {
            startDate = request.getParameter("startDate");
            startDatep = LocalDate.parse(startDate);
            endDate = request.getParameter("startDate");
            endDatep = LocalDate.parse(endDate);
        } else {
            startDatep = LocalDate.now();
            endDatep = LocalDate.now();

        }


        List<DayBook> summaries = new ArrayList<>();
        if (users.getBranch() != null)
            summaries = daybookRepository.findByTranxDateAndStatusAndOutletIdAndBranchId(startDatep, endDatep, true, users.getOutlet().getId(), users.getBranch().getId());
        else {
            summaries = daybookRepository.findByTranxDateAndStatusAndOutletId(startDatep, endDatep, true, users.getOutlet().getId());
        }
        for (DayBook details : summaries) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("transaction_date", details.getTranxDate().toString());
            jsonObject.addProperty("perticulars", details.getParticulars());
            jsonObject.addProperty("voucher_type", details.getVoucherType());
            jsonObject.addProperty("voucher_no", details.getVoucherNo());
            jsonObject.addProperty("amount", Math.abs(details.getAmount()));
//            jsonObject.addProperty("narration",details.get);
            result.add(jsonObject);
        }
        JsonObject json = new JsonObject();
        json.addProperty("company_name", users.getOutlet().getCompanyName());
        json.addProperty("message", "success");
        json.addProperty("responseStatus", HttpStatus.OK.value());
        json.add("responseList", result);
        return json;
    }

    public InputStream exportDayBook(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"DATE", "PARTICULARS", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT AMOUNT","CREDIT AMOUNT"};

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

                    int sumOfDr = 0;
                    int sumOfCr =0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {

                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);

                            row.createCell(0).setCellValue(batchNo.get("transaction_date").getAsString());
                            row.createCell(1).setCellValue(batchNo.get("perticulars").getAsString());
                            row.createCell(2).setCellValue(batchNo.get("voucher_type").getAsString());
                            row.createCell(3).setCellValue(batchNo.get("voucher_no").getAsString());
                            if(batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Sales Invoice") ||
                                    batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Payment") ||
                        batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Purchase Return Invoice") ||
                                    batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Debitnote")) {
                                row.createCell(4).setCellValue(batchNo.get("amount").getAsDouble());
                            }else row.createCell(4).setCellValue("");
                        if(batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Purchase Invoice") ||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Receipt") ||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Sales Return Invoice") ||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Contra")) {
                            row.createCell(5).setCellValue(batchNo.get("amount").getAsDouble());
                        }else row.createCell(5).setCellValue("");


                        if(batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Sales Invoice") ||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Payment") ||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Purchase Return Invoice") ||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Debitnote")){
                            sumOfDr += batchNo.get("amount").getAsDouble();
                        }
                        if(batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Purchase Invoice") ||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Receipt") ||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Sales Return Invoice") ||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Contra")){
                            sumOfCr += batchNo.get("amount").getAsDouble();
                        }
//                        sumOfQty += batchNo.get("qty").getAsDouble();


                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(4);
                    Cell cell1 = prow.createCell(5);
                    cell.setCellValue(sumOfDr);
                    cell1.setCellValue(sumOfCr);

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

}
