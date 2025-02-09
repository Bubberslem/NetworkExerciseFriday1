package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/*
* Purpose: Demo to show the use of Socket, ServerSocket
* And starting new connections each in its own thread
*
*/
public class SimpleDemo1Threaded {
    private ServerSocket server;

    // Purpose: To start a new ServerSocket and endlessly listen for client connections
    public void run(int port){
        try {
            server = new ServerSocket(port);
            System.out.println("Server ready to receive requests");
            while(true) {
                Socket clientSocket = server.accept();
                System.out.println("Client registered");
                // Create a container for the client socket so that we can have each end of the communcation pipe (in, out).
                Runnable clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
//            String inputLine;
//            while((inputLine = in.readLine()) != null){
//                System.out.println("Message from client: " + inputLine);
//                out.println(inputLine);
//            }
//            String request = in.readLine();
//            System.out.println("Message from client: " + request);
//            out.println("Response from server");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        new SimpleDemo1Threaded().run(8080);
    }
    private static class ClientHandler implements Runnable{
        private Socket clientSocket;
        public ClientHandler(Socket socket){
            this.clientSocket = socket;
        }

        @Override
        public void run() {
           try {
               PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
               BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
               String msg = in.readLine();
               out.println("We received this: " + msg);
               System.out.println("Message from client: " + msg);


           }catch(IOException e){
               e.printStackTrace();
           }
        }
    }
}
