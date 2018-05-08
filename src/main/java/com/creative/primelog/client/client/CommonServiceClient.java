package com.creative.primelog.client.client;

import com.primelog.cirrus.common.backend.protoGen.CommonDataProto;
import com.primelog.cirrus.common.backend.protoGen.CommonServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;

import static com.creative.primelog.client.client.MasterDataClient.AUTH_TICKET;

public class CommonServiceClient {

    private final ManagedChannel channel;
    private final CommonServiceGrpc.CommonServiceBlockingStub blockingStub;

    public CommonServiceClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build());
        System.out.println("Grpc host : " + host + " port : " + port);
    }

    public CommonServiceClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = CommonServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
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

        CommonServiceClient client = new CommonServiceClient("localhost", 9090);

        try {
            client.getCurrencies();
        } finally {
            client.shutdown();
        }
    }

    public void getCurrencies() {
        System.out.println("Looking for currencies from common web service");
        CommonDataProto.CurrencyRequest request = CommonDataProto.CurrencyRequest.newBuilder()
                .setAuthenticationTicket(AUTH_TICKET).build();
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
