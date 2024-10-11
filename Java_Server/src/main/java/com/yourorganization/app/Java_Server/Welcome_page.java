package com.yourorganization.app.Java_Server;

import javax.swing.*;
import java.awt.*;

import java.io.IOException;
import java.net.ServerSocket;

public class Welcome_page {
    private static ServerGUI serverGUI;

    public static void main(String[] args) {
        createWelcomeWindow();
    }

    static void createWelcomeWindow() {
        JFrame frame = new JFrame("Welcome");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.LIGHT_GRAY);

        JLabel welcomeLabel = new JLabel("Welcome to the Communication Server", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        welcomeLabel.setForeground(new Color(0, 102, 204));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        JButton createServerButton = new JButton("Create Server");
        styleButton(createServerButton, new Color(0, 153, 76), Color.WHITE);

        JButton joinServerButton = new JButton("Join Server");
        styleButton(joinServerButton, new Color(0, 153, 76), Color.WHITE);
        joinServerButton.addActionListener(e -> {
            frame.dispose(); 
            Log_into_server.createWindow(); 
        });
        
        createServerButton.addActionListener(e -> {
            serverGUI = new ServerGUI(); 
            frame.dispose(); 

            new Thread(() -> {
                try (ServerSocket serverSocket = new ServerSocket(1234)) { 
                    serverGUI.setServerStatus(true, serverSocket.getLocalPort());
                    
                    Server server = new Server(serverSocket, serverGUI);  
                    server.startServer();
                } catch (IOException ioException) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Could not start the server. Port may be in use."));
                    ioException.printStackTrace();
                }
            }).start();
        });

        buttonPanel.add(createServerButton);
        buttonPanel.add(joinServerButton);

        mainPanel.add(welcomeLabel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private static void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(200, 50));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setBorderPainted(false);
        button.setOpaque(true);
    }
}

