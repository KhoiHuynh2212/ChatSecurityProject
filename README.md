Secure Instant Point-to-Point (P2P) Messaging System
A secure instant messaging application that provides end-to-end encryption for text messages and file transfers between users. This project implements both 56-bit (DES) and 128-bit (AES) encryption standards with password-based key derivation.

Requirements
- Software Requirements
- Java JDK 8 or higher
- Intelij IDEA, NetBeans IDE
- 
Project Structure

src/
├── sendfile/
│ ├── client/
      CryptoManager.java
      SecureLoginForm.java
      SecureMainForm.java
      SecureClientThread.java
      SecureSendFile.java
      SecureSendingFileThread.java
      SecureReceivingFileThread.java
      MessageStyle.java
│ ├── server/
      MainForm.java
      ServerThread.java
      SocketThread.java
      OnlineListThread.java
      Installation & Setup

Clone this project to your local machine through this github link
https://github.com/KhoiHuynh2212/ChatSecurityProject.git

How to Run
Step 1: Start the Server (ServerForm.java)
IMPORTANT: Always start the server first before running any clients

Server Setup Process:

1. The MainForm GUI will open with the server interface
2. Default port is 3333 (you can change this if needed)
3. Click "Start the server" button
4. Server status and client connections will be displayed in the text area
5. Keep this window open - it must run continuously
6. After the server is running, you can start one or more clients

Client Login Process:

The SecureLoginForm login window will appear
    Fill in the connection details:
    Username: Enter your username (max 15 characters)
    Password: Enter a password (min 4 characters) - this will be used for encryption

3. Choose encryption level:

56-bit (DES) - Legacy support
128-bit (AES) - Recommended for better security
Click "SECURE LOGIN"

If successful, the main chat interface (SecureMainForm) will open

The login window will close automatically

Step 3: Connect Multiple Users
To test the chat functionality:

Keep the server running (ServerForm)
Start additional clients by running SecureLoginForm again
Use different usernames for each client
Use the same password for all clients to enable secure communication
All clients will appear in each other's "Online Users" list


Using the Application

Sending Messages
Type your message in the input field
Press Enter or click "Send"
Messages are automatically encrypted before transmission
Recipients will see the decrypted message

File Transfer
Go to File Sharing → Send Encrypted File
Browse and select the file to send
Enter the recipient's username
Confirm the secure transfer
File will be encrypted and transmitted


Security Options
Key Info: View current encryption details
Show Ciphertext: Toggle to see encrypted message data
Refresh Session: Generate new encryption parameters


Academic References
This project implements concepts from:

Applied Cryptography by Bruce Schneier
Network Security Essentials by William Stallings
Java Cryptography Architecture (JCA) documentation


License
This project is developed for educational purposes as part of CS 5173/4173 Computer Security coursework.

Note: This application is designed for educational purposes. For production use, additional security measures and thorough security auditing would be required.
