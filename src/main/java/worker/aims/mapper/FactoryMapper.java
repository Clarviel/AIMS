package worker.aims.mapper;

import org.apache.ibatis.annotations.*;
import worker.aims.entity.Factory;

import java.util.List;

@Mapper
public interface FactoryMapper {

    @Select("SELECT * FROM factory WHERE fid = #{fid}")
    Factory getFactoryByFid(@Param("fid") String fid);



}
