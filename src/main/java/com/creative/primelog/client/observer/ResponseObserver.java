package com.creative.primelog.client.observer;

import io.grpc.examples.CalculatorOuterClass;
import io.grpc.stub.StreamObserver;

public class ResponseObserver implements StreamObserver<CalculatorOuterClass.CalculatorResponse> {


    @Override
    public void onNext(CalculatorOuterClass.CalculatorResponse value) {
        System.out.println("onNext");
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
