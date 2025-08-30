package worker.aims.mapper;

import org.apache.ibatis.annotations.*;
import worker.aims.entity.Factory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface FactoryMapper {

    @Insert("INSERT INTO factory(fid, name, industry, address, description, employee_count, subscription_plan, contact_name, contact_phone, contact_email, is_active, created_at, updated_at, confidence, factory_year, industry_code, inference_data, legacy_id, manually_verified, region_code, sequence_number) " +
            "VALUES (#{fid}, #{name}, #{industry}, #{address}, #{description}, #{employeeCount}, #{subscriptionPlan}, #{contactName}, #{contactPhone}, #{contactEmail}, #{isActive}, #{createdAt}, #{updatedAt}, #{confidence}, #{factoryYear}, #{industryCode}, #{inferenceData}, #{legacyId}, #{manuallyVerified}, #{regionCode}, #{sequenceNumber})")
    int insertFactory(Factory factory);

    @Select("SELECT * FROM factory WHERE fid = #{fid}")
    Factory getFactoryById(@Param("fid") String fid);

    @Select("SELECT * FROM factory WHERE fid = #{fid}")
    Factory getFactoryByFid(@Param("fid") String fid);

    @Select("SELECT * FROM factory WHERE name = #{name}")
    Factory getFactoryByName(@Param("name") String name);

    @Select("SELECT * FROM factory WHERE contact_email = #{contactEmail}")
    Factory getFactoryByContactEmail(@Param("contactEmail") String contactEmail);

    @Update("UPDATE factory SET name = #{name}, industry = #{industry}, address = #{address}, description = #{description}, employee_count = #{employeeCount}, subscription_plan = #{subscriptionPlan}, contact_name = #{contactName}, contact_phone = #{contactPhone}, contact_email = #{contactEmail}, updated_at = #{updatedAt} WHERE fid = #{fid}")
    int updateFactory(Factory factory);

    @Update("UPDATE factory SET is_active = #{isActive}, updated_at = #{updatedAt} WHERE fid = #{fid}")
    int updateFactoryStatus(@Param("fid") String fid, @Param("isActive") Boolean isActive, @Param("updatedAt") LocalDateTime updatedAt);

    @Delete("DELETE FROM factory WHERE fid = #{fid}")
    int deleteFactory(@Param("fid") String fid);

    @Select("SELECT * FROM factory ORDER BY created_at DESC")
    List<Factory> getAllFactories();

    @Select("SELECT COUNT(*) FROM factory")
    long countAllFactories();

    @Select("SELECT COUNT(*) FROM factory WHERE is_active = 1")
    long countActiveFactories();

    @Select("SELECT COUNT(*) FROM factory WHERE is_active = 0")
    long countPendingFactories();

    @Select("SELECT COUNT(*) FROM factory WHERE created_at >= #{since}")
    long countFactoriesCreatedAfter(@Param("since") LocalDateTime since);

    // 按条件查询工厂（支持关键词和状态筛选）
    @Select("<script>" +
            "SELECT * FROM factory " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR contact_email LIKE CONCAT('%', #{keyword}, '%') " +
            "OR contact_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR address LIKE CONCAT('%', #{keyword}, '%') " +
            "OR industry LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "<choose>" +
            "<when test='status == \"active\"'>AND is_active = 1</when>" +
            "<when test='status == \"suspended\"'>AND is_active = 0</when>" +
            "</choose>" +
            "</if>" +
            "</where>" +
            "ORDER BY created_at DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<Factory> getFactoriesByCondition(Map<String, Object> params);

    // 按条件统计工厂数量
    @Select("<script>" +
            "SELECT COUNT(*) FROM factory " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR contact_email LIKE CONCAT('%', #{keyword}, '%') " +
            "OR contact_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR address LIKE CONCAT('%', #{keyword}, '%') " +
            "OR industry LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "<choose>" +
            "<when test='status == \"active\"'>AND is_active = 1</when>" +
            "<when test='status == \"suspended\"'>AND is_active = 0</when>" +
            "</choose>" +
            "</if>" +
            "</where>" +
            "</script>")
    long countFactoriesByCondition(Map<String, Object> params);

    // 按行业统计工厂数量
    @Select("SELECT industry, COUNT(*) as count FROM factory WHERE is_active = 1 GROUP BY industry")
    List<Map<String, Object>> groupByIndustry();

    // 获取今天新增的工厂数量
    @Select("SELECT COUNT(*) FROM factory WHERE DATE(created_at) = CURDATE()")
    long countTodayAddedFactories();

    // 获取工厂及其用户数量
    @Select("SELECT f.*, COUNT(u.uid) as user_count FROM factory f LEFT JOIN user u ON f.fid = u.factory_id GROUP BY f.fid ORDER BY f.created_at DESC")
    List<Map<String, Object>> getFactoriesWithUserCount();
}
