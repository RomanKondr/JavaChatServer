package com.yourorganization.app.Java_Server;

import javax.swing.*;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private CommunicationServerGUI gui;
    private AtomicBoolean isAdmin = new AtomicBoolean(false);
    private AtomicBoolean nicknameSet = new AtomicBoolean(false);
    private String clientID;

    // Constructor with clientID
    public Client(String serverIP, int port, String clientID) {
        this.clientID = clientID;
        initializeClient(serverIP, port, clientID);
    }

    // Overloaded constructor without clientID
    public Client(String serverIP, int port) {
        this(serverIP, port, ""); // Delegate to the primary constructor with an empty clientID
    }

    private void initializeClient(String serverIP, int port, String clientID) {
        try {
            System.out.println("Attempting to connect to server at " + serverIP + ":" + port);
            socket = new Socket(serverIP, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            // Pass the writer to the GUI so it can use it
            this.gui = new CommunicationServerGUI(false, writer);
            
            setupMessageSending();
            sendMessage("CLIENT_ID " + clientID); // Send initial client ID message

            new Thread(this::receiveMessages).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to connect to the server: " + e.getMessage());
            System.exit(1);
        }
    }

    private void setupMessageSending() {
    	gui.sendButton.addActionListener(e -> sendMessageFromField());
        gui.addRequestDetailsButtonListener(e -> sendMessage("REQUEST_DETAILS"));
    }

    private void sendMessageFromField() {
        String message = gui.messageField.getText().trim();
        if (!message.isEmpty()) {
            sendMessage(message);
            if (nicknameSet.compareAndSet(false, true)) {
                gui.setNickname("Nickname: " + message);
            }
            gui.messageField.setText("");
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                processMessage(message);
            }
        } catch (IOException e) {
            handleDisconnect();
        }
    }

    private void processMessage(String message) {
        SwingUtilities.invokeLater(() -> {
        	if (message.startsWith("CURRENT_ADMIN")) {
        	    String adminDetails = message.substring("CURRENT_ADMIN ".length());
        	    isAdmin.set(false);
        	    gui.setAdminStatus(false); // The user is not an admin, so set admin status to false
        	    gui.setUserListVisibility(false); // Hide user list for non-admins
        	    gui.setRequestDetailsButtonVisibility(true);
        	    String adminInfo = message.substring("CURRENT_ADMIN ".length());
        	    gui.displayMessage("Current admin: " + adminInfo, false, false);
        	    
        	} else if ("ADMIN_DETAILS".equals(message)) {
                isAdmin.set(true);
                gui.setAdminStatus(true); // The user is now an admin, so set admin status to true
                gui.setUserListVisibility(true); // Show user list for admins
                gui.setRequestDetailsButtonVisibility(false);
            } else if (message.startsWith("DETAILS_REQUESTED_BY") && isAdmin.get()) {
                // This client is the admin and needs to respond to a details request
                gui.displayAdminRequest(message);
                
            } else if (message.startsWith("NICKNAME_CONFIRMED")) {
                updateNickname(message.split(" ", 2)[1]);
            } else if (message.startsWith("Private from") || message.startsWith("Private to")) {
                gui.displayMessage(message, true, true);
            } else if (message.startsWith("USER ")) {
                gui.addUser(message.substring("USER ".length()));
            } else if (message.equals("CLEAR_USER_LIST")) {
                gui.clearUserList();
            } else if (message.startsWith("DETAILS_RESPONSE")) {
                gui.displayMessage(message, false, true);
            } else {
                // Display the chat message
                gui.displayMessage(message, false, true);
            }
        });
    }

    
    
    private void handleNewAdminSelection(String message) {
        isAdmin.set(message.contains(clientID));
        gui.setAdminStatus(isAdmin.get());
        
        // If this client is the new admin, request an immediate user list update
        if (isAdmin.get()) {
            gui.setUserListVisibility(true);
            requestUserListUpdate();
        }
    }
    
    private void requestUserListUpdate() {
        sendMessage("REQUEST_USER_LIST_UPDATE"); // Tell the server to send the updated user list
    }

    private void updateNickname(String confirmedNickname) {
        gui.setNickname("Nickname: " + confirmedNickname);
        nicknameSet.set(true);
    }

    private void handleDisconnect() {
        JOptionPane.showMessageDialog(null, "Connection to server lost.");
        closeConnection();
        System.exit(1);
    }
    
    

    private void closeConnection() {
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    public void sendMessage(String message) {
        writer.println(message);
        writer.flush();
    }

    public static void main(String[] args) {
        String serverIP = "localhost";
        int port = 1234;
        String clientID = ""; // Optionally set or input clientID
        SwingUtilities.invokeLater(() -> new Client(serverIP, port, clientID));
    }
}
