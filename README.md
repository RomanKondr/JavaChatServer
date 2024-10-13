# Java Client-Server Chat Application

## Project Overview

This project is a **Java-based client-server chat application** designed for real-time communication between multiple clients. The system follows a **client-server architecture** where clients connect to a central server to exchange messages. One user is assigned as an **admin** with additional privileges such as managing users and handling special requests. The system includes a **Graphical User Interface (GUI)** for both the client and the server, making it more user-friendly.

## Key Features

- **Real-Time Communication**: Multiple clients can connect and communicate in real-time.
- **Admin Privileges**: The admin has special privileges such as managing the user list and handling client requests.
- **Private Messaging**: Clients can send private messages to each other using the `/private` command.
- **Dynamic Admin Assignment**: If the admin disconnects, the system automatically assigns a new admin from the remaining users.
- **Secure User Management**: Only registered users can post messages, and user authentication (sign-in and sign-up) is enforced.
- **Graphical User Interface**: Both server and clients have user-friendly GUIs for interaction.
- **SQL Injection Prevention**: Security measures are implemented to guard against SQL injections.
  
## Technologies Used

- **Java**: Core programming language.
- **Swing**: GUI framework used to create client and server interfaces.
- **Socket Programming**: Facilitates the connection between clients and server.
- **Multithreading**: Ensures that the server can handle multiple client connections simultaneously.
- **Maven**: Optional for project management and dependency resolution.

## Project Structure

- `Client`: Handles the client-side connection to the server and user interface interaction.
- `ClientHandler`: Manages the communication and interaction between the server and individual clients.
- `Server`: Handles client connections, message broadcasting, and admin assignments.
- `CommunicationServerGUI`: Provides a client-side graphical user interface for message handling.
- `ServerGUI`: Provides a server-side graphical user interface for monitoring server activity and connections.
- `Log_into_server`: Provides a GUI for clients to log into the server.
- `Welcome_page`: Provides a GUI for creating a server.

## How to Run

### Prerequisites
- **Java Development Kit (JDK)**: Ensure JDK 8 or higher is installed on your system.
- **Maven** (Optional): For managing dependencies and building the project.

### Running the Server

- Run the Welcome_Page file
- To start the server click on "Create Server" button
- The server will start and wait for incoming client connections on the default port (1234). Modify the port in the code if necessary.
- To join server click "Join Server" button , Enter the server's IP address and port number to connect. Optionally, you can enter a unique client ID.


### Messaging
After connecting to the server, you can start exchanging messages with other connected users. Admins have additional privileges, such as managing users and responding to special requests.


## Key Functionalities

- **Message Broadcasting**: All users can broadcast public messages.
- **Private Messaging**: Use the `/private <username> <message>` command to send direct messages to other users.
- **Admin Control**: Admins can view the user list, respond to requests, and manage connected users.
- **Automatic Admin Assignment**: If the current admin disconnects, the system automatically selects a new admin.
- **User List**: Admins can view all connected users with their usernames and IDs.

## Future Enhancements

- **Database Integration**: Add persistent storage to save chat history in a database (e.g., MySQL).
- **Encryption**: Implement SSL/TLS for secure communication between clients and the server.
- **Additional Features**: Add file sharing, enhanced user management, and more detailed logging.

## Troubleshooting

- **Port Conflicts**: Ensure that port `1234` (or the one configured) is available on your system.
- **Firewall Issues**: If the server is hosted remotely, ensure that the required ports are open in your firewall.
