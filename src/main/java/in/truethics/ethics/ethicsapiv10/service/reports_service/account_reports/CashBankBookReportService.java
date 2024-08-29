package in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.FiscalYearRepository;
import in.truethics.ethics.ethicsapiv10.service.master_service.ProductService;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CashBankBookReportService {
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private FiscalYearRepository fiscalYearRepository;
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    private static final Logger productLogger = LogManager.getLogger(ProductService.class);
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;

//    public Object getCashBookTransactionDetails_(HttpServletRequest request) {
//        JsonObject cashres = new JsonObject();
//        JsonObject bankres = new JsonObject();
//        JsonObject finalRes = new JsonObject();
//        try {
//            Map<String, String[]> paramMap = request.getParameterMap();
//            LocalDate endDatep = null;
//            LocalDate startDatep = null;
//            Double opening_balance = 0.0, cash_account = 0.0, bank_account = 0.0, closing_balance = 0.0, bank_account_dr = 0.0, bank_account_cr = 0.0;
//            Double cash_dr = 0.0, cash_cr = 0.0, bank_cr = 0.0, bank_dr = 0.0, le_bank_cr = 0.0, le_bank_dr = 0.0;
//            List<Object[]> cash_obj = new ArrayList<>();
//            List<Object[]> bank_obj = new ArrayList<>();
//            JsonArray filterArray = new JsonArray();
//
//            Boolean flag = false;
//            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
//                startDatep = LocalDate.parse(request.getParameter("start_date"));
//                endDatep = LocalDate.parse(request.getParameter("end_date"));
//                flag = true;
//            } else {
//                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
//                if (fiscalYear != null) {
//                    startDatep = fiscalYear.getDateStart();
//                    endDatep = fiscalYear.getDateEnd();
//                }
//                LedgerMaster cashLedgers = ledgerMasterRepository.findByUniqueCodeAndStatus("CAIH", true);
//
//                opening_balance = ledgerMasterRepository.findByIdAndOutletIdAndStatuslm(users.getOutlet().getId(), true, cashLedgers.getId());
//
//
//            }
//            LedgerMaster cashLedgers;
//            cashLedgers = ledgerMasterRepository.findByUniqueCodeAndStatus("CAIH", true);
//
//            JsonArray innerArr = new JsonArray();
//            if (users.getBranch() != null) {
//                cash_dr = ledgerTransactionPostingsRepository.findCashBankBookTotal("CAIH",
//                        users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, "DR");
//                cash_cr = ledgerTransactionPostingsRepository.findCashBankBookTotal("CAIH",
//                        users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, "CR");
//                bank_dr = ledgerTransactionPostingsRepository.findCashBankBookTotal("BAAC",
//                        users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, "DR");
//                bank_cr = ledgerTransactionPostingsRepository.findCashBankBookTotal("BAAC",
//                        users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, "CR");
//
//
//            } else {
////                cash_dr = ledgerTransactionPostingsRepository.findCashBankBookTotalWithNoBR("CAIH",
////                        users.getOutlet().getId(), startDatep, endDatep, "DR");
////                cash_cr = ledgerTransactionPostingsRepository.findCashBankBookTotalWithNoBR("CAIH",
////                        users.getOutlet().getId(), startDatep, endDatep, "CR");
//                cash_dr = ledgerTransactionPostingsRepository.findSumDRCR(Long.valueOf(cashLedgers.getId()), startDatep, endDatep, "DR");
//
//                cash_cr = ledgerTransactionPostingsRepository.findSumDRCR(Long.valueOf(cashLedgers.getId()), startDatep, endDatep, "CR");
//
//                bank_dr = ledgerTransactionPostingsRepository.findCashBankBookTotalWithNoBR("BAAC",
//                        users.getOutlet().getId(), startDatep, endDatep, "DR");
//                bank_cr = ledgerTransactionPostingsRepository.findCashBankBookTotalWithNoBR("BAAC",
//                        users.getOutlet().getId(), startDatep, endDatep, "CR");
//
//
//            }
//            Double closingCash = opening_balance + cash_dr - cash_cr;
//            Double closingBank = opening_balance + bank_dr - bank_cr;
//
//            /**** Cash Account *****/
//            cashres.addProperty("particulars", "Cash-in-Hand");
//            cashres.addProperty("ledger", "Cash");
//            JsonObject tranxObj = new JsonObject();
//            JsonObject tranxObj1 = new JsonObject();
//
////            cashLedger.addProperty("name", "Cash");
//            tranxObj.addProperty("debit", cash_dr);
//            tranxObj.addProperty("credit", bank_cr);
//            tranxObj1.add("transactionData", tranxObj);
//
//
////            JsonArray cashLedgersArray = new JsonArray();
////            cashres.add("closing", cashLedgersArray);
////            JsonArray closingArray = new JsonArray();
//            JsonObject cloObj = new JsonObject();
//            JsonObject cloObj1 = new JsonObject();
//
//
//            if (closingCash > 0) {
//                cloObj.addProperty("debit", closingCash);
//                cloObj.addProperty("credit", 0);
//            } else {
//                cloObj.addProperty("credit", Math.abs(closingCash));
//                cloObj.addProperty("debit", 0);
//
//            }
//            cloObj1.add("closingData", cloObj);
//            filterArray.add(tranxObj1);
//            filterArray.add(cloObj1);
//            cashres.add("data", filterArray);
////            opening_balance = ledgerMasterRepository.findByIdAndOutletIdAndStatuslm(users.getOutlet().getId(), true, principle_id);
//
//            JsonArray cashArray = new JsonArray();
//            cashArray.add(cashres);
////            cashArray.add(filterArray);
//
//            /***** Bank Account *****/
//            bankres.addProperty("particulars", "Bank Accounts");
//            List<LedgerMaster> bankLedgers = new ArrayList<>();
//            if (users.getBranch() != null) {
//                bankLedgers = ledgerMasterRepository.findByUniqueCodeAndBranchIdAndOutletIdAndStatus("BAAC",
//                        users.getBranch().getId(), users.getOutlet().getId(), true);
//            } else {
//                bankLedgers = ledgerMasterRepository.findByUniqueCodeAndBranchIsNullAndOutletIdAndStatus("BAAC",
//                        users.getOutlet().getId(), true);
//            }
//            JsonArray bankledgersArray = new JsonArray();
//            for (LedgerMaster mBankLedger : bankLedgers) {
//                JsonObject ledgerObject = new JsonObject();
//                ledgerObject.addProperty("name", mBankLedger.getLedgerName());
//                ledgerObject.addProperty("ledger_master_id", mBankLedger.getId());
//                le_bank_dr = ledgerTransactionPostingsRepository.findSumDRCR(Long.valueOf(cashLedgers.getId()), startDatep, endDatep, "DR");
//                bank_account_dr = bank_account_dr + le_bank_dr;
//                le_bank_cr = ledgerTransactionPostingsRepository.findSumDRCR(Long.valueOf(cashLedgers.getId()), startDatep, endDatep, "CR");
//                bank_account_cr = bank_account_cr + le_bank_cr;
//                ledgerObject.addProperty("debit", bank_account_dr);
//                ledgerObject.addProperty("credit", bank_account_cr);
//                bankledgersArray.add(ledgerObject);
//            }
//            bankres.add("ledgerName", bankledgersArray);
//            if (closingBank > 0)
//                bankres.addProperty("debit", closingBank);
//            else
//                bankres.addProperty("credit", Math.abs(closingBank));
//            JsonArray bankArr = new JsonArray();
//            bankArr.add(bankres);
//
//            finalRes.addProperty("message", "success");
//            finalRes.addProperty("responseStatus", HttpStatus.OK.value());
//            finalRes.add("cashParticular", cashArray);
//            finalRes.add("bankParticular", bankArr);
//
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//        return finalRes;
//    }

    public Object getCashBookTransactionDetails(HttpServletRequest request) {
        JsonObject finalRes = new JsonObject();
        JsonArray particular = new JsonArray();
        JsonObject particularObject = new JsonObject();
        JsonArray bankParticular = new JsonArray();
        JsonObject bankparticularObject = new JsonObject();

        try {
            Map<String, String[]> paramMap = request.getParameterMap();
            LocalDate endDatep = null;
            LocalDate startDatep = null;
            Double opening_balance = 0.0, cash_account = 0.0, bank_account = 0.0, closing_balance = 0.0, bank_account_dr = 0.0, bank_account_cr = 0.0;
            Double cash_dr = 0.0, cash_cr = 0.0, bank_cr = 0.0, bank_dr = 0.0, le_bank_cr = 0.0, le_bank_dr = 0.0;
            Double opening_balance_dr = 0.0, opening_balance_cr = 0.0, total_opening_dr = 0.0, total_opening_cr = 0.0, total_opening_bank_dr = 0.0, total_opening_bank_cr = 0.0, opening_balance_bank = 0.0;
            Boolean flag = false;
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
                startDatep = LocalDate.parse(request.getParameter("start_date"));
                endDatep = LocalDate.parse(request.getParameter("end_date"));
                flag = true;
            } else {
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();

                }
                List<LedgerMaster> cashLedgers = ledgerMasterRepository.findByUniqueCodeAndStatus("CAIH", true);
            }
            List<LedgerMaster> cashLedgers;
            cashLedgers = ledgerMasterRepository.findByUniqueCodeAndStatus("CAIH", true);
            List<LedgerMaster> bankLedger = ledgerMasterRepository.findByUniqueCodeAndStatus("BAAC", true);
            if (users.getBranch() != null) {
                cash_dr = ledgerTransactionPostingsRepository.findCashBankBookTotal("CAIH",
                        users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, "DR");
                cash_cr = ledgerTransactionPostingsRepository.findCashBankBookTotal("CAIH",
                        users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, "CR");
                bank_dr = ledgerTransactionPostingsRepository.findCashBankBookTotal("BAAC",
                        users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, "DR");
                bank_cr = ledgerTransactionPostingsRepository.findCashBankBookTotal("BAAC",
                        users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, "CR");
            } else {
//                cash_dr = ledgerTransactionPostingsRepository.findSumDRCR(Long.valueOf(cashLedgers.getId()), startDatep, endDatep, "DR");
//
//                cash_cr = ledgerTransactionPostingsRepository.findSumDRCR(Long.valueOf(cashLedgers.getId()), startDatep, endDatep, "CR");
                cash_dr = ledgerTransactionPostingsRepository.findCashBankBookTotalWithNoBR("CAIH",
                        users.getOutlet().getId(), startDatep, endDatep, "DR");
                cash_cr = ledgerTransactionPostingsRepository.findCashBankBookTotalWithNoBR("CAIH",
                        users.getOutlet().getId(), startDatep, endDatep, "CR");

                bank_dr = ledgerTransactionPostingsRepository.findCashBankBookTotalWithNoBR("BAAC",
                        users.getOutlet().getId(), startDatep, endDatep, "DR");
                bank_cr = ledgerTransactionPostingsRepository.findCashBankBookTotalWithNoBR("BAAC",
                        users.getOutlet().getId(), startDatep, endDatep, "CR");
            }
//            Double closingCash = opening_balance + cash_dr - cash_cr;
//            Double closingBank = opening_balance + bank_dr - bank_cr;

            /**** Cash Account *****/
            particularObject.addProperty("particulars", "Cash Account");
            particularObject.addProperty("tranxDebit", cash_dr);
            particularObject.addProperty("tranxCredit", cash_cr);

            JsonArray multiData = new JsonArray();

            for (LedgerMaster ledgerMaster : cashLedgers) {
                JsonObject cashObject = new JsonObject();
                cashObject.addProperty("name", ledgerMaster.getLedgerName());
                cashObject.addProperty("id", ledgerMaster.getId());
                Double dr = ledgerTransactionPostingsRepository.findSumDRCR(ledgerMaster.getId(), startDatep, endDatep, "DR");
                Double cr = ledgerTransactionPostingsRepository.findSumDRCR(ledgerMaster.getId(), startDatep, endDatep, "CR");
                cashObject.addProperty("tranxDebit", Math.abs(dr));
                cashObject.addProperty("tranxCredit", Math.abs(cr));
                if (flag == false) {
                    if (ledgerMaster.getOpeningBalType().equalsIgnoreCase("DR")) {
                        opening_balance_dr = ledgerMasterRepository.findOpening(users.getOutlet().getId(), true, ledgerMaster.getId(), "DR");
                        total_opening_bank_dr = total_opening_bank_dr + opening_balance_dr;

                    }
                    else {
                        opening_balance_cr = ledgerMasterRepository.findOpening(users.getOutlet().getId(), true, ledgerMaster.getId(), "CR");
                        total_opening_bank_cr = total_opening_bank_cr + opening_balance_cr;
                    }

                } else {
                    if (ledgerMaster.getOpeningBalType().equalsIgnoreCase("DR")) {
                        opening_balance_dr = ledgerTransactionPostingsRepository.sumOfOpeningAmtWithdate(startDatep, "DR", ledgerMaster.getId(), true);
                        total_opening_dr = total_opening_dr + opening_balance_dr;
                    }else {
                        opening_balance_cr = ledgerTransactionPostingsRepository.sumOfOpeningAmtWithdate(startDatep, "CR", ledgerMaster.getId(), true);

                        total_opening_cr = total_opening_cr + opening_balance_cr;
                    }

//                    SELECT SUM(amount) FROM core_product_multilevel_db.ledger_transaction_postings_tbl where transaction_date<"2023-12-01" AND ledger_type="DR" AND ledger_master_id=1;
//                    SELECT SUM(amount) FROM core_product_multilevel_db.ledger_transaction_postings_tbl where transaction_date<"2023-12-01" AND ledger_type="CR" AND ledger_master_id=1;

                }
                opening_balance = opening_balance_dr - opening_balance_cr;

                if (opening_balance > 0) {
                    cashObject.addProperty("opnDebit", opening_balance);
                    cashObject.addProperty("opnCredit", 0);


                } else {
                    cashObject.addProperty("opnCredit", opening_balance);
                    cashObject.addProperty("opnDebit", 0);

                }
                closing_balance = opening_balance + dr - cr;
                if (closing_balance > 0) {
                    cashObject.addProperty("cloDebit", Math.abs(closing_balance));
                    cashObject.addProperty("cloCredit", 0);
                } else {
                    cashObject.addProperty("cloCredit", Math.abs(closing_balance));
                    cashObject.addProperty("cloDebit", 0);
                }


                multiData.add(cashObject);


            }
            particularObject.addProperty("opnDebit", total_opening_dr);
            particularObject.addProperty("opnCredit", total_opening_cr);
            Double total_opening = total_opening_dr - total_opening_cr;
            Double total_closing_amt = total_opening + cash_dr - cash_cr;
            if (total_closing_amt > 0) {
                particularObject.addProperty("cloDebit", total_closing_amt);
                particularObject.addProperty("cloCredit", 0);
            } else {
                particularObject.addProperty("cloCredit", total_closing_amt);
                particularObject.addProperty("cloDebit", 0);
            }

            particularObject.add("data", multiData);
            particular.add(particularObject);

            /**********bank Account************/
            bankparticularObject.addProperty("particulars", "Bank Account");
            bankparticularObject.addProperty("tranxDebit", bank_dr);
            bankparticularObject.addProperty("tranxCredit", bank_cr);

            JsonArray multiBankData = new JsonArray();
            for (LedgerMaster ledgerMaster : bankLedger) {
                JsonObject bankObject = new JsonObject();

                bankObject.addProperty("name", ledgerMaster.getLedgerName());
                bankObject.addProperty("id", ledgerMaster.getId());
                Double dr = ledgerTransactionPostingsRepository.findSumDRCR(ledgerMaster.getId(), startDatep, endDatep, "DR");
                Double cr = ledgerTransactionPostingsRepository.findSumDRCR(ledgerMaster.getId(), startDatep, endDatep, "CR");
                bankObject.addProperty("tranxDebit", Math.abs(dr));
                bankObject.addProperty("tranxCredit", Math.abs(cr));
                if (flag == false) {
                    if (ledgerMaster.getOpeningBalType().equalsIgnoreCase("DR")) {
                        opening_balance_dr = ledgerMasterRepository.findOpening(users.getOutlet().getId(), true, ledgerMaster.getId(), "DR");
                        total_opening_bank_dr = total_opening_bank_dr + opening_balance_dr;

                    }
                    else {
                        opening_balance_cr = ledgerMasterRepository.findOpening(users.getOutlet().getId(), true, ledgerMaster.getId(), "CR");
                        total_opening_bank_cr = total_opening_bank_cr + opening_balance_cr;
                    }

                } else {
                    if (ledgerMaster.getOpeningBalType().equalsIgnoreCase("DR")) {
                        opening_balance_dr = ledgerTransactionPostingsRepository.sumOfOpeningAmtWithdate(startDatep, "DR", ledgerMaster.getId(), true);
                        total_opening_bank_dr = total_opening_bank_dr + opening_balance_dr;
                    }else {
                        opening_balance_cr = ledgerTransactionPostingsRepository.sumOfOpeningAmtWithdate(startDatep, "CR", ledgerMaster.getId(), true);

                        total_opening_bank_cr = total_opening_bank_cr + opening_balance_cr;
                    }

//                    SELECT SUM(amount) FROM core_product_multilevel_db.ledger_transaction_postings_tbl where transaction_date<"2023-12-01" AND ledger_type="DR" AND ledger_master_id=1;
//                    SELECT SUM(amount) FROM core_product_multilevel_db.ledger_transaction_postings_tbl where transaction_date<"2023-12-01" AND ledger_type="CR" AND ledger_master_id=1;

                }
                opening_balance_bank = opening_balance_dr - opening_balance_cr;

                if (opening_balance_bank > 0) {
                    bankObject.addProperty("opnDebit", opening_balance_bank);
                    bankObject.addProperty("opnCredit", 0);

                } else {
                    bankObject.addProperty("opnCredit", opening_balance_bank);
                    bankObject.addProperty("opnDebit", 0);

                }
                closing_balance = opening_balance_bank + dr - cr;
                if (closing_balance > 0) {
                    bankObject.addProperty("cloDebit", Math.abs(closing_balance));
                    bankObject.addProperty("cloCredit", 0);
                } else {
                    bankObject.addProperty("cloCredit", Math.abs(closing_balance));
                    bankObject.addProperty("cloDebit", 0);
                }
                multiBankData.add(bankObject);

            }
            bankparticularObject.addProperty("opnDebit", total_opening_bank_dr);
            bankparticularObject.addProperty("opnCredit", total_opening_bank_cr);
            Double total_opening_bank = total_opening_bank_dr - total_opening_bank_cr;
            Double total_closing_amt_bank = total_opening_bank + bank_dr - bank_cr;
            if (total_closing_amt > 0) {
                bankparticularObject.addProperty("cloDebit", total_closing_amt_bank);
                bankparticularObject.addProperty("cloCredit", 0);
            } else {
                bankparticularObject.addProperty("cloCredit", total_closing_amt_bank);
                bankparticularObject.addProperty("cloDebit", 0);
            }


            bankparticularObject.add("data", multiBankData);
            bankParticular.add(bankparticularObject);
            finalRes.addProperty("message", "success");
            finalRes.addProperty("responseStatus", HttpStatus.OK.value());
            finalRes.addProperty("company_name", users.getOutlet().getCompanyName());
            finalRes.addProperty("d_start_date", startDatep.toString());
            finalRes.addProperty("d_end_date", endDatep.toString());
            finalRes.add("cashParticular", particular);
            finalRes.add("bankParticular", bankParticular);


        }catch (Exception e) {
            System.out.println(e);
        }
        return finalRes;
    }


    public Object getExpensesReports(HttpServletRequest request) {
        JsonObject dexp = new JsonObject();
        JsonObject indexp = new JsonObject();
        JsonObject finalRes = new JsonObject();
        try {
            Map<String, String[]> paramMap = request.getParameterMap();
            LocalDate endDatep = null;
            LocalDate startDatep = null;
            Double opening_balance = 0.0;
            Double dexp_dr = 0.0, dexp_cr = 0.0, indexp_cr = 0.0, indexp_dr = 0.0;
            Double sumdexp_dr = 0.0, sumdexp_cr = 0.0;
            Double sumindexp_dr = 0.0, sumindexp_cr = 0.0;

            Boolean flag = false;
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
                startDatep = LocalDate.parse(request.getParameter("start_date"));
                endDatep = LocalDate.parse(request.getParameter("end_date"));
                flag = true;
            } else {
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }
            JsonArray indexpledgersArray = new JsonArray();
            JsonArray dexpLedgersArray = new JsonArray();
            /***** find Direct Expenses Ledger from Ledger Master *******/
            List<LedgerMaster> directExpensLedgers = new ArrayList<>();
            directExpensLedgers = ledgerMasterRepository.findByPrinciplesIdAndStatus(11L, true);
            try {
                for (LedgerMaster mLedger : directExpensLedgers) {
                    JsonObject ledgerObject = new JsonObject();
                    ledgerObject.addProperty("name", mLedger.getLedgerName());
                    ledgerObject.addProperty("ledger_master_id", mLedger.getId());
                    dexp_dr = ledgerTransactionPostingsRepository.findSumDRCR(Long.valueOf(mLedger.getId()), startDatep, endDatep, "DR");
                    sumdexp_dr = sumdexp_dr + dexp_dr;
                    dexp_cr = ledgerTransactionPostingsRepository.findSumDRCR(Long.valueOf(mLedger.getId()), startDatep, endDatep, "CR");
                    sumdexp_cr = sumdexp_cr + dexp_cr;
                    ledgerObject.addProperty("dr", dexp_dr);
                    ledgerObject.addProperty("cr", dexp_cr);

                    dexpLedgersArray.add(ledgerObject);
                }
            } catch (Exception e) {

            }


            /***** find Indirect Expenses Ledger from Ledger Master *******/
            List<LedgerMaster> indrectExpensLedgers = new ArrayList<>();
            indrectExpensLedgers = ledgerMasterRepository.findByPrinciplesIdAndStatus(12L, true);
            try {
                for (LedgerMaster mLedger : indrectExpensLedgers) {
                    JsonObject ledgerObject = new JsonObject();
                    ledgerObject.addProperty("name", mLedger.getLedgerName());
                    ledgerObject.addProperty("ledger_master_id", mLedger.getId());
                    indexp_dr = ledgerTransactionPostingsRepository.findSumDRCR(Long.valueOf(mLedger.getId()), startDatep, endDatep, "DR");
                    sumindexp_dr = sumindexp_dr + indexp_dr;
                    indexp_cr = ledgerTransactionPostingsRepository.findSumDRCR(Long.valueOf(mLedger.getId()), startDatep, endDatep, "CR");
                    sumindexp_cr = sumindexp_cr + indexp_cr;
                    ledgerObject.addProperty("dr", indexp_dr);
                    ledgerObject.addProperty("cr", indexp_cr);
                    indexpledgersArray.add(ledgerObject);
                }
            } catch (Exception e) {
                System.out.println(e);
            }

            Double closingDexp = sumdexp_dr - sumdexp_cr; //DR Rule
            Double closingIdexp = sumindexp_dr - sumindexp_cr; //DR Rule
            /***** Direct Expenses Account *****/
            dexp.addProperty("particulars", "Direct Expenses");
            dexp.addProperty("direct_exp_dr", sumdexp_dr);
            dexp.addProperty("direct_exp_cr", sumdexp_cr);
            dexp.addProperty("ledger", "Cash");
            dexp.add("ledgerName", dexpLedgersArray);
            if (closingDexp > 0)
                dexp.addProperty("closingDidexp", closingDexp);
            else
                dexp.addProperty("closingDidexp", Math.abs(closingDexp));
            JsonArray dexpArray = new JsonArray();
            dexpArray.add(dexp);
            /***** Indirect Expenses Account *****/
            indexp.addProperty("particulars", "Indirect Expenses");
            indexp.addProperty("indirect_exp_dr", sumindexp_dr);
            indexp.addProperty("indirect_exp_cr", sumindexp_cr);
            indexp.add("ledgerName", indexpledgersArray);
            if (closingIdexp > 0)
                indexp.addProperty("closingIdexp", closingIdexp);
            else
                indexp.addProperty("closingIdexp", Math.abs(closingIdexp));
            JsonArray indexpArray = new JsonArray();
            indexpArray.add(indexp);
            finalRes.addProperty("message", "success");
            finalRes.addProperty("company_name", users.getOutlet().getCompanyName());
            finalRes.addProperty("d_start_date", startDatep.toString());
            finalRes.addProperty("d_end_date", endDatep.toString());

            finalRes.addProperty("responseStatus", HttpStatus.OK.value());
            finalRes.add("directExpensesParticular", dexpArray);
            finalRes.add("indirectExpensesParticular", indexpArray);

        } catch (Exception e) {

        }
        return finalRes;
    }
    public InputStream exportExpensesReport1(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");
            String JsonToStr1 = jsonRequest.get("list1");
            Boolean flag =true;

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            JsonArray productBatchNos1 = new JsonParser().parse(JsonToStr1).getAsJsonArray();
            System.out.println("productBatchNos1 "+productBatchNos1);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            if (productBatchNos.size() > 0 && productBatchNos1.size() >0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"PARTICULARS", "DEBIT", "CREDIT","CLOSING BALANCE","TYPE"};

                    Sheet sheet = workbook.createSheet("expenses_report");
                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    double sumOfDir = 0;
                    double sumOfInd =0;
                    double sumOfDir1 =0;
                    double sumOfInd1 =0;
                    double totalCr =0;
                    double totalDr =0;
                    int rowIdx = 1;
                    //for direct expenses
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject direct = productBatchNos.get(i).getAsJsonObject();
                        JsonArray dirArray = direct.getAsJsonArray("ledgerName");

                        Row row = sheet.createRow(rowIdx++);

                        row.createCell(0).setCellValue(direct.get("particulars").getAsString());
//                        row.createCell(1).setCellValue(batchNo.get("direct_exp_dr").getAsDouble());
//                        row.createCell(2).setCellValue(batchNo.get("direct_exp_cr").getAsDouble());
//                        row.createCell(3).setCellValue(batchNo.get("closingDidexp").getAsDouble());
//                        row.createCell(4).setCellValue("CR");
                        if(flag == true){

                                for(int j=0; j<dirArray.size(); j++){
                                    Row row1= sheet.createRow(rowIdx++);
                                    JsonObject dirObj =dirArray.get(0).getAsJsonObject();
                                    if(dirArray.size() >0){

                                    row1.createCell(0).setCellValue(dirObj.get("name").getAsString());
                                    row1.createCell(1).setCellValue(dirObj.get("dr").getAsDouble());
                                    row1.createCell(2).setCellValue(dirObj.get("cr").getAsDouble());

                                }else{
                                   row1.createCell(0).setCellValue("");
                                        row1.createCell(1).setCellValue("");
                                        row1.createCell(2).setCellValue("");
                                    }
                               sumOfDir += dirObj.get("dr").getAsDouble();
                                    sumOfInd += dirObj.get("cr").getAsDouble();
                            }

                        }
                    }
                    //for indirect expenses

                        for(int i=0;i<productBatchNos1.size(); i++){
                            JsonObject indirect = productBatchNos1.get(i).getAsJsonObject();
                            JsonArray indArr = indirect.getAsJsonArray("ledgerName");
                            JsonObject indArray = indArr.get(0).getAsJsonObject();

                            Row row = sheet.createRow(rowIdx++);
                            System.out.println("indArr "+indArray);
                            row.createCell(0).setCellValue(indirect.get("particulars").getAsString());
//                            row.createCell(1).setCellValue(indArray.get("dr").getAsDouble());
//                            row.createCell(2).setCellValue(indArray.get("cr").getAsDouble());
                            if(flag == true){

                                for (int j=0; j<indArr.size(); j++){
                                    Row row1  = sheet.createRow(rowIdx++);
                                    JsonObject indArray1 = indArr.get(j).getAsJsonObject();

                                    row1.createCell(0).setCellValue(indArray1.get("name").getAsString());
                                    row1.createCell(1).setCellValue(indArray1.get("dr").getAsDouble());
                                    row1.createCell(2).setCellValue(indArray1.get("cr").getAsDouble());

                                    sumOfDir1 += indArray1.get("dr").getAsDouble();
                                    sumOfInd1 += indArray1.get("cr").getAsDouble();
                                }
                            }
                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(1);
                    Cell cell1 = prow.createCell(2);
                    totalDr = sumOfDir1 + sumOfDir;
                    totalCr = sumOfInd + sumOfInd1;
                    cell.setCellValue(totalDr);
                    cell1.setCellValue(totalCr);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }



    public InputStream exportExcelCashbankReport(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            String JsonToStr1 = jsonRequest.get("list1");
            Boolean Allbatchflag = Boolean.valueOf(jsonRequest.get("Allbatchflag"));
            Boolean batchFlag = Boolean.valueOf(jsonRequest.get("batchFlag"));
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            JsonArray productBatchNos1 = new JsonParser().parse(JsonToStr1).getAsJsonArray();

            System.out.println("productBatchNos size:" + productBatchNos.size());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0 && productBatchNos1.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"Particulars", "Debit","Credit"};
                    Sheet sheet = workbook.createSheet(" Cash_Bank_Report");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    int sumOfQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
                        System.out.println("batcNO  " + batchNo);
                        Row row = sheet.createRow(rowIdx++);
