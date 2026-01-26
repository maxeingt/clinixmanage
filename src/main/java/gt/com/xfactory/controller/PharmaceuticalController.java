package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.CommonPageRequest;
import gt.com.xfactory.dto.request.PharmaceuticalRequest;
import gt.com.xfactory.dto.request.filter.PharmaceuticalFilterDto;
import gt.com.xfactory.dto.response.PageResponse;
import gt.com.xfactory.dto.response.PharmaceuticalDto;
import gt.com.xfactory.service.impl.PharmaceuticalService;
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
@Path("/api/v1/pharmaceuticals")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"user", "admin", "doctor", "secretary"})
public class PharmaceuticalController {

    @Inject
    PharmaceuticalService pharmaceuticalService;

    @GET
    public PageResponse<PharmaceuticalDto> getPharmaceuticals(
            @Valid @BeanParam PharmaceuticalFilterDto filter,
            @Valid @BeanParam CommonPageRequest pageRequest) {
        return pharmaceuticalService.getPharmaceuticals(filter, pageRequest);
    }

    @GET
    @Path("/active")
    public List<PharmaceuticalDto> getAllActive() {
        return pharmaceuticalService.getAllActive();
    }

    @GET
    @Path("/{id}")
    public PharmaceuticalDto getById(@PathParam("id") UUID id) {
        return pharmaceuticalService.getById(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Valid PharmaceuticalRequest request) {
        PharmaceuticalDto created = pharmaceuticalService.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public PharmaceuticalDto update(@PathParam("id") UUID id, @Valid PharmaceuticalRequest request) {
        return pharmaceuticalService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        pharmaceuticalService.delete(id);
        return Response.noContent().build();
    }
}
