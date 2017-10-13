package com.creative.grpc.main;

import com.creative.grpc.config.GRpcServerProperties;
import com.creative.grpc.observer.ResponseObserver;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.CalculatorGrpc;
import io.grpc.examples.CalculatorOuterClass;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

class Main {

    static ManagedChannel channel;
    static ManagedChannel inProcChannel;

    public static void main(String [] args) {

        init();
        callCalculateService();
        //calculate();
    }

    private static void callCalculateService()  {

        final CalculatorGrpc.CalculatorFutureStub calculatorFutureStub = CalculatorGrpc.newFutureStub(Optional.ofNullable(channel).orElse(inProcChannel));
        final CalculatorOuterClass.CalculatorRequest request = CalculatorOuterClass.CalculatorRequest.newBuilder().setNumber1(20).setNumber2(15)
                .setOperation(CalculatorOuterClass.CalculatorRequest.OperationType.SUBTRACT).build();

        try {
            final Double answer = calculatorFutureStub.calculate(request).get().getResult();
            System.out.println("Congratulation!!! It Works.");
            System.out.println("Answer is : " + answer);
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

    private static void calculate() {

        CalculatorGrpc.CalculatorStub asyncStub = CalculatorGrpc.newStub(Optional.ofNullable(channel)
                .orElse(inProcChannel));

        StreamObserver<CalculatorOuterClass.CalculatorResponse> responseObserver = new StreamObserver<CalculatorOuterClass
                .CalculatorResponse>() {

            @Override
            public void onNext(CalculatorOuterClass.CalculatorResponse value) {
                System.out.println("Response Observer : onNext");
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Response Observer : Error");
            }

            @Override
            public void onCompleted() {
                System.out.println("Response Observer : completed");
            }
        };

        final CalculatorOuterClass.CalculatorRequest request = CalculatorOuterClass.CalculatorRequest.newBuilder().setNumber1(30)
                .setNumber2(15).setOperation(CalculatorOuterClass.CalculatorRequest.OperationType.SUBTRACT).build();



        asyncStub.calculate(request, responseObserver);

        System.out.println("Something else happening");
    }

}