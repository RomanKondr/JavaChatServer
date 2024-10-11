package com.yourorganization.app.Java_Server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.text.Style;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class CommunicationServerGUI {
    private JFrame frame;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private JLabel countdownLabel;
    JTextPane chatPane;
    StyledDocument chatDocument;
    private DefaultListModel<String> userListModel;
    public JList<String> userList; 
    private JTextArea chatArea; 
    JTextField messageField; 
    JButton sendButton; 
    private JButton requestDetailsButton; 
    
    private boolean isAdmin = false;
    private PrintWriter writer;
    private JLabel nicknameLabel; 
    private JSplitPane splitPane;
    
    private String[] currentUsers = new String[0];
    private final int USER_PANEL_PREFERRED_WIDTH = 150;
    private Timer visibilityTimer;
    private int showDuration = 10000; // Duration to show the user list
    private int hideDuration = 3000; // Duration to hide the user list
    private boolean isUserListVisible = false; 
    
   
    

    public CommunicationServerGUI(boolean isAdmin, PrintWriter writer) {
        
        this.isAdmin = isAdmin;
        this.writer = writer; // Assign the writer received from Client
        initializeGUI();
         
        
    }

    private void initializeGUI() {
    	
    	    frame = new JFrame("Communication Server");
    	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	    frame.setSize(600, 400);
    	    frame.setLocationRelativeTo(null);

    	    chatPane = new JTextPane();
    	    chatPane.setEditable(false);
    	    chatDocument = chatPane.getStyledDocument();
    	    setupTextStyles();
    	    JScrollPane chatScrollPane = new JScrollPane(chatPane);
    	    JPanel chatPanel = new JPanel(new BorderLayout());
    	    chatPanel.add(new JLabel("Chat", SwingConstants.CENTER), BorderLayout.NORTH);
    	    chatPanel.add(chatScrollPane, BorderLayout.CENTER);

    	    messageField = new JTextField();
    	    sendButton = new JButton("Send");
    	    JPanel messagePanel = new JPanel(new BorderLayout());
    	    messagePanel.add(messageField, BorderLayout.CENTER);
    	    messagePanel.add(sendButton, BorderLayout.EAST);

    	    requestDetailsButton = new JButton("Request Information");
    	    messagePanel.add(requestDetailsButton, BorderLayout.WEST);
    	    requestDetailsButton.setVisible(isAdmin);

    	    userListModel = new DefaultListModel<>();
    	    userList = new JList<>(userListModel);
    	    JScrollPane userScrollPane = new JScrollPane(userList);

    	    JLabel usersLabel = new JLabel("Users", SwingConstants.CENTER);
    	    usersLabel.setPreferredSize(new Dimension(frame.getWidth(), 20)); 
    	    usersLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20)); 

    	    JPanel usersPanel = new JPanel(new BorderLayout());
    	    usersPanel.add(usersLabel, BorderLayout.NORTH);
    	    usersPanel.add(userScrollPane, BorderLayout.CENTER);

    	    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatPanel, usersPanel);
    	    splitPane.setDividerLocation(450);

    	    nicknameLabel = new JLabel("Nickname: Not set", SwingConstants.CENTER);
    	    
    	    
    	    JPanel topPanel = new JPanel(new BorderLayout());
    	    
    	    topPanel.add(nicknameLabel, BorderLayout.SOUTH);

    	    frame.add(topPanel, BorderLayout.NORTH);
    	    frame.add(splitPane, BorderLayout.CENTER);
    	    frame.add(messagePanel, BorderLayout.SOUTH);

        // Action listeners for buttons
        addActionListeners();

        // Display the window
        frame.setLocationRelativeTo(null);
        
        
        if (userList.isVisible()) {
            visibilityTimer = new Timer(1000, new ActionListener() {
                private int countdown = showDuration / 1000; // Start by showing the list for 10 seconds.

                @Override
                public void actionPerformed(ActionEvent e) {
                    countdown--;
                   

                    if (countdown <= 0) {
                        isUserListVisible = !isUserListVisible;
                        userList.setVisible(isUserListVisible);
                        countdown = isUserListVisible ? hideDuration / 1000 : showDuration / 1000;
                    }
                }
            });

            visibilityTimer.setInitialDelay(0); // Start timer immediately.
            visibilityTimer.start();
        }
        
        frame.setVisible(true);
        
    }
    
    
    
    
    public void showUserListPanel(boolean show) {
        userList.setVisible(show);
    }
    
    
    
    public void updateUserList(String[] usernames) {
        currentUsers = usernames; // Store the latest list of users.
        updateUserListContents(); // Update the contents of the user list.
    }
    
    private void updateUserListContents() {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear(); // Clear the current list.
            for (String user : currentUsers) {
                userListModel.addElement(user); // Add each user to the list model.
            }
        });
    }
    

    
    private void addActionListeners() {
        sendButton.addActionListener(e -> {
            String inputText = messageField.getText().trim();
            if (!inputText.isEmpty()) {
                writer.println(inputText);
                writer.flush();
                messageField.setText("");
                messageField.requestFocusInWindow();
            }
        });

        requestDetailsButton.addActionListener(e -> {
            writer.flush();
        });
    }
    
    
    
    

    public void addRequestDetailsButtonListener(ActionListener actionListener) {
        requestDetailsButton.addActionListener(actionListener);
    }
    
    public void setCountdownLabelText(String text) {
        if (countdownLabel != null) {
            countdownLabel.setText(text);
        }
    }
    
    
    private void setupTextStyles() {
        // Regular message style
        Style regularStyle = chatPane.addStyle("Regular_Style", null);
        StyleConstants.setForeground(regularStyle, Color.BLACK);

        // Private message style
        Style privateStyle = chatPane.addStyle("Private_Style", null);
        StyleConstants.setForeground(privateStyle, Color.BLUE);
    }

    
    
    public void showRequestInformationButton(boolean show) {
        SwingUtilities.invokeLater(() -> {
            requestDetailsButton.setVisible(show);
        });
    }

    public void displayMessage(String message, boolean isPrivate, boolean addTimestamp) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize formattedMessage variable
                String formattedMessage;

                // Check if the message is one of the special cases where we never add a timestamp
                if ("Please input your username:".equals(message) || "You are the admin.".equals(message)|| "Please accept or decline the request by typing yes/no.".equals(message)) {
                    formattedMessage = message; // Use the message as is, without a timestamp
                } else if (addTimestamp) {
                    // If it's not a special case and addTimestamp is true, prepend the timestamp to the message
                    String timestamp = timeFormat.format(new Date());
                    formattedMessage = String.format("[%s] %s", timestamp, message);
                } else {
                    // If addTimestamp is false, use the message as is
                    formattedMessage = message;
                }

                // Determine the style based on whether the message is private or not
                Style style = isPrivate ? chatPane.getStyle("Private_Style") : chatPane.getStyle("Regular_Style");

                // Insert the message into the chatDocument
                chatDocument.insertString(chatDocument.getLength(), formattedMessage + "\n", style);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    
    
    
    

    public void setAdminStatus(boolean isAdmin) {
        SwingUtilities.invokeLater(() -> {
            this.isAdmin = isAdmin;
            requestDetailsButton.setVisible(isAdmin); // The button should be visible only to the admin
            setUserListVisibility(isAdmin); // The user list should be visible only to the admin
        });
    }
    
    
    public void addUser(String username) {
        SwingUtilities.invokeLater(() -> userListModel.addElement(username));
    }

    public void removeUser(String username) {
        SwingUtilities.invokeLater(() -> userListModel.removeElement(username));
    }

    public void clearUserList() {
        SwingUtilities.invokeLater(userListModel::clear);
    }


    
    public void setRequestDetailsButtonVisibility(boolean visible) {
        SwingUtilities.invokeLater(() -> {
            requestDetailsButton.setVisible(visible);
        });
    }
    
    public void displayAdminRequest(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                chatDocument.insertString(chatDocument.getLength(), message + "\n", null);
                // Make sure to scroll to the new message
                chatPane.setCaretPosition(chatDocument.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    
    public void displayRequest(String request) {
        SwingUtilities.invokeLater(() -> {
            // Display the request in the chat area with instruction
            chatArea.append(request + "\nPlease accept or decline the request by typing yes/no.\n");
        });
    }
    
    
    
    public void setUserListVisibility(boolean visible) {
        SwingUtilities.invokeLater(() -> {
            if (splitPane != null) {
                if (visible) {
                    int dividerLocation = frame.getWidth() - USER_PANEL_PREFERRED_WIDTH;
                    splitPane.setDividerLocation(dividerLocation);
                    splitPane.setEnabled(true);
                } else {
                    // Hide the user panel for non-admins
                    splitPane.setDividerLocation(Integer.MAX_VALUE);
                    splitPane.setEnabled(false);
                    splitPane.getRightComponent().setMinimumSize(new Dimension(0, 0));
                    splitPane.setResizeWeight(1);
                }
            }
        });
    }

    public void setNickname(String nickname) {
        nicknameLabel.setText(nickname);
    }
}
