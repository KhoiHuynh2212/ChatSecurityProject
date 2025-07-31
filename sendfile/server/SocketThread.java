package sendfile.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * Enhanced SocketThread that supports both encrypted and legacy messaging
 * Drop-in replacement for your existing SocketThread.java
 *
 * NEW FEATURES:
 * - Handles CMD_CHATALL_ENCRYPTED for secure messaging
 * - Enhanced file transfer with better buffering
 * - Improved error handling and logging
 * - Backward compatible with all existing functionality
 */
public class SocketThread implements Runnable {

    Socket socket;
    ServerForm main;
    DataInputStream dis;
    StringTokenizer st;
    String client, filesharing_username;

    private final int BUFFER_SIZE = 8192; // Increased from 100 for better performance

    public SocketThread(Socket socket, ServerForm main) {
        this.main = main;
        this.socket = socket;

        try {
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            main.appendMessage("[SocketThread IOException]: " + e.getMessage());
        }
    }

    /**
     * Create connection for file sharing (enhanced for secure transfers)
     */
    private void createConnection(String receiver, String sender, String filename) {
        try {
            main.appendMessage("[createConnection]: Creating file sharing connection.");
            Socket receiverSocket = main.getClientList(receiver);

            if (receiverSocket != null) {
                main.appendMessage("[createConnection]: Socket OK");
                DataOutputStream dosReceiver = new DataOutputStream(receiverSocket.getOutputStream());
                main.appendMessage("[createConnection]: DataOutputStream OK");

                // Format: CMD_FILE_XD [sender] [receiver] [filename]
                String format = "CMD_FILE_XD " + sender + " " + receiver + " " + filename;
                dosReceiver.writeUTF(format);
                main.appendMessage("[createConnection]: " + format);

            } else {
                // Client not found
                main.appendMessage("[createConnection]: Client '" + receiver + "' not found");
                DataOutputStream dosSender = new DataOutputStream(socket.getOutputStream());
                dosSender.writeUTF("CMD_SENDFILEERROR Client '" + receiver + "' not found in user list. Ensure user is online.");
            }
        } catch (IOException e) {
            main.appendMessage("[createConnection]: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                /**
                 * Receive data from client
                 */
                String data = dis.readUTF();
                st = new StringTokenizer(data);
                String CMD = st.nextToken();

                /**
                 * Process commands
                 */
                switch (CMD) {
                    case "CMD_JOIN":
                        /**
                         * CMD_JOIN [clientUsername]
                         */
                        String clientUsername = st.nextToken();
                        client = clientUsername;
                        main.setClientList(clientUsername);
                        main.setSocketList(socket);
                        main.appendMessage("[Client]: " + clientUsername + " joined chatroom!");
                        break;

                    case "CMD_CHAT":
                        /**
                         * CMD_CHAT [from] [sendTo] [message]
                         */
                        String from = st.nextToken();
                        String sendTo = st.nextToken();
                        String msg = "";
                        while (st.hasMoreTokens()) {
                            msg = msg + " " + st.nextToken();
                        }
                        Socket tsoc = main.getClientList(sendTo);
                        try {
                            DataOutputStream dos = new DataOutputStream(tsoc.getOutputStream());
                            /**
                             * CMD_MESSAGE
                             */
                            String content = from + ": " + msg;
                            dos.writeUTF("CMD_MESSAGE " + content);
                            main.appendMessage("[Message]: From " + from + " To " + sendTo + " : " + msg);
                        } catch (IOException e) {
                            main.appendMessage("[IOException]: Cannot send message to " + sendTo);
                        }
                        break;

                    case "CMD_CHATALL":
                        /**
                         * CMD_CHATALL [from] [message] - Legacy broadcast
                         */
                        String chatall_from = st.nextToken();
                        String chatall_msg = "";
                        while (st.hasMoreTokens()) {
                            chatall_msg = chatall_msg + " " + st.nextToken();
                        }
                        String chatall_content = chatall_from + " " + chatall_msg;

                        for (int x = 0; x < main.clientList.size(); x++) {
                            if (!main.clientList.elementAt(x).equals(chatall_from)) {
                                try {
                                    Socket tsoc2 = (Socket) main.socketList.elementAt(x);
                                    DataOutputStream dos2 = new DataOutputStream(tsoc2.getOutputStream());
                                    dos2.writeUTF("CMD_MESSAGE " + chatall_content);
                                } catch (IOException e) {
                                    main.appendMessage("[CMD_CHATALL]: " + e.getMessage());
                                }
                            }
                        }
                        main.appendMessage("[CMD_CHATALL]: " + chatall_content);
                        break;

                    case "CMD_CHATALL_ENCRYPTED":
                        String encrypted_from = st.nextToken();
                        String encryptedMsg = st.nextToken();
                        String mac = st.nextToken();

                        main.appendMessage("[DEBUG] Encrypted message from: " + encrypted_from);
                        main.appendMessage("[DEBUG] Broadcasting to " + main.clientList.size() + " clients");

                        // Broadcast to all clients except sender
                        for (int x = 0; x < main.clientList.size(); x++) {
                            String targetClient = (String) main.clientList.elementAt(x);
                            main.appendMessage("[DEBUG] Checking client: " + targetClient);

                            if (!targetClient.equals(encrypted_from)) {
                                try {
                                    Socket targetSocket = (Socket) main.socketList.elementAt(x);
                                    DataOutputStream dos = new DataOutputStream(targetSocket.getOutputStream());

                                    String messageToSend = "CMD_MESSAGE_ENCRYPTED " + encrypted_from + " " + encryptedMsg + " " + mac;
                                    dos.writeUTF(messageToSend);

                                    main.appendMessage("[DEBUG] Sent encrypted message to: " + targetClient);

                                } catch (IOException e) {
                                    main.appendMessage("[ERROR] Failed to send to " + targetClient + ": " + e.getMessage());
                                }
                            }
                        }
                        break;

                    case "CMD_SHARINGSOCKET":
                        main.appendMessage("CMD_SHARINGSOCKET: Client setting up file sharing socket...");
                        String file_sharing_username = st.nextToken();
                        filesharing_username = file_sharing_username;
                        main.setClientFileSharingUsername(file_sharing_username);
                        main.setClientFileSharingSocket(socket);
                        main.appendMessage("CMD_SHARINGSOCKET: Username: " + file_sharing_username);
                        main.appendMessage("CMD_SHARINGSOCKET: File Sharing is now active");
                        break;

                    case "CMD_SENDFILE":
                        main.appendMessage("CMD_SENDFILE: Client sending file...");
                        /*
                         Format: CMD_SENDFILE [Filename] [Size] [Recipient] [Consignee]
                         */
                        String file_name = st.nextToken();
                        String filesize = st.nextToken();
                        String sendto = st.nextToken();
                        String consignee = st.nextToken();
                        main.appendMessage("CMD_SENDFILE: From: " + consignee);
                        main.appendMessage("CMD_SENDFILE: To: " + sendto);
                        main.appendMessage("CMD_SENDFILE: File size: " + filesize + " bytes");

                        /**
                         * Get client socket
                         */
                        main.appendMessage("CMD_SENDFILE: Ready for connections...");
                        Socket cSock = main.getClientFileSharingSocket(sendto);

                        if (cSock != null) {
                            try {
                                main.appendMessage("CMD_SENDFILE: Connected!");
                                /**
                                 * Send file info to receiver
                                 */
                                main.appendMessage("CMD_SENDFILE: Sending file to client...");
                                DataOutputStream cDos = new DataOutputStream(cSock.getOutputStream());
                                cDos.writeUTF("CMD_SENDFILE " + file_name + " " + filesize + " " + consignee);

                                /**
                                 * Transfer file data with enhanced buffering
                                 */
                                InputStream input = socket.getInputStream();
                                OutputStream sendFile = cSock.getOutputStream();

                                byte[] buffer = new byte[BUFFER_SIZE];
                                int totalBytes = Integer.parseInt(filesize);
                                int bytesTransferred = 0;
                                int count;

                                while (bytesTransferred < totalBytes && (count = input.read(buffer)) > 0) {
                                    int bytesToWrite = Math.min(count, totalBytes - bytesTransferred);
                                    sendFile.write(buffer, 0, bytesToWrite);
                                    bytesTransferred += bytesToWrite;

                                    if (bytesTransferred >= totalBytes) {
                                        break;
                                    }
                                }

                                sendFile.flush();
                                sendFile.close();

                                /**
                                 * Clean up client lists
                                 */
                                main.removeClientFileSharing(sendto);
                                main.removeClientFileSharing(consignee);
                                main.appendMessage("CMD_SENDFILE: File sent to client successfully");

                            } catch (IOException e) {
                                main.appendMessage("[CMD_SENDFILE]: " + e.getMessage());
                            }
                        } else {
                            /*   Client not available for file sharing   */
                            main.removeClientFileSharing(consignee);
                            main.appendMessage("CMD_SENDFILE: Client '" + sendto + "' not found for file sharing!");
                            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                            dos.writeUTF("CMD_SENDFILEERROR Client '" + sendto + "' not found, File Sharing will exit.");
                        }
                        break;

                    case "CMD_SENDFILERESPONSE":
                        /*
                         Format: CMD_SENDFILERESPONSE [username] [Message]
                         */
                        String receiver = st.nextToken();
                        String rMsg = "";
                        main.appendMessage("[CMD_SENDFILERESPONSE]: username: " + receiver);
                        while (st.hasMoreTokens()) {
                            rMsg = rMsg + " " + st.nextToken();
                        }
                        try {
                            Socket rSock = main.getClientFileSharingSocket(receiver);
                            DataOutputStream rDos = new DataOutputStream(rSock.getOutputStream());
                            rDos.writeUTF("CMD_SENDFILERESPONSE " + receiver + " " + rMsg);
                        } catch (IOException e) {
                            main.appendMessage("[CMD_SENDFILERESPONSE]: " + e.getMessage());
                        }
                        break;

                    case "CMD_SEND_FILE_XD":  // Format: CMD_SEND_FILE_XD [sender] [receiver] [filename]
                        try {
                            String send_sender = st.nextToken();
                            String send_receiver = st.nextToken();
                            String send_filename = st.nextToken();
                            main.appendMessage("[CMD_SEND_FILE_XD]: From: " + send_sender);
                            this.createConnection(send_receiver, send_sender, send_filename);
                        } catch (Exception e) {
                            main.appendMessage("[CMD_SEND_FILE_XD]: " + e.getLocalizedMessage());
                        }
                        break;

                    case "CMD_SEND_FILE_ERROR":  // Format: CMD_SEND_FILE_ERROR [receiver] [Message]
                        String eReceiver = st.nextToken();
                        String eMsg = "";
                        while (st.hasMoreTokens()) {
                            eMsg = eMsg + " " + st.nextToken();
                        }
                        try {
                            Socket eSock = main.getClientFileSharingSocket(eReceiver);
                            DataOutputStream eDos = new DataOutputStream(eSock.getOutputStream());
                            eDos.writeUTF("CMD_RECEIVE_FILE_ERROR " + eMsg);
                        } catch (IOException e) {
                            main.appendMessage("[CMD_RECEIVE_FILE_ERROR]: " + e.getMessage());
                        }
                        break;

                    case "CMD_SEND_FILE_ACCEPT": // Format: CMD_SEND_FILE_ACCEPT [receiver] [Message]
                        String aReceiver = st.nextToken();
                        String aMsg = "";
                        while (st.hasMoreTokens()) {
                            aMsg = aMsg + " " + st.nextToken();
                        }
                        try {
                            Socket aSock = main.getClientFileSharingSocket(aReceiver);
                            DataOutputStream aDos = new DataOutputStream(aSock.getOutputStream());
                            aDos.writeUTF("CMD_RECEIVE_FILE_ACCEPT " + aMsg);
                        } catch (IOException e) {
                            main.appendMessage("[CMD_RECEIVE_FILE_ACCEPT]: " + e.getMessage());
                        }
                        break;

                    default:
                        main.appendMessage("[CMDException]: Unknown command " + CMD);
                        break;
                }
            }
        } catch (IOException e) {
            /*   Handle client disconnection   */
            System.out.println("Client disconnected: " + client);
            System.out.println("File Sharing: " + filesharing_username);

            if (client != null) {
                main.removeFromTheList(client);
            }
            if (filesharing_username != null) {
                main.removeClientFileSharing(filesharing_username);
            }
            main.appendMessage("[SocketThread]: Client connection closed!");
        }
    }
}