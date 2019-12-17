package net.virtualviking.b3inject.handlers;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.virtualviking.b3inject.Constants;
import net.virtualviking.b3inject.Context;
import net.virtualviking.b3inject.Logger;
import net.virtualviking.b3inject.Matchers;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;


public class JaxRsEgressHandler {

    public static class B3HeadersRequestFilter implements ClientRequestFilter {
        @Override
        public void filter(ClientRequestContext clientRequest) throws IOException {
            Context context = Context.Factory.getContext();
            if(context == null) {
                return;
            }
            if(context.isEgressHandled()) {
                return;
            }
            Logger.debug("EGRESS: Passing B3 headers: " + Logger.mapToString(context.getB3Headers()));
            for(String k : Constants.b3Headers) {
                String v = context.getB3Headers().get(k);
                if(v != null) {
                    clientRequest.getHeaders().add(k, v);
                }
            }
            context.setEgressHandled(true);
        }
    }

    public static AgentBuilder buildAgent(final AgentBuilder b) {
        return b.type(hasSuperType(named("javax.ws.rs.client.ClientBuilder")))
                .transform((builder, type, classLoader, module) ->
                        builder.visit(Advice.to(JaxRsEgressHandler.class).on(new Matchers.WildcardMethodMatcher("build(*)"))));
    }

    @Advice.OnMethodEnter
    public static void enter(final @Advice.Origin String origin, final @Advice.This Object thiz) {
        final ClientBuilder builder = (ClientBuilder)thiz;
        builder.register(B3HeadersRequestFilter.class);
    }
}
