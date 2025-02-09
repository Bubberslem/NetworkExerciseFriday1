package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleDemo1 {
    private ServerSocket server;
    private Socket clientHandler;
    private PrintWriter out;
    private BufferedReader in;

    public void run(int port){
        try {
            server = new ServerSocket(port);
            clientHandler = server.accept();
            out = new PrintWriter(clientHandler.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(clientHandler.getInputStream()));
            System.out.println("Server ready to receive requests");
            String inputLine;
            while((inputLine = in.readLine()) != null){
                System.out.println("Message from client: " + inputLine);
                out.println(inputLine);
            }
           // String request = in.readLine();
           // System.out.println("Message from client: " + request);
           // out.println("Response from server");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        new SimpleDemo1().run(8080);
    }
}
