package sendfile.client;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;
import javax.swing.*;

/**
 * Secure file sending form with encryption
 */
public class SecureSendFile extends javax.swing.JFrame {

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String myusername;
    private String host;
    private int port;
    private StringTokenizer st;
    private String sendTo;
    private String file;
    private SecureMainForm main;
    private CryptoManager cryptoManager;

    // GUI Components
    private javax.swing.JTextField txtFile;
    private javax.swing.JButton btnBrowse;
    private javax.swing.JTextField txtSendTo;
    private javax.swing.JProgressBar progressbar;
    private javax.swing.JButton btnSendFile;
    private javax.swing.JLabel lblEncryptionStatus;
    private javax.swing.JCheckBox chkEncryptFile;

    public SecureSendFile() {
        initComponents();
        setLocationRelativeTo(null);
        progressbar.setVisible(false);
    }

    public boolean prepare(String username, String host, int port, SecureMainForm main, CryptoManager cryptoManager) {
        this.host = host;
        this.myusername = username;
        this.port = port;
        this.main = main;
        this.cryptoManager = cryptoManager;

        try {
            socket = new Socket(host, port);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());

            // Format: CMD_SHARINGSOCKET [sender]
            String format = "CMD_SHARINGSOCKET " + myusername;
            dos.writeUTF(format);
            System.out.println("Secure file sharing initialized: " + format);

            new Thread(new SecureSendFileThread(this)).start();
            return true;
        } catch (IOException e) {
            System.err.println("Secure file sharing setup error: " + e.getMessage());
        }
        return false;
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Secure File Transfer - Chat");
        setResizable(false);

        // Create components
        JLabel lblTitle = new JLabel(" Encrypted File Transfer");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setForeground(new Color(25, 25, 112));

        JLabel jLabel1 = new JLabel("Select File:");
        txtFile = new JTextField();
        txtFile.setEditable(false);
        txtFile.setBackground(Color.WHITE);
        txtFile.setFont(new Font("Tahoma", Font.PLAIN, 11));

        btnBrowse = new JButton("Browse");
        btnBrowse.setBackground(new Color(255, 153, 153));
        btnBrowse.setFont(new Font("Arial", Font.BOLD, 10));
        btnBrowse.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel jLabel2 = new JLabel("Send to:");
        txtSendTo = new JTextField();
        txtSendTo.setFont(new Font("Tahoma", Font.PLAIN, 12));

        chkEncryptFile = new JCheckBox("Encrypt file content", true);
        chkEncryptFile.setFont(new Font("Arial", Font.BOLD, 11));
        chkEncryptFile.setForeground(new Color(0, 100, 0));
        chkEncryptFile.setEnabled(false); // Always encrypt in secure mode

        lblEncryptionStatus = new JLabel("File will be encrypted with " + cryptoManager.getKeyInfo());
        lblEncryptionStatus.setFont(new Font("Arial", Font.ITALIC, 10));
        lblEncryptionStatus.setForeground(new Color(0, 128, 0));

        progressbar = new JProgressBar();
        progressbar.setStringPainted(true);
        progressbar.setString("Encrypting and sending...");

        btnSendFile = new JButton("Send Encrypted File");
        btnSendFile.setBackground(new Color(255, 153, 153));
        btnSendFile.setFont(new Font("Arial", Font.BOLD, 12));
        btnSendFile.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Layout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        add(lblTitle, gbc);

        // File selection
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        add(jLabel1, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        add(txtFile, gbc);

        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        add(btnBrowse, gbc);

        // Send to
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        add(jLabel2, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        add(txtSendTo, gbc);

        // Encryption options
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        add(chkEncryptFile, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3;
        add(lblEncryptionStatus, gbc);

        // Progress bar
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(progressbar, gbc);

        // Send button
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(btnSendFile, gbc);

        // Event handlers
        btnBrowse.addActionListener(e -> showOpenDialog());
        btnSendFile.addActionListener(e -> sendEncryptedFile());

        pack();
    }

    private void showOpenDialog() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select File to Encrypt and Send");
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            txtFile.setText(selectedFile.getAbsolutePath());

            // Show file size and estimated encryption time
            long fileSize = selectedFile.length();
            String sizeInfo = String.format("File size: %.2f KB", fileSize / 1024.0);
            lblEncryptionStatus.setText(" " + sizeInfo + " | Will be encrypted with " + cryptoManager.getKeyInfo());
        } else {
            txtFile.setText("");
        }
    }

