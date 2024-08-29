package in.truethics.ethics.ethicsapiv10.controller.reports.account_books;

import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.PaymentReportService;
import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.ReceiptReportService;
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
public class PaymentReportController {
    @Autowired
    private PaymentReportService service;

    @PostMapping(path = "/get_monthwise_payment_details")
    public Object getMonthwisePaymentTransactionDetails(HttpServletRequest request) {
        return service.getMonthwisePaymentTransactionDetails(request).toString();
    }
    @PostMapping(path = "/get_payment_details")
    public Object getPaymentTransactionDetails(HttpServletRequest request) {
        return service.getPaymentTransactionDetails(request).toString();
    }

    @PostMapping(path = "/exportExcelPaymenttReport")
    public Object exportExcelPaymenttReport(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "PaymenttReport.xlsx";
        InputStreamResource file = new InputStreamResource(service.exportExcelPaymenttReport(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }


    @PostMapping(path = "/exportExcelPaymenttReport2")
    public Object exportExcelPaymenttReport2(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "PaymenttReport.xlsx";
        InputStreamResource file = new InputStreamResource(service.exportExcelPaymenttReport2(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }


}
