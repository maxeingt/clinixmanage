package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.request.filter.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.service.impl.*;
import jakarta.annotation.security.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.*;

@RequestScoped
@Path("/api/v1/lab-orders")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"user", "admin", "doctor"})
public class LabOrderController {

    @Inject
    LabOrderService labOrderService;

    @Inject
    PdfService pdfService;

    @GET
    public PageResponse<LabOrderDto> getLabOrders(
            @BeanParam LabOrderFilterDto filter,
            @BeanParam CommonPageRequest pageRequest) {
        return labOrderService.getLabOrders(filter, pageRequest);
    }

    @GET
    @Path("/{id}")
    public LabOrderDto getLabOrderById(@PathParam("id") UUID id) {
        return labOrderService.getLabOrderById(id);
    }

    @GET
    @Path("/patient/{patientId}")
    public List<LabOrderDto> getLabOrdersByPatientId(@PathParam("patientId") UUID patientId) {
        return labOrderService.getLabOrdersByPatientId(patientId);
    }

    @GET
    @Path("/{id}/pdf")
    @Produces("application/pdf")
    public Response getLabOrderPdf(@PathParam("id") UUID id) {
        LabOrderDto labOrder = labOrderService.getLabOrderById(id);
        byte[] pdf = pdfService.generateLabOrderPdf(labOrder);
        return Response.ok(pdf, "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"orden-laboratorio-" + id + ".pdf\"")
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"admin", "doctor"})
    public Response createLabOrder(@Valid LabOrderRequest request) {
        LabOrderDto created = labOrderService.createLabOrder(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"admin", "doctor"})
    public LabOrderDto updateLabOrder(@PathParam("id") UUID id, @Valid LabOrderRequest request) {
        return labOrderService.updateLabOrder(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"admin", "doctor"})
    public Response deleteLabOrder(@PathParam("id") UUID id) {
        labOrderService.deleteLabOrder(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{orderId}/results")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"admin", "doctor"})
    public Response addResult(@PathParam("orderId") UUID orderId, @Valid LabResultRequest request) {
        LabResultDto created = labOrderService.addResult(orderId, request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/results/{resultId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"admin", "doctor"})
    public LabResultDto updateResult(@PathParam("resultId") UUID resultId, @Valid LabResultRequest request) {
        return labOrderService.updateResult(resultId, request);
    }

    @DELETE
    @Path("/results/{resultId}")
    @RolesAllowed({"admin", "doctor"})
    public Response deleteResult(@PathParam("resultId") UUID resultId) {
        labOrderService.deleteResult(resultId);
        return Response.noContent().build();
    }
}