//                        if (mfgShow) {
                            row.createCell(0).setCellValue(batchNo.get("particulars").getAsString());
                            row.createCell(1).setCellValue(batchNo.get("cloDebit").getAsString());
                            row.createCell(2).setCellValue(batchNo.get("cloCredit").getAsString());
                        JsonArray cashAcc1 = batchNo.get("data").getAsJsonArray();

                        if(Allbatchflag == true){
                            Row row1 = sheet.createRow(rowIdx++);
                            for (int j = 0; j < productBatchNos.size(); j++) {
                                JsonObject cashAcc2 = cashAcc1.get(i).getAsJsonObject();
                                row1.createCell(0).setCellValue(cashAcc2.get("name").getAsString());
                                row1.createCell(1).setCellValue(cashAcc2.get("cloDebit").getAsString());
                                row1.createCell(2).setCellValue(cashAcc2.get("cloCredit").getAsString());

                            }


                        }

                        sumOfQty += batchNo.get("cloDebit").getAsDouble();

                    }

                    for (int i = 0; i < productBatchNos1.size(); i++) {
                        JsonObject batchNo = productBatchNos1.get(i).getAsJsonObject();

                        System.out.println("batcNO  " + batchNo);
                        Row row = sheet.createRow(rowIdx++);
//                        if (mfgShow) {
                            row.createCell(0).setCellValue(batchNo.get("particulars").getAsString());
                            row.createCell(1).setCellValue(batchNo.get("cloDebit").getAsString());
                            row.createCell(2).setCellValue(batchNo.get("cloCredit").getAsString());
//
                        JsonArray bankacc = batchNo.get("data").getAsJsonArray();
                        if(batchFlag == true){
                            Row row1 = sheet.createRow(rowIdx++);
                            for (int j = 0; j < productBatchNos1.size(); j++) {
                                JsonObject bankAcc2 = bankacc.get(i).getAsJsonObject();
                                row1.createCell(0).setCellValue(bankAcc2.get("name").getAsString());
                                row1.createCell(1).setCellValue(bankAcc2.get("cloDebit").getAsString());
                                row1.createCell(2).setCellValue(bankAcc2.get("cloCredit").getAsString());

                            }

                        }



                        sumOfQty += batchNo.get("cloDebit").getAsDouble();

                    }

//
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(1);
                    cell.setCellValue(sumOfQty);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                            System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }


                }


            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }

    }


}
