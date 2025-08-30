package worker.aims.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import worker.aims.entity.UserWhitelist;
import worker.aims.service.itf.UserWhitelistService;
import worker.aims.util.JsonResult;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@Slf4j
@RestController
@RequestMapping("/whitelist")
@Api(tags = "白名单管理接口")
public class WhitelistController extends BaseController {

    @Autowired
    private UserWhitelistService userWhitelistService;

    @ApiOperation(value = "批量添加白名单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "factoryId", value = "工厂ID", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "uid", value = "用户ID", dataTypeClass = Integer.class, required = true),
            @ApiImplicitParam(name = "phoneNumbers", value = "手机号列表", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "expiresAt", value = "过期时间", dataTypeClass = String.class, required = false)
    })
    @PostMapping("/add_whitelist")
    public JsonResult<Map<String, Object>> addWhitelist(HttpServletRequest request) {
        String factoryId = request.getParameter("factoryId");
        Integer uid = request.getParameter("uid") != null ? Integer.parseInt(request.getParameter("uid")) : null;
        String phoneNumbers = request.getParameter("phoneNumbers");
        String expiresAtStr = request.getParameter("expiresAt");
        
        LocalDateTime expiresAt = null;
        if (expiresAtStr != null && !expiresAtStr.trim().isEmpty()) {
            expiresAt = LocalDateTime.parse(expiresAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        
        Map<String, Object> result = userWhitelistService.addWhitelist(factoryId, uid, phoneNumbers, expiresAt);
        return new JsonResult<>(OK, "白名单添加成功", result);
    }

    @ApiOperation(value = "分页获取白名单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "factoryId", value = "工厂ID", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "page", value = "页码", dataTypeClass = Integer.class, required = false),
            @ApiImplicitParam(name = "pageSize", value = "每页大小", dataTypeClass = Integer.class, required = false),
            @ApiImplicitParam(name = "status", value = "状态筛选", dataTypeClass = String.class, required = false),
            @ApiImplicitParam(name = "search", value = "搜索关键词", dataTypeClass = String.class, required = false)
    })
    @PostMapping("/list")
    public JsonResult<Map<String, Object>> getWhitelist(HttpServletRequest request) {
        String factoryId = request.getParameter("factoryId");
        Integer page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;
        Integer pageSize = request.getParameter("pageSize") != null ? Integer.parseInt(request.getParameter("pageSize")) : 10;
        String status = request.getParameter("status");
        String search = request.getParameter("search");
        
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("factoryId", factoryId);
        params.put("page", page);
        params.put("pageSize", pageSize);
        params.put("status", status != null ? status : "");
        params.put("search", search != null ? search : "");
        
        Map<String, Object> result = userWhitelistService.getWhitelist(params);
        return new JsonResult<>(OK, "获取白名单成功", result);
    }

    @ApiOperation("更新白名单状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "factoryId", value = "工厂ID", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "id", value = "白名单ID", dataTypeClass = Integer.class, required = true),
            @ApiImplicitParam(name = "status", value = "新状态", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "expiresAt", value = "过期时间", dataTypeClass = String.class, required = false)
    })
    @PostMapping("/update")
    public JsonResult<UserWhitelist> updateWhitelist(HttpServletRequest request) {
        String factoryId = request.getParameter("factoryId");
        Integer id = Integer.parseInt(request.getParameter("id"));
        String status = request.getParameter("status");
        String expiresAtStr = request.getParameter("expiresAt");
        
        LocalDateTime expiresAt = null;
        if (expiresAtStr != null && !expiresAtStr.trim().isEmpty()) {
            expiresAt = LocalDateTime.parse(expiresAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        
        Map<String, Object> updateParams = new java.util.HashMap<>();
        updateParams.put("id", id);
        updateParams.put("status", status);
        updateParams.put("expiresAt", expiresAt);
        
        UserWhitelist updated = userWhitelistService.updateWhitelist(factoryId, updateParams);
        return new JsonResult<>(OK, "白名单状态更新成功", updated);
    }

    @ApiOperation("删除白名单记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "factoryId", value = "工厂ID", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "id", value = "白名单ID", dataTypeClass = Integer.class, required = true)
    })
    @DeleteMapping("/delete/{id}")
    public JsonResult<Void> deleteWhitelist(@PathVariable("id") Integer id, HttpServletRequest request) {
        String factoryId = request.getParameter("factoryId");
        userWhitelistService.deleteWhitelist(factoryId, id);
        return new JsonResult<>(OK, "白名单记录删除成功", null);
    }

    @ApiOperation("批量删除白名单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "factoryId", value = "工厂ID", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "ids", value = "白名单ID列表", dataTypeClass = String.class, required = true)
    })
    @PostMapping("/batch_delete")
    public JsonResult<Map<String, Object>> batchDeleteWhitelist(HttpServletRequest request) {
        String factoryId = request.getParameter("factoryId");
        String idsStr = request.getParameter("ids");
        
        // 解析ID列表，假设格式为 "1,2,3" 或 "[1,2,3]"
        List<Integer> ids = parseIdsFromString(idsStr);
        
        Map<String, Object> result = userWhitelistService.batchDeleteWhitelist(factoryId, ids);
        return new JsonResult<>(OK, "批量删除成功", result);
    }

    @ApiOperation("获取白名单统计信息")
    @ApiImplicitParam(name = "factoryId", value = "工厂ID", dataTypeClass = String.class, required = true)
    @GetMapping("/get_whitelist_status")
    public JsonResult<Map<String, Object>> getWhitelistStats(HttpServletRequest request) {
        String factoryId = request.getParameter("factoryId");
        Map<String, Object> result = userWhitelistService.getWhitelistStats(factoryId);
        return new JsonResult<>(OK, "获取统计信息成功", result);
    }

    @ApiOperation("批量更新过期白名单")
    @ApiImplicitParam(name = "factoryId", value = "工厂ID", dataTypeClass = String.class, required = true)
    @PostMapping("/update_expired")
    public JsonResult<Map<String, Object>> updateExpiredWhitelist(HttpServletRequest request) {
        String factoryId = request.getParameter("factoryId");
        Map<String, Object> result = userWhitelistService.updateExpiredWhitelist(factoryId);
        return new JsonResult<>(OK, "过期记录更新成功", result);
    }

    /**
     * 解析ID列表字符串
     * 支持格式: "1,2,3" 或 "[1,2,3]" 或 "1 2 3"
     */
    private List<Integer> parseIdsFromString(String idsStr) {
        if (idsStr == null || idsStr.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        // 移除方括号和多余空格
        String cleaned = idsStr.replaceAll("[\\[\\]]", "").trim();
        
        // 分割字符串
        String[] parts = cleaned.split("[,，\\s]+");
        
        List<Integer> ids = new java.util.ArrayList<>();
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                try {
                    ids.add(Integer.parseInt(part.trim()));
                } catch (NumberFormatException e) {
                    // 忽略无效的数字
                    log.warn("Invalid ID format: {}", part);
                }
            }
        }
        
        return ids;
    }
}
