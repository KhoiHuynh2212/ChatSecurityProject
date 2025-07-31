package sendfile.client;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Enhanced client thread with integrated decryption capabilities
 * Sound effects removed for cleaner implementation
 */
public class SecureClientThread implements Runnable {

    private final Socket socket;
    private DataInputStream dis;
    private final SecureMainForm main;
    private final CryptoManager cryptoManager;
    private StringTokenizer st;

    public SecureClientThread(Socket socket, SecureMainForm main, CryptoManager cryptoManager) {
        this.socket = socket;
        this.main = main;
        this.cryptoManager = cryptoManager;

        try {
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            main.appendMessage("[IOException]: " + e.getMessage(), "Error", Color.RED, Color.RED);
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String data = dis.readUTF();
                System.out.println("Received: " + data);

                st = new StringTokenizer(data);
                String CMD = st.nextToken();

                switch (CMD) {
                    case "CMD_MESSAGE":
                        handleRegularMessage();
                        break;

                    case "CMD_MESSAGE_ENCRYPTED":
                        handleEncryptedMessage();
                        break;

                    case "CMD_ONLINE":
                        handleOnlineList();
                        break;

                    case "CMD_FILE_XD":
                        handleFileRequest();
                        break;

                    default:
                        System.out.println("Unknown command: " + CMD);
                        SwingUtilities.invokeLater(() -> {
                            main.appendMessage("[Unknown Command]: " + CMD, "System", Color.ORANGE, Color.ORANGE);
                        });
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            SwingUtilities.invokeLater(() -> {
                main.appendMessage("Connection lost to server!", "Error", Color.RED, Color.RED);
            });
        }
    }

    /**
     * Handle regular unencrypted messages (legacy support)
     */
    private void handleRegularMessage() {
        String msg = "";
        String from = st.nextToken();
        while (st.hasMoreTokens()) {
            msg = msg + " " + st.nextToken();
        }

        String finalMsg = msg;
        SwingUtilities.invokeLater(() -> {
            main.appendMessage(" " + finalMsg +" (UNENCRYPTED)", from, Color.ORANGE, Color.ORANGE);
        });
    }

    /**
     * Handle encrypted messages with MAC verification
     */
    private void handleEncryptedMessage() {
        try {
            System.out.println("Processing encrypted message");

            String from = st.nextToken();
            String encryptedMsg = st.nextToken();
            String mac = st.nextToken();

            System.out.println("From: " + from + ", Encrypted: " + encryptedMsg.substring(0, Math.min(20, encryptedMsg.length())) + "...");

            // Decrypt message
            String decryptedMsg = cryptoManager.decrypt(encryptedMsg);
            System.out.println("Decrypted: " + decryptedMsg);

            // Verify MAC for message integrity
            if (cryptoManager.verifyMAC(decryptedMsg, mac)) {
                // Use SwingUtilities.invokeLater for GUI updates
                SwingUtilities.invokeLater(() -> {
                    try {
                        System.out.println(" Updating GUI with message: " + decryptedMsg);

                        // Display the message in GUI
                        if (main.shouldShowCiphertext()) {
                            String displayMsg = "" + decryptedMsg + "\n " + encryptedMsg;
                            main.appendMessage(displayMsg, from + " ", Color.MAGENTA, Color.BLUE);
                        } else {
                            main.appendMessage(" " + decryptedMsg, from + " ", Color.MAGENTA, Color.BLUE);
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } else {
                // MAC verification failed
                SwingUtilities.invokeLater(() -> {
                    main.appendMessage("Message integrity verification FAILED! Possible tampering detected.",
                            from, Color.RED, Color.RED);
                });
            }

        } catch (Exception e) {
            e.printStackTrace();

            // Show error in GUI
            SwingUtilities.invokeLater(() -> {
                main.appendMessage("Decryption failed: " + e.getMessage(), "Error", Color.RED, Color.RED);
            });
        }
    }

    /**
     * Handle online user list
     */
    private void handleOnlineList() {
        Vector<String> online = new Vector<>();
        while (st.hasMoreTokens()) {
            String user = st.nextToken();
            if (!user.equalsIgnoreCase(main.getMyUsername())) {
                online.add(user);
            }
        }

        SwingUtilities.invokeLater(() -> {
            main.appendOnlineList(online);
        });
    }

    /**
     * Handle encrypted file transfer request
     */
    private void handleFileRequest() {
        String sender = st.nextToken();
        String receiver = st.nextToken();
        String fname = st.nextToken();

        SwingUtilities.invokeLater(() -> {
            int confirm = JOptionPane.showConfirmDialog(
                    main,
                    "Encrypted File Transfer Request\n\n" +
                            "From: " + sender + "\n" +
                            "File: " + fname + "\n\n" +
                            "This file will be encrypted during transfer.\n" +
                            "Do you want to accept this secure file transfer?",
                    "Secure File Transfer",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            try {
                if (confirm == JOptionPane.YES_OPTION) {
                    // Accept encrypted file transfer
                    main.openFolder();

                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    String format = "CMD_SEND_FILE_ACCEPT " + sender + " Secure transfer accepted";
                    dos.writeUTF(format);

                    // Create secure file receiving socket
                    Socket fSoc = new Socket(main.getMyHost(), main.getMyPort());
                    DataOutputStream fdos = new DataOutputStream(fSoc.getOutputStream());
                    fdos.writeUTF("CMD_SHARINGSOCKET " + main.getMyUsername());

                    // Start secure file receiving thread
                    new Thread(new SecureReceivingFileThread(fSoc, main, cryptoManager)).start();

                } else {
                    // Reject file transfer
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    String format = "CMD_SEND_FILE_ERROR " + sender + " User rejected the secure file transfer request.";
                    dos.writeUTF(format);
                }
            } catch (IOException e) {
                main.appendMessage("File transfer setup error: " + e.getMessage(), "Error", Color.RED, Color.RED);
                System.err.println("File transfer error: " + e.getMessage());
            }
        });
    }
}