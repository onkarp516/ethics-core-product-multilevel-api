package in.truethics.ethics.ethicsapiv10.controller.tranx.contra;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.tranx_service.contra.TranxContraNewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class TranxContraController {
    //    @Autowired
//    private TranxContraService service;
    @Autowired
    private TranxContraNewService service;
    /* get last records of voucher Contra   */
    @GetMapping(path = "/get_last_record_contra")
    public Object contraLastRecord(HttpServletRequest request) {
        JsonObject result = service.contraLastRecord(request);
        return result.toString();
    }
    /* Create Contra */
    @PostMapping(path = "/create_contra")
    public Object createContra(HttpServletRequest request) {
        JsonObject array = service.createContra(request);
        return array.toString();
    }


    /* Get List of contra   */
//    @GetMapping(path = "/get_contra_list_by_outlet")
//    public Object contraListbyOutlet(HttpServletRequest request) {
//        JsonObject object = service.contraListbyOutlet(request);
//        return object.toString();
//    }
    /* contra list with pagination  starts*/
    @PostMapping(path = "/get_contra_list_by_outlet")
    public Object contraListbyOutlet(@RequestBody Map<String, String> request,  HttpServletRequest req) {

        return service.contraListbyOutlet(request, req);
    }
    /* contra list with pagination  end*/
    /*Update contra*/
    @PostMapping(path = "/update_contra")
    public Object updateContra(HttpServletRequest request) {
        JsonObject array = service.updateContra(request);
        return array.toString();
    }

    /*get contra by id*/
    @PostMapping(path = "/get_contra_by_id")
    public Object getContraById(HttpServletRequest request) {
        JsonObject array = service.getContraById(request);
        return array.toString();
    }
    /***** Delete Contra  ****/
    @PostMapping(path = "/delete_contra")
    public Object deleteContra(HttpServletRequest request) {
        JsonObject object = service.deleteContra(request);
        return object.toString();
    }

}
