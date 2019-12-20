package net.virtualviking.b3inject.examples.trader;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class TraderServer {
    private static final Logger logger = Logger.getLogger(TraderServer.class.getName());

    private Server server;

    private void start() throws IOException {
        int port = 50052;
        server = ServerBuilder.forPort(port)
                .addService(new TraderImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    TraderServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        final TraderServer server = new TraderServer();
        server.start();
        server.blockUntilShutdown();
    }

    static class TraderImpl extends TraderGrpc.TraderImplBase {
        @Override
        public void makeTrade(TradeRequest req, StreamObserver<Status> responseObserver) {
            Status reply = Status.newBuilder().setResult(true).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
