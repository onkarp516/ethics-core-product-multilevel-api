package in.truethics.ethics.ethicsapiv10.controller.reports.account_books;

import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.ContraReportService;
import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.SalesRegisterReportService;
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
public class SalesRegisterReportController {
    @Autowired
    private SalesRegisterReportService service;

    @PostMapping(path = "/get_monthwise_sales_register_details")
    public Object getMonthwiseSalesRegisterTransactionDetails(HttpServletRequest request) {
        return service.getMonthwiseSalesRegisterTransactionDetails(request).toString();
    }
    @PostMapping(path = "/get_sales_register_details")
    public Object getSalesRegisterTransactionDetails(HttpServletRequest request) {
        return service.getSalesRegisterTransactionDetails(request).toString();
    }

    @PostMapping(path = "/get_sales_order_details")
    public Object getSalesOrderTransactionDetails(HttpServletRequest request) {
        return service.getSalesOrderTransactionDetails(request).toString();
    }
    @PostMapping(path = "/get_monthwise_sales_order_details")
    public Object getMonthwiseSalesOrderTransactionDetails(HttpServletRequest request) {
        return service.getMonthwiseSalesOrderTransactionDetails(request).toString();
    }
    @PostMapping(path = "/get_sales_Challan_details")
    public Object getSalesChallanTransactionDetails(HttpServletRequest request) {
        return service.getSalesChallanTransactionDetails(request).toString();
    }
    @PostMapping(path = "/get_monthwise_sales_challan_details")
    public Object getMonthwiseSalesChallanTransactionDetails(HttpServletRequest request) {
        return service.getMonthwiseSalesChallanTransactionDetails(request).toString();
    }

    @PostMapping(path = "/exportExcelSaleRegReport")
    public Object exportExcelSaleRegReport(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "SalesRegisterReport.xlsx";
        InputStreamResource file = new InputStreamResource(service.exportExcelSaleRegReport(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }


    @PostMapping(path = "/exportExcelSaleRegReport2")
    public Object exportExcelSaleRegReport2(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "SalesRegisterReport.xlsx";
        InputStreamResource file = new InputStreamResource(service.exportExcelSaleRegReport2(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }
}
