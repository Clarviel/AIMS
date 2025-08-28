package worker.aims.mapper;

import org.apache.ibatis.annotations.*;
import worker.aims.entity.PlatformAdmin;

@Mapper
public interface PlatformAdminMapper {

    @Select("SELECT * FROM platform_admin WHERE username = #{username}")
    PlatformAdmin getAdminByUsername(@Param("username") String username);

    @Select("SELECT * FROM platform_admin WHERE pid = #{pid}")
    PlatformAdmin getAdminByPid(@Param("pid") Integer pid);
}
