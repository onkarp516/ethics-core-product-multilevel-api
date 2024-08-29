package in.truethics.ethics.ethicsapiv10.controller.masters;


import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class CountryController {
    @Autowired
    private CountryService service;

    /* get all Country  */
    @GetMapping(path = "/getCountry")
    public Object getCountry(HttpServletRequest request) {
        JsonObject res = service.getCountry(request);
        return res.toString();
    }

    /* get India Country  */
    @GetMapping(path = "/getIndiaCountry")
    public ResponseEntity<?> getIndiaCountry(HttpServletRequest request) {
        return ResponseEntity.ok(service.getIndiaCountry(request));
    }
}
