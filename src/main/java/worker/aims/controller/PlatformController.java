package worker.aims.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import worker.aims.entity.Factory;
import worker.aims.entity.User;
import worker.aims.entity.UserWhitelist;
import worker.aims.service.itf.PlatformService;
import worker.aims.util.JsonResult;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@Slf4j
@RestController
@RequestMapping("/platform")
@Api(tags = "平台管理接口")
public class PlatformController extends BaseController {

    @Autowired
    private PlatformService platformService;

    @ApiOperation(value = "获取平台概览数据")
    @GetMapping("/overview")
    public JsonResult<Map<String, Object>> getPlatformOverview() {
        Map<String, Object> overviewData = platformService.getPlatformOverview();
        return new JsonResult<>(OK, "获取平台概览数据成功", overviewData);
    }

    @ApiOperation(value = "获取工厂详细信息")
    @ApiImplicitParam(name = "id", value = "工厂ID", dataTypeClass = String.class, required = true)
    @GetMapping("/factories/{id}")
    public JsonResult<Map<String, Object>> getFactoryDetails(@PathVariable String id) {
        Map<String, Object> factoryDetails = platformService.getFactoryDetails(id);
        return new JsonResult<>(OK, "获取工厂详情成功", factoryDetails);
    }

    @ApiOperation(value = "创建工厂")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "工厂名称", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "industry", value = "所属行业", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "contactEmail", value = "联系邮箱", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "contactPhone", value = "联系电话", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "address", value = "工厂地址", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "description", value = "工厂描述", dataTypeClass = String.class, required = false)
    })
    @PostMapping("/factories")
    public JsonResult<Map<String, Object>> createFactory(HttpServletRequest request) {
        String name = request.getParameter("name");
        String industry = request.getParameter("industry");
        String contactEmail = request.getParameter("contactEmail");
        String contactPhone = request.getParameter("contactPhone");
        String address = request.getParameter("address");
        String description = request.getParameter("description");

        Map<String, Object> result = platformService.createFactory(name, industry, contactEmail, contactPhone, address, description);
        return new JsonResult<>(OK, "工厂创建成功", result);
    }

    @ApiOperation(value = "获取工厂列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", dataTypeClass = Integer.class, required = false),
            @ApiImplicitParam(name = "size", value = "每页大小", dataTypeClass = Integer.class, required = false),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "status", value = "状态筛选", dataTypeClass = String.class, required = false)
    })
    @GetMapping("/factories")
    public JsonResult<Map<String, Object>> getFactories(HttpServletRequest request) {
        Integer page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;
        Integer size = request.getParameter("size") != null ? Integer.parseInt(request.getParameter("size")) : 10;
        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");

        Map<String, Object> result = platformService.getFactories(page, size, keyword, status);
        return new JsonResult<>(OK, "获取工厂列表成功", result);
    }

    @ApiOperation(value = "更新工厂信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "工厂名称", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "industry", value = "所属行业", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "contactEmail", value = "联系邮箱", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "contactPhone", value = "联系电话", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "address", value = "工厂地址", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "description", value = "工厂描述", dataTypeClass = String.class, required = false)
    })
    @PutMapping("/factories/{id}")
    public JsonResult<Map<String, Object>> updateFactory(@PathVariable String id, HttpServletRequest request) {
        String name = request.getParameter("name");
        String industry = request.getParameter("industry");
        String contactEmail = request.getParameter("contactEmail");
        String contactPhone = request.getParameter("contactPhone");
        String address = request.getParameter("address");
        String description = request.getParameter("description");

        Map<String, Object> result = platformService.updateFactory(id, name, industry, contactEmail, contactPhone, address, description);
        return new JsonResult<>(OK, "工厂信息更新成功", result);
    }

    @ApiOperation(value = "启用/停用工厂")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "状态", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "reason", value = "原因", dataTypeClass = String.class, required = false)
    })
    @PutMapping("/factories/{id}/status")
    public JsonResult<Map<String, Object>> toggleFactoryStatus(@PathVariable String id, HttpServletRequest request) {
        String status = request.getParameter("status");
        String reason = request.getParameter("reason");

        Map<String, Object> result = platformService.toggleFactoryStatus(id, status, reason);
        return new JsonResult<>(OK, "工厂状态更新成功", result);
    }

    @ApiOperation(value = "获取工厂统计信息")
    @GetMapping("/factories/stats")
    public JsonResult<Map<String, Object>> getFactoryStats() {
        Map<String, Object> stats = platformService.getFactoryStats();
        return new JsonResult<>(OK, "获取统计信息成功", stats);
    }

    @ApiOperation(value = "导出工厂数据")
    @ApiImplicitParam(name = "format", value = "导出格式", dataTypeClass = String.class, required = false)
    @GetMapping("/factories/export")
    public JsonResult<Map<String, Object>> exportFactoriesData(HttpServletRequest request) {
        String format = request.getParameter("format");
        Map<String, Object> result = platformService.exportFactoriesData(format);
        return new JsonResult<>(OK, "工厂数据导出成功", result);
    }

    @ApiOperation(value = "导出用户统计数据")
    @GetMapping("/users/export")
    public JsonResult<Map<String, Object>> exportUsersData() {
        Map<String, Object> result = platformService.exportUsersData();
        return new JsonResult<>(OK, "用户统计数据导出成功", result);
    }

    @ApiOperation(value = "导出平台概览数据")
    @GetMapping("/overview/export")
    public JsonResult<Map<String, Object>> exportOverviewData() {
        Map<String, Object> result = platformService.exportOverviewData();
        return new JsonResult<>(OK, "平台概览数据导出成功", result);
    }

    @ApiOperation(value = "获取操作日志")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", dataTypeClass = Integer.class, required = false),
            @ApiImplicitParam(name = "size", value = "每页大小", dataTypeClass = Integer.class, required = false),
            @ApiImplicitParam(name = "startDate", value = "开始日期", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "endDate", value = "结束日期", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "action", value = "操作类型", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "actorType", value = "操作者类型", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "factoryId", value = "工厂ID", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "result", value = "操作结果", dataTypeClass = String.class, required = false)
    })
    @GetMapping("/logs")
    public JsonResult<Map<String, Object>> getOperationLogs(HttpServletRequest request) {
        Integer page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;
        Integer size = request.getParameter("size") != null ? Integer.parseInt(request.getParameter("size")) : 20;
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String action = request.getParameter("action");
        String actorType = request.getParameter("actorType");
        String factoryId = request.getParameter("factoryId");
        String result = request.getParameter("result");

        Map<String, Object> logs = platformService.getOperationLogs(page, size, startDate, endDate, action, actorType, factoryId, result);
        return new JsonResult<>(OK, "获取操作日志成功", logs);
    }

    @ApiOperation(value = "导出操作日志")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "开始日期", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "endDate", value = "结束日期", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "action", value = "操作类型", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "actorType", value = "操作者类型", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "factoryId", value = "工厂ID", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "result", value = "操作结果", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "limit", value = "导出数量限制", dataTypeClass = Integer.class, required = false)
    })
    @GetMapping("/logs/export")
    public JsonResult<Map<String, Object>> exportOperationLogs(HttpServletRequest request) {
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String action = request.getParameter("action");
        String actorType = request.getParameter("actorType");
        String factoryId = request.getParameter("factoryId");
        String result = request.getParameter("result");
        Integer limit = request.getParameter("limit") != null ? Integer.parseInt(request.getParameter("limit")) : 10000;

        Map<String, Object> resultData = platformService.exportOperationLogs(startDate, endDate, action, actorType, factoryId, result, limit);
        return new JsonResult<>(OK, "操作日志导出成功", resultData);
    }

    @ApiOperation(value = "为工厂创建超级管理员")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "email", value = "邮箱", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "fullName", value = "全名", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "phone", value = "手机号", dataTypeClass = String.class, required = true)
    })
    @PostMapping("/factories/{id}/super-admin")
    public JsonResult<Map<String, Object>> createSuperAdmin(@PathVariable String id, HttpServletRequest request) {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");

        Map<String, Object> result = platformService.createSuperAdmin(id, username, email, fullName, phone);
        return new JsonResult<>(OK, "超级管理员创建成功", result);
    }

    @ApiOperation(value = "更新工厂信息（简化版）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "工厂名称", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "industry", value = "所属行业", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "address", value = "工厂地址", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "contactName", value = "联系人姓名", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "contactEmail", value = "联系邮箱", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "contactPhone", value = "联系电话", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "subscriptionPlan", value = "订阅套餐", dataTypeClass = String.class, required = false)
    })
    @PutMapping("/factories/{id}/info")
    public JsonResult<Factory> updateFactoryInfo(@PathVariable String id, HttpServletRequest request) {
        String name = request.getParameter("name");
        String industry = request.getParameter("industry");
        String address = request.getParameter("address");
        String contactName = request.getParameter("contactName");
        String contactEmail = request.getParameter("contactEmail");
        String contactPhone = request.getParameter("contactPhone");
        String subscriptionPlan = request.getParameter("subscriptionPlan");

        Factory result = platformService.updateFactoryInfo(id, name, industry, address, contactName, contactEmail, contactPhone, subscriptionPlan);
        return new JsonResult<>(OK, "工厂信息更新成功", result);
    }

    @ApiOperation(value = "暂停工厂")
    @ApiImplicitParam(name = "reason", value = "暂停原因", dataTypeClass = String.class, required = false)
    @PutMapping("/factories/{id}/suspend")
    public JsonResult<Factory> suspendFactory(@PathVariable String id, HttpServletRequest request) {
        String reason = request.getParameter("reason");
        Factory result = platformService.suspendFactory(id, reason);
        return new JsonResult<>(OK, "工厂已暂停，所有员工登录已禁用", result);
    }

    @ApiOperation(value = "激活工厂")
    @PutMapping("/factories/{id}/activate")
    public JsonResult<Factory> activateFactory(@PathVariable String id) {
        Factory result = platformService.activateFactory(id);
        return new JsonResult<>(OK, "工厂已激活，员工登录已恢复", result);
    }

    @ApiOperation(value = "删除工厂")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "平台管理员密码", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "confirmText", value = "确认文字", dataTypeClass = String.class, required = true)
    })
    @DeleteMapping("/factories/{id}")
    public JsonResult<Void> deleteFactory(@PathVariable String id, HttpServletRequest request) {
        String password = request.getParameter("password");
        String confirmText = request.getParameter("confirmText");
        platformService.deleteFactory(id, password, confirmText);
        return new JsonResult<>(OK, "工厂删除成功", null);
    }

    @ApiOperation(value = "获取指定工厂的员工列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", dataTypeClass = Integer.class, required = false),
            @ApiImplicitParam(name = "size", value = "每页大小", dataTypeClass = Integer.class, required = false),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词", dataTypeClass = String.class, required = false)
    })
    @GetMapping("/factories/{factoryId}/employees")
    public JsonResult<Map<String, Object>> getFactoryEmployees(@PathVariable String factoryId, HttpServletRequest request) {
        Integer page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;
        Integer size = request.getParameter("size") != null ? Integer.parseInt(request.getParameter("size")) : 10;
        String keyword = request.getParameter("keyword");

        Map<String, Object> result = platformService.getFactoryEmployees(factoryId, page, size, keyword);
        return new JsonResult<>(OK, "获取员工列表成功", result);
    }

    @ApiOperation(value = "更新员工状态")
    @ApiImplicitParam(name = "status", value = "员工状态", dataTypeClass = String.class, required = true)
    @PutMapping("/factories/{factoryId}/employees/{employeeId}/status")
    public JsonResult<Map<String, Object>> updateEmployeeStatus(@PathVariable String factoryId, @PathVariable Integer employeeId, HttpServletRequest request) {
        String status = request.getParameter("status");
        Map<String, Object> result = platformService.updateEmployeeStatus(factoryId, employeeId, status);
        return new JsonResult<>(OK, "员工状态更新成功", result);
    }

    @ApiOperation(value = "删除员工")
    @DeleteMapping("/factories/{factoryId}/employees/{employeeId}")
    public JsonResult<Void> deleteEmployee(@PathVariable String factoryId, @PathVariable Integer employeeId) {
        platformService.deleteEmployee(factoryId, employeeId);
        return new JsonResult<>(OK, "员工删除成功", null);
    }

    @ApiOperation(value = "获取平台白名单列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", dataTypeClass = Integer.class, required = false),
            @ApiImplicitParam(name = "size", value = "每页大小", dataTypeClass = Integer.class, required = false),
            @ApiImplicitParam(name = "factoryId", value = "工厂ID", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "status", value = "状态筛选", dataTypeClass = String.class, required = false)
    })
    @GetMapping("/whitelists")
    public JsonResult<Map<String, Object>> getPlatformWhitelists(HttpServletRequest request) {
        Integer page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;
        Integer size = request.getParameter("size") != null ? Integer.parseInt(request.getParameter("size")) : 20;
        String factoryId = request.getParameter("factoryId");
        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");

        Map<String, Object> result = platformService.getPlatformWhitelists(page, size, factoryId, keyword, status);
        return new JsonResult<>(OK, "获取白名单列表成功", result);
    }

    @ApiOperation(value = "批量导入白名单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "factory_id", value = "工厂ID", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "whitelists", value = "白名单列表", dataTypeClass = String.class, required = true)
    })
    @PostMapping("/whitelists/batch-import")
    public JsonResult<Map<String, Object>> batchImportWhitelists(HttpServletRequest request) {
        String factoryId = request.getParameter("factory_id");
        String whitelistsJson = request.getParameter("whitelists");
        
        Map<String, Object> result = platformService.batchImportWhitelists(factoryId, whitelistsJson);
        return new JsonResult<>(OK, "批量导入完成", result);
    }

    @ApiOperation(value = "更新白名单状态")
    @ApiImplicitParam(name = "status", value = "白名单状态", dataTypeClass = String.class, required = true)
    @PutMapping("/whitelists/{whitelistId}/status")
    public JsonResult<Map<String, Object>> updateWhitelistStatus(@PathVariable Integer whitelistId, HttpServletRequest request) {
        String status = request.getParameter("status");
        Map<String, Object> result = platformService.updateWhitelistStatus(whitelistId, status);
        return new JsonResult<>(OK, "白名单状态更新成功", result);
    }

    @ApiOperation(value = "删除白名单记录")
    @DeleteMapping("/whitelists/{whitelistId}")
    public JsonResult<Void> deletePlatformWhitelist(@PathVariable Integer whitelistId) {
        platformService.deletePlatformWhitelist(whitelistId);
        return new JsonResult<>(OK, "白名单记录删除成功", null);
    }

    @ApiOperation(value = "批量删除白名单记录")
    @ApiImplicitParam(name = "ids", value = "白名单ID列表", dataTypeClass = String.class, required = true)
    @DeleteMapping("/whitelists/batch-delete")
    public JsonResult<Map<String, Object>> batchDeleteWhitelists(HttpServletRequest request) {
        String idsJson = request.getParameter("ids");
        Map<String, Object> result = platformService.batchDeleteWhitelists(idsJson);
        return new JsonResult<>(OK, "批量删除成功", result);
    }

    @ApiOperation(value = "清理过期白名单记录")
    @DeleteMapping("/whitelists/cleanup")
    public JsonResult<Map<String, Object>> cleanupExpiredWhitelists() {
        Map<String, Object> result = platformService.cleanupExpiredWhitelists();
        return new JsonResult<>(OK, "过期记录清理完成", result);
    }
}

