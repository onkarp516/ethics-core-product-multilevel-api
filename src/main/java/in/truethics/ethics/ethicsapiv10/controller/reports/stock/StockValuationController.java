package in.truethics.ethics.ethicsapiv10.controller.reports.stock;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.reports_service.StockService;
import in.truethics.ethics.ethicsapiv10.service.reports_service.dashboard.StockValuationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
public class StockValuationController {
    @Autowired
    private StockValuationService stockValuationService;

//    @GetMapping(path = "/get_allstock_valuation")
//    public static Object getAllstockValuation(HttpServletRequest request) {
//        JsonObject result = new JsonObject();
//        result = StockValuationService.getAllstockValuation(request);
//        return result.toString();
//    }

//    Api to get all stock valuation details for screen 1
    @PostMapping(path = "/get_allstock_valuation")
    public Object getAllstockValuation(HttpServletRequest request) {
        return stockValuationService.getAllstockValuation(request).toString();
    }

    //    Api to get all monthwise stock valuation details for screen 1
    @PostMapping(path = "/get_monthwise_stock_valuation_details")
    public Object getMonthwiseStockValuationDetails(HttpServletRequest request) {
        return stockValuationService.getMonthwiseStockValuationDetails(request).toString();
    }


    // Api controller for Export Stock Valuation screen-1 in Excel format
@PostMapping(path = "/exportExcelStockValuation1")
public Object exportExcelNearExpiryProduct(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
    String filename = "stockValuation1Excelsheet.xlsx";
    InputStreamResource file = new InputStreamResource(stockValuationService.exportStockValuation1(jsonRequest, request));
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
            .body(file);
}

//    API for excel export for stock Valuation Screen-2
    @PostMapping(path = "/exportExcelStkValScreen2")
    public Object exportExcelMinLevel2Product(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "stockValuation2ExcelSheet.xlsx";
        InputStreamResource file = new InputStreamResource(stockValuationService.exportExcelStkValScreen2(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    //API for excel export for stock Valuation Screen-3
    @PostMapping(path = "/exportExcelStkValScreen3")
    public Object exportExcelMinLevel3Product(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "stockValuation3ExcelSheet.xlsx";
        InputStreamResource file = new InputStreamResource(stockValuationService.exportExcelStkValScreen3(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

}
