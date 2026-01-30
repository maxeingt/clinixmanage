package gt.com.xfactory.entity;

import gt.com.xfactory.dto.request.LocalDateAdapter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.*;

import javax.json.bind.annotation.JsonbTypeAdapter;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@ToString
@Audited
@Entity
@Table(name = "doctor")
@AllArgsConstructor
@NoArgsConstructor
public class DoctorEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;
    @JsonbTypeAdapter(LocalDateAdapter.class)
    private LocalDate birthdate;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    private String address;
    private String email;
    private String phone;
    @Column(name = "created_at")
    private LocalDate createdAt;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "updated_at")
    private LocalDate updatedAt;
    @Column(name = "updated_by")
    private String updatedBy;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private UserEntity user;

}
