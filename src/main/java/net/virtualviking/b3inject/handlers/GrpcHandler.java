package net.virtualviking.b3inject.handlers;

import io.grpc.*;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.virtualviking.b3inject.Constants;
import net.virtualviking.b3inject.Context;

import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class GrpcHandler {
    public static AgentBuilder buildAgent(final AgentBuilder b) {
        return b
                .type(named("io.grpc.util.MutableHandlerRegistry"))
                .transform((builder, type, loader, module) ->
                        builder.visit(Advice.to(Registry.class).on(named("addService").and(takesArguments(1)))))
                .type(hasSuperType(named("io.grpc.ServerBuilder")))
                                .transform((builder, type, loader, module) ->
                        builder.visit(Advice.to(Registry.class).on(named("addService").and(takesArguments(1)))))
                .type(hasSuperType(named("io.grpc.stub.AbstractStub")))
                .transform(((builder, type, loader, module) ->
                        builder.visit(Advice.to(Stub.class).on(named("getChannel")))));

    }

    public static class Registry {
        @Advice.OnMethodEnter
        public static void enter(final @Advice.Origin String origin, @Advice.Argument(value = 0, readOnly = false, typing = Assigner.Typing.DYNAMIC) Object service) {
            service = ServerInterceptors.intercept((ServerServiceDefinition) service, new ServerInterceptor[] { new B3ServerHeaderExtractor() });
        }
    }

    public static class Stub {
        @Advice.OnMethodExit
        public static void exit(final @Advice.Origin String origin, @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object returned) {
            returned = ClientInterceptors.intercept((Channel) returned, new B3ClientHeaderInjector());
        }
    }

    public static class B3ClientHeaderInjector implements ClientInterceptor {
        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                                   CallOptions callOptions, Channel next) {
            return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {
                    Context ctx = Context.Factory.getContext();
                    if(ctx != null && !ctx.isEgressHandled()) {
                        for(Map.Entry<String, String> e : ctx.getB3Headers().entrySet()) {
                            Metadata.Key mk = Metadata.Key.of(e.getKey(), Metadata.ASCII_STRING_MARSHALLER);
                            headers.put(mk, e.getValue());
                        }
                    }
                    super.start(responseListener, headers);
                }
            };
        }
    }

    public static class B3ServerHeaderExtractor implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                ServerCall<ReqT, RespT> call,
                final Metadata requestHeaders,
                ServerCallHandler<ReqT, RespT> next) {

            Context ctx = Context.Factory.newContext();
            Map<String, String> headers = ctx.getB3Headers();
            for(String k : Constants.b3Headers) {
                String v = headers.get(k);
                if(v == null) {
                    continue;
                }
                Metadata.Key mk = Metadata.Key.of(k, Metadata.ASCII_STRING_MARSHALLER);
                requestHeaders.put(mk, v);
            }

            return next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {}, requestHeaders);
        }
    }
}
