package com.creative.primelog.client.client;

import com.creative.primelog.client.config.GRpcServerProperties;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.calculator.CalculatorGrpc;
import io.grpc.examples.calculator.CalculatorOuterClass;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.SslContext;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class CalculatorClient {

    static ManagedChannel channel;
    static ManagedChannel inProcChannel;
    static CalculatorGrpc.CalculatorStub asyncStub;

    public static void main(String [] args) throws SSLException {

        init();
        try {
            calculate();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void callCalculateService()  {

//        final CalculatorFutureStub calculatorFutureStub = CalculatorGrpc.newFutureStub(Optional.ofNullable(channel).orElse(inProcChannel));
//        final CalculatorRequest request = CalculatorRequest.newBuilder().setNumber1(20).setNumber2(10)
//                .setOperation(CalculatorRequest.OperationType.SUBTRACT).build();
//
//        io.grpc.stub.StreamObserver<io.grpc.examples.CalculatorOuterClass.CalculatorResponse> responseObserver
//                = new StreamObserver<CalculatorResponse>() {
//            @Override
//            public void onNext(CalculatorResponse value) {
//                System.out.println("Succes on next");
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                System.out.println("error");
//            }
//
//            @Override
//            public void onCompleted() {
//                System.out.println("Success on complete");
//            }
//        };
//
//        try {
//            asyncStub.calculate(request, responseObserver);
//        } catch (Exception ex) {
//            System.out.println("Error" + ex);
//        }
//        System.out.println("Something else happening");
    }

    private static void init() throws SSLException {

        File clientCertFile = new File("/home/kasun/apps/treafik/frontend.cert");
        File keyFile = new File("/home/kasun/apps/treafik/frontend.key");
        File caFile = new File("/home/kasun/apps/treafik/cert.pem");

        GRpcServerProperties gRpcServerProperties = new GRpcServerProperties();

        SslContext sslcontext = GrpcSslContexts.forClient()
                // if server's cert doesn't chain to a standard root
                .trustManager(caFile)
//                .keyManager(clientCertFile, keyFile) // client cert
                .build();

        if(gRpcServerProperties.isEnabled()) {
//            channel = onChannelBuild(ManagedChannelBuilder.forAddress("" +
//                    "" +
//                    "" +
//                    "localhost", 2525)
//                    .usePlaintext(true)
//            ).build();

            channel = NettyChannelBuilder.forAddress("localhost", 6565)
                    .sslContext(sslcontext).build();
        }
        if(gRpcServerProperties.getInProcessServerName() != null){
            inProcChannel = onChannelBuild(
                    InProcessChannelBuilder.forName(gRpcServerProperties.getInProcessServerName())
                            .usePlaintext(true)
            ).build();

        }

        asyncStub = CalculatorGrpc.newStub(channel);
    }

    protected static ManagedChannelBuilder<?>  onChannelBuild(ManagedChannelBuilder<?> channelBuilder){
        return  channelBuilder;
    }

    protected static InProcessChannelBuilder onChannelBuild(InProcessChannelBuilder channelBuilder){
        return  channelBuilder;
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    private static void calculate() throws InterruptedException {

        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<CalculatorOuterClass.CalculatorResponse> responseObserver = new StreamObserver<CalculatorOuterClass.CalculatorResponse>() {

            @Override
            public void onNext(CalculatorOuterClass.CalculatorResponse value) {
                System.out.println("Result is " +  value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Response Observer : Error" + t);
            }

            @Override
            public void onCompleted() {
                System.out.println("Completed");
            }
        };

        final CalculatorOuterClass.CalculatorRequest request = CalculatorOuterClass.CalculatorRequest.newBuilder().setNumber1(30)
                .setNumber2(15).setOperation(CalculatorOuterClass.CalculatorRequest.OperationType.SUBTRACT).build();

        StreamObserver<CalculatorOuterClass.CalculatorRequest> requestObserver = asyncStub.calculate(responseObserver);

        try {
            System.out.println("Processing ");
            requestObserver.onNext(request);
            System.out.println("continue ....");

            requestObserver.onCompleted();

            //Thread.sleep(20000);
            System.out.println("continue again....");
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }



        // Receiving happens asynchronously
        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            System.out.println("recordRoute can not finish within 1 minutes");
        }
    }

}