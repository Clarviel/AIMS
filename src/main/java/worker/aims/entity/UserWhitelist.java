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
@Entity(name = "user_whitelist")
public class UserWhitelist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "factory_id", nullable = false)
    private String factoryId;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "status", nullable = false)
    private String status;  // PENDING, REGISTERED, EXPIRED

    @Column(name = "added_by_user_id")
    private Integer addedByUserId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "added_by_platform_id")
    private Integer addedByPlatformId;

}

