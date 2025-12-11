package com.chat;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ChatServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private boolean isRunning = true;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;

        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // First message from client is the username
            username = in.readLine();
            if (username == null || username.trim().isEmpty()) {
                username = "Anonymous";
            }

            // Notify server of new connection
            server.clientConnected(this);

            // Send welcome message to the client
            sendMessage("Welcome to the chat, " + username + "!");
            sendMessage("Type your message and press Enter or click Send");

            // Listen for messages from this client
            String message;
            while (isRunning && (message = in.readLine()) != null) {
                if (!message.trim().isEmpty()) {
                    server.broadcast(username + ": " + message, this);
                }
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Error handling client: " + e.getMessage());
            }
        } finally {
            close();
            server.removeClient(this);
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public String getUsername() {
        return username != null ? username : "Unknown";
    }

    public void close() {
        isRunning = false;
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}