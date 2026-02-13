package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.*;
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
@Path("/api/v1/form-templates")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"user", "admin", "doctor", "secretary"})
public class SpecialtyFormTemplateController {

    @Inject
    SpecialtyFormTemplateService templateService;

    @GET
    public List<SpecialtyFormTemplateDto> getAllActive() {
        return templateService.getAllActive();
    }

    @GET
    @Path("/specialty/{specialtyId}")
    public List<SpecialtyFormTemplateDto> getBySpecialtyId(@PathParam("specialtyId") UUID specialtyId) {
        return templateService.getBySpecialtyId(specialtyId);
    }

    @GET
    @Path("/specialty/{specialtyId}/active")
    public List<SpecialtyFormTemplateDto> getActiveBySpecialtyId(@PathParam("specialtyId") UUID specialtyId) {
        return templateService.getActiveBySpecialtyId(specialtyId);
    }

    @GET
    @Path("/{id}")
    public SpecialtyFormTemplateDto getById(@PathParam("id") UUID id) {
        return templateService.getById(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public Response create(@Valid SpecialtyFormTemplateRequest request) {
        SpecialtyFormTemplateDto created = templateService.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public SpecialtyFormTemplateDto update(@PathParam("id") UUID id, @Valid SpecialtyFormTemplateRequest request) {
        return templateService.update(id, request);
    }

    @PATCH
    @Path("/{id}/status")
    @RolesAllowed("admin")
    public SpecialtyFormTemplateDto toggleStatus(@PathParam("id") UUID id) {
        return templateService.toggleStatus(id);
    }

    @POST
    @Path("/{id}/duplicate")
    @RolesAllowed("admin")
    public Response duplicate(@PathParam("id") UUID id) {
        SpecialtyFormTemplateDto duplicated = templateService.duplicate(id);
        return Response.status(Response.Status.CREATED).entity(duplicated).build();
    }
}
