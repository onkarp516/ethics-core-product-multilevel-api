package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.master.PatientMaster;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.PatientMasterRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PatientMasterService {
    @Autowired
    private PatientMasterRepository patientMasterRepository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;
    private static final Logger doctorLogger = LogManager.getLogger(PatientMasterService.class);

    public Object createPatientMaster(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        try {
            PatientMaster patientMaster = new PatientMaster();
            patientMaster.setPatientName(request.getParameter("patientName").trim());
            if (request.getParameter("age") != null && !request.getParameter("age").equalsIgnoreCase("")) {
                patientMaster.setAge(Long.parseLong(request.getParameter("age")));
            }
            if (request.getParameter("weight") != null && !request.getParameter("weight").equalsIgnoreCase("")) {
                patientMaster.setPatientWeight(Double.parseDouble(request.getParameter("weight")));
            }
            if (request.getParameter("patientAddress") != null && !request.getParameter("patientAddress").equalsIgnoreCase("")) {
                patientMaster.setPatientAddress(request.getParameter("patientAddress").trim());
            }

            patientMaster.setMobileNumber(request.getParameter("mobileNumber").trim());
            if (request.getParameter("birthDate") != null && !request.getParameter("birthDate").equalsIgnoreCase("")) {
                LocalDate date = LocalDate.parse(request.getParameter("birthDate"));

                patientMaster.setBirthDate(date);
            }
            if (request.getParameter("tbDiagnosisDate") != null && !request.getParameter("tbDiagnosisDate").equalsIgnoreCase("")) {
                patientMaster.setTbDiagnosisDate(LocalDate.parse(request.getParameter("tbDiagnosisDate").trim()));
            }
            if (request.getParameter("tbTreatmentInitiationDate") != null && !request.getParameter("tbTreatmentInitiationDate").equalsIgnoreCase("")) {
                patientMaster.setTbTreatmentInitiationDate(LocalDate.parse(request.getParameter("tbTreatmentInitiationDate").trim()));
            }
            if (request.getParameter("idNo") != null && !request.getParameter("idNo").equalsIgnoreCase("")) {
                patientMaster.setIdNo(request.getParameter("idNo").trim());
            }
            if (request.getParameter("gender") != null && !request.getParameter("gender").equalsIgnoreCase("")) {
                patientMaster.setGender(request.getParameter("gender").trim());
            }
            if (request.getParameter("pincode") != null && !request.getParameter("pincode").equalsIgnoreCase("")) {
                patientMaster.setPincode(Long.valueOf(request.getParameter("pincode").trim()));
            }
            if (request.getParameter("bloodGroup") != null && !request.getParameter("bloodGroup").equalsIgnoreCase("")) {
                patientMaster.setBloodGroup(request.getParameter("bloodGroup").trim());
            }
            patientMaster.setOutletId(users.getOutlet().getId());
            patientMaster.setStatus(true);
            /***** Last Record ****/
            PatientMaster patientMaster1 = patientMasterRepository.findTopByOrderByIdDesc();
            patientMaster.setPatientCode("PAT#" + (patientMaster1 != null ? patientMaster1.getId() + 1L : 1L));
            PatientMaster mContent = patientMasterRepository.save(patientMaster);
            responseObject.setMessage("Customer Master created successfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setResponseObject(mContent.getId().toString());
        } catch (DataIntegrityViolationException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            doctorLogger.error("createPatientMaster-> failed to create PatientMaster" + exceptionAsString);
            responseObject.setMessage("Internal Server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            doctorLogger.error("createPatientMaster-> failed to create PatientMaster" + exceptionAsString);
            responseObject.setMessage("Error");
        }
        return responseObject;
    }

    public JsonObject getAllPatientMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<PatientMaster> list = new ArrayList<>();
        try {
//            list = patientMasterRepository.findByStatus(true);
            list = patientMasterRepository.findByOutletIdAndStatus(users.getOutlet().getId(),true);
            if (list.size() > 0) {
                for (PatientMaster mDoctor : list) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", mDoctor.getId());
                    response.addProperty("patientName", mDoctor.getPatientName());
                    response.addProperty("age", mDoctor.getAge());
                    response.addProperty("weight", mDoctor.getWeight());
                    response.addProperty("patientAddress", mDoctor.getPatientAddress());
                    response.addProperty("mobileNumber", mDoctor.getMobileNumber());
                    response.addProperty("birthDate", mDoctor.getBirthDate() != null ? mDoctor.getBirthDate().toString() : "");
//                    response.addProperty("birthDate", mDoctor.getTbDiagnosisDate() != null ? mDoctor.getTbDiagnosisDate().toString() : "");
//                    response.addProperty("birthDate", mDoctor.getTbTreatmentInitiationDate() != null ? mDoctor.getTbTreatmentInitiationDate().toString() : "");
                    response.addProperty("idNo", mDoctor.getIdNo());
                    response.addProperty("gender", mDoctor.getGender());
                    response.addProperty("bloodGroup", mDoctor.getBloodGroup());
                    response.addProperty("patientCode", mDoctor.getPatientCode());
                    response.addProperty("pincode",mDoctor.getPincode());
                    result.add(response);
                }
                res.addProperty("message", "success");
                res.addProperty("responseStatus", HttpStatus.OK.value());
                res.add("responseObject", result);

            } else {
                res.addProperty("message", "empty list");
                res.addProperty("responseStatus", HttpStatus.OK.value());
                res.add("responseObject", result);
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            doctorLogger.error("Error in getAllPatientMaster:" + exceptionAsString);
        }
        return res;
    }

    public JsonObject getPatientMasterById(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        try {
            PatientMaster patientMaster = patientMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("id")), users.getOutlet().getId(), true);
            if (patientMaster != null) {
                response.addProperty("id", patientMaster.getId());
                response.addProperty("patientName", patientMaster.getPatientName());
                response.addProperty("age", patientMaster.getAge());
                response.addProperty("weight", patientMaster.getWeight());
                response.addProperty("patientAddress", patientMaster.getPatientAddress());
                response.addProperty("mobileNumber", patientMaster.getMobileNumber());
                response.addProperty("idNo", patientMaster.getIdNo());
                response.addProperty("gender", patientMaster.getGender());
                response.addProperty("pincode", patientMaster.getPincode());
                response.addProperty("bloodGroup", patientMaster.getBloodGroup());
                response.addProperty("birthDate", patientMaster.getBirthDate() != null ? patientMaster.getBirthDate().toString() : "");
                response.addProperty("diagnosisDate", patientMaster.getTbDiagnosisDate() != null ? patientMaster.getTbDiagnosisDate().toString() : "");
                response.addProperty("tinitiationdate", patientMaster.getTbTreatmentInitiationDate() != null ? patientMaster.getTbTreatmentInitiationDate().toString() : "");
                response.addProperty("patientCode", patientMaster.getPatientCode());

                result.addProperty("message", "success");
                result.addProperty("responseStatus", HttpStatus.OK.value());
                result.add("responseObject", response);
            } else {
                result.addProperty("message", "not found");
                result.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            doctorLogger.error("Error in getDoctorMaster:" + exceptionAsString);
        }
        return result;
    }

    //
    public JsonObject updatePatientMaster(HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        try {
            PatientMaster patientMaster = patientMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("id")), users.getOutlet().getId(), true);
            patientMaster.setPatientName(request.getParameter("patientName").trim());
            if (request.getParameter("age") != null && !request.getParameter("age").equalsIgnoreCase("")) {
                patientMaster.setAge(Long.parseLong(request.getParameter("age")));
            }
            if (request.getParameter("weight") != null && !request.getParameter("weight").equalsIgnoreCase("")) {
                patientMaster.setWeight(Long.parseLong(request.getParameter("weight")));
            }
            if (request.getParameter("patientAddress") != null && !request.getParameter("patientAddress").equalsIgnoreCase("")) {
                patientMaster.setPatientAddress(request.getParameter("patientAddress").trim());
            }

            patientMaster.setMobileNumber(request.getParameter("mobileNumber").trim());
            if (request.getParameter("birthDate") != null && !request.getParameter("birthDate").equalsIgnoreCase("")) {
                LocalDate date = LocalDate.parse(request.getParameter("birthDate"));

                patientMaster.setBirthDate(date);
            }
            if (request.getParameter("tbDiagnosisDate") != null && !request.getParameter("tbDiagnosisDate").equalsIgnoreCase("")) {
                patientMaster.setTbDiagnosisDate(LocalDate.parse(request.getParameter("tbDiagnosisDate").trim()));
            }
            if (request.getParameter("tbTreatmentInitiationDate") != null && !request.getParameter("tbTreatmentInitiationDate").equalsIgnoreCase("")) {
                patientMaster.setTbTreatmentInitiationDate(LocalDate.parse(request.getParameter("tbTreatmentInitiationDate").trim()));
            }
            if (request.getParameter("idNo") != null && !request.getParameter("idNo").equalsIgnoreCase("")) {
                patientMaster.setIdNo(request.getParameter("idNo").trim());
            }
            if (request.getParameter("gender") != null && !request.getParameter("gender").equalsIgnoreCase("")) {
                patientMaster.setGender(request.getParameter("gender").trim());
            }
            if (request.getParameter("pincode") != null && !request.getParameter("pincode").equalsIgnoreCase("")) {
                patientMaster.setPincode(Long.valueOf(request.getParameter("pincode").trim()));
            }
            if (request.getParameter("bloodGroup") != null && !request.getParameter("bloodGroup").equalsIgnoreCase("")) {
                patientMaster.setBloodGroup(request.getParameter("bloodGroup").trim());
            }


            PatientMaster mContent = patientMasterRepository.save(patientMaster);
            responseObject.addProperty("message", "Customer Master updated succussfully");
            responseObject.addProperty("responseStatus", HttpStatus.OK.value());
            responseObject.addProperty("responseObject", mContent.getId().toString());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            doctorLogger.error("updatePatientMaster-> failed to update CustomerMaster" + exceptionAsString);
            responseObject.addProperty("message", "Internal Server Error");
            responseObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            doctorLogger.error("updatePatientMaster-> failed to update CustomerMaster" + exceptionAsString);
            responseObject.addProperty("message", "Error");
        }
        return responseObject;
    }

    //
    public JsonObject removePatientMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject jsonObject = new JsonObject();
        try {
            PatientMaster patientMaster = patientMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("id")), users.getOutlet().getId(), true);
            if (patientMaster != null) {
                patientMaster.setStatus(false);
                patientMasterRepository.save(patientMaster);
                jsonObject.addProperty("message", "Customer Master deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "Error in Customer Master deletion");
                jsonObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            doctorLogger.error("removeCustomerMaster-> failed to delete Customer" + exceptionAsString);
        }
        return jsonObject;
    }


}
