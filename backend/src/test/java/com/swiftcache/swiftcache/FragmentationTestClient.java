package com.swiftcache.swiftcache;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.Socket;

public class FragmentationTestClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 7777);
             OutputStream out = socket.getOutputStream();
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("SERVER: " + in.readLine());
            System.out.println("⏳ Simulating a terrible network connection...");

            // The command we want to send
            String fragmentedCommand = "PUSH agent:slow Hello! I am arriving very slowly.\n";

            // Send it byte by byte with a delay
            for (char c : fragmentedCommand.toCharArray()) {
                out.write(c);
                out.flush();
                System.out.print(c); // Print to console so you can watch it send
                Thread.sleep(200);   // Wait 200ms between EVERY character
            }

            System.out.println("\n✅ Finished sending. Waiting for response...");

            // Read the response. If the server didn't crash, it will reply "OK"!
            System.out.println("REPLY: " + in.readLine());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}