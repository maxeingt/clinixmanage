package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.CommonPageRequest;
import gt.com.xfactory.dto.request.DistributorRequest;
import gt.com.xfactory.dto.request.filter.DistributorFilterDto;
import gt.com.xfactory.dto.response.DistributorDto;
import gt.com.xfactory.dto.response.PageResponse;
import gt.com.xfactory.service.impl.DistributorService;
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
@Path("/api/v1/distributors")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"user", "admin", "doctor", "secretary"})
public class DistributorController {

    @Inject
    DistributorService distributorService;

    @GET
    public PageResponse<DistributorDto> getDistributors(
            @Valid @BeanParam DistributorFilterDto filter,
            @Valid @BeanParam CommonPageRequest pageRequest) {
        return distributorService.getDistributors(filter, pageRequest);
    }

    @GET
    @Path("/active")
    public List<DistributorDto> getAllActive() {
        return distributorService.getAllActive();
    }

    @GET
    @Path("/{id}")
    public DistributorDto getById(@PathParam("id") UUID id) {
        return distributorService.getById(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public Response create(@Valid DistributorRequest request) {
        DistributorDto created = distributorService.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public DistributorDto update(@PathParam("id") UUID id, @Valid DistributorRequest request) {
        return distributorService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin")
    public Response delete(@PathParam("id") UUID id) {
        distributorService.delete(id);
        return Response.noContent().build();
    }
}
