package com.creative.grpc.main;

import com.creative.grpc.config.Channel;
import com.creative.grpc.config.GRpcServerProperties;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.CalculatorGrpc;
import io.grpc.examples.CalculatorOuterClass;
import io.grpc.inprocess.InProcessChannelBuilder;

import java.util.Optional;

class Main {

    static ManagedChannel channel;
    static ManagedChannel inProcChannel;

    public static void main(String [] args) {

        init();
        callCallculateService();
    }

    private static void callCallculateService()  {

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

}