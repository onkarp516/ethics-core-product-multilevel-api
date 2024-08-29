package in.truethics.ethics.ethicsapiv10.controller.reports.account_books;

import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.CashBankBookReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
public class CashBankBookReportController {
    @Autowired
    private CashBankBookReportService service;
    @PostMapping(path = "/get_cashbook_details")
    public Object getCashBookTransactionDetails(HttpServletRequest request) {
        return service.getCashBookTransactionDetails(request).toString();
    }
    @PostMapping(path = "/get_expenses_reports")
    public Object getExpensesReports(HttpServletRequest request) {
        return service.getExpensesReports(request).toString();
    }
    //API for Export Excel Expenses screen1
    @PostMapping(path = "/exportExcelExpenseReport1")
    public Object exportExpensesReport1(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "excelExpensesReport1.xlsx";
        InputStreamResource file = new InputStreamResource(service.exportExpensesReport1(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    @PostMapping(path = "/exportExcelCashbankReport")
    public Object exportExcelCashbankReport(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "ExcelCash bankReport.xlsx";
        InputStreamResource file = new InputStreamResource(service.exportExcelCashbankReport(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

}
