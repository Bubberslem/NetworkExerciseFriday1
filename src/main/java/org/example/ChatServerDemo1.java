package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServerDemo1 implements IObservable{
    private List<IObserver> clients = new ArrayList<>();

    private static volatile ChatServerDemo1 instance;
    private ChatServerDemo1() {}

    public static ChatServerDemo1 getInstance() {
        if (instance == null) {
            instance = new ChatServerDemo1();
        }
        return instance;
    }

    public void startServer(int port) {
        try {
            ServerSocket server = new ServerSocket(port);
            while (true) {
                Socket client = server.accept();
                Runnable runnable = new ClientHandler(client,this);
                new Thread(runnable).start();
                IObserver clientHandler = (IObserver) runnable;
                clients.add(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void removeClient(IObserver client) {
        clients.remove(client);
    }

    @Override
    public void broadcast(String msg) {
        for (IObserver observer : clients) {
            observer.notify(msg);
        }
    }

    @Override
    public void addObserver(IObserver observer) {

    }

    @Override
    public void removeObserver(IObserver observer) {

    }

    public static void main(String[] args) {
        new ChatServerDemo1().startServer(8080);
    }

    private static class ClientHandler implements Runnable, IObserver {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private ChatServerDemo1 server;
        private String name = " ";

        public ClientHandler(Socket client, ChatServerDemo1 server) throws IOException {
            this.client = client;
            this.server = server;
            this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            this.out = new PrintWriter(client.getOutputStream(), true);
        }

        public void run() {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    if (msg.startsWith("#JOIN ")) {
                        this.name = msg.split(" ")[1];
                        server.broadcast("New client joined the chat. Welcome to: " + name);
                    } else if (msg.startsWith("#LEAVE")) {
                        if (name == null || name.isEmpty()) {
                            // Hvis klienten ikke har joinet, send en besked tilbage
                            out.println("You must join the chat first using #JOIN <name>");
                        } else {
                            server.broadcast(name + " has left the chat");
                            server.removeClient(this);
                            client.close();
                            return;
                        }
                    } else {
                        if (name != null && !name.isEmpty()) {
                            // Kun broadcast beskeder hvis klienten har et navn
                            server.broadcast(name + ": " + msg);
                        } else {
                            out.println("You must join the chat first using #JOIN <name>");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void notify(String msg) {
            out.println(msg);
        }
    }
}
