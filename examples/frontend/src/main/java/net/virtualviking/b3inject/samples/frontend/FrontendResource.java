package net.virtualviking.b3inject.samples.frontend;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.virtualviking.b3inject.examples.trader.Status;
import net.virtualviking.b3inject.examples.trader.TradeRequest;
import net.virtualviking.b3inject.examples.trader.TraderGrpc;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

@Path("/")
public class FrontendResource {
    private String quoterRoot;

    private Client client = ClientBuilder.newClient();

    private static final Logger logger = Logger.getLogger(FrontendResource.class.getName());

    private final ManagedChannel channel;
    private final TraderGrpc.TraderBlockingStub blockingStub;

    public FrontendResource(String quoterHost, int quoterPort, String traderHost, int traderPort) {
        quoterRoot = "http://" + quoterHost + ":" + quoterPort + "/quoter";
        this.channel = ManagedChannelBuilder.forAddress(traderHost, traderPort)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build();
        this.blockingStub = TraderGrpc.newBlockingStub(this.channel);
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
    public Quote requestCallback(@PathParam("symbol") String symbol) throws InterruptedException, ExecutionException {
        Future<Quote> f = client
                .target(quoterRoot)
                .path("quote_callback")
                .queryParam("symbol", symbol)
                .request(MediaType.APPLICATION_JSON)
                .async()
                .get(Quote.class);
        return f.get();
    }

    @Path("request_callback_sw/{symbol}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Quote requestCallbacSpringWeb(@PathParam("symbol") String symbol) throws InterruptedException, ExecutionException {
        Future<Quote> f = client
                .target(quoterRoot)
                .path("quote_callback_sw")
                .queryParam("symbol", symbol)
                .request(MediaType.APPLICATION_JSON)
                .async()
                .get(Quote.class);
        return f.get();
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

    @Path("trade")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public boolean trade(@QueryParam("symbol") String symbol, @QueryParam("amount") int amount) {
        TradeRequest rq = TradeRequest
                .newBuilder()
                .setAmount(amount)
                .setSymbol(symbol)
                .build();
       return blockingStub.makeTrade(rq).getResult();
    }
}
