package tukano.api.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

@Path(RestFunctions.PATH)
public interface RestFunctions {
	
	String PATH = "/functions";
	String VIEWS = "views";
	String RECS = "recs";
	String SHORT_ID = "shortId";
	String TOKEN = "token";

 	@PUT
 	@Path("/{" + SHORT_ID +"}/" + VIEWS)
	void countView(@PathParam(SHORT_ID) String shortId, @QueryParam(TOKEN) String token);

 	@GET
 	@Path("/" + RECS)
	void tukanoRecommends();
}
