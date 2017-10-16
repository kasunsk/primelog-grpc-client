package com.creative.grpc.main;

import com.creative.grpc.config.GRpcServerProperties;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.CalculatorGrpc;
import io.grpc.examples.CalculatorGrpc.*;
import io.grpc.examples.CalculatorOuterClass.CalculatorResponse;
import io.grpc.examples.CalculatorOuterClass.CalculatorRequest;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class Main {

    static Random random = new Random();

    static ManagedChannel channel;
    static ManagedChannel inProcChannel;
    static CalculatorStub asyncStub;

    public static void main(String [] args) {

        init();
        //callCalculateService();
        try {
            calculate();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void callCalculateService()  {

        final CalculatorGrpc.CalculatorFutureStub calculatorFutureStub = CalculatorGrpc.newFutureStub(Optional.ofNullable(channel).orElse(inProcChannel));
        final CalculatorRequest request = CalculatorRequest.newBuilder().setNumber1(20).setNumber2(15)
                .setOperation(CalculatorRequest.OperationType.SUBTRACT).build();

        try {
            //final Double answer = calculatorFutureStub.calculate(request).get().getResult();
            System.out.println("Congratulation!!! It Works.");
           // System.out.println("Answer is : " + answer);
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
                System.out.println("Result is" +  value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Response Observer : Error");
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
            System.out.println("Processing");
            requestObserver.onNext(request);
            Thread.sleep(random.nextInt(1000) + 500);
            if (finishLatch.getCount() == 0) {
                // RPC completed or errored before we finished sending.
                // Sending further requests won't error, but they will just be thrown away.
                return;
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }

        requestObserver.onCompleted();

        // Receiving happens asynchronously
        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            System.out.println("recordRoute can not finish within 1 minutes");
        }
    }

}