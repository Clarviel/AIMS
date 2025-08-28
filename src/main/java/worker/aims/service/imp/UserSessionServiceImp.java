package worker.aims.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import worker.aims.mapper.UserSessionMapper;
import worker.aims.service.ex.UpdateException;
import worker.aims.service.itf.UserSessionService;

@Service
public class UserSessionServiceImp implements UserSessionService {

    @Autowired
    private UserSessionMapper userSessionMapper;

    @Override
    public void updateUserSessionIsRevokedByUserIdAndFactoryId(Integer userId, String factoryId, Boolean isRevoked) {
        int rows = userSessionMapper.updateUserSessionIsRevokedByUserIdAndFactoryId(userId, factoryId, isRevoked);
        if (rows != 1) {
            throw new UpdateException("更新时出现未知异常！");
        }
    }

}
