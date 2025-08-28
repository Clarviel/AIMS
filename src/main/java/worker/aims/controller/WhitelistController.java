package worker.aims.controller;

import io.swagger.annotations.Api;
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
import worker.aims.DTO.AddWhitelistRequest;
import worker.aims.DTO.GetWhitelistRequest;
import worker.aims.DTO.UpdateWhitelistRequest;
import worker.aims.entity.UserWhitelist;
import worker.aims.service.itf.UserWhitelistService;
import worker.aims.util.JsonResult;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
    @PostMapping("/add_whitelist")
    public JsonResult<Map<String, Object>> addWhitelist(@RequestBody AddWhitelistRequest request) {
        Map<String, Object> result = userWhitelistService.addWhitelist(
                request.getFactoryId(),
                request.getUid(),
                request.getPhoneNumbers(),
                request.getExpiresAt()
        );
        return new JsonResult<>(OK, "白名单添加成功", result);
    }

    @ApiOperation(value = "分页获取白名单")
    @PostMapping("/list")
    public JsonResult<Map<String, Object>> getWhitelist(@RequestBody GetWhitelistRequest request) {
        Map<String, Object> result = userWhitelistService.getWhitelist(request);
        return new JsonResult<>(OK, "获取白名单成功", result);
    }

    @ApiOperation("更新白名单状态")
    @PostMapping("/update")
    public JsonResult<UserWhitelist> updateWhitelist(@RequestParam("factoryId") String factoryId,
                                                     @RequestBody UpdateWhitelistRequest request) {
        UserWhitelist updated = userWhitelistService.updateWhitelist(factoryId, request);
        return new JsonResult<>(OK, "白名单状态更新成功", updated);
    }

    @ApiOperation("删除白名单记录")
    @DeleteMapping("/delete/{id}")
    public JsonResult<Void> deleteWhitelist(@RequestParam("factoryId") String factoryId,
                                            @PathVariable("id") Integer id) {
        userWhitelistService.deleteWhitelist(factoryId, id);
        return new JsonResult<>(OK, "白名单记录删除成功", null);
    }

    @ApiOperation("批量删除白名单")
    @PostMapping("/batch_delete")
    public JsonResult<Map<String, Object>> batchDeleteWhitelist(@RequestParam("factoryId") String factoryId,
                                                                @RequestBody List<Integer> ids) {
        Map<String, Object> result = userWhitelistService.batchDeleteWhitelist(factoryId, ids);
        return new JsonResult<>(OK, "批量删除成功", result);
    }

    @ApiOperation("获取白名单统计信息")
    @GetMapping("/get_whitelist_status")
    public JsonResult<Map<String, Object>> getWhitelistStats(@RequestParam("factoryId") String factoryId) {
        Map<String, Object> result = userWhitelistService.getWhitelistStats(factoryId);
        return new JsonResult<>(OK, "获取统计信息成功", result);
    }

    @ApiOperation("批量更新过期白名单")
    @PostMapping("/update_expired")
    public JsonResult<Map<String, Object>> updateExpiredWhitelist(@RequestParam("factoryId") String factoryId) {
        Map<String, Object> result = userWhitelistService.updateExpiredWhitelist(factoryId);
        return new JsonResult<>(OK, "过期记录更新成功", result);
    }


}
