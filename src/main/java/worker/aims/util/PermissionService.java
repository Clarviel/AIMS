package worker.aims.util;

import org.springframework.stereotype.Service;
import worker.aims.DTO.Permissions;
import worker.aims.DTO.Role;

import java.util.*;

@Service
public class PermissionService {

    /**
     * 角色代码映射到系统角色名
     */
    public String mapRoleCodeToRoleName(String roleCode) {
        return Role.fromCode(roleCode).getRoleName();
    }

    /**
     * 获取角色显示名
     */
    public String getRoleDisplayName(String roleCode) {
        return Role.fromCode(roleCode).getDisplayName();
    }

    /**
     * 平台用户权限生成
     */
    public Permissions generatePlatformUserPermissions(String roleCode) {
        Permissions p = new Permissions();
        if ("system_developer".equals(roleCode)) {
            Map<String, Boolean> modules = new HashMap<>();
            modules.put("farming_access", true);
            modules.put("processing_access", true);
            modules.put("logistics_access", true);
            modules.put("trace_access", true);
            modules.put("admin_access", true);
            modules.put("platform_access", true);
            modules.put("debug_access", true);
            modules.put("system_config", true);
            p.setModules(modules);
            List<String> features = Arrays.asList(
                    "user_manage_all",
                    "whitelist_manage_all",
                    "stats_view_all",
                    "developer_debug_access",
                    "developer_system_config",
                    "developer_data_export",
                    "developer_cross_platform",
                    "all_factories_access"
            );
            p.setFeatures(features);
            p.setRole("DEVELOPER");
            p.setUserType("platform");
            return p;
        }

        if ("platform_super_admin".equals(roleCode)) {
            Map<String, Boolean> modules = new HashMap<>();
            modules.put("farming_access", false);
            modules.put("processing_access", false);
            modules.put("logistics_access", false);
            modules.put("trace_access", false);
            modules.put("admin_access", false);
            modules.put("platform_access", true);
            p.setModules(modules);
            List<String> features = Arrays.asList(
                    "platform_manage_all",
                    "factory_manage_all",
                    "user_manage_all"
            );
            p.setFeatures(features);
            p.setRole("PLATFORM_ADMIN");
            p.setUserType("platform");
            return p;
        }

        // 默认：平台操作员
        Map<String, Boolean> modules = new HashMap<>();
        modules.put("farming_access", false);
        modules.put("processing_access", false);
        modules.put("logistics_access", false);
        modules.put("trace_access", false);
        modules.put("admin_access", false);
        modules.put("platform_access", true);
        p.setModules(modules);
        List<String> features = Collections.singletonList("platform_view_only");
        p.setFeatures(features);
        p.setRole("PLATFORM_OPERATOR");
        p.setUserType("platform");
        return p;
    }

