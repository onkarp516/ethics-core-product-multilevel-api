package in.truethics.ethics.ethicsapiv10.controller.reports.account_books;

import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.CreditNoteReportService;
import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.DebitNoteReportService;
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
public class CreditNoteReportController {
    @Autowired
    private CreditNoteReportService service;

    @PostMapping(path = "/get_monthwise_creditnote_details")
    public Object getMonthwiseCreditNoteTransactionDetails(HttpServletRequest request) {
        return service.getMonthwiseCreditNoteTransactionDetails(request).toString();
    }
    @PostMapping(path = "/get_creditnote_details")
    public Object getCreditNoteTransactionDetails(HttpServletRequest request) {
        return service.getCreditNoteTransactionDetails(request).toString();
    }

    @PostMapping(path = "/exportExcelCreditNoteReport")
    public Object exportExcelCreditNoteReport(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "CreditNoteReport.xlsx";
        InputStreamResource file = new InputStreamResource(service.exportExcelCreditNoteReport(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }


    @PostMapping(path = "/exportExcelCreditNoteReport2")
    public Object exportExcelCreditNoteReport2(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "CreditNoteReport.xlsx";
        InputStreamResource file = new InputStreamResource(service.exportExcelCreditNoteReport2(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }
}
