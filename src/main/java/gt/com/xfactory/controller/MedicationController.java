package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.CommonPageRequest;
import gt.com.xfactory.dto.request.MedicationRequest;
import gt.com.xfactory.dto.request.filter.MedicationFilterDto;
import gt.com.xfactory.dto.response.MedicationDto;
import gt.com.xfactory.dto.response.PageResponse;
import gt.com.xfactory.service.impl.MedicationService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@RequestScoped
@Path("/api/v1/medications")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "doctor", "secretary"})
public class MedicationController {

    @Inject
    MedicationService medicationService;

    @GET
    public PageResponse<MedicationDto> getMedications(
            @Valid @BeanParam MedicationFilterDto filter,
            @Valid @BeanParam CommonPageRequest pageRequest) {
        return medicationService.getMedications(filter, pageRequest);
    }

    @GET
    @Path("/active")
    public List<MedicationDto> getAllActive() {
        return medicationService.getAllActive();
    }

    @GET
    @Path("/{id}")
    public MedicationDto getById(@PathParam("id") UUID id) {
        return medicationService.getById(id);
    }

    @GET
    @Path("/search")
    public List<MedicationDto> searchByActiveIngredient(@QueryParam("ingredient") String ingredient) {
        return medicationService.searchByActiveIngredient(ingredient);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public Response create(@Valid MedicationRequest request) {
        MedicationDto created = medicationService.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public MedicationDto update(@PathParam("id") UUID id, @Valid MedicationRequest request) {
        return medicationService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin")
    public Response delete(@PathParam("id") UUID id) {
        medicationService.delete(id);
        return Response.noContent().build();
    }
}
