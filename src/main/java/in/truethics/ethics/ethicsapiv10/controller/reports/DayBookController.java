package in.truethics.ethics.ethicsapiv10.controller.reports;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.reports_service.DayBookService;
import in.truethics.ethics.ethicsapiv10.service.reports_service.dashboard.DashboardService;
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
public class DayBookController {
    @Autowired
    private DayBookService dayBookService;

    /* get All Ledger Transactions*/
    @PostMapping(path = "/get_all_ledger_tranx_details")
    public Object getAllLedgersTransactions(HttpServletRequest request) {
        JsonObject result = dayBookService.getAllLedgersTransactions(request);
        return result.toString();
    }
    @PostMapping(path = "/exportExcelDayBook")
    public Object exportDayBook(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "excelLedgerReport1.xlsx";
        InputStreamResource file = new InputStreamResource(dayBookService.exportDayBook(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }
}
