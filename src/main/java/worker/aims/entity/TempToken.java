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
@Entity
@Table(name = "temp_token")
public class TempToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "type", nullable = false)
    private String type; //  PHONE_VERIFICATION, PASSWORD_RESET

    @Column(name = "factory_id", nullable = false)
    private String factoryId;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "data", columnDefinition = "json")
    private String data;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
