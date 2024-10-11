package com.yourorganization.app.Java_Server;

import java.io.*;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String userId;
    private String username;
    private boolean isAdmin;

    
    
    private Server server; // Reference to the server to call broadcastMessage
    
    public ClientHandler(Socket socket, Server server, String userId) {
        this.socket = socket;
        this.server = server;
        this.userId = userId;
        // Initialize input and output streams
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error setting up streams for client handler: " + e.getMessage());
            closeEverything();
        }
    }

    

    @Override
    public void run() {
        try {
            sendMessage("Please input your username:");
            username = input.readLine();

            if (username == null || username.trim().isEmpty()) {
                sendMessage("Invalid username. Connection closing.");
                closeEverything();
                return;
            }

            server.addClientHandler(this);

            boolean isAdmin = server.checkAndSetAdmin(this);
            if (isAdmin) {
                sendMessage("You are the admin.");
                
            } else {
                // Send the current admin details to this non-admin client
                ClientHandler currentAdmin = server.getAdminHandler();
                if (currentAdmin != null) {
                    sendMessage("CURRENT_ADMIN " + currentAdmin.getUsername() + " " + currentAdmin.getUserId());
                }
            }
            server.updateAdminUserList();
            server.broadcastMessage(username + " (" + userId + ") has joined the chat!", false);

            String message;
            while ((message = input.readLine()) != null) {
            	if ("REQUEST_USER_LIST_UPDATE".equals(message.trim())) {
                    if (this.isAdmin) {
                        server.sendUserListToAdmin(this);
                    }
                }
            	else if (message.startsWith("/private")) {
                    // Handle private messages for both admin and regular clients
                    handlePrivateMessage(message);
                } else if ("yes".equalsIgnoreCase(message) || "no".equalsIgnoreCase(message)) {
                    if (this == server.getAdminHandler()) {
                        // Only the admin can process these types of responses
                        server.processAdminResponse(message, this);
                    } else {
                        // If a non-admin sends "yes" or "no", treat it as a normal message
                        server.broadcastMessage(username + ": " + message, false);
                    }
                } else if ("REQUEST_DETAILS".equalsIgnoreCase(message.trim())) {
                    server.setDetailsRequester(this);
                    if (server.getAdminHandler() != null) {
                        server.getAdminHandler().sendMessage("DETAILS_REQUESTED_BY " + this.getUsername());
                        server.getAdminHandler().sendMessage("Please accept or decline the request by typing yes/no.");
                    }
                }  else if ("exit".equalsIgnoreCase(message.trim())) {
                    break;
                } else {
                    
                    server.broadcastMessage(username + ": " + message, false);
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client [" + userId + "]: " + e.getMessage());
        } finally {
            closeEverything();
        }
    }


    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
    
    public Socket getSocket() {
        return socket;
    }
    
    public String getAdminName() {
        return this == server.getAdminHandler() ? username : null;
    }
    
    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
        sendMessage(isAdmin ? "ADMIN_DETAILS" : "CURRENT_ADMIN " + getAdminName());
        if (isAdmin) {
            server.updateAdminUserList();
        }
    }
      
    private void handlePrivateMessage(String message) {
        // Split the message, but limit the number of parts to 3 to preserve spaces within the message itself
        String[] parts = message.split(" ", 3);
        if (parts.length < 3) {
            sendMessage("Incorrect private message format. Use /private userId \"message\"");
            return;
        }
        
        String nickname = parts[1]; 
    
        if (!parts[2].startsWith("\"") || !parts[2].endsWith("\"")) {
            sendMessage("The message must be enclosed in quotes.");
            return;
        }
        
        
        String privateMessage = parts[2].substring(1, parts[2].length() - 1);
        
        server.sendPrivateMessage(this, nickname, privateMessage);
    }

    void closeEverything() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing client handler streams and socket: " + e.getMessage());
        } finally {
            server.removeClientHandler(this);
            if (this.isAdmin) {
                server.electNewAdmin();
            }
        }
    }
   

    public void sendMessage(String message) {
        output.println(message);
        output.flush();
    }
}
