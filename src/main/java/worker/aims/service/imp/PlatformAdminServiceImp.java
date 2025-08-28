package worker.aims.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import worker.aims.entity.PlatformAdmin;
import worker.aims.mapper.PlatformAdminMapper;
import worker.aims.service.ex.NotFoundException;
import worker.aims.service.itf.PlatformAdminService;

@Service
public class PlatformAdminServiceImp implements PlatformAdminService {

    @Autowired
    PlatformAdminMapper platformAdminMapper;

    @Override
    public PlatformAdmin getAdminByUsername(String username) {
        return platformAdminMapper.getAdminByUsername(username);
    }

    @Override
    public PlatformAdmin getAdminByPid(Integer pid) {
        return platformAdminMapper.getAdminByPid(pid);
    }
}
