package gt.com.xfactory.controller;

import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.service.impl.*;
import jakarta.annotation.security.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.*;

@RequestScoped
@Path("/api/v1/search")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "doctor", "secretary"})
public class SearchController {

    @Inject
    SearchService searchService;

    @GET
    public GlobalSearchDto search(
            @QueryParam("q") String q,
            @QueryParam("types") List<String> types) {
        return searchService.search(q, types);
    }
}
