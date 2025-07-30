package sendfile.client;

import java.io.*;
import java.net.Socket;
import java.text.DecimalFormat;
import javax.swing.JOptionPane;

/**
 * Thread for sending encrypted files
 */
public class SecureSendingFileThread implements Runnable {

    protected Socket socket;
    private DataOutputStream dos;
    protected SecureSendFile form;
    protected String file;
    protected String receiver;
    protected String sender;
    protected CryptoManager cryptoManager;
    protected DecimalFormat df = new DecimalFormat("##,#00");
    private final int BUFFER_SIZE = 8192; // Larger buffer for better performance

    public SecureSendingFileThread(Socket socket, String file, String receiver, String sender,
                                   SecureSendFile form, CryptoManager cryptoManager) {
        this.socket = socket;
        this.file = file;
        this.receiver = receiver;
        this.sender = sender;
        this.form = form;
        this.cryptoManager = cryptoManager;
    }

    @Override
    public void run() {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream output = null;

        try {
            form.disableGUI(true);
            form.setMyTitle(" Encrypting and sending file...");
            System.out.println("Starting secure file transfer...");

            dos = new DataOutputStream(socket.getOutputStream());

            // Get file information
            File fileObj = new File(file);
            if (!fileObj.exists()) {
                throw new IOException("File does not exist: " + file);
            }

            // Read entire file into memory for encryption
            form.updateProgress(10);
            byte[] fileData = readFileToBytes(fileObj);
            form.updateProgress(25);

            // Encrypt the file data
            form.setMyTitle(" Encrypting file content...");
            byte[] encryptedData = cryptoManager.encryptFile(fileData);
            form.updateProgress(50);

            // Calculate encrypted file size
            long encryptedSize = encryptedData.length;
            String cleanFilename = fileObj.getName().replace(" ", "_");

            // Send file header with encrypted size
            String fileHeader = String.format("CMD_SENDFILE %s %d %s %s",
                    cleanFilename, encryptedSize, receiver, sender);
            dos.writeUTF(fileHeader);

            System.out.println("Sending encrypted file:");
            System.out.println("From: " + sender);
            System.out.println("To: " + receiver);
            System.out.println("Original size: " + fileData.length + " bytes");
            System.out.println("Encrypted size: " + encryptedSize + " bytes");

            // Send encrypted file data
            form.setMyTitle(" Sending encrypted file...");
            output = socket.getOutputStream();

            int totalSent = 0;
            int bufferSize = Math.min(BUFFER_SIZE, encryptedData.length);

            for (int i = 0; i < encryptedData.length; i += bufferSize) {
                int chunkSize = Math.min(bufferSize, encryptedData.length - i);
                output.write(encryptedData, i, chunkSize);

                totalSent += chunkSize;
                int progress = (int) ((totalSent * 100.0) / encryptedData.length);
                form.updateProgress(Math.min(50 + (progress / 2), 100));

                // Update title with progress
                form.setMyTitle(String.format("Sending encrypted file... %d%%", progress));

                // Small delay to prevent overwhelming the network
                Thread.sleep(10);
            }

            output.flush();
            form.updateProgress(100);

            // Success
            form.setMyTitle("Encrypted file sent successfully!");
            form.updateAttachment(false);

            JOptionPane.showMessageDialog(form,
                    "File encrypted and sent successfully!\n\n" +
                            "Original file: " + fileObj.getName() + "\n" +
                            "Original size: " + formatFileSize(fileData.length) + "\n" +
                            "Encrypted size: " + formatFileSize(encryptedData.length) + "\n" +
                            "Encryption: " + cryptoManager.getKeyInfo(),
                    "Secure Transfer Complete",
                    JOptionPane.INFORMATION_MESSAGE);

            form.closeThis();

            System.out.println("Encrypted file transfer completed successfully!");

        } catch (Exception e) {
            form.updateAttachment(false);
            form.disableGUI(false);
            form.setMyTitle("Encrypted file transfer failed!");

            String errorMsg = "Secure file transfer failed: " + e.getMessage();
            JOptionPane.showMessageDialog(form, errorMsg, "Transfer Error", JOptionPane.ERROR_MESSAGE);

            System.err.println("Secure file transfer error: " + e.getMessage());
            e.printStackTrace();

        } finally {
            // Clean up resources
            try {
                if (bis != null) bis.close();
                if (fis != null) fis.close();
                if (output != null) output.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    /**
     * Read entire file into byte array
     */
    private byte[] readFileToBytes(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        while ((bytesRead = bis.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }

        bis.close();
        fis.close();

        return baos.toByteArray();
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