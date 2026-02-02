package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.request.filter.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.entity.enums.*;
import gt.com.xfactory.repository.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.transaction.*;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.*;

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
    PatientRepository patientRepository;

    @Inject
    DoctorRepository doctorRepository;

    @Inject
    MedicalAppointmentRepository medicalAppointmentRepository;

    public PageResponse<LabOrderDto> getLabOrders(LabOrderFilterDto filter, CommonPageRequest pageRequest) {
        log.info("Fetching lab orders with filter");

        StringBuilder query = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        List<String> conditions = new ArrayList<>();

        if (filter.patientId != null) {
            conditions.add("patient.id = :patientId");
            params.put("patientId", filter.patientId);
        }

        if (filter.doctorId != null) {
            conditions.add("doctor.id = :doctorId");
            params.put("doctorId", filter.doctorId);
        }

        if (filter.status != null) {
            conditions.add("status = :status");
            params.put("status", filter.status);
        }

        if (filter.startDate != null) {
            conditions.add("orderDate >= :startDate");
            params.put("startDate", filter.startDate);
        }

        if (filter.endDate != null) {
            conditions.add("orderDate <= :endDate");
            params.put("endDate", filter.endDate);
        }

        if (!conditions.isEmpty()) {
            query.append(String.join(" AND ", conditions));
        }

        return toPageResponse(labOrderRepository, query, pageRequest, params, toLabOrderDto);
    }

    public List<LabOrderDto> getLabOrdersByPatientId(UUID patientId) {
        log.info("Fetching lab orders for patient: {}", patientId);

        patientRepository.findByIdOptional(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));

        return labOrderRepository.findByPatientId(patientId)
                .stream()
                .map(toLabOrderDto)
                .collect(Collectors.toList());
    }

    public LabOrderDto getLabOrderById(UUID id) {
        log.info("Fetching lab order by id: {}", id);

        return labOrderRepository.findByIdOptional(id)
                .map(toLabOrderDto)
                .orElseThrow(() -> new NotFoundException("Lab order not found with id: " + id));
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
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
}
