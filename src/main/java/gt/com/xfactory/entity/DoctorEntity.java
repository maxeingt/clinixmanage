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
    @Column(name = "first_name", length = 150)
    private String firstName;
    @Column(name = "last_name", length = 150)
    private String lastName;
    @Column(length = 250)
    private String address;
    @Column(length = 150)
    private String email;
    @Column(length = 20)
    private String phone;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by", length = 255)
    private String createdBy;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private UserEntity user;

}
