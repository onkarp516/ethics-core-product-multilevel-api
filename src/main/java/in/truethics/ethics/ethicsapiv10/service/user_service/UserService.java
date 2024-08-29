package in.truethics.ethics.ethicsapiv10.service.user_service;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.CommonAccessPermissions;
import in.truethics.ethics.ethicsapiv10.common.GenerateFiscalYear;
import in.truethics.ethics.ethicsapiv10.common.PasswordEncoders;
import in.truethics.ethics.ethicsapiv10.model.access_permissions.SystemAccessPermissions;
import in.truethics.ethics.ethicsapiv10.model.access_permissions.SystemActionMapping;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductHsn;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import in.truethics.ethics.ethicsapiv10.model.user.UserRole;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.access_permission_repository.SystemAccessPermissionsRepository;
import in.truethics.ethics.ethicsapiv10.repository.access_permission_repository.SystemActionMappingRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.BranchRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.OutletRepository;
import in.truethics.ethics.ethicsapiv10.repository.user_repository.UserRoleRepository;
import in.truethics.ethics.ethicsapiv10.repository.user_repository.UsersRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Transactional
//@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private UsersRepository userRepository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;
    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private OutletRepository outletRepository;
    @Autowired
    private PasswordEncoders bcryptEncoder;
    @Autowired
    private SystemActionMappingRepository mappingRepository;
    @Autowired
    private SystemAccessPermissionsRepository accessPermissionsRepository;
    @Autowired
    private CommonAccessPermissions accessPermissions;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private SystemAccessPermissionsRepository systemAccessPermissionsRepository;
    private static final Logger UserLogger = LogManager.getLogger(UserService.class);

    public ResponseMessage createSuperAdmin(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = new Users();
        Map<String, String[]> paramMap = request.getParameterMap();
        try {
            if (paramMap.containsKey("mobileNumber"))
                users.setMobileNumber(Long.valueOf(request.getParameter("mobileNumber")));


            users.setFullName(request.getParameter("fullName"));
            if (paramMap.containsKey("email")) users.setEmail(request.getParameter("email"));
            else users.setEmail("");
            users.setGender(request.getParameter("gender"));
            users.setUsercode(request.getParameter("usercode"));
            users.setUsername(request.getParameter("usercode"));
            users.setUserRole(request.getParameter("userRole"));
            if (paramMap.containsKey("address")) users.setAddress(request.getParameter("address"));
            else users.setAddress("");
            users.setStatus(true);
            users.setPassword(bcryptEncoder.passwordEncoderNew().encode(request.getParameter("password")));
            users.setPlain_password(request.getParameter("password"));
            users.setIsSuperAdmin(true);
            users.setPermissions("all");

            userRepository.save(users);
            responseObject.setMessage("Super admin created successfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            UserLogger.error("Exception in createSuperAdmin:" + e.getMessage());
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseObject.setMessage("Internal Server Error");
            e.printStackTrace();
            System.out.println("Exception:" + e.getMessage());
        }
        return responseObject;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users users = userRepository.findByUsername(username);
        if (users == null) {
            log.error("User not found In the database");
            throw new UsernameNotFoundException("UserController not found with username: " + username);

        } else {
            log.info("User found In the database: {}", username);
            return new org.springframework.security.core.userdetails.User(users.getUsercode(), users.getPassword(), new ArrayList<>());
        }
    }

    public Users findUser(String usercode) throws UsernameNotFoundException {
        Users users = userRepository.findByUsername(usercode);
        if (users != null) {

        } else {
            throw new UsernameNotFoundException("User not found with username: " + usercode);
        }
        return users;

    }

    public Users findUserWithPassword(String usercode, String password) throws UsernameNotFoundException {
        Users users = userRepository.findByUsername(usercode);
        if (bcryptEncoder.passwordEncoderNew().matches(password, users.getPassword())) {
            return users;
        }
        return null;
    }

    public JsonObject addUser(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        //  ResponseMessage responseObject = new ResponseMessage();
        JsonObject responseObject = new JsonObject();
        Users users = new Users();
        Users user = null;
        try {
            if (paramMap.containsKey("mobileNumber")) {
                users.setMobileNumber(Long.valueOf(request.getParameter("mobileNumber")));

            }
            if (paramMap.containsKey("fullName")) {
                users.setFullName(request.getParameter("fullName"));
            }
            if (paramMap.containsKey("email")) {
                users.setEmail(request.getParameter("email"));
            } else {
                users.setEmail("");
            }

            if (paramMap.containsKey("address")) {
                users.setAddress(request.getParameter("address"));
            } else {
                users.setAddress("");
            }
            if (paramMap.containsKey("gender")) {
                users.setGender(request.getParameter("gender"));
            } else {
                users.setGender("");
            }


            if (!request.getParameter("userRole").equals("BADMIN") && !request.getParameter("userRole").equals("CADMIN")) {
                UserRole userRole = userRoleRepository.findByIdAndStatus(Long.parseLong(request.getParameter("roleId")), true);
                if (userRole != null) {
                    users.setRoleMaster(userRole);
                }
            }
            users.setUsercode(request.getParameter("usercode"));
            users.setUsername(request.getParameter("usercode"));
            users.setUserRole(request.getParameter("userRole"));
            users.setStatus(true);
            users.setIsSuperAdmin(false);
            //  users.setPermissions(request.getParameter("permissions"));
            if (request.getHeader("Authorization") != null) {
                user = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
                users.setCreatedBy(user.getId());
            }
            users.setPassword(bcryptEncoder.passwordEncoderNew().encode(request.getParameter("password")));
            users.setPlain_password(request.getParameter("password"));
            if (paramMap.containsKey("companyId")) {
                Outlet mOutlet = outletRepository.findByIdAndStatus(Long.parseLong(request.getParameter("companyId")), true);
                users.setOutlet(mOutlet);
            }
            if (paramMap.containsKey("branchId")) {
                Branch mBranch = branchRepository.findByIdAndStatus(Long.parseLong(request.getParameter("branchId")), true);
                users.setBranch(mBranch);
            }
            if (paramMap.containsKey("permissions")) users.setPermissions(request.getParameter("permissions"));
            Users newUser = userRepository.save(users);
            try {
                if (request.getParameter("userRole").equalsIgnoreCase("USER")) {
                    /* Create Permissions */
                    String jsonStr = request.getParameter("user_permissions");
                    if (jsonStr != null) {
                        JsonArray userPermissions = new JsonParser().parse(jsonStr).getAsJsonArray();
                        for (int i = 0; i < userPermissions.size(); i++) {
                            JsonObject mObject = userPermissions.get(i).getAsJsonObject();
                            SystemAccessPermissions mPermissions = new SystemAccessPermissions();
                            mPermissions.setUsers(newUser);
                            SystemActionMapping mappings = mappingRepository.findByIdAndStatus(mObject.get("mapping_id").getAsLong(), true);
                            mPermissions.setSystemActionMapping(mappings);
                            mPermissions.setStatus(true);
                            mPermissions.setCreatedBy(user.getId());
                            JsonArray mActionsArray = mObject.get("actions").getAsJsonArray();
                            String actionsId = "";
                            for (int j = 0; j < mActionsArray.size(); j++) {
                                actionsId = actionsId + mActionsArray.get(j).getAsString();
                                if (j < mActionsArray.size() - 1) {
                                    actionsId = actionsId + ",";
                                }
                            }
                            mPermissions.setUserActionsId(actionsId);
                            accessPermissionsRepository.save(mPermissions);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                UserLogger.error("Exception in addUser: " + e.getMessage());
                System.out.println(e.getMessage());
            }
            responseObject.addProperty("message", "User added succussfully");
            responseObject.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (DataIntegrityViolationException e1) {
            e1.printStackTrace();
            UserLogger.error("Exception in addUser: " + e1.getMessage());
            System.out.println("DataIntegrityViolationException " + e1.getMessage());
            responseObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            responseObject.addProperty("message", "Usercode already used");
            return responseObject;
        } catch (Exception e) {
            e.printStackTrace();
            UserLogger.error("Exception in addUser: " + e.getMessage());
            responseObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseObject.addProperty("message", "Internal Server Error");
            e.printStackTrace();
            System.out.println("Exception:" + e.getMessage());
        }
        return responseObject;
    }

    //List of Users Service
    public JsonObject getUsers() {
        JsonObject res = new JsonObject();
        List<Users> list = userRepository.findAll();
        res = getUserData(list);
        return res;
    }

    //Get user By Id
    public JsonObject getUsersById(String id) {
        Users user = userRepository.findByIdAndStatus(Long.parseLong(id), true);
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        JsonArray user_permission = new JsonArray();
        if (user != null) {
            response.addProperty("id", user.getId());
            if (user.getOutlet() != null) {
                response.addProperty("companyName", user.getOutlet().getCompanyName());
                response.addProperty("companyId", user.getOutlet().getId());
            }
            if (user.getBranch() != null) {
                response.addProperty("branchName", user.getBranch().getBranchName());
                response.addProperty("branchId", user.getBranch().getId());
            }
            response.addProperty("roleId", user.getRoleMaster() != null ? user.getRoleMaster().getId().toString() : "");
            response.addProperty("userRole", user.getUserRole());
            response.addProperty("password", user.getPlain_password());
            response.addProperty("fullName", user.getFullName() != null ? user.getFullName().toString() : "");
            response.addProperty("mobileNumber", user.getMobileNumber() != null ? user.getMobileNumber().toString() : "");
            response.addProperty("email", user.getEmail() != null ? user.getEmail().toString() : "");
            response.addProperty("gender", user.getGender() != null ? user.getGender().toString() : "");
            response.addProperty("usercode", user.getUsercode());
            /***** get User Permissions from access_permissions_tbl ****/
            List<SystemAccessPermissions> accessPermissions = new ArrayList<>();
            accessPermissions = systemAccessPermissionsRepository.findByUsersIdAndStatus(user.getId(), true);
            for (SystemAccessPermissions mPermissions : accessPermissions) {
                JsonObject mObject = new JsonObject();
                mObject.addProperty("mapping_id", mPermissions.getSystemActionMapping().getId());
                JsonArray actions = new JsonArray();
                String actionsId = mPermissions.getUserActionsId();
                String[] actionsList = actionsId.split(",");
                Arrays.sort(actionsList);
                for (String actionId : actionsList) {
                    actions.add(actionId);
                }
                mObject.add("actions", actions);
                user_permission.add(mObject);
            }
            response.add("permissions", user_permission);
            result.addProperty("message", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());

            result.add("responseObject", response);
        } else {
            result.addProperty("message", "error");
            result.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return result;
    }

    public Object updateUser(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        ResponseMessage responseObject = new ResponseMessage();
        Users users = userRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        Users loginUser = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        if (users != null) {
            if (paramMap.containsKey("mobileNumber")) {
                users.setMobileNumber(Long.valueOf(request.getParameter("mobileNumber")));
            }
            if (paramMap.containsKey("fullName")) {
                users.setFullName(request.getParameter("fullName"));
            } else {
                users.setEmail("");
            }
            if (paramMap.containsKey("email")) {
                users.setEmail(request.getParameter("email"));
            } else {
                users.setEmail("");
            }
            if (paramMap.containsKey("address")) {
                users.setAddress(request.getParameter("address"));
            } else {
                users.setAddress("");
            }
            if (paramMap.containsKey("gender")) {
                users.setGender(request.getParameter("gender"));
            } else {
                users.setGender("");
            }
            users.setUsercode(request.getParameter("usercode"));
            users.setUsername(request.getParameter("usercode"));
            if (!request.getParameter("userRole").equals("BADMIN") && !request.getParameter("userRole").equals("CADMIN")) {
                UserRole userRole = userRoleRepository.findByIdAndStatus(Long.parseLong(request.getParameter("roleId")), true);
                if (userRole != null) {
                    users.setRoleMaster(userRole);
                }
            }
            users.setUserRole(request.getParameter("userRole"));
            users.setStatus(true);
            users.setIsSuperAdmin(false);
            users.setPermissions(request.getParameter("permissions"));
            users.setPassword(bcryptEncoder.passwordEncoderNew().encode(request.getParameter("password")));
            users.setPlain_password(request.getParameter("password"));
            if (paramMap.containsKey("companyId")) {
                Outlet mOutlet = outletRepository.findByIdAndStatus(Long.parseLong(request.getParameter("companyId")), true);
                users.setOutlet(mOutlet);
            }
            if (paramMap.containsKey("branchId")) {
                Branch mBranch = branchRepository.findByIdAndStatus(Long.parseLong(request.getParameter("branchId")), true);
                users.setBranch(mBranch);
            }
            String del_user_perm = request.getParameter("del_user_permissions");
            if (del_user_perm != null) {
                JsonArray deleteUserPermission = new JsonParser().parse(del_user_perm).getAsJsonArray();
                for (int j = 0; j < deleteUserPermission.size(); j++) {
                    Long moduleId = deleteUserPermission.get(j).getAsLong();
                    //  SystemActionMapping delMapping = mappingRepository.findByIdAndStatus(moduleId, true);
                    SystemAccessPermissions delPermissions = accessPermissionsRepository.findByUsersIdAndStatusAndSystemActionMappingId(users.getId(), true, moduleId);
                    delPermissions.setStatus(false);
                    try {
                        accessPermissionsRepository.save(delPermissions);
                    } catch (Exception e) {
                    }
                }
            }
            /* Update Permissions */
            String jsonStr = request.getParameter("user_permissions");
            if (jsonStr != null) {
                JsonArray userPermissions = new JsonParser().parse(jsonStr).getAsJsonArray();
                for (int i = 0; i < userPermissions.size(); i++) {
                    JsonObject mObject = userPermissions.get(i).getAsJsonObject();
                    SystemActionMapping mappings = mappingRepository.findByIdAndStatus(mObject.get("mapping_id").getAsLong(), true);
                    SystemAccessPermissions mPermissions = accessPermissionsRepository.findByUsersIdAndStatusAndSystemActionMappingId(users.getId(), true, mappings.getId());
                    System.out.println("User Id:" + users.getId());
                    if (mPermissions != null) {
                        JsonArray mActionsArray = mObject.get("actions").getAsJsonArray();
                        String actionsId = "";
                        for (int j = 0; j < mActionsArray.size(); j++) {
                            actionsId = actionsId + mActionsArray.get(j).getAsString();
                            if (j < mActionsArray.size() - 1) {
                                actionsId = actionsId + ",";
                            }
                        }
                        mPermissions.setUserActionsId(actionsId);
                        mPermissions.setUsers(users);
                        accessPermissionsRepository.save(mPermissions);
                    } else {
                        /* Create Permissions */
                        SystemAccessPermissions mPermissions1 = new SystemAccessPermissions();
                        mPermissions1.setSystemActionMapping(mappings);
                        mPermissions1.setStatus(true);
                        // mPermissions1.setCreatedBy(users.getId());
                        mPermissions1.setCreatedBy(loginUser.getId());
                        JsonArray mActionsArray = mObject.get("actions").getAsJsonArray();
                        String actionsId = "";
                        for (int j = 0; j < mActionsArray.size(); j++) {
                            actionsId = actionsId + mActionsArray.get(j).getAsString();
                            if (j < mActionsArray.size() - 1) {
                                actionsId = actionsId + ",";
                            }
                        }
                        mPermissions1.setUserActionsId(actionsId);
                        mPermissions1.setUsers(users);

                        try {
                            accessPermissionsRepository.save(mPermissions1);
                        } catch (Exception e) {
                            System.out.println("Exception:" + e.getMessage());
                        }
                    }
                }
            }
            userRepository.save(users);
            responseObject.setMessage("User updated successfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            /*else {
                responseObject.setResponseStatus(HttpStatus.FORBIDDEN.value());
                responseObject.setMessage("Not found");
            }*/
        } else {
            responseObject.setResponseStatus(HttpStatus.FORBIDDEN.value());
            responseObject.setMessage("Not found");
        }
        return responseObject;
    }

    /* Get all Branch Users of  Institute Admin */
    /*public JsonObject getBranchUsers(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        Long branchId = users.getBranch().getId();
        List<Users> list = new ArrayList<>();
        JsonObject res = new JsonObject();
        list = userRepository.findByBranchIdAndStatus(branchId, true);
        res = getUserData(list);
        return res;
    }*/

    /* Get all outlet Users of Branch Admin */
    /*public JsonObject getOutletUsers(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        Long outletId = users.getOutlet().getId();
        JsonObject res = new JsonObject();
        JsonArray result = new JsonArray();
        List<Users> list = userRepository.findByOutletIdAndStatus(outletId, true);
        res = getUserData(list);
        return res;
    }*/

    /* Get All users Rolewise of Super Admin */
    public JsonObject getUsersOfCompany(HttpServletRequest httpServletRequest, String userRole, String currentUserRole) {
        Users users = jwtRequestFilter.getUserDataFromToken(httpServletRequest.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        List<Users> list = new ArrayList<>();
        if (currentUserRole.equalsIgnoreCase("SADMIN")) list = userRepository.findByUserRoleAndStatus(userRole, true);
        else {
            list = userRepository.findByUserRoleAndCreatedByAndStatus(userRole, users.getId(), true);
        }
        JsonObject res = getUserData(list);
        return res;
    }


    public JsonObject getUserData(List<Users> list) {
        JsonObject res = new JsonObject();
        JsonArray result = new JsonArray();
        if (list.size() > 0) {
            for (Users mUser : list) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mUser.getId());
                if (mUser.getOutlet() != null) response.addProperty("companyName", mUser.getOutlet().getCompanyName());
                if (mUser.getBranch() != null)
                    response.addProperty("branchName", mUser.getBranch() != null ? mUser.getBranch().getBranchName() : "");
                response.addProperty("username", mUser.getUsername());
                response.addProperty("fullName", mUser.getFullName());
                response.addProperty("mobileNumber", mUser.getMobileNumber() != null ? mUser.getMobileNumber().toString() : "");
                response.addProperty("email", mUser.getEmail());
                response.addProperty("address", mUser.getAddress());
                response.addProperty("gender", mUser.getGender());
                response.addProperty("usercode", mUser.getUsercode());
                response.addProperty("isSwitch", mUser.getStatus() == true ? 1 : 0);
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
        return res;
    }

    public JsonObject getMesgForTokenExpired(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", "Hello Token");
        jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
        return jsonObject;
    }

    public JsonObject getUsersOfCompanyNew(HttpServletRequest httpServletRequest) {
        Users users = jwtRequestFilter.getUserDataFromToken(httpServletRequest.getHeader("Authorization").substring(7));
        JsonObject res = new JsonObject();
        JsonArray result = new JsonArray();
        List<Users> list = new ArrayList<>();
        list = userRepository.findByUserRoleIgnoreCaseAndStatusAndOutletId("USER", true, users.getOutlet().getId());
        if (list.size() > 0) {
            res = getUserData(list);
        } else {
            res.addProperty("message", "empty list");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", result);
        }
        return res;
    }

    public JsonObject setUserPermissions(HttpServletRequest request) {
        Long userId = Long.parseLong(request.getParameter("user_id"));
        Users users = userRepository.findByIdAndStatus(userId, true);
        users.setPermissions(request.getParameter("permissions"));
        userRepository.save(users);
        JsonObject res = new JsonObject();
        res.addProperty("message", "success");
        res.addProperty("responseStatus", HttpStatus.OK.value());
        return res;
    }

    public JsonObject getCompanyAdmins(HttpServletRequest httpServletRequest) {
//        Users users = jwtRequestFilter.getUserDataFromToken(httpServletRequest.getHeader("Authorization").substring(7));
        JsonObject res = new JsonObject();
        JsonArray result = new JsonArray();
        List<Users> list = new ArrayList<>();
        // Long companyId = Long.parseLong(httpServletRequest.getParameter("companyId"));
        list = userRepository.findByUserRoleIgnoreCaseAndStatus("CADMIN", true);
        if (list.size() > 0) {
            res = getUserData(list);
        } else {
            res.addProperty("message", "empty list");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", result);
        }
        return res;
    }

    public JsonObject getBranchAdmins(HttpServletRequest httpServletRequest) {
//        Users users = jwtRequestFilter.getUserDataFromToken(httpServletRequest.getHeader("Authorization").substring(7));
        JsonObject res = new JsonObject();
        JsonArray result = new JsonArray();
        List<Users> list = new ArrayList<>();
        Users users = jwtRequestFilter.getUserDataFromToken(httpServletRequest.getHeader("Authorization").substring(7));

        // Long companyId = Long.parseLong(httpServletRequest.getParameter("companyId"));
        list = userRepository.findByUserRoleIgnoreCaseAndStatusAndOutletId("BADMIN", true, users.getOutlet().getId());
        if (list.size() > 0) {
            res = getUserData(list);
        } else {
            res.addProperty("message", "empty list");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", result);
        }
        return res;
    }


    public JsonObject getUsersOfBranchNew(HttpServletRequest httpServletRequest) {
        Users users = jwtRequestFilter.getUserDataFromToken(httpServletRequest.getHeader("Authorization").substring(7));
        JsonObject res = new JsonObject();
        JsonArray result = new JsonArray();
        List<Users> list = new ArrayList<>();
//        list = userRepository.findByUserRoleIgnoreCaseAndStatusAndOutletIdAndBranchId("USER", true, users.getOutlet().getId(), users.getBranch().getId());
        list = userRepository.findByUserRoleIgnoreCaseAndStatusAndOutletId("USER", true, users.getOutlet().getId());

        if (list.size() > 0) {
            res = getUserData(list);
        } else {
            res.addProperty("message", "empty list");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", result);
        }
        return res;
    }

    public JsonObject getUserPermissions(HttpServletRequest request) {
        /* getting User Permissions */
        JsonObject finalResult = new JsonObject();
        JsonArray userPermissions = new JsonArray();
        JsonArray permissions = new JsonArray();
        JsonArray masterModules = new JsonArray();
        Long userId = Long.parseLong(request.getParameter("user_id"));
        List<SystemAccessPermissions> list = systemAccessPermissionsRepository.findByUsersIdAndStatus(userId, true);
        /*
         * Print elements using the forEach
         */
        for (SystemAccessPermissions mapping : list) {
            JsonObject mObject = new JsonObject();
            mObject.addProperty("id", mapping.getId());
            mObject.addProperty("action_mapping_id", mapping.getSystemActionMapping().getId());
            mObject.addProperty("action_mapping_name", mapping.getSystemActionMapping().getSystemMasterModules().getName());
            mObject.addProperty("action_mapping_slug", mapping.getSystemActionMapping().getSystemMasterModules().getSlug());
            String[] actions = mapping.getUserActionsId().split(",");
            permissions = accessPermissions.getActions(actions);
            masterModules = accessPermissions.getParentMasters(mapping.getSystemActionMapping().getSystemMasterModules().getParentModuleId());
            mObject.add("actions", permissions);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", mapping.getSystemActionMapping().getSystemMasterModules().getId());
            jsonObject.addProperty("name", mapping.getSystemActionMapping().getSystemMasterModules().getName());
            jsonObject.addProperty("slug", mapping.getSystemActionMapping().getSystemMasterModules().getSlug());
            masterModules.add(jsonObject);
            mObject.add("parent_modules", masterModules);
            userPermissions.add(mObject);
        }
        finalResult.add("userActions", userPermissions);
        return finalResult;
    }

    public Object checkInvoiceDateIsBetweenFY(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        try {
            LocalDate invoiceDate = LocalDate.parse(request.getParameter("invoiceDate"));
            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(invoiceDate);
            if (fiscalYear == null) {
                response.addProperty("response", false);
                response.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
            } else {
                response.addProperty("response", true);
                response.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            response.addProperty("response", false);
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public JsonObject validateUser(HttpServletRequest request) {
        //  Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject jsonObject = new JsonObject();
        String userCode = request.getParameter("userCode");
        Users user = userRepository.findByUsercodeIgnoreCaseAndStatus(userCode, true);
        if (user != null) {
            jsonObject.addProperty("message", "Username is already availabe,please try another");
            jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            jsonObject.addProperty("message", "New User");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return jsonObject;
    }

    public JsonObject userDelete(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Users user1 = userRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        try {

            if (user1 != null) {
                user1.setStatus(false);
                userRepository.save(user1);
                jsonObject.addProperty("message", "User deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());

            } else {
                jsonObject.addProperty("message", "Not allowed to delete default user");
                jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
        }
        return jsonObject;
    }


    public JsonObject forgetPasswordOwner(Map<String, String> request) {
        JsonObject responseMessage = new JsonObject();
        Users users = userRepository.findByUsernameAndUserRole(request.get("username"), "SADMIN");
        if (users != null) {
            try {
                responseMessage.addProperty("mobileNumber", users.getMobileNumber() != null ? users.getMobileNumber().toString() : "");
                responseMessage.addProperty("otp", "1234");
                responseMessage.addProperty("message", "Password changed successfully");
                responseMessage.addProperty("responseStatus", HttpStatus.OK.value());
            } catch (Exception e) {
                responseMessage.addProperty("message", "error");
                responseMessage.addProperty("responseStatus", HttpStatus.BAD_REQUEST.value());
            }
        } else {
            responseMessage.addProperty("message", "error");
            responseMessage.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
        }
        return responseMessage;
    }

    public Object changePassword(String username, String password) {
        Users users = null;
        ResponseMessage responseMessage = new ResponseMessage();
        users = userRepository.findByUsernameAndStatus(username, true);
        if (users != null) {
            users.setPlain_password(password);
            String encPassword = bcryptEncoder.passwordEncoderNew().encode(password);
            users.setPassword(encPassword);
            Users users1 = userRepository.save(users);
            if (users1 != null) {
                responseMessage.setMessage("Password changed successfully.");
                responseMessage.setResponseStatus(HttpStatus.OK.value());
            } else {
                responseMessage.setMessage("Failed to change password.");
                responseMessage.setResponseStatus(HttpStatus.BAD_REQUEST.value());
            }
        } else {
            responseMessage.setMessage("Failed to match current password.");
            responseMessage.setResponseStatus(HttpStatus.NOT_FOUND.value());
        }
        return responseMessage;
    }

    public JsonObject disableUser(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users user1 = userRepository.findById(Long.parseLong(request.getParameter("id"))).get();
        Boolean status = Boolean.parseBoolean(request.getParameter("isEnable"));//true for enable / false for disable
        try {

            if (user1 != null) {
                user1.setStatus(status);
                userRepository.save(user1);
                if (status) {
                    jsonObject.addProperty("message", "User Enable successfully");
//                    jsonObject.addProperty("status", true);
                } else {
                    jsonObject.addProperty("message", "User Disable successfully");
//                    jsonObject.addProperty("status", false);
                }
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "Not allowed to delete default user");
                jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
        }
        return jsonObject;
    }

    public JsonObject getCompanyallAdmins(HttpServletRequest httpServletRequest) {
        JsonObject res = new JsonObject();
        JsonArray result = new JsonArray();
        List<Users> list = new ArrayList<>();
        // Long companyId = Long.parseLong(httpServletRequest.getParameter("companyId"));
        list = userRepository.findByUserRoleIgnoreCase("CADMIN");
        if (list.size() > 0) {
            res = getUserData(list);
        } else {
            res.addProperty("message", "empty list");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", result);
        }
        return res;
    }

    public JsonObject validateUserUpdate(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Users user = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        Long branchId = null;
        Long hsnId = Long.parseLong(request.getParameter("id"));
        if (paramMap.containsKey("branchId")) branchId = Long.parseLong(request.getParameter("branchId"));
        if (branchId != null) {
            user = userRepository.findByOutletIdAndBranchIdAndUsernameAndStatus(users.getOutlet().getId(), branchId, request.getParameter("userCode"), true);
        } else {
            user = userRepository.findByOutletIdAndUsernameAndStatusAndBranchIsNull(users.getOutlet().getId(), request.getParameter("userCode"), true);
        }
        JsonObject result = new JsonObject();
        if (user != null && hsnId != user.getId()) {
            result.addProperty("message", "duplicate User");
            result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            result.addProperty("message", "New user");
            result.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return result;
    }

    public JsonObject validateCadminUpdate(HttpServletRequest request) {

        Users user = null;
        Map<String, String[]> paramMap = request.getParameterMap();

        Long adminId = Long.parseLong(request.getParameter("id"));

        user = userRepository.findByUsernameAndStatus(request.getParameter("userCode"), true);

        JsonObject result = new JsonObject();
        if (user != null && adminId != user.getId()) {
            result.addProperty("message", "Duplicate User");
            result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            result.addProperty("message", "New user");
            result.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return result;
    }

    public Object resetPasswordWithMobile(String password, String currentPassword, HttpServletRequest httpServletRequest) {
        Users tokenUser = jwtRequestFilter.getUserDataFromToken(httpServletRequest.getHeader("Authorization").substring(7));
        Users users = null;
        ResponseMessage responseMessage = new ResponseMessage();
        users = userRepository.findByUsernameAndStatus(tokenUser.getUsername(), true);
        if (users != null) {
            if (bcryptEncoder.passwordEncoderNew().matches(currentPassword, users.getPassword())) {
                users.setPlain_password(password);
                String encPassword = bcryptEncoder.passwordEncoderNew().encode(password);
                users.setPassword(encPassword);
                userRepository.save(users);
                responseMessage.setMessage("Password changed successfully.");
                responseMessage.setResponseStatus(HttpStatus.OK.value());
            } else {
                responseMessage.setMessage("Current password is incorrect.");
                responseMessage.setResponseStatus(HttpStatus.BAD_REQUEST.value());
            }
        } else {
            responseMessage.setMessage("Failed to match current password.");
            responseMessage.setResponseStatus(HttpStatus.NOT_FOUND.value());
        }
        return responseMessage;
    }
}
