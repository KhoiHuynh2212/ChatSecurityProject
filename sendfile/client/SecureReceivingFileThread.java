package sendfile.client;

import java.io.*;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;

/**
 * Thread for receiving and decrypting files
 */
public class SecureReceivingFileThread implements Runnable {

    protected Socket socket;
    protected DataInputStream dis;
    protected DataOutputStream dos;
    protected SecureMainForm main;
    protected CryptoManager cryptoManager;
    protected StringTokenizer st;
    protected DecimalFormat df = new DecimalFormat("##,#00");
    private final int BUFFER_SIZE = 8192;

    public SecureReceivingFileThread(Socket socket, SecureMainForm main, CryptoManager cryptoManager) {
        this.socket = socket;
        this.main = main;
        this.cryptoManager = cryptoManager;

        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("[SecureReceivingFileThread]: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String data = dis.readUTF();
                st = new StringTokenizer(data);
                String CMD = st.nextToken();

                switch (CMD) {
                    case "CMD_SENDFILE":
                        handleEncryptedFileReceive();
                        break;

                    default:
                        System.err.println("Unknown command in secure file thread: " + CMD);
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("[SecureReceivingFileThread]: " + e.getMessage());
        }
    }

    /**
     * Handle receiving and decrypting file
     */
    private void handleEncryptedFileReceive() {
        String consignee = null;
        FileOutputStream fos = null;
        InputStream input = null;

        try {
            String filename = st.nextToken();
            long encryptedFileSize = Long.parseLong(st.nextToken());
            consignee = st.nextToken();

            main.setTitle("Receiving encrypted file...");
            System.out.println("Receiving encrypted file...");
            System.out.println("From: " + consignee);
            System.out.println("Encrypted size: " + encryptedFileSize + " bytes");

            String downloadPath = main.getMyDownloadFolder() + filename;

            // Receive encrypted file data
            input = socket.getInputStream();
            ByteArrayOutputStream encryptedDataStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[BUFFER_SIZE];
            long totalReceived = 0;
            int bytesRead;

            main.setTitle("Downloading encrypted file...");

            while (totalReceived < encryptedFileSize && (bytesRead = input.read(buffer)) != -1) {
                int bytesToWrite = (int) Math.min(bytesRead, encryptedFileSize - totalReceived);
                encryptedDataStream.write(buffer, 0, bytesToWrite);
                totalReceived += bytesToWrite;

                // Update progress
                int progress = (int) ((totalReceived * 100.0) / encryptedFileSize);
                main.setTitle(String.format("Downloading encrypted file... %d%%", progress));

                if (totalReceived >= encryptedFileSize) {
                    break;
                }
            }

            // Get encrypted data
            byte[] encryptedData = encryptedDataStream.toByteArray();
            encryptedDataStream.close();

            // Decrypt the file
            main.setTitle("Decrypting file...");
            System.out.println("Decrypting file with " + cryptoManager.getKeyInfo());

            byte[] decryptedData = cryptoManager.decryptFile(encryptedData);

            // Save decrypted file
            main.setTitle(" Saving decrypted file...");
            fos = new FileOutputStream(downloadPath);
            fos.write(decryptedData);
            fos.flush();
            fos.close();

            // Success
            main.setTitle("Secure file received!");

            String successMsg = String.format(
                    "🔐 Encrypted file received and decrypted successfully!\n\n" +
                            "File: %s\n" +
                            "From: %s\n" +
                            "Encrypted size: %s\n" +
                            "Decrypted size: %s\n" +
                            "Saved to: %s\n" +
                            "Decryption: %s",
                    filename,
                    consignee,
                    formatFileSize(encryptedData.length),
                    formatFileSize(decryptedData.length),
                    downloadPath,
                    cryptoManager.getKeyInfo()
            );

            JOptionPane.showMessageDialog(main, successMsg,
                    "Secure File Received", JOptionPane.INFORMATION_MESSAGE);

            System.out.println("Encrypted file received and decrypted successfully: " + downloadPath);

            // Reset title
            main.setTitle(" Secure Chat - " + main.getMyUsername() + " (" + cryptoManager.getKeyInfo() + ")");

        } catch (NumberFormatException e) {
            handleFileError(consignee, "Invalid file size format");
        } catch (Exception e) {
            handleFileError(consignee, "File decryption failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up resources
            try {
                if (fos != null) fos.close();
                if (input != null) input.close();
            } catch (IOException e) {
                System.err.println("Error closing file resources: " + e.getMessage());
            }
        }
    }

    /**
     * Handle file transfer errors
     */
    private void handleFileError(String consignee, String errorMessage) {
        try {
            // Send error response to sender
            if (consignee != null) {
                DataOutputStream errorDos = new DataOutputStream(socket.getOutputStream());
                errorDos.writeUTF("CMD_SENDFILERESPONSE " + consignee + " " + errorMessage);
            }

            System.err.println("Secure file receive error: " + errorMessage);

            main.setTitle("Secure file transfer failed!");

            JOptionPane.showMessageDialog(main,
                    "Secure file transfer failed:\n" + errorMessage,
                    "Transfer Error", JOptionPane.ERROR_MESSAGE);

            // Reset title
            main.setTitle("🔐 Secure Chat - " + main.getMyUsername() + " (" + cryptoManager.getKeyInfo() + ")");

            socket.close();

        } catch (IOException e) {
            System.err.println("Error sending file error response: " + e.getMessage());
        }
    }

    /**
     * Format file size for display
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}