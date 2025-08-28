package worker.aims.mapper;

import org.apache.ibatis.annotations.*;
import worker.aims.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO user(username, password, email, role, full_name, permissions, last_login, is_active, phone, department, position, factory_id, created_at) " +
            "VALUES (#{username}, #{password}, #{email}, #{role}, #{fullName}, #{permissions}, #{lastLogin}, #{isActive}, #{phone}, #{department}, #{position}, #{factoryId}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "uid", keyColumn = "uid")
    int insertUser(User user);

    @Select("SELECT * FROM user WHERE username = #{username}")
    User getUserByUsername(@Param("username") String username);

    @Select("SELECT * FROM user WHERE email = #{email}")
    User getUserByEmail(@Param("email") String email);

    @Select("SELECT * FROM user WHERE uid = #{uid}")
    User getUserByUid(@Param("uid") Integer uid);

    @Select("SELECT * FROM user WHERE uid = #{uid} and factory_id = #{factoryId}")
    User getUserByUidAndFactoryId(Integer uid, String factoryId);

    @Update("UPDATE user SET password = #{password} WHERE uid = #{uid}")
    int updatePasswordByUid(@Param("uid") Integer uid, @Param("password") String password);

    @Update("UPDATE user SET username = #{username} WHERE uid = #{uid}")
    int updateUsernameByUid(@Param("uid") Integer uid, @Param("username") String username);

    @Update("UPDATE user SET phone = #{phone} WHERE uid = #{uid}")
    int updatePhoneByUid(@Param("uid") Integer uid, @Param("phone") String phone);

    @Update("UPDATE user SET full_name = #{fullName} WHERE uid = #{uid}")
    int updateFullNameByUid(@Param("uid") Integer uid, @Param("fullName") String fullName);

    @Update("UPDATE user SET email = #{email} WHERE uid = #{uid}")
    int updateEmailByUid(@Param("uid") Integer uid, @Param("email") String email);

    @Update("UPDATE user SET department = #{department} WHERE uid = #{uid}")
    int updateDepartmentByUid(@Param("uid") Integer uid, @Param("department") String department);

    @Update("UPDATE user SET position = #{position} WHERE uid = #{uid}")
    int updatePositionByUid(@Param("uid") Integer uid, @Param("position") String position);

    @Update("UPDATE user SET factory_id = #{factoryId} WHERE uid = #{uid}")
    int updateFactoryIdByUid(@Param("uid") Integer uid, @Param("factoryId") String FactoryId);

    @Update("UPDATE user SET permissions = #{permissions} WHERE uid = #{uid}")
    int updatePermissionsByUid(@Param("uid") Integer uid, @Param("permissions") String permissions);

    @Update("UPDATE user SET role= #{role} WHERE uid = #{uid}")
    int updateRoleByUid(@Param("uid") Integer uid, @Param("role") String role);

    @Update("UPDATE user SET last_login = #{lastLogin} WHERE uid = #{uid}")
    int updateLastLogin(@Param("uid") Integer uid, @Param("lastLogin") java.time.LocalDateTime lastLogin);

    @Update("UPDATE user SET is_active = #{isActive} WHERE uid = #{uid}")
    int updateUserActiveStatus(@Param("uid") Integer uid, @Param("isActive") Boolean isActive);

    @Delete("DELETE FROM user WHERE uid = #{uid}")
    int deleteUserByUid(@Param("uid") Integer uid);

    @Select("SELECT * FROM user")
    List<User> getAllUsers();

    @Select("SELECT * FROM user WHERE is_active = #{isActive} and factory_id = #{factoryId} ORDER BY created_at DESC")
    List<User> getPendingUsers(@Param("isActive") Boolean isActive, @Param("factoryId")  String factoryId);

    // 启用用户数量
    @Select("SELECT COUNT(*) FROM user WHERE factory_id = #{factoryId} AND is_active = 1")
    long countActiveUsers(@Param("factoryId") String factoryId);

    // 未启用用户数量
    @Select("SELECT COUNT(*) FROM user WHERE factory_id = #{factoryId} AND is_active = 0")
    long countPendingUsers(@Param("factoryId") String factoryId);

    // 最近7天登录的用户数量
    @Select("SELECT COUNT(*) FROM user WHERE factory_id = #{factoryId} AND is_active = 1 AND last_login >= #{since}")
    long countRecentLoginUsers(@Param("factoryId") String factoryId, @Param("since") LocalDateTime since);

    // 按角色分组统计
    @Select("SELECT role AS roleCode, COUNT(*) AS count FROM user " +
            "WHERE factory_id = #{factoryId} AND is_active = 1 " +
            "GROUP BY role")
    List<Map<String, Object>> groupByRole(@Param("factoryId") String factoryId);

    // 按部门分组统计
    @Select("SELECT department AS department, COUNT(*) AS count FROM user " +
            "WHERE factory_id = #{factoryId} AND is_active = 1 AND department IS NOT NULL " +
            "GROUP BY department")
    List<Map<String, Object>> groupByDepartment(@Param("factoryId") String factoryId);



}
