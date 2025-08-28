package worker.aims.service.itf;

import org.apache.ibatis.annotations.Param;
import worker.aims.entity.PlatformAdmin;

public interface PlatformAdminService {

    PlatformAdmin getAdminByUsername(String username);

    PlatformAdmin getAdminByPid(Integer pid);

}
