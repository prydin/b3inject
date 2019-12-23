/*
 *  Copyright 2019 Pontus Rydin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.virtualviking.b3inject.handlers;

import io.grpc.*;
import net.virtualviking.b3inject.Constants;
import net.virtualviking.b3inject.Context;
import net.virtualviking.b3inject.Logger;

import java.util.Map;

public class GrpcFilters {
    public static class B3ClientHeaderInjector implements ClientInterceptor {
        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                                   CallOptions callOptions, Channel next) {
            return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {
                    net.virtualviking.b3inject.Context ctx = net.virtualviking.b3inject.Context.Factory.getContext();
                    if(ctx != null && !ctx.isEgressHandled()) {
                        Logger.debug("EGRESS: Passing B3 headers: " + Logger.mapToString(ctx.getB3Headers()));
                        for(Map.Entry<String, String> e : ctx.getB3Headers().entrySet()) {
                            Metadata.Key<String> mk = Metadata.Key.of(e.getKey(), Metadata.ASCII_STRING_MARSHALLER);
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

            net.virtualviking.b3inject.Context ctx = Context.Factory.newContext();
            Map<String, String> headers = ctx.getB3Headers();
            for(String k : Constants.b3Headers) {
                Metadata.Key<String> mk = Metadata.Key.of(k, Metadata.ASCII_STRING_MARSHALLER);
                String v = requestHeaders.get(mk);
                if(v != null) {
                    headers.put(k, v);
                }
            }
            Logger.debug("INGRESS: Captured B3 headers: " + Logger.mapToString(headers));

            return next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {}, requestHeaders);
        }
    }

    public static ServerServiceDefinition addIngressFilter(ServerServiceDefinition ssd) {
        return  ServerInterceptors.intercept(ssd, new ServerInterceptor[]{new GrpcFilters.B3ServerHeaderExtractor()});
    }
}
