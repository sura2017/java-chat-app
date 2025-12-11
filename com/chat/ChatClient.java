package com.chat;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatClient extends JFrame {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;

    private JTextArea chatArea;
    private JTextField messageField;
    private JTextField usernameField;
    private JButton sendButton;
    private JButton connectButton;
    private JLabel statusLabel;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private boolean isConnected = false;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    public ChatClient() {
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 650);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(400, 500));

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 249, 250));

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(59, 130, 246));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Chat Application");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        statusLabel = new JLabel("Disconnected");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(new Color(254, 202, 202));
        headerPanel.add(statusLabel, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Connection panel
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        connectionPanel.setBackground(new Color(241, 245, 249));
        connectionPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLabel.setForeground(new Color(71, 85, 105));
        connectionPanel.add(userLabel);

        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        connectionPanel.add(usernameField);

        connectButton = createStyledButton("Connect", new Color(34, 197, 94));
        connectButton.addActionListener(e -> toggleConnection());
        connectionPanel.add(connectButton);

        // Chat area panel
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(Color.WHITE);
        chatPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(Color.WHITE);
        chatArea.setForeground(new Color(30, 41, 59));
        chatArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        // --- FIXED: Added the missing centerPanel definition here ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(connectionPanel, BorderLayout.NORTH);
        centerPanel.add(chatPanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        // ------------------------------------------------------------

        // Message input panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(new Color(248, 250, 252));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        messageField = new JTextField();
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        messageField.setEnabled(false);
        messageField.addActionListener(e -> sendMessage());
        inputPanel.add(messageField, BorderLayout.CENTER);

        sendButton = createStyledButton("Send", new Color(59, 130, 246));
        sendButton.setEnabled(false);
        sendButton.addActionListener(e -> sendMessage());
        inputPanel.add(sendButton, BorderLayout.EAST);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);

        // Focus on username field
        usernameField.requestFocus();
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        return button;
    }

    private void toggleConnection() {
        if (isConnected) {
            disconnect();
        } else {
            connect();
        }
    }

    private void connect() {
        username = usernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a username!",
                    "Username Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send username to server
            out.println(username);

            isConnected = true;
            updateConnectionStatus(true);

            // Start listening thread
            new Thread(this::listenForMessages).start();

            appendMessage("Connected to server as " + username);

        } catch (IOException e) {
            appendMessage("Failed to connect: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Could not connect to server.\nMake sure the server is running!",
                    "Connection Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void disconnect() {
        try {
            isConnected = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            updateConnectionStatus(false);
            appendMessage("Disconnected from server");
        } catch (IOException e) {
            appendMessage("Error disconnecting: " + e.getMessage());
        }
    }

    private void updateConnectionStatus(boolean connected) {
        SwingUtilities.invokeLater(() -> {
            if (connected) {
                statusLabel.setText("Connected");
                statusLabel.setForeground(new Color(187, 247, 208));
                connectButton.setText("Disconnect");
                connectButton.setBackground(new Color(239, 68, 68));
                usernameField.setEnabled(false);
                messageField.setEnabled(true);
                sendButton.setEnabled(true);
                messageField.requestFocus();
            } else {
                statusLabel.setText("Disconnected");
                statusLabel.setForeground(new Color(254, 202, 202));
                connectButton.setText("Connect");
                connectButton.setBackground(new Color(34, 197, 94));
                usernameField.setEnabled(true);
                messageField.setEnabled(false);
                sendButton.setEnabled(false);
            }
        });
    }

    private void listenForMessages() {
        try {
            String message;
            while (isConnected && (message = in.readLine()) != null) {
                final String msg = message;
                SwingUtilities.invokeLater(() -> appendMessage(msg));
            }
        } catch (IOException e) {
            if (isConnected) {
                SwingUtilities.invokeLater(() -> {
                    appendMessage("Connection lost: " + e.getMessage());
                    disconnect();
                });
            }
        }
    }

    private void sendMessage() {
        if (!isConnected || out == null) {
            return;
        }

        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            messageField.setText("");
        }
    }

    private void appendMessage(String message) {
        String time = timeFormat.format(new Date());
        chatArea.append("[" + time + "] " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(ChatClient::new);
    }
}