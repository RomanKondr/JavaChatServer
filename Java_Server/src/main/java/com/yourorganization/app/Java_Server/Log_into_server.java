package com.yourorganization.app.Java_Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

public class Log_into_server {

    private static JButton loginButton; // Make loginButton a static field to access it inside ActionListener

    public static void main(String[] args) {
        createWindow();
    }

    static void createWindow() {
        JFrame frame = new JFrame("Communication Server Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.LIGHT_GRAY);

        JLabel headerLabel = new JLabel("Please Log in to the server", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        headerLabel.setForeground(new Color(0, 102, 204));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.LIGHT_GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        JTextField portField = new JTextField(20);
        JTextField serverIPField = new JTextField(20);
        JTextField idField = new JTextField(20); // ID field for user ID input

        addFormField(formPanel, "Port:", portField, gbc);
        addFormField(formPanel, "Server IP:", serverIPField, gbc);
        addFormField(formPanel, "ID (optional):", idField, gbc); // Add ID field to form

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginButton = new JButton("Log In");
        styleButton(loginButton, new Color(0, 153, 76), Color.WHITE);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Attempt the connection only if the login button is enabled
                if(loginButton.isEnabled()) {
                    tryConnection(serverIPField, portField, idField, frame); // Pass ID field to tryConnection method
                }
            }
        });

        JButton backButton = new JButton("Back");
        styleButton(backButton, new Color(255, 60, 60), Color.WHITE);
        backButton.addActionListener(e -> frame.dispose()); // Just dispose the frame to go back

        buttonPanel.add(loginButton);
        buttonPanel.add(backButton);
        buttonPanel.setOpaque(false);

        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private static void tryConnection(JTextField serverIPField, JTextField portField, JTextField idField, JFrame frame) {
        String serverIP = serverIPField.getText().trim();
        String portStr = portField.getText().trim();
        String clientID = idField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException("Invalid port number.");
            }
            loginButton.setEnabled(false);
            System.out.println("Connecting to server at " + serverIP + ":" + port);
            
            // Here you create a new Client instance with the server IP, port, and client ID
            new Client(serverIP, port, clientID);

            frame.dispose(); // Dispose the frame upon successful connection
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid port number (1-65535).");
            loginButton.setEnabled(true);
        }
    }

    private static void addFormField(JPanel panel, String labelText, JTextField textField, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(label, gbc);
        panel.add(textField, gbc);
    }

    private static void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setPreferredSize(new Dimension(120, 40));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setOpaque(true);
        button.setBorderPainted(false);
    }
}
