package gt.com.xfactory.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDownloadInfo {
    private String fileName;
    private String contentType;
    private byte[] data;
}
