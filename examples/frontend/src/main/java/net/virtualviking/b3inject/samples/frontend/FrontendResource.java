package net.virtualviking.b3inject.samples.frontend;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public class FrontendResource {
    private String quoterRoot;

    private Client client = ClientBuilder.newClient();

    public FrontendResource(String quoterHost, int quoterPort) {
        quoterRoot = "http://" + quoterHost + ":" + quoterPort + "/quoter";
    }

    @Path("quote/{symbol}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Quote quote(@PathParam("symbol") String symbol) {
        return client
                .target("quote")
                .queryParam("symbol", symbol)
                .request(MediaType.APPLICATION_JSON)
                .get(Quote.class);
    }

    @Path("symbols")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @SuppressWarnings("Unchecked")
    public List<String> symbols() {
        return client
                .target(quoterRoot)
                .path("symbols")
                .request(MediaType.APPLICATION_JSON)
                .get(List.class);
    }
}
