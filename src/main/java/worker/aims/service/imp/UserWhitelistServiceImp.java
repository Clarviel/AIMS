package worker.aims.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import worker.aims.DTO.GetWhitelistRequest;
import worker.aims.DTO.UpdateWhitelistRequest;
import worker.aims.entity.User;
import worker.aims.entity.UserWhitelist;
import worker.aims.mapper.UserWhitelistMapper;
import worker.aims.service.ex.*;
import worker.aims.service.itf.UserWhitelistService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserWhitelistServiceImp implements UserWhitelistService {

    @Autowired
    private UserWhitelistMapper userWhitelistMapper;


    @Override
    public void updateUserWhitelistStatus(Integer id, String status) {
        UserWhitelist result = userWhitelistMapper.getUserWhitelistById(id);
        if (result == null) {
            throw new NotFoundException("该条白名单不存在！");
        }
        int rows = userWhitelistMapper.updateUserWhitelistStatus(id, status);
        if (rows != 1) {
            throw new UpdateException("更新白名单状态时出现未知异常！");
        }
    }

    @Override
    public UserWhitelist getUserWhitelistByFactoryIdAndPhoneNumber(String factoryId, String phoneNumber) {
        UserWhitelist result = userWhitelistMapper.getUserWhitelistByFactoryIdAndPhoneNumber(factoryId, phoneNumber);
        if (result == null) {
            throw new NotFoundException("该手机号未被邀请注册！");
        }
        else {
            if (result.getStatus().equals("REGISTERED")) {
                throw new AccessDeniedException("该手机号已被注册");
            }
            if (result.getStatus().equals("EXPIRED")) {
                throw new AccessDeniedException("邀请已过期，请联系管理员");
            }
        }
        if (result.getExpiresAt() != null && LocalDateTime.now().isAfter(result.getExpiresAt())) {
            userWhitelistMapper.updateUserWhitelistStatus(result.getId(), "EXPIRED");
            throw new AccessDeniedException("邀请已过期，请联系管理员");
        }
        return result;
    }

    @Override
    public Map<String, Object> addWhitelist(String factoryId, Integer userId, List<String> phoneNumbers, LocalDateTime expiresAt) {
        Set<String> existingSet = new HashSet<>(userWhitelistMapper.findExistingPhoneNumbers(factoryId));
        List<String> duplicates = phoneNumbers.stream()
                .filter(existingSet::contains)
                .collect(Collectors.toList());
        if (!duplicates.isEmpty()) {
            throw new NameDuplicateException("以下手机号已在白名单中: " + String.join(", ", duplicates));
        }
        List<UserWhitelist> whitelist = phoneNumbers.stream().map(phone -> {
            UserWhitelist w = new UserWhitelist();
            w.setFactoryId(factoryId);
            w.setPhoneNumber(phone);
            w.setAddedByUserId(userId);
            w.setExpiresAt(expiresAt);
            w.setStatus("PENDING");
            w.setCreatedAt(LocalDateTime.now());
            w.setUpdatedAt(LocalDateTime.now());
            w.setAddedByPlatformId(null);
            return w;
        }).collect(Collectors.toList());
        int inserted = 0;
        List<String> successPhones = new ArrayList<>();
        for (UserWhitelist w : whitelist) {
            int rows = userWhitelistMapper.insertWhitelist(w);
            if (rows != 1) {
                throw new InsertException("注册过程出现未知异常！");
            }
            else {
                inserted += rows;
                successPhones.add(w.getPhoneNumber());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("addedCount", inserted);
        result.put("phoneNumbers", successPhones);
        return result;
    }

    @Override
    public Map<String, Object> getWhitelist(GetWhitelistRequest request) {
        int page = request.getPage() != null ? request.getPage() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
        int offset = (page - 1) * pageSize;
        // 查询总数
        int total = userWhitelistMapper.countWhitelist(
                request.getFactoryId(),
                request.getStatus(),
                request.getSearch()
        );
        // 分页数据
        List<UserWhitelist> items = userWhitelistMapper.findWhitelist(
                request.getFactoryId(),
                request.getStatus(),
                request.getSearch(),
                offset,
                pageSize
        );
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("pageSize", pageSize);
        pagination.put("total", total);
        pagination.put("totalPages", (int) Math.ceil((double) total / pageSize));
        pagination.put("hasNext", offset + pageSize < total);
        pagination.put("hasPrev", page > 1);
        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("pagination", pagination);
        return result;
    }

    // 更新白名单
    @Override
    public UserWhitelist updateWhitelist(String factoryId, UpdateWhitelistRequest request) {
        UserWhitelist whitelist = userWhitelistMapper.findByIdAndFactoryId(request.getId(), factoryId);
        if (whitelist == null) {
            throw new NotFoundException("白名单记录不存在");
        }

        int rows = userWhitelistMapper.updateWhitelist(
                request.getId(),
                request.getStatus(),
                request.getExpiresAt()
        );
        if (rows != 1) {
            throw new UpdateException("更新白名单状态失败");
        }

        return userWhitelistMapper.findByIdAndFactoryId(request.getId(), factoryId);
    }

    // 删除白名单
    @Override
    public void deleteWhitelist(String factoryId, Integer id) {
        UserWhitelist whitelist = userWhitelistMapper.findByIdAndFactoryId(id, factoryId);
        if (whitelist == null) {
            throw new NotFoundException("白名单记录不存在");
        }

        if ("REGISTERED".equals(whitelist.getStatus())) {
            throw new AccessDeniedException("已注册的白名单记录无法删除");
        }

        int rows = userWhitelistMapper.deleteWhitelist(id);
        if (rows != 1) {
            throw new DeleteException("删除白名单失败");
        }
    }

    // 批量删除
    @Override
    public Map<String, Object> batchDeleteWhitelist(String factoryId, List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new DeleteException("请选择要删除的记录");
        }
        List<UserWhitelist> whitelist = userWhitelistMapper.findByIdsAndFactoryId(ids, factoryId);
        if (whitelist.isEmpty()) {
            throw new NotFoundException("未找到可删除的记录");
        }
        boolean hasRegistered = whitelist.stream().anyMatch(w -> "REGISTERED".equals(w.getStatus()));
        if (hasRegistered) {
            throw new AccessDeniedException("存在已注册的白名单记录，无法删除");
        }
        int deleted = userWhitelistMapper.batchDelete(ids, factoryId);
        Map<String, Object> result = new HashMap<>();
        result.put("deletedCount", deleted);
        return result;
    }

    // 白名单统计
    @Override
    public Map<String, Object> getWhitelistStats(String factoryId) {
        List<Map<String, Object>> stats = userWhitelistMapper.countByStatus(factoryId);
        Map<String, Integer> statusStats = new HashMap<>();
        statusStats.put("PENDING", 0);
        statusStats.put("REGISTERED", 0);
        statusStats.put("EXPIRED", 0);
        for (Map<String, Object> stat : stats) {
            String status = (String) stat.get("status");
            Long count = ((Number) stat.get("count")).longValue();
            statusStats.put(status, count.intValue());
        }
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        int todayAdded = userWhitelistMapper.countTodayAdded(factoryId, today);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);
        int expiringSoon = userWhitelistMapper.countExpiringSoon(factoryId, now, sevenDaysLater);
        int total = statusStats.values().stream().mapToInt(Integer::intValue).sum();
        Map<String, Object> result = new HashMap<>();
        result.put("statusStats", statusStats);
        result.put("todayAdded", todayAdded);
        result.put("expiringSoon", expiringSoon);
        result.put("total", total);
        return result;
    }

    // 批量更新过期
    @Override
    public Map<String, Object> updateExpiredWhitelist(String factoryId) {
        int updated = userWhitelistMapper.updateExpired(factoryId, LocalDateTime.now());
        Map<String, Object> result = new HashMap<>();
        result.put("updatedCount", updated);
        return result;
    }

}
