package org.example;

public interface Command {


    void execute(ChatServerDemo2.ClientHandler client, String message);
}
