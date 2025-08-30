package worker.aims.mapper;

import org.apache.ibatis.annotations.*;
import worker.aims.entity.UserWhitelist;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserWhitelistMapper {

    @Insert("INSERT INTO user_whitelist(factory_id, phone_number, status, added_by_user_id, expires_at, created_at, updated_at, added_by_platform_id) " +
            "VALUES (#{factoryId}, #{phoneNumber}, #{status}, #{addedByUserId}, #{expiresAt}, #{createdAt}, #{updatedAt}, #{addedByPlatformId})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertUserWhitelist(UserWhitelist userWhitelist);

    @Select("SELECT * FROM user_whitelist WHERE id = #{id}")
    UserWhitelist getUserWhitelistById(@Param("id") Integer id);

    @Select("SELECT * FROM user_whitelist WHERE factory_id = #{factoryId} AND phone_number = #{phoneNumber}")
    UserWhitelist getUserWhitelistByFactoryIdAndPhoneNumber(@Param("factoryId") String factoryId, @Param("phoneNumber") String phoneNumber);

    @Update("UPDATE user_whitelist SET status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateUserWhitelistStatus(@Param("id") Integer id, @Param("status") String status);

    @Delete("DELETE FROM user_whitelist WHERE id = #{id}")
    int deleteUserWhitelist(@Param("id") Integer id);

    @Select("SELECT * FROM user_whitelist ORDER BY created_at DESC")
    List<UserWhitelist> getAllUserWhitelists();

    @Select("SELECT COUNT(*) FROM user_whitelist")
    long countAllUserWhitelists();

    @Select("SELECT COUNT(*) FROM user_whitelist WHERE status = #{status}")
    long countUserWhitelistsByStatus(@Param("status") String status);

    // 按条件查询白名单（支持工厂、关键词和状态筛选）
    @Select("<script>" +
            "SELECT * FROM user_whitelist " +
            "<where>" +
            "<if test='factoryId != null and factoryId != \"\" and factoryId != \"all\"'>" +
            "AND factory_id = #{factoryId}" +
            "</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (phone_number LIKE CONCAT('%', #{keyword}, '%') " +
            "OR email LIKE CONCAT('%', #{keyword}, '%') " +
            "OR identity_card LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "<if test='status != null and status != \"\" and status != \"all\"'>" +
            "<choose>" +
            "<when test='status == \"active\"'>AND status = 'PENDING'</when>" +
            "<when test='status == \"expired\"'>AND status = 'EXPIRED'</when>" +
            "<when test='status == \"suspended\"'>AND status = 'SUSPENDED'</when>" +
            "</choose>" +
            "</if>" +
            "</where>" +
            "ORDER BY created_at DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<UserWhitelist> getUserWhitelistsByCondition(Map<String, Object> params);

    // 按条件统计白名单数量
    @Select("<script>" +
            "SELECT COUNT(*) FROM user_whitelist " +
            "<where>" +
            "<if test='factoryId != null and factoryId != \"\" and factoryId != \"all\"'>" +
            "AND factory_id = #{factoryId}" +
            "</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (phone_number LIKE CONCAT('%', #{keyword}, '%') " +
            "OR email LIKE CONCAT('%', #{keyword}, '%') " +
            "OR identity_card LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "<if test='status != null and status != \"\" and status != \"all\"'>" +
            "<choose>" +
            "<when test='status == \"active\"'>AND status = 'PENDING'</when>" +
            "<when test='status == \"expired\"'>AND status = 'EXPIRED'</when>" +
            "<when test='status == \"suspended\"'>AND status = 'SUSPENDED'</when>" +
            "</choose>" +
            "</if>" +
            "</where>" +
            "</script>")
    long countUserWhitelistsByCondition(Map<String, Object> params);

    // 批量删除白名单
    @Delete("<script>" +
            "DELETE FROM user_whitelist WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int deleteUserWhitelistsByIds(@Param("ids") List<Integer> ids);

    // 清理过期的白名单记录
    @Delete("DELETE FROM user_whitelist WHERE status = 'EXPIRED'")
    int deleteExpiredUserWhitelists();

    // 清理超过过期时间但状态仍为PENDING的记录
    @Delete("DELETE FROM user_whitelist WHERE status = 'PENDING' AND expires_at < #{now}")
    int deleteExpiredPendingUserWhitelists(@Param("now") LocalDateTime now);

    // 获取白名单及其关联的工厂信息
    @Select("SELECT w.*, f.name as factory_name, u.username as added_by_username, u.full_name as added_by_full_name " +
            "FROM user_whitelist w " +
            "LEFT JOIN factory f ON w.factory_id = f.fid " +
            "LEFT JOIN user u ON w.added_by_user_id = u.uid " +
            "ORDER BY w.created_at DESC")
    List<Map<String, Object>> getUserWhitelistsWithFactoryAndUser();

    // 兼容旧版本的方法名
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
