package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.request.filter.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.service.impl.*;
import gt.com.xfactory.utils.*;
import jakarta.annotation.security.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.*;

@RequestScoped
@Path("/api/v1/organizations")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("super_admin")
public class OrganizationController {

    @Inject
    OrganizationService organizationService;

    @Inject
    UserService userService;

    @Inject
    TenantContext tenantContext;

    @GET
    public PageResponse<OrganizationDto> getOrganizations(
            @Valid @BeanParam CommonPageRequest pageRequest) {
        return organizationService.getOrganizations(pageRequest);
    }

    @GET
    @Path("/{id}")
    public OrganizationDto getById(@PathParam("id") UUID id) {
        return organizationService.getById(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Valid OrganizationRequest request) {
        OrganizationDto created = organizationService.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/{orgId}/users")
    public PageResponse<UserDto> getUsersByOrganization(
            @PathParam("orgId") UUID orgId,
            @Valid @BeanParam CommonPageRequest pageRequest
    ) {
        tenantContext.set(orgId.toString());
        return userService.getUsersPaginated(new UserFilterDto(), pageRequest);
    }

    @POST
    @Path("/{orgId}/admins")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAdminForOrganization(
            @PathParam("orgId") UUID orgId,
            @Valid AdminRequest request
    ) {
        tenantContext.set(orgId.toString());
        UserDto createdAdmin = userService.createAdminForOrganization(orgId, request);
        return Response.status(Response.Status.CREATED)
                .entity(createdAdmin)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public OrganizationDto update(@PathParam("id") UUID id,
                                  @Valid OrganizationRequest request) {
        return organizationService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        organizationService.delete(id);
        return Response.noContent().build();
    }
}
