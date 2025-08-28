package worker.aims.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uid",nullable = false)
    private Integer uid;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "role")
    private String role; //factory_super_admin, permission_admin, department_admin, operator, viewer, unactivated

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "permissions")
    private String permissions;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "phone")
    private String phone;

    @Column(name = "department")
    private String department; //  farming, processing, logistics, quality, management

    @Column(name = "position")
    private String position;

    @Column(name = "factory_id")
    private String factoryId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
