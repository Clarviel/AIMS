package worker.aims.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import worker.aims.entity.User;
import worker.aims.service.ex.AccessDeniedException;
import worker.aims.service.ex.NameDuplicateException;
import worker.aims.service.ex.NotFoundException;
import worker.aims.service.itf.UserService;
import worker.aims.service.itf.UserSessionService;
import worker.aims.util.JsonResult;
import worker.aims.util.PasswordUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@CrossOrigin(origins = "*")
@Slf4j
@RestController
@RequestMapping("/users")
@Api(tags = "用户接口")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private UserSessionService userSessionService;

    @ApiOperation(value = "管理员激活账户")
    @ApiImplicitParams({
            @ApiImplicitParam(name ="department",value = "用户部门",dataTypeClass = String.class,required = true),
            @ApiImplicitParam(name ="position",value = "用户职位",dataTypeClass = String.class,required = true),
//            @ApiImplicitParam(name ="permission",value = "用户权限",dataTypeClass = String.class,required = true),
            @ApiImplicitParam(name ="role",value = "用户角色",dataTypeClass = String.class,required = true),
            @ApiImplicitParam(name ="uid",value = "用户id",dataTypeClass = Integer.class,required = true),
    })
    @PostMapping("/active_user")
    public JsonResult<User> activeUser(HttpServletRequest request){
        String department = request.getParameter("department");
        String position = request.getParameter("position");
        String permission = request.getParameter("permission");
        String role = request.getParameter("role");
        int uid = Integer.parseInt(request.getParameter("uid"));
        int currentUid = StpUtil.getLoginIdAsInt();
        User currentUser = userService.getUserByUid(currentUid);
        String factoryId = currentUser.getFactoryId();
        User user = userService.getUserByUidAndFactoryId(uid,factoryId);
        if (Objects.equals(role, "permission_admin") && !Objects.equals(currentUser.getRole(), "factory_super_admin")) {
            throw new AccessDeniedException("只有超级管理员可以创建权限管理员");
        }
        userService.updateDepartment(uid, department);
        userService.updatePosition(uid, position);
//        userService.updatePermissions(uid, permission);
        userService.updateRole(uid, role);
        userService.updateIsActive(uid, true);
        User result = userService.getUserByUid(uid);
        return new JsonResult<>(OK,"用户激活成功", result);
    }

    @ApiOperation(value = "获取待激活用户列表")
    @ApiImplicitParam(name ="factoryId",value = "用户工厂id",dataTypeClass = String.class,required = true)
    @PostMapping("/get_pending_users")
    public JsonResult<Map<String, Object>>getPendingUsers(HttpServletRequest request) {
        String factoryId = request.getParameter("factoryId");
        List<User> users = userService.getPendingUsers(false, factoryId);
        int count = users.size();
        Map<String, Object> result = new HashMap<>();
        result.put("items", users);
        result.put("count", count);
        return new JsonResult<>(OK,"获取待激活用户成功", result);
    }

    @ApiOperation(value = "更新用户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name ="uid",value = "用户id",dataTypeClass = Integer.class,required = true),
            @ApiImplicitParam(name ="fullName",value = "用户全名",dataTypeClass = String.class,required = true),
            @ApiImplicitParam(name ="email",value = "用户邮箱",dataTypeClass = String.class,required = true),
            @ApiImplicitParam(name ="phone",value = "用户手机号",dataTypeClass = String.class,required = true),
            @ApiImplicitParam(name ="department",value = "用户部门",dataTypeClass = String.class,required = true),
            @ApiImplicitParam(name ="position",value = "用户职位",dataTypeClass = String.class,required = true),
            @ApiImplicitParam(name ="permissions",value = "用户权限",dataTypeClass = String.class,required = true),
    })
    @PostMapping("/update_user")
    public JsonResult<Void> updateUser(HttpServletRequest request){
        int uid = Integer.parseInt(request.getParameter("uid"));
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String department = request.getParameter("department");
        String position = request.getParameter("position");
        String permissions = request.getParameter("permissions");
        int currentUid = StpUtil.getLoginIdAsInt();
        User currentUser = userService.getUserByUid(currentUid);
        String factoryId = currentUser.getFactoryId();
        User user = userService.getUserByUidAndFactoryId(uid, factoryId);
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }
        User user_1 = userService.getUserByEmail(email);
        if (user_1 != null) {
            throw new NameDuplicateException("邮箱已被其他用户使用");
        }
        if (fullName != null) {
            userService.updateFullName(uid, fullName);
        }
        if (phone != null) {
            userService.updatePhone(uid, phone);
        }
        if (department != null) {
            userService.updateDepartment(uid, department);
        }
        if (position != null) {
            userService.updatePosition(uid, position);
        }
        if (permissions != null) {
            userService.updateRole(uid, permissions);
        }
        if (email != null) {
            userService.changeEmail(uid, email);
        }
        return new JsonResult<>(OK,"用户信息更新成功", null);
    }

    // 只做了所有用户查询，细化一下，比如获取同一factory，department下的用户
    @ApiOperation(value = "获取所有用户信息")
    @GetMapping("/get_all_users")
    public JsonResult<List<User>> getAllUsers(){
        List<User> users = userService.getAllUsers();
        return new JsonResult<>(OK,users);
    }

    @ApiOperation(value = "停用/启用用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name ="uid",value = "用户ID",dataTypeClass = Integer.class,required = true),
            @ApiImplicitParam(name ="isActive",value = "用户状态",dataTypeClass = Boolean.class,required = true)
    })
    @PostMapping("/toggle_user_status")
    public JsonResult<Void> toggleUserStatus(HttpServletRequest request){
        int uid = Integer.parseInt(request.getParameter("uid"));
        boolean isActive = Boolean.parseBoolean(request.getParameter("isActive"));
        int currentUid = StpUtil.getLoginIdAsInt();
        User currentUser = userService.getUserByUid(currentUid);
        String factoryId = currentUser.getFactoryId();
        User user = userService.getUserByUidAndFactoryId(uid, factoryId);
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }
        if (Objects.equals(user.getRole(), "factory_super_admin")) {
            throw new AccessDeniedException("超级管理员不能被停用");
        }
        if (uid == currentUid) {
            throw new AccessDeniedException("不能停用自己");
        }
        userService.updateIsActive(uid, isActive);
        if (!isActive) {
            userSessionService.updateUserSessionIsRevokedByUserIdAndFactoryId(uid, factoryId, true);
        }
        return new JsonResult<>(OK, user.getUsername() + (isActive ? "启用" : "停用") + "成功",null);
    }

    @ApiOperation(value = "按照ID删除用户")
    @PostMapping("/delete_user_by_uid")
    @ApiImplicitParam(name ="uid",value = "用户id",dataTypeClass = Integer.class,required = true)
    public JsonResult<Void> deleteUserByUid(HttpServletRequest request){
        int uid = Integer.parseInt(request.getParameter("uid"));
        User user = userService.getUserByUid(uid);
        String factoryId = user.getFactoryId();
        userService.deleteUserByUid(uid);
        userSessionService.updateUserSessionIsRevokedByUserIdAndFactoryId(uid, factoryId, true);
        return new JsonResult<>(OK);
    }

    @ApiOperation(value = "重制用户密码")
    @PostMapping("/reset_user_password")
    @ApiImplicitParam(name ="uid",value = "用户id",dataTypeClass = Integer.class,required = true)
    public JsonResult<String> resetUserPassword(HttpServletRequest request){
        int uid = Integer.parseInt(request.getParameter("uid"));
        int currentUid = StpUtil.getLoginIdAsInt();
        User currentUser = userService.getUserByUid(currentUid);
        String factoryId = currentUser.getFactoryId();
        User user = userService.getUserByUidAndFactoryId(uid, factoryId);
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }
        String tempPassword = passwordUtil.generateRandomPassword(12);
        String passwordHash = passwordUtil.hashPassword(tempPassword);
        userService.resetPassword(uid, passwordHash);
        userSessionService.updateUserSessionIsRevokedByUserIdAndFactoryId(uid, factoryId, true);
        return new JsonResult<>(OK,"密码重置成功，请将临时密码告知用户",tempPassword);
    }

    @ApiOperation(value = "获取用户统计信息")
    @PostMapping("/get_all_user_status")
    @ApiImplicitParam(name ="factoryId",value = "工厂id",dataTypeClass = String.class,required = true)
    public JsonResult<Map<String, Object>> getAllUserStatus(HttpServletRequest request){
        String factoryId = request.getParameter("factoryId");
        Map<String, Object> stats = userService.getUserStats(factoryId);
        return new JsonResult<>(OK, "获取用户统计成功", stats);
    }


}
