package gt.com.xfactory.service.impl;

import gt.com.xfactory.entity.*;
import gt.com.xfactory.repository.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.transaction.*;
import lombok.extern.slf4j.*;

import java.util.*;

@ApplicationScoped
@Slf4j
public class FileStorageService {

    @Inject
    LabOrderAttachmentRepository attachmentRepository;

    @Transactional
    public LabOrderAttachmentEntity store(LabOrderEntity labOrder, String fileName, String contentType, long fileSize, byte[] fileData, String uploadedBy) {
        log.info("Storing file '{}' ({} bytes) for lab order: {}", fileName, fileSize, labOrder.getId());

        LabOrderAttachmentEntity attachment = new LabOrderAttachmentEntity();
        attachment.setLabOrder(labOrder);
        attachment.setFileName(fileName);
        attachment.setContentType(contentType);
        attachment.setFileSize(fileSize);
        attachment.setFileData(fileData);
        attachment.setUploadedBy(uploadedBy);

        attachmentRepository.persist(attachment);
        log.info("File stored with attachment id: {}", attachment.getId());
        return attachment;
    }

    public byte[] retrieve(LabOrderAttachmentEntity attachment) {
        log.info("Retrieving file data for attachment: {}", attachment.getId());
        return attachment.getFileData();
    }

    @Transactional
    public void delete(LabOrderAttachmentEntity attachment) {
        log.info("Deleting attachment: {}", attachment.getId());
        attachmentRepository.delete(attachment);
        log.info("Attachment deleted: {}", attachment.getId());
    }
}
