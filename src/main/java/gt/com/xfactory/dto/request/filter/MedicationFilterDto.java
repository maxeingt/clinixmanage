package gt.com.xfactory.dto.request.filter;

import gt.com.xfactory.entity.enums.PresentationType;
import jakarta.ws.rs.QueryParam;
import lombok.Data;

import java.util.UUID;

@Data
public class MedicationFilterDto {

    @QueryParam("search")
    public String search;

    @QueryParam("name")
    public String name;

    @QueryParam("code")
    public String code;

    @QueryParam("activeIngredient")
    public String activeIngredient;

    @QueryParam("presentation")
    public PresentationType presentation;

    @QueryParam("pharmaceuticalId")
    public UUID pharmaceuticalId;

    @QueryParam("distributorId")
    public UUID distributorId;

    @QueryParam("active")
    public Boolean active;
}
