package com.creative.primelog.client.client;

import com.primelog.cirrus.masterdata.frontend.protoGen.MasterDataProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import com.primelog.cirrus.masterdata.frontend.protoGen.masterDataServiceGrpc;
import java.util.concurrent.TimeUnit;

public class MasterDataClient {

    public static final String AUTH_TICKET = "5cxuqKBHh8sxGuq4K68WWFbO8AIkJVqrHUzBXI-n1I-3K05oQ1wSCU4jDV9WNraG_Q2tLGC_87sZCL91de5l5A";

    private final ManagedChannel channel;
    private final masterDataServiceGrpc.masterDataServiceBlockingStub blockingStub;

    public MasterDataClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build());
        System.out.println("Grpc host : " + host + " port : " + port);
    }

    public MasterDataClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = masterDataServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    private void getCountryOptions() {
        System.out.println("Will get country option " + " ...");
        MasterDataProto.CountryOptionRequest request = MasterDataProto.CountryOptionRequest.newBuilder().setAuthenticationTicket(AUTH_TICKET).build();
        MasterDataProto.CountryOptionResponse response;
        try {
            response = blockingStub.getCountryOptions(request);
        } catch (StatusRuntimeException e) {
            System.out.println("RPC failed: " + e.getStatus());
            return;
        }
        System.out.println("Country Options Name: " + response.getCountryOptionsList());
    }

    private void printMasterDataVersion() {

        System.out.println("Printing master data version");
        MasterDataProto.VersionRequest request = MasterDataProto.VersionRequest.newBuilder()
                .build();

        MasterDataProto.VersionResponse response;
        try {
            response = blockingStub.getVersion(request);
        } catch (StatusRuntimeException e) {
            System.out.println("RPC failed: " + e.getStatus());
            return;
        }
        System.out.println("Master data version : " + response.getVersion());
    }

    public static void run(String host, int port) throws InterruptedException {

        MasterDataClient client = new MasterDataClient(host, port);

        try {
            client.printMasterDataVersion();
            client.getCountryOptions();
        } finally {
            client.shutdown();
        }
    }

    public static void main(String [] args) throws InterruptedException {

        MasterDataClient client = new MasterDataClient("localhost", 6565);

        try {
            client.printMasterDataVersion();
            client.getCountryOptions();
        } catch (Exception ex) {
            System.err.println("Unable to retrieve data " + ex.getMessage());
        }finally {
            client.shutdown();
        }
    }
}
