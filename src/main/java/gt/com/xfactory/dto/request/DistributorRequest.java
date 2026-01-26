package gt.com.xfactory.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributorRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private Boolean active;
}
