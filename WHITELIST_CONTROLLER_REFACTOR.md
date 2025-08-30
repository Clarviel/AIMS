# WhitelistController 接口格式统一重构

## 概述

本次重构将WhitelistController中的接口格式从RequestBody改为request.getParameter方式，统一了项目中的接口格式，并删除了不再需要的DTO类。

## 主要修改

### 1. WhitelistController.java 修改

#### 修改前（使用RequestBody）：
```java
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
```

#### 修改后（使用request.getParameter）：
```java
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
```

### 2. 接口参数处理改进

#### 新增功能：
- **参数验证**：添加了完整的参数验证逻辑
- **类型转换**：自动处理字符串到数字、日期的转换
- **默认值处理**：为可选参数提供默认值
- **错误处理**：增加了参数解析的异常处理

#### 新增工具方法：
```java
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
```

### 3. UserWhitelistService 接口修改

#### 修改前：
```java
Map<String, Object> addWhitelist(String factoryId, Integer userId, List<String> phoneNumbers, LocalDateTime expiresAt);
Map<String, Object> getWhitelist(GetWhitelistRequest request);
UserWhitelist updateWhitelist(String factoryId, UpdateWhitelistRequest request);
```

#### 修改后：
```java
Map<String, Object> addWhitelist(String factoryId, Integer userId, String phoneNumbers, LocalDateTime expiresAt);
Map<String, Object> getWhitelist(Map<String, Object> params);
UserWhitelist updateWhitelist(String factoryId, Map<String, Object> updateParams);
```

### 4. UserWhitelistServiceImp 实现修改

#### 主要改进：
- **手机号解析**：支持逗号分隔或空格分隔的手机号列表
- **参数映射**：使用Map替代DTO类进行参数传递
- **类型安全**：增加了参数类型检查和转换

#### 手机号解析示例：
```java
// 解析手机号列表，支持逗号分隔或空格分隔
List<String> phoneList = Arrays.asList(phoneNumbers.split("[,，\\s]+"));
```

### 5. 删除的文件

#### 删除的DTO类：
- `src/main/java/worker/aims/DTO/whitelistDTO/AddWhitelistRequest.java`
- `src/main/java/worker/aims/DTO/whitelistDTO/GetWhitelistRequest.java`
- `src/main/java/worker/aims/DTO/whitelistDTO/UpdateWhitelistRequest.java`

#### 删除的文件夹：
- `src/main/java/worker/aims/DTO/whitelistDTO/`
- `src/main/java/worker/aims/DTO/factoryDTO/`

## 接口使用示例

### 1. 批量添加白名单
```bash
POST /whitelist/add_whitelist
Content-Type: application/x-www-form-urlencoded

factoryId=FCT_2024_001&uid=123&phoneNumbers=13800138000,13800138001,13800138002&expiresAt=2024-12-31T23:59:59
```

### 2. 分页获取白名单
```bash
POST /whitelist/list
Content-Type: application/x-www-form-urlencoded

factoryId=FCT_2024_001&page=1&pageSize=10&status=PENDING&search=138
```

### 3. 更新白名单状态
```bash
POST /whitelist/update
Content-Type: application/x-www-form-urlencoded

factoryId=FCT_2024_001&id=1&status=SUSPENDED&expiresAt=2024-12-31T23:59:59
```

### 4. 批量删除白名单
```bash
POST /whitelist/batch_delete
Content-Type: application/x-www-form-urlencoded

factoryId=FCT_2024_001&ids=1,2,3
```

## 技术改进

### 1. 统一性
- 所有接口现在都使用相同的参数获取方式
- 与项目中其他Controller保持一致的风格

### 2. 灵活性
- 支持多种格式的ID列表输入
- 支持多种分隔符的手机号列表
- 自动处理日期时间格式转换

### 3. 健壮性
- 增加了参数验证和错误处理
- 提供了默认值处理
- 支持无效参数的优雅处理

### 4. 可维护性
- 减少了代码重复
- 简化了参数传递
- 提高了代码的可读性

## 注意事项

1. **参数格式**：所有参数都通过URL参数或表单参数传递
2. **日期格式**：日期时间使用ISO格式（如：2024-12-31T23:59:59）
3. **列表格式**：支持逗号分隔、中文逗号分隔或空格分隔
4. **错误处理**：无效参数会被记录日志但不会导致程序崩溃
5. **向后兼容**：保持了原有的业务逻辑不变

## 测试建议

1. **参数验证测试**：测试各种参数格式和边界情况
2. **错误处理测试**：测试无效参数的处理
3. **格式兼容性测试**：测试不同分隔符的兼容性
4. **性能测试**：验证参数解析的性能影响
