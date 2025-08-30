package worker.aims.service.itf;

import worker.aims.entity.Factory;
import worker.aims.entity.User;

import java.util.List;
import java.util.Map;

public interface PlatformService {

    /**
     * 获取平台概览数据
     */
    Map<String, Object> getPlatformOverview();

    /**
     * 获取工厂详细信息
     */
    Map<String, Object> getFactoryDetails(String id);

    /**
     * 创建工厂
     */
    Map<String, Object> createFactory(String name, String industry, String contactEmail, String contactPhone, String address, String description);

    /**
     * 获取工厂列表
     */
    Map<String, Object> getFactories(Integer page, Integer size, String keyword, String status);

    /**
     * 更新工厂信息
     */
    Map<String, Object> updateFactory(String id, String name, String industry, String contactEmail, String contactPhone, String address, String description);

    /**
     * 启用/停用工厂
     */
    Map<String, Object> toggleFactoryStatus(String id, String status, String reason);

    /**
     * 获取工厂统计信息
     */
    Map<String, Object> getFactoryStats();

    /**
     * 导出工厂数据
     */
    Map<String, Object> exportFactoriesData(String format);

    /**
     * 导出用户统计数据
     */
    Map<String, Object> exportUsersData();

    /**
     * 导出平台概览数据
     */
    Map<String, Object> exportOverviewData();

    /**
     * 获取操作日志
     */
    Map<String, Object> getOperationLogs(Integer page, Integer size, String startDate, String endDate, String action, String actorType, String factoryId, String result);

    /**
     * 导出操作日志
     */
    Map<String, Object> exportOperationLogs(String startDate, String endDate, String action, String actorType, String factoryId, String result, Integer limit);

    /**
     * 为工厂创建超级管理员
     */
    Map<String, Object> createSuperAdmin(String factoryId, String username, String email, String fullName, String phone);

    /**
     * 更新工厂信息（简化版）
     */
    Factory updateFactoryInfo(String id, String name, String industry, String address, String contactName, String contactEmail, String contactPhone, String subscriptionPlan);

    /**
     * 暂停工厂
     */
    Factory suspendFactory(String id, String reason);

    /**
     * 激活工厂
     */
    Factory activateFactory(String id);

    /**
     * 删除工厂
     */
    void deleteFactory(String id, String password, String confirmText);

    /**
     * 获取指定工厂的员工列表
     */
    Map<String, Object> getFactoryEmployees(String factoryId, Integer page, Integer size, String keyword);

    /**
     * 更新员工状态
     */
    Map<String, Object> updateEmployeeStatus(String factoryId, Integer employeeId, String status);

    /**
     * 删除员工
     */
    void deleteEmployee(String factoryId, Integer employeeId);

    /**
     * 获取平台白名单列表
     */
    Map<String, Object> getPlatformWhitelists(Integer page, Integer size, String factoryId, String keyword, String status);

    /**
     * 批量导入白名单
     */
    Map<String, Object> batchImportWhitelists(String factoryId, String whitelistsJson);

    /**
     * 更新白名单状态
     */
    Map<String, Object> updateWhitelistStatus(Integer whitelistId, String status);

    /**
     * 删除白名单记录
     */
    void deletePlatformWhitelist(Integer whitelistId);

    /**
     * 批量删除白名单记录
     */
    Map<String, Object> batchDeleteWhitelists(String idsJson);

    /**
     * 清理过期白名单记录
     */
    Map<String, Object> cleanupExpiredWhitelists();
}

