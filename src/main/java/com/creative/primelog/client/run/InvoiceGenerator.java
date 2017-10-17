package com.creative.primelog.client.run;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.primelog.invoice.InvoiceGrpc;
import io.grpc.primelog.invoice.InvoiceGrpc.InvoiceStub;
import io.grpc.primelog.invoice.InvoiceGrpc.InvoiceBlockingStub;
import io.grpc.primelog.invoice.InvoiceGrpc.InvoiceFutureStub;
import io.grpc.primelog.invoice.InvoiceOuterClass.InvoiceRequest;

import java.util.concurrent.TimeUnit;

public class InvoiceGenerator {

    public static void main(String [] args) {

        InvoiceGenerator invoiceGenerator = new InvoiceGenerator("localhost", 6565);
        invoiceGenerator.generateInvoice();
    }

    public InvoiceGenerator(String host, int port) {

        channel = onChannelBuild(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true)).build();
        asyncStub = InvoiceGrpc.newStub(channel);
        blockingStub = InvoiceGrpc.newBlockingStub(channel);
        futureStub = InvoiceGrpc.newFutureStub(channel);
    }

    private ManagedChannel channel;
    private InvoiceStub asyncStub;
    private InvoiceBlockingStub blockingStub;
    private InvoiceFutureStub futureStub;

    public void generateInvoice() {

        final InvoiceRequest request = InvoiceRequest.newBuilder().setDestination("CMB")
                .setOrigin("SIN").setPrice("25 USD").setTenant("IOC").setTransportServiceProvider("DPL").setWeight("30 Kg")
                .build();

        try {
            String reference = futureStub.issue(request).get().getReference();
            System.out.println( "Invoice reference is " + reference);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public void shutDown() {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to shutdown channel", e);
        }
    }

    private static ManagedChannelBuilder<?>  onChannelBuild(ManagedChannelBuilder<?> channelBuilder){
        return  channelBuilder;
    }

    public void setChannel(ManagedChannel channel) {
        this.channel = channel;
    }

    public void setAsyncStub(InvoiceStub asyncStub) {
        this.asyncStub = asyncStub;
    }

    public void setBlockingStub(InvoiceBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }
}
