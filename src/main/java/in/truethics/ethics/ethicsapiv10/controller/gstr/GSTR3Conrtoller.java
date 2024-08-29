package in.truethics.ethics.ethicsapiv10.controller.gstr;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.Gstr_Service.GSTR3Service;
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
public class GSTR3Conrtoller {
    @Autowired
    private GSTR3Service gstr3Service;

    //API for GSTR3 first Screen
    @PostMapping(path = "/get_GSTR3")
    public Object getGSTR2Data( HttpServletRequest request) {
        JsonObject mObject =gstr3Service.getGSTR3(request);
        return  mObject.toString();
    }

    @PostMapping(path = "/get_GSTR3B_outward_tax_suplier_data")
    public Object getGSTR1DataScreen1( HttpServletRequest request) {
        JsonObject mObject =gstr3Service.getGSTR3BOutwardTaxSuplierData(request);
        return  mObject.toString();
    }

    @PostMapping(path = "/export_excel_GSTR3B_outward_tax_suplier_data")
    public Object ExportExcelGSTR1B2BSalesData(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "GSTR3B_Outward_Tax_Suplier_ExcelSheet.xlsx";
        InputStreamResource file = new InputStreamResource(gstr3Service.ExportExcelGSTR3BOutwardTaxSuplierData(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    //    GSTR3B Purchase Itc Api
    @PostMapping(path = "/get_GSTR3B_all_other_itc_data")
    public Object getGSTR3BAllOtherITCData( HttpServletRequest request) {
        JsonObject mObject =gstr3Service.getGSTR3BAllOtherITCData(request);
        return  mObject.toString();
    }

//    GSTR3B Purchase Itc Export excel Api
    @PostMapping(path = "/export_excel_GSTR3B_purchase_data")
    public Object exportExcelGSTR3BPurchaseData(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "GSTR3B_purchase_ExcelSheet.xlsx";
        InputStreamResource file = new InputStreamResource(gstr3Service.exportExcelGSTR3BPurchaseData(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }
}
