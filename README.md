          Java Real-Time GUI Chat Application

            Project Overview
This is a **Graphical User Interface (GUI)** based Chat Application developed in Java. It enables real-time messaging between multiple users through a centralized server. Unlike basic command-line chat apps, this project features a modern **Swing Interface** for both the server dashboard and the client chat window.

The system uses **Java Sockets** for networking, **Multithreading** for handling concurrent users, and **Java Swing** for the visual interface.

##  Key Features
### Server Dashboard
*   **Visual Control:** Start and Stop the server with simple buttons.
*   **Live Logs:** View real-time server events (connections, disconnections, errors).
*   **Active Users:** A dynamic list showing who is currently online.

###   Client Application
*   **User-Friendly Interface:** Clean design with a chat history area and message input.
*   **Connection Management:** Connect and disconnect easily with a username.
*   **Real-Time Updates:** Messages appear instantly for all connected users.

##  Technologies Used
*   **Language:** Java (JDK 17+)
*   **GUI Framework:** Java Swing (`javax.swing`)
*   **Networking:** TCP/IP Sockets (`java.net`)
*   **Concurrency:** Multithreading (`Thread`, `Runnable`)
*   **Architecture:** Client-Server Model

---

## Project Structure
The project is organized into a package `com.chat` to follow Java best practices:

```text
JavaChatApp/
├── com/
│   └── chat/
│       ├── ChatServer.java      # The GUI Server Dashboard
│       ├── ChatClient.java      # The GUI Client Application
│       └── ClientHandler.java   # Handles individual client threads
├── README.md                    # Project Documentation
└── output.png 
        
## 📷 Output Screenshot
Below is the application running with the Server Dashboard and two Clients (Sura and Beka) connected simultaneously:

![alt text](output.png)
