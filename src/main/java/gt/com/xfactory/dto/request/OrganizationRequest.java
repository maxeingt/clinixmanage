package gt.com.xfactory.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 50)
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers and hyphens")
    private String slug;

    @Size(max = 250)
    private String legalName;

    @Size(max = 20)
    private String taxId;

    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 250)
    private String address;

    @Size(max = 500)
    private String logoUrl;

    private String subscriptionPlan;

    private Integer maxUsers;
}
