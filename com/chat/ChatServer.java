package com.chat;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ChatServer extends JFrame {

    private static final int PORT = 5000;
    private JTextArea logArea;
    private JTextArea clientListArea;
    private JLabel statusLabel;
    private JLabel clientCountLabel;
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private boolean isRunning = false;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public ChatServer() {
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Chat Server Console");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(30, 30, 35));

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(45, 45, 50));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Chat Server Console ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(100, 200, 255));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        statusLabel = new JLabel("Stopped");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(new Color(255, 100, 100));
        headerPanel.add(statusLabel, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center panel with logs and client list
        JPanel centerPanel = new JPanel(new BorderLayout(10, 0));
        centerPanel.setOpaque(false);

        // Log area panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(new Color(25, 25, 30));
        logPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 80)),
                "Server Logs",
                0, 0, new Font("Segoe UI", Font.BOLD, 12), new Color(150, 150, 160)
        ));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setBackground(new Color(20, 20, 25));
        logArea.setForeground(new Color(200, 200, 210));
        logArea.setCaretColor(Color.WHITE);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(null);
        logPanel.add(logScroll, BorderLayout.CENTER);

        centerPanel.add(logPanel, BorderLayout.CENTER);

        // Client list panel
        JPanel clientPanel = new JPanel(new BorderLayout());
        clientPanel.setBackground(new Color(25, 25, 30));
        clientPanel.setPreferredSize(new Dimension(200, 0));

        JPanel clientHeaderPanel = new JPanel(new BorderLayout());
        clientHeaderPanel.setBackground(new Color(35, 35, 40));
        clientHeaderPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel clientsLabel = new JLabel("Connected Clients");
        clientsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        clientsLabel.setForeground(new Color(150, 150, 160));
        clientHeaderPanel.add(clientsLabel, BorderLayout.WEST);

        clientCountLabel = new JLabel("0");
        clientCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clientCountLabel.setForeground(new Color(100, 255, 150));
        clientHeaderPanel.add(clientCountLabel, BorderLayout.EAST);

        clientPanel.add(clientHeaderPanel, BorderLayout.NORTH);

        clientListArea = new JTextArea();
        clientListArea.setEditable(false);
        clientListArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        clientListArea.setBackground(new Color(20, 20, 25));
        clientListArea.setForeground(new Color(180, 180, 190));
        JScrollPane clientScroll = new JScrollPane(clientListArea);
        clientScroll.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 60)));
        clientPanel.add(clientScroll, BorderLayout.CENTER);

        centerPanel.add(clientPanel, BorderLayout.EAST);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Control buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(35, 35, 40));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton startButton = createStyledButton("Start Server", new Color(50, 180, 100));
        JButton stopButton = createStyledButton("Stop Server", new Color(220, 80, 80));
        JButton clearButton = createStyledButton("Clear Logs", new Color(100, 150, 200));

        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());
        clearButton.addActionListener(e -> logArea.setText(""));

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(clearButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = timeFormat.format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void updateClientList() {
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder();
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    sb.append("- ").append(client.getUsername()).append("\n");
                }
            }
            clientListArea.setText(sb.toString());
            clientCountLabel.setText(String.valueOf(clients.size()));
        });
    }

    private void startServer() {
        if (isRunning) {
            log("Server is already running!");
            return;
        }

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                isRunning = true;
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Running on port " + PORT);
                    statusLabel.setForeground(new Color(100, 255, 150));
                });
                log("Server started successfully on port " + PORT);
                log("Waiting for client connections...");

                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ClientHandler handler = new ClientHandler(clientSocket, this);
                        clients.add(handler);
                        new Thread(handler).start();
                    } catch (SocketException e) {
                        if (isRunning) {
                            log("Socket error: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                log("Failed to start server: " + e.getMessage());
            }
        }).start();
    }

    private void stopServer() {
        if (!isRunning) {
            log("Server is not running!");
            return;
        }

        isRunning = false;

        try {
            // Close all client connections
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.close();
                }
                clients.clear();
            }

            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Stopped");
                statusLabel.setForeground(new Color(255, 100, 100));
            });
            updateClientList();
            log("Server stopped");
        } catch (IOException e) {
            log("Error stopping server: " + e.getMessage());
        }
    }

    public void broadcast(String message, ClientHandler sender) {
        log("Message: " + message);
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        updateClientList();
        log(client.getUsername() + " disconnected");
        broadcast("[System] " + client.getUsername() + " has left the chat", null);
    }

    public void clientConnected(ClientHandler client) {
        updateClientList();
        log(client.getUsername() + " connected");
        broadcast("[System] " + client.getUsername() + " has joined the chat", null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatServer::new);
    }
}