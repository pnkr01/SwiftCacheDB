package com.swiftcache.swiftcache;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TestClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 7777);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("✅ Connected to AgentMemory Engine!");
            System.out.println("Type your commands below (type 'exit' to quit):");

            // Read the initial welcome message from the server
            System.out.println("SERVER: " + in.readLine());

            while (true) {
                System.out.print("> ");
                String command = scanner.nextLine();

                if ("exit".equalsIgnoreCase(command)) break;

                // Send to server
                out.println(command);

                // Read response
                String response = in.readLine();
                System.out.println("REPLY: " + response);
            }
        } catch (Exception e) {
            System.out.println("❌ Connection failed: " + e.getMessage());
        }
    }
}