# PlatformController Java版本实现

## 概述

这是platformController.js的完整Java版本实现，采用Spring Boot + MyBatis架构，遵循项目的分层设计模式。

## 文件结构

```
src/main/java/worker/aims/
├── controller/
│   └── PlatformController.java          # 平台管理控制器
├── service/
│   ├── itf/
│   │   └── PlatformService.java         # 平台服务接口
│   └── imp/
│       └── PlatformServiceImp.java     # 平台服务实现
├── mapper/
│   ├── FactoryMapper.java              # 工厂数据访问层
│   └── UserWhitelistMapper.java         # 白名单数据访问层
├── entity/
│   ├── Factory.java                    # 工厂实体类（已更新）
│   └── UserWhitelist.java              # 白名单实体类
└── util/
    └── FactoryIdGenerator.java          # 智能工厂ID生成器
```

## 主要功能

### 1. 平台概览管理
- `GET /platform/overview` - 获取平台概览数据
- `GET /platform/overview/export` - 导出平台概览数据

### 2. 工厂管理
- `GET /platform/factories` - 获取工厂列表（支持分页、搜索、状态筛选）
- `POST /platform/factories` - 创建工厂（使用智能ID生成系统）
- `GET /platform/factories/{id}` - 获取工厂详细信息
- `PUT /platform/factories/{id}` - 更新工厂信息
- `PUT /platform/factories/{id}/status` - 启用/停用工厂
- `PUT /platform/factories/{id}/info` - 更新工厂信息（简化版）
- `PUT /platform/factories/{id}/suspend` - 暂停工厂
- `PUT /platform/factories/{id}/activate` - 激活工厂
- `DELETE /platform/factories/{id}` - 删除工厂
- `GET /platform/factories/stats` - 获取工厂统计信息
- `GET /platform/factories/export` - 导出工厂数据

### 3. 员工管理
- `GET /platform/factories/{factoryId}/employees` - 获取工厂员工列表
- `PUT /platform/factories/{factoryId}/employees/{employeeId}/status` - 更新员工状态
- `DELETE /platform/factories/{factoryId}/employees/{employeeId}` - 删除员工

### 4. 超级管理员管理
- `POST /platform/factories/{id}/super-admin` - 为工厂创建超级管理员

### 5. 白名单管理
- `GET /platform/whitelists` - 获取平台白名单列表
- `POST /platform/whitelists/batch-import` - 批量导入白名单
- `PUT /platform/whitelists/{whitelistId}/status` - 更新白名单状态
- `DELETE /platform/whitelists/{whitelistId}` - 删除白名单记录
- `DELETE /platform/whitelists/batch-delete` - 批量删除白名单记录
- `DELETE /platform/whitelists/cleanup` - 清理过期白名单记录

### 6. 操作日志管理
- `GET /platform/logs` - 获取操作日志
- `GET /platform/logs/export` - 导出操作日志

### 7. 数据导出
- `GET /platform/users/export` - 导出用户统计数据

## 智能工厂ID生成系统

### 功能特点
- 自动推断行业代码和地区代码
- 生成唯一序列号
- 置信度评估
- 支持手动验证标记

### ID格式
新格式：`FCT_{行业代码}_{地区代码}_{年份}_{序列号}`
例如：`FCT_MFG_BJ_2024_001`

### 推断逻辑
- **行业推断**：基于工厂名称和行业信息
- **地区推断**：基于地址信息
- **置信度计算**：综合评估推断准确性

## 技术特点

### 1. 分层架构
- **Controller层**：处理HTTP请求，参数验证，返回统一响应格式
- **Service层**：业务逻辑处理，事务管理
- **Mapper层**：数据访问，使用MyBatis注解方式

### 2. 统一响应格式
使用`JsonResult<T>`统一响应格式：
```
java
{
    "code": 200,
    "message": "操作成功",
    "data": {...}
}
```

### 3. 异常处理
- `NotFoundException` - 资源不存在
- `NameDuplicateException` - 名称重复
- `ValidationError` - 参数验证错误
- `BusinessLogicError` - 业务逻辑错误

### 4. 分页支持
所有列表接口都支持分页查询，参数：
- `page` - 页码（默认1）
- `size` - 每页大小（默认10）

### 5. 搜索和筛选
支持关键词搜索和状态筛选，具体参数根据接口而定。

## 数据库设计

### Factory表新增字段
- `description` - 工厂描述
- `industry_code` - 行业代码
- `region_code` - 地区代码
- `factory_year` - 工厂年份
- `sequence_number` - 序列号
- `inference_data` - 推断数据（JSON格式）
- `legacy_id` - 老格式ID
- `manually_verified` - 手动验证标记

## 使用示例

### 创建工厂
```bash
POST /platform/factories
Content-Type: application/x-www-form-urlencoded

name=测试工厂&industry=制造业&contactEmail=test@example.com&contactPhone=13800138000&address=北京市朝阳区&description=这是一个测试工厂
```

### 获取工厂列表
```bash
GET /platform/factories?page=1&size=10&keyword=测试&status=active
```

### 创建超级管理员
```bash
POST /platform/factories/FCT_MFG_BJ_2024_001/super-admin
Content-Type: application/x-www-form-urlencoded

username=admin&email=admin@example.com&fullName=管理员&phone=13800138000
```

## 注意事项

1. **权限控制**：所有接口都需要平台管理员权限
2. **数据验证**：所有输入参数都会进行验证
3. **事务管理**：涉及多表操作时使用事务保证数据一致性
4. **日志记录**：重要操作会记录操作日志
5. **软删除**：删除操作通常是软删除，保留数据用于审计

## 扩展性

该实现具有良好的扩展性：
- 可以轻松添加新的工厂类型
- 支持自定义行业和地区代码
- 可以扩展更多的统计功能
- 支持自定义导出格式

