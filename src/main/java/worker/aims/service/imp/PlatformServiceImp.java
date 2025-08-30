package worker.aims.service.imp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import worker.aims.entity.Factory;
import worker.aims.entity.User;
import worker.aims.entity.UserWhitelist;
import worker.aims.mapper.FactoryMapper;
import worker.aims.mapper.UserMapper;
import worker.aims.mapper.UserWhitelistMapper;
import worker.aims.service.ex.*;
import worker.aims.service.itf.PlatformService;
import worker.aims.util.FactoryIdGenerator;
import worker.aims.util.PasswordUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlatformServiceImp implements PlatformService {

    @Autowired
    private FactoryMapper factoryMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserWhitelistMapper userWhitelistMapper;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private FactoryIdGenerator factoryIdGenerator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> getPlatformOverview() {
        // 获取工厂统计数据
        long totalFactories = factoryMapper.countAllFactories();
        long activeFactories = factoryMapper.countActiveFactories();
        long pendingFactories = factoryMapper.countPendingFactories();

        // 获取用户统计数据
        long totalUsers = userMapper.countAllUsers();
        long activeUsers = userMapper.countAllActiveUsers();

        // 按角色统计用户数量
        List<Map<String, Object>> roleStats = userMapper.groupAllByRole();

        // 计算月增长率
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long recentUsers = userMapper.countUsersCreatedAfter(thirtyDaysAgo);
        long oldUsers = totalUsers - recentUsers;
        double monthlyGrowthRate = oldUsers > 0 ? ((double) recentUsers / oldUsers) * 100 : 0;

        // 收入数据（功能尚未实现）
        double totalRevenue = 0;

        // 数据使用量（功能尚未实现）
        double dataUsageTotalGb = 0;

        Map<String, Object> overviewData = new HashMap<>();
        overviewData.put("total_factories", totalFactories);
        overviewData.put("active_factories", activeFactories);
        overviewData.put("pending_factories", pendingFactories);
        overviewData.put("total_users", totalUsers);
        overviewData.put("active_users", activeUsers);
        overviewData.put("monthly_growth_rate", Math.round(monthlyGrowthRate * 10.0) / 10.0);
        overviewData.put("total_revenue", totalRevenue);
        overviewData.put("data_usage_total_gb", dataUsageTotalGb);

        // 角色分布
        Map<String, Long> roleDistribution = roleStats.stream()
                .collect(Collectors.toMap(
                        stat -> (String) stat.get("roleCode"),
                        stat -> (Long) stat.get("count")
                ));
        overviewData.put("role_distribution", roleDistribution);

        return overviewData;
    }

    @Override
    public Map<String, Object> getFactoryDetails(String id) {
        Factory factory = factoryMapper.getFactoryById(id);
        if (factory == null) {
            throw new NotFoundException("工厂不存在");
        }

        // 获取工厂用户
        List<User> users = userMapper.getUsersByFactoryId(id);
        
        // 按角色统计用户
        Map<String, Long> roleDistribution = users.stream()
                .collect(Collectors.groupingBy(User::getRole, Collectors.counting()));

        // 统计活跃用户（最近7天内登录）
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        long activeUsersRecent = users.stream()
                .filter(user -> user.getLastLogin() != null && user.getLastLogin().isAfter(sevenDaysAgo))
                .count();

        Map<String, Object> factoryDetails = new HashMap<>();
        factoryDetails.put("id", factory.getFid());
        factoryDetails.put("name", factory.getName());
        factoryDetails.put("industry", factory.getIndustry());
        factoryDetails.put("address", factory.getAddress());
        factoryDetails.put("contactName", factory.getContactName());
        factoryDetails.put("contactEmail", factory.getContactEmail());
        factoryDetails.put("contactPhone", factory.getContactPhone());
        factoryDetails.put("employeeCount", factory.getEmployeeCount());
        factoryDetails.put("subscriptionPlan", factory.getSubscriptionPlan());
        factoryDetails.put("isActive", factory.getIsActive());
        factoryDetails.put("createdAt", factory.getCreatedAt());
        factoryDetails.put("updatedAt", factory.getUpdatedAt());

        // 统计信息
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", users.size());
        statistics.put("activeUsers", users.stream().filter(User::getIsActive).count());
        statistics.put("recentActiveUsers", activeUsersRecent);
        statistics.put("roleDistribution", roleDistribution);
        factoryDetails.put("statistics", statistics);

        // 用户列表
        List<Map<String, Object>> userList = users.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getUid());
            userMap.put("username", user.getUsername());
            userMap.put("email", user.getEmail());
            userMap.put("fullName", user.getFullName());
            userMap.put("roleCode", user.getRole());
            userMap.put("department", user.getDepartment());
            userMap.put("isActive", user.getIsActive());
            userMap.put("lastLogin", user.getLastLogin());
            userMap.put("createdAt", user.getCreatedAt());
            return userMap;
        }).collect(Collectors.toList());
        factoryDetails.put("users", userList);

        return factoryDetails;
    }

    // 其他方法的实现将在后续添加...
    @Override
    public Map<String, Object> createFactory(String name, String industry, String contactEmail, String contactPhone, String address, String description) {
        // 检查工厂名称是否已存在
        Factory existingFactory = factoryMapper.getFactoryByName(name);
        if (existingFactory != null) {
            throw new NameDuplicateException("工厂名称已存在");
        }

        // 检查邮箱是否已存在
        if (contactEmail != null) {
            Factory existingEmail = factoryMapper.getFactoryByContactEmail(contactEmail);
            if (existingEmail != null) {
                throw new NameDuplicateException("联系邮箱已被使用");
            }
        }

        // 使用智能工厂ID生成系统
        Map<String, Object> factoryData = new HashMap<>();
        factoryData.put("name", name);
        factoryData.put("industry", industry);
        factoryData.put("address", address);
        factoryData.put("contactEmail", contactEmail);
        factoryData.put("contactPhone", contactPhone);

        Map<String, Object> generationResult = factoryIdGenerator.generateNewFactoryId(factoryData);

        // 创建工厂
        Factory factory = new Factory();
        factory.setFid((String) generationResult.get("factoryId"));
        factory.setName(name);
        factory.setIndustry(industry);
        factory.setContactEmail(contactEmail);
        factory.setContactPhone(contactPhone);
        factory.setAddress(address);
        factory.setDescription(description);
        factory.setIsActive(true);
        factory.setCreatedAt(LocalDateTime.now());
        factory.setUpdatedAt(LocalDateTime.now());
        
        // 新增字段
        factory.setIndustryCode((String) generationResult.get("industryCode"));
        factory.setRegionCode((String) generationResult.get("regionCode"));
        factory.setFactoryYear((Integer) generationResult.get("factoryYear"));
        factory.setSequenceNumber((Integer) generationResult.get("sequenceNumber"));
        factory.setLegacyId(factoryIdGenerator.generateFactoryId(name, industry, address)); // 保存老格式ID
        factory.setInferenceData((String) generationResult.get("reasoning"));
        factory.setConfidence(((Map<String, Double>) generationResult.get("confidence")).get("overall").floatValue());
        factory.setManuallyVerified(!(Boolean) generationResult.get("needsConfirmation"));

        int rows = factoryMapper.insertFactory(factory);
        if (rows != 1) {
            throw new InsertException("创建工厂失败");
        }

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> factoryInfo = new HashMap<>();
        factoryInfo.put("id", factory.getFid());
        factoryInfo.put("name", factory.getName());
        factoryInfo.put("industry", factory.getIndustry());
        factoryInfo.put("contactEmail", factory.getContactEmail());
        factoryInfo.put("contactPhone", factory.getContactPhone());
        factoryInfo.put("address", factory.getAddress());
        factoryInfo.put("description", factory.getDescription());
        factoryInfo.put("isActive", factory.getIsActive());
        factoryInfo.put("createdAt", factory.getCreatedAt());
        
        // 新增智能推断信息
        Map<String, Object> intelligentCoding = new HashMap<>();
        intelligentCoding.put("industryCode", factory.getIndustryCode());
        intelligentCoding.put("regionCode", factory.getRegionCode());
        intelligentCoding.put("factoryYear", factory.getFactoryYear());
        intelligentCoding.put("sequenceNumber", factory.getSequenceNumber());
        intelligentCoding.put("industryName", generationResult.get("industryName"));
        intelligentCoding.put("regionName", generationResult.get("regionName"));
        intelligentCoding.put("confidence", factory.getConfidence());
        intelligentCoding.put("needsConfirmation", generationResult.get("needsConfirmation"));
        intelligentCoding.put("reasoning", generationResult.get("reasoning"));
        intelligentCoding.put("legacyId", factory.getLegacyId());
        factoryInfo.put("intelligentCoding", intelligentCoding);
        
        result.put("factory", factoryInfo);
        return result;
    }

    @Override
    public Map<String, Object> getFactories(Integer page, Integer size, String keyword, String status) {
        // 构建查询条件
        Map<String, Object> params = new HashMap<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            params.put("keyword", keyword);
        }
        if (status != null && !status.trim().isEmpty()) {
            params.put("status", status);
        }

        // 获取总数
        long total = factoryMapper.countFactoriesByCondition(params);

        // 分页查询
        int offset = (page - 1) * size;
        params.put("offset", offset);
        params.put("size", size);
        List<Factory> factories = factoryMapper.getFactoriesByCondition(params);

        // 转换数据格式
        List<Map<String, Object>> formattedFactories = factories.stream().map(factory -> {
            Map<String, Object> factoryMap = new HashMap<>();
            factoryMap.put("id", factory.getFid());
            factoryMap.put("name", factory.getName());
            factoryMap.put("industry", factory.getIndustry() != null ? factory.getIndustry() : "未分类");
            factoryMap.put("status", factory.getIsActive() ? "active" : "suspended");
            factoryMap.put("subscription_plan", factory.getSubscriptionPlan() != null ? factory.getSubscriptionPlan() : "basic");
            factoryMap.put("employee_count", factory.getEmployeeCount() != null ? factory.getEmployeeCount() : 0);
            factoryMap.put("owner_user_id", "owner_" + factory.getFid());
            factoryMap.put("owner_name", factory.getContactName() != null ? factory.getContactName() : "未设置");
            factoryMap.put("owner_email", factory.getContactEmail() != null ? factory.getContactEmail() : "");
            factoryMap.put("owner_phone", factory.getContactPhone() != null ? factory.getContactPhone() : "");
            factoryMap.put("contact_address", factory.getAddress() != null ? factory.getAddress() : "");
            factoryMap.put("created_at", factory.getCreatedAt().toString());
            factoryMap.put("updated_at", factory.getUpdatedAt().toString());
            factoryMap.put("last_active_at", factory.getUpdatedAt().toString());
            factoryMap.put("monthly_revenue", factory.getIsActive() ? Math.floor(Math.random() * 50000) + 10000 : 0);
            factoryMap.put("data_usage_gb", Math.random() * 30 + 1);
            return factoryMap;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("factories", formattedFactories);
        
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("size", size);
        pagination.put("total", total);
        pagination.put("pages", (int) Math.ceil((double) total / size));
        result.put("pagination", pagination);

        return result;
    }

    // 继续实现其他方法...
    @Override
    public Map<String, Object> updateFactory(String id, String name, String industry, String contactEmail, String contactPhone, String address, String description) {
        // 实现更新工厂逻辑
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> toggleFactoryStatus(String id, String status, String reason) {
        // 实现切换工厂状态逻辑
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getFactoryStats() {
        // 实现获取工厂统计逻辑
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> exportFactoriesData(String format) {
        // 实现导出工厂数据逻辑
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> exportUsersData() {
        // 实现导出用户数据逻辑
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> exportOverviewData() {
        // 实现导出概览数据逻辑
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getOperationLogs(Integer page, Integer size, String startDate, String endDate, String action, String actorType, String factoryId, String result) {
        // 实现获取操作日志逻辑
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> exportOperationLogs(String startDate, String endDate, String action, String actorType, String factoryId, String result, Integer limit) {
        // 实现导出操作日志逻辑
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> createSuperAdmin(String factoryId, String username, String email, String fullName, String phone) {
        // 实现创建超级管理员逻辑
        return new HashMap<>();
    }

    @Override
    public Factory updateFactoryInfo(String id, String name, String industry, String address, String contactName, String contactEmail, String contactPhone, String subscriptionPlan) {
        // 实现更新工厂信息逻辑
        return new Factory();
    }

    @Override
    public Factory suspendFactory(String id, String reason) {
        // 实现暂停工厂逻辑
        return new Factory();
    }

    @Override
    public Factory activateFactory(String id) {
        // 实现激活工厂逻辑
        return new Factory();
    }

    @Override
    public void deleteFactory(String id, String password, String confirmText) {
        // 实现删除工厂逻辑
    }

    @Override
    public Map<String, Object> getFactoryEmployees(String factoryId, Integer page, Integer size, String keyword) {
        // 实现获取工厂员工逻辑
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> updateEmployeeStatus(String factoryId, Integer employeeId, String status) {
        // 实现更新员工状态逻辑
        return new HashMap<>();
    }

    @Override
    public void deleteEmployee(String factoryId, Integer employeeId) {
        // 实现删除员工逻辑
    }

    @Override
    public Map<String, Object> getPlatformWhitelists(Integer page, Integer size, String factoryId, String keyword, String status) {
        // 实现获取平台白名单逻辑
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> batchImportWhitelists(String factoryId, String whitelistsJson) {
        // 实现批量导入白名单逻辑
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> updateWhitelistStatus(Integer whitelistId, String status) {
        // 实现更新白名单状态逻辑
        return new HashMap<>();
    }

    @Override
    public void deletePlatformWhitelist(Integer whitelistId) {
        // 实现删除白名单逻辑
    }

    @Override
    public Map<String, Object> batchDeleteWhitelists(String idsJson) {
        // 实现批量删除白名单逻辑
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> cleanupExpiredWhitelists() {
        // 实现清理过期白名单逻辑
        return new HashMap<>();
    }
}
