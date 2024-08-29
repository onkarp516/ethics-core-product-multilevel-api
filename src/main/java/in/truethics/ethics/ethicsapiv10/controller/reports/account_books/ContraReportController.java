package in.truethics.ethics.ethicsapiv10.controller.reports.account_books;

import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.ContraReportService;
import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.JournalReportService;
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
public class ContraReportController {
    @Autowired
    private ContraReportService service;

    @PostMapping(path = "/get_monthwise_contra_details")
    public Object getMonthwiseContraTransactionDetails(HttpServletRequest request) {
        return service.getMonthwiseContraTransactionDetails(request).toString();
    }
    @PostMapping(path = "/get_conta_details")
    public Object getContraTransactionDetails(HttpServletRequest request) {
        return service.getContraTransactionDetails(request).toString();
    }


    @PostMapping(path = "/exportExcelContraReport")
    public Object exportExcelContraReport(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "ContraReport.xlsx";
        InputStreamResource file = new InputStreamResource(service.exportExcelContraReport(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }


    @PostMapping(path = "/exportExcelContraReport2")
    public Object exportExcelContraReport2(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "ContraReport.xlsx";
        InputStreamResource file = new InputStreamResource(service.exportExcelContraReport2(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

//    @GetMapping("/getAllBillOrderReport/{fromDate}/{toDate}")
//    public ResponseEntity<?> getAllBillOrderReport(@PathVariable(value = "fromDate") String fromDate,
//                                                   @PathVariable(value = "toDate") String toDate,
//                                                   HttpServletRequest req) throws ParseException {
////        return ResponseEntity.ok(orderService.getAllCategoryOrdersReport(request));
//
//        String filename = "tutorials.xlsx";
//        InputStreamResource file = new InputStreamResource(service.getDownloadContraReport(fromDate,
//                toDate,req));
//
//        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
//                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
//                .body(file);
//    }
}
