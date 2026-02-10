package gt.com.xfactory.entity;

import io.quarkus.hibernate.orm.panache.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.*;

import java.io.*;
import java.time.*;
import java.util.*;

@Getter
@Setter
@ToString
@Audited
@Entity
@Table(name = "doctor")
@AllArgsConstructor
@NoArgsConstructor
public class DoctorEntity extends PanacheEntityBase implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;
    private LocalDate birthdate;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    private String address;
    private String email;
    private String phone;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private String updatedBy;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private UserEntity user;

}
