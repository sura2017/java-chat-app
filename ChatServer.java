import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    // Port number for the communication
    private static final int PORT = 12345;
    
    // Set to store all connected client handlers to broadcast messages
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Chat Server started...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Accept new client connection
                Socket socket = serverSocket.accept();
                System.out.println("New user connected!");

                // Create a new handler for this client and start it
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Broadcast a message to all clients except the sender
    public static void broadcast(String message, ClientHandler excludeUser) {
        for (ClientHandler client : clientHandlers) {
            if (client != excludeUser) {
                client.sendMessage(message);
            }
        }
    }

    // Remove client when they disconnect
    public static void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        System.out.println("A user disconnected.");
    }

    // Inner class to handle each client connection
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // Setup streams
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Ask for username
                out.println("Enter your username:");
                username = in.readLine();
                System.out.println("User " + username + " connected.");
                
                ChatServer.broadcast(username + " has joined the chat!", this);

                String message;
                while ((message = in.readLine()) != null) {
                    ChatServer.broadcast(username + ": " + message, this);
                }

            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ChatServer.removeClient(this);
                ChatServer.broadcast(username + " has left the chat.", this);
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }
}