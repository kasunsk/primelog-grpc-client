package com.creative.primelog.client.client;

import com.creative.primelog.client.interceptor.HeaderClientInterceptor;
import com.primelog.cirrus.common.backend.protoGen.CommonDataProto;
import com.primelog.cirrus.common.backend.protoGen.CommonServiceGrpc;
import com.primelog.cirrus.masterdata.frontend.protoGen.MasterDataServiceGrpc;
import io.grpc.*;

import java.util.concurrent.TimeUnit;

import static com.creative.primelog.client.client.MasterDataClient.AUTH_TICKET;

public class CommonServiceClient {

    private final ManagedChannel originChannel;
    private final CommonServiceGrpc.CommonServiceBlockingStub blockingStub;

    public CommonServiceClient(String host, int port) {
        System.out.println("Grpc host : " + host + " port : " + port);

        originChannel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build();
        ClientInterceptor interceptor = new HeaderClientInterceptor();
        Channel channel = ClientInterceptors.intercept(originChannel, interceptor);
        blockingStub = CommonServiceGrpc.newBlockingStub(channel);
        System.out.println("Grpc host : " + host + " port : " + port);
    }


    public void shutdown() throws InterruptedException {
        originChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void run(String localhost, int port) throws InterruptedException {

        CommonServiceClient client = new CommonServiceClient(localhost, port);

        try {
            client.getCurrencies();
        } finally {
            client.shutdown();
        }
    }

    public static void main(String [] args) throws InterruptedException {

        CommonServiceClient client = new CommonServiceClient("localhost", 7575);

        try {
            client.getCurrencies();
        } finally {
            client.shutdown();
        }
    }

    public void getCurrencies() {
        System.out.println("Looking for currencies from common web service");
        CommonDataProto.CurrencyRequest request = CommonDataProto.CurrencyRequest.newBuilder()
                .build();
        CommonDataProto.CurrencyResponse response;

        try {
            response = blockingStub.getCurrencies(request);
        } catch (StatusRuntimeException ex) {
            System.out.println("RPC failed: " + ex.getMessage());
            return;
        }
        System.out.println("Currencies Result : " + response.getCurrencyTypesList());
        System.out.println();
        System.out.println("Success : getCurrencies from Common Service");
    }
}
