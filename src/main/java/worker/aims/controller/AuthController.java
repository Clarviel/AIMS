package worker.aims.controller;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import worker.aims.config.JwtConfig;
import worker.aims.entity.Factory;
import worker.aims.entity.PlatformAdmin;
import worker.aims.entity.User;
import worker.aims.service.ex.*;
import worker.aims.service.itf.FactoryService;
import worker.aims.service.itf.PlatformAdminService;
import worker.aims.service.itf.UserService;
import worker.aims.service.itf.UserWhitelistService;
import worker.aims.entity.UserWhitelist;
import worker.aims.util.JWT;
import worker.aims.util.JsonResult;
import worker.aims.util.PermissionService;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@Slf4j
@RestController
@RequestMapping("/auth")
@Api(tags = "认证接口")
@Controller
public class AuthController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private FactoryService factoryService;

    @Autowired
    private UserWhitelistService userWhitelistService;

    @Autowired
    private JWT jwt;

    @Autowired
    private PlatformAdminService platformAdminService;

    @ApiOperation(value = "手机号验证")
    @ApiImplicitParams({
            @ApiImplicitParam(name ="phoneNumber",value = "手机号",dataTypeClass = String.class,required = true),
            @ApiImplicitParam(name ="factoryId",value = "工厂id",dataTypeClass = String.class,required = true),
    })
    @PostMapping("/verify_phone")
    public JsonResult<String> verifyPhone(HttpServletRequest request){
        String phoneNumber = request.getParameter("phoneNumber");
        String factoryId = request.getParameter("factoryId");
        Factory factory = factoryService.getFactoryByFid(factoryId);
        if (factory.getIsActive() == false)
            throw new NotFoundException("此工厂未激活！");
        try {
            UserWhitelist result = userWhitelistService.getUserWhitelistByFactoryIdAndPhoneNumber(factoryId, phoneNumber);
            Map<String, Object> data = new HashMap<>();
            data.put("whitelistId", result.getId());
            String tempToken = jwt.generateTempToken("PHONE_VERIFICATION", factoryId, phoneNumber, data, 30);
            return new JsonResult<>(OK, "验证通过，请在30分钟内完成注册", tempToken);
        } catch (AccessDeniedException | NotFoundException e) {
            System.out.println(e.getMessage());
        }
        return new JsonResult<>(FAIL, "验证失败", null);
    }

    @ApiOperation(value = "用户注册")
    @ApiImplicitParams({
            @ApiImplicitParam(name ="username",value = "用户名称",dataTypeClass = String.class,required = true),
            @ApiImplicitParam(name ="password",value = "用户密码",dataTypeClass = String.class,required = true),
            @ApiImplicitParam(name ="email",value = "用户邮箱",dataTypeClass = String.class,required = true),
            @ApiImplicitParam(name ="phone",value = "用户手机号",dataTypeClass = String.class,required = true),
            @ApiImplicitParam(name ="fullName",value = "用户全名",dataTypeClass = String.class,required = true),
            @ApiImplicitParam(name ="tempToken",value = "手机临时令牌",dataTypeClass = String.class,required = true),
    })
    @PostMapping("/register")
    public JsonResult<Map<String, Object>> register(HttpServletRequest request){
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String fullName = request.getParameter("fullName");
        String tempToken = request.getParameter("tempToken");
        Map<String, Object> userMap = userService.register(username, password, email, phone, fullName, tempToken);
        System.out.println("register success");
        return new JsonResult<>(OK, "注册成功，请等待管理员激活", userMap);
    }

    @ApiOperation(value = "用户登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name ="username",value = "用户名称",dataTypeClass = String.class,required = true),
            @ApiImplicitParam(name ="password",value = "用户密码",dataTypeClass = String.class,required = true),
    })
    @PostMapping("/login")
    public JsonResult<Map<String, Object>> login(HttpServletRequest request){
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        Map<String, Object> result = userService.login(username, password);
        if (result.get("userType").equals("platform")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> adminMap = (Map<String, Object>) result.get("admin");
            Integer adminId = (Integer) adminMap.get("id");
            StpUtil.login(adminId);
            System.out.println(username + " admin" + " login success");
        }
        else if (result.get("userType").equals("factory")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> userMap = (Map<String, Object>) result.get("user");
            Integer userId = (Integer) userMap.get("id");
            StpUtil.login(userId);
            System.out.println(username + " user" + " login success");
        }
        return new JsonResult<>(OK, result);
    }

    @ApiOperation(value = "用户登出")
    @GetMapping("/user_logout")
    public JsonResult<Void> userLogout(){
        User user = userService.getUserByUid(StpUtil.getLoginIdAsInt());
        StpUtil.logout(StpUtil.getLoginIdAsInt());
        System.out.println(user.getUsername()+" logout success");
        return new JsonResult<>(OK, "登出成功", null);
    }

    @ApiOperation(value = "管理员登出")
    @GetMapping("/admin_logout")
    public JsonResult<Void> adminLogout(){
        PlatformAdmin platformAdmin = platformAdminService.getAdminByPid(StpUtil.getLoginIdAsInt());
        StpUtil.logout(StpUtil.getLoginIdAsInt());
        System.out.println(platformAdmin.getUsername()+" logout success");
        return new JsonResult<>(OK, "登出成功", null);
    }

    @ApiOperation(value = "获取当前用户信息")
    @GetMapping("/get_current_user")
    public JsonResult<Map<String, Object>> getCurrentUser(HttpServletRequest request){
        Integer userId = StpUtil.getLoginIdAsInt();
        User user = userService.getUserByUid(userId);
        String factoryId = user.getFactoryId();
        Factory factory = factoryService.getFactoryByFid(factoryId);
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getUid());
        userMap.put("username", user.getUsername());
        userMap.put("email", user.getEmail());
        userMap.put("fullName", user.getFullName());
        userMap.put("role", user.getRole());
        userMap.put("department", user.getDepartment());
        userMap.put("position", user.getPosition());
        userMap.put("permissions", user.getPermissions());
        userMap.put("lastLogin", user.getLastLogin());
        result.put("user", userMap);
        Map<String, Object> factoryMap = new HashMap<>();
        factoryMap.put("id", user.getFactoryId());
        factoryMap.put("name", factory.getName());
        factoryMap.put("industry", factory.getIndustry());
        result.put("factory", factoryMap);
        return new JsonResult<>(OK, "获取用户信息成功", result);
    }

    @ApiOperation(value = "刷新令牌")
    @GetMapping("/refresh_token")
    @ApiImplicitParam(name ="refreshToken",value = "刷新令牌",dataTypeClass = String.class,required = true)
    public JsonResult<JWT.Tokens> refreshToken(HttpServletRequest request){
        String refreshToken = request.getParameter("refreshToken");
        JWT.Tokens newTokens = jwt.refreshAuthToken(refreshToken);
        return new JsonResult<>(OK, "令牌刷新成功", newTokens);
    }

    @ApiOperation(value = "更改用户密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name ="opassword",value = "旧用户密码",dataTypeClass = String.class,required = true),
            @ApiImplicitParam(name ="npassword",value = "新用户密码",dataTypeClass = String.class,required = true)
    })
    @PostMapping("/change_password")
    public JsonResult<Void> changePassword(HttpServletRequest request){
        String opassword = request.getParameter("opassword");
        String npassword = request.getParameter("npassword");
        userService.changePassword(StpUtil.getLoginIdAsInt(), opassword, npassword);
        return new JsonResult<>(OK, "密码修改成功，请重新登录", null);
    }

}
