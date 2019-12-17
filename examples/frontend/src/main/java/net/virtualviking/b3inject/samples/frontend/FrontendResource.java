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
                .target(quoterRoot)
                .path("quote")
                .queryParam("symbol", symbol)
                .request(MediaType.APPLICATION_JSON)
                .get(Quote.class);
    }

    @Path("request_callback/{symbol}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Quote requestCallback(@PathParam("symbol") String symbol) {
        return client
                .target(quoterRoot)
                .path("quote_callback")
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

    @Path("callback")
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @SuppressWarnings("Unchecked")
    public void callback(Quote quote) {
       System.out.println("Received callback: " + quote);
    }
}
