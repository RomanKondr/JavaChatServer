package com.yourorganization.app.Java_Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;



import java.util.Timer;
import java.util.TimerTask;

public class Server {
	private Timer timer;
    private volatile ClientHandler adminHandler = null; // Tracks the admin client handler
    private ServerSocket serverSocket;
    private ServerGUI serverGUI;
    private volatile ClientHandler detailsRequester = null;
    private ConcurrentHashMap<String, ClientHandler> clientHandlersMap = new ConcurrentHashMap<>(); // For fast lookup
    private CopyOnWriteArrayList<ClientHandler> clientHandlers = new CopyOnWriteArrayList<>(); // For iteration
    
    


    public Server(ServerSocket serverSocket, ServerGUI serverGUI) {
        this.serverSocket = serverSocket;
        this.serverGUI = serverGUI;
    }

    public void startServer() {
        try {
            serverGUI.setServerStatus(true, serverSocket.getLocalPort());
            serverGUI.log("Server started and waiting for connections...");

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();

                
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String initialMessage = bufferedReader.readLine(); // This should be in format "CLIENT_ID your_id"
                if (initialMessage != null && initialMessage.startsWith("CLIENT_ID")) {
                    String userId = initialMessage.split(" ")[1]; // Get the user ID from the message

                    serverGUI.log("Connection accepted from " + socket.getRemoteSocketAddress() + " with ID " + userId);
                    ClientHandler clientHandler = new ClientHandler(socket, this, userId);
                    clientHandlers.add(clientHandler);
                    serverGUI.log("Starting client handler thread for: " + userId); // Use userId for logging
                    new Thread(clientHandler).start();
                } else {
                    socket.close(); // Close the connection if the initial message is not formatted correctly
                    serverGUI.log("Connection closed. Initial message was not as expected.");
                }
            }
            
        } catch (IOException e) {
            serverGUI.log("Error: " + e.getMessage());
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            serverGUI.log("Error closing the server socket.");
        }
    }
    
    

    public synchronized void updateAdminUserList() {
        if (adminHandler != null) {
            adminHandler.sendMessage("CLEAR_USER_LIST");
            for (ClientHandler client : clientHandlers) {
                // Check if the client is the admin, include the admin in the user list as well
                String userInfo = "USER " + client.getUsername() + " (" + client.getUserId() + ")";
                adminHandler.sendMessage(userInfo);
            }
        }
    }
    
    public void shutdown() {
        
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                serverGUI.log("Error closing the server socket: " + e.getMessage());
            }
        }

        

        
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.closeEverything();
        }

        serverGUI.log("Server has been shut down.");
    }

    public synchronized void addClientHandler(ClientHandler clientHandler) {
        if (!clientHandlers.contains(clientHandler)) {
            clientHandlers.add(clientHandler);
            clientHandlersMap.put(clientHandler.getUserId(), clientHandler);
            String joinMessage = clientHandler.getUsername() + " (" + clientHandler.getUserId() + ") has joined the chat!";
            broadcastMessage(joinMessage, false);
            if (adminHandler != null && clientHandler != adminHandler) {
                clientHandler.sendMessage("CURRENT_ADMIN " + adminHandler.getUsername());
            }
            updateAdminUserList(); // Update the admin user list with the new client
        }
    }

    public synchronized void removeClientHandler(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        clientHandlersMap.remove(clientHandler.getUserId());
        serverGUI.log("Client disconnected: " + clientHandler.getUsername());
        updateAdminUserList(); // Update the admin user list after removing the client
        
        if (clientHandler.equals(adminHandler)) {
            selectNewAdmin(); // Call a method to select a new admin
        }
    }
    
    private void selectNewAdmin() {
        
        synchronized (this) {
            if (!clientHandlers.isEmpty()) {
                // Randomly select a new admin from the list
                int newAdminIndex = (int) (Math.random() * clientHandlers.size());
                ClientHandler newAdmin = clientHandlers.get(newAdminIndex);

                // Inform the old admin that they are no longer admin, if necessary
                if (adminHandler != null) {
                    adminHandler.setAdmin(false);
                    adminHandler.sendMessage("NOT_ADMIN");
                }

                // Update the reference to the new admin in the adminHandler variable
                adminHandler = newAdmin;

                // Set the new admin
                newAdmin.setAdmin(true);
                newAdmin.sendMessage("You are the admin.");
                
                // Send the user list to the new admin - Make sure this method does what's intended
                sendUserListToAdmin(newAdmin); 

                // Notify all clients (except the new admin) about the new admin
                for (ClientHandler client : clientHandlers) {
                    if (client != newAdmin) {
                        client.sendMessage("NEW_ADMIN " + newAdmin.getUsername());
                    }
                }

                // Finally, notify the new admin that they are the admin.
                newAdmin.sendMessage("ADMIN_DETAILS"); 
            } else {
                adminHandler = null; // No clients connected, no admin
            }
        }
    }
    
    public void sendUserListToAdmin(ClientHandler admin) {
        if (admin != null && admin.isAdmin()) {
            admin.sendMessage("CLEAR_USER_LIST");
            for (ClientHandler client : clientHandlers) {
                String userInfo = "USER " + client.getUsername() + " (" + client.getUserId() + ")";
                admin.sendMessage(userInfo);
            }
        }
    }

    public synchronized boolean checkAndSetAdmin(ClientHandler clientHandler) {
        if (adminHandler == null) {
            adminHandler = clientHandler;
            return true;
        }
        return false;
    }
    
    public void processAdminResponse(String response, ClientHandler admin) {
        if (admin == adminHandler && detailsRequester != null) {
            if ("yes".equalsIgnoreCase(response)) {
                sendClientDetailsToRequester(detailsRequester);
            } else if ("no".equalsIgnoreCase(response)) {
                detailsRequester.sendMessage("Your request for details has been denied by the admin.");
            }
            clearDetailsRequester(); 
        }
    }
    
    private void clearDetailsRequester() {
        this.detailsRequester = null;
    }
    
    public void setDetailsRequester(ClientHandler requester) {
        this.detailsRequester = requester;
    }

    public ClientHandler getDetailsRequester() {
        return this.detailsRequester;
    }
    
    public void requestClientDetails(ClientHandler requester) {
        if (requester != adminHandler) {
            // Send the request to the admin
            if (adminHandler != null) {
                adminHandler.sendMessage("DETAILS_REQUESTED_BY " + requester.getUsername());
                // Send the prompt message to the admin
                adminHandler.sendMessage("Please accept or decline the request by typing yes/no.");
            }
        }
    }
    
    public void sendClientDetailsToRequester(ClientHandler requester) {
        StringBuilder details = new StringBuilder("DETAILS_RESPONSE: ");
        for (ClientHandler client : clientHandlers) {
            details.append(client.getUsername())
                   .append(", ID: ").append(client.getUserId())
                   .append(", IP: ").append(client.getSocket().getInetAddress().getHostAddress())
                   .append(", Port: ").append(client.getSocket().getPort()).append("; ");
        }
        requester.sendMessage(details.toString());
    }
    
    
    
    public ClientHandler getAdminHandler() {
        return adminHandler;
    }
    
    
    
    public void sendPrivateMessage(ClientHandler sender, String recipientNickname, String message) {
        // Iterate over the clientHandlers list to find the recipient by nickname
        boolean recipientFound = false;
        for (ClientHandler client : clientHandlers) {
            if (client.getUsername().equalsIgnoreCase(recipientNickname)) {
                // Format the private message to indicate who it's from and send it to the recipient
                String formattedMessage = "Private from " + sender.getUsername() + ": " + message;
                client.sendMessage(formattedMessage);
                recipientFound = true;
                break;
            }
        }
        // If the recipient is found, also send the sender a confirmation of the sent private message
        if (recipientFound) {
            String confirmationMessage = "Private to " + recipientNickname + ": " + message;
            sender.sendMessage(confirmationMessage);
        } else {
            // Inform the sender that the recipient was not found
            sender.sendMessage("Nickname " + recipientNickname + " not found.");
        }
    }
    
    public synchronized void electNewAdmin() {
        if (!clientHandlers.isEmpty()) {
            ClientHandler newAdmin = clientHandlers.get(0); // Just an example to pick the first non-admin client.
            adminHandler = newAdmin;
            newAdmin.setAdmin(true);
            updateAdminUserList(); // Call this immediately after setting new admin
            for (ClientHandler clientHandler : clientHandlers) {
                if (clientHandler != newAdmin) {
                    clientHandler.sendMessage("CURRENT_ADMIN " + newAdmin.getUsername());
                } else {
                    clientHandler.sendMessage("ADMIN_DETAILS");
                }
            }
        }
    }
    
    

    public void broadcastMessage(String message, boolean toAdminOnly) {
        if (toAdminOnly && adminHandler != null) {
            adminHandler.sendMessage(message);
        } else {
            for (ClientHandler clientHandler : clientHandlers) {
                clientHandler.sendMessage(message);
            }
        }
    }

    public void injectMockedClientHandler(ClientHandler mockedHandler) {
        this.clientHandlers.add(mockedHandler);

        this.clientHandlersMap.put(mockedHandler.getUserId(), mockedHandler);

        if (mockedHandler.isAdmin()) {
            this.adminHandler = mockedHandler;
        }
    }
    
    
}
