package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.request.filter.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.entity.enums.*;
import gt.com.xfactory.repository.*;
import gt.com.xfactory.utils.*;
import io.quarkus.security.identity.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.transaction.*;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.*;
import org.eclipse.microprofile.jwt.*;
import org.jboss.resteasy.reactive.multipart.*;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static gt.com.xfactory.dto.response.PageResponse.toPageResponse;

@ApplicationScoped
@Slf4j
public class LabOrderService {

    @Inject
    LabOrderRepository labOrderRepository;

    @Inject
    LabResultRepository labResultRepository;

    @Inject
    LabOrderAttachmentRepository labOrderAttachmentRepository;

    @Inject
    FileStorageService fileStorageService;

    @Inject
    PatientRepository patientRepository;

    @Inject
    DoctorRepository doctorRepository;

    @Inject
    MedicalAppointmentRepository medicalAppointmentRepository;

    @Inject
    SecurityContextService securityContextService;

    private UUID getCurrentDoctorId() {
        return securityContextService.getCurrentDoctorId();
    }

    private boolean isSecretary() {
        return securityContextService.hasRole("secretary") && !securityContextService.hasRole("admin");
    }

    private LabOrderDto mapToDto(LabOrderEntity entity) {
        LabOrderDto dto = toLabOrderDto.apply(entity);
        if (isSecretary()) {
            dto.setResults(null);
            dto.setAttachments(null);
        }
        return dto;
    }

    public PageResponse<LabOrderDto> getLabOrders(LabOrderFilterDto filter, CommonPageRequest pageRequest) {
        log.info("Fetching lab orders with filter");

        UUID currentDoctorId = getCurrentDoctorId();
        var fb = FilterBuilder.create()
                .addEquals(currentDoctorId, "doctor.id", "currentDoctorId")
                .addEquals(filter.patientId, "patient.id", "patientId")
                .addEquals(filter.doctorId, "doctor.id", "doctorId")
                .addEquals(filter.status, "status")
                .addDateRange(filter.startDate, "orderDate", "startDate",
                              filter.endDate, "orderDate", "endDate");

        return toPageResponse(labOrderRepository, fb.buildQuery(), pageRequest, fb.getParams(), this::mapToDto);
    }

    public List<LabOrderDto> getLabOrdersByPatientId(UUID patientId) {
        log.info("Fetching lab orders for patient: {}", patientId);

        patientRepository.findByIdOptional(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));

        UUID currentDoctorId = getCurrentDoctorId();
        List<LabOrderEntity> orders = currentDoctorId != null
                ? labOrderRepository.findByPatientIdAndDoctorId(patientId, currentDoctorId)
                : labOrderRepository.findByPatientId(patientId);
        return orders.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public LabOrderDto getLabOrderById(UUID id) {
        log.info("Fetching lab order by id: {}", id);

        LabOrderEntity order = labOrderRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Lab order not found with id: " + id));

        UUID currentDoctorId = getCurrentDoctorId();
        if (currentDoctorId != null && !order.getDoctor().getId().equals(currentDoctorId)) {
            throw new ForbiddenException("No tiene acceso a esta orden de laboratorio");
        }

        return mapToDto(order);
    }

