package gt.com.xfactory.dto.response;

import lombok.*;

import java.time.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationDto {
    private UUID id;
    private String name;
    private String slug;
    private String legalName;
    private String taxId;
    private String email;
    private String phone;
    private String address;
    private String logoUrl;
    private Boolean active;
    private String subscriptionPlan;
    private Integer maxUsers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
