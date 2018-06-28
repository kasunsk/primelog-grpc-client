package com.creative.primelog.client.client;

import com.creative.primelog.client.interceptor.HeaderClientInterceptor;
import com.primelog.cirrus.masterdata.frontend.protoGen.MasterDataProto;
import io.grpc.*;
import com.primelog.cirrus.masterdata.frontend.protoGen.MasterDataServiceGrpc;
import java.util.concurrent.TimeUnit;

public class MasterDataClient {

    public static final String AUTH_TICKET =
//"5cxuqKBHh8sxGuq4K68WWFbO8AIkJVqrHUzBXI-n1I-fVGlPaxho7d1ukFptgwyFDUk--tJIru2nXKhfAw8qyw";
    "5cxuqKBHh8sxGuq4K68WWFbO8AIkJVqrHUzBXI-n1I-fVGlPaxho7d1ukFptgwyFDUk--tJIru2nXKhfAw8qyw";

        private final ManagedChannel originChannel;
        private final MasterDataServiceGrpc.MasterDataServiceBlockingStub blockingStub;

    public MasterDataClient(String host, int port) {
        originChannel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build();
        ClientInterceptor interceptor = new HeaderClientInterceptor();
        Channel channel = ClientInterceptors.intercept(originChannel, interceptor);
        blockingStub = MasterDataServiceGrpc.newBlockingStub(channel);
        System.out.println("Grpc host : " + host + " port : " + port);
    }

    public void shutdown() throws InterruptedException {
        originChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    private void getCountryOptions() {
        System.out.println("Will get country option " + " ...");
        MasterDataProto.CountryOptionRequest request = MasterDataProto.CountryOptionRequest.newBuilder().build();
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
            System.out.println();
            System.out.println("Success : getVersion from Master Data Service");
            System.out.println();

            client.getCountryOptions();

            System.out.println();
            System.out.println("Success : getCountryOptions from Master Data Service");
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
