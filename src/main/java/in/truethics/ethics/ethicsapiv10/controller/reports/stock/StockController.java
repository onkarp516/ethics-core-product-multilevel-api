package in.truethics.ethics.ethicsapiv10.controller.reports.stock;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductUnitPacking;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.service.master_service.ProductService;
import in.truethics.ethics.ethicsapiv10.service.reports_service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
public class StockController {

    @Autowired
    private StockService stockService;

    /****** WholeStock and Available Stock and Batch Stock product details: Screen1 ********/
    @GetMapping(path = "/get_whole_stock_product")
    public Object getWholeStockProducts(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        result = stockService.getWholeStockProducts(request);
        return result.toString();
    }

    /****** WholeStock and Available Stock Monthwise product stocks :screen2********/
    @PostMapping(path = "/get_monthwise_whole_stock_details")
    public Object getMonthwiseWholeStockDetails(HttpServletRequest request) {
        return stockService.getMonthwiseWholeStockDetails(request).toString();
    }
    /****** WholeStock and Available Stock Monthwise product stock tranx details :screen3 ********/
    @PostMapping(path = "/get_monthwise_whole_stock_prdtranx_details")
    public Object getMonthwiseWholeStockDetailsPrdTranx(HttpServletRequest request) {
        return stockService.getMonthwiseWholeStockDetailsPrdTranx(request).toString();
    }


    /****** Batch wise Monthwise product details ********/
    @PostMapping(path = "/get_monthwise_batch_stock_details")
    public Object getMonthwiseBatchStockDetails(HttpServletRequest request) {
        return stockService.getMonthwiseBatchStockDetails(request).toString();
    }

