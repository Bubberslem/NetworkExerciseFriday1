package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClientThreaded {
    private Socket clientSocket;
    private static PrintWriter out;
    private static BufferedReader in;

    public void startConnection(String ip, int port){
        try {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }catch(IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args){
        try {
            new TCPClientThreaded().startConnection("localHost", 8080);
            out.println("Besked fra klienten som vi har kodet");
            String firstResponse = in.readLine();
            System.out.println(firstResponse);
            out.println("Second message");
            String secondResponse = in.readLine();
            System.out.println(secondResponse);

            String response = in.readLine();
            System.out.println(response);
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
}
