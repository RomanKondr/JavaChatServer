package com.yourorganization.app.Java_Server;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerGUI {
    private JFrame frame;
    private JTextArea logArea;
    private JLabel serverInfoLabel; 

    public ServerGUI() {
        frame = new JFrame("Server Monitor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        serverInfoLabel = new JLabel("Server IP: N/A | Port: N/A", SwingConstants.CENTER);
        serverInfoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(serverInfoLabel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void setServerStatus(boolean isRunning, int port) {
        SwingUtilities.invokeLater(() -> {
            String ip;
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                ip = "Unable to determine IP";
            }

            if (isRunning) {
                serverInfoLabel.setText("Server running at: " + ip + ":" + port);
            } else {
                serverInfoLabel.setText("Server not started. IP: N/A | Port: N/A");
            }
        });
    }
}