    /****** Expiry product details ********/
    @PostMapping(path = "/get_expiry_product")
    public Object getExpiryProducts(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        result = stockService.getExpiryProducts(request);
        return result.toString();
    }
    @PostMapping(path = "/exportExcelNearExpiryProduct")
    public Object exportExcelNearExpiryProduct(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "nearExpiryProduct.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelNearExpiryProduct(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }


    //API fo Near expiry csv screen-1
    @PostMapping(path = "/exportCsvNearExpiry1")
    public void exportCsvNearExpiry1(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.addHeader("Content-Disposition", "attachment; filename=\"exportCsvNearExpiry1.csv\"");
        stockService.exportCsvNearExpiry1(jsonRequest, request,response.getWriter());
    }
    @PostMapping(path = "/exportCSVNearExpiryProduct")
    public Object exportCSVNearExpiryProduct(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "nearExpiryProduct.csv";
        response.setContentType("text/csv");
        InputStreamResource file = new InputStreamResource(stockService.exportCSVNearExpiryProduct(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
//                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
//                .contentType("text/csv")
                .body(file);
    }

    /****** Expiry product details : Screen 2 ********/
    @PostMapping(path = "/get_expiry_product_details_by_batch")
    public Object getExpiryProductsByBatch(HttpServletRequest request) {
        return stockService.getExpiryProductsByBatch(request).toString();
    }
    /****** Expiry product details : Screen 2 ********/
    @PostMapping(path = "/get_expiry_product_monthwise")
    public Object getExpiryProductsMonthwise(HttpServletRequest request) {
        return stockService.getExpiryProductsMonthwise(request).toString();
    }

    /****** Expiry product details : Screen 3 ********/
    @PostMapping(path = "/get_expiry_product_details")
    public Object getExpiryProductsDetails(HttpServletRequest request) {
        return stockService.getExpiryProductsDetails(request).toString();
    }

    /******* Batch wise product closing stock *****/
    @PostMapping(path = "/get_batchwise_product_stocks")
    public Object getBatchwiseProductStocks(HttpServletRequest request) {
        return stockService.getBatchwiseProductStocks(request).toString();
    }


    /****** Expired product details ********/
    @PostMapping(path = "/get_expired_product")
    public Object getExpiredProducts(HttpServletRequest request) {
        JsonObject result = stockService.getExpiredProducts(request);
        return result.toString();
    }
    @PostMapping(path = "/exportExcelExpiredProduct")
    public Object exportExcelExpiredProduct(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request) {
        String filename = "expiredProduct.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelExpiredProduct(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }
    //API for min Level Stock Screen-1
    @PostMapping(path = "/exportExcelMinLevelProduct")
    public Object exportExcelMinLevelProduct(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "minimumLevelStockReport.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelMinLevelProduct(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }
    //API for  min Level Stock Screen-2
    @PostMapping(path = "/exportExcelMinLevelSecondScreen")
    public Object exportExcelMinLevel2Product(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "minimumLevelStockReport2.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelMinLevel2Product(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    //API for min Level Stock Screen-3
    @PostMapping(path = "/exportExcelMinLevelThirdScreen")
    public Object exportExcelMinLevel3Product(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "minLevelStockReportScreen3.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelMinLevel3Product(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }
    //API for whole Stock Screen-1
    @PostMapping(path = "/exportToExcelWholeStock1")
    public Object exportExcelWholeStock1(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "minLevelStockReportScreen3.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelWholeStock1(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }




    //API for whole stock -2
    @PostMapping(path = "/exportExcelWholeStock2")
    public Object exportExcelWholeStock2(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "minimumLevelStockReport2.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelWholeStock2(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }
    //API for third screen of whole stock
    @PostMapping(path = "/exportExcelWholeStock3")
    public Object exportExcelWholeStock3(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "minLevelStockReportScreen3.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelWholeStock3(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }


    @PostMapping(path = "/exportExcelExpiredProduct2")
    public Object exportExcelExpiredProduct2(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request) {
        String filename = "expiredProduct.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelExpiredProduct2(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }



    @PostMapping(path = "/exportExcelExpiredProduct3")
    public Object exportExcelExpiredProduct3(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request) {
        String filename = "expiredProduct.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelExpiredProduct3(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }


    //    API for Available stock Screen-1
    @PostMapping(path = "/exportExcelAvailableProduct")
    public Object exportExcelAvailableProduct(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request) {
        String filename = "availableProduct.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelAvailableProduct(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }
    //API for Available stock screen 1 CSV
    @PostMapping(path = "/exportToCsvAvailableStock1")
    public void exportCsvAvailableStock1(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.addHeader("Content-Disposition", "attachment; filename=\"availableStockReportScreen1.csv\"");
        stockService.exportCsvAvailableStock1(jsonRequest, request,response.getWriter());
    }

    //    API for Available stock Screen-2
    @PostMapping(path = "/exportExcelAvailableProduct2")
    public Object exportExcelAvailableProduct2(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "availableProduct.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelAvailableProduct2(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    //API for Available stock screen 2 CSV
    @PostMapping(path = "/exportToCsvAvailableStock2")
    public void exportCsvAvailableStock2(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.addHeader("Content-Disposition", "attachment; filename=\"availableStockReportScreen2.csv\"");

        stockService.exportCsvAvailableStock2(jsonRequest, request,response.getWriter());
        //return ResponseEntity.ok();
    }



    //    API for Available stock Screen-3
    @PostMapping(path = "/exportExcelAvailableProduct3")
    public Object exportExcelAvailableProduct3(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request) {
        String filename = "availableProduct.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelAvailableProduct3(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    //API for Available stock screen 3 CSV
    @PostMapping(path = "/exportCsvAvailableStock3")
    public void exportCsvAvailableStock3(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.addHeader("Content-Disposition", "attachment; filename=\"availableStockReportScreen3.csv\"");

        stockService.exportCsvAvailableStock3(jsonRequest, request,response.getWriter());
        //return ResponseEntity.ok();
    }


    @PostMapping(path = "/exportExcelMaximumLevelProduct")
    public Object exportExcelMaximumLevelPdct(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "MaximumLevelProduct.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelMaximumLevelPdct(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    @PostMapping(path = "/exportExcelMaximumLevel2Product")
    public Object exportExcelMaximumLevel2Pdct(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "MaximumLevelProduct.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelMaximumLevel2Pdct(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }


    @PostMapping(path = "/exportExcelMaximumLevel3Product")
    public Object exportExcelMaximumLevel3Pdct(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "MaximumLevelProduct.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelMaximumLevel3Pdct(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    //    API for Batch stock Screen-2
    @PostMapping(path = "/exportExcelBatchStockScreen1")
    public Object exportExcelBatchStockScreen1(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "BatchStock1ExcelSheet.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelBatchStockScreen1(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }


    //API for Batch stock screen 1 CSV
    @PostMapping(path = "/exportCsvBatchStockScreen1")
    public void exportCsvBatchStockScreen1(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.addHeader("Content-Disposition", "attachment; filename=\"exportCsvBatchStockScreen1.csv\"");

        stockService.exportCsvBatchStockScreen1(jsonRequest, request,response.getWriter());
        //return ResponseEntity.ok();
    }


    //    API for Batch stock Screen-2
    @PostMapping(path = "/exportExcelBatchStockScreen2")
    public Object exportExcelBatchStockScreen2(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "BatchStock2ExcelSheet.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelBatchStockScreen2(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    //API for Batch stock screen 2 CSV
    @PostMapping(path = "/exportCsvBatchStockScreen2")
    public void exportCsvBatchStockScreen2(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.addHeader("Content-Disposition", "attachment; filename=\"exportCsvBatchStockScreen2.csv\"");

        stockService.exportCsvBatchStockScreen2(jsonRequest, request,response.getWriter());
        //return ResponseEntity.ok();
    }



    //API for Batch stock Stock Screen-3
    @PostMapping(path = "/exportExcelBatchStockScreen3")
    public Object exportExcelBatchStockScreen3(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "BatchStock3ExcelSheet.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelBatchStockScreen3(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    //API for Batch stock screen 3 CSV
    @PostMapping(path = "/exportCsvBatchStockScreen3")
    public void exportCsvBatchStockScreen3(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.addHeader("Content-Disposition", "attachment; filename=\"exportCsvBatchStockScreen3.csv\"");

        stockService.exportCsvBatchStockScreen3(jsonRequest, request,response.getWriter());
        //return ResponseEntity.ok();
    }

    //API for Near Expiry Screen-2
    @PostMapping(path = "/exportExcelNearEx2")
    public Object exportExcelNearExpiry2(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "minLevelStockReportScreen3.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelNearExpiry2(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    //    API for CSV Near Expiry stock Screen-2
    @PostMapping(path = "/exportCsvNearExpiry2")
    public void exportCsvNearExpiry2(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.addHeader("Content-Disposition", "attachment; filename=\"exportCsvNearExpiry2.csv\"");

        stockService.exportCsvNearExpiry2(jsonRequest, request,response.getWriter());
        //return ResponseEntity.ok();
    }


    //API for Near Expiry -3
    @PostMapping(path = "/exportExcelNearEx3")
    public Object exportExcelNearExpiry3(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "minLevelStockReportScreen3.xlsx";
        InputStreamResource file = new InputStreamResource(stockService.exportExcelNearExpiry3(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    //    API for CSV Near Expiry stock Screen-3
//    @PostMapping(path = "/exportCsvNearExpiry3")
//    public void exportCsvNearExpiry3(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
//
//        response.setContentType("text/csv");
//        response.addHeader("Content-Disposition", "attachment; filename=\"exportCsvNearExpiry3.csv\"");
//
//        stockService.exportCsvNearExpiry3(jsonRequest, request,response.getWriter());
//    }


    //API for Whole stock screen 1 CSV
    @PostMapping(path = "/exportToCsvWholeStock1")
    public void exportCsvWholeStock1(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.addHeader("Content-Disposition", "attachment; filename=\"wholeStockReportScreen1.csv\"");

        stockService.exportCsvWholeStock1(jsonRequest, request,response.getWriter());
        //return ResponseEntity.ok();
    }
    //API for Whole stock screen 2 CSV
    @PostMapping(path = "/exportToCsvWholeStock2")
    public void exportCsvWholeStock2(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.addHeader("Content-Disposition", "attachment; filename=\"wholeStockReportScreen2.csv\"");

        stockService.exportCsvWholeStock2(jsonRequest, request,response.getWriter());
        //return ResponseEntity.ok();
    }
    //API for Whole stock screen 3 CSV
    @PostMapping(path = "/exportToCsvWholeStock3")
    public void exportCsvWholeStock3(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.addHeader("Content-Disposition", "attachment; filename=\"wholeStockReportScreen3.csv\"");

        stockService.exportCsvWholeStock3(jsonRequest, request,response.getWriter());
        //return ResponseEntity.ok();
    }

    @PostMapping(path = "/exportToExcelPurchaseReg")
    public ResponseEntity<?> exportToExcelPurchaseReg(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "exportExcelPurchaseReg.xlsx";
            InputStreamResource file = new InputStreamResource(stockService.exportToExcelPurchaseReg(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }
}
