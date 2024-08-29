//package in.truethics.ethics.ethicsapiv10.filter.helper;
//
//import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
//import in.truethics.ethics.ethicsapiv10.model.tranx.contra.TranxContraDetails;
//import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
//import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.contra_repository.TranxContraDetailsRepository;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//public class ContraExcelHelper {
//
//    @Autowired
//    private TranxContraDetailsRepository tranxContraDetailsRepository;
//    @Autowired
//    private LedgerTransactionPostingsRepository postingsRepository;
//
//    public String TYPE1 = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
//
//    static String[] HEADERs = {"Company Name", "Category Name", "Total Qty", "Total Amount", "Product Name", "Quantity", "Price",
//            "Total"};
//    static String SHEET = "Contra Date Wise Report";
//
//    static String[] HEADERs1 = {"Serial No", "Bill No", "Date & Time", "Sub Total",        //this is for headings
//            "Discount", "Round Off", "Item Name", "Quantity", "Price", "Total"};
//
//    public ByteArrayInputStream OrderStatusReport(List<TranxContraDetails> contra) {
//
//        try (Workbook workbook = new XSSFWorkbook()) {
//            Sheet sheet = workbook.createSheet(SHEET);     //SHEET == sheet name which is at bottom of the excel
//
//
//            // Header
//            Row headerRow = sheet.createRow(0);
//
//            // Define header cell style
//            CellStyle headerCellStyle = workbook.createCellStyle();
//            headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
//            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//
//            for (int col = 0; col < HEADERs1.length; col++) {
//                Cell cell = headerRow.createCell(col);
//                cell.setCellValue(HEADERs1[col]);
//                cell.setCellStyle(headerCellStyle);
//            }
//
//            int rowIdx = 1;
//            for (TranxContraDetails order : contra) {
//                if (order != null) {
//                    Row row = sheet.createRow(rowIdx++);
//                    //row.createCell(0).setCellValue(order.getId().toString());
//                    try {
//                        String serialNo = "1";
//                        row.createCell(0).setCellValue(serialNo);
//                        String orderNo = "";
//
//                        if (order.getBankPaymentNo() != null) {
//                            orderNo = order.getBankPaymentNo();
//                        }
//                        row.createCell(1).setCellValue(orderNo);
//
//                        String billDate = "";
//                        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//                        LocalDateTime localDateTime = LocalDateTime.parse(order.getPaymentDate().toString());
//                        System.out.println("localDateTime " + localDateTime);
//
//                        LocalDate localDate = localDateTime.toLocalDate();
//                        System.out.println("localDate " + localDate);
//
//                        billDate = LocalDate.parse(localDate.toString(), formatter1)
//                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//                        System.out.println("billDate " + billDate);
//
//                        //set Date
//                        row.createCell(2).setCellValue(billDate);
//
//
////                        Double discount = 0.0;
////                        if (order.() != null) {
////                            discount = order.getDiscount();
////                        }
////                        row.createCell(3).setCellValue(discount);
////                        Double discount = 0.0;
////                        row.createCell(6).setCellValue(discount);
//
//                        Double roundOf = 0.0;
//                        row.createCell(4).setCellValue(roundOf);
//
//
//                        Double netTotal = Double.valueOf(Long.parseLong("0"));
//                        if (order.getPaidAmount() != null) {
//                            netTotal = order.getPaidAmount();
//                        }
//                        row.createCell(5).setCellValue(netTotal);
//
////                        String paymentMode = "";
////                        if (order.getPaymentMode() != null) {
////                            paymentMode = order.getPaymentMode();
////                        }
////                        row.createCell(3).setCellValue(paymentMode);
////                        Double subTotal = null;
////                        if (order.getTotalPrice() != null) {
////                            subTotal = order.getTotalPrice();
////                        }
////                        row.createCell(4).setCellValue(subTotal);
//
//
////                        String itemWiseSale = "";
////                        row.createCell(9).setCellValue(itemWiseSale);
//                        String itemName = "";
//                        int tmp = 0;
//                        //  System.out.println("order.getOrderProductDetails() " + order.getOrderProductDetails().size());
//                        List<TranxContraDetails> orderProductDetails = new ArrayList<>();
//                        try {
//                            orderProductDetails = tranxContraDetailsRepository.findByOrderIdAndStatus(order.getId(), true);
//                        } catch (Exception e) {
//                            System.out.println("Exception: " + e.getCause());
//                        } finally {
//                            System.out.println("Size1: " + orderProductDetails.size());
//                        }
//                        System.out.println("Size2: " + orderProductDetails.size());
//                        for (TranxContraDetails mProduct : orderProductDetails) {
//                            if (tmp == 0) {
//                                row.createCell(6).setCellValue(mProduct.getBankName());
//                                row.createCell(7).setCellValue(mProduct.getLedgerName());
//                                row.createCell(8).setCellValue(mProduct.getPaidAmount());
//                                row.createCell(9).setCellValue(mProduct.getPaymentDate());
//                                tmp++;
//                            } else {
//                                row.createCell(0).setCellValue("");
//                                row.createCell(1).setCellValue("");
//                                row.createCell(2).setCellValue("");
//                                row.createCell(3).setCellValue("");
//                                row.createCell(4).setCellValue("");
//                                row.createCell(5).setCellValue("");
//                                row.createCell(6).setCellValue(mProduct.getBankName());
//                                row.createCell(7).setCellValue(mProduct.getLedgerName());
//                                row.createCell(8).setCellValue(mProduct.getPaidAmount());
//                                row.createCell(9).setCellValue(mProduct.getPaymentDate());
//                            }
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        System.out.println("Exception e");
//                    }
//                }
//            }
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            workbook.write(out);
//
//            return new ByteArrayInputStream(out.toByteArray());
//        } catch (IOException e) {
//            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
//        }
//    }
//
//}
//
