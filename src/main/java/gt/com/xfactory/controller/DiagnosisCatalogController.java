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
@Path("/api/v1/diagnosis-catalog")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "doctor"})
public class DiagnosisCatalogController {

    @Inject
    DiagnosisCatalogService diagnosisCatalogService;

    @GET
    public PageResponse<DiagnosisCatalogDto> search(
            @Valid @BeanParam DiagnosisCatalogFilterDto filter,
            @Valid @BeanParam CommonPageRequest pageRequest) {
        return diagnosisCatalogService.search(filter, pageRequest);
    }

    @GET
    @Path("/{id}")
    public DiagnosisCatalogDto getById(@PathParam("id") UUID id) {
        return diagnosisCatalogService.getById(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public Response create(@Valid DiagnosisCatalogRequest request) {
        DiagnosisCatalogDto created = diagnosisCatalogService.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public DiagnosisCatalogDto update(@PathParam("id") UUID id, @Valid DiagnosisCatalogRequest request) {
        return diagnosisCatalogService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin")
    public Response delete(@PathParam("id") UUID id) {
        diagnosisCatalogService.delete(id);
        return Response.noContent().build();
    }
}
