package in.truethics.ethics.ethicsapiv10.controller.barcode;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.Barcode_service.ProductBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ProductBatchNoController {

    @Autowired
    private ProductBatchService service;

    @PostMapping(path = "/get_Product_batch")
    public Object getProductBatch(HttpServletRequest request) {
        JsonObject jsonObject = service.getProductBatch(request);
        return jsonObject.toString();
    }

    /****** get Batch Number ******/
    @PostMapping(path = "/fetch_Product_batch")
    public Object fetchProductBatchNo(HttpServletRequest request) {
        JsonObject jsonObject = service.fetchProductBatchNo(request);
        return jsonObject.toString();
    }

    /****** Batch Details by id at Transactions ******/
    @PostMapping(path = "/transaction_batch_details")
    public Object batchTransactionsDetails(HttpServletRequest request) {
        return service.batchTransactionsDetails(request).toString();
    }

    /****** Create Batch while creating purchase invoice transactions ******/
    @PostMapping(path = "/create_batch_details")
    public Object createBatchDetails(HttpServletRequest request) {
        return service.createBatchDetails(request).toString();
    }
    /****** Edit Batch while creating or editing purchase invoice transactions ******/
    @PostMapping(path = "/edit_batch_details")
    public Object editBatchDetails(HttpServletRequest request) {
        return service.editBatchDetails(request).toString();
    }

   /**** Batch Details by id at Product List ******/
    @PostMapping(path = "/product_batch_details")
    public Object batchProductDetails(HttpServletRequest request) {
        return service.batchProductDetails(request).toString();
    }
}
