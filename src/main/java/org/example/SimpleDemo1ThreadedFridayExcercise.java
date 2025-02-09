package org.example;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class SimpleDemo1ThreadedFridayExcercise {
    private ServerSocket server;
    private static String echoMessage = ""; // Store message from POST requests.

    // Purpose: To start a new ServerSocket and endlessly listen for client connections
    public void run(int port) {
        try {
            server = new ServerSocket(port);
            System.out.println("Server ready to receive requests");
            while (true) {
                Socket clientSocket = server.accept();
                System.out.println("Client registered");
                // Create a container for the client socket so that we can have each end of the communication pipe (in, out).
                Runnable clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new SimpleDemo1ThreadedFridayExcercise().run(8080);
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String requestLine = in.readLine();

                if (requestLine != null) {
                    System.out.println("Request: " + requestLine);
                    String[] requestParts = requestLine.split(" ");

                    if (requestParts.length == 2) {
                        // Add "HTTP/1.1" if version is missing.
                        requestLine += " HTTP/1.1";
                        requestParts = requestLine.split(" ");
                    }

                    if (requestParts.length < 3) {
                        sendErrorResponse(out, "400 Bad Request", "HTTP/1.1");
                        return;
                    }

                    String method = requestParts[0];
                    String path = requestParts[1];
                    String version = requestParts[2];

                    if (method.equals("GET")) {
                        handleGetRequest(out, path, version);
                    } else if (method.equals("POST")) {
                        handlePostRequest(in, out, path, version);
                    } else {
                        sendErrorResponse(out, "405 Method Not Allowed", version);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void handleGetRequest(PrintWriter out, String path, String version) {
            String content = "";
            String statusCode = "200 OK";
            String contentType = "text/plain";

            switch (path) {
                case "/hello":
                    content = "Hello, world!";
                    break;
                case "/time":
                    content = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z").format(new Date());
                    break;
                case "/echo":
                    content = echoMessage.isEmpty() ? "No message stored." : echoMessage;
                    break;
                default:
                    statusCode = "404 Not Found";
                    content = "The requested resource was not found.";
                    break;
            }

            sendResponse(out, statusCode, version, contentType, content);
        }

        public void handlePostRequest(BufferedReader in, PrintWriter out, String path, String version) throws IOException {
            if (!path.equals("/echo")) {
                sendErrorResponse(out, "404 Not Found", version);
                return;
            }

            // Read headers to get content length
            String line;
            int contentLength = 0;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(": ")[1]);
                }
            }

            // Read body
            char[] body = new char[contentLength];
            in.read(body, 0, contentLength);
            echoMessage = new String(body).trim(); // Store the message

            sendResponse(out, "200 OK", version, "text/plain", "Message stored.");
        }

        public void sendResponse(PrintWriter out, String statusCode, String version, String contentType, String content) {
            Date now = new Date();
            String date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z").format(now);

            out.println(version + " " + statusCode);
            out.println("Date: " + date);
            out.println("Server: SimpleDemo1Threaded");
            out.println("Content-Type: " + contentType);
            out.println("Content-Length: " + content.length());
            out.println();
            out.println(content);
        }

        public void sendErrorResponse(PrintWriter out, String statusCode, String version) {
            sendResponse(out, statusCode, version, "text/plain", "Error: " + statusCode);
        }
    }
}
