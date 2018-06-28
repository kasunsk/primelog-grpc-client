package com.creative.primelog.client.interceptor;

import com.creative.primelog.client.client.MasterDataClient;
import com.google.common.annotations.VisibleForTesting;
import io.grpc.*;

public abstract class AbstractClientInterceptor implements ClientInterceptor {

    @VisibleForTesting
    static final Metadata.Key<String> CLIENT_AUTH_TICKET =
            Metadata.Key.of("authenticationTicket", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
        /* put custom header */
                headers.put(CLIENT_AUTH_TICKET, MasterDataClient.AUTH_TICKET);
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onHeaders(Metadata headers) {
                        /**
                         * if you don't need receive header from server,
                         * you can use {@link io.grpc.stub.MetadataUtils#attachHeaders}
                         * directly to send header
                         */
                        System.out.println("header received from server:" + headers);
                        super.onHeaders(headers);
                    }
                }, headers);
            }
        };
    }
}