    private void sendEncryptedFile() {
        sendTo = txtSendTo.getText().trim();
        file = txtFile.getText().trim();

        if (sendTo.isEmpty() || file.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a file and specify recipient!",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File fileObj = new File(file);
        if (!fileObj.exists()) {
            JOptionPane.showMessageDialog(this, "Selected file does not exist!",
                    "File Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirm encryption
        int confirm = JOptionPane.showConfirmDialog(this,
                "Secure File Transfer Confirmation\n\n" +
                        "File: " + fileObj.getName() + "\n" +
                        "Size: " + String.format("%.2f KB", fileObj.length() / 1024.0) + "\n" +
                        "Recipient: " + sendTo + "\n" +
                        "Encryption: " + cryptoManager.getKeyInfo() + "\n\n" +
                        "The file will be encrypted before sending.\n" +
                        "Continue with secure transfer?",
                "Confirm Secure Transfer",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // Send file transfer request
            String fname = getCleanFilename(file);
            String format = "CMD_SEND_FILE_XD " + myusername + " " + sendTo + " " + fname;
            dos.writeUTF(format);
            System.out.println("Secure file transfer request: " + format);

            updateBtn("Requesting secure transfer...");
            btnSendFile.setEnabled(false);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to initiate secure file transfer: " + e.getMessage(),
                    "Transfer Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("File transfer request error: " + e.getMessage());
        }
    }

    /**
     * Get clean filename for transfer
     */
    public String getCleanFilename(String path) {
        File file = new File(path);
        String fname = file.getName();
        return fname.replace(" ", "_");
    }

    /**
     * Disable/enable GUI components during transfer
     */
    public void disableGUI(boolean disable) {
        if (disable) {
            txtSendTo.setEditable(false);
            btnBrowse.setEnabled(false);
            btnSendFile.setEnabled(false);
            txtFile.setEditable(false);
            progressbar.setVisible(true);
        } else {
            txtSendTo.setEditable(true);
            btnSendFile.setEnabled(true);
            btnBrowse.setEnabled(true);
            txtFile.setEditable(true);
            progressbar.setVisible(false);
        }
    }

    /**
     * Update form title
     */
    public void setMyTitle(String title) {
        setTitle(title);
    }

    /**
     * Close form
     */
    protected void closeThis() {
        dispose();
    }

    /**
     * Update attachment status in main form
     */
    public void updateAttachment(boolean status) {
        main.updateAttachment(status);
    }

    /**
     * Update send button text
     */
    public void updateBtn(String text) {
        btnSendFile.setText(text);
    }

    /**
     * Update progress bar
     */
    public void updateProgress(int value) {
        progressbar.setValue(value);
    }

    /**
     * Get crypto manager
     */
    public CryptoManager getCryptoManager() {
        return cryptoManager;
    }

    /**
     * Get selected file path
     */
    public String getSelectedFile() {
        return file;
    }

    /**
     * Get recipient
     */
    public String getRecipient() {
        return sendTo;
    }

    /**
     * Get username
     */
    public String getUsername() {
        return myusername;
    }

    /**
     * Get socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Inner class for handling secure file transfer responses
     */
    class SecureSendFileThread implements Runnable {
        private SecureSendFile form;

        public SecureSendFileThread(SecureSendFile form) {
            this.form = form;
        }

        private void closeConnection() {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
            dispose();
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    String data = dis.readUTF();
                    st = new StringTokenizer(data);
                    String cmd = st.nextToken();

                    switch (cmd) {
                        case "CMD_RECEIVE_FILE_ERROR":
                            String errorMsg = "";
                            while (st.hasMoreTokens()) {
                                errorMsg = errorMsg + " " + st.nextToken();
                            }
                            form.updateAttachment(false);
                            JOptionPane.showMessageDialog(SecureSendFile.this,
                                    "Secure transfer failed: " + errorMsg,
                                    "Transfer Error", JOptionPane.ERROR_MESSAGE);
                            closeConnection();
                            break;

                        case "CMD_RECEIVE_FILE_ACCEPT":
                            String acceptMsg = "";
                            while (st.hasMoreTokens()) {
                                acceptMsg = acceptMsg + " " + st.nextToken();
                            }
                            // Start encrypted file sending
                            new Thread(new SecureSendingFileThread(socket, file, sendTo, myusername,
                                    SecureSendFile.this, cryptoManager)).start();
                            break;

                        case "CMD_SENDFILEERROR":
                            String sendErrorMsg = "";
                            while (st.hasMoreTokens()) {
                                sendErrorMsg = sendErrorMsg + " " + st.nextToken();
                            }
                            System.err.println("Send file error: " + sendErrorMsg);
                            JOptionPane.showMessageDialog(SecureSendFile.this,
                                    " " + sendErrorMsg, "Error", JOptionPane.ERROR_MESSAGE);
                            form.updateAttachment(false);
                            form.disableGUI(false);
                            form.updateBtn(" Send Encrypted File");
                            break;

                        case "CMD_SENDFILERESPONSE":
                            String responseUser = st.nextToken();
                            String responseMsg = "";
                            while (st.hasMoreTokens()) {
                                responseMsg = responseMsg + " " + st.nextToken();
                            }
                            form.updateAttachment(false);
                            JOptionPane.showMessageDialog(SecureSendFile.this,
                                    "📨 " + responseMsg, "Transfer Response", JOptionPane.INFORMATION_MESSAGE);
                            dispose();
                            break;

                        default:
                            System.err.println("Unknown secure file transfer command: " + cmd);
                            break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Secure file transfer thread error: " + e.getMessage());
            }
        }
    }
}