package worker.aims.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import worker.aims.entity.Factory;
import worker.aims.mapper.FactoryMapper;
import worker.aims.service.ex.*;
import worker.aims.service.itf.FactoryService;

@Service
public class FactoryServiceImp implements FactoryService {

    @Autowired
    private FactoryMapper factoryMapper;

    @Override
    public Factory getFactoryByFid(String fid) {
        Factory result = factoryMapper.getFactoryByFid(fid);
        if (result == null) {
            throw new NotFoundException("工厂不存在或已停用！");
        }
        return result;
    }
}
