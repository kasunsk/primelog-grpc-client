package com.creative.primelog.client.run;

import com.creative.primelog.client.config.GRpcServerProperties;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.CalculatorGrpc;
import io.grpc.examples.CalculatorGrpc.CalculatorFutureStub;
import io.grpc.examples.CalculatorGrpc.CalculatorStub;
import io.grpc.examples.CalculatorOuterClass;
import io.grpc.examples.CalculatorOuterClass.CalculatorRequest;
import io.grpc.examples.CalculatorOuterClass.CalculatorResponse;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class Calculator {

    static Random random = new Random();

    static ManagedChannel channel;
    static ManagedChannel inProcChannel;
    static CalculatorStub asyncStub;

    public static void main(String [] args) {

        init();
        callCalculateService();
//        try {
//            calculate();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    private static void callCalculateService()  {

        final CalculatorFutureStub calculatorFutureStub = CalculatorGrpc.newFutureStub(Optional.ofNullable(channel).orElse(inProcChannel));
        final CalculatorRequest request = CalculatorRequest.newBuilder().setNumber1(20).setNumber2(10)
                .setOperation(CalculatorRequest.OperationType.SUBTRACT).build();

        io.grpc.stub.StreamObserver<io.grpc.examples.CalculatorOuterClass.CalculatorResponse> responseObserver
                = new StreamObserver<CalculatorResponse>() {
            @Override
            public void onNext(CalculatorResponse value) {
                System.out.println("Succes on next");
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("error");
            }

            @Override
            public void onCompleted() {
                System.out.println("Success on complete");
            }
        };

        try {
            asyncStub.calculate(responseObserver).onCompleted();
        } catch (Exception ex) {
            System.out.println("Error" + ex);
        }
        System.out.println("Something else happening");
    }

    private static void init() {

        GRpcServerProperties gRpcServerProperties = new GRpcServerProperties();

        if(gRpcServerProperties.isEnabled()) {
            channel = onChannelBuild(ManagedChannelBuilder.forAddress("localhost", gRpcServerProperties.getPort())
                    .usePlaintext(true)
            ).build();
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

        StreamObserver<CalculatorResponse> responseObserver = new StreamObserver<CalculatorResponse>() {

            @Override
            public void onNext(CalculatorResponse value) {
                System.out.println("Result is " +  value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Response Observer : Error" + t);
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Completed");
                finishLatch.countDown();
            }
        };

        final CalculatorRequest request = CalculatorRequest.newBuilder().setNumber1(30)
                .setNumber2(15).setOperation(CalculatorRequest.OperationType.SUBTRACT).build();

        StreamObserver<CalculatorRequest> requestObserver = asyncStub.calculate(responseObserver);

        try {
            System.out.println("Processing ");
            requestObserver.onNext(request);
            System.out.println("continue ....");

            requestObserver.onCompleted();

            Thread.sleep(20000);
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