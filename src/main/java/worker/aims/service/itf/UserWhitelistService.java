package worker.aims.service.itf;

import worker.aims.entity.UserWhitelist;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface UserWhitelistService {

    void updateUserWhitelistStatus(Integer id, String status);

    UserWhitelist getUserWhitelistByFactoryIdAndPhoneNumber(String factoryId, String phoneNumber);

    Map<String, Object> addWhitelist(String factoryId, Integer userId, String phoneNumbers, LocalDateTime expiresAt);

    Map<String, Object> getWhitelist(Map<String, Object> params);

    UserWhitelist updateWhitelist(String factoryId, Map<String, Object> updateParams);

    void deleteWhitelist(String factoryId, Integer id);

    Map<String, Object> batchDeleteWhitelist(String factoryId, List<Integer> ids);

    Map<String, Object> getWhitelistStats(String factoryId);

    Map<String, Object> updateExpiredWhitelist(String factoryId);

}
