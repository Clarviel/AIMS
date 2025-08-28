package worker.aims.mapper;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserSessionMapper {

    @Update("UPDATE user_session SET is_revoked = #{isRevoked} WHERE user_id = #{userId} and factory_id = #{factoryId}")
    int updateUserSessionIsRevokedByUserIdAndFactoryId(@Param("user_id") Integer userId, @Param("factory_id") String factoryId, @Param("isRevoked") Boolean isRevoked);

}