    /**
     * 工厂/普通用户权限生成
     */
    public Permissions generateUserPermissions(String roleCode, String department, String position) {
        Permissions p = new Permissions();
        switch (roleCode) {
            case "developer":
                Map<String, Boolean> modules = new HashMap<>();
                modules.put("farming_access", true);
                modules.put("processing_access", true);
                modules.put("logistics_access", true);
                modules.put("trace_access", true);
                modules.put("admin_access", true);
                modules.put("platform_access", true);
                p.setModules(modules);
                List<String> features = Arrays.asList(
                        "user_manage_all",
                        "whitelist_manage_all",
                        "stats_view_all",
                        "developer_debug_access",
                        "developer_system_config",
                        "developer_data_export",
                        "developer_cross_platform"
                );
                p.setFeatures(features);
                p.setRole("DEVELOPER");
                p.setRoleLevel(-1);
                p.setDepartment(department);
                break;

            case "platform_admin":
                Map<String, Boolean> modules_1 = new HashMap<>();
                modules_1.put("farming_access", false);
                modules_1.put("processing_access", false);
                modules_1.put("logistics_access", false);
                modules_1.put("trace_access", false);
                modules_1.put("admin_access", false);
                modules_1.put("platform_access", true);
                p.setModules(modules_1);
                List<String> features_1 = Collections.singletonList("platform_manage_all");
                p.setFeatures(features_1);
                p.setRole("PLATFORM_ADMIN");
                p.setRoleLevel(0);
                p.setDepartment(null);
                break;

            case "factory_super_admin":
                if ("SYSTEM_DEVELOPER".equals(position)) {
                    // 特殊处理：开发者
                    Map<String, Boolean> modules_2 = new HashMap<>();
                    modules_2.put("farming_access", true);
                    modules_2.put("processing_access", true);
                    modules_2.put("logistics_access", true);
                    modules_2.put("trace_access", true);
                    modules_2.put("admin_access", true);
                    modules_2.put("platform_access", true);
                    List<String> features_2 = Arrays.asList(
                            "user_manage_all",
                            "whitelist_manage_all",
                            "stats_view_all",
                            "developer_debug_access",
                            "developer_system_config",
                            "developer_data_export",
                            "developer_cross_platform"
                    );
                    p.setModules(modules_2);
                    p.setFeatures(features_2);
                    p.setRole("DEVELOPER");
                    p.setRoleLevel(-1);
                    p.setDepartment(department);
                    break;
                }

                Map<String, Boolean> modules_3 = new HashMap<>();
                modules_3.put("farming_access", true);
                modules_3.put("processing_access", true);
                modules_3.put("logistics_access", true);
                modules_3.put("trace_access", true);
                modules_3.put("admin_access", true);
                modules_3.put("platform_access", false);
                p.setModules(modules_3);
                List<String> features_3 = Arrays.asList(
                        "user_manage_all",
                        "whitelist_manage_all",
                        "stats_view_all"
                );
                p.setFeatures(features_3);
                p.setRole("SUPER_ADMIN");
                p.setRoleLevel(0);
                p.setDepartment(department);
                break;

            case "permission_admin":
                Map<String, Boolean> modules_4 = new HashMap<>();
                modules_4.put("farming_access", false);
                modules_4.put("processing_access", false);
                modules_4.put("logistics_access", false);
                modules_4.put("trace_access", true);
                modules_4.put("admin_access", true);
                modules_4.put("platform_access", false);
                p.setModules(modules_4);
                List<String> features_4 = Arrays.asList(
                        "user_manage_all",
                        "stats_view_all"
                );
                p.setFeatures(features_4);
                p.setRole("PERMISSION_ADMIN");
                p.setRoleLevel(5);
                p.setDepartment(department);
                break;

            case "department_admin":
                Map<String, Boolean> modules_5 = new HashMap<>();
                modules_5.put("farming_access", "farming".equals(department));
                modules_5.put("processing_access", "processing".equals(department));
                modules_5.put("logistics_access", "logistics".equals(department));
                modules_5.put("trace_access", true);
                modules_5.put("admin_access", false);
                modules_5.put("platform_access", false);
                p.setModules(modules_5);
                List<String> features_5 = Arrays.asList(
                        "user_manage_own_dept",
                        "stats_view_own_dept"
                );
                p.setFeatures(features_5);
                p.setRole("DEPARTMENT_ADMIN");
                p.setRoleLevel(10);
                p.setDepartment(department);
                break;

            default: // 普通用户
                Map<String, Boolean> modules_6 = new HashMap<>();
                modules_6.put("farming_access", "farming".equals(department));
                modules_6.put("processing_access", "processing".equals(department));
                modules_6.put("logistics_access", "logistics".equals(department));
                modules_6.put("trace_access", true);
                modules_6.put("admin_access", false);
                modules_6.put("platform_access", false);
                p.setModules(modules_6);
                p.setFeatures(new ArrayList<>());
                p.setRole("USER");
                p.setRoleLevel(50);
                p.setDepartment(department);
                break;
        }

        return p;
    }
}

