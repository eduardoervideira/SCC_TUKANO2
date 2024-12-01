package tukano.api.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path(RestFunctions.PATH)
public interface RestFunctions {
    String PATH = "/functions";
    String SHORT_ID = "shortId";

    @GET
    @Path("/stats")
    @Produces(MediaType.APPLICATION_JSON)
    String stats(@QueryParam(SHORT_ID) String shortId);
}
