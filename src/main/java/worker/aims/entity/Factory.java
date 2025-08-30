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
@Table(name = "factory")
public class Factory {

    @Id
    @Column(name = "fid", nullable = false)
    private String fid;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "industry")
    private String industry;

    @Column(name = "address")
    private String address;

    @Column(name = "description")
    private String description;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Column(name = "subscription_plan")
    private String subscriptionPlan;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "confidence")
    private Float confidence;

    @Column(name = "factory_year")
    private Integer factoryYear;

    @Column(name = "industry_code")
    private String industryCode;

    @Column(name = "inference_data", columnDefinition = "json")
    private String inferenceData;

    @Column(name = "legacy_id")
    private String legacyId;

    @Column(name = "manually_verified")
    private Boolean manuallyVerified = false;

    @Column(name = "region_code")
    private String regionCode;

    @Column(name = "sequence_number")
    private Integer sequenceNumber;


}
