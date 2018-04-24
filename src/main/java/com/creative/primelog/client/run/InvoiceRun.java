package com.creative.primelog.client.run;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.InvoiceGrpc;
import io.grpc.examples.InvoiceOuterClass;

import java.util.concurrent.TimeUnit;

public class InvoiceRun {

    private final ManagedChannel channel;
    private final InvoiceGrpc.InvoiceBlockingStub blockingStub;

    public InvoiceRun(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext(true)
                .build());
    }

    public InvoiceRun(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = InvoiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    private void issueInvoice() {
        System.out.println("Will issue an invoice " + " ...");
        InvoiceOuterClass.InvoiceRequest request = InvoiceOuterClass.InvoiceRequest.newBuilder().setInvoiceid("invoiceId").build();
        InvoiceOuterClass.InvoiceResponse response;
        try {
            response = blockingStub.issueInvoice(request);
        } catch (StatusRuntimeException e) {
            System.out.println("RPC failed: {0}" + e.getStatus());
            return;
        }
        System.out.println("Invoice Name: " + response.getInvoicename());
    }

    public static void main(String [] args) throws InterruptedException {

        InvoiceRun client = new InvoiceRun("localhost", 6565);

        try {
            client.issueInvoice();
        } finally {
            client.shutdown();
        }
    }
}
