package net.virtualviking.b3inject.handlers;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import static net.bytebuddy.matcher.ElementMatchers.*;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class ServletFilterHandler {
    @Advice.OnMethodExit
    public static void exit(final @Advice.Origin String origin, final @Advice.This Object self) {
        B3CaptureFilter.addTo(self);
    }

    public static AgentBuilder buildAgent(AgentBuilder b) {
        return b.type(not(isInterface()).and(hasSuperType(named("javax.servlet.ServletContext"))))
                .transform((builder, type, classLoader, module) ->
                        builder
                                .visit(Advice.to(ServletFilterHandler.class).on(isConstructor())));
    }
}
