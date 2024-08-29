package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.FindProduct;
import in.truethics.ethics.ethicsapiv10.common.GenerateFiscalYear;
import in.truethics.ethics.ethicsapiv10.common.GenericDTData;
import in.truethics.ethics.ethicsapiv10.common.InventoryCommonPostings;
import in.truethics.ethics.ethicsapiv10.dto.ProductDTO;
import in.truethics.ethics.ethicsapiv10.dto.ProductUnitDTO;
import in.truethics.ethics.ethicsapiv10.dto.PurchaseProductData;
import in.truethics.ethics.ethicsapiv10.fileConfig.FileStorageProperties;
import in.truethics.ethics.ethicsapiv10.fileConfig.FileStorageService;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBarcode;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.*;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoiceDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.ProductBatchNoRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.product_barcode.ProductBarcodeRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurInvoiceDetailsUnitsRepository;
import in.truethics.ethics.ethicsapiv10.response.GenericDatatable;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductHsnRepository productHsnRepository;
    @Autowired
    private UnitsRepository unitsRepository;
    @Autowired
    private ProductUnitRepository productUnitRepository;
    @Autowired
    private TaxMasterRepository taxMasterRepository;
    @Autowired
    private PackingMasterRepository packingMasterRepository;
    @Autowired
    private ProductBarcodeRepository barcodeRepository;
    @Autowired
    FiscalYearRepository fiscalYearRepository;
    @Autowired
    private ProductOpeningStocksRepository openingStocksRepository;
    @Autowired
    private InventoryDetailsPostingsRepository inventoryDetailsPostingsRepository;
    @Autowired
    private InventoryCommonPostings inventoryCommonPostings;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private FindProduct findProduct;
    @Autowired
    private LevelARepository levelARepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private LevelBRepository levelBRepository;
    @Autowired
    private LevelCRepository levelCRepository;
    @Autowired
    private ProductBatchNoRepository productBatchNoRepository;
    private static final Logger productLogger = LogManager.getLogger(ProductService.class);
    @Autowired
    private TranxPurInvoiceDetailsUnitsRepository tranxPurInvoiceDetailsUnitsRepository;
    @Autowired
    private SubcategoryRepository subcategoryRepository;
    @Autowired
    private SubgroupRepository subgroupRepository;
    @Autowired
    private OutletRepository outletRepository;
    @Value("${spring.serversource.url}")
    private String serverUrl;
    @Autowired
    private ProductTaxDateMasterRepository productTaxDateMasterRepository;

    public JsonObject validateProduct(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        Long packageId = null;
        String productName = "";
        Product product = null;
        JsonObject result = new JsonObject();
        try {
            if (paramMap.containsKey("packageId") && !request.getParameter("packageId").equalsIgnoreCase("")) {
                packageId = Long.parseLong(request.getParameter("packageId"));
            }
            if (paramMap.containsKey("productName") && !request.getParameter("productName").equalsIgnoreCase(""))
                productName = request.getParameter("productName");
            if (users.getBranch() != null) {
                if (packageId != null) {
                    product = productRepository.findByduplicateProductPKWBR(users.getOutlet().getId(), users.getBranch().getId(), productName, packageId, true);
                } else {
                    product = productRepository.findByduplicateProductPNWBR(users.getOutlet().getId(), users.getBranch().getId(), productName, true);
                }
            } else if (packageId != null) {
                product = productRepository.findByduplicateProductPK(users.getOutlet().getId(), productName, packageId, true);
            } else {
                product = productRepository.findByduplicateProductPN(users.getOutlet().getId(), productName, true);
            }
            if (product != null) {
                result.addProperty("message", "Duplicate Product");
                result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            } else {
                result.addProperty("message", "new product");
                result.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in Product Validation:" + exceptionAsString);
        }
        return result;
    }

    private void setProductOpeningStocks(Product newProduct, Users users, double opening_qty, Units unit, PackingMaster packingMaster, Double openingRate, Double valuation) {
        ProductOpeningStocks openingStocks = new ProductOpeningStocks();
        openingStocks.setProduct(newProduct);
        openingStocks.setOpeningQty(opening_qty);
        openingStocks.setUnits(unit);
        openingStocks.setPackingMaster(packingMaster);
        openingStocks.setCreatedBy(users.getId());
        openingStocks.setOpeningStocks(openingRate);
        openingStocks.setOpeningValuation(valuation);
        openingStocks.setStatus(true);
        try {
            openingStocksRepository.save(openingStocks);
        } catch (Exception e) {
            productLogger.error("Error in setProductOpeningStocks()->>" + e.getMessage());
        }
    }

    /* get all products of outlet */
/*
    public Object getAllProduct(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<Product> productList = new ArrayList<>();
        if (users.getBranch() != null) {
            productList = productRepository.findByOutletIdAndBranchIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true);
        } else {
            productList = productRepository.findByOutletIdAndStatus(users.getOutlet().getId(), true);
        }
        List<PurchaseProductAllData> list = new ArrayList<>();
        try {
            for (Product mProduct : productList) {
                List<ProductUnitPacking> pUnits = productUnitRepository.findByProductIdAndStatus(mProduct.getId(), true);
                List<UnitDTO> units = new ArrayList<>();
                PurchaseProductAllData pData = new PurchaseProductAllData();
                pData.setId(mProduct.getId());
                pData.setProductName(mProduct.getProductName());
                pData.setProductCode(mProduct.getProductCode());
                pData.setHsnId(mProduct.getProductHsn().getId());
                pData.setHsnNo(mProduct.getProductHsn().getHsnNumber());
                pData.setTaxMasterId(mProduct.getTaxMaster().getId());
                pData.setIgst(mProduct.getTaxMaster().getIgst());
                pData.setSgst(mProduct.getTaxMaster().getSgst());
                pData.setCgst(mProduct.getTaxMaster().getCgst());
                pData.setIsSerialNumber(mProduct.getIsSerialNumber());
                pData.setIsBatchNumber(mProduct.getIsBatchNumber());
                pData.setIsNegativeStocks(mProduct.getIsNegativeStocks());
                pData.setGroupId(mProduct.getGroup().getId());
                pData.setGroupName(mProduct.getGroup().getGroupName());
                pData.setSubGroupId(mProduct.getSubgroup().getId());
                pData.setSubGroupName(mProduct.getSubgroup().getSubgroupName());
                if (mProduct.getCategory() != null) {
                    pData.setCategoryId(mProduct.getCategory().getId());
                    pData.setCategoryName(mProduct.getCategory().getCategoryName());
                }
                if (mProduct.getSubcategory() != null) {
                    pData.setSubCategoryId(mProduct.getSubcategory().getId());
                    pData.setSubCategoryName(mProduct.getSubcategory().getSubcategoryName());
                }
                for (ProductUnitPacking mUnit : pUnits) {
                    UnitDTO unitData = new UnitDTO();
                    unitData.setId(mUnit.getId());
                    unitData.setUnitName(mUnit.getUnits().getUnitName());
                    unitData.setUnitType(mUnit.getUnitType());
                    units.add(unitData);
                }
                pData.setUnits(units);
                list.add(pData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            productLogger.error("getAllProduct-> failed to getAllProduct" + e);
        }
        return list;
    }
*/

    /* Get Product by id for edit */
    public Object getProductById(HttpServletRequest request) {
        Long productId = Long.parseLong(request.getParameter("product_id"));
        Product mProduct = productRepository.findByIdAndStatus(productId, true);
        ResponseMessage responseMessage = new ResponseMessage();
        ProductDTO pData = new ProductDTO();
        try {
            if (mProduct != null) {
                pData.setId(mProduct.getId());
                pData.setProduct_name(mProduct.getProductName());
                pData.setSearch_code(mProduct.getProductCode());
                pData.setDescription(mProduct.getDescription());
                pData.setIsWarrantyApplicable(mProduct.getIsWarrantyApplicable());
                pData.setWarrantyDays(mProduct.getWarrantyDays());
                pData.setIsSerialNumber(mProduct.getIsSerialNumber());
                pData.setIsBatchNumber(mProduct.getIsBatchNumber());
                pData.setIsInventory(mProduct.getIsInventory());
                pData.setIsBrand(mProduct.getIsBrand());
                pData.setIsGroup(mProduct.getIsGroup());
                pData.setIsCategory(mProduct.getIsCategory());
                pData.setIsSubcategory(mProduct.getIsSubCategory());
                pData.setIsPackage(mProduct.getIsPackage());
                pData.setAlias(mProduct.getAlias());
                List<ProductUnitDTO> list = new ArrayList<>();
                List<ProductUnitPacking> units = productUnitRepository.findByProductIdAndStatus(mProduct.getId(), true);
                for (ProductUnitPacking mUnit : units) {
                    ProductUnitDTO pUnit = new ProductUnitDTO();
                    pUnit.setUnitConversion(mUnit.getUnitConversion());
                    pUnit.setUnitConvMargn(mUnit.getUnitConvMargn());
                    pUnit.setUnitId(mUnit.getUnits().getId());
                    pUnit.setUnitDetailId(mUnit.getId());
                    if (mUnit.getPackingMaster() != null) pUnit.setPackageId(mUnit.getPackingMaster().getId());
                    if (mUnit.getBrand() != null) pUnit.setBrandId(mUnit.getBrand().getId());
                    if (mUnit.getGroup() != null) pUnit.setGroupId(mUnit.getGroup().getId());
                    if (mUnit.getCategory() != null) pUnit.setCategoryId(mUnit.getCategory().getId());
                    if (mUnit.getSubcategory() != null) pUnit.setSubcategoryId(mUnit.getSubcategory().getId());
                    list.add(pUnit);
                }
                pData.setUnits(list);
                responseMessage.setMessage("success");
                responseMessage.setResponseStatus(HttpStatus.OK.value());
                responseMessage.setResponseObject(pData);
            } else {
                responseMessage.setMessage("error");
                responseMessage.setResponseStatus(HttpStatus.BAD_REQUEST.value());
                responseMessage.setResponseObject("");
            }
        } catch (Exception e) {
            e.printStackTrace();
            productLogger.error("getProductById-> failed to get ProductById" + e);
            //   productLogger.error("createService -> failed to update Product" + e);
        }
        return responseMessage;
    }

    public JsonObject getProductsOfOutlet(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<Product> productList = new ArrayList<>();
        Long branchId = null;
        if (users.getBranch() != null) {
            productList = productRepository.findByOutletIdAndBranchIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true);
            branchId = users.getBranch().getId();
        } else {
            productList = productRepository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        JsonObject finalResult = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (Product mProduct : productList) {
            JsonObject mObject = new JsonObject();
            mObject.addProperty("id", mProduct.getId());
            mObject.addProperty("product_name", mProduct.getProductName());
            mObject.addProperty("search_code", mProduct.getProductCode() != null ? mProduct.getProductCode() : "");
            mObject.addProperty("brand", mProduct.getBrand() != null ? mProduct.getBrand().getBrandName() : "");
            mObject.addProperty("packing", mProduct.getPackingMaster() != null ? mProduct.getPackingMaster().getPackName() : "");
            mObject.addProperty("barcode", mProduct.getBarcodeNo() != null ? mProduct.getBarcodeNo() : "");
            List<ProductUnitPacking> mUnits = productUnitRepository.findByProductId(mProduct.getId());
            if (mUnits != null && mUnits.size() > 0)
                mObject.addProperty("unit", mUnits.get(0).getUnits().getUnitName());
            else {
                mObject.addProperty("unit", "PCS");
            }
            if (mProduct.getIsBatchNumber()) {
                TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductIdOrderByIdDesc(mProduct.getId());
                if (tranxPurInvoiceDetailsUnits != null) {
                    mObject.addProperty("mrp", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getMrp() : 0.00);
                    mObject.addProperty("sales_rate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() : 0.00);
                } else {
                    ProductBatchNo productBatchNos = productBatchNoRepository.findTop1ByProductIdAndStatusOrderByIdDesc(mProduct.getId(), true);
                    mObject.addProperty("mrp", productBatchNos != null ? productBatchNos.getMrp() : 0.00);
                    mObject.addProperty("sales_rate", productBatchNos != null ? productBatchNos.getSalesRate() : 0.00);
                }
            } else {
                if (mUnits != null && mUnits.size() > 0) {
                    mObject.addProperty("mrp", mUnits.get(0).getMrp() != null ? mUnits.get(0).getMrp() : 0.00);
                    mObject.addProperty("sales_rate", mUnits.get(0).getMinRateA() != null ? mUnits.get(0).getMinRateA() : 0.00);
                }
                mObject.addProperty("unit", "PCS");
                mObject.addProperty("mrp", 0.00);
                mObject.addProperty("sales_rate", 0.00);
            }

            LocalDate currentDate = LocalDate.now();
            /*     fiscal year mapping  */
            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
//            System.out.println("Fisc Yesr"+fiscalYear);
            Double freeQty = inventoryCommonPostings.calculateFreeQty(mProduct.getId(), users.getOutlet().getId(), branchId, fiscalYear);
            Double openingStocks = 0.0;
            openingStocks = inventoryCommonPostings.calculateOpening(mProduct.getId(), users.getOutlet().getId(), branchId, fiscalYear);
            mObject.addProperty("opening_stocks", openingStocks);
            Double closingStocks = inventoryCommonPostings.getClosingStockProduct(mProduct.getId(), users.getOutlet().getId(), branchId, fiscalYear);
            Double currentStock = closingStocks + openingStocks + freeQty;
            mObject.addProperty("closing_stocks", currentStock);
            jsonArray.add(mObject);
        }
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("data", jsonArray);
        return finalResult;
    }

//    public Object getProductsOfOutletNew(@RequestBody Map<String, String> request, HttpServletRequest req) {
//        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
//        ResponseMessage responseMessage = new ResponseMessage();
//
//        System.out.println("request " + request + "  req=" + req);
//        Integer pageNo = Integer.parseInt(request.get("pageNo"));
//        Integer pageSize = Integer.parseInt(request.get("pageSize"));
//        String searchText = request.get("searchText");
//        String SearchText = "";
//        Boolean flag = false;
//        List<Product> products = new ArrayList<>();
//        List<Product> productList = new ArrayList<>();
//        List<ProductDTO> productDTOList = new ArrayList<>();
//        Long branchId = null;
//        if (users.getBranch() != null) {
//            productList = productRepository.findByOutletIdAndBranchIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true);
//            branchId = users.getBranch().getId();
//        } else {
//            productList = productRepository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
//        }
//        GenericDTData genericDTData = new GenericDTData();
//        try {
//            String query = "SELECT * FROM `product_tbl` WHERE status=1 AND outlet_id=" + users.getOutlet().getId();
//            if (users.getBranch() != null) {
//                query = query + " AND branch_id=" + users.getBranch().getId();
//            } else {
//                query = query + " AND branch_id IS NULL";
//
//            }
//
//            if (!searchText.equalsIgnoreCase("")) {
//                query = query + " AND product_name LIKE '%" + searchText + "%'";
//            }
//            String query1 = query;       //we get all lists in this list
//            System.out.println("query== " + query);
//
//            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;
//
//            Query q = entityManager.createNativeQuery(query, Product.class);
//            System.out.println("q ==" + q + "  product " + products);
//            products = q.getResultList();
//            Query q1 = entityManager.createNativeQuery(query1, Product.class);
//
//            productList = q1.getResultList();
//            System.out.println("Limit total rows " + productDTOList.size());
//
//            Integer total_pages = (productList.size() / pageSize);
//            if ((productList.size() % pageSize > 0)) {
//                total_pages = total_pages + 1;
//            }
//            System.out.println("total pages " + total_pages);
//            for (Product productListView : products) {
//                productDTOList.add(convertToDTDTO(productListView, users, branchId));
//            }
//            GenericDatatable<ProductDTO> data = new GenericDatatable<>(productDTOList, productList.size(),
//                    pageNo, pageSize, total_pages);
//            responseMessage.setResponseObject(data);
//            responseMessage.setResponseStatus(HttpStatus.OK.value());
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("Exception " + e.getMessage());
//
//            genericDTData.setRows(productDTOList);
//            genericDTData.setTotalRows(0);
//            responseMessage.setResponseStatus(HttpStatus.BAD_REQUEST.value());
//        }
//        return responseMessage;
//    }

//    private ProductDTO convertToDTDTO(Product productList, Users users, Long branchId) {
//        ProductDTO productDTO = new ProductDTO();
//        productDTO.setId(productList.getId());
//        productDTO.setProduct_name(productList.getProductName());
//        productDTO.setSearch_code(productList.getProductCode());
//        productDTO.setBrand(productList.getBrand().getBrandName());
//        productDTO.setPacking(productList.getPackingMaster().getPackName());
//        productDTO.setBarcode(productList.getBarcodeNo());
//        List<ProductUnitPacking> mUnits = productUnitRepository.findByProductId(productList.getId());
//        if (mUnits != null && mUnits.size() > 0)
//            productDTO.setUnit(mUnits.get(0).getUnits().getUnitName());
//        else {
//            productDTO.setUnit("PCS");
//        }
//
//        if (productList.getIsBatchNumber()) {
//            TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductIdOrderByIdDesc(productList.getId());
//            if (tranxPurInvoiceDetailsUnits != null) {
//                productDTO.setMrp(tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getMrp() : 0.00);
//                productDTO.setSales_rate(tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() : 0.00);
//            } else {
//                ProductBatchNo productBatchNos = productBatchNoRepository.findTop1ByProductIdAndStatusOrderByIdDesc(
//                        productList.getId(), true);
//                productDTO.setMrp(productBatchNos != null ? productBatchNos.getMrp() : 0.00);
//                productDTO.setSales_rate(productBatchNos != null ? productBatchNos.getSalesRate() : 0.00);
//                productDTO.setPurchase_rate(productBatchNos.getPurchaseRate() != null ? productBatchNos.getPurchaseRate() : 0.0);
//
//            }
//        } else {
//            if (mUnits != null && mUnits.size() > 0) {
//                productDTO.setMrp(mUnits.get(0).getMrp() != null ? mUnits.get(0).getMrp() : 0.00);
//                productDTO.setSales_rate(mUnits.get(0).getMinRateA() != null ? mUnits.get(0).getMinRateA() : 0.00);
//                productDTO.setPurchase_rate(mUnits.get(0).getPurchaseRate() != null ? mUnits.get(0).getPurchaseRate() : 0.0);
//            } else {
//                productDTO.setUnit("PCS");
//                productDTO.setMrp(0.00);
//                productDTO.setSales_rate(0.00);
//            }
//        }
//        LocalDate currentDate = LocalDate.now();
//        /*     fiscal year mapping  */
//        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
////            System.out.println("Fisc Yesr"+fiscalYear);
//        Double freeQty = inventoryCommonPostings.calculateFreeQty(productList.getId(), users.getOutlet().getId(), branchId, fiscalYear);
//        Double openingStocks = 0.0;
//        openingStocks = inventoryCommonPostings.calculateOpening(productList.getId(), users.getOutlet().getId(), branchId, fiscalYear);
//        productDTO.setOpening_stocks(openingStocks);
//        Double closingStocks = inventoryCommonPostings.getClosingStockProduct(productList.getId(), users.getOutlet().getId(), branchId, fiscalYear);
//        Double currentStock = closingStocks + openingStocks + freeQty;
//        productDTO.setClosing_stocks(currentStock);
//
//        return productDTO;
//
//    }


    public Object getProductsOfOutletNew(@RequestBody Map<String, String> request, HttpServletRequest req) {
        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Integer pageNo = Integer.parseInt(request.get("pageNo"));
        Integer pageSize = Integer.parseInt(request.get("pageSize"));
        String searchText = request.get("searchText");
        List products = new ArrayList<>();
        List<Product> productList = new ArrayList<>();
        List<ProductDTO> productDTOList = new ArrayList<>();
        Long branchId = null;
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT id FROM product_tbl WHERE status=1 AND outlet_id=" + users.getOutlet().getId();
            if (users.getBranch() != null) {
                query = query + " AND branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND branch_id IS NULL";

            }
            if (!searchText.equalsIgnoreCase("")) {
                query = query + " AND product_name LIKE '%" + searchText + "%'";
            }

            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;  //
            //Query q = entityManager.createNativeQuery(query, Product.class);
            //  products = q.getResultList();// limit of 50 product list
            Query q = entityManager.createNativeQuery(query);
            products = q.getResultList();
            String query1 = "SELECT COUNT(product_tbl.id) as totalcount FROM product_tbl WHERE product_tbl.status=? " + "AND product_tbl.outlet_id=?";
            if (users.getBranch() != null) {
                query1 = query1 + " AND product_tbl.branch_id=?";
            } else {
                query1 = query1 + " AND product_tbl.branch_id IS NULL";
            }
            Query q1 = entityManager.createNativeQuery(query1);
            q1.setParameter(1, true);
            q1.setParameter(2, users.getOutlet().getId());
            if (users.getBranch() != null) q1.setParameter(3, users.getOutlet().getId());
            //    Integer size = (Integer) q1.getSingleResult();
            int totalProducts = ((BigInteger) q1.getSingleResult()).intValue();
            Integer total_pages = (totalProducts / pageSize);
            if ((totalProducts % pageSize > 0)) {
                total_pages = total_pages + 1;
            }
            System.out.println("total pages " + total_pages);
            for (Object mProduct : products) {
                Product productListView = productRepository.findByIdAndStatus(Long.parseLong(mProduct.toString()), true);
                productDTOList.add(convertToDTDTO(productListView, users, branchId));
            }
            GenericDatatable<ProductDTO> data = new GenericDatatable<>(productDTOList, totalProducts, pageNo, pageSize, total_pages);
            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Exception:" + exceptionAsString);
            genericDTData.setRows(productDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }


    private ProductDTO convertToDTDTO(Product productList, Users users, Long branchId) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(productList.getId());
        productDTO.setProduct_name(productList.getProductName());
        productDTO.setSearch_code(productList.getProductCode());
        productDTO.setBrand(productList.getBrand().getBrandName());
        productDTO.setPacking(productList.getPackingMaster() != null ? productList.getPackingMaster().getPackName() : "");
        productDTO.setBarcode(productList.getBarcodeNo());
        productDTO.setMinimumStock(productList.getMinStock());
        List<ProductUnitPacking> mUnits = productUnitRepository.findByProductId(productList.getId());
        if (mUnits != null && mUnits.size() > 0) {
            productDTO.setUnit(mUnits.get(0).getUnits().getUnitName());
            productDTO.setUnitId(mUnits.get(0).getUnits().getId());
        } else {
            productDTO.setUnit("PCS");
        }

        if (productList.getIsBatchNumber()) {
            TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductIdOrderByIdDesc(productList.getId());
            if (tranxPurInvoiceDetailsUnits != null) {
                productDTO.setMrp(tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getMrp() : 0.00);
                productDTO.setSales_rate(tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() : 0.00);
            } else {
                ProductBatchNo productBatchNos = productBatchNoRepository.findTop1ByProductIdAndStatusOrderByIdDesc(productList.getId(), true);
                productDTO.setMrp(productBatchNos != null ? productBatchNos.getMrp() != null ? productBatchNos.getMrp() : 0.0 : 0.00);
                productDTO.setSales_rate(productBatchNos != null ? productBatchNos.getSalesRate() != null ? productBatchNos.getSalesRate() : 0.0 : 0.00);
                productDTO.setPurchase_rate(productBatchNos != null ? productBatchNos.getPurchaseRate() != null ? productBatchNos.getPurchaseRate() : 0.0 : 0.0);

            }
        } else {
            if (mUnits != null && mUnits.size() > 0) {
                productDTO.setMrp(mUnits.get(0).getMrp() != null ? mUnits.get(0).getMrp() : 0.00);
                productDTO.setSales_rate(mUnits.get(0).getMinRateA() != null ? mUnits.get(0).getMinRateA() : 0.00);
                productDTO.setPurchase_rate(mUnits.get(0).getPurchaseRate() != null ? mUnits.get(0).getPurchaseRate() : 0.0);
            } else {
                productDTO.setUnit("PCS");
                productDTO.setMrp(0.00);
                productDTO.setSales_rate(0.00);
            }
        }
        LocalDate currentDate = LocalDate.now();
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
        Double freeQty = inventoryCommonPostings.calculateFreeQty(productList.getId(), users.getOutlet().getId(), branchId, fiscalYear);
        Double openingStocks = 0.0;
        openingStocks = inventoryCommonPostings.calculateOpening(productList.getId(), users.getOutlet().getId(), branchId, fiscalYear);
        productDTO.setOpening_stocks(openingStocks);
        Double closingStocks = inventoryCommonPostings.getClosingStockProduct(productList.getId(), users.getOutlet().getId(), branchId, fiscalYear);

        Double currentStock = closingStocks + openingStocks + freeQty;
        productDTO.setClosing_stocks(currentStock);

        return productDTO;

    }

    //Product List API for product-minimum level stock
    public Object getProductsOfOutletNew1(@RequestBody Map<String, String> request, HttpServletRequest req) {
        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Integer pageNo = Integer.parseInt(request.get("pageNo"));
        Integer pageSize = Integer.parseInt(request.get("pageSize"));
        String searchText = request.get("searchText");

        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        if (request.containsKey("end_date") && request.containsKey("start_date")) {
            endDate = request.get("end_date");
            endDatep = LocalDate.parse(endDate);
            startDate = request.get("start_date");
            startDatep = LocalDate.parse(startDate);
        } else {
            List<Object[]> list = new ArrayList<>();
            list = fiscalYearRepository.findByStartDateAndEndDateOutletIdAndBranchIdAndStatus();
            System.out.println("list" + list);
            Object obj[] = list.get(0);
            System.out.println("start Date111:" + obj[0].toString());
            System.out.println("end Date:" + obj[1].toString());
            startDatep = LocalDate.parse(obj[0].toString());   //start date of fiscal year  2023-04-01
            endDatep = LocalDate.parse(obj[1].toString());      //end date of fiscal year   2024-03-31
        }
        List products = new ArrayList<>();
        List<Product> productList = new ArrayList<>();
        List<ProductDTO> productDTOList = new ArrayList<>();
        Long branchId = null;
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT id FROM product_tbl WHERE status=1 AND outlet_id=" + users.getOutlet().getId();
            if (users.getBranch() != null) {
                query = query + " AND branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND branch_id IS NULL";

            }
            if (!searchText.equalsIgnoreCase("")) {
                query = query + " AND product_name LIKE '%" + searchText + "%'";
            }

            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;  //
            //Query q = entityManager.createNativeQuery(query, Product.class);
            //  products = q.getResultList();// limit of 50 product list
            Query q = entityManager.createNativeQuery(query);
            products = q.getResultList();
            String query1 = "SELECT COUNT(product_tbl.id) as totalcount FROM product_tbl WHERE product_tbl.status=? " + "AND product_tbl.outlet_id=?";
            if (users.getBranch() != null) {
                query1 = query1 + " AND product_tbl.branch_id=?";
            } else {
                query1 = query1 + " AND product_tbl.branch_id IS NULL";
            }
            Query q1 = entityManager.createNativeQuery(query1);
            q1.setParameter(1, true);
            q1.setParameter(2, users.getOutlet().getId());
            if (users.getBranch() != null) q1.setParameter(3, users.getOutlet().getId());
            //    Integer size = (Integer) q1.getSingleResult();
            int totalProducts = ((BigInteger) q1.getSingleResult()).intValue();
            Integer total_pages = (totalProducts / pageSize);
            if ((totalProducts % pageSize > 0)) {
                total_pages = total_pages + 1;
            }

            for (Object mProduct : products) {
                Product productListView = productRepository.findByIdAndStatus(Long.parseLong(mProduct.toString()), true);
                productDTOList.add(convertToDTDTO1(productListView, users, branchId));
            }
            GenericDatatable<ProductDTO> data = new GenericDatatable<>(productDTOList, totalProducts, pageNo, pageSize, total_pages);
            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Exception:" + exceptionAsString);
            genericDTData.setRows(productDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }

    //
    private ProductDTO convertToDTDTO1(Product productList, Users users, Long branchId) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(productList.getId());
        productDTO.setProduct_name(productList.getProductName());
        productDTO.setSearch_code(productList.getProductCode());
        productDTO.setBrand(productList.getBrand().getBrandName());
        productDTO.setPacking(productList.getPackingMaster() != null ? productList.getPackingMaster().getPackName() : "");
        productDTO.setBarcode(productList.getBarcodeNo());
        productDTO.setMinimumStock(productList.getMinStock());
        productDTO.setMaximumStock(productList.getMaxStock());
        productDTO.setCategory(productList.getCategory() != null ? productList.getCategory().getCategoryName() : "");
        productDTO.setGroup(productList.getGroup() != null ? productList.getGroup().getGroupName() : "");
        productDTO.setSubGroup(productList.getSubgroup() != null ? productList.getSubgroup().getSubgroupName() : "");
        productDTO.setHsn(productList.getProductHsn() != null ? productList.getProductHsn().getHsnNumber() : "");
        productDTO.setTaxType(productList.getTaxType());
        productDTO.setPurchase_rate(productList.getPurchaseRate());
        List<ProductUnitPacking> mUnits = productUnitRepository.findByProductId(productList.getId());
        if (mUnits != null && mUnits.size() > 0) {
            productDTO.setUnit(mUnits.get(0).getUnits().getUnitName());
            productDTO.setUnitId(mUnits.get(0).getUnits().getId());
        } else {
            productDTO.setUnit("PCS");
        }

        if (productList.getIsBatchNumber()) {
            TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductIdOrderByIdDesc(productList.getId());
            if (tranxPurInvoiceDetailsUnits != null) {
                productDTO.setMrp(tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getMrp() : 0.00);
                productDTO.setSales_rate(tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() : 0.00);
            } else {
                ProductBatchNo productBatchNos = productBatchNoRepository.findTop1ByProductIdAndStatusOrderByIdDesc(productList.getId(), true);
                productDTO.setMrp(productBatchNos != null ? productBatchNos.getMrp() != null ? productBatchNos.getMrp() : 0.0 : 0.00);
                productDTO.setSales_rate(productBatchNos != null ? productBatchNos.getSalesRate() != null ? productBatchNos.getSalesRate() : 0.0 : 0.00);
                productDTO.setPurchase_rate(productBatchNos != null ? productBatchNos.getPurchaseRate() != null ? productBatchNos.getPurchaseRate() : 0.0 : 0.0);

            }
        } else {
            if (mUnits != null && mUnits.size() > 0) {
                productDTO.setMrp(mUnits.get(0).getMrp() != null ? mUnits.get(0).getMrp() : 0.00);
                productDTO.setSales_rate(mUnits.get(0).getMinRateA() != null ? mUnits.get(0).getMinRateA() : 0.00);
                productDTO.setPurchase_rate(mUnits.get(0).getPurchaseRate() != null ? mUnits.get(0).getPurchaseRate() : 0.0);
            } else {
                productDTO.setUnit("PCS");
                productDTO.setMrp(0.00);
                productDTO.setSales_rate(0.00);
            }
        }
        LocalDate currentDate = LocalDate.now();
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
        Double freeQty = inventoryCommonPostings.calculateFreeQty(productList.getId(), users.getOutlet().getId(), branchId, fiscalYear);
        Double openingStocks = 0.0;
        openingStocks = inventoryCommonPostings.calculateOpening(productList.getId(), users.getOutlet().getId(), branchId, fiscalYear);
        productDTO.setOpening_stocks(openingStocks);
        Double closingStocks = inventoryCommonPostings.getClosingStockProduct(productList.getId(), users.getOutlet().getId(), branchId, fiscalYear);

        Double currentStock = closingStocks + openingStocks + freeQty;
        productDTO.setClosing_stocks(currentStock);

        return productDTO;

    }

    /****** new Architecture : added Level A ,Level B , Level C and units in Product ,PK visit *****/
    public JsonArray getUnitBrandsFlavourPackageUnitsCommonNew(Long product_id) {
        JsonArray mLevelArray = new JsonArray();
        Product mProduct = productRepository.findByIdAndStatus(product_id, true);
        List<Long> levelaArray = productUnitRepository.findLevelAIdDistinct(product_id);
        for (Long mLeveA : levelaArray) {
            Long levelAId = null;
            JsonObject levelaJsonObject = new JsonObject();
            LevelA levelA = null;
            if (mLeveA != null) {
                levelA = levelARepository.findByIdAndStatus(mLeveA, true);
                if (levelA != null) {
                    levelAId = levelA.getId();
                    levelaJsonObject.addProperty("value", levelA.getId());
                    levelaJsonObject.addProperty("label", levelA.getLevelName());
                }
            } else {
                levelaJsonObject.addProperty("value", "");
                levelaJsonObject.addProperty("label", "");
            }
            JsonArray levelBArray = new JsonArray();
            List<Long> levelBunits = productUnitRepository.findByProductsLevelB(product_id, mLeveA);
            for (Long mLeveB : levelBunits) {
                Long levelBId = null;
                JsonObject levelbJsonObject = new JsonObject();
                LevelB levelB = null;
                if (mLeveB != null) {
                    levelB = levelBRepository.findByIdAndStatus(mLeveB, true);
                    if (levelB != null) {
                        levelBId = levelB.getId();
                        levelbJsonObject.addProperty("value", levelB.getId());
                        levelbJsonObject.addProperty("label", levelB.getLevelName());
                    }
                } else {
                    levelbJsonObject.addProperty("value", "");
                    levelbJsonObject.addProperty("label", "");
                }
                JsonArray levelCArray = new JsonArray();
                List<Long> levelCunits = productUnitRepository.findByProductsLevelC(product_id, mLeveA, mLeveB);
                for (Long mLeveC : levelCunits) {
                    Long levelCId = null;
                    JsonObject levelcJsonObject = new JsonObject();
                    LevelC levelC = null;
                    if (mLeveC != null) {
                        levelC = levelCRepository.findByIdAndStatus(mLeveC, true);
                        if (levelC != null) {
                            levelCId = levelC.getId();
                            levelcJsonObject.addProperty("value", levelC.getId());
                            levelcJsonObject.addProperty("label", levelC.getLevelName());
                        }
                    } else {
                        levelcJsonObject.addProperty("value", "");
                        levelcJsonObject.addProperty("label", "");
                    }
                    List<Object[]> unitList = productUnitRepository.findUniqueUnitsByProductId(product_id, mLeveA, mLeveB, mLeveC);
                    JsonArray unitArray = new JsonArray();
                    for (int j = 0; j < unitList.size(); j++) {
                        Object[] objects = unitList.get(j);

                        Long unitId = Long.parseLong(objects[0].toString());
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("value", Long.parseLong(objects[0].toString()));
                        jsonObject.addProperty("unitId", Long.parseLong(objects[0].toString()));
                        jsonObject.addProperty("label", objects[1].toString());
                        jsonObject.addProperty("unitName", objects[1].toString());
                        jsonObject.addProperty("unitCode", objects[2].toString());
                        jsonObject.addProperty("unitConversion", objects[3].toString());
                        unitArray.add(jsonObject);
                    }
                    levelcJsonObject.add("unitOpts", unitArray);
                    levelCArray.add(levelcJsonObject);
                }
                levelbJsonObject.add("levelCOpts", levelCArray);
                levelBArray.add(levelbJsonObject);
            }
            levelaJsonObject.add("levelBOpts", levelBArray);
            mLevelArray.add(levelaJsonObject);
        }
        return mLevelArray;
    }

    public Object updateProduct(HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        Product product = productRepository.findByIdAndStatus(Long.parseLong(request.getParameter("productId")), true);
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        product.setProductName(request.getParameter("productName").trim());
        if (paramMap.containsKey("productCode")) product.setProductCode(request.getParameter("productCode"));
        else product.setProductCode("");
        if (paramMap.containsKey("productDescription"))
            product.setDescription(request.getParameter("productDescription"));
        else product.setDescription("");
        product.setStatus(true);
        if (paramMap.containsKey("barcodeNo")) product.setBarcodeNo(request.getParameter("barcodeNo"));
        else product.setBarcodeNo("");
        if (paramMap.containsKey("isSerialNo"))
            product.setIsSerialNumber(Boolean.parseBoolean(request.getParameter("isSerialNo")));
        product.setIsBatchNumber(Boolean.parseBoolean(request.getParameter("isBatchNo")));
        product.setIsInventory(Boolean.parseBoolean(request.getParameter("isInventory")));
        if (paramMap.containsKey("isWarranty")) {
            product.setIsWarrantyApplicable(Boolean.parseBoolean(request.getParameter("isWarranty")));
            if (Boolean.parseBoolean(request.getParameter("isWarranty"))) {
                product.setWarrantyDays(Integer.parseInt(request.getParameter("nodays")));
            } else {
                product.setWarrantyDays(0);
            }
        }
        product.setCreatedBy(users.getId());
        /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
        if (paramMap.containsKey("shelfId")) product.setShelfId(request.getParameter("shelfId"));
        else product.setShelfId("");
        if (paramMap.containsKey("barcodeSaleQuantity"))
            product.setBarcodeSalesQty(Double.parseDouble(request.getParameter("barcodeSaleQuantity")));
        if (paramMap.containsKey("margin")) product.setMarginPer(Double.parseDouble(request.getParameter("margin")));
        else product.setMarginPer(0.0);
        PackingMaster mPackingMaster = null;
        Group mGroupMaster = null;
        Brand mBrandMaster = null;
        Category mCategoryMaster = null;
        if (paramMap.containsKey("brandId")) {
            mBrandMaster = brandRepository.findByIdAndStatus(Long.parseLong(request.getParameter("brandId")), true);
            product.setBrand(mBrandMaster);
        } else {
            product.setBrand(null);
        }
        if (paramMap.containsKey("packagingId")) {
            mPackingMaster = packingMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("packagingId")), true);
            product.setPackingMaster(mPackingMaster);
        } else {
            product.setPackingMaster(null);
        }
        if (paramMap.containsKey("groupId")) {
            mGroupMaster = groupRepository.findByIdAndStatus(Long.parseLong(request.getParameter("groupId")), true);
            product.setGroup(mGroupMaster);
        } else {
            product.setGroup(null);
        }
        if (paramMap.containsKey("categoryId")) {
            mCategoryMaster = categoryRepository.findByIdAndStatus(Long.parseLong(request.getParameter("categoryId")), true);
            product.setCategory(mCategoryMaster);
        } else {
            product.setCategory(null);
        }
        if (paramMap.containsKey("weight")) product.setWeight(Double.parseDouble(request.getParameter("weight")));
        else product.setWeight(0.0);

        if (paramMap.containsKey("weightUnit")) product.setWeightUnit(request.getParameter("weightUnit"));
        else product.setWeightUnit("");
        if (paramMap.containsKey("disPer1"))
            product.setDiscountInPer(Double.parseDouble(request.getParameter("disPer1")));
        else product.setDiscountInPer(0.0);
        if (paramMap.containsKey("hsnNo")) {
            ProductHsn productHsn = productHsnRepository.findByIdAndStatus(Long.parseLong(request.getParameter("hsnNo")), true);
            if (productHsn != null) {
                product.setProductHsn(productHsn);
            }
        }
        if (paramMap.containsKey("tax")) {
            LocalDate applicableDate = null;
            TaxMaster taxMaster = taxMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("tax")), true);
            if (taxMaster != null) {
                product.setTaxMaster(taxMaster);
            }
            if (paramMap.containsKey("taxApplicableDate"))
                product.setApplicableDate(LocalDate.parse(request.getParameter("taxApplicableDate")));
          /* inserting into ProductTax Master to maintain tax information of Product,
            /***** End of inserting into ProductTax Master  *****/
        }
        if (paramMap.containsKey("taxType")) product.setTaxType(request.getParameter("taxType"));
        if (paramMap.containsKey("igst")) product.setIgst(Double.parseDouble(request.getParameter("igst")));
        if (paramMap.containsKey("cgst")) product.setCgst(Double.parseDouble(request.getParameter("cgst")));
        if (paramMap.containsKey("sgst")) product.setSgst(Double.parseDouble(request.getParameter("sgst")));
        if (paramMap.containsKey("minStock")) product.setMinStock(Double.parseDouble(request.getParameter("minStock")));
        if (paramMap.containsKey("maxStock")) product.setMaxStock(Double.parseDouble(request.getParameter("maxStock")));
        /**** END ****/
        try {
            Product newProduct = productRepository.save(product);
            JsonParser parser = new JsonParser();
            String jsonStr = request.getParameter("mstPackaging");
            JsonElement tradeElement = parser.parse(jsonStr);
            JsonArray array = tradeElement.getAsJsonArray();
            for (JsonElement mList : array) {
                JsonObject object = mList.getAsJsonObject();
                LevelA levelA = null; //brand
                LevelB levelB = null;//group
                LevelC levelC = null;//category
                /**** LevelA Master ****/
                if (!object.get("levela_id").getAsString().equalsIgnoreCase("")) {
                    levelA = levelARepository.findByIdAndStatus(object.get("levela_id").getAsLong(), true);
                }
                JsonArray leveBArray = object.get("levelb").getAsJsonArray();
                for (JsonElement mLevelB : leveBArray) {
                    JsonObject mLevelBAsJsonObject = mLevelB.getAsJsonObject();
                    /**** LevelB Master ****/
                    if (!mLevelBAsJsonObject.get("levelb_id").getAsString().equalsIgnoreCase("")) {
                        levelB = levelBRepository.findByIdAndStatus(mLevelBAsJsonObject.get("levelb_id").getAsLong(), true);
                    }
                    JsonArray levelCArray = mLevelBAsJsonObject.get("levelc").getAsJsonArray();
                    for (JsonElement mLevelC : levelCArray) {
                        JsonObject mLevelCObject = mLevelC.getAsJsonObject();
                        /**** LevelC Master ****/
                        if (!mLevelCObject.get("levelc_id").getAsString().equalsIgnoreCase("")) {
                            levelC = levelCRepository.findByIdAndStatus(mLevelCObject.get("levelc_id").getAsLong(), true);
                        }
                        JsonArray unitsArray = mLevelCObject.get("units").getAsJsonArray();
                        for (JsonElement mUnitsList : unitsArray) {
                            ProductUnitPacking productUnitPacking = new ProductUnitPacking();
                            JsonObject mUnitObject = mUnitsList.getAsJsonObject();
                            Long details_id = mUnitObject.get("details_id").getAsLong();
                            if (details_id != 0) {
                                productUnitPacking = productUnitRepository.findByIdAndStatus(details_id, true);
                            } else {
                                productUnitPacking = new ProductUnitPacking();
                                productUnitPacking.setStatus(true);
                            }
                            Units unit = unitsRepository.findByIdAndStatus(mUnitObject.get("unit_id").getAsLong(), true);
                            productUnitPacking.setUnits(unit);
                            productUnitPacking.setUnitConversion(mUnitObject.get("unit_conv").getAsDouble());
                            productUnitPacking.setUnitConvMargn(mUnitObject.get("unit_marg").getAsDouble());
                            if (mUnitObject.get("isNegativeStocks").getAsInt() == 1) {
                                productUnitPacking.setIsNegativeStocks(true);
                            } else {
                                productUnitPacking.setIsNegativeStocks(false);
                            }
                            productUnitPacking.setMrp(mUnitObject.get("mrp").getAsDouble());
                            productUnitPacking.setPurchaseRate(mUnitObject.get("purchase_rate").getAsDouble());
                            productUnitPacking.setMinRateA(mUnitObject.get("min_rate_a").getAsDouble());//sales Rate
                            productUnitPacking.setMinRateB(mUnitObject.get("min_rate_b").getAsDouble());
                            productUnitPacking.setMinRateC(mUnitObject.get("min_rate_c").getAsDouble());
                            productUnitPacking.setStatus(true);
                            productUnitPacking.setProduct(newProduct);
                            productUnitPacking.setCreatedBy(users.getId());
                            /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
                            productUnitPacking.setMinQty(mUnitObject.get("min_qty").getAsDouble());
                            productUnitPacking.setMaxQty(mUnitObject.get("max_qty").getAsDouble());
                            productUnitPacking.setLevelA(levelA);
                            productUnitPacking.setLevelB(levelB);
                            productUnitPacking.setLevelC(levelC);
                            try {
                                productUnitRepository.save(productUnitPacking);
                            } catch (Exception e) {
                                System.out.println("Exception:" + e.getMessage());
                                productLogger.error("Error in Product Creation:" + e.getMessage());
                            }
                            /****** Inserting Product Opening Stocks ******/
                            JsonArray mBatchJsonArray = mUnitObject.getAsJsonArray("batchList");
                            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
                            for (JsonElement mBatchElement : mBatchJsonArray) {
                                ProductBatchNo productBatchNo = null;
                                JsonObject mBatchJsonObject = mBatchElement.getAsJsonObject();
                                ProductBatchNo mproductBatchNo = null;
                                Long id = mBatchJsonObject.get("id").getAsLong();
                                if (mBatchJsonObject.get("isOpeningbatch").getAsBoolean()) {
                                    String batch_id = mBatchJsonObject.get("batch_id").getAsString();
                                    if (batch_id.equalsIgnoreCase("")) {
                                        mproductBatchNo = new ProductBatchNo();
                                        mproductBatchNo.setStatus(true);
                                        if (fiscalYear != null) mproductBatchNo.setFiscalYear(fiscalYear);
                                    } else
                                        mproductBatchNo = productBatchNoRepository.findByIdAndStatus(Long.parseLong(batch_id), true);
                                    if (mBatchJsonObject.has("b_no"))
                                        mproductBatchNo.setBatchNo(mBatchJsonObject.get("b_no").getAsString());
                                    if (mBatchJsonObject.has("b_mrp"))
                                        mproductBatchNo.setMrp(mBatchJsonObject.get("b_mrp").getAsDouble());
                                    if (mBatchJsonObject.has("b_purchase_rate"))
                                        mproductBatchNo.setPurchaseRate(mBatchJsonObject.get("b_purchase_rate").getAsDouble());
                                    if (mBatchJsonObject.has("b_sale_rate"))
                                        mproductBatchNo.setSalesRate(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                                    mproductBatchNo.setMinRateA(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                                    if (mBatchJsonObject.has("b_free_qty"))
                                        mproductBatchNo.setFreeQty(mBatchJsonObject.get("b_free_qty").getAsDouble());
                                    if (mBatchJsonObject.has("b_manufacturing_date") && !mBatchJsonObject.get("b_manufacturing_date").getAsString().equalsIgnoreCase(""))
                                        mproductBatchNo.setManufacturingDate(LocalDate.parse(mBatchJsonObject.get("b_manufacturing_date").getAsString()));
                                    if (mBatchJsonObject.has("b_expiry") && !mBatchJsonObject.get("b_expiry").getAsString().equalsIgnoreCase(""))
                                        mproductBatchNo.setExpiryDate(LocalDate.parse(mBatchJsonObject.get("b_expiry").getAsString()));
                                    mproductBatchNo.setUnits(unit);
                                    productBatchNo = productBatchNoRepository.save(mproductBatchNo);
                                }
                                try {
                                    ProductOpeningStocks newOpeningStock = null;
                                    if (id != 0) {
                                        newOpeningStock = openingStocksRepository.findByIdAndStatus(id, true);
                                    } else {
                                        newOpeningStock = new ProductOpeningStocks();
                                        newOpeningStock.setProduct(newProduct);
                                        newOpeningStock.setUnits(unit);
                                        newOpeningStock.setPackingMaster(mPackingMaster);
                                        newOpeningStock.setBrand(mBrandMaster);
                                        newOpeningStock.setGroup(mGroupMaster);
                                        newOpeningStock.setCategory(mCategoryMaster);
                                        newOpeningStock.setLevelA(levelA);
                                        newOpeningStock.setLevelB(levelB);
                                        newOpeningStock.setLevelC(levelC);
                                        newOpeningStock.setStatus(true);
                                        if (fiscalYear != null) newOpeningStock.setFiscalYear(fiscalYear);
                                    }
                                    newOpeningStock.setOpeningStocks(Double.parseDouble(mBatchJsonObject.get("opening_qty").getAsString()));
                                    newOpeningStock.setProductBatchNo(productBatchNo);
                                    if (mBatchJsonObject.has("b_free_qty"))
                                        newOpeningStock.setFreeOpeningQty(mBatchJsonObject.get("b_free_qty").getAsDouble());
                                    if (mBatchJsonObject.has("b_mrp"))
                                        newOpeningStock.setMrp(mBatchJsonObject.get("b_mrp").getAsDouble());
                                    if (mBatchJsonObject.has("b_purchase_rate"))
                                        newOpeningStock.setPurchaseRate(mBatchJsonObject.get("b_purchase_rate").getAsDouble());
                                    if (mBatchJsonObject.has("b_sale_rate"))
                                        newOpeningStock.setSalesRate(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                                    if (mBatchJsonObject.has("b_manufacturing_date") && !mBatchJsonObject.get("b_manufacturing_date").getAsString().equalsIgnoreCase(""))
                                        newOpeningStock.setManufacturingDate(LocalDate.parse(mBatchJsonObject.get("b_manufacturing_date").getAsString()));
                                    if (mBatchJsonObject.has("b_expiry") && !mBatchJsonObject.get("b_expiry").getAsString().equalsIgnoreCase(""))
                                        newOpeningStock.setExpiryDate(LocalDate.parse(mBatchJsonObject.get("b_expiry").getAsString()));
                                    newOpeningStock.setCosting(mBatchJsonObject.get("b_costing").getAsDouble());
                                    try {
                                        openingStocksRepository.save(newOpeningStock);
                                    } catch (Exception e) {
                                        productLogger.error("Exception:" + e.getMessage());
                                    }
                                } catch (Exception e) {
                                  /*  responseObject.setMessage("Error in Product Creation");
                                    responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());*/
                                    e.printStackTrace();
                                    System.out.println("Exception " + e.getMessage());
                                }
                            }
                        }//Units
                    }//Level C
                }//Level B
            }//Level A

            /*** delete Product units while updating product ***/
            String delJsonStr = request.getParameter("rowDelDetailsIds");
            JsonElement delElement = parser.parse(delJsonStr);
            JsonArray delArray = delElement.getAsJsonArray();
            for (JsonElement mDelList : delArray) {
                JsonObject mDelObject = mDelList.getAsJsonObject();
                Long delId = mDelObject.get("del_id").getAsLong();
                ProductUnitPacking mUnitDel = productUnitRepository.findByIdAndStatus(delId, true);
                try {
                    mUnitDel.setStatus(false);
                    productUnitRepository.save(mUnitDel);
                } catch (Exception e) {
                    productLogger.error("Exception in Product Delete:" + e.getMessage());
                    System.out.println("Exception e:" + e.getMessage());
                }
            }
            responseMessage.setMessage("Product updated Successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            productLogger.error("updateProduct-> failed to updateProduct" + e);
            System.out.println(e.getMessage());
            responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseMessage.setMessage("Internal Server Error");
        }
        return responseMessage;
    }

    /* get Product Level, Multilvel Architecture PK Visit *****/
    public JsonObject getProductLevelsNew(Product mProduct, Users users) {
        JsonObject levelAList = new JsonObject();
        /***** Level A Array of Product ****/
        JsonArray levelaArray = new JsonArray();
        List<LevelA> list = new ArrayList<>();
        if (users.getBranch() != null) {
            list = levelARepository.findByOutletIdAndStatusAndBranchId(users.getOutlet().getId(), true, users.getBranch().getId());
        } else {
            list = levelARepository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        for (LevelA mLevelA : list) {
            JsonObject levelAJsonObject = new JsonObject();
            levelAJsonObject.addProperty("id", mLevelA.getId());
            levelAJsonObject.addProperty("levelName", mLevelA.getLevelName());
            levelaArray.add(levelAJsonObject);
        }
        levelAList.add("levelALst", levelaArray);
        /***** LevelB Array of Product ****/
        JsonArray levelBJsonArray = new JsonArray();
        List<LevelB> levelBList = new ArrayList<>();
        if (users.getBranch() != null) {
            levelBList = levelBRepository.findByOutletIdAndStatusAndBranchId(users.getOutlet().getId(), true, users.getBranch().getId());
        } else {
            levelBList = levelBRepository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        for (LevelB mLevelB : levelBList) {
            JsonObject levelBJsonObject = new JsonObject();
            levelBJsonObject.addProperty("id", mLevelB.getId());
            levelBJsonObject.addProperty("levelName", mLevelB.getLevelName());
            levelBJsonArray.add(levelBJsonObject);
        }
        levelAList.add("levelBLst", levelBJsonArray);

        /***** LevelC Array of Product ****/
        JsonArray levelCJsonArray = new JsonArray();
        List<LevelC> levelCList = new ArrayList<>();
        if (users.getBranch() != null) {
            levelCList = levelCRepository.findByOutletIdAndBranchIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true);
        } else {
            levelCList = levelCRepository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        for (LevelC mLevelC : levelCList) {
            JsonObject levelCJsonObject = new JsonObject();
            levelCJsonObject.addProperty("id", mLevelC.getId());
            levelCJsonObject.addProperty("levelName", mLevelC.getLevelName());
            levelCJsonArray.add(levelCJsonObject);
        }
        levelAList.add("levelCLst", levelCJsonArray);
        return levelAList;
    }

    public JsonArray getUnitPackageCommon(Long product_id, Boolean isPakage) {
        JsonArray packArray = new JsonArray();
        int mCount = 0, mCounts = 0;
        /* for No Packaging */
        if (isPakage == false) {

            JsonObject mObject = new JsonObject();
            mObject.addProperty("id", "");
            mObject.addProperty("pack_name", "");
            /*   product units list*/
            List<ProductUnitPacking> productUnitPackings = productUnitRepository.findByProductId(product_id);
            JsonArray unitArray = new JsonArray();
            for (ProductUnitPacking mUnits : productUnitPackings) {
                JsonObject mUnitsObj = new JsonObject();
                mCount++;
                mUnitsObj.addProperty("units_id", mUnits.getUnits().getId());
                mUnitsObj.addProperty("details_id", mUnits.getId());
                mUnitsObj.addProperty("unit_name", mUnits.getUnits().getUnitName());
                mUnitsObj.addProperty("unit_conv", mUnits.getUnitConversion());
                mUnitsObj.addProperty("unit_marg", mUnits.getUnitConvMargn());
              /*  mUnitsObj.addProperty("disc_per", mUnits.getDiscountInPer());
                mUnitsObj.addProperty("min_qty", mUnits.getMinQty());
                mUnitsObj.addProperty("max_qty", mUnits.getMaxQty());*/
                mUnitsObj.addProperty("mrp", mUnits.getMrp());
                if (mUnits.getIsNegativeStocks() != null)
                    mUnitsObj.addProperty("isNegativeStocks", mUnits.getIsNegativeStocks() == true ? 1 : 0);
                else mUnitsObj.addProperty("isNegativeStocks", 0);
                mUnitsObj.addProperty("purchase_rate", mUnits.getPurchaseRate());
                mUnitsObj.addProperty("rateA", mUnits.getMinRateA() != null ? mUnits.getMinRateA() : 0);
                mUnitsObj.addProperty("rateB", mUnits.getMinRateB() != null ? mUnits.getMinRateB() : 0);
                mUnitsObj.addProperty("rateC", mUnits.getMinRateC() != null ? mUnits.getMinRateC() : 0);
                // mUnitsObj.addProperty("sales_rate", mUnits.getSalesRate());
               /* mUnitsObj.addProperty("min_sales_rate", mUnits.getMinSalesRate());
                mUnitsObj.addProperty("disc_amt", mUnits.getDiscountInAmt());
                mUnitsObj.addProperty("opening_qty", mUnits.getOpeningQty());
                mUnitsObj.addProperty("opening_valution", mUnits.getOpeningValution());*/
                mUnitsObj.addProperty("hsnId", mUnits.getProductHsn() != null ? mUnits.getProductHsn().getId() : null);
                mUnitsObj.addProperty("taxMasterId", mUnits.getTaxMaster() != null ? mUnits.getTaxMaster().getId() : null);
                mUnitsObj.addProperty("applicableDate", mUnits.getTaxApplicableDate() != null ? mUnits.getTaxApplicableDate().toString() : "");
                mUnitsObj.addProperty("igst", mUnits.getTaxMaster().getIgst());
                mUnitsObj.addProperty("cgst", mUnits.getTaxMaster().getCgst());
                mUnitsObj.addProperty("sgst", mUnits.getTaxMaster().getSgst());
                unitArray.add(mUnitsObj);
            }
            if (mCount == 1) {
                mObject.addProperty("is_multi_unit", false);
            } else {
                mObject.addProperty("is_multi_unit", true);
            }
            mObject.addProperty("unitCount", mCount);
            mObject.add("units", unitArray);
            packArray.add(mObject);
        } else {

            /*  product packing List*/
            List<Long> unitPackingList = new ArrayList<>();
            unitPackingList = productUnitRepository.findProductIdDistinct(product_id);

            if (unitPackingList != null && unitPackingList.size() > 0) {
                for (Long mPack : unitPackingList) {
                    mCounts = 0;
                    JsonObject mObject = new JsonObject();
                    PackingMaster mPacking = null;
                    List<ProductUnitPacking> productUnitPackings = new ArrayList<>();
                    if (mPack != null) {
                        mPacking = packingMasterRepository.findById(mPack).get();
                        mObject.addProperty("id", mPacking.getId());
                        mObject.addProperty("pack_name", mPacking.getPackName());
                        productUnitPackings = productUnitRepository.findByProductIdAndPackingMasterId(product_id, mPack);
                    } else {
                        mObject.addProperty("id", "");
                        mObject.addProperty("pack_name", "");
                        productUnitPackings = productUnitRepository.findByProductIdAndPackingIsNULL(product_id);
                    }
                    /*product units list*/

                    JsonArray unitArray = new JsonArray();
                    for (ProductUnitPacking mUnits : productUnitPackings) {

                        JsonObject mUnitsObj = new JsonObject();
                        mUnitsObj.addProperty("units_id", mUnits.getUnits().getId());
                        mUnitsObj.addProperty("details_id", mUnits.getId());
                        mUnitsObj.addProperty("unit_name", mUnits.getUnits().getUnitName());
                        mUnitsObj.addProperty("unit_conv", mUnits.getUnitConversion());
                        mUnitsObj.addProperty("unit_marg", mUnits.getUnitConvMargn());
//                        mUnitsObj.addProperty("max_qty", mUnits.getMaxQty());
//                        mUnitsObj.addProperty("min_qty", mUnits.getMinQty());
//                        mUnitsObj.addProperty("disc_per", mUnits.getDiscountInPer());
                        mUnitsObj.addProperty("mrp", mUnits.getMrp());
                        mUnitsObj.addProperty("purchase_rate", mUnits.getPurchaseRate());
//                        mUnitsObj.addProperty("sales_rate", mUnits.getSalesRate());
                        mUnitsObj.addProperty("rateA", mUnits.getMinRateA() != null ? mUnits.getMinRateA() : 0);
                        mUnitsObj.addProperty("rateB", mUnits.getMinRateB() != null ? mUnits.getMinRateB() : 0);
                        mUnitsObj.addProperty("rateC", mUnits.getMinRateC() != null ? mUnits.getMinRateC() : 0);
                        unitArray.add(mUnitsObj);
                    }
                    if (mCounts == 1) {
                        mObject.addProperty("is_multi_unit", false);
                    } else {
                        mObject.addProperty("is_multi_unit", true);
                    }
                    mObject.addProperty("unitCount", mCounts);
                    /*    end of product units list*/
                    mObject.add("units", unitArray);
                    packArray.add(mObject);


                }
            }
        }
        return packArray;
    }

    public JsonObject getProduct(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<Product> productList = new ArrayList<>();
        if (users.getBranch() != null) {
            productList = productRepository.findByOutletIdAndBranchIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true);
        } else {
            productList = productRepository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        List<PurchaseProductData> list = new ArrayList<>();
        JsonArray array = new JsonArray();
        JsonObject result = new JsonObject();
        for (Product mProduct : productList) {
            List<ProductUnitPacking> units = productUnitRepository.findByProductIdAndStatus(mProduct.getId(), true);
            JsonObject response = new JsonObject();
            response.addProperty("productName", mProduct.getProductName());
            /* get barcode of product */
            ProductBarcode productBarcode = barcodeRepository.findByProductIdAndOutletIdAndStatus(mProduct.getId(), users.getOutlet().getId(), true);
            if (productBarcode != null) {
                response.addProperty("product_barcode", productBarcode.getBarcodeUniqueCode());
            }
            PurchaseProductData pData = new PurchaseProductData();
            response.addProperty("id", mProduct.getId());
            response.addProperty("productCode", mProduct.getProductCode());
            response.addProperty("isBatchNo", mProduct.getIsBatchNumber() != null ? mProduct.getIsBatchNumber() : false);
            response.addProperty("isInventory", mProduct.getIsInventory());
            array.add(response);
        }
        result.addProperty("messege", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.add("responseObject", array);
        return result;
    }

    public JsonObject getUnitsPackingsFlavours(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long product_id = Long.parseLong(request.getParameter("product_id"));
        Product mProduct = productRepository.findByIdAndStatus(product_id, true);
        JsonArray packArray = new JsonArray();
        JsonObject finalResult = new JsonObject();
        JsonObject result = new JsonObject();
        /*result.addProperty("isBrand", mProduct.getIsBrand());
        result.addProperty("isGroup", mProduct.getIsGroup());
        result.addProperty("isCategory", mProduct.getIsCategory());
        result.addProperty("isSubcategory", mProduct.getIsSubCategory());
        result.addProperty("isPackage", mProduct.getIsPackage());*/
        packArray = getUnitBrandsFlavourPackageUnitsCommonNew(product_id);
        result.add("lst_packages", packArray);
        /* End of  product packing List */
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("responseObject", result);
        return finalResult;
    }

    public JsonObject deleteProductList(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject jsonObject = new JsonObject();
        String removerProductList = request.getParameter("id");
        Long object = Long.valueOf(removerProductList);
        if (object != 0) {
            List<ProductOpeningStocks> productOpeningStocks = openingStocksRepository.findByProductIdAndStatus(object, true);
            if (productOpeningStocks != null && productOpeningStocks.size() > 0) {
                jsonObject.addProperty("message", "Product with opening stocks can't delete ");
            } else {
                List<InventoryDetailsPostings> inventoryList = inventoryDetailsPostingsRepository.findByProductIdAndStatus(object, true);
//                double sumOfCr = inventoryDetailsPostingsRepository.getSumOfCrOrDrByProductId(object, "CR");
//                double sumOfDr = inventoryDetailsPostingsRepository.getSumOfCrOrDrByProductId(object, "DR");
//                double result = sumOfCr - sumOfDr;
                if (inventoryList != null && inventoryList.size() > 0) {
//                if (result > 0) {
                    jsonObject.addProperty("message", "Product is used in transaction ,first delete transaction");
                } else {
                    Product product = productRepository.findByIdAndStatus(object, true);
                    if (product != null) product.setStatus(false);
                    try {
                        productRepository.save(product);
                        jsonObject.addProperty("message", "Product deleted successfully");
                        jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Exception:" + e.getMessage());
                        e.getMessage();
                        e.printStackTrace();
                    }
                }
            }
        }
        return jsonObject;
    }

    /***** Multilevel Architecture PK Visit *****/
    public Object createNewProduct(MultipartHttpServletRequest request) {
        Product product = new Product();
        Product newProduct = new Product();
        Map<String, String[]> paramMap = request.getParameterMap();
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Outlet outlet = users.getOutlet();
        ProductHsn productHsn = null;
        LocalDate applicableDate = null;
        TaxMaster taxMaster = null;
        try {
            if (users.getBranch() != null) branch = users.getBranch();
            product.setBranch(branch);
            product.setOutlet(outlet);
            product.setProductName(request.getParameter("productName").trim());
            if (paramMap.containsKey("productCode") && !request.getParameter("productCode").equalsIgnoreCase(""))
                product.setProductCode(request.getParameter("productCode"));
            else product.setProductCode("");
            if (paramMap.containsKey("productDescription"))
                product.setDescription(request.getParameter("productDescription"));
            product.setStatus(true);
            if (paramMap.containsKey("barcodeNo")) product.setBarcodeNo(request.getParameter("barcodeNo"));
            if (paramMap.containsKey("isSerialNo"))
                product.setIsSerialNumber(Boolean.parseBoolean(request.getParameter("isSerialNo")));
            product.setIsBatchNumber(Boolean.parseBoolean(request.getParameter("isBatchNo")));
            product.setIsInventory(Boolean.parseBoolean(request.getParameter("isInventory")));
            if (paramMap.containsKey("isWarranty")) {
                product.setIsWarrantyApplicable(Boolean.parseBoolean(request.getParameter("isWarranty")));
                if (Boolean.parseBoolean(request.getParameter("isWarranty"))) {
                    product.setWarrantyDays(Integer.parseInt(request.getParameter("nodays")));
                } else {
                    product.setWarrantyDays(0);
                }
            }
            product.setCreatedBy(users.getId());
            /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
            if (paramMap.containsKey("shelfId")) product.setShelfId(request.getParameter("shelfId"));
            if (paramMap.containsKey("barcodeSaleQuantity"))
                product.setBarcodeSalesQty(Double.parseDouble(request.getParameter("barcodeSaleQuantity")));
            if (paramMap.containsKey("purchaseRate"))
                product.setPurchaseRate(Double.parseDouble(request.getParameter("purchaseRate")));
            if (paramMap.containsKey("margin"))
                product.setMarginPer(Double.parseDouble(request.getParameter("margin")));
            PackingMaster mPackingMaster = null;
            Group mGroupMaster = null;
            Brand mBrandMaster = null;
            Category mCategoryMaster = null;
            if (paramMap.containsKey("brandId")) {
                mBrandMaster = brandRepository.findByIdAndStatus(Long.parseLong(request.getParameter("brandId")), true);
                product.setBrand(mBrandMaster);
            }
            if (paramMap.containsKey("packagingId")) {
                mPackingMaster = packingMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("packagingId")), true);
                product.setPackingMaster(mPackingMaster);
            }
            if (paramMap.containsKey("groupId")) {
                mGroupMaster = groupRepository.findByIdAndStatus(Long.parseLong(request.getParameter("groupId")), true);
                product.setGroup(mGroupMaster);
            }
            if (paramMap.containsKey("categoryId")) {
                mCategoryMaster = categoryRepository.findByIdAndStatus(Long.parseLong(request.getParameter("categoryId")), true);
                product.setCategory(mCategoryMaster);
            }
            if (paramMap.containsKey("weight")) product.setWeight(Double.parseDouble(request.getParameter("weight")));
            if (paramMap.containsKey("weightUnit")) product.setWeightUnit(request.getParameter("weightUnit"));
            if (paramMap.containsKey("disPer1"))
                product.setDiscountInPer(Double.parseDouble(request.getParameter("disPer1")));
            if (paramMap.containsKey("hsnNo")) {
                productHsn = productHsnRepository.findByIdAndStatus(Long.parseLong(request.getParameter("hsnNo")), true);
                if (productHsn != null) {
                    product.setProductHsn(productHsn);
                }
            }
            if (paramMap.containsKey("tax")) {

                taxMaster = taxMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("tax")), true);
                if (taxMaster != null) {
                    product.setTaxMaster(taxMaster);
                }
                if (paramMap.containsKey("taxApplicableDate"))
                    applicableDate = LocalDate.parse(request.getParameter("taxApplicableDate"));
                product.setApplicableDate(LocalDate.parse(request.getParameter("taxApplicableDate")));

            }
            if (paramMap.containsKey("taxType")) product.setTaxType(request.getParameter("taxType"));
            if (paramMap.containsKey("igst")) product.setIgst(Double.parseDouble(request.getParameter("igst")));
            if (paramMap.containsKey("cgst")) product.setCgst(Double.parseDouble(request.getParameter("cgst")));
            if (paramMap.containsKey("sgst")) product.setSgst(Double.parseDouble(request.getParameter("sgst")));
            if (paramMap.containsKey("minStock"))
                product.setMinStock(Double.parseDouble(request.getParameter("minStock")));
            if (paramMap.containsKey("maxStock"))
                product.setMaxStock(Double.parseDouble(request.getParameter("maxStock")));
            newProduct = productRepository.save(product);
            /**** inserting into ProductTax Master to maintain tax information of Product *****/
            if (applicableDate != null) {
                try {
                    ProductTaxDateMaster productTaxDateMaster = new ProductTaxDateMaster();
                    productTaxDateMaster.setProduct(newProduct);
                    productTaxDateMaster.setProductHsn(productHsn);
                    productTaxDateMaster.setTaxMaster(taxMaster);
                    productTaxDateMaster.setApplicableDate(applicableDate);
                    productTaxDateMaster.setStatus(true);
                    productTaxDateMaster.setUpdatedBy(users.getId());
                    productTaxDateMasterRepository.save(productTaxDateMaster);
                } catch (Exception e) {
                    productLogger.error("Error in Product Creation-> ProductTaxDateMaster Creation-> " + e.getMessage());
                }
            }

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in Product Creation:" + exceptionAsString);
        }
        /**** END ****/
        try {
            JsonParser parser = new JsonParser();
            String jsonStr = request.getParameter("mstPackaging");
            JsonElement tradeElement = parser.parse(jsonStr);
            JsonArray array = tradeElement.getAsJsonArray();
            for (JsonElement mList : array) {
                JsonObject object = mList.getAsJsonObject();
                LevelB levelB = null; //group
                LevelA levelA = null;//brand
                LevelC levelC = null;//Category
                /**** LevelA Master ****/
                if (!object.get("levela_id").getAsString().equalsIgnoreCase("")) {
                    levelA = levelARepository.findByIdAndStatus(object.get("levela_id").getAsLong(), true);
                }
                JsonArray leveBArray = object.get("levelb").getAsJsonArray();
                for (JsonElement mLevelB : leveBArray) {
                    JsonObject mLevelBAsJsonObject = mLevelB.getAsJsonObject();
                    /**** LevelB Master ****/
                    if (!mLevelBAsJsonObject.get("levelb_id").getAsString().equalsIgnoreCase("")) {
                        levelB = levelBRepository.findByIdAndStatus(mLevelBAsJsonObject.get("levelb_id").getAsLong(), true);
                    }
                    JsonArray levelCArray = mLevelBAsJsonObject.get("levelc").getAsJsonArray();
                    for (JsonElement mLevelC : levelCArray) {
                        JsonObject mLevelCObject = mLevelC.getAsJsonObject();
                        /**** LevelC Master ****/
                        if (!mLevelCObject.get("levelc_id").getAsString().equalsIgnoreCase("")) {
                            levelC = levelCRepository.findByIdAndStatus(mLevelCObject.get("levelc_id").getAsLong(), true);
                        }
                        JsonArray unitsArray = mLevelCObject.get("units").getAsJsonArray();
                        for (JsonElement mUnitsList : unitsArray) {
                            ProductUnitPacking productUnitPacking = new ProductUnitPacking();
                            JsonObject mUnitObject = mUnitsList.getAsJsonObject();
                            Units unit = unitsRepository.findByIdAndStatus(mUnitObject.get("unit_id").getAsLong(), true);
                            productUnitPacking.setUnits(unit);
                            productUnitPacking.setUnitConversion(mUnitObject.get("unit_conv").getAsDouble());
                            productUnitPacking.setUnitConvMargn(mUnitObject.get("unit_marg").getAsDouble());
                            if (mUnitObject.get("isNegativeStocks").getAsInt() == 1) {
                                productUnitPacking.setIsNegativeStocks(true);
                            } else {
                                productUnitPacking.setIsNegativeStocks(false);
                            }
                            productUnitPacking.setMrp(mUnitObject.get("mrp").getAsDouble());
                            productUnitPacking.setPurchaseRate(mUnitObject.get("purchase_rate").getAsDouble());
                            productUnitPacking.setMinMargin(mUnitObject.get("min_margin").getAsDouble());
                            productUnitPacking.setMinRateA(mUnitObject.get("min_rate_a").getAsDouble());//sales Rate
                            productUnitPacking.setMinRateB(mUnitObject.get("min_rate_b").getAsDouble());
                            productUnitPacking.setMinRateC(mUnitObject.get("min_rate_c").getAsDouble());
                            productUnitPacking.setStatus(true);
                            productUnitPacking.setProduct(newProduct);
                            productUnitPacking.setCreatedBy(users.getId());
                            /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
                            productUnitPacking.setMinQty(mUnitObject.get("min_qty").getAsDouble());
                            productUnitPacking.setMaxQty(mUnitObject.get("max_qty").getAsDouble());
                            productUnitPacking.setLevelA(levelA);
                            productUnitPacking.setLevelB(levelB);
                            productUnitPacking.setLevelC(levelC);
                            productUnitRepository.save(productUnitPacking);
                            /****** Inserting Product Opening Stocks ******/
                            JsonArray mBatchJsonArray = mUnitObject.getAsJsonArray("batchList");
                            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
                            for (JsonElement mBatchElement : mBatchJsonArray) {
                                ProductBatchNo productBatchNo = null;
                                JsonObject mBatchJsonObject = mBatchElement.getAsJsonObject();
                                ProductBatchNo mproductBatchNo = null;
                                Long id = mBatchJsonObject.get("id").getAsLong();
                                if (mBatchJsonObject.get("isOpeningbatch").getAsBoolean()) {

                                    mproductBatchNo = new ProductBatchNo();

                                    if (mBatchJsonObject.has("b_no"))
                                        mproductBatchNo.setBatchNo(mBatchJsonObject.get("b_no").getAsString());
                                    if (mBatchJsonObject.has("b_mrp"))
                                        mproductBatchNo.setMrp(mBatchJsonObject.get("b_mrp").getAsDouble());
                                    if (mBatchJsonObject.has("b_purchase_rate"))
                                        mproductBatchNo.setPurchaseRate(mBatchJsonObject.get("b_purchase_rate").getAsDouble());

                                    if (mBatchJsonObject.has("b_sale_rate"))
                                        mproductBatchNo.setSalesRate(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                                    mproductBatchNo.setMinRateA(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                                    if (mBatchJsonObject.has("b_free_qty"))
                                        mproductBatchNo.setFreeQty(mBatchJsonObject.get("b_free_qty").getAsDouble());
                                    if (mBatchJsonObject.has("b_manufacturing_date") && !mBatchJsonObject.get("b_manufacturing_date").getAsString().equalsIgnoreCase(""))
                                        mproductBatchNo.setManufacturingDate(LocalDate.parse(mBatchJsonObject.get("b_manufacturing_date").getAsString()));
                                    if (mBatchJsonObject.has("b_expiry") && !mBatchJsonObject.get("b_expiry").getAsString().equalsIgnoreCase(""))
                                        mproductBatchNo.setExpiryDate(LocalDate.parse(mBatchJsonObject.get("b_expiry").getAsString()));
                                    mproductBatchNo.setStatus(true);
                                    mproductBatchNo.setProduct(product);
                                    mproductBatchNo.setOutlet(outlet);
                                    mproductBatchNo.setBranch(branch);
                                    mproductBatchNo.setUnits(unit);
                                    mproductBatchNo.setQnty(Integer.parseInt(mBatchJsonObject.get("opening_qty").getAsString()));
                                    if (fiscalYear != null) mproductBatchNo.setFiscalYear(fiscalYear);
                                    productBatchNo = productBatchNoRepository.save(mproductBatchNo);
                                }
                                try {
                                    ProductOpeningStocks newOpeningStock = new ProductOpeningStocks();
                                    newOpeningStock.setOpeningStocks(Double.parseDouble(mBatchJsonObject.get("opening_qty").getAsString()));
                                    newOpeningStock.setProduct(newProduct);
                                    newOpeningStock.setUnits(unit);
                                    newOpeningStock.setBranch(branch);
                                    newOpeningStock.setOutlet(outlet);
                                    newOpeningStock.setProductBatchNo(productBatchNo);
                                    if (mBatchJsonObject.has("b_free_qty"))
                                        newOpeningStock.setFreeOpeningQty(mBatchJsonObject.get("b_free_qty").getAsDouble());
                                    if (mBatchJsonObject.has("b_mrp"))
                                        newOpeningStock.setMrp(mBatchJsonObject.get("b_mrp").getAsDouble());
                                    if (mBatchJsonObject.has("b_purchase_rate"))
                                        newOpeningStock.setPurchaseRate(mBatchJsonObject.get("b_purchase_rate").getAsDouble());
                                    if (mBatchJsonObject.has("b_sale_rate"))
                                        newOpeningStock.setSalesRate(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                                    newOpeningStock.setLevelA(levelA);
                                    newOpeningStock.setLevelB(levelB);
                                    newOpeningStock.setLevelC(levelC);
                                    newOpeningStock.setStatus(true);
                                    if (mBatchJsonObject.has("b_manufacturing_date") && !mBatchJsonObject.get("b_manufacturing_date").getAsString().equalsIgnoreCase(""))
                                        newOpeningStock.setManufacturingDate(LocalDate.parse(mBatchJsonObject.get("b_manufacturing_date").getAsString()));
                                    if (mBatchJsonObject.has("b_expiry") && !mBatchJsonObject.get("b_expiry").getAsString().equalsIgnoreCase(""))
                                        newOpeningStock.setExpiryDate(LocalDate.parse(mBatchJsonObject.get("b_expiry").getAsString()));
                                    newOpeningStock.setCosting(mBatchJsonObject.get("b_costing").getAsDouble());
                                    if (fiscalYear != null) newOpeningStock.setFiscalYear(fiscalYear);
                                    openingStocksRepository.save(newOpeningStock);
                                } catch (Exception e) {
                                    StringWriter sw = new StringWriter();
                                    e.printStackTrace(new PrintWriter(sw));
                                    String exceptionAsString = sw.toString();
                                    productLogger.error("Product Creation: Product Opening Stock" + exceptionAsString);
                                    responseObject.setMessage("Error in Product Creation:ProductOpeningStock");
                                    responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                                }
                            }
                        }//Units
                    }//Level C
                }//Level B
            }//Level A
            responseObject.setMessage("Product Created Successfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setData(newProduct.getId().toString());
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in create product:" + exceptionAsString);
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseObject.setMessage("Internal Server Error");
        }
        return responseObject;
    }

    public JsonObject getByIdEdit(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long product_id = Long.parseLong(request.getParameter("productId"));
        Product mProduct = productRepository.findByIdAndStatus(product_id, true);
        JsonObject listObject = new JsonObject();
        JsonObject finalResult = new JsonObject();
        JsonObject result = new JsonObject();
        listObject = getProductLevelsNew(mProduct, users);
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("responseObject", listObject);
        return finalResult;
    }

    public JsonObject getProductByIdEditFlavourNew(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long productId = Long.parseLong(request.getParameter("product_id"));
        Product mProduct = productRepository.findByIdAndStatus(productId, true);
        JsonObject result = new JsonObject();
        //   List<ProductUnitPacking> units = productUnitRepository.findByProductIdAndStatus(mProduct.getId(), true);
        JsonObject response = new JsonObject();
        try {
            if (mProduct != null) {
                response.addProperty("productName", mProduct.getProductName());
                response.addProperty("id", mProduct.getId());
                response.addProperty("description", mProduct.getDescription());
                response.addProperty("productCode", mProduct.getProductCode());
                response.addProperty("isBatchNo", mProduct.getIsBatchNumber());
                response.addProperty("isInventory", mProduct.getIsInventory());
                response.addProperty("isSerialNo", mProduct.getIsSerialNumber());
                response.addProperty("barcodeNo", mProduct.getBarcodeNo());
                response.addProperty("shelfId", mProduct.getShelfId());
                response.addProperty("barcodeSalesQty", mProduct.getBarcodeSalesQty());
                response.addProperty("purchaseRate", mProduct.getPurchaseRate());
                response.addProperty("margin", mProduct.getMarginPer());
                response.addProperty("brandId", mProduct.getBrand() != null ? mProduct.getBrand().getId() : null);
                response.addProperty("packagingId", mProduct.getPackingMaster() != null ? mProduct.getPackingMaster().getId() : null);
                response.addProperty("groupId", mProduct.getGroup() != null ? mProduct.getGroup().getId() : null);
                response.addProperty("categoryId", mProduct.getCategory() != null ? mProduct.getCategory().getId() : null);
                response.addProperty("weight", mProduct.getWeight());
                response.addProperty("weightUnit", mProduct.getWeightUnit());
                response.addProperty("disPer1", mProduct.getDiscountInPer());
                response.addProperty("hsnNo", mProduct.getProductHsn() != null ? mProduct.getProductHsn().getId() : null);
                response.addProperty("tax", mProduct.getTaxMaster() != null ? mProduct.getTaxMaster().getId() : null);
                response.addProperty("taxApplicableDate", mProduct.getApplicableDate() != null ? mProduct.getApplicableDate().toString() : null);
                response.addProperty("taxType", mProduct.getTaxType() != null ? mProduct.getTaxType() : null);
                response.addProperty("igst", mProduct.getIgst() != null ? mProduct.getIgst() : null);
                response.addProperty("cgst", mProduct.getCgst() != null ? mProduct.getCgst() : null);
                response.addProperty("sgst", mProduct.getSgst() != null ? mProduct.getSgst() : null);
                response.addProperty("minStock", mProduct.getMinStock() != null ? mProduct.getMinStock() : 0.0);
                response.addProperty("maxStock", mProduct.getMaxStock() != null ? mProduct.getMaxStock() : 0.0);
                /* getting Level A, Level B, Level C and its Units from Product Id */
                JsonArray unitArray = new JsonArray();
                unitArray = getUnitBrandsFlavourPackageUnitsCommonNewProductEdit(mProduct.getId());
                response.add("mstPackaging", unitArray);
                result.addProperty("messege", "success");
                result.addProperty("responseStatus", HttpStatus.OK.value());
                result.add("responseObject", response);
            } else {
                result.addProperty("messege", "empty");
                result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
                result.add("responseObject", response);
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in getProductByIdEditFlavourNew:" + exceptionAsString);
        }
        return result;
    }

    public Object productTransactionsDetails(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonObject mObject = new JsonObject();
        LocalDate invoiceDate=null;
        String date="";
        Map<String, String[]> paramMap = request.getParameterMap();
        Long productId = Long.parseLong(request.getParameter("product_id").isEmpty() ? "0" : request.getParameter("product_id"));
        if(paramMap.containsKey("invoice_date")){
            date = request.getParameter("invoice_date");
             invoiceDate = LocalDate.parse(date);
        }

        Product product = null;
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            if (users.getBranch() != null) {
                product = productRepository.findByIdAndOutletIdAndBranchIdAndStatus(productId, users.getOutlet().getId(), users.getBranch().getId(), true);
            } else {
                product = productRepository.findByIdAndOutletIdAndStatusAndBranchIsNull(productId, users.getOutlet().getId(), true);
            }
            if (product != null) {
                mObject.addProperty("min_stocks", product.getMinStock() != null ? product.getMinStock() : 0.0);
                mObject.addProperty("max_stocks", product.getMinStock() != null ? product.getMaxStock() : 0.0);
                ProductBatchNo productBatchNo = productBatchNoRepository.findTop1ByProductIdAndStatusOrderByIdDesc(product.getId(), true);
                List<ProductUnitPacking> mUnits = productUnitRepository.findByProductId(product.getId());
                if (mUnits != null) {
                    mObject.addProperty("min_stocks", mUnits.get(0).getMinQty() != null ? mUnits.get(0).getMinQty() : 0.0);
                    mObject.addProperty("max_stocks", mUnits.get(0).getMaxQty() != null ? mUnits.get(0).getMaxQty() : 0.0);
                }
                if (productBatchNo != null) {
                    if (productBatchNo.getExpiryDate() != null) {
                        mObject.addProperty("batch_expiry", productBatchNo.getExpiryDate().toString());
                    } else {
                        mObject.addProperty("batch_expiry", "");
                    }
                    mObject.addProperty("mrp", productBatchNo.getMrp() != null ? productBatchNo.getMrp() : 0.00);
                    mObject.addProperty("purchase_rate", productBatchNo.getPurchaseRate() != null ? productBatchNo.getPurchaseRate() : 0.00);
                    mObject.addProperty("cost", productBatchNo.getCosting() != null ? productBatchNo.getCosting() : 0.00);
                } else {
                    if (mUnits != null && mUnits.size() > 0) {
                        mObject.addProperty("purchase_rate", mUnits.get(0).getPurchaseRate() != null ? mUnits.get(0).getPurchaseRate() : 0.0);
                        mObject.addProperty("cost", mUnits.get(0).getCosting() != null ? mUnits.get(0).getCosting() : 0.0);
                    } else {
                        mObject.addProperty("purchase_rate", 0.00);
                        mObject.addProperty("cost", 0.00);
                    }
                }
                mObject.addProperty("brand", product.getBrand().getBrandName());
                mObject.addProperty("group", product.getGroup() != null ? product.getGroup().getGroupName() : "");
                mObject.addProperty("subgroup", product.getSubgroup() != null ? product.getSubgroup().getSubgroupName() : "");
                mObject.addProperty("category", product.getCategory() != null ? product.getCategory().getCategoryName() : "");
                mObject.addProperty("hsn", product.getProductHsn() != null ? product.getProductHsn().getHsnNumber() : "");
                mObject.addProperty("tax_type", product.getTaxType());
                /**** getting tax of product using purchase and sales invoice date, bcz we maintained product tax
                 applicable date ****/
                TaxMaster taxMaster = null;
                if (users.getBranch() != null) {
                    List<ProductTaxDateMaster> productTaxDateMasters = productTaxDateMasterRepository.
                            findByProductIdAndOutletIdAndBranchIdAndStatus(productId, users.getOutlet().getId(),
                                    users.getBranch().getId(), true);
                    for (ProductTaxDateMaster mProductTaxMaster : productTaxDateMasters) {
                        if (invoiceDate.compareTo(mProductTaxMaster.getApplicableDate()) > 0) {
                            taxMaster = mProductTaxMaster.getTaxMaster();
                        }
                    }
                    mObject.addProperty("tax_per", taxMaster != null ? taxMaster.getIgst() : 0.0);
                    mObject.addProperty("igst", taxMaster != null ? taxMaster.getIgst() : 0.0);
                    mObject.addProperty("cgst", taxMaster != null ? taxMaster.getCgst() : 0.0);
                    mObject.addProperty("sgst", taxMaster != null ? taxMaster.getSgst() : 0.0);
                } else {
                    List<ProductTaxDateMaster> productTaxDateMasters = productTaxDateMasterRepository.
                            findByProductIdAndOutletIdAndBranchIdIsNullAndStatus(productId, users.getOutlet().getId(),
                                    true);
                    for (ProductTaxDateMaster mProductTaxMaster : productTaxDateMasters) {
                        if (invoiceDate.compareTo(mProductTaxMaster.getApplicableDate()) > 0) {
                            taxMaster = mProductTaxMaster.getTaxMaster();
                            System.out.println("Applicable Date:" + mProductTaxMaster.getApplicableDate() +
                                    "\nInvoice Date:" + invoiceDate);
                            System.out.println("IGST:" + taxMaster.getIgst());
                        }
                    }
                    mObject.addProperty("tax_per", taxMaster != null ? taxMaster.getIgst() : 0.0);
                    mObject.addProperty("igst", taxMaster != null ? taxMaster.getIgst() : 0.0);
                    mObject.addProperty("cgst", taxMaster != null ? taxMaster.getCgst() : 0.0);
                    mObject.addProperty("sgst", taxMaster != null ? taxMaster.getSgst() : 0.0);
                }
                mObject.addProperty("margin_per", product.getMarginPer());
                mObject.addProperty("shelf_id", product.getShelfId());
                mObject.addProperty("min_stocks", product.getMinStock());
                mObject.addProperty("max_stocks", product.getMaxStock());
                mObject.addProperty("is_batch", product.getIsBatchNumber());
                mObject.addProperty("supplier", "");
                mObject.addProperty("barcode", product.getBarcodeNo());
                mObject.addProperty("margin", product.getMarginPer());
                TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductIdOrderByIdDesc(product.getId());
                if (tranxPurInvoiceDetailsUnits != null) {
                    mObject.addProperty("supplier", tranxPurInvoiceDetailsUnits.getPurchaseTransaction().getSundryCreditors().getLedgerName());
                }
                response.addProperty("message", "success");
                response.addProperty("responseStatus", HttpStatus.OK.value());
                response.add("result", mObject);
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in Product Transaction Details:" + exceptionAsString);
        }
        return response;
    }


    public Object productDetailsLevelB(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        Long productId = Long.parseLong(request.getParameter("product_id"));
        Long levelAId = null;
        if (!request.getParameter("level_a_id").equalsIgnoreCase(""))
            levelAId = Long.parseLong(request.getParameter("level_a_id"));
        List<Long> levelBArray = productUnitRepository.findLevelBIdDistinct(productId, levelAId);
        JsonArray levelBJsonArray = new JsonArray();
        for (Long mLevelB : levelBArray) {
            if (mLevelB != null) {
                LevelB levelB = levelBRepository.findByIdAndStatus(mLevelB, true);
                if (levelB != null) {
                    JsonObject levelBJsonObject = new JsonObject();
                    levelBJsonObject.addProperty("levelb_id", levelB.getId());
                    levelBJsonObject.addProperty("levelb_name", levelB.getLevelName());
                    levelBJsonArray.add(levelBJsonObject);
                }
            }
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("levelBOpt", levelBJsonArray);
        return response;
    }

    public Object productDetailsLevelC(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        Long productId = Long.parseLong(request.getParameter("product_id"));
        Long levelAId = null;
        if (!request.getParameter("level_a_id").equalsIgnoreCase(""))
            levelAId = Long.parseLong(request.getParameter("level_a_id"));
        Long levelBId = null;
        if (!request.getParameter("level_b_id").equalsIgnoreCase(""))
            levelBId = Long.parseLong(request.getParameter("level_b_id"));
        List<Long> levelCArray = productUnitRepository.findLevelCIdDistinct(productId, levelAId, levelBId);
        JsonArray levelCJsonArray = new JsonArray();
        for (Long mLevelC : levelCArray) {
            if (mLevelC != null) {
                LevelC levelC = levelCRepository.findByIdAndStatus(mLevelC, true);
                if (levelC != null) {
                    JsonObject levelCJsonObject = new JsonObject();
                    levelCJsonObject.addProperty("levelc_id", levelC.getId());
                    levelCJsonObject.addProperty("levelc_name", levelC.getLevelName());
                    levelCJsonArray.add(levelCJsonObject);
                }
            }
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("levelCOpt", levelCJsonArray);
        return response;
    }

    public Object productTransactionsListByBarcode(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();
        List<Product> productDetails = new ArrayList<>();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String barcode = request.getParameter("barcode");

        String query = "SELECT * FROM `product_tbl` LEFT JOIN packing_master_tbl ON product_tbl.packing_master_id=packing_master_tbl.id" + " WHERE product_tbl.outlet_id=" + users.getOutlet().getId() + " AND product_tbl.status=1";

        if (users.getBranch() != null) {
            query = query + " AND product_tbl.branch_id=" + users.getBranch().getId();
        }
        if (!barcode.equalsIgnoreCase("")) {
            query = query + " AND barcode_no=" + barcode;
        }
        System.out.println("query " + query);
        Query q = entityManager.createNativeQuery(query, Product.class);
        productDetails = q.getResultList();
        if (productDetails != null && productDetails.size() > 0) {
            for (Product mDetails : productDetails) {
                JsonObject mObject = new JsonObject();
                mObject.addProperty("id", mDetails.getId());
                mObject.addProperty("code", mDetails.getProductCode());
                mObject.addProperty("product_name", mDetails.getProductName());
                mObject.addProperty("packing", mDetails.getPackingMaster().getPackName());
                mObject.addProperty("barcode", mDetails.getBarcodeNo());
                // ProductBatchNo batchNo = productBatchNoRepository.findByIdAndStatus()
                mObject.addProperty("mrp", 0);
                mObject.addProperty("sales_rate", 0);
                mObject.addProperty("current_stock", 0);

                result.add(mObject);
            }
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }

    public Object productUnits(HttpServletRequest request) {
        JsonObject response = new JsonObject();

        try {
            JsonArray jsonArray = new JsonArray();

            String productId = request.getParameter("product_id");
            Long level_a_id = Long.valueOf(request.getParameter("level_a_id"));
            Long level_b_id = request.getParameter("level_b_id").equalsIgnoreCase("null") ? null : Long.valueOf(request.getParameter("level_b_id"));
            Long level_c_id = request.getParameter("level_c_id").equalsIgnoreCase("null") ? null : Long.valueOf(request.getParameter("level_c_id"));
            List<Object[]> unitList = productUnitRepository.findUniqueUnitsByProductId(Long.valueOf(productId), level_a_id, level_b_id, level_c_id);

            for (int i = 0; i < unitList.size(); i++) {
                Object[] objects = unitList.get(i);

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("unitId", objects[0].toString());
                jsonObject.addProperty("unitName", objects[1].toString());
                jsonObject.addProperty("unitCode", objects[2].toString());
                jsonObject.addProperty("unitConversion", objects[3].toString());
                jsonArray.add(jsonObject);
            }

            response.add("response", jsonArray);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            productLogger.error("error in create product:" + e.getMessage());
            response.addProperty("message", "Failed to load data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public JsonArray getUnitBrandsFlavourPackageUnitsCommonNewProductEdit(Long product_id) {
        JsonArray LevelAJsonArray = new JsonArray();
        List<Long> levelaArray = new ArrayList<>();
        levelaArray = productUnitRepository.findLevelAIdDistinct(product_id);
        for (Long mLeveA : levelaArray) {
            JsonObject levelaJsonObject = new JsonObject();
            LevelA levelA = null;
            if (mLeveA != null) {
                levelA = levelARepository.findByIdAndStatus(mLeveA, true);
                levelaJsonObject.addProperty("levela_id", levelA.getId());
                levelaJsonObject.addProperty("levela_name", levelA.getLevelName());
            } else {
                levelaJsonObject.addProperty("levela_id", "");
                levelaJsonObject.addProperty("levela_name", "");
            }
            List<Long> levelBArray = new ArrayList<>();
            levelBArray = productUnitRepository.findLevelBIdDistinct(product_id, mLeveA);
            JsonArray levelBJsonArray = new JsonArray();
            for (Long mLevelB : levelBArray) {
                JsonObject levelBJsonObject = new JsonObject();
                LevelB levelB = null;
                if (mLevelB != null) {
                    levelB = levelBRepository.findByIdAndStatus(mLevelB, true);
                    levelBJsonObject.addProperty("levelb_id", levelB.getId());
                    levelBJsonObject.addProperty("levelb_name", levelB.getLevelName());
                } else {
                    levelBJsonObject.addProperty("levelb_id", "");
                    levelBJsonObject.addProperty("levelb_name", "");
                }
                JsonArray levelCJsonArray = new JsonArray();
                List<Long> levelCArray = new ArrayList<>();
                levelCArray = productUnitRepository.findLevelCIdDistinct(product_id, mLeveA, mLevelB);
                for (Long mLevelC : levelCArray) {
                    JsonObject levelCJsonObject = new JsonObject();
                    LevelC levelC = null;
                    if (mLevelC != null) {
                        levelC = levelCRepository.findByIdAndStatus(mLevelC, true);
                        levelCJsonObject.addProperty("levelc_id", levelC.getId());
                        levelCJsonObject.addProperty("levelc_name", levelC.getLevelName());
                    } else {
                        levelCJsonObject.addProperty("levelc_id", "");
                        levelCJsonObject.addProperty("levelc_name", "");
                    }
                    JsonArray unitJsonArray = new JsonArray();
                    List<ProductUnitPacking> unitPackingList = new ArrayList<>();
                    unitPackingList = productUnitRepository.findByPackingUnits(product_id, mLeveA, mLevelB, mLevelC);
                    for (ProductUnitPacking mUnits : unitPackingList) {
                        JsonObject mUnitObject = new JsonObject();
                        mUnitObject.addProperty("isNegativeStocks", mUnits.getIsNegativeStocks() == true ? 1 : 0);
                        mUnitObject.addProperty("unit_id", mUnits.getUnits().getId());
                        mUnitObject.addProperty("details_id", mUnits.getId());
                        mUnitObject.addProperty("unit_name", mUnits.getUnits().getUnitName());
                        mUnitObject.addProperty("unit_conv", mUnits.getUnitConversion());
                        mUnitObject.addProperty("unit_marg", mUnits.getUnitConvMargn());
                        mUnitObject.addProperty("mrp", mUnits.getMrp());
                        mUnitObject.addProperty("purchase_rate", mUnits.getPurchaseRate());
                        mUnitObject.addProperty("rateA", mUnits.getMinRateA() != null ? mUnits.getMinRateA() : 0);
                        mUnitObject.addProperty("rateB", mUnits.getMinRateB() != null ? mUnits.getMinRateB() : 0);
                        mUnitObject.addProperty("rateC", mUnits.getMinRateC() != null ? mUnits.getMinRateC() : 0);
                        mUnitObject.addProperty("min_qty", mUnits.getMinQty() != null ? mUnits.getMinQty() : 0.0);
                        mUnitObject.addProperty("max_qty", mUnits.getMaxQty() != null ? mUnits.getMaxQty() : 0.0);
                        mUnitObject.addProperty("levelAId", mUnits.getLevelA() != null ? mUnits.getLevelA().getId() : null);
                        mUnitObject.addProperty("levelBId", mUnits.getLevelB() != null ? mUnits.getLevelB().getId() : null);
                        mUnitObject.addProperty("levelCId", mUnits.getLevelC() != null ? mUnits.getLevelC().getId() : null);
                        JsonArray batchJsonArray = new JsonArray();
                        Long levelaUnit = mUnits.getLevelA() != null ? mUnits.getLevelA().getId() : null;
                        Long levelbUnit = mUnits.getLevelB() != null ? mUnits.getLevelB().getId() : null;
                        Long levelcUnit = mUnits.getLevelC() != null ? mUnits.getLevelC().getId() : null;
                        List<ProductOpeningStocks> openingStocks = openingStocksRepository.findByProductOpening(mUnits.getProduct().getId(), mUnits.getUnits().getId(), levelaUnit, levelbUnit, levelcUnit);
                        for (ProductOpeningStocks mOpeningStocks : openingStocks) {
                            JsonObject mObject = new JsonObject();
                            mObject.addProperty("id", mOpeningStocks.getId());
                            mObject.addProperty("b_no", mOpeningStocks.getProductBatchNo() != null ? mOpeningStocks.getProductBatchNo().getBatchNo() : "");
                            mObject.addProperty("batch_id", mOpeningStocks.getProductBatchNo() != null ? mOpeningStocks.getProductBatchNo().getId().toString() : "");
                            mObject.addProperty("opening_qty", mOpeningStocks.getOpeningStocks());
                            mObject.addProperty("b_free_qty", mOpeningStocks.getFreeOpeningQty());
                            mObject.addProperty("b_mrp", mOpeningStocks.getMrp());
                            mObject.addProperty("b_sale_rate", mOpeningStocks.getSalesRate());
                            mObject.addProperty("b_purchase_rate", mOpeningStocks.getPurchaseRate());
                            mObject.addProperty("b_costing", mOpeningStocks.getCosting());
                            mObject.addProperty("b_expiry", mOpeningStocks.getExpiryDate() != null ? mOpeningStocks.getExpiryDate().toString() : "");
                            mObject.addProperty("b_manufacturing_date", mOpeningStocks.getManufacturingDate() != null ? mOpeningStocks.getManufacturingDate().toString() : "");
                            batchJsonArray.add(mObject);
                        }
                        mUnitObject.add("batchList", batchJsonArray);
                        unitJsonArray.add(mUnitObject);
                    }
                    levelCJsonObject.add("units", unitJsonArray);
                    levelCJsonArray.add(levelCJsonObject);
                }
                levelBJsonObject.add("levelc", levelCJsonArray);
                levelBJsonArray.add(levelBJsonObject);
            }
            levelaJsonObject.add("levelb", levelBJsonArray);
            LevelAJsonArray.add(levelaJsonObject);
        }
        return LevelAJsonArray;
    }

    public Object getLastproductData(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Product mProduct = productRepository.findTopByStatusOrderByIdDesc(true);
        JsonObject result = new JsonObject();
        JsonObject response = new JsonObject();
        if (mProduct != null) {
            response.addProperty("isBatchNo", mProduct.getIsBatchNumber());
            response.addProperty("isInventory", mProduct.getIsInventory());
//            response.addProperty("shelfId", mProduct.getShelfId());
            response.addProperty("brandId", mProduct.getBrand() != null ? mProduct.getBrand().getId() : null);
            response.addProperty("groupId", mProduct.getGroup() != null ? mProduct.getGroup().getId() : null);
            response.addProperty("subgroupId", mProduct.getSubgroup() != null ? mProduct.getSubgroup().getId() : null);
            response.addProperty("categoryId", mProduct.getCategory() != null ? mProduct.getCategory().getId() : null);
            response.addProperty("subcategoryId", mProduct.getSubcategory() != null ? mProduct.getSubcategory().getId() : null);
            response.addProperty("hsnNo", mProduct.getProductHsn() != null ? mProduct.getProductHsn().getId() : null);
            response.addProperty("tax", mProduct.getTaxMaster() != null ? mProduct.getTaxMaster().getId() : null);
            response.addProperty("taxApplicableDate", mProduct.getApplicableDate() != null ? mProduct.getApplicableDate().toString() : null);
            response.addProperty("taxType", mProduct.getTaxType() != null ? mProduct.getTaxType() : null);
            response.addProperty("igst", mProduct.getIgst() != null ? mProduct.getIgst() : null);
            response.addProperty("cgst", mProduct.getCgst() != null ? mProduct.getCgst() : null);
            response.addProperty("sgst", mProduct.getSgst() != null ? mProduct.getSgst() : null);
//            response.addProperty("minStock", mProduct.getMinStock() != null ? mProduct.getMinStock() : 0.0);
//            response.addProperty("maxStock", mProduct.getMaxStock() != null ? mProduct.getMaxStock() : 0.0);
            List<ProductUnitPacking> mUnits = productUnitRepository.findByProductIdAndStatus(mProduct.getId(), true);
            if (mUnits != null && mUnits.size() > 0) {
                response.addProperty("selectedUnit", mUnits.get(0).getUnits().getId());
            }
            result.addProperty("messege", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("responseObject", response);
        } else {
            result.addProperty("messege", "empty");
            result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            result.add("responseObject", response);
        }
        return result;
    }

    public Object getPurchaseRateProduct(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        try {
            Long productId = Long.parseLong(request.getParameter("product_id"));
            Long levelAId = null;
            if (!request.getParameter("level_a_id").equalsIgnoreCase(""))
                levelAId = Long.parseLong(request.getParameter("level_a_id"));
            Long levelBId = null;
            if (!request.getParameter("level_b_id").equalsIgnoreCase(""))
                levelBId = Long.valueOf(request.getParameter("level_b_id"));
            Long levelCId = null;
            if (!request.getParameter("level_c_id").equalsIgnoreCase(""))
                levelCId = Long.valueOf(request.getParameter("level_c_id"));
            Long unitId = Long.parseLong(request.getParameter("unit_id"));
            ProductUnitPacking mUnitPackaging = productUnitRepository.findRate(productId, levelAId, levelBId, levelCId, unitId, true);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("purchase_rate", mUnitPackaging.getPurchaseRate());
            jsonObject.addProperty("mrp", mUnitPackaging.getMrp());
            jsonObject.addProperty("rate_a", mUnitPackaging.getMinRateA());
            jsonObject.addProperty("rate_b", mUnitPackaging.getMinRateB());
            jsonObject.addProperty("rate_c", mUnitPackaging.getMinRateC());
            response.addProperty("message", "Successs");
            response.add("data", jsonObject);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            productLogger.error("error in create product:" + e.getMessage());
            response.addProperty("message", "Failed to load data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    /***** Optimising Code for Product Creation (Kiran visit Solapur) ******/
    public Object createProductNew(MultipartHttpServletRequest request) {
        Product product = new Product();
        Product newProduct = new Product();
        Map<String, String[]> paramMap = request.getParameterMap();
        ResponseMessage responseObject = new ResponseMessage();
        FileStorageProperties fileStorageProperties = new FileStorageProperties();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Outlet outlet = users.getOutlet();
        LocalDate applicableDate = null;
        TaxMaster taxMaster = null;
        ProductHsn productHsn = null;
        try {
            if (users.getBranch() != null) branch = users.getBranch();
            product.setBranch(branch);
            product.setOutlet(outlet);
            product.setProductName(request.getParameter("productName").trim());
            if (paramMap.containsKey("productCode")) product.setProductCode(request.getParameter("productCode"));
            if (paramMap.containsKey("productDescription"))
                product.setDescription(request.getParameter("productDescription"));
            product.setStatus(true);
            if (paramMap.containsKey("barcodeNo")) product.setBarcodeNo(request.getParameter("barcodeNo"));
            if (paramMap.containsKey("isSerialNo"))
                product.setIsSerialNumber(Boolean.parseBoolean(request.getParameter("isSerialNo")));
            product.setIsBatchNumber(Boolean.parseBoolean(request.getParameter("isBatchNo")));
            product.setIsInventory(Boolean.parseBoolean(request.getParameter("isInventory")));
            if (paramMap.containsKey("isWarranty")) {
                product.setIsWarrantyApplicable(Boolean.parseBoolean(request.getParameter("isWarranty")));
                if (Boolean.parseBoolean(request.getParameter("isWarranty"))) {
                    product.setWarrantyDays(Integer.parseInt(request.getParameter("nodays")));
                } else {
                    product.setWarrantyDays(0);
                }
            } else {
                product.setIsWarrantyApplicable(false);
            }
            if (paramMap.containsKey("isGroup"))
                product.setIsGroup(Boolean.parseBoolean(request.getParameter("isGroup")));
//            if (paramMap.containsKey("isFormulation"))
//                product.setIsFormulation(Boolean.parseBoolean(request.getParameter("isFormulation")));
            if (paramMap.containsKey("isCategory"))
                product.setIsCategory(Boolean.parseBoolean(request.getParameter("isCategory")));
            if (paramMap.containsKey("isSubCategory"))
                product.setIsSubCategory(Boolean.parseBoolean(request.getParameter("isSubCategory")));
            if (paramMap.containsKey("isMIS")) product.setIsMIS(Boolean.parseBoolean(request.getParameter("isMIS")));
            if (request.getFile("uploadImage") != null) {
                MultipartFile image = request.getFile("uploadImage");
                fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
                String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
                if (imagePath != null) {
                    product.setUploadImage(File.separator + "uploads" + File.separator + imagePath);
                }
            }
            product.setCreatedBy(users.getId());

            /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
            if (paramMap.containsKey("shelfId")) product.setShelfId(request.getParameter("shelfId"));
            if (paramMap.containsKey("barcodeSaleQuantity"))
                product.setBarcodeSalesQty(Double.parseDouble(request.getParameter("barcodeSaleQuantity")));
            if (paramMap.containsKey("purchaseRate"))
                product.setPurchaseRate(Double.parseDouble(request.getParameter("purchaseRate")));
            if (paramMap.containsKey("margin"))
                product.setMarginPer(Double.parseDouble(request.getParameter("margin")));
            PackingMaster mPackingMaster = null;
            Group mGroupMaster = null;
            Brand mBrandMaster = null;
            Category mCategoryMaster = null;
            Subcategory msubCategory = null;
            Subgroup mSubgroup = null;
            if (paramMap.containsKey("brandId")) {
                mBrandMaster = brandRepository.findByIdAndStatus(Long.parseLong(request.getParameter("brandId")), true);
                product.setBrand(mBrandMaster);
            }
            if (paramMap.containsKey("packagingId")) {
                mPackingMaster = packingMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("packagingId")), true);
                product.setPackingMaster(mPackingMaster);
            }
            if (paramMap.containsKey("groupId")) {
                mGroupMaster = groupRepository.findByIdAndStatus(Long.parseLong(request.getParameter("groupId")), true);
                product.setGroup(mGroupMaster);
            }
            if (paramMap.containsKey("subgroupId")) {
                mSubgroup = subgroupRepository.findByIdAndStatus(Long.parseLong(request.getParameter("subgroupId")), true);
                product.setSubgroup(mSubgroup);
            }
            if (paramMap.containsKey("categoryId")) {
                mCategoryMaster = categoryRepository.findByIdAndStatus(Long.parseLong(request.getParameter("categoryId")), true);
                product.setCategory(mCategoryMaster);
            }
            if (paramMap.containsKey("subcategoryId")) {
                msubCategory = subcategoryRepository.findByIdAndStatus(Long.parseLong(request.getParameter("subcategoryId")), true);
                product.setSubcategory(msubCategory);
            }
            if (paramMap.containsKey("weight")) product.setWeight(Double.parseDouble(request.getParameter("weight")));
            if (paramMap.containsKey("weightUnit")) product.setWeightUnit(request.getParameter("weightUnit"));
            if (paramMap.containsKey("disPer1"))
                product.setDiscountInPer(Double.parseDouble(request.getParameter("disPer1")));
            if (paramMap.containsKey("hsnNo")) {
                productHsn = productHsnRepository.findByIdAndStatus(Long.parseLong(request.getParameter("hsnNo")), true);
                if (productHsn != null) {
                    product.setProductHsn(productHsn);
                }
            }
            if (paramMap.containsKey("tax")) {

                taxMaster = taxMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("tax")), true);
                if (taxMaster != null) {
                    product.setTaxMaster(taxMaster);
                }
                if (paramMap.containsKey("taxApplicableDate"))
                    applicableDate = LocalDate.parse(request.getParameter("taxApplicableDate"));
                product.setApplicableDate(applicableDate);
            }
            if (paramMap.containsKey("taxType")) product.setTaxType(request.getParameter("taxType"));
            if (paramMap.containsKey("igst")) product.setIgst(Double.parseDouble(request.getParameter("igst")));
            if (paramMap.containsKey("cgst")) product.setCgst(Double.parseDouble(request.getParameter("cgst")));
            if (paramMap.containsKey("sgst")) product.setSgst(Double.parseDouble(request.getParameter("sgst")));
            if (paramMap.containsKey("minStock"))
                product.setMinStock(Double.parseDouble(request.getParameter("minStock")));
            if (paramMap.containsKey("maxStock"))
                product.setMaxStock(Double.parseDouble(request.getParameter("maxStock")));
            if (paramMap.containsKey("ecomType") && !request.getParameter("ecomType").isEmpty())
                product.setEcommerceTypeId(Long.valueOf(request.getParameter("ecomType")));
            if (paramMap.containsKey("ecomPrice") && !request.getParameter("ecomPrice").isEmpty())
                product.setSellingPrice(Double.parseDouble(request.getParameter("ecomPrice")));
            if (paramMap.containsKey("ecomDiscount") && !request.getParameter("ecomDiscount").isEmpty())
                product.setDiscountPer(Double.parseDouble(request.getParameter("ecomDiscount")));
            if (paramMap.containsKey("ecomAmount") && !request.getParameter("ecomAmount").isEmpty())
                product.setAmount(Double.parseDouble(request.getParameter("ecomAmount")));
            if (paramMap.containsKey("ecomLoyality") && !request.getParameter("ecomLoyality").isEmpty())
                product.setLoyalty(Double.parseDouble(request.getParameter("ecomLoyality")));
            if (request.getFile("image1") != null) {
                MultipartFile image = request.getFile("image1");
                fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
                String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

                if (imagePath != null) {
                    product.setImage1("/uploads" + File.separator + "product" + File.separator + imagePath);
                } else {
                    responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    responseObject.setMessage("Failed to upload documents. Please try again!");

                }
            }
            if (request.getFile("image2") != null) {
                MultipartFile image = request.getFile("image2");
                fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
                String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

                if (imagePath != null) {
                    product.setImage2("/uploads" + File.separator + "product" + File.separator + imagePath);
                } else {
                    responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    responseObject.setMessage("Failed to upload documents. Please try again!");

                }
            }
            if (request.getFile("image3") != null) {
                MultipartFile image = request.getFile("image3");
                fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
                String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

                if (imagePath != null) {
                    product.setImage3("/uploads" + File.separator + "product" + File.separator + imagePath);
                } else {
                    responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    responseObject.setMessage("Failed to upload documents. Please try again!");

                }
            }
            if (request.getFile("image4") != null) {
                MultipartFile image = request.getFile("image4");
                fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
                String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

                if (imagePath != null) {
                    product.setImage4("/uploads" + File.separator + "product" + File.separator + imagePath);
                } else {
                    responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    responseObject.setMessage("Failed to upload documents. Please try again!");

                }
            }
            if (request.getFile("image5") != null) {
                MultipartFile image = request.getFile("image5");
                fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
                String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

                if (imagePath != null) {
                    product.setImage5("/uploads" + File.separator + "product" + File.separator + imagePath);
                } else {
                    responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    responseObject.setMessage("Failed to upload documents. Please try again!");

                }
            }
            product.setIsDelete(true);
            newProduct = productRepository.save(product);

            /*   inserting into ProductTax Master to maintain tax information of Product,*/
            if (applicableDate != null) {
                try {
                    ProductTaxDateMaster productTaxDateMaster = new ProductTaxDateMaster();
                    productTaxDateMaster.setProduct(newProduct);
                    productTaxDateMaster.setProductHsn(productHsn);
                    productTaxDateMaster.setTaxMaster(taxMaster);
                    productTaxDateMaster.setApplicableDate(newProduct.getApplicableDate());
                    productTaxDateMaster.setStatus(true);
                    productTaxDateMaster.setUpdatedBy(users.getId());
                    productTaxDateMaster.setOutlet(outlet);
                    productTaxDateMaster.setBranch(branch);
                    productTaxDateMasterRepository.save(productTaxDateMaster);
                } catch (Exception e) {
                    productLogger.error("Error in Product Creation-> ProductTaxDateMaster Creation-> " + e.getMessage());
                }
            }
            /***** End of inserting into ProductTax Master  *****/
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in Product Creation:" + exceptionAsString);
        }
        /**** END ****/
        try {
            JsonParser parser = new JsonParser();
            String jsonStr = request.getParameter("productrows");
            JsonElement tradeElement = parser.parse(jsonStr);
            JsonArray array = tradeElement.getAsJsonArray();
            for (JsonElement mList : array) {
                JsonObject object = mList.getAsJsonObject();
                LevelB levelB = null; //group
                LevelA levelA = null;//brand
                LevelC levelC = null;//Category
                if (!object.get("selectedLevelA").getAsString().equalsIgnoreCase("")) {
                    levelA = levelARepository.findByIdAndStatus(object.get("selectedLevelA").getAsLong(), true);
                }
                if (!object.get("selectedLevelB").getAsString().equalsIgnoreCase("")) {
                    levelB = levelBRepository.findByIdAndStatus(object.get("selectedLevelB").getAsLong(), true);
                }
                if (!object.get("selectedLevelC").getAsString().equalsIgnoreCase("")) {
                    levelC = levelCRepository.findByIdAndStatus(object.get("selectedLevelC").getAsLong(), true);
                }
                Units unit = unitsRepository.findByIdAndStatus(object.get("selectedUnit").getAsLong(), true);
                ProductUnitPacking productUnitPacking = new ProductUnitPacking();
                productUnitPacking.setUnits(unit);
                if (object.has("conv")) productUnitPacking.setUnitConversion(object.get("conv").getAsDouble());
                if (object.has("unit_marg")) productUnitPacking.setUnitConvMargn(object.get("unit_marg").getAsDouble());
                if (object.has("is_negetive") && object.get("is_negetive").getAsBoolean()) {
                    productUnitPacking.setIsNegativeStocks(true);
                } else {
                    productUnitPacking.setIsNegativeStocks(false);
                }
                productUnitPacking.setMrp(object.get("mrp").getAsDouble());
                productUnitPacking.setPurchaseRate(object.get("pur_rate").getAsDouble());
                if (object.has("min_margin")) productUnitPacking.setMinMargin(object.get("min_margin").getAsDouble());
                productUnitPacking.setMinRateA(object.get("rate_1").getAsDouble());//sales Rate
                productUnitPacking.setMinRateB(object.get("rate_2").getAsDouble());
                productUnitPacking.setMinRateC(object.get("rate_3").getAsDouble());
                productUnitPacking.setStatus(true);
                productUnitPacking.setProduct(newProduct);
                productUnitPacking.setCreatedBy(users.getId());
                /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
                productUnitPacking.setMinQty(object.get("min_qty").getAsDouble());
                productUnitPacking.setMaxQty(object.get("max_qty").getAsDouble());
                productUnitPacking.setLevelA(levelA);
                productUnitPacking.setLevelB(levelB);
                productUnitPacking.setLevelC(levelC);
                productUnitRepository.save(productUnitPacking);
                /****** Inserting Product Opening Stocks ******/
                JsonArray mBatchJsonArray = object.getAsJsonArray("batchList");
                /* fiscal year mapping */
                LocalDate mDate = LocalDate.now();
                FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(mDate);
                if (mBatchJsonArray.size() > 0) {
                    for (JsonElement mBatchElement : mBatchJsonArray) {
                        ProductBatchNo productBatchNo = null;
                        JsonObject mBatchJsonObject = mBatchElement.getAsJsonObject();
                        ProductBatchNo mproductBatchNo = null;
                        Double costing = 0.0;
                        Long id = mBatchJsonObject.get("id").getAsLong();
                        if (mBatchJsonObject.get("isOpeningbatch").getAsBoolean()) {
                            mproductBatchNo = new ProductBatchNo();
                            if (fiscalYear != null) {
                                mproductBatchNo.setFiscalYear(fiscalYear);
                            }
                            if (mBatchJsonObject.has("b_no"))
                                mproductBatchNo.setBatchNo(mBatchJsonObject.get("b_no").getAsString());
                            if (mBatchJsonObject.has("b_mrp") && !mBatchJsonObject.get("b_mrp").getAsString().isEmpty())
                                mproductBatchNo.setMrp(mBatchJsonObject.get("b_mrp").getAsDouble());
                            if (mBatchJsonObject.has("b_purchase_rate") && !mBatchJsonObject.get("b_purchase_rate").getAsString().isEmpty())
                                mproductBatchNo.setPurchaseRate(mBatchJsonObject.get("b_purchase_rate").getAsDouble());
                            if (mBatchJsonObject.has("b_costing") && !mBatchJsonObject.get("b_costing").getAsString().isEmpty()) {
                                costing = mBatchJsonObject.get("b_costing").getAsDouble();
                            }
                            mproductBatchNo.setCosting(costing);
                            mproductBatchNo.setSalesRate(0.0);
                            mproductBatchNo.setMinRateA(0.0);
                            if (mBatchJsonObject.has("b_sale_rate") && !mBatchJsonObject.get("b_sale_rate").isJsonNull() && !mBatchJsonObject.get("b_sale_rate").getAsString().isEmpty()) {
                                mproductBatchNo.setSalesRate(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                                mproductBatchNo.setMinRateA(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                            }
                            mproductBatchNo.setQnty(Integer.parseInt(mBatchJsonObject.get("opening_qty").getAsString()));
                            mproductBatchNo.setOpeningQty(mBatchJsonObject.get("opening_qty").getAsDouble());
                            if (mBatchJsonObject.has("b_free_qty") && !mBatchJsonObject.get("b_free_qty").getAsString().isEmpty())
                                mproductBatchNo.setFreeQty(mBatchJsonObject.get("b_free_qty").getAsDouble());
                            if (mBatchJsonObject.has("b_manufacturing_date") &&
                                    !mBatchJsonObject.get("b_manufacturing_date").getAsString().equalsIgnoreCase("")
                                    && !mBatchJsonObject.get("b_manufacturing_date").getAsString().toLowerCase().contains("invalid"))
                                mproductBatchNo.setManufacturingDate(LocalDate.parse(mBatchJsonObject.get("b_manufacturing_date").getAsString()));
                            if (mBatchJsonObject.has("b_expiry") &&
                                    !mBatchJsonObject.get("b_expiry").getAsString().equalsIgnoreCase("")
                                    && !mBatchJsonObject.get("b_expiry").getAsString().toLowerCase().contains("invalid"))
                                mproductBatchNo.setExpiryDate(LocalDate.parse(mBatchJsonObject.get("b_expiry").getAsString()));
                            mproductBatchNo.setStatus(true);
                            mproductBatchNo.setProduct(newProduct);
                            mproductBatchNo.setOutlet(outlet);
                            mproductBatchNo.setBranch(branch);
                            mproductBatchNo.setUnits(unit);

                            productBatchNo = productBatchNoRepository.save(mproductBatchNo);
                        } else {
                            List<ProductUnitPacking> mUnitPackaging = productUnitRepository.findByProductIdAndStatus(newProduct.getId(), true);
                            if (mUnitPackaging != null) {
                                if (mBatchJsonObject.has("b_costing") && !mBatchJsonObject.get("b_costing").getAsString().equalsIgnoreCase(""))
                                    costing = mBatchJsonObject.get("b_costing").getAsDouble();
                                mUnitPackaging.get(0).setCosting(costing);
                                productUnitRepository.save(mUnitPackaging.get(0));
                            }
                        }
                        try {
                            ProductOpeningStocks newOpeningStock = new ProductOpeningStocks();
                            newOpeningStock.setOpeningStocks(Double.parseDouble(mBatchJsonObject.get("opening_qty").getAsString()));
                            newOpeningStock.setProduct(newProduct);
                            newOpeningStock.setUnits(unit);
                            newOpeningStock.setBranch(branch);
                            newOpeningStock.setOutlet(outlet);
                            newOpeningStock.setProductBatchNo(productBatchNo);
                            newOpeningStock.setFiscalYear(fiscalYear);
                            if (mBatchJsonObject.has("b_free_qty") && !mBatchJsonObject.get("b_free_qty").getAsString().isEmpty())
                                newOpeningStock.setFreeOpeningQty(mBatchJsonObject.get("b_free_qty").getAsDouble());
                            if (mBatchJsonObject.has("b_mrp") && !mBatchJsonObject.get("b_mrp").getAsString().isEmpty())
                                newOpeningStock.setMrp(mBatchJsonObject.get("b_mrp").getAsDouble());
                            if (mBatchJsonObject.has("b_purchase_rate") && !mBatchJsonObject.get("b_purchase_rate").getAsString().isEmpty())
                                newOpeningStock.setPurchaseRate(mBatchJsonObject.get("b_purchase_rate").getAsDouble());
                            if (mBatchJsonObject.has("b_sale_rate") && !mBatchJsonObject.get("b_sale_rate").getAsString().isEmpty())
                                newOpeningStock.setSalesRate(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                            newOpeningStock.setLevelA(levelA);
                            newOpeningStock.setLevelB(levelB);
                            newOpeningStock.setLevelC(levelC);
                            newOpeningStock.setStatus(true);
                            newOpeningStock.setCosting(costing);
                            if (mBatchJsonObject.has("b_manufacturing_date") && !mBatchJsonObject.get("b_manufacturing_date").getAsString().equalsIgnoreCase(""))
                                newOpeningStock.setManufacturingDate(LocalDate.parse(mBatchJsonObject.get("b_manufacturing_date").getAsString()));
                            if (mBatchJsonObject.has("b_expiry") && !mBatchJsonObject.get("b_expiry").getAsString().equalsIgnoreCase(""))
                                newOpeningStock.setExpiryDate(LocalDate.parse(mBatchJsonObject.get("b_expiry").getAsString()));
                            try {
                                openingStocksRepository.save(newOpeningStock);
                            } catch (Exception e) {
                                productLogger.error("Exception:" + e.getMessage());
                            }
                        } catch (Exception e) {
                            responseObject.setMessage("Error in Product Creation");
                            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            String exceptionAsString = sw.toString();
                            productLogger.error("Error in Product Creation:" + exceptionAsString);
                        }
                    }
                    /**
                     * @implNote validation of Product Delete , if any tranx done for this product, user cant delete this product **
                     * @auther ashwins@opethic.com
                     * @version sprint 21
                     **/
                    if (newProduct != null && newProduct.getIsDelete()) {
                        newProduct.setIsDelete(false);
                        productRepository.save(newProduct);
                    }
                }
                /****** Inserting Product Opening Stocks ******/
            }
            responseObject.setMessage("Product Created Successfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setData(newProduct.getId().toString());
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in Product Creation:" + exceptionAsString);
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseObject.setMessage("Internal Server Error");
        }
        return responseObject;
    }

    public Object importProduct(MultipartHttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;

        try {
            MultipartFile excelFile = request.getFile("productfile");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            if (excelFile != null) {
                XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream());
                XSSFSheet sheet = workbook.getSheetAt(0);
                productLogger.error("Total Rows : " + sheet.getPhysicalNumberOfRows() + sdf.format(new Date()));
                for (int s = 1; s < sheet.getPhysicalNumberOfRows(); s++) {
                    productLogger.error("Record:" + s + " Current time: " + sdf.format(new Date()));
                    Product product;
                    Product newProduct;
                    XSSFRow row = sheet.getRow(s);
                    Outlet outlet = users.getOutlet();
                    productLogger.error("Before Product Find Execution... " + sdf.format(new Date()));

                    product = productRepository.findFirstByProductCodeAndProductNameAndOutletId(row.getCell(0).toString(), row.getCell(1).toString(), outlet.getId());
                    productLogger.error("After Product Find Execution... " + sdf.format(new Date()));
                    if (product == null) {
                        product = new Product();
                        if (users.getBranch() != null) branch = users.getBranch();
                        product.setBranch(branch);
                        product.setOutlet(outlet);
                        product.setProductName(row.getCell(1).toString());
                        product.setProductCode(row.getCell(0).toString());
                        product.setIsSerialNumber(false);
                        product.setIsBatchNumber(true);
                        product.setIsInventory(true);
                        product.setStatus(true);
                        product.setIsDelete(true);
                        product.setCreatedBy(users.getId());
                        PackingMaster mPackingMaster = null;
                        Group mGroupMaster = null;
                        Brand mBrandMaster = null;
                        productLogger.error("Before Brand Find Execution... " + sdf.format(new Date()));

                        mBrandMaster = brandRepository.findFirstByBrandName(row.getCell(5).toString());
                        productLogger.error("After Brand Find Execution... " + sdf.format(new Date()));
                        if (mBrandMaster == null) {
                            mBrandMaster = new Brand();
                            mBrandMaster.setBrandName(row.getCell(5).toString());
                            mBrandMaster.setBranch(branch);
                            mBrandMaster.setOutlet(outlet);
                            mBrandMaster.setStatus(true);
                            mBrandMaster.setCreatedBy(users.getId());
                            productLogger.error("Before Saving Brand  " + sdf.format(new Date()));
                            brandRepository.save(mBrandMaster);
                            productLogger.error("After Succesfully Saving Brand  " + sdf.format(new Date()));

                        }

                        product.setBrand(mBrandMaster);
                        productLogger.error("Before Packing Find Execution... " + sdf.format(new Date()));
                        mPackingMaster = packingMasterRepository.findFirstByPackNameIgnoreCase(row.getCell(2).toString());
                        productLogger.error("After Packing Find Execution... " + sdf.format(new Date()));
                        if (mPackingMaster == null) {
                            mPackingMaster = new PackingMaster();
                            mPackingMaster.setPackName(row.getCell(2).toString());
                            mPackingMaster.setBranch(branch);
                            mPackingMaster.setOutlet(outlet);
                            mPackingMaster.setStatus(true);
                            mPackingMaster.setCreatedBy(users.getId());
                            productLogger.error("Before Saving Packing  " + sdf.format(new Date()));
                            packingMasterRepository.save(mPackingMaster);
                            productLogger.error("After Succesfully Saving Packing  " + sdf.format(new Date()));

                        }
                        product.setPackingMaster(mPackingMaster);
                        productLogger.error("Before Group Find Execution... " + sdf.format(new Date()));
                        mGroupMaster = groupRepository.findFirstByGroupNameIgnoreCase(row.getCell(6).toString());
                        productLogger.error("After Group Find Execution... " + sdf.format(new Date()));
                        if (mGroupMaster == null) {
                            mGroupMaster = new Group();
                            mGroupMaster.setGroupName(row.getCell(6).toString());
                            mGroupMaster.setBranch(branch);
                            mGroupMaster.setOutlet(outlet);
                            mGroupMaster.setStatus(true);
                            mGroupMaster.setCreatedBy(users.getId());
                            productLogger.error("Before Saving Group  " + sdf.format(new Date()));
                            groupRepository.save(mGroupMaster);
                            productLogger.error("After Succesfully Saving Group  " + sdf.format(new Date()));

                        }
                        product.setGroup(mGroupMaster);
                        productLogger.error("Before ProductHsn Find Execution... " + sdf.format(new Date()));
                        ProductHsn productHsn = productHsnRepository.findByHsnNumber(row.getCell(7).getRawValue());
                        productLogger.error("After ProductHsn Find Execution... " + sdf.format(new Date()));
                        if (productHsn == null) {
                            productHsn = new ProductHsn();
                            productHsn.setHsnNumber(row.getCell(7).getRawValue());
                            productHsn.setIgst(Double.valueOf(row.getCell(10).toString()));
                            productHsn.setSgst(Double.valueOf(row.getCell(8).toString()));
                            productHsn.setCgst(Double.valueOf(row.getCell(9).toString()));
                            productHsn.setBranch(branch);
                            productHsn.setOutlet(outlet);
                            productHsn.setStatus(true);
                            productHsn.setCreatedBy(users.getId());
                            productLogger.error("Before Saving ProductHsn  " + sdf.format(new Date()));
                            productHsnRepository.save(productHsn);
                            productLogger.error("After Succesfully Saving ProductHsn  " + sdf.format(new Date()));
                        }
                        product.setProductHsn(productHsn);
                        product.setTaxType("Taxable");

                        String igst = row.getCell(10).getRawValue();
                        productLogger.error("Before TaxMaster Find Execution... " + sdf.format(new Date()));
                        TaxMaster taxMaster = taxMasterRepository.findDuplicateGSTWithOutlet(outlet.getId(), igst, true);
                        productLogger.error("After TaxMaster Find Execution... " + sdf.format(new Date()));
/*
                        TaxMaster taxMaster;

                        if (branch != null)
                            taxMaster = taxMasterRepository.findDuplicateGSTWithBranch(outlet.getId(), branch.getId(), row.getCell(12).toString(), true);
                        else
                            taxMaster = taxMasterRepository.findDuplicateGSTWithOutlet(outlet.getId(), row.getCell(12).toString(), true);

                        if (taxMaster == null) {
                            taxMaster = new TaxMaster();
                            taxMaster.setGst_per(row.getCell(12).toString());
                            taxMaster.setIgst(Double.valueOf(row.getCell(12).toString()));
                            taxMaster.setCgst(Double.valueOf(row.getCell(11).toString()));
                            taxMaster.setSgst(Double.valueOf(row.getCell(10).toString()));
                            taxMaster.setSratio(Double.valueOf("50"));
                            taxMaster.setCreatedBy(outlet.getId());
                            taxMaster.setCreatedAt(LocalDateTime.now());
                            taxMaster.setStatus(true);
                            taxMaster.setOutlet(outlet);
                            if (branch != null)
                                taxMaster.setBranch(branch);

                            taxMasterRepository.save(taxMaster);

                        }
                        product.setTaxMaster(taxMaster);*/

                        product.setTaxMaster(taxMaster);
                        product.setIgst(Double.valueOf(row.getCell(10).toString()));
                        product.setSgst(Double.valueOf(row.getCell(8).toString()));
                        product.setCgst(Double.valueOf(row.getCell(9).toString()));
                        productLogger.error("Before Saving Product " + sdf.format(new Date()));
                        newProduct = productRepository.save(product);
                        productLogger.error("After Successfully Saving Product " + sdf.format(new Date()));
                        productLogger.error("Product : " + row.getCell(1).toString() + " Saved Successfully!" + sdf.format(new Date()));

                        productLogger.error("Before Units Find Execution... " + sdf.format(new Date()));
                        Units unit = unitsRepository.findFirstByUnitNameIgnoreCase(row.getCell(4).toString());
                        productLogger.error("After Units Find Execution... " + sdf.format(new Date()));
                        if (unit == null) {
                            unit = new Units();
                            unit.setUnitName(row.getCell(4).toString());
                            unit.setUnitCode(row.getCell(4).toString());
                            unit.setBranch(branch);
                            unit.setOutlet(outlet);
                            unit.setStatus(true);
                            unit.setCreatedBy(users.getId());
                            productLogger.error("Before Saving Units  " + sdf.format(new Date()));
                            unitsRepository.save(unit);
                            productLogger.error("After Successfully Saving Units  " + sdf.format(new Date()));
                            productLogger.error("New unit : " + row.getCell(4).toString() + " found and saved!" + sdf.format(new Date()));
                        }
                        ProductUnitPacking productUnitPacking = new ProductUnitPacking();
                        productUnitPacking.setUnits(unit);
                        productUnitPacking.setUnitConversion(Double.valueOf(row.getCell(3).toString()));
                        productUnitPacking.setIsNegativeStocks(false);
                        productUnitPacking.setStatus(true);
                        productUnitPacking.setProduct(newProduct);
                        productUnitPacking.setCreatedBy(users.getId());
                        productUnitPacking.setBrand(mBrandMaster);
                        productLogger.error("Before Saving productUnitPacking  " + sdf.format(new Date()));
                        productUnitRepository.save(productUnitPacking);
                        productLogger.error("After Successfully Saving productUnitPacking  " + sdf.format(new Date()));
                        productLogger.error(s + "Product Unit : " + row.getCell(4).toString() + " Saved Successfully!" + sdf.format(new Date()));


                    } else {
                        productLogger.error("Product : " + row.getCell(1).toString() + " is already available!");
                    }


                    // No.of rows end
                }

                responseObject.setResponseStatus(200);
                responseObject.setMessage("Product import completed successfully!");
            } else {
                responseObject.setResponseStatus(400);
                responseObject.setMessage("Product import failed!");
            }
        } catch (Exception x) {
            responseObject.setResponseStatus(400);
            responseObject.setMessage("Product import failed!");

            StringWriter sw = new StringWriter();
            x.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in Product Import:" + exceptionAsString);

        }


        return responseObject;
    }

    public Object importProductStock(MultipartHttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        try {
            MultipartFile excelFile = request.getFile("productstockfile");
            if (excelFile != null) {
                XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream());
                XSSFSheet sheet = workbook.getSheetAt(0);
                for (int s = 1; s < sheet.getPhysicalNumberOfRows(); s++) {

                    Product product = null;
                    Product newProduct;
                    XSSFRow row = sheet.getRow(s);
                    Outlet outlet = users.getOutlet();

                    product = productRepository.findByProductCode(row.getCell(0).toString());

                    if (product != null) {
//                        product = new Product();
                        if (users.getBranch() != null) branch = users.getBranch();

                        ProductBatchNo productBatchNo = new ProductBatchNo();

                        if (row.getCell(7).toString().isEmpty()) {
                            System.out.println("No batch availabe, skipped at " + s);
                        } else {

                            productBatchNo.setBatchNo(row.getCell(7).toString());
                            productBatchNo.setMrp(Double.valueOf(row.getCell(4).toString().isEmpty() ? "0" : row.getCell(4).toString()));
                            productBatchNo.setSalesRate(Double.valueOf(row.getCell(6).toString().isEmpty() ? "0" : row.getCell(6).toString()));
                            productBatchNo.setPurchaseRate(Double.valueOf(row.getCell(5).toString()));
                            if (!row.getCell(8).toString().isEmpty())
                                productBatchNo.setManufacturingDate(LocalDate.parse(row.getCell(8).toString(), DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
                            if (!row.getCell(9).toString().isEmpty())
                                productBatchNo.setExpiryDate(LocalDate.parse(row.getCell(9).toString(), DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
                            productBatchNo.setOpeningQty(Double.valueOf(row.getCell(2).toString().isEmpty() ? "0" : row.getCell(2).toString()));
                            productBatchNo.setStatus(true);
                            productBatchNo.setCreatedBy(users.getId());
                            if (branch != null) productBatchNo.setBranch(branch);
                            productBatchNo.setOutlet(outlet);
                            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
                            productBatchNo.setFiscalYear(fiscalYear);
                            productBatchNo.setProduct(product);
                            productBatchNo.setPackingMaster(product.getPackingMaster());
                            productBatchNo.setGroup(product.getGroup());
                            productBatchNo.setBrand(product.getBrand());
                            productBatchNo.setCosting(Double.valueOf(row.getCell(3).toString().isEmpty() ? "0" : row.getCell(3).toString()));
                            List<ProductUnitPacking> unit = productUnitRepository.findByProductIdAndStatus(product.getId(), true);
                            productBatchNo.setUnits(unit.get(0).getUnits());
                            ProductBatchNo newProductBatchNo = productBatchNoRepository.save(productBatchNo);
                            System.out.println("Batch No : " + row.getCell(7).toString() + " Saved Successfully!");
                            try {
                                ProductOpeningStocks newOpeningStock = new ProductOpeningStocks();
                                newOpeningStock.setOpeningStocks(Double.valueOf(row.getCell(2).toString()));
                                newOpeningStock.setProduct(product);
                                if (branch != null) newOpeningStock.setBranch(branch);
                                newOpeningStock.setOutlet(outlet);
                                newOpeningStock.setProductBatchNo(newProductBatchNo);
                                newOpeningStock.setFiscalYear(fiscalYear);
                                newOpeningStock.setMrp(Double.valueOf(row.getCell(4).toString().isEmpty() ? "0" : row.getCell(4).toString()));
                                newOpeningStock.setPurchaseRate(Double.valueOf(row.getCell(5).toString().isEmpty() ? "0" : row.getCell(5).toString()));
                                newOpeningStock.setSalesRate(Double.valueOf(row.getCell(6).toString().isEmpty() ? "0" : row.getCell(6).toString()));
                                newOpeningStock.setStatus(true);
                                if (!row.getCell(8).toString().isEmpty())
                                    newOpeningStock.setManufacturingDate(LocalDate.parse(row.getCell(8).toString(), DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
                                if (!row.getCell(9).toString().isEmpty())
                                    newOpeningStock.setExpiryDate(LocalDate.parse(row.getCell(9).toString(), DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
                                newOpeningStock.setCosting(Double.valueOf(row.getCell(3).toString().isEmpty() ? "0" : row.getCell(3).toString()));
                                newOpeningStock.setUnits(unit.get(0).getUnits());
                                productBatchNo.setPackingMaster(product.getPackingMaster());

                                openingStocksRepository.save(newOpeningStock);
                                System.out.println(s + "Product Name : " + row.getCell(1).toString() + " Opening set Successfully!");
                            } catch (Exception e) {
                                responseObject.setMessage("Error in importing Opening Stock at " + s);
                                responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                                StringWriter sw = new StringWriter();
                                e.printStackTrace(new PrintWriter(sw));
                                String exceptionAsString = sw.toString();
                                productLogger.error("Error in Importing Opening Stock at:" + exceptionAsString);
                            }
                        }
                    } else {
                        System.out.println("Product : " + row.getCell(1).toString() + " not available!");
                    }


                    // No.of rows end
                }

                responseObject.setResponseStatus(200);
                responseObject.setMessage("Product stock import completed successfully!");
            } else {
                responseObject.setResponseStatus(400);
                responseObject.setMessage("Product stock import failed!");
            }
        } catch (Exception x) {
            responseObject.setResponseStatus(400);
            responseObject.setMessage("Product stock import failed!");
            StringWriter sw = new StringWriter();
            x.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in Importing Opening Stock at:" + exceptionAsString);
        }


        return responseObject;
    }

    public JsonObject getProductByIdEditNew(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long productId = Long.parseLong(request.getParameter("product_id"));
        Product mProduct = productRepository.findByIdAndStatus(productId, true);
        JsonObject result = new JsonObject();
        //   List<ProductUnitPacking> units = productUnitRepository.findByProductIdAndStatus(mProduct.getId(), true);
        JsonObject response = new JsonObject();
        if (mProduct != null) {
            /*DataLockModel dataLockModel = DataLockModel.getInstance();
            if (dataLockModel.isPresent("productMaster_" + mProduct.getId())) {
                result.addProperty("message", "Selected row already in use");
                result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            } else {*/
            //  dataLockModel.addObject("productMaster_" + mProduct.getId(), mProduct);
            response.addProperty("productName", mProduct.getProductName());
            response.addProperty("id", mProduct.getId());
            response.addProperty("description", mProduct.getDescription());
            response.addProperty("productCode", mProduct.getProductCode());
            response.addProperty("isBatchNo", mProduct.getIsBatchNumber());
            response.addProperty("isInventory", mProduct.getIsInventory());
            response.addProperty("isSerialNo", mProduct.getIsSerialNumber());
            response.addProperty("barcodeNo", mProduct.getBarcodeNo());
            response.addProperty("shelfId", mProduct.getShelfId());
            response.addProperty("barcodeSalesQty", mProduct.getBarcodeSalesQty());
            response.addProperty("purchaseRate", mProduct.getPurchaseRate());
            response.addProperty("margin", mProduct.getMarginPer());
            response.addProperty("brandId", mProduct.getBrand() != null ? mProduct.getBrand().getId() : null);
            response.addProperty("packagingId", mProduct.getPackingMaster() != null ? mProduct.getPackingMaster().getId() : null);
            response.addProperty("groupId", mProduct.getGroup() != null ? mProduct.getGroup().getId() : null);
            response.addProperty("subgroupId", mProduct.getSubgroup() != null ? mProduct.getSubgroup().getId() : null);
            response.addProperty("categoryId", mProduct.getCategory() != null ? mProduct.getCategory().getId() : null);
            response.addProperty("subcategoryId", mProduct.getSubcategory() != null ? mProduct.getSubcategory().getId() : null);
            response.addProperty("weight", mProduct.getWeight());
            response.addProperty("weightUnit", mProduct.getWeightUnit());
            response.addProperty("disPer1", mProduct.getDiscountInPer());
            response.addProperty("hsnNo", mProduct.getProductHsn() != null ? mProduct.getProductHsn().getId() : null);
            response.addProperty("tax", mProduct.getTaxMaster() != null ? mProduct.getTaxMaster().getId() : null);
            response.addProperty("taxApplicableDate", mProduct.getApplicableDate() != null ? mProduct.getApplicableDate().toString() : null);
            response.addProperty("taxType", mProduct.getTaxType() != null ? mProduct.getTaxType() : null);
            response.addProperty("igst", mProduct.getIgst() != null ? mProduct.getIgst() : null);
            response.addProperty("cgst", mProduct.getCgst() != null ? mProduct.getCgst() : null);
            response.addProperty("sgst", mProduct.getSgst() != null ? mProduct.getSgst() : null);
            response.addProperty("minStock", mProduct.getMinStock() != null ? mProduct.getMinStock() : 0.0);
            response.addProperty("maxStock", mProduct.getMaxStock() != null ? mProduct.getMaxStock() : 0.0);
            response.addProperty("isWarranty", mProduct.getIsWarrantyApplicable());
            response.addProperty("nodays", mProduct.getWarrantyDays());

            response.addProperty("isEcom", mProduct.getEcommerceTypeId() != null ? true : false);
            response.addProperty("ecomType", mProduct.getEcommerceTypeId() != null ? mProduct.getEcommerceTypeId().toString() : "");
            response.addProperty("ecomPrice", mProduct.getSellingPrice() != null ? mProduct.getSellingPrice().toString() : "");
            response.addProperty("ecomDiscount", mProduct.getDiscountPer() != null ? mProduct.getDiscountPer().toString() : "");
            response.addProperty("ecomAmount", mProduct.getAmount() != null ? mProduct.getAmount().toString() : "");
            response.addProperty("ecomLoyality", mProduct.getLoyalty() != null ? mProduct.getLoyalty().toString() : "");
            response.addProperty("isMIS", mProduct.getIsMIS() != null ? mProduct.getIsMIS() : false);
            response.addProperty("isGroup", mProduct.getIsGroup() != null ? mProduct.getIsGroup() : false);
//            response.addProperty("isFormulation", mProduct.getIsFormulation() != null ? mProduct.getIsFormulation() : false);
            response.addProperty("isCategory", mProduct.getIsCategory() != null ? mProduct.getIsCategory() : false);
            response.addProperty("isSubcategory", mProduct.getIsSubCategory() != null ? mProduct.getIsSubCategory() : false);
            response.addProperty("uploadImage", mProduct.getUploadImage() != null ? mProduct.getUploadImage() : "");

            response.addProperty("imageExists", false);
            if (mProduct.getImage1() != null || mProduct.getImage2() != null || mProduct.getImage3() != null || mProduct.getImage4() != null
                    || mProduct.getImage5() != null)
                response.addProperty("imageExists", true);
            response.addProperty("prevImage1", mProduct.getImage1() != null ? serverUrl + mProduct.getImage1() : "");
            response.addProperty("prevImage2", mProduct.getImage2() != null ? serverUrl + mProduct.getImage2() : "");
            response.addProperty("prevImage3", mProduct.getImage3() != null ? serverUrl + mProduct.getImage3() : "");
            response.addProperty("prevImage4", mProduct.getImage4() != null ? serverUrl + mProduct.getImage4() : "");
            response.addProperty("prevImage5", mProduct.getImage5() != null ? serverUrl + mProduct.getImage5() : "");
            /* getting Level A, Level B, Level C and its Units from Product Id */
            JsonArray unitArray = new JsonArray();
            unitArray = getUnitRowsNewProductEdit(mProduct.getId());
            response.add("productrows", unitArray);
            //  array.add(response);
            result.addProperty("messege", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("responseObject", response);
            // }
        } else {
            result.addProperty("messege", "empty");
            result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            result.add("responseObject", response);
        }
        return result;
    }

    private JsonArray getUnitRowsNewProductEdit(Long id) {
        List<ProductUnitPacking> mUnits = productUnitRepository.findByProductId(id);
        JsonArray unitsArray = new JsonArray();
        for (ProductUnitPacking munitPacking : mUnits) {
            JsonObject mObject = new JsonObject();
            mObject.addProperty("id", munitPacking.getId());
            mObject.addProperty("selectedLevelA", munitPacking.getLevelA() != null ? munitPacking.getLevelA().getId().toString() : "");
            mObject.addProperty("selectedLevelB", munitPacking.getLevelB() != null ? munitPacking.getLevelB().getId().toString() : "");
            mObject.addProperty("selectedLevelB", munitPacking.getLevelB() != null ? munitPacking.getLevelB().getId().toString() : "");
            mObject.addProperty("selectedLevelC", munitPacking.getLevelC() != null ? munitPacking.getLevelC().getId().toString() : "");
            mObject.addProperty("selectedUnit", munitPacking.getUnits() != null ? munitPacking.getUnits().getId().toString() : "");
            mObject.addProperty("conv", munitPacking.getUnitConversion() != null ? munitPacking.getUnitConversion() : 0);
            mObject.addProperty("unit_marg", munitPacking.getUnitConvMargn() != null ? munitPacking.getUnitConvMargn() : 0);
            mObject.addProperty("mrp", munitPacking.getMrp() != null ? munitPacking.getMrp() : 0);
            mObject.addProperty("pur_rate", munitPacking.getPurchaseRate() != null ? munitPacking.getPurchaseRate() : 0);
//            mObject.addProperty("min_margin", munitPacking.getMinMargin());
            mObject.addProperty("rate_1", munitPacking.getMinRateA() != null ? munitPacking.getMinRateA() : 0);
            mObject.addProperty("rate_2", munitPacking.getMinRateB() != null ? munitPacking.getMinRateB() : 0);
            mObject.addProperty("rate_3", munitPacking.getMinRateC() != null ? munitPacking.getMinRateC() : 0);
            mObject.addProperty("min_qty", munitPacking.getMinQty() != null ? munitPacking.getMinQty() : 0);
            mObject.addProperty("max_qty", munitPacking.getMaxQty() != null ? munitPacking.getMaxQty() : 0);
            mObject.addProperty("is_negetive", munitPacking.getIsNegativeStocks());

            /******** Batch List *****/
            JsonArray batchJsonArray = new JsonArray();
            Long levelaUnit = munitPacking.getLevelA() != null ? munitPacking.getLevelA().getId() : null;
            Long levelbUnit = munitPacking.getLevelB() != null ? munitPacking.getLevelB().getId() : null;
            Long levelcUnit = munitPacking.getLevelC() != null ? munitPacking.getLevelC().getId() : null;
            List<ProductOpeningStocks> openingStocks = openingStocksRepository.findByProductOpening(id, munitPacking.getUnits().getId(), levelaUnit, levelbUnit, levelcUnit);
            for (ProductOpeningStocks mOpeningStocks : openingStocks) {
                JsonObject mBatchObject = new JsonObject();
                mBatchObject.addProperty("id", mOpeningStocks.getId());
                mBatchObject.addProperty("b_no", mOpeningStocks.getProductBatchNo() != null ? mOpeningStocks.getProductBatchNo().getBatchNo() : "");
                mBatchObject.addProperty("batch_id", mOpeningStocks.getProductBatchNo() != null ? mOpeningStocks.getProductBatchNo().getId().toString() : "");
                mBatchObject.addProperty("opening_qty", mOpeningStocks.getOpeningStocks());
                mBatchObject.addProperty("b_free_qty", mOpeningStocks.getFreeOpeningQty() != null ? mOpeningStocks.getFreeOpeningQty().toString() : "");
                mBatchObject.addProperty("b_mrp", mOpeningStocks.getMrp() != null ? mOpeningStocks.getMrp().toString() : "");
                mBatchObject.addProperty("b_sale_rate", mOpeningStocks.getSalesRate() != null ? mOpeningStocks.getSalesRate().toString() : "");
                mBatchObject.addProperty("b_purchase_rate", mOpeningStocks.getPurchaseRate() != null ? mOpeningStocks.getPurchaseRate().toString() : "");
                mBatchObject.addProperty("b_costing", mOpeningStocks.getCosting());
                mBatchObject.addProperty("b_expiry", mOpeningStocks.getExpiryDate() != null ? mOpeningStocks.getExpiryDate().toString() : "");
                mBatchObject.addProperty("b_manufacturing_date", mOpeningStocks.getManufacturingDate() != null ? mOpeningStocks.getManufacturingDate().toString() : "");
                batchJsonArray.add(mBatchObject);
            }
            mObject.add("batchList", batchJsonArray);
            unitsArray.add(mObject);
        }
        return unitsArray;
    }

    public Object updateProduct_new(MultipartHttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        LocalDate applicableDate = null;
        ProductHsn productHsn = null;
        TaxMaster taxMaster = null;
        FileStorageProperties fileStorageProperties = new FileStorageProperties();
        Product product = productRepository.findByIdAndStatus(Long.parseLong(request.getParameter("productId")), true);
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        product.setProductName(request.getParameter("productName").trim());
        if (paramMap.containsKey("productCode")) product.setProductCode(request.getParameter("productCode"));
        else product.setProductCode("");
        if (paramMap.containsKey("productDescription"))
            product.setDescription(request.getParameter("productDescription"));
        else product.setDescription("");
        product.setStatus(true);
        if (paramMap.containsKey("barcodeNo")) product.setBarcodeNo(request.getParameter("barcodeNo"));
        else product.setBarcodeNo("");
        if (paramMap.containsKey("isSerialNo"))
            product.setIsSerialNumber(Boolean.parseBoolean(request.getParameter("isSerialNo")));
        product.setIsBatchNumber(Boolean.parseBoolean(request.getParameter("isBatchNo")));
        product.setIsInventory(Boolean.parseBoolean(request.getParameter("isInventory")));
        if (paramMap.containsKey("isWarranty")) {
            product.setIsWarrantyApplicable(Boolean.parseBoolean(request.getParameter("isWarranty")));
            if (Boolean.parseBoolean(request.getParameter("isWarranty"))) {
                product.setWarrantyDays(Integer.parseInt(request.getParameter("nodays")));
            } else {
                product.setWarrantyDays(0);
            }
        }
        if (paramMap.containsKey("isMIS")) product.setIsMIS(Boolean.parseBoolean(request.getParameter("isMIS")));
        if (paramMap.containsKey("isGroup")) product.setIsGroup(Boolean.parseBoolean(request.getParameter("isGroup")));
//        if (paramMap.containsKey("isFormulation"))
//            product.setIsFormulation(Boolean.parseBoolean(request.getParameter("isFormulation")));
        if (paramMap.containsKey("isCategory"))
            product.setIsCategory(Boolean.parseBoolean(request.getParameter("isCategory")));
        if (paramMap.containsKey("isSubCategory"))
            product.setIsSubCategory(Boolean.parseBoolean(request.getParameter("isSubCategory")));

        if (request.getFile("uploadImage") != null) {
            MultipartFile image = request.getFile("uploadImage");
            fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
            String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
            if (imagePath != null) {
                product.setUploadImage(File.separator + "uploads" + File.separator + imagePath);
            }
        }
        product.setCreatedBy(users.getId());
        /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
        if (paramMap.containsKey("shelfId")) product.setShelfId(request.getParameter("shelfId"));
        else product.setShelfId("");
        if (paramMap.containsKey("barcodeSaleQuantity"))
            product.setBarcodeSalesQty(Double.parseDouble(request.getParameter("barcodeSaleQuantity")));
        if (paramMap.containsKey("margin")) product.setMarginPer(Double.parseDouble(request.getParameter("margin")));
        else product.setMarginPer(0.0);
        PackingMaster mPackingMaster = null;
        Group mGroupMaster = null;
        Brand mBrandMaster = null;
        Category mCategoryMaster = null;
        Subcategory msubCategory = null;
        Subgroup mSubgroup = null;
        if (paramMap.containsKey("brandId")) {
            mBrandMaster = brandRepository.findByIdAndStatus(Long.parseLong(request.getParameter("brandId")), true);
            product.setBrand(mBrandMaster);
        } else {
            product.setBrand(null);
        }
        if (paramMap.containsKey("packagingId")) {
            mPackingMaster = packingMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("packagingId")), true);
            product.setPackingMaster(mPackingMaster);
        } else {
            product.setPackingMaster(null);
        }
        if (paramMap.containsKey("groupId")) {
            mGroupMaster = groupRepository.findByIdAndStatus(Long.parseLong(request.getParameter("groupId")), true);
            product.setGroup(mGroupMaster);
        } else {
            product.setGroup(null);
        }
        if (paramMap.containsKey("subgroupId")) {
            mSubgroup = subgroupRepository.findByIdAndStatus(Long.parseLong(request.getParameter("subgroupId")), true);
            product.setSubgroup(mSubgroup);
        } else {
            product.setSubgroup(null);
        }
        if (paramMap.containsKey("categoryId")) {
            mCategoryMaster = categoryRepository.findByIdAndStatus(Long.parseLong(request.getParameter("categoryId")), true);
            product.setCategory(mCategoryMaster);
        } else {
            product.setCategory(null);
        }
        if (paramMap.containsKey("subcategoryId")) {
            msubCategory = subcategoryRepository.findByIdAndStatus(Long.parseLong(request.getParameter("subcategoryId")), true);
            product.setSubcategory(msubCategory);
        } else {
            product.setSubcategory(null);
        }
        if (paramMap.containsKey("weight")) product.setWeight(Double.parseDouble(request.getParameter("weight")));
        else product.setWeight(0.0);

        if (paramMap.containsKey("weightUnit")) product.setWeightUnit(request.getParameter("weightUnit"));
        else product.setWeightUnit("");
        if (paramMap.containsKey("disPer1"))
            product.setDiscountInPer(Double.parseDouble(request.getParameter("disPer1")));
        else product.setDiscountInPer(0.0);
        if (paramMap.containsKey("hsnNo")) {
            productHsn = productHsnRepository.findByIdAndStatus(Long.parseLong(request.getParameter("hsnNo")), true);
            if (productHsn != null) {
                product.setProductHsn(productHsn);
            }
        }
        if (paramMap.containsKey("tax")) {
            taxMaster = taxMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("tax")), true);
            if (taxMaster != null) {
                product.setTaxMaster(taxMaster);
            }
            if (paramMap.containsKey("taxApplicableDate")) {
                applicableDate = LocalDate.parse(request.getParameter("taxApplicableDate"));
                product.setApplicableDate(applicableDate);
            }
        }
        if (paramMap.containsKey("taxType")) product.setTaxType(request.getParameter("taxType"));
        if (paramMap.containsKey("igst")) product.setIgst(Double.parseDouble(request.getParameter("igst")));
        if (paramMap.containsKey("cgst")) product.setCgst(Double.parseDouble(request.getParameter("cgst")));
        if (paramMap.containsKey("sgst")) product.setSgst(Double.parseDouble(request.getParameter("sgst")));
        if (paramMap.containsKey("minStock")) product.setMinStock(Double.parseDouble(request.getParameter("minStock")));
        if (paramMap.containsKey("maxStock")) product.setMaxStock(Double.parseDouble(request.getParameter("maxStock")));

        /**** END ****/
        product.setEcommerceTypeId(null);
        product.setSellingPrice(null);
        product.setDiscountPer(null);
        product.setAmount(null);
        product.setLoyalty(null);

        if (paramMap.containsKey("ecomType") && !request.getParameter("ecomType").isEmpty())
            product.setEcommerceTypeId(Long.valueOf(request.getParameter("ecomType")));
        if (paramMap.containsKey("ecomPrice") && !request.getParameter("ecomPrice").isEmpty())
            product.setSellingPrice(Double.parseDouble(request.getParameter("ecomPrice")));
        if (paramMap.containsKey("ecomDiscount") && !request.getParameter("ecomDiscount").isEmpty())
            product.setDiscountPer(Double.parseDouble(request.getParameter("ecomDiscount")));
        if (paramMap.containsKey("ecomAmount") && !request.getParameter("ecomAmount").isEmpty())
            product.setAmount(Double.parseDouble(request.getParameter("ecomAmount")));
        if (paramMap.containsKey("ecomLoyality") && !request.getParameter("ecomLoyality").isEmpty())
            product.setLoyalty(Double.parseDouble(request.getParameter("ecomLoyality")));
        if (request.getFile("image1") != null) {
            if (product.getImage1() != null) {
                System.out.println("product.getImage1() " + product.getImage1());
                File oldFile = new File("." + product.getImage1());
                if (oldFile.exists()) {
                    System.out.println("Document Deleted");
                    //remove file from local directory
                    if (!oldFile.delete()) {
                        System.out.println("Failed to delete document. Please try again!");
                    }
                }
                product.setImage1(null);
            }
            MultipartFile image = request.getFile("image1");
            fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
            String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

            if (imagePath != null) {
                product.setImage1("/uploads" + File.separator + "product" + File.separator + imagePath);
            } else {
                responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                responseMessage.setMessage("Failed to upload documents. Please try again!");
            }
        }
        if (request.getFile("image2") != null) {
            if (product.getImage2() != null) {
                System.out.println("product.getImage2() " + product.getImage2());
                File oldFile = new File("." + product.getImage2());
                if (oldFile.exists()) {
                    System.out.println("Document Deleted");
                    //remove file from local directory
                    if (!oldFile.delete()) {
                        System.out.println("Failed to delete document. Please try again!");
                    }
                }
                product.setImage2(null);
            }
            MultipartFile image = request.getFile("image2");
            fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
            String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

            if (imagePath != null) {
                product.setImage2("/uploads" + File.separator + "product" + File.separator + imagePath);
            } else {
                responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                responseMessage.setMessage("Failed to upload documents. Please try again!");
            }
        }
        if (request.getFile("image3") != null) {
            if (product.getImage3() != null) {
                System.out.println("product.getImage3() " + product.getImage3());
                File oldFile = new File("." + product.getImage3());
                if (oldFile.exists()) {
                    System.out.println("Document Deleted");
                    //remove file from local directory
                    if (!oldFile.delete()) {
                        System.out.println("Failed to delete document. Please try again!");
                    }
                }
                product.setImage3(null);
            }
            MultipartFile image = request.getFile("image3");
            fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
            String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

            if (imagePath != null) {
                product.setImage3("/uploads" + File.separator + "product" + File.separator + imagePath);
            } else {
                responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                responseMessage.setMessage("Failed to upload documents. Please try again!");
            }
        }
        if (request.getFile("image4") != null) {
            if (product.getImage4() != null) {
                System.out.println("product.getImage4() " + product.getImage4());
                File oldFile = new File("." + product.getImage4());
                if (oldFile.exists()) {
                    System.out.println("Document Deleted");
                    //remove file from local directory
                    if (!oldFile.delete()) {
                        System.out.println("Failed to delete document. Please try again!");
                    }
                }
                product.setImage4(null);
            }
            MultipartFile image = request.getFile("image4");
            fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
            String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

            if (imagePath != null) {
                product.setImage4("/uploads" + File.separator + "product" + File.separator + imagePath);
            } else {
                responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                responseMessage.setMessage("Failed to upload documents. Please try again!");
            }
        }
        if (request.getFile("image5") != null) {
            if (product.getImage5() != null) {
                System.out.println("product.getImage5() " + product.getImage5());
                File oldFile = new File("." + product.getImage5());
                if (oldFile.exists()) {
                    System.out.println("Document Deleted");
                    //remove file from local directory
                    if (!oldFile.delete()) {
                        System.out.println("Failed to delete document. Please try again!");
                    }
                }
                product.setImage5(null);
            }
            MultipartFile image = request.getFile("image5");
            fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
            String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

            if (imagePath != null) {
                product.setImage5("/uploads" + File.separator + "product" + File.separator + imagePath);
            } else {
                responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                responseMessage.setMessage("Failed to upload documents. Please try again!");
            }
        }
        try {
            Product newProduct = productRepository.save(product);
            /*   updating into ProductTax Master to maintain tax information of Product,*/
            if (applicableDate != null) {
                try {
//                    ProductTaxDateMaster productTaxDateMaster = new ProductTaxDateMaster();
                    ProductTaxDateMaster productTaxDateMaster = productTaxDateMasterRepository.findRecord(taxMaster,
                            true, applicableDate);
                    if (productTaxDateMaster == null) {
                        productTaxDateMaster = new ProductTaxDateMaster();
                        productTaxDateMaster.setProduct(newProduct);
                        productTaxDateMaster.setProductHsn(productHsn);
                        productTaxDateMaster.setTaxMaster(taxMaster);
                        productTaxDateMaster.setApplicableDate(newProduct.getApplicableDate());
                        productTaxDateMaster.setStatus(true);
                        productTaxDateMaster.setUpdatedBy(users.getId());
                        productTaxDateMaster.setOutlet(users.getOutlet());
                        productTaxDateMaster.setBranch(users.getBranch());
                        productTaxDateMasterRepository.save(productTaxDateMaster);
                    }
                } catch (Exception e) {
                    productLogger.error("Error in Product Creation-> ProductTaxDateMaster Creation-> " + e.getMessage());
                }
            }
            /***** End of inserting into ProductTax Master  *****/
            JsonParser parser = new JsonParser();
            String jsonStr = request.getParameter("productrows");
            JsonElement tradeElement = parser.parse(jsonStr);
            JsonArray array = tradeElement.getAsJsonArray();
            for (JsonElement mList : array) {
                JsonObject object = mList.getAsJsonObject();
                LevelB levelB = null; //group
                LevelA levelA = null;//brand
                LevelC levelC = null;//Category
                if (!object.get("selectedLevelA").getAsString().equalsIgnoreCase("")) {
                    levelA = levelARepository.findByIdAndStatus(object.get("selectedLevelA").getAsLong(), true);
                }
                if (!object.get("selectedLevelB").getAsString().equalsIgnoreCase("")) {
                    levelB = levelBRepository.findByIdAndStatus(object.get("selectedLevelB").getAsLong(), true);
                }
                if (!object.get("selectedLevelC").getAsString().equalsIgnoreCase("")) {
                    levelC = levelCRepository.findByIdAndStatus(object.get("selectedLevelC").getAsLong(), true);
                }
                Units unit = unitsRepository.findByIdAndStatus(object.get("selectedUnit").getAsLong(), true);
                ProductUnitPacking productUnitPacking = null;
                Long details_id = object.get("details_id").getAsLong();
                if (details_id != 0) {
                    productUnitPacking = productUnitRepository.findByIdAndStatus(details_id, true);
                } else {
                    productUnitPacking = new ProductUnitPacking();
                    productUnitPacking.setStatus(true);
                }
                productUnitPacking.setUnits(unit);
                if (object.has("conv")) productUnitPacking.setUnitConversion(object.get("conv").getAsDouble());
                if (object.has("unit_marg"))
                    productUnitPacking.setUnitConvMargn(object.get("unit_marg").getAsDouble());
                if (object.has("is_negetive") && object.get("is_negetive").getAsBoolean()) {
                    productUnitPacking.setIsNegativeStocks(true);
                } else {
                    productUnitPacking.setIsNegativeStocks(false);
                }
                productUnitPacking.setMrp(object.get("mrp").getAsDouble());
                productUnitPacking.setPurchaseRate(object.get("pur_rate").getAsDouble());
                if (object.has("min_margin"))
                    productUnitPacking.setMinMargin(object.get("min_margin").getAsDouble());
                productUnitPacking.setMinRateA(object.get("rate_1").getAsDouble());//sales Rate
                productUnitPacking.setMinRateB(object.get("rate_2").getAsDouble());
                productUnitPacking.setMinRateC(object.get("rate_3").getAsDouble());
                productUnitPacking.setStatus(true);
                productUnitPacking.setProduct(newProduct);
                productUnitPacking.setCreatedBy(users.getId());
                /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
                productUnitPacking.setMinQty(object.get("min_qty").getAsDouble());
                productUnitPacking.setMaxQty(object.get("max_qty").getAsDouble());
                productUnitPacking.setLevelA(levelA);
                productUnitPacking.setLevelB(levelB);
                productUnitPacking.setLevelC(levelC);
                productUnitPacking.setUpdatedBy(users.getId());
                productUnitRepository.save(productUnitPacking);


                /****** Inserting Product Opening Stocks ******/
                JsonArray mBatchJsonArray = object.getAsJsonArray("batchList");
                LocalDate mDate = LocalDate.now();
                FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(mDate);
                for (JsonElement mBatchElement : mBatchJsonArray) {
                    ProductBatchNo mproductBatchNo = null;
                    ProductBatchNo productBatchNo = null;
                    JsonObject mBatchJsonObject = mBatchElement.getAsJsonObject();
                    Long id = mBatchJsonObject.get("id").getAsLong();
                    if (mBatchJsonObject.get("isOpeningbatch").getAsBoolean()) {
                        String batch_id = mBatchJsonObject.get("batch_id").getAsString();
                        if (batch_id.equalsIgnoreCase("") && id == 0) {
                            mproductBatchNo = new ProductBatchNo();
                            mproductBatchNo.setStatus(true);
                            mproductBatchNo.setFiscalYear(fiscalYear);
                        } else
                            mproductBatchNo = productBatchNoRepository.findByIdAndStatus(Long.parseLong(batch_id), true);
                        if (mBatchJsonObject.has("b_no"))
                            mproductBatchNo.setBatchNo(mBatchJsonObject.get("b_no").getAsString());
                        if (mBatchJsonObject.has("b_mrp") && !mBatchJsonObject.get("b_mrp").isJsonNull() &&
                                !mBatchJsonObject.get("b_mrp").getAsString().isEmpty())
                            mproductBatchNo.setMrp(mBatchJsonObject.get("b_mrp").getAsDouble());
                        if (mBatchJsonObject.has("b_purchase_rate") &&
                                !mBatchJsonObject.get("b_purchase_rate").isJsonNull() &&
                                !mBatchJsonObject.get("b_purchase_rate").getAsString().isEmpty())
                            mproductBatchNo.setPurchaseRate(mBatchJsonObject.get("b_purchase_rate").getAsDouble());
                        if (mBatchJsonObject.has("b_sale_rate") &&
                                !mBatchJsonObject.get("b_sale_rate").isJsonNull() &&
                                !mBatchJsonObject.get("b_sale_rate").getAsString().isEmpty()) {
                            mproductBatchNo.setSalesRate(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                            mproductBatchNo.setMinRateA(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                        }
                        if (mBatchJsonObject.has("b_costing") && !mBatchJsonObject.get("b_costing").getAsString().isEmpty())
                            mproductBatchNo.setCosting(mBatchJsonObject.get("b_costing").getAsDouble());
                        if (mBatchJsonObject.has("b_free_qty") &&
                                !mBatchJsonObject.get("b_free_qty").isJsonNull() &&
                                !mBatchJsonObject.get("b_free_qty").getAsString().isEmpty())
                            mproductBatchNo.setFreeQty(mBatchJsonObject.get("b_free_qty").getAsDouble());
                        if (mBatchJsonObject.has("b_manufacturing_date") &&
                                !mBatchJsonObject.get("b_manufacturing_date").getAsString().isEmpty()
                                && !mBatchJsonObject.get("b_manufacturing_date").getAsString().toLowerCase().contains("invalid"))
                            mproductBatchNo.setManufacturingDate(LocalDate.parse(mBatchJsonObject.get("b_manufacturing_date").getAsString()));
                        if (mBatchJsonObject.has("b_expiry") &&
                                !mBatchJsonObject.get("b_expiry").getAsString().isEmpty()
                                && !mBatchJsonObject.get("b_expiry").getAsString().toLowerCase().contains("invalid"))
                            mproductBatchNo.setStatus(true);
                        mproductBatchNo.setProduct(newProduct);
                        mproductBatchNo.setOutlet(newProduct.getOutlet());
                        mproductBatchNo.setBranch(newProduct.getBranch());
                        mproductBatchNo.setUnits(unit);
                        productBatchNo = productBatchNoRepository.save(mproductBatchNo);
                    } else {

                        List<ProductUnitPacking> mUnitPackaging = productUnitRepository.findByProductIdAndStatus(newProduct.getId(), true);
                        if (mUnitPackaging != null) {
                            if (mBatchJsonObject.has("b_costing") && !mBatchJsonObject.get("b_costing").getAsString().equalsIgnoreCase("")) {
                                mUnitPackaging.get(0).setCosting(mBatchJsonObject.get("b_costing").getAsDouble());
                                productUnitRepository.save(mUnitPackaging.get(0));
                            }
                        }
                    }

                    try {

                        ProductOpeningStocks newOpeningStock = null;
                        if (id != 0) {
                            newOpeningStock = openingStocksRepository.findByIdAndStatus(id, true);
                        } else {
                            newOpeningStock = new ProductOpeningStocks();
                            newOpeningStock.setProduct(newProduct);
                            newOpeningStock.setUnits(unit);
                            newOpeningStock.setPackingMaster(mPackingMaster);
                            newOpeningStock.setBrand(mBrandMaster);
                            newOpeningStock.setGroup(mGroupMaster);
                            newOpeningStock.setCategory(mCategoryMaster);
                            newOpeningStock.setLevelA(levelA);
                            newOpeningStock.setLevelB(levelB);
                            newOpeningStock.setLevelC(levelC);
                            newOpeningStock.setStatus(true);
                            newOpeningStock.setOutlet(newProduct.getOutlet());
                            newOpeningStock.setFiscalYear(fiscalYear);
                        }
                        newOpeningStock.setOpeningStocks(Double.parseDouble(mBatchJsonObject.get("opening_qty").getAsString()));
                        newOpeningStock.setProductBatchNo(productBatchNo);
                        if (mBatchJsonObject.has("b_free_qty") && !mBatchJsonObject.get("b_free_qty").getAsString().isEmpty())
                            newOpeningStock.setFreeOpeningQty(mBatchJsonObject.get("b_free_qty").getAsDouble());
                        if (mBatchJsonObject.has("b_mrp") && !mBatchJsonObject.get("b_mrp").getAsString().isEmpty())
                            newOpeningStock.setMrp(mBatchJsonObject.get("b_mrp").getAsDouble());
                        if (mBatchJsonObject.has("b_purchase_rate") &&
                                !mBatchJsonObject.get("b_purchase_rate").getAsString().isEmpty())
                            newOpeningStock.setPurchaseRate(mBatchJsonObject.get("b_purchase_rate").getAsDouble());
                        if (mBatchJsonObject.has("b_sale_rate") && !mBatchJsonObject.get("b_sale_rate").isJsonNull() &&
                                !mBatchJsonObject.get("b_sale_rate").getAsString().isEmpty())
                            newOpeningStock.setSalesRate(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                        newOpeningStock.setLevelA(levelA);
                        newOpeningStock.setLevelB(levelB);
                        newOpeningStock.setLevelC(levelC);
                        newOpeningStock.setStatus(true);
                        if (mBatchJsonObject.has("b_manufacturing_date") &&
                                !mBatchJsonObject.get("b_manufacturing_date").getAsString().isEmpty() &&
                                !mBatchJsonObject.get("b_manufacturing_date").getAsString().toLowerCase().contains("invalid"))
                            newOpeningStock.setManufacturingDate(LocalDate.parse(mBatchJsonObject.get("b_manufacturing_date").getAsString()));
                        if (mBatchJsonObject.has("b_expiry") &&
                                !mBatchJsonObject.get("b_expiry").getAsString().isEmpty() &&
                                !mBatchJsonObject.get("b_expiry").getAsString().toLowerCase().contains("invalid"))
                            newOpeningStock.setExpiryDate(LocalDate.parse(mBatchJsonObject.get("b_expiry").getAsString()));
                        if (mBatchJsonObject.has("b_costing") && !mBatchJsonObject.get("b_costing").getAsString().isEmpty())
                            newOpeningStock.setCosting(mBatchJsonObject.get("b_costing").getAsDouble());
                        try {
                            openingStocksRepository.save(newOpeningStock);
                        } catch (Exception e) {
                            productLogger.error("Exception:" + e.getMessage());
                        }
                    } catch (Exception e) {
                        responseMessage.setMessage("Error in Product Updation");
                        responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        String exceptionAsString = sw.toString();
                        productLogger.error("Error in Product Updation:" + exceptionAsString);
                    }
                }
                /****** Inserting Product Opening Stocks ******/
            }
            /*** delete Product units while updating product ***/
            String delJsonStr = request.getParameter("rowDelDetailsIds");
            JsonElement delElement = parser.parse(delJsonStr);
            JsonArray delArray = delElement.getAsJsonArray();
            for (JsonElement mDelList : delArray) {
                JsonObject mDelObject = mDelList.getAsJsonObject();
                Long delId = mDelObject.get("del_id").getAsLong();
                ProductUnitPacking mUnitDel = productUnitRepository.findByIdAndStatus(delId, true);
                mUnitDel.setStatus(false);
                productUnitRepository.save(mUnitDel);

            }
          /*  DataLockModel dataLockModel = DataLockModel.getInstance();
            dataLockModel.removeObject("productMaster_" + product.getId());*/
            responseMessage.setMessage("Product Updated Successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
            responseMessage.setData(newProduct.getId().toString());
        } catch (Exception e) {
            responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseMessage.setMessage("Internal Server Error");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in Product Updation:" + exceptionAsString);
        }
        return responseMessage;
    }

    public Object productStockList() {
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();
        List<Product> productDetails = new ArrayList<>();
//        Users users = jwtRequestFilter.getUserDataFromToken(request.get("Authorization").substring(7));
        String searchKey = "";
        String barcodeKey = "";
//        if (paramMap.containsKey("search"))
//            searchKey = request.get("search");
//        if (paramMap.containsKey("barcode"))
//            barcodeKey = request.get("barcode");

        String query = "SELECT * FROM `product_tbl` LEFT JOIN packing_master_tbl ON product_tbl.packing_master_id=packing_master_tbl.id" + " WHERE product_tbl.status=1";

        if (!searchKey.equalsIgnoreCase("")) {
            query = query + " AND (product_name LIKE '%" + searchKey + "%' OR product_code LIKE '%" + searchKey + "%' OR barcode_no LIKE '%" + searchKey + "%' OR  packing_master_tbl.pack_name LIKE '%" + searchKey + "%')";
        }
        if (!barcodeKey.equalsIgnoreCase("")) {
            query = query + " AND barcode_no=" + barcodeKey;
        }

        Query q = entityManager.createNativeQuery(query, Product.class);
        productDetails = q.getResultList();
        if (productDetails != null && productDetails.size() > 0) {
            for (Product mDetails : productDetails) {
                List<ProductUnitPacking> productUnitPacking = productUnitRepository.findByProductIdAndStatus(mDetails.getId(), true);
                ProductBatchNo productBatchNo = productBatchNoRepository.findTop1ByProductIdAndStatusOrderByIdDesc(mDetails.getId(), true);
                JsonObject mObject = new JsonObject();
                if (productBatchNo != null) {
                    if (productBatchNo.getExpiryDate() != null) {
                        mObject.addProperty("batch_expiry", productBatchNo.getExpiryDate().toString());
                    } else {
                        mObject.addProperty("batch_expiry", "");
                    }
                }
                mObject.addProperty("hsn", mDetails.getProductHsn() != null ? mDetails.getProductHsn().getHsnNumber() : "");
                mObject.addProperty("tax_type", mDetails.getTaxType());
                mObject.addProperty("tax_per", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
                mObject.addProperty("igst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
                mObject.addProperty("cgst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getCgst() : 0);
                mObject.addProperty("sgst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getSgst() : 0);
                mObject.addProperty("id", mDetails.getId());
                mObject.addProperty("code", mDetails.getProductCode() != null ? mDetails.getProductCode() : "");
                mObject.addProperty("product_name", mDetails.getProductName());
                mObject.addProperty("packing", mDetails.getPackingMaster() != null ? mDetails.getPackingMaster().getPackName() : "");
                mObject.addProperty("barcode", mDetails.getBarcodeNo() != null ? mDetails.getBarcodeNo() : "");
                mObject.addProperty("mrp", 0.0);
                mObject.addProperty("purchaserate", 0.0);
                mObject.addProperty("sales_rate", 0.0);
                mObject.addProperty("current_stock", "0.0");
                mObject.addProperty("is_batch", mDetails.getIsBatchNumber());
                mObject.addProperty("is_inventory", mDetails.getIsInventory());
                mObject.addProperty("is_serial", mDetails.getIsSerialNumber());
                mObject.addProperty("brandName", mDetails.getBrand().getBrandName());
                mObject.addProperty("product_category", mDetails.getCategory() != null ? mDetails.getCategory().getCategoryName() : "");
                if (mDetails.getIsBatchNumber()) {
                    TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductIdOrderByIdDesc(mDetails.getId());
                    if (tranxPurInvoiceDetailsUnits != null) {
                        mObject.addProperty("mrp", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getMrp() : 0.0);
                        mObject.addProperty("sales_rate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() : 0.0);
                        mObject.addProperty("purchaserate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() : 0.0);
                        Double closingStocks = inventoryCommonPostings.getmobileClosingStockProduct(mDetails.getId(), tranxPurInvoiceDetailsUnits.getPurchaseTransaction().getFiscalYear());
                        mObject.addProperty("current_stock", closingStocks > 0 ? closingStocks.toString() : "0.0");
                        if (productBatchNo != null) {
                            mObject.addProperty("costing", productBatchNo.getCosting() != null ? productBatchNo.getCosting() : 0.0);
                        } else {
                            mObject.addProperty("costing", 0.0);
                        }
                    } else {
                        mObject.addProperty("costing", 0.0);
                    }

                } else if (mDetails.getIsSerialNumber()) {
                    System.out.println("Is Serial number:");
                    if (productUnitPacking != null && productUnitPacking.size() > 0) {
                        mObject.addProperty("unit", productUnitPacking.get(0).getUnits() != null ? productUnitPacking.get(0).getUnits().getUnitName() : "");
                        mObject.addProperty("mrp", productUnitPacking.get(0).getMrp());
                        mObject.addProperty("purchaserate", productUnitPacking.get(0).getPurchaseRate());
                        mObject.addProperty("sales_rate", productUnitPacking.get(0).getMinRateA());
                        mObject.addProperty("costing", productUnitPacking.get(0).getCosting() != null ? productUnitPacking.get(0).getCosting() : 0.0);
                    } else {
                        mObject.addProperty("mrp", 0.0);
                        mObject.addProperty("purchaserate", 0.0);
                        mObject.addProperty("sales_rate", 0.0);
                        mObject.addProperty("costing", 0.0);
                        mObject.addProperty("unit", "");
                    }
                } else {
                    if (productUnitPacking != null && productUnitPacking.size() > 0) {
                        mObject.addProperty("unit", productUnitPacking.get(0).getUnits() != null ? productUnitPacking.get(0).getUnits().getUnitName() : "");
                        mObject.addProperty("mrp", productUnitPacking.get(0).getMrp());
                        mObject.addProperty("purchaserate", productUnitPacking.get(0).getPurchaseRate());
                        mObject.addProperty("sales_rate", productUnitPacking.get(0).getMinRateA());
                        mObject.addProperty("costing", productUnitPacking.get(0).getCosting() != null ? productUnitPacking.get(0).getCosting() : 0.0);
                    } else {
                        mObject.addProperty("mrp", 0.0);
                        mObject.addProperty("purchaserate", 0.0);
                        mObject.addProperty("sales_rate", 0.0);
                        mObject.addProperty("costing", 0.0);
                        mObject.addProperty("unit", "");
                    }
                }
                result.add(mObject);
            }
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }

    public JsonObject validateProductCode(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        String productCode = "";
        Product product = null;
        JsonObject result = new JsonObject();
        try {
            if (paramMap.containsKey("productCode") && !request.getParameter("productCode").equalsIgnoreCase(""))
                productCode = request.getParameter("productCode");
            if (users.getBranch() != null)
                product = productRepository.findByduplicateProductWithBranch(users.getOutlet().getId(), users.getBranch().getId(), productCode, true);
            else {
                product = productRepository.findByduplicateProduct(users.getOutlet().getId(), productCode, true);
            }
            if (product != null) {
                result.addProperty("message", "Duplicate Product Code");
                result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            } else {
                result.addProperty("message", "new product");
                result.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in ProductCode validations:" + exceptionAsString);
        }
        return result;
    }

    public JsonObject validateProductCodeUpdate(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        String productCode = "";
        Long productId = null;
        Product product = null;
        JsonObject result = new JsonObject();
        try {
            if (paramMap.containsKey("productCode") && !request.getParameter("productCode").equalsIgnoreCase(""))
                productCode = request.getParameter("productCode");
            if (paramMap.containsKey("productId")) productId = Long.parseLong(request.getParameter("productId"));
            if (users.getBranch() != null) {
                product = productRepository.findByProductCodeAndStatusAndOutletIdAndBranchId(productCode, true, users.getOutlet().getId(), users.getBranch().getId());
            } else {
                product = productRepository.findByProductCodeAndStatusAndOutletIdAndBranchIsNull(productCode, true, users.getOutlet().getId());
            }
            if (product.getId() != productId) {
                result.addProperty("message", "Duplicate Product Code");
                result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            } else {
                result.addProperty("message", "new product");
                result.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in ProductCode validation update:" + exceptionAsString);
        }
        return result;
    }

    public JsonObject validateProductUpdate(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        Long packageId = null;
        String productName = "";
        Product product = null;
        Long productId = null;
        JsonObject result = new JsonObject();
        try {
            if (paramMap.containsKey("packageId") && !request.getParameter("packageId").equalsIgnoreCase("")) {
                packageId = Long.parseLong(request.getParameter("packageId"));
            }
            if (paramMap.containsKey("productName") && !request.getParameter("productName").equalsIgnoreCase(""))
                productName = request.getParameter("productName");
            if (paramMap.containsKey("productId")) productId = Long.parseLong(request.getParameter("productId"));
            if (users.getBranch() != null) {
                if (packageId != null) {
                    product = productRepository.findByProductNameAndPackingMasterIdAndOutletIdAndBranchIdAndStatus(productName, packageId, users.getOutlet().getId(), users.getBranch().getId(), true);
                } else {
                    product = productRepository.findByProductNameAndOutletIdAndBranchIdAndStatus(productName, users.getOutlet().getId(), users.getBranch().getId(), true);
                }
            } else {
                if (packageId != null) {
                    product = productRepository.findByProductNameAndPackingMasterIdAndOutletIdAndStatusAndBranchIsNull(productName, packageId, users.getOutlet().getId(), true);
                } else {
                    product = productRepository.findByProductNameAndOutletIdAndStatusAndBranchIsNull(productName, users.getOutlet().getId(), true);
                }
            }
            if (product.getId() != productId) {
                result.addProperty("message", "Duplicate Product");
                result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            } else {
                result.addProperty("message", "new product");
                result.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in ProductValidationUpdate:" + exceptionAsString);
        }
        return result;
    }

    public JsonObject productTransactionList(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();
        List<Product> productDetails = new ArrayList<>();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String searchKey = "";
        String barcodeKey = "";
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
        if (paramMap.containsKey("search")) searchKey = request.getParameter("search");
        if (paramMap.containsKey("barcode")) barcodeKey = request.getParameter("barcode");

        String query = "SELECT * FROM `product_tbl` LEFT JOIN packing_master_tbl ON " + "product_tbl.packing_master_id=packing_master_tbl.id" + " " + "WHERE product_tbl.outlet_id=" + users.getOutlet().getId() + " AND product_tbl.status=1";

        if (users.getBranch() != null) {
            query = query + " AND product_tbl.branch_id=" + users.getBranch().getId();
        }
        if (!searchKey.equalsIgnoreCase("")) {
            query = query + " AND (product_name LIKE '%" + searchKey + "%' OR product_code LIKE '%" + searchKey + "%' OR barcode_no LIKE '%" + searchKey + "%' OR  packing_master_tbl.pack_name LIKE '%" + searchKey + "%')";
        }
        if (!barcodeKey.equalsIgnoreCase("")) {
            query = query + " AND barcode_no=" + barcodeKey;
        }
        query = query + " LIMIT 50 ";
        System.out.println("query " + query);
        Query q = entityManager.createNativeQuery(query, Product.class);
        productDetails = q.getResultList();
        if (productDetails != null && productDetails.size() > 0) {
            for (Product mDetails : productDetails) {
                ProductBatchNo productBatchNo = productBatchNoRepository.findTop1ByProductIdAndStatusOrderByIdDesc(mDetails.getId(), true);
                List<ProductUnitPacking> productUnitPacking = productUnitRepository.findByProductIdAndStatus(mDetails.getId(), true);
                JsonObject mObject = new JsonObject();
                if (productBatchNo != null) {
                    if (productBatchNo.getExpiryDate() != null) {
                        mObject.addProperty("batch_expiry", productBatchNo.getExpiryDate().toString());
                    } else {
                        mObject.addProperty("batch_expiry", "");
                    }
                    mObject.addProperty("mrp", productBatchNo.getMrp());
                    mObject.addProperty("sales_rate", productBatchNo.getSalesRate());
                    //   mObject.addProperty("current_stock", productBatchNo.getQnty());
                }
                if (productUnitPacking != null && productUnitPacking.size() > 0) {
                    mObject.addProperty("unit", productUnitPacking.get(0).getUnits() != null ? productUnitPacking.get(0).getUnits().getUnitName() : "PCS");
                    mObject.addProperty("is_negative", productUnitPacking.get(0).getIsNegativeStocks() != null ? productUnitPacking.get(0).getIsNegativeStocks() : false);

                } else {
                    mObject.addProperty("unit", "PCS");
                }
                mObject.addProperty("hsn", mDetails.getProductHsn() != null ? mDetails.getProductHsn().getHsnNumber() : "");
                mObject.addProperty("tax_type", mDetails.getTaxType());
                mObject.addProperty("tax_per", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
                mObject.addProperty("igst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
                mObject.addProperty("cgst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getCgst() : 0);
                mObject.addProperty("sgst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getSgst() : 0);
                mObject.addProperty("id", mDetails.getId());
                mObject.addProperty("code", mDetails.getProductCode());
                mObject.addProperty("product_name", mDetails.getProductName());
                mObject.addProperty("packing", mDetails.getPackingMaster() != null ? mDetails.getPackingMaster().getPackName() : "");
                mObject.addProperty("barcode", mDetails.getBarcodeNo());
                mObject.addProperty("is_batch", mDetails.getIsBatchNumber());
                mObject.addProperty("is_inventory", mDetails.getIsInventory());
                mObject.addProperty("is_serial", mDetails.getIsSerialNumber());
                mObject.addProperty("brand", mDetails.getBrand().getBrandName());
                if (mDetails.getIsBatchNumber()) {
                    /**** with Transaction Purchase Invoice  *****/
                    TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductIdOrderByIdDesc(mDetails.getId());
                    if (tranxPurInvoiceDetailsUnits != null) {
                        mObject.addProperty("mrp", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getMrp() : 0.00);
                        mObject.addProperty("sales_rate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() : 0.00);
                        mObject.addProperty("purchaserate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() : 0.00);
                    }
                } else if (mDetails.getIsSerialNumber()) {

                } else {
                    if (productUnitPacking != null && productUnitPacking.size() > 0) {
                        mObject.addProperty("mrp", productUnitPacking.get(0).getMrp());
                        mObject.addProperty("purchaserate", productUnitPacking.get(0).getPurchaseRate());
                        mObject.addProperty("sales_rate", productUnitPacking.get(0).getMinRateA());
                    } else {
                        mObject.addProperty("mrp", 0);
                        mObject.addProperty("purchaserate", 0);
                        mObject.addProperty("sales_rate", 0);
                    }
                }
                Double productOpeningStocks = openingStocksRepository.findSumProductOpeningStocks(mDetails.getId(), mDetails.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear.getId());
                Double closingStocks = inventoryCommonPostings.getClosingStockProduct(mDetails.getId(), users.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear);
                mObject.addProperty("current_stock", (productOpeningStocks + closingStocks));
                result.add(mObject);
            }
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }

    /**** Product , Barcode and Company Barcode Search functionality of Product selection in Tranx Perticular *****/
    public JsonObject productTransactionListNew(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();
        List productDetails = new ArrayList<>();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String searchKey = "";
        String barcodeKey = "";
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
        try {
            if (paramMap.containsKey("search")) searchKey = request.getParameter("search");
            String query = "SELECT product_tbl.id FROM `product_tbl` LEFT JOIN product_barcode_tbl ON " +
                    "product_tbl.id=product_barcode_tbl.id WHERE product_tbl.outlet_id=" + users.getOutlet().getId() +
                    " AND product_tbl.status=1";

            if (users.getBranch() != null) {
                query = query + " AND product_tbl.branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND product_tbl.branch_id IS NULL";

            }
            if (!searchKey.equalsIgnoreCase("")) {
                query = query + " AND (product_tbl.product_name LIKE '%" + searchKey + "%' OR " +
                        "product_tbl.product_code LIKE '%" + searchKey + "%' " + "OR " +
                        "product_barcode_tbl.barcode_unique_code LIKE '%" + searchKey + "%' OR " +
                        "product_barcode_tbl.company_barcode LIKE '%" + searchKey + "%')";
            }

            query = query + " LIMIT 50 ";
            Query q = entityManager.createNativeQuery(query);
            productDetails = q.getResultList();
            if (productDetails != null && productDetails.size() > 0) {
                for (Object mList : productDetails) {
                    Product mDetails = productRepository.findByIdAndStatus(Long.parseLong(mList.toString()), true);
                    ProductBatchNo productBatchNo = productBatchNoRepository.findTop1ByProductIdAndStatusOrderByIdDesc(mDetails.getId(), true);
                    List<ProductUnitPacking> productUnitPacking = productUnitRepository.findByProductIdAndStatus(mDetails.getId(), true);
                    JsonObject mObject = new JsonObject();
                    if (productBatchNo != null) {
                        if (productBatchNo.getExpiryDate() != null) {
                            mObject.addProperty("batch_expiry", productBatchNo.getExpiryDate().toString());
                        } else {
                            mObject.addProperty("batch_expiry", "");
                        }
                        mObject.addProperty("mrp", productBatchNo.getMrp());
                        mObject.addProperty("sales_rate", productBatchNo.getSalesRate());
                        //   mObject.addProperty("current_stock", productBatchNo.getQnty());
                    }
                    if (productUnitPacking != null && productUnitPacking.size() > 0) {
                        mObject.addProperty("unit", productUnitPacking.get(0).getUnits() != null ? productUnitPacking.get(0).getUnits().getUnitName() : "PCS");
                        mObject.addProperty("is_negative", productUnitPacking.get(0).getIsNegativeStocks() != null ? productUnitPacking.get(0).getIsNegativeStocks() : false);

                    } else {
                        mObject.addProperty("unit", "PCS");
                    }
                    mObject.addProperty("hsn", mDetails.getProductHsn() != null ? mDetails.getProductHsn().getHsnNumber() : "");
                    mObject.addProperty("tax_type", mDetails.getTaxType());
                    mObject.addProperty("tax_per", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
                    mObject.addProperty("igst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
                    mObject.addProperty("cgst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getCgst() : 0);
                    mObject.addProperty("sgst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getSgst() : 0);
                    mObject.addProperty("id", mDetails.getId());
                    mObject.addProperty("code", mDetails.getProductCode());
                    mObject.addProperty("product_name", mDetails.getProductName());
                    mObject.addProperty("packing", mDetails.getPackingMaster() != null ? mDetails.getPackingMaster().getPackName() : "");
                    mObject.addProperty("barcode", mDetails.getBarcodeNo());
                    mObject.addProperty("is_batch", mDetails.getIsBatchNumber());
                    mObject.addProperty("is_inventory", mDetails.getIsInventory());
                    mObject.addProperty("is_serial", mDetails.getIsSerialNumber());
                    mObject.addProperty("brand", mDetails.getBrand().getBrandName());
                    //   mObject.addProperty("productType", mDetails.getProductType());
                    if (mDetails.getIsBatchNumber()) {
                        /**** with Transaction Purchase Invoice  *****/
                        TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductIdOrderByIdDesc(mDetails.getId());
                        if (tranxPurInvoiceDetailsUnits != null) {
                            mObject.addProperty("mrp", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getMrp() : 0.00);
                            mObject.addProperty("sales_rate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() : 0.00);
                            mObject.addProperty("purchaserate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() : 0.00);
                        }
                    } else if (mDetails.getIsSerialNumber()) {

                    } else {
                        if (productUnitPacking != null && productUnitPacking.size() > 0) {
                            mObject.addProperty("mrp", productUnitPacking.get(0).getMrp());
                            mObject.addProperty("purchaserate", productUnitPacking.get(0).getPurchaseRate());
                            mObject.addProperty("sales_rate", productUnitPacking.get(0).getMinRateA());
                        } else {
                            mObject.addProperty("mrp", 0);
                            mObject.addProperty("purchaserate", 0);
                            mObject.addProperty("sales_rate", 0);
                        }
                    }
                    Double productOpeningStocks = openingStocksRepository.findSumProductOpeningStocks(mDetails.getId(), mDetails.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear.getId());
                    Double freeQty = inventoryCommonPostings.calculateFreeQty(mDetails.getId(), users.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear);
                    Double closingStocks = inventoryCommonPostings.getClosingStockProduct(mDetails.getId(), users.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear);
                    Double currentStock = closingStocks + productOpeningStocks + freeQty;
                    mObject.addProperty("current_stock", currentStock);
                    result.add(mObject);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in productTransactionListNew:" + exceptionAsString);
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }


//    public JsonObject productTransactionListNew(HttpServletRequest request) {
//        Map<String, String[]> paramMap = request.getParameterMap();
//        JsonObject response = new JsonObject();
//        JsonArray result = new JsonArray();
//        List productDetails = new ArrayList<>();
//        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//        String searchKey = "";
//        String barcodeKey = "";
//        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
//        try {
//            if (paramMap.containsKey("search")) searchKey = request.getParameter("search");
//            String query = "SELECT product_tbl.id FROM `product_tbl` LEFT JOIN product_barcode_tbl ON " +
//                    "product_tbl.id=product_barcode_tbl.id WHERE product_tbl.outlet_id=" + users.getOutlet().getId() +
//                    " AND product_tbl.status=1";
//
//            if (users.getBranch() != null) {
//                query = query + " AND product_tbl.branch_id=" + users.getBranch().getId();
//            } else {
//                query = query + " AND product_tbl.branch_id IS NULL";
//
//            }
//            if (!searchKey.equalsIgnoreCase("")) {
//                query = query + " AND (product_tbl.product_name LIKE '%" + searchKey + "%' OR " +
//                        "product_tbl.product_code LIKE '%" + searchKey + "%' " + "OR " +
//                        "product_barcode_tbl.barcode_unique_code LIKE '%" + searchKey + "%' OR " +
//                        "product_barcode_tbl.company_barcode LIKE '%" + searchKey + "%')";
//            }
//
//            query = query + " LIMIT 50 ";
//            Query q = entityManager.createNativeQuery(query);
//            productDetails = q.getResultList();
//            if (productDetails != null && productDetails.size() > 0) {
//                for (Object mList : productDetails) {
//                    Product mDetails = productRepository.findByIdAndStatus(Long.parseLong(mList.toString()), true);
//                    ProductBatchNo productBatchNo = productBatchNoRepository.findTop1ByProductIdAndStatusOrderByIdDesc(mDetails.getId(), true);
//                    List<ProductUnitPacking> productUnitPacking = productUnitRepository.findByProductIdAndStatus(mDetails.getId(), true);
//                    JsonObject mObject = new JsonObject();
//                    if (productBatchNo != null) {
//                        if (productBatchNo.getExpiryDate() != null) {
//                            mObject.addProperty("batch_expiry", productBatchNo.getExpiryDate().toString());
//                        } else {
//                            mObject.addProperty("batch_expiry", "");
//                        }
//                        mObject.addProperty("mrp", productBatchNo.getMrp());
//                        mObject.addProperty("sales_rate", productBatchNo.getSalesRate());
//                        //   mObject.addProperty("current_stock", productBatchNo.getQnty());
//                    }
//                    if (productUnitPacking != null && productUnitPacking.size() > 0) {
//                        mObject.addProperty("unit", productUnitPacking.get(0).getUnits() != null ? productUnitPacking.get(0).getUnits().getUnitName() : "PCS");
//                        mObject.addProperty("is_negative", productUnitPacking.get(0).getIsNegativeStocks() != null ? productUnitPacking.get(0).getIsNegativeStocks() : false);
//
//                    } else {
//                        mObject.addProperty("unit", "PCS");
//                    }
//                    mObject.addProperty("hsn", mDetails.getProductHsn() != null ? mDetails.getProductHsn().getHsnNumber() : "");
//                    mObject.addProperty("tax_type", mDetails.getTaxType());
//                    mObject.addProperty("tax_per", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
//                    mObject.addProperty("igst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
//                    mObject.addProperty("cgst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getCgst() : 0);
//                    mObject.addProperty("sgst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getSgst() : 0);
//                    mObject.addProperty("id", mDetails.getId());
//                    mObject.addProperty("code", mDetails.getProductCode());
//                    mObject.addProperty("product_name", mDetails.getProductName());
//                    mObject.addProperty("packing", mDetails.getPackingMaster() != null ? mDetails.getPackingMaster().getPackName() : "");
//                    mObject.addProperty("barcode", mDetails.getBarcodeNo());
//                    mObject.addProperty("is_batch", mDetails.getIsBatchNumber());
//                    mObject.addProperty("is_inventory", mDetails.getIsInventory());
//                    mObject.addProperty("is_serial", mDetails.getIsSerialNumber());
//                    mObject.addProperty("brand", mDetails.getBrand().getBrandName());
//                    if (mDetails.getIsBatchNumber()) {
//                        /**** with Transaction Purchase Invoice  *****/
//                        TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductIdOrderByIdDesc(mDetails.getId());
//                        if (tranxPurInvoiceDetailsUnits != null) {
//                            mObject.addProperty("mrp", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getMrp() : 0.00);
//                            mObject.addProperty("sales_rate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() : 0.00);
//                            mObject.addProperty("purchaserate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() : 0.00);
//                        }
//                    } else if (mDetails.getIsSerialNumber()) {
//
//                    } else {
//                        if (productUnitPacking != null && productUnitPacking.size() > 0) {
//                            mObject.addProperty("mrp", productUnitPacking.get(0).getMrp());
//                            mObject.addProperty("purchaserate", productUnitPacking.get(0).getPurchaseRate());
//                            mObject.addProperty("sales_rate", productUnitPacking.get(0).getMinRateA());
//                        } else {
//                            mObject.addProperty("mrp", 0);
//                            mObject.addProperty("purchaserate", 0);
//                            mObject.addProperty("sales_rate", 0);
//                        }
//                    }
//                    Double productOpeningStocks = openingStocksRepository.findSumProductOpeningStocks(mDetails.getId(), mDetails.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear.getId());
//                    Double freeQty = inventoryCommonPostings.calculateFreeQty(mDetails.getId(), users.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear);
//                    Double closingStocks = inventoryCommonPostings.getClosingStockProduct(mDetails.getId(), users.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear);
//                    Double currentStock = closingStocks + productOpeningStocks + freeQty;
//                    mObject.addProperty("current_stock", currentStock);
//                    result.add(mObject);
//                }
//            }
//        } catch (Exception e) {
//            StringWriter sw = new StringWriter();
//            e.printStackTrace(new PrintWriter(sw));
//            String exceptionAsString = sw.toString();
//            productLogger.error("Error in productTransactionListNew:" + exceptionAsString);
//        }
//        response.addProperty("message", "success");
//        response.addProperty("responseStatus", HttpStatus.OK.value());
//        response.add("list", result);
//        return response;
//    }

    public JsonObject deleteProduct(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject jsonObject = new JsonObject();
        Long productId = Long.parseLong(request.getParameter("id"));
        String source = request.getParameter("source");
        Product mProduct = productRepository.findByIdAndStatusAndIsDelete(productId, true, true);
        if (mProduct != null) {
            jsonObject.addProperty("message", "Product deleted successfully");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            if (!source.equalsIgnoreCase("product_edit")) mProduct.setStatus(false);
            productRepository.save(mProduct);
        } else {
            jsonObject.addProperty("message", "Product is used in transaction ,first delete transaction");
            jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        }
        return jsonObject;
    }
}