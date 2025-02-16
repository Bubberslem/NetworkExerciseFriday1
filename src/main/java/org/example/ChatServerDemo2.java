package org.example;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ChatServerDemo2 implements IObservable{

    private List<IObserver> clients = new ArrayList<>();
    private static volatile ChatServerDemo2 instance;
    ServerSocket serverSocket;

    private ChatServerDemo2() {
    }

    public static ChatServerDemo2 getInstance() {
        if (instance == null) {
            instance = new ChatServerDemo2();
        }
        return instance;
    }

    public static void main(String[] args) {
        new ChatServerDemo2().startServer(8080);
    }

    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            ExecutorService executor = Executors.newCachedThreadPool();
            while (true) {
                Socket client = serverSocket.accept();
                Runnable runnable = new ClientHandler(client, this);
//                new Thread(runnable).start();
                executor.submit(runnable);
                IObserver clientHandler = (IObserver) runnable;
                clients.add(clientHandler);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void addObserver(IObserver observer) {
        clients.add(observer);
    }

    @Override
    public void removeObserver(IObserver observer) {
        clients.remove(observer);
    }

    @Override
    public void broadcast(String msg) {

        for (IObserver observer : clients) {
            observer.notify(msg);
        }
    }

   private static class JoinCommand implements Command {
        @Override
        public void execute(ClientHandler client, String message) {
            client.setName(message.split(" ")[1]);
            client.getServer().broadcast("New client joined the chat. Welcome to " + client.getName());
        }
    }

   private static class MessageCommand implements Command {
        @Override
        public void execute(ClientHandler client, String message) {
            String msg = client.replaceInappropriateWords(message.substring(9));
            client.getServer().broadcast(client.getName() + ": " + msg);
        }
    }

    private static class PrivateMessageCommand implements Command {
        @Override
        public void execute(ClientHandler client, String message) {
            String[] strs = message.split(" ", 3);
            String recipient = strs[1];
            String msg = client.replaceInappropriateWords(strs[2]);
            client.directMessage(recipient, msg);
        }
    }
    private static class ColorCommand implements Command {
        @Override
        public void execute(ClientHandler client, String message) {
            String[] parts = message.split(" ", 3);
            String colorinput = parts[1];

            String colorCode = getColorCode(colorinput);
            String text = client.replaceInappropriateWords(parts[2]);

            //System.out.println("Color Code: " + colorCode + ".");
           // System.out.println("Text: " + text);

            String msg = new ColorDecorator(colorCode).decorate(text);
            client.getServer().broadcast(client.getName() + ": " + msg);
        }

        public String getColorCode(String colorInput) {
            switch (colorInput.toUpperCase()) {
                case "RED":
                    return "\u001B[31m";
                case "GREEN":
                    return "\u001B[32m";
                case "YELLOW":
                    return "\u001B[33m";
                case "BLUE":
                    return "\u001B[34m";
                case "MAGENTA":
                    return "\u001B[35m";
                case "CYAN":
                    return "\u001B[36m";
                default:
                    return "\u001B[37m";  // Default color (white)
            }
        }
    }

    private static class CommandFactory {
        public static Command getCommand(String message) {
            if (message.startsWith("#JOIN ")) return new JoinCommand();
            if (message.startsWith("#MESSAGE ")) return new MessageCommand();
            if (message.startsWith("#PRIVATE ")) return new PrivateMessageCommand();
            if (message.startsWith("#COLORMESSAGE ")) return new ColorCommand();

            return null;
        }
    }

    private static class ColorDecorator implements ITextDecorator {
        private String color;

        public ColorDecorator(String color) {
            this.color = color;
        }

        @Override
        public String decorate(String text) {
            return color + text + "\u001B[0m";
        }
    }

    static class ClientHandler implements Runnable, IObserver {
        private Socket client;
        private PrintWriter out;
        private BufferedReader in;
        private ChatServerDemo2 server;
        private String name = "Guest";
        private List<String> inappropriateWords = Arrays.asList("Lort", "Dumme svin", "Idiot");

        public ClientHandler(Socket client, ChatServerDemo2 server) throws IOException {
            this.client = client;
            this.server = server;
            this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            this.out = new PrintWriter(client.getOutputStream(), true);
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public ChatServerDemo2 getServer() {
            return server;
        }

        @Override
        public void run() {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                        Command command = CommandFactory.getCommand(msg);
                        if (command != null) {
                            command.execute(this, msg);
                        }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void notify(String msg) {
            out.println(msg);
        }

        public void directMessage(String recipient, String msg) {
            for (IObserver obs : server.clients) {
                ClientHandler ch = (ClientHandler) obs;
                if (ch.name.equals(recipient)) {
                    ch.notify(name + ": " + msg);
                }
            }
        }

        public String replaceInappropriateWords(String msg) {
            for (String word : inappropriateWords) {
                if (msg.contains(word)) {
                    msg = msg.replace(word, "****");
                }
            }
            return msg;
        }
    }
}