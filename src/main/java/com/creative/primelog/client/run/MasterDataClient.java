package com.creative.primelog.client.run;

import com.primelog.cirrus.masterdata.frontend.protoGen.MasterDataProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import com.primelog.cirrus.masterdata.frontend.protoGen.masterDataServiceGrpc;
import java.util.concurrent.TimeUnit;

public class MasterDataClient {

    private final ManagedChannel channel;
    private final masterDataServiceGrpc.masterDataServiceBlockingStub blockingStub;

    public MasterDataClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build());
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
        MasterDataProto.CountryOptionRequest request = MasterDataProto.CountryOptionRequest.newBuilder().setAuthenticationTicket("qKuYFpRS2xM_sMHXUbVFf0t4tJ2ZZ21A4uYGoc32_0Xs1OrugoltDvnKs5ermtdLbaJj5pM0ZS9Ui0oDOW2EbqR-YuLaV0X5IzEXo3iqFeN82YiyntRfA41TWePUZ_DS").build();
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

    public static void main(String [] args) throws InterruptedException {

        MasterDataClient client = new MasterDataClient("localhost", 9090);

        try {
            client.printMasterDataVersion();
            client.getCountryOptions();
        } finally {
            client.shutdown();
        }
    }
}
