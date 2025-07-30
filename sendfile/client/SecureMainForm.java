package sendfile.client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.Border;
/**
 * Secure main form with integrated encryption display
 */
public class SecureMainForm extends javax.swing.JFrame {

    private String username;
    private String host;
    private int port;
    private Socket socket;
    private DataOutputStream dos;
    private CryptoManager cryptoManager;
    private boolean isConnected = false;
    private String mydownloadfolder = "D:\\";
    private boolean attachmentOpen = false;

    // GUI Components
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton jButton1;
    private javax.swing.JTextPane txtpane2;
    private javax.swing.JLabel lblEncryptionInfo;
    private javax.swing.JCheckBox chkShowCiphertext;
    private javax.swing.JButton btnKeyInfo;
    private javax.swing.JProgressBar encryptionProgress;

    public SecureMainForm() {
        initComponents();
        setLocationRelativeTo(null);
    }

    public void initFrame(String username, String host, int port, CryptoManager cryptoManager) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.cryptoManager = cryptoManager;
        setTitle("Secure Chat - " + username + " (" + cryptoManager.getKeyInfo() + ")");
        connect();
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Secure Chat");

        // Create main components
        jTextPane1 = new JTextPane();
        jTextField1 = new JTextField();
        jButton1 = new JButton("Send ");
        txtpane2 = new JTextPane();
        lblEncryptionInfo = new JLabel();
        chkShowCiphertext = new JCheckBox("Show Ciphertext");
        btnKeyInfo = new JButton("🔑 Key Info");
        encryptionProgress = new JProgressBar();

        // Configure main chat area
        jTextPane1.setEditable(false);
        jTextPane1.setFont(new Font("Consolas", Font.PLAIN, 12));
        jTextPane1.setBackground(new Color(248, 248, 255));

        // Configure input field
        jTextField1.setFont(new Font("Tahoma", Font.PLAIN, 12));
        jTextField1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Configure send button
        jButton1.setBackground(new Color(255, 153, 153));
        jButton1.setForeground(Color.BLACK);
        jButton1.setFont(new Font("Arial", Font.BOLD, 12));
        jButton1.setEnabled(false);
        jButton1.setBorder(BorderFactory.createRaisedBevelBorder());
        jButton1.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Configure encryption info
        lblEncryptionInfo.setFont(new Font("Arial", Font.BOLD, 11));
        lblEncryptionInfo.setForeground(new Color(0, 100, 0));
        lblEncryptionInfo.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        // Configure show ciphertext checkbox
        chkShowCiphertext.setFont(new Font("Arial", Font.PLAIN, 10));
        chkShowCiphertext.setSelected(false);

        // Configure key info button
        btnKeyInfo.setFont(new Font("Arial", Font.PLAIN, 10));
        btnKeyInfo.setBackground(new Color(173, 216, 230));
        btnKeyInfo.setBorder(BorderFactory.createRaisedBevelBorder());
        btnKeyInfo.setEnabled(false);

        // Configure online users panel
        txtpane2.setEditable(false);
        txtpane2.setFont(new Font("Tahoma", Font.BOLD, 9));
        txtpane2.setForeground(new Color(120, 14, 3));
        txtpane2.setBackground(new Color(255, 255, 240));

        // Configure progress bar
        encryptionProgress.setVisible(false);
        encryptionProgress.setStringPainted(true);
        encryptionProgress.setString("Encrypting...");

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();

        JMenu accountMenu = new JMenu("Account");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        accountMenu.add(logoutItem);

        JMenu securityMenu = new JMenu("🛡Security");
        JMenuItem refreshKeyItem = new JMenuItem("Refresh Session");
        refreshKeyItem.addActionListener(e -> refreshEncryption());
        JMenuItem showStatsItem = new JMenuItem("Encryption Stats");
        showStatsItem.addActionListener(e -> showEncryptionStats());
        securityMenu.add(refreshKeyItem);
        securityMenu.add(showStatsItem);

