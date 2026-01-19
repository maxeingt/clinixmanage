package gt.com.xfactory.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@Entity
@Table(name = "patient")
@AllArgsConstructor
@NoArgsConstructor
public class PatientEntity extends PanacheEntityBase implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "name", nullable = false, length = 250)
    private String name;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 250)
    private String address;

    @Column(name = "marital_status", nullable = false, length = 75)
    private String maritalStatus;

    @Column(name = "occupation", length = 150)
    private String occupation;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
