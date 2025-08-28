package worker.aims.service.itf;


public interface UserSessionService {

    void updateUserSessionIsRevokedByUserIdAndFactoryId(Integer userId, String factoryId, Boolean isRevoked);

}
