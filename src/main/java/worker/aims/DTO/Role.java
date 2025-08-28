package worker.aims.DTO;

import lombok.Getter;

@Getter
public enum Role {
    DEVELOPER("developer", "系统开发者", "DEVELOPER"),
    PLATFORM_ADMIN("platform_admin", "平台管理员", "PLATFORM_ADMIN"),
    PLATFORM_SUPER_ADMIN("platform_super_admin", "平台超级管理员", "PLATFORM_ADMIN"),
    FACTORY_SUPER_ADMIN("factory_super_admin", "工厂超级管理员", "SUPER_ADMIN"),
    PERMISSION_ADMIN("permission_admin", "权限管理员", "PERMISSION_ADMIN"),
    DEPARTMENT_ADMIN("department_admin", "部门管理员", "DEPARTMENT_ADMIN"),
    USER("user", "普通用户", "USER"),
    UNACTIVATED("unactivated", "待激活用户", "USER");

    private final String code;
    private final String displayName;
    private final String roleName;

    Role(String code, String displayName, String roleName) {
        this.code = code;
        this.displayName = displayName;
        this.roleName = roleName;
    }

    public static Role fromCode(String code) {
        for (Role r : values()) {
            if (r.code.equalsIgnoreCase(code)) {
                return r;
            }
        }
        return USER; // 默认
    }
}