        JMenu fileMenu = new JMenu("File Sharing");
        JMenuItem sendFileItem = new JMenuItem("Send Encrypted File");
        sendFileItem.addActionListener(e -> sendEncryptedFile());
        fileMenu.add(sendFileItem);

        menuBar.add(accountMenu);
        menuBar.add(securityMenu);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Layout
        setLayout(new BorderLayout());

        // Main chat area
        JPanel chatPanel = new JPanel(new BorderLayout());
        JScrollPane chatScroll = new JScrollPane(jTextPane1);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        chatPanel.add(chatScroll, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(jTextField1, BorderLayout.CENTER);
        inputPanel.add(jButton1, BorderLayout.EAST);
        inputPanel.add(encryptionProgress, BorderLayout.SOUTH);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(lblEncryptionInfo);
        infoPanel.add(Box.createHorizontalStrut(10));
        infoPanel.add(chkShowCiphertext);
        infoPanel.add(Box.createHorizontalStrut(5));
        infoPanel.add(btnKeyInfo);
        infoPanel.setBackground(new Color(240, 248, 255));
        infoPanel.setBorder(BorderFactory.createLoweredBevelBorder());

        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        chatPanel.add(infoPanel, BorderLayout.NORTH);

        // Online users panel
        JPanel onlinePanel = new JPanel(new BorderLayout());
        JLabel onlineLabel = new JLabel("Online Users (Encrypted)");
        onlineLabel.setFont(new Font("Arial", Font.BOLD, 11));
        onlineLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        onlinePanel.add(onlineLabel, BorderLayout.NORTH);

        JScrollPane onlineScroll = new JScrollPane(txtpane2);
        onlineScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        onlinePanel.add(onlineScroll, BorderLayout.CENTER);
        onlinePanel.setPreferredSize(new Dimension(220, 400));
        onlinePanel.setBorder(BorderFactory.createTitledBorder("Secure Users"));

        add(chatPanel, BorderLayout.CENTER);
        add(onlinePanel, BorderLayout.EAST);

        // Event handlers
        jButton1.addActionListener(this::sendMessage);
        jTextField1.addActionListener(this::sendMessage);
        btnKeyInfo.addActionListener(this::showKeyInfo);

        setSize(900, 650);
    }

