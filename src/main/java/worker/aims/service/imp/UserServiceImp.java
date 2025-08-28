package worker.aims.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import worker.aims.DTO.Permissions;
import worker.aims.entity.Factory;
import worker.aims.entity.PlatformAdmin;
import worker.aims.entity.User;
import worker.aims.entity.UserWhitelist;
import worker.aims.mapper.FactoryMapper;
import worker.aims.mapper.PlatformAdminMapper;
import worker.aims.mapper.UserMapper;
import worker.aims.mapper.UserWhitelistMapper;
import worker.aims.service.ex.*;
import worker.aims.service.itf.UserService;
import worker.aims.util.JWT;
import worker.aims.util.PasswordUtil;
import worker.aims.util.PermissionService;

import java.io.NotActiveException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class UserServiceImp implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserWhitelistMapper userWhitelistMapper;

    @Autowired
    private JWT jwt;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private PlatformAdminMapper platformAdminMapper;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private FactoryMapper factoryMapper;

    @Override
    public Map<String, Object> register(String username, String password, String email, String phone, String fullName, String tempToken) {
        if (tempToken == null) {
            throw new AccessDeniedException("缺少手机验证令牌!");
        }
        JWT.TempTokenData token = jwt.verifyAndUseTempToken(tempToken, "PHONE_VERIFICATION");
        if (!Objects.equals(token.phoneNumber, phone)) {
            throw new AccessDeniedException("手机号与验证令牌不匹配!");
        }
        String factoryId = token.factoryId;
        UserWhitelist userWhitelist = userWhitelistMapper.getUserWhitelistByFactoryIdAndPhoneNumber(factoryId, phone);
        if (userWhitelist == null || !userWhitelist.getStatus().equals("PENDING")) {
            throw new AccessDeniedException("白名单状态已改变，请重新验证");
        }
        User result = userMapper.getUserByUsername(username);
        if (result != null) {
            throw new NameDuplicateException("用户名已存在！");
        }
        User result_1 = userMapper.getUserByEmail(email);
        if (result_1 != null) {
            throw new NameDuplicateException("邮箱已存在");
        }
        String passwordHash = passwordUtil.hashPassword(password);
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordHash);
        user.setLastLogin(LocalDateTime.now());
        user.setDepartment(null);
        user.setPosition(null);
        user.setPhone(phone);
        user.setEmail(email);
        user.setIsActive(false);
        user.setPermissions("[]");
        user.setRole("unactivated");
        user.setFullName(fullName);
        user.setCreatedAt(LocalDateTime.now());
        user.setFactoryId(null);
        int rows = userMapper.insertUser(user);
        if (rows != 1) {
            throw new InsertException("注册过程出现未知异常！");
        }
        int rows_1 = userWhitelistMapper.updateUserWhitelistStatus(userWhitelist.getId(), "REGISTERED");
        if (rows_1 != 1) {
            throw new UpdateException("更新白名单状态时出现未知异常！");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getUid());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("isActive", user.getIsActive());
        return map;
    }

    @Override
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();
        // 智能用户识别：优先级1 - 平台用户 (PlatformAdmin)
        PlatformAdmin admin = platformAdminMapper.getAdminByUsername(username);
        // 智能用户识别：优先级2 - 工厂用户 (User)
        User user = userMapper.getUserByUsername(username);
        if (admin != null) {
            boolean isPasswordValid = passwordUtil.verifyPassword(password, admin.getPassword());
            if (!isPasswordValid) {
                throw new AccessDeniedException("用户名或密码错误!");
            }
            JWT.Tokens tokens = jwt.generatePlatformAuthTokens(admin);
            Permissions permissions = permissionService.generatePlatformUserPermissions(admin.getRole());
            Map<String, Object> map = new HashMap<>();
            map.put("id", admin.getPid());
            map.put("username", admin.getUsername());
            map.put("email", admin.getEmail());
            map.put("fullName", admin.getFullName());
            Map<String, String> roleMap = new HashMap<>();
            roleMap.put("name", permissionService.mapRoleCodeToRoleName(admin.getRole()));
            roleMap.put("displayName", permissionService.getRoleDisplayName(admin.getRole()));
            map.put("role", roleMap);
            map.put("permissions", permissions);
            result.put("admin", map);
            result.put("token", tokens);
            result.put("userType", "platform");
        }
        else if (user != null) {
            String factoryId = user.getFactoryId();
            Factory factory = factoryMapper.getFactoryByFid(factoryId);
            if (factory == null) {
                throw new NotFoundException("所属工厂不存在或已停用！");
            }
            if (!factory.getIsActive()) {
                throw new AccessDeniedException("用户所属工厂未激活！");
            }
            boolean isPasswordValid = passwordUtil.verifyPassword(password, user.getPassword());
            if (!isPasswordValid) {
                throw new AccessDeniedException("用户名或密码错误!");
            }
            if (!user.getIsActive()) {
                throw new AccessDeniedException("账户尚未激活，请联系管理员");
            }
            int rows = userMapper.updateLastLogin(user.getUid(), LocalDateTime.now());
            if (rows != 1) {
                throw new UpdateException("更新用户最后登陆时间时出现未知异常！");
            }
            JWT.Tokens tokens = jwt.generateAuthTokens(user);
            Permissions permissions = permissionService.generateUserPermissions(user.getRole(), user.getDepartment(), user.getPosition());
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getUid());
            userMap.put("username", user.getUsername());
            userMap.put("email", user.getEmail());
            userMap.put("fullName", user.getFullName());
            Map<String, String> roleMap = new HashMap<>();
            roleMap.put("name", permissionService.mapRoleCodeToRoleName(user.getRole()));
            roleMap.put("displayName", permissionService.getRoleDisplayName(user.getRole()));
            userMap.put("permissions", permissions);
            userMap.put("role", roleMap);
            userMap.put("department", user.getDepartment());
            userMap.put("position", user.getPosition());
            result.put("user", userMap);
            Map<String, Object> factoryMap = new HashMap<>();
            factoryMap.put("id", user.getFactoryId());
            factoryMap.put("name", factory.getName());
            factoryMap.put("industry", factory.getIndustry());
            result.put("factory", factoryMap);
            result.put("token", tokens);
            result.put("userType", "factory");
        }
        else {
            throw new NotFoundException("不存在该用户");
        }
        return result;
    }

    @Override
    public User getUserByUid(Integer uid) {
        User result = userMapper.getUserByUid(uid);
        if (result == null) {
            throw new NotFoundException("用户不存在！");
        }
        result.setPassword(null);
        return result;
    }

    @Override
    public User getUserByUidAndFactoryId(Integer uid, String factoryId) {
        User result = userMapper.getUserByUidAndFactoryId(uid, factoryId);
        if (result == null) {
            throw new NotFoundException("用户不存在！");
        }
        if (result.getIsActive()) {
            throw new AccessDeniedException("用户已激活！");
        }
        result.setPassword(null);
        return result;
    }


    @Override
    public User getUserByUsername(String username) {
        return userMapper.getUserByUsername(username);
    }

    @Override
    public User  getUserByEmail(String email) {
        return userMapper.getUserByEmail(email);
    }

    @Override
    public void changeUsername(Integer uid, String username) {
        User result = userMapper.getUserByUid(uid);
        if (result == null) {
            throw new NotFoundException("用户不存在！");
        }
        User olduser = userMapper.getUserByUsername(username);
        if (olduser != null) {
            throw new NameDuplicateException("用户名已被使用！");
        }
        int rows = userMapper.updateUsernameByUid(uid, username);
        if (rows != 1) {
            throw new UpdateException("更新用户名时出现未知异常！");
        }
    }

    @Override
    public void changePassword(Integer uid, String opassword, String npassword) {
        User result = userMapper.getUserByUid(uid);
        if (result == null) {
            throw new NotFoundException("用户不存在！");
        }
        boolean isPasswordValid = passwordUtil.verifyPassword(opassword, npassword);
        if (!isPasswordValid) {
            throw new AccessDeniedException("原密码错误，不能修改密码！");
        }
        String npasswordHash = passwordUtil.hashPassword(npassword);
        int rows = userMapper.updatePasswordByUid(uid, npasswordHash);
        if (rows != 1) {
            throw new UpdateException("更新用户密码时出现未知异常！");
        }
        jwt.revokeUserTokens(result.getUid(), result.getFactoryId());
    }

    @Override
    public void resetPassword(Integer uid, String password) {
        User result = userMapper.getUserByUid(uid);
        if (result == null) {
            throw new NotFoundException("用户不存在！");
        }
        int rows = userMapper.updatePasswordByUid(uid, password);
        if (rows != 1) {
            throw new UpdateException("更新用户密码时出现未知异常！");
        }
    }

    @Override
    public void changeEmail(Integer uid, String email) {
        User result = userMapper.getUserByUid(uid);
        if (result == null) {
            throw new NotFoundException("用户不存在！");
        }
        String oldEmail = result.getEmail();
        if (oldEmail.equals(email)) {
            throw new NameDuplicateException("邮箱号重复！");
        }
        int rows = userMapper.updateEmailByUid(uid, email);
        if (rows != 1) {
            throw new UpdateException("更新邮箱时出现未知异常！");
        }
    }

    @Override
    public void updatePhone(Integer uid, String phone) {
        User result = userMapper.getUserByUid(uid);
        if (result == null) {
            throw new NotFoundException("用户不存在！");
        }
        int rows = userMapper.updatePhoneByUid(uid, phone);
        if (rows != 1) {
            throw new UpdateException("更新电话时出现未知异常！");
        }
    }

    @Override
    public void updateFullName(Integer uid, String fullName) {
        User result = userMapper.getUserByUid(uid);
        if (result == null) {
            throw new NotFoundException("用户不存在！");
        }
        int rows = userMapper.updateFullNameByUid(uid, fullName);
        if (rows != 1) {
            throw new UpdateException("更新全名时出现未知异常！");
        }
    }

    @Override
    public void updateDepartment(Integer uid, String department) {
        User result = userMapper.getUserByUid(uid);
        if (result == null) {
            throw new NotFoundException("用户不存在！");
        }
        int rows = userMapper.updateDepartmentByUid(uid, department);
        if (rows != 1) {
            throw new UpdateException("更新部门名时出现未知异常！");
        }
    }

    @Override
    public void updatePosition(Integer uid, String position) {
        User result = userMapper.getUserByUid(uid);
        if (result == null) {
            throw new NotFoundException("用户不存在！");
        }
        int rows = userMapper.updatePositionByUid(uid, position);
        if (rows != 1) {
            throw new UpdateException("更新职位名时出现未知异常！");
        }
    }

    @Override
    public void updateFactoryId(Integer uid, String factoryId) {
        User result = userMapper.getUserByUid(uid);
        if (result == null) {
            throw new NotFoundException("用户不存在！");
        }
        int rows = userMapper.updateFactoryIdByUid(uid, factoryId);
        if (rows != 1) {
            throw new UpdateException("更新用户工厂ID时出现未知异常！");
        }
    }

    @Override
    public void updatePermissions(Integer uid, String permissions) {
        User result = userMapper.getUserByUid(uid);
        if (result == null) {
            throw new NotFoundException("用户不存在！");
        }
        int rows = userMapper.updatePermissionsByUid(uid, permissions);
        if (rows != 1) {
            throw new UpdateException("更新权限时出现未知异常！");
        }
    }

    @Override
    public void updateRole(Integer uid, String role) {
        User result = userMapper.getUserByUid(uid);
        if (result == null) {
            throw new NotFoundException("用户不存在！");
        }
        int rows = userMapper.updateRoleByUid(uid, role);
        if (rows != 1) {
            throw new UpdateException("更新角色名时出现未知异常！");
        }
    }

    @Override
    public void updateIsActive(Integer uid, boolean isActive) {
        User result = userMapper.getUserByUid(uid);
        if (result == null) {
            throw new NotFoundException("用户不存在！");
        }
        int rows = userMapper.updateUserActiveStatus(uid, isActive);
        if (rows != 1) {
            throw new UpdateException("更新激活状态时出现未知异常！");
        }
    }

    @Override
    public void deleteUserByUid(Integer uid) {
        User result = userMapper.getUserByUid(uid);
        if (result == null) {
            throw new NotFoundException("该用户不存在！");
        }
        int rows = userMapper.deleteUserByUid(uid);
        if (rows != 1) {
            throw new DeleteException("删除用户时出现未知异常！");
        }
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.getAllUsers();
    }

    @Override
    public List<User> getPendingUsers(Boolean isActive, String factoryId) {
        return userMapper.getPendingUsers(isActive, factoryId);
    }

    @Override
    public Map<String, Object> getUserStats(String factoryId) {
        Map<String, Object> result = new HashMap<>();
        long activeUsers = userMapper.countActiveUsers(factoryId);
        long pendingUsers = userMapper.countPendingUsers(factoryId);
        long totalUsers = activeUsers + pendingUsers;
        // 最近7天登录
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        long recentLoginUsers = userMapper.countRecentLoginUsers(factoryId, since);
        // 按角色
        Map<String, Long> roleStats = new HashMap<>();
        List<Map<String, Object>> roleList = userMapper.groupByRole(factoryId);
        for (Map<String, Object> row : roleList) {
            String roleCode = (String) row.get("roleCode");
            Long count = ((Number) row.get("count")).longValue();
            roleStats.put(roleCode, count);
        }
        // 按部门
        Map<String, Long> departmentStats = new HashMap<>();
        List<Map<String, Object>> deptList = userMapper.groupByDepartment(factoryId);
        for (Map<String, Object> row : deptList) {
            String dept = (String) row.get("department");
            Long count = ((Number) row.get("count")).longValue();
            departmentStats.put(dept, count);
        }
        result.put("activeUsers", activeUsers);
        result.put("pendingUsers", pendingUsers);
        result.put("totalUsers", totalUsers);
        result.put("recentLoginUsers", recentLoginUsers);
        result.put("roleStats", roleStats);
        result.put("departmentStats", departmentStats);
        return result;
    }

}