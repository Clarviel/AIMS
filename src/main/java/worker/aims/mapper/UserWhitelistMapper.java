package worker.aims.mapper;

import org.apache.ibatis.annotations.*;
import worker.aims.entity.UserWhitelist;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserWhitelistMapper {

    @Select("SELECT * FROM user_whitelist WHERE factory_id = #{factoryId} and phone_number = #{phoneNumber}")
    UserWhitelist getUserWhitelistByFactoryIdAndPhoneNumber(@Param("factoryId") String factoryId, @Param("phoneNumber") String phoneNumber);

    @Select("SELECT * FROM user_whitelist WHERE id = #{id}}")
    UserWhitelist getUserWhitelistById(@Param("id") Integer id);

    @Update("UPDATE user_whitelist SET status = #{status} WHERE id = #{id}")
    int updateUserWhitelistStatus(@Param("id") Integer id, @Param("status") String status);

    @Select("SELECT phone_number FROM user_whitelist WHERE factory_id = #{factoryId}")
    List<String> findExistingPhoneNumbers(@Param("factoryId") String factoryId);

    @Insert("INSERT INTO user_whitelist (factory_id, phone_number, status, added_by_user_id, expires_at, created_at, updated_at, added_by_platform_id)" +
            "VALUES (#{factoryId}, #{phoneNumber}, #{status}, #{addedByUserId}, #{expiresAt}, #{createdAt}, #{updatedAt}, #{addedByPlatformId})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertWhitelist(UserWhitelist userWhitelist);

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM user_whitelist",
            "WHERE factory_id = #{factoryId}",
            "<if test='status != null and status != \"\"'>",
            " AND status = #{status}",
            "</if>",
            "<if test='search != null and search != \"\"'>",
            " AND phone_number LIKE CONCAT('%', #{search}, '%')",
            "</if>",
            "</script>"
    })
    int countWhitelist(@Param("factoryId") String factoryId,
                       @Param("status") String status,
                       @Param("search") String search);

    // 分页查询
    @Select({
            "<script>",
            "SELECT w.*, u.uid as addedByUserId, u.username, u.full_name as fullName ",
            "FROM user_whitelist w ",
            "LEFT JOIN user u ON w.added_by_user_id = u.uid ",
            "WHERE w.factory_id = #{factoryId}",
            "<if test='status != null and status != \"\"'>",
            " AND w.status = #{status}",
            "</if>",
            "<if test='search != null and search != \"\"'>",
            " AND w.phone_number LIKE CONCAT('%', #{search}, '%')",
            "</if>",
            "ORDER BY w.created_at DESC ",
            "LIMIT #{offset}, #{pageSize}",
            "</script>"
    })
    List<UserWhitelist> findWhitelist(@Param("factoryId") String factoryId,
                                      @Param("status") String status,
                                      @Param("search") String search,
                                      @Param("offset") int offset,
                                      @Param("pageSize") int pageSize);

    @Select("SELECT * FROM user_whitelist WHERE id = #{id} AND factory_id = #{factoryId}")
    UserWhitelist findByIdAndFactoryId(@Param("id") Integer id, @Param("factoryId") String factoryId);

    @Update({
            "<script>",
            "UPDATE user_whitelist",
            "SET status = #{status},",
            "<if test='expiresAt != null'> expires_at = #{expiresAt}, </if>",
            "updated_at = NOW()",
            "WHERE id = #{id}",
            "</script>"
    })
    int updateWhitelist(@Param("id") Integer id,
                        @Param("status") String status,
                        @Param("expiresAt") LocalDateTime expiresAt);

    @Delete("DELETE FROM user_whitelist WHERE id = #{id}")
    int deleteWhitelist(@Param("id") Integer id);

    @Select({
            "<script>",
            "SELECT * FROM user_whitelist ",
            "WHERE factory_id = #{factoryId} ",
            "AND id IN ",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    List<UserWhitelist> findByIdsAndFactoryId(@Param("ids") List<Integer> ids, @Param("factoryId") String factoryId);

    @Delete({
            "<script>",
            "DELETE FROM user_whitelist ",
            "WHERE factory_id = #{factoryId} ",
            "AND id IN ",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int batchDelete(@Param("ids") List<Integer> ids, @Param("factoryId") String factoryId);

    @Select("SELECT status, COUNT(*) as count FROM user_whitelist WHERE factory_id = #{factoryId} GROUP BY status")
    List<Map<String, Object>> countByStatus(@Param("factoryId") String factoryId);

    @Select("SELECT COUNT(*) FROM user_whitelist WHERE factory_id = #{factoryId} AND created_at >= #{today}")
    int countTodayAdded(@Param("factoryId") String factoryId, @Param("today") LocalDateTime today);

    @Select("SELECT COUNT(*) FROM user_whitelist WHERE factory_id = #{factoryId} AND status = 'PENDING' AND expires_at BETWEEN #{now} AND #{sevenDaysLater}")
    int countExpiringSoon(@Param("factoryId") String factoryId, @Param("now") LocalDateTime now, @Param("sevenDaysLater") LocalDateTime sevenDaysLater);

    @Update("UPDATE user_whitelist SET status = 'EXPIRED', updated_at = NOW() WHERE factory_id = #{factoryId} AND status = 'PENDING' AND expires_at < #{now}")
    int updateExpired(@Param("factoryId") String factoryId, @Param("now") LocalDateTime now);

}
