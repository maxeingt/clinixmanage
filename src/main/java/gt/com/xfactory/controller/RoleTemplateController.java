package gt.com.xfactory.controller;

import gt.com.xfactory.dto.response.RoleTemplateDto;
import gt.com.xfactory.service.impl.RoleTemplateService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@RequestScoped
@Path("/api/v1/role-templates")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class RoleTemplateController {

    @Inject
    RoleTemplateService roleTemplateService;

    @GET
    public List<RoleTemplateDto> getAllRoleTemplates() {
        return roleTemplateService.getAllRoleTemplates();
    }

    @GET
    @Path("/{id}")
    public RoleTemplateDto getRoleTemplateById(@PathParam("id") UUID id) {
        return roleTemplateService.getRoleTemplateById(id);
    }

    @GET
    @Path("/name/{name}")
    public RoleTemplateDto getRoleTemplateByName(@PathParam("name") String name) {
        return roleTemplateService.getRoleTemplateByName(name);
    }

    @POST
    @Path("/initialize")
    public Response initializeDefaultRoles() {
        roleTemplateService.initializeDefaultRoles();
        return Response.ok().entity("Default roles initialized").build();
    }
}
