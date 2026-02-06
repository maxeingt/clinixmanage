package gt.com.xfactory.dto.response;

import lombok.*;

import java.io.*;
import java.time.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabOrderAttachmentDto implements Serializable {
    private UUID id;
    private UUID labOrderId;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String uploadedBy;
    private LocalDateTime createdAt;
}
