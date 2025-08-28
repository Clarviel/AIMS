package worker.aims.service.itf;

import worker.aims.DTO.GetWhitelistRequest;
import worker.aims.DTO.UpdateWhitelistRequest;
import worker.aims.entity.UserWhitelist;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface UserWhitelistService {

    void updateUserWhitelistStatus(Integer id, String status);

    UserWhitelist getUserWhitelistByFactoryIdAndPhoneNumber(String factoryId, String phoneNumber);

    Map<String, Object> addWhitelist(String factoryId, Integer userId, List<String> phoneNumbers, LocalDateTime expiresAt);

    Map<String, Object> getWhitelist(GetWhitelistRequest request);

    UserWhitelist updateWhitelist(String factoryId, UpdateWhitelistRequest request);

    void deleteWhitelist(String factoryId, Integer id);

    Map<String, Object> batchDeleteWhitelist(String factoryId, List<Integer> ids);

    Map<String, Object> getWhitelistStats(String factoryId);

    Map<String, Object> updateExpiredWhitelist(String factoryId);

}