    private void connect() {
        appendMessage("Connecting with " + cryptoManager.getKeyInfo() + "...", "System", Color.BLUE, Color.BLUE);

        try {
            socket = new Socket(host, port);
            dos = new DataOutputStream(socket.getOutputStream());

            // Send join command
            dos.writeUTF("CMD_JOIN " + username);
            appendMessage("Connected securely!", "System", Color.GREEN, Color.GREEN);
            appendMessage(" All messages are now encrypted end-to-end", "System", Color.GREEN, Color.GREEN);

            // Update UI
            lblEncryptionInfo.setText(" " + cryptoManager.getKeyInfo() + " | Session Active");
            jButton1.setEnabled(true);
            btnKeyInfo.setEnabled(true);
            isConnected = true;

            // Start client thread
            new Thread(new SecureClientThread(socket, this, cryptoManager)).start();

        } catch (IOException e) {
            isConnected = false;
            JOptionPane.showMessageDialog(this, "Cannot connect to server: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            appendMessage(" [Error]: " + e.getMessage(), "System", Color.RED, Color.RED);
        }
    }

    private void sendMessage(java.awt.event.ActionEvent evt) {
        String message = jTextField1.getText().trim();
        if (message.isEmpty()) return;

        new Thread(() -> {
            try {
                // Show encryption progress
                SwingUtilities.invokeLater(() -> {
                    encryptionProgress.setVisible(true);
                    encryptionProgress.setIndeterminate(true);
                    jButton1.setEnabled(false);
                });

                // Encrypt the message
                String encryptedMessage = cryptoManager.encrypt(message);
                String mac = cryptoManager.generateMAC(message);

                // Send encrypted message with MAC
                String content = username + " " + encryptedMessage + " " + mac;
                dos.writeUTF("CMD_CHATALL_ENCRYPTED " + content);

                // Display in chat
                SwingUtilities.invokeLater(() -> {
                    if (chkShowCiphertext.isSelected()) {
                        appendMyMessage(" " + message + "\n " + encryptedMessage, username);
                    } else {
                        appendMyMessage(" " + message, username);
                    }

                    jTextField1.setText("");
                    encryptionProgress.setVisible(false);
                    jButton1.setEnabled(true);
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    appendMessage("Encryption failed: " + e.getMessage(), "Error", Color.RED, Color.RED);
                    encryptionProgress.setVisible(false);
                    jButton1.setEnabled(true);
                });
            }
        }).start();
    }

    private void showKeyInfo(java.awt.event.ActionEvent evt) {
        String info = String.format(
                "Encryption Information\n\n" +
                        "Algorithm: %s\n" +
                        "Key Size: %d bits\n" +
                        "Mode: CBC with PKCS5 Padding\n" +
                        "MAC: SHA-256 based\n" +
                        "Session: Active\n\n" +
                        "Security Level: %s",
                cryptoManager.getAlgorithm(),
                cryptoManager.getKeyBits(),
                cryptoManager.getKeyBits() == 128 ? "High (Recommended)" : "Medium (Legacy)"
        );

        JOptionPane.showMessageDialog(this, info, "Encryption Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshEncryption() {
        try {
            cryptoManager.refreshIV();
            appendMessage("Encryption session refreshed", "Security", Color.BLUE, Color.BLUE);
        } catch (Exception e) {
            appendMessage("Failed to refresh encryption: " + e.getMessage(), "Error", Color.RED, Color.RED);
        }
    }

    private void showEncryptionStats() {
        String stats = String.format(
                "Session Statistics\n\n" +
                        "User: %s\n" +
                        "Encryption: %s\n" +
                        "Connection: %s\n" +
                        "Security: End-to-End Encrypted\n" +
                        "Message Integrity: MAC Verified",
                username,
                cryptoManager.getKeyInfo(),
                isConnected ? "Secure" : "Disconnected"
        );

        JOptionPane.showMessageDialog(this, stats, "Security Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    private void sendEncryptedFile() {
        if (!attachmentOpen) {
            SecureSendFile s = new SecureSendFile();
            if (s.prepare(username, host, port, this, cryptoManager)) {
                s.setLocationRelativeTo(null);
                s.setVisible(true);
                attachmentOpen = true;
            } else {
                JOptionPane.showMessageDialog(this, "Cannot setup encrypted file sharing!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout from secure session?",
                "Secure Logout", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (socket != null) {
                    socket.close();
                }
                setVisible(false);
                new SecureLoginForm().setVisible(true);
            } catch (IOException e) {
                System.out.println("Logout error: " + e.getMessage());
            }
        }
    }

    public void appendMessage(String msg, String header, Color headerColor, Color contentColor) {

        try {
            jTextPane1.setEditable(true);
            getMsgHeader(header, headerColor);
            getMsgContent(msg, contentColor);
            jTextPane1.setEditable(false);

            // Auto-scroll to bottom
            jTextPane1.setCaretPosition(jTextPane1.getDocument().getLength());

            // Force repaint
            jTextPane1.repaint();
            jTextPane1.revalidate();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void appendMyMessage(String msg, String header) {
        jTextPane1.setEditable(true);
        getMsgHeader(header, new Color(0, 128, 0));
        getMsgContent(msg, Color.BLACK);
        jTextPane1.setEditable(false);

        // Auto-scroll to bottom
        jTextPane1.setCaretPosition(jTextPane1.getDocument().getLength());
    }

    private void getMsgHeader(String header, Color color) {
        int len = jTextPane1.getDocument().getLength();
        jTextPane1.setCaretPosition(len);

        // Use softer colors for headers
        Color softColor = getSoftHeaderColor(color);
        jTextPane1.setCharacterAttributes(MessageStyle.styleMessageContent(softColor, "Arial", 13), false);
        jTextPane1.replaceSelection("[" + getCurrentTime() + "] " + header + ":");
    }

    private void getMsgContent(String msg, Color color) {
        int len = jTextPane1.getDocument().getLength();
        jTextPane1.setCaretPosition(len);

        // Use softer colors for content
        Color softColor = getSoftContentColor(color);
        jTextPane1.setCharacterAttributes(MessageStyle.styleMessageContent(softColor, "Arial", 12), false);
        jTextPane1.replaceSelection(" " + msg + "\n\n");
    }

    private Color getSoftHeaderColor(Color originalColor) {
        if (originalColor == Color.GREEN) {
            return MessageStyle.ENCRYPTION_GREEN;
        } else if (originalColor == Color.BLUE) {
            return MessageStyle.DARK_BLUE;
        } else if (originalColor == Color.RED) {
            return MessageStyle.SOFT_RED;
        } else if (originalColor == Color.MAGENTA) {
            return MessageStyle.SOFT_PURPLE;
        } else {
            return MessageStyle.DARK_BLUE; // Default safe color
        }
    }

    /**
     * Convert content colors to eye-friendly alternatives
     */
    private Color getSoftContentColor(Color originalColor) {
        if (originalColor == Color.BLACK) {
            return new Color(50, 50, 50); // Dark gray instead of pure black
        } else if (originalColor == Color.BLUE) {
            return MessageStyle.SOFT_BLUE;
        } else if (originalColor == Color.RED) {
            return MessageStyle.SOFT_RED;
        } else if (originalColor == Color.ORANGE) {
            return MessageStyle.SOFT_ORANGE;
        } else if (originalColor == Color.MAGENTA) {
            return MessageStyle.SOFT_PURPLE;
        } else {
            return new Color(70, 70, 70); // Default readable gray
        }
    }

    private String getCurrentTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
        return sdf.format(new java.util.Date());
    }

    public void appendOnlineList(Vector list) {
        txtpane2.setEditable(true);
        txtpane2.removeAll();
        txtpane2.setText("");

        Iterator i = list.iterator();
        while (i.hasNext()) {
            Object e = i.next();
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panel.setBackground(Color.white);

            // Try to load icon, use text if not available
            Icon icon = null;
            try {
                icon = new ImageIcon(this.getClass().getResource("/images/online.png"));
            } catch (Exception ex) {
                // Use text indicator if icon not found
            }

            JLabel label = new JLabel(icon);
            label.setText(" " + e + " ");
            label.setFont(new Font("Arial", Font.PLAIN, 10));
            panel.add(label);

            int len = txtpane2.getDocument().getLength();
            txtpane2.setCaretPosition(len);
            txtpane2.insertComponent(panel);

            len = txtpane2.getDocument().getLength();
            txtpane2.setCaretPosition(len);
            txtpane2.replaceSelection("\n");
        }
        txtpane2.setEditable(false);
    }

    // Getters
    public boolean isConnected() {
        return this.isConnected;
    }

    public String getMyUsername() {
        return this.username;
    }

    public String getMyHost() {
        return this.host;
    }

    public int getMyPort() {
        return this.port;
    }

    public CryptoManager getCryptoManager() {
        return this.cryptoManager;
    }

    public boolean shouldShowCiphertext() {
        return chkShowCiphertext.isSelected();
    }

    public String getMyDownloadFolder() {
        return this.mydownloadfolder;
    }

    public void updateAttachment(boolean b) {
        this.attachmentOpen = b;
    }

    public void openFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int open = chooser.showDialog(this, "Select Download Folder");
        if (open == chooser.APPROVE_OPTION) {
            mydownloadfolder = chooser.getSelectedFile().toString() + "\\";
        } else {
            mydownloadfolder = "D:\\";
        }
    }
}