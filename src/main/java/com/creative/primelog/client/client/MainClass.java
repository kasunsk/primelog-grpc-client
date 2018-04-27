package com.creative.primelog.client.client;

import java.util.Scanner;

public class MainClass {

    public static void main(String [] args) {

        Scanner reader = new Scanner(System.in);
        System.out.println("1 - Common Service Client");
        System.out.println("2 - Master Data Service client");
        System.out.print("Select the client : ");
        int selectedClass = reader.nextInt();

        System.out.print("Enter the port :");
        int port = reader.nextInt();
        reader.close();

        if (selectedClass == 1) {
            try {
                CommonServiceClient.run("localhost", port);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (selectedClass == 2) {
            try {
                MasterDataClient.run("localhost", port);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("Invalid client selected");
        }
    }
}
