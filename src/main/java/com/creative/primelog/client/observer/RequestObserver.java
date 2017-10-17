package com.creative.primelog.client.observer;

import io.grpc.examples.CalculatorGrpc;
import io.grpc.examples.CalculatorOuterClass;
import io.grpc.stub.StreamObserver;

public class RequestObserver implements StreamObserver<CalculatorOuterClass.CalculatorRequest>{

    @Override
    public void onNext(CalculatorOuterClass.CalculatorRequest value) {
        System.out.println("Request observer");
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("Error");
    }

    @Override
    public void onCompleted() {
        System.out.println("Completed");
    }
}