    @Transactional
    public LabOrderDto createLabOrder(LabOrderRequest request) {
        log.info("Creating lab order for patient: {}", request.getPatientId());

        var patient = patientRepository.findByIdOptional(request.getPatientId())
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + request.getPatientId()));

        var doctor = doctorRepository.findByIdOptional(request.getDoctorId())
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + request.getDoctorId()));

        LabOrderEntity order = new LabOrderEntity();
        order.setPatient(patient);
        order.setDoctor(doctor);
        order.setNotes(request.getNotes());

        if (request.getAppointmentId() != null) {
            var appointment = medicalAppointmentRepository.findByIdOptional(request.getAppointmentId())
                    .orElseThrow(() -> new NotFoundException("Appointment not found with id: " + request.getAppointmentId()));
            order.setAppointment(appointment);
        }

        labOrderRepository.persist(order);

        if (request.getResults() != null && !request.getResults().isEmpty()) {
            for (LabResultRequest resultRequest : request.getResults()) {
                LabResultEntity result = mapResultRequestToEntity(resultRequest, order);
                labResultRepository.persist(result);
                order.getResults().add(result);
            }
        }

        log.info("Lab order created with id: {}", order.getId());
        return toLabOrderDto.apply(order);
    }

    @Transactional
    public LabOrderDto updateLabOrder(UUID id, LabOrderRequest request) {
        log.info("Updating lab order: {}", id);

        var order = labOrderRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Lab order not found with id: " + id));

        if (request.getNotes() != null) order.setNotes(request.getNotes());

        if (request.getAppointmentId() != null) {
            var appointment = medicalAppointmentRepository.findByIdOptional(request.getAppointmentId())
                    .orElseThrow(() -> new NotFoundException("Appointment not found with id: " + request.getAppointmentId()));
            order.setAppointment(appointment);
        }

        labOrderRepository.persist(order);
        log.info("Lab order updated: {}", id);

        return toLabOrderDto.apply(order);
    }

    @Transactional
    public void deleteLabOrder(UUID id) {
        log.info("Deleting lab order: {}", id);

        var order = labOrderRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Lab order not found with id: " + id));

        labOrderRepository.delete(order);
        log.info("Lab order deleted: {}", id);
    }

    @Transactional
    public LabResultDto addResult(UUID orderId, LabResultRequest request) {
        log.info("Adding result to lab order: {}", orderId);

        var order = labOrderRepository.findByIdOptional(orderId)
                .orElseThrow(() -> new NotFoundException("Lab order not found with id: " + orderId));

        LabResultEntity result = mapResultRequestToEntity(request, order);
        labResultRepository.persist(result);

        log.info("Lab result created with id: {}", result.getId());
        return toLabResultDto.apply(result);
    }

    @Transactional
    public LabResultDto updateResult(UUID resultId, LabResultRequest request) {
        log.info("Updating lab result: {}", resultId);

        var result = labResultRepository.findByIdOptional(resultId)
                .orElseThrow(() -> new NotFoundException("Lab result not found with id: " + resultId));

        if (request.getTestName() != null) result.setTestName(request.getTestName());
        if (request.getTestCode() != null) result.setTestCode(request.getTestCode());
        if (request.getValue() != null) result.setValue(request.getValue());
        if (request.getUnit() != null) result.setUnit(request.getUnit());
        if (request.getReferenceMin() != null) result.setReferenceMin(request.getReferenceMin());
        if (request.getReferenceMax() != null) result.setReferenceMax(request.getReferenceMax());
        if (request.getIsAbnormal() != null) result.setIsAbnormal(request.getIsAbnormal());
        if (request.getResultDate() != null) result.setResultDate(request.getResultDate());

        labResultRepository.persist(result);
        log.info("Lab result updated: {}", resultId);

        return toLabResultDto.apply(result);
    }

    @Transactional
    public void deleteResult(UUID resultId) {
        log.info("Deleting lab result: {}", resultId);

        var result = labResultRepository.findByIdOptional(resultId)
                .orElseThrow(() -> new NotFoundException("Lab result not found with id: " + resultId));

        labResultRepository.delete(result);
        log.info("Lab result deleted: {}", resultId);
    }

    private LabResultEntity mapResultRequestToEntity(LabResultRequest request, LabOrderEntity order) {
        LabResultEntity result = new LabResultEntity();
        result.setLabOrder(order);
        result.setTestName(request.getTestName());
        result.setTestCode(request.getTestCode());
        result.setValue(request.getValue());
        result.setUnit(request.getUnit());
        result.setReferenceMin(request.getReferenceMin());
        result.setReferenceMax(request.getReferenceMax());
        result.setIsAbnormal(request.getIsAbnormal() != null ? request.getIsAbnormal() : false);
        result.setResultDate(request.getResultDate());
        return result;
    }

    // Mappers

    public static final Function<LabResultEntity, LabResultDto> toLabResultDto = entity ->
            LabResultDto.builder()
                    .id(entity.getId())
                    .labOrderId(entity.getLabOrder().getId())
                    .testName(entity.getTestName())
                    .testCode(entity.getTestCode())
                    .value(entity.getValue())
                    .unit(entity.getUnit())
                    .referenceMin(entity.getReferenceMin())
                    .referenceMax(entity.getReferenceMax())
                    .isAbnormal(entity.getIsAbnormal())
                    .resultDate(entity.getResultDate())
                    .build();

    public static final Function<LabOrderAttachmentEntity, LabOrderAttachmentDto> toAttachmentDto = entity ->
            LabOrderAttachmentDto.builder()
                    .id(entity.getId())
                    .labOrderId(entity.getLabOrder().getId())
                    .fileName(entity.getFileName())
                    .contentType(entity.getContentType())
                    .fileSize(entity.getFileSize())
                    .uploadedBy(entity.getUploadedBy())
                    .createdAt(entity.getCreatedAt())
                    .build();

    public static final Function<LabOrderEntity, LabOrderDto> toLabOrderDto = entity ->
            LabOrderDto.builder()
                    .id(entity.getId())
                    .appointmentId(entity.getAppointment() != null ? entity.getAppointment().getId() : null)
                    .patientId(entity.getPatient().getId())
                    .patientName(entity.getPatient().getFirstName() + " " + entity.getPatient().getLastName())
                    .doctorId(entity.getDoctor().getId())
                    .doctorName(entity.getDoctor().getFirstName() + " " + entity.getDoctor().getLastName())
                    .orderDate(entity.getOrderDate())
                    .status(entity.getStatus())
                    .notes(entity.getNotes())
                    .results(entity.getResults() != null ?
                            entity.getResults().stream()
                                    .map(LabOrderService.toLabResultDto)
                                    .collect(Collectors.toList()) : null)
                    .attachments(entity.getAttachments() != null ?
                            entity.getAttachments().stream()
                                    .map(LabOrderService.toAttachmentDto)
                                    .collect(Collectors.toList()) : null)
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();

    // Attachment methods

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf", "image/jpeg", "image/png"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private void validateDoctorAccess(LabOrderEntity order) {
        UUID currentDoctorId = getCurrentDoctorId();
        if (currentDoctorId != null && !order.getDoctor().getId().equals(currentDoctorId)) {
            throw new ForbiddenException("No tiene acceso a esta orden de laboratorio");
        }
    }

    @Transactional
    public LabOrderAttachmentDto uploadAttachment(UUID orderId, FileUpload file) {
        log.info("Uploading attachment to lab order: {}", orderId);

        LabOrderEntity order = labOrderRepository.findByIdOptional(orderId)
                .orElseThrow(() -> new NotFoundException("Lab order not found with id: " + orderId));

        validateDoctorAccess(order);

        String contentType = file.contentType();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Tipo de archivo no permitido. Solo se aceptan: PDF, JPG, PNG");
        }

        long fileSize = file.size();
        if (fileSize > MAX_FILE_SIZE) {
            throw new BadRequestException("El archivo excede el tamaño máximo permitido de 10MB");
        }

        byte[] fileData;
        try {
            fileData = java.nio.file.Files.readAllBytes(file.filePath());
        } catch (IOException e) {
            log.error("Error reading uploaded file", e);
            throw new InternalServerErrorException("Error al leer el archivo subido");
        }

        String uploadedBy = securityContextService.getUserName();
        LabOrderAttachmentEntity attachment = fileStorageService.store(
                order, file.fileName(), contentType, fileSize, fileData, uploadedBy
        );

        log.info("Attachment uploaded with id: {}", attachment.getId());
        return toAttachmentDto.apply(attachment);
    }

    public AttachmentDownloadInfo getAttachmentDownload(UUID attachmentId) {
        log.info("Downloading attachment: {}", attachmentId);
        LabOrderAttachmentEntity attachment = getAttachmentEntity(attachmentId);
        byte[] data = fileStorageService.retrieve(attachment);
        return AttachmentDownloadInfo.builder()
                .fileName(attachment.getFileName())
                .contentType(attachment.getContentType())
                .data(data)
                .build();
    }

    private LabOrderAttachmentEntity getAttachmentEntity(UUID attachmentId) {
        LabOrderAttachmentEntity attachment = labOrderAttachmentRepository.findByIdOptional(attachmentId)
                .orElseThrow(() -> new NotFoundException("Attachment not found with id: " + attachmentId));

        validateDoctorAccess(attachment.getLabOrder());
        return attachment;
    }

    @Transactional
    public void deleteAttachment(UUID attachmentId) {
        log.info("Deleting attachment: {}", attachmentId);
        LabOrderAttachmentEntity attachment = getAttachmentEntity(attachmentId);
        fileStorageService.delete(attachment);
        log.info("Attachment deleted: {}", attachmentId);
    }
}
