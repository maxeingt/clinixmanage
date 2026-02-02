package gt.com.xfactory.dto.response;

import lombok.*;

import java.io.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisCatalogDto implements Serializable {
    private UUID id;
    private String code;
    private String name;
    private String category;
    private String chapter;
}
