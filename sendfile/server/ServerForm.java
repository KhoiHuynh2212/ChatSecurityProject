package sendfile.server;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

/**
 * Modern and responsive MainForm with contemporary UI design
 */
public class ServerForm extends JFrame {

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    Thread t;
    ServerThread serverThread;

    /** Chat List **/
    public Vector socketList = new Vector();
    public Vector clientList = new Vector();

    /** File Sharing List **/
    public Vector clientFileSharingUsername = new Vector();
    public Vector clientFileSharingSocket = new Vector();

    /** Server **/
    ServerSocket server;

    // Modern color scheme
    private static final Color PRIMARY_COLOR = new Color(67, 56, 202);      // Indigo
    private static final Color SUCCESS_COLOR = new Color(16, 185, 129);     // Emerald
    private static final Color DANGER_COLOR = new Color(239, 68, 68);       // Red
    private static final Color BACKGROUND_COLOR = new Color(249, 250, 251); // Gray-50
    private static final Color SURFACE_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);        // Gray-900
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);   // Gray-500
    private static final Color BORDER_COLOR = new Color(229, 231, 235);     // Gray-200

    // GUI Components
    private JTextField portField;
    private JButton startButton;
    private JButton stopButton;
    private JTextArea logArea;
    private JLabel statusLabel;
    private JLabel clientCountLabel;
    private JPanel statusPanel;
    private JProgressBar serverProgress;

    public ServerForm() {
        initModernComponents();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initModernComponents() {
        setTitle("Secure Chat Server");
        setSize(900, 650);
        setMinimumSize(new Dimension(700, 500));

        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        } catch (Exception e) {
            // Use default if system L&F not available
        }

        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Create header panel
        createHeaderPanel();

        // Create main content area
        createMainContent();

        // Create footer/status panel
        createFooterPanel();

        // Apply modern styling
        applyModernStyling();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(SURFACE_COLOR);
        headerPanel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, false),
                new EmptyBorder(20, 25, 20, 25)
        ));

        // Title section
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(SURFACE_COLOR);

        JLabel titleLabel = new JLabel("Secure Chat Server");
        titleLabel.setFont(new Font("SF Pro Display", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Enterprise-grade encrypted messaging server");
        subtitleLabel.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);

        titlePanel.add(titleLabel);

        JPanel titleContainer = new JPanel(new BorderLayout());
        titleContainer.setBackground(SURFACE_COLOR);
        titleContainer.add(titlePanel, BorderLayout.NORTH);
        titleContainer.add(subtitleLabel, BorderLayout.CENTER);

        // Control section
        JPanel controlPanel = createControlPanel();

        headerPanel.add(titleContainer, BorderLayout.WEST);
        headerPanel.add(controlPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SURFACE_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();

        // Port configuration
        JLabel portLabel = new JLabel("Server Port:");
        portLabel.setFont(new Font("SF Pro Text", 1, 13));
        portLabel.setForeground(TEXT_PRIMARY);

        portField = new JTextField("3333", 8);
        styleTextField(portField);

        // Server controls
        startButton = createModernButton("Start Server", SUCCESS_COLOR);
        stopButton = createModernButton("Stop Server", DANGER_COLOR);
        stopButton.setEnabled(false);

        // Progress indicator
        serverProgress = new JProgressBar();
        serverProgress.setIndeterminate(false);
        serverProgress.setVisible(false);
        serverProgress.setPreferredSize(new Dimension(150, 6));
        styleProgressBar(serverProgress);

        // Layout components
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(portLabel, gbc);

        gbc.gridx = 1;
        panel.add(portField, gbc);

        gbc.gridx = 2;
        panel.add(startButton, gbc);

        gbc.gridx = 3;
        panel.add(stopButton, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(serverProgress, gbc);

        // Event handlers
        startButton.addActionListener(this::startServerAction);
        stopButton.addActionListener(this::stopServerAction);
        portField.addActionListener(this::startServerAction);

        return panel;
    }

    private void createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 0));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Server log area
        createLogArea(mainPanel);

        // Stats sidebar
        createStatsSidebar(mainPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void createLogArea(JPanel parent) {
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(SURFACE_COLOR);
        logPanel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));

        // Log header
        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.setBackground(new Color(248, 250, 252));
        logHeader.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel logTitle = new JLabel("Server Activity Log");
        logTitle.setFont(new Font("SF Pro Text", 1, 14));
        logTitle.setForeground(TEXT_PRIMARY);

        JButton clearLogButton = createSmallButton("Clear", TEXT_SECONDARY);
        clearLogButton.addActionListener(e -> {
            logArea.setText("");
            appendMessage("Log cleared by administrator");
        });

        logHeader.add(logTitle, BorderLayout.WEST);
        logHeader.add(clearLogButton, BorderLayout.EAST);

        // Log content area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        logArea.setBackground(SURFACE_COLOR);
        logArea.setForeground(TEXT_PRIMARY);
        logArea.setMargin(new Insets(16, 16, 16, 16));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        styleScrollPane(scrollPane);

        logPanel.add(logHeader, BorderLayout.NORTH);
        logPanel.add(scrollPane, BorderLayout.CENTER);

        parent.add(logPanel, BorderLayout.CENTER);

        // Add initial welcome message
        appendMessage("Secure Chat Server initialized");
        appendMessage("Configure port and click 'Start Server' to begin");
    }

    private void createStatsSidebar(JPanel parent) {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(BACKGROUND_COLOR);
        statsPanel.setPreferredSize(new Dimension(280, 0));

        // Server status card
        JPanel statusCard = createStatsCard("Server Status", "Offline");

        // Client count card
        JPanel clientCard = createStatsCard("Connected Clients", "0");

        // Security info card
        JPanel securityCard = createInfoCard();

        statsPanel.add(statusCard);
        statsPanel.add(Box.createVerticalStrut(15));
        statsPanel.add(clientCard);
        statsPanel.add(Box.createVerticalStrut(15));
        statsPanel.add(securityCard);
        statsPanel.add(Box.createVerticalGlue());

        parent.add(statsPanel, BorderLayout.EAST);
    }

    private JPanel createStatsCard(String title, String initialValue) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(SURFACE_COLOR);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SF Pro Text",1, 13));
        titleLabel.setForeground(TEXT_SECONDARY);

        JLabel valueLabel = new JLabel(initialValue);
        valueLabel.setFont(new Font("SF Pro Display", Font.BOLD, 20));
        valueLabel.setForeground(TEXT_PRIMARY);

        // Store references to the labels for later updates
        if (title.contains("Server Status")) {
            statusLabel = valueLabel;
        } else if (title.contains("Connected Clients")) {
            clientCountLabel = valueLabel;
        }

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(SURFACE_COLOR);
        content.add(titleLabel, BorderLayout.NORTH);
        content.add(valueLabel, BorderLayout.CENTER);

        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JPanel createInfoCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(239, 246, 255)); // Blue-50
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(147, 197, 253), 1, true), // Blue-300
                new EmptyBorder(16, 16, 16, 16)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JLabel titleLabel = new JLabel("🛡Security Features");
        titleLabel.setFont(new Font("SF Pro Text", 1, 14));
        titleLabel.setForeground(new Color(30, 64, 175)); // Blue-800

        String[] features = {
                "• End-to-end encryption",
                "• AES-128 & DES support",
                "• MAC verification",
                "• Secure file transfer",
                "• Real-time monitoring"
        };

        JPanel featuresPanel = new JPanel();
        featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
        featuresPanel.setBackground(new Color(239, 246, 255));

        for (String feature : features) {
            JLabel featureLabel = new JLabel(feature);
            featureLabel.setFont(new Font("SF Pro Text", Font.PLAIN, 11));
            featureLabel.setForeground(new Color(30, 64, 175));
            featureLabel.setBorder(new EmptyBorder(2, 0, 2, 0));
            featuresPanel.add(featureLabel);
        }

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(Box.createVerticalStrut(8), BorderLayout.CENTER);
        card.add(featuresPanel, BorderLayout.SOUTH);

        return card;
    }

    private void createFooterPanel() {
        statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(248, 250, 252));
        statusPanel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, false),
                new EmptyBorder(12, 25, 12, 25)
        ));

        JLabel footerLabel = new JLabel("Secure Chat Server v2.0 | Ready for enterprise deployment");
        footerLabel.setFont(new Font("SF Pro Text", Font.PLAIN, 12));
        footerLabel.setForeground(TEXT_SECONDARY);

        JLabel timeLabel = new JLabel(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        timeLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
        timeLabel.setForeground(TEXT_SECONDARY);

        statusPanel.add(footerLabel, BorderLayout.WEST);
        statusPanel.add(timeLabel, BorderLayout.EAST);

        add(statusPanel, BorderLayout.SOUTH);

        // Update time every second
        Timer timer = new Timer(1000, e -> {
            timeLabel.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        });
        timer.start();
    }

    private JButton createModernButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("SF Pro Text", 1, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(bgColor.darker());
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(bgColor);
                }
            }
        });

        return button;
    }

    private JButton createSmallButton(String text, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("SF Pro Text", Font.PLAIN, 12));
        button.setForeground(textColor);
        button.setBackground(Color.WHITE);
        button.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(4, 12, 4, 12)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("SF Pro Text", Font.PLAIN, 13));
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(SURFACE_COLOR);
        field.setForeground(TEXT_PRIMARY);
    }

    private void styleProgressBar(JProgressBar progressBar) {
        progressBar.setBackground(new Color(229, 231, 235));
        progressBar.setForeground(PRIMARY_COLOR);
        progressBar.setBorderPainted(false);
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBackground(new Color(248, 250, 252));
    }

    private void applyModernStyling() {
        // Additional modern styling can be added here
        setBackground(BACKGROUND_COLOR);
    }

    // Event handlers
    private void startServerAction(ActionEvent evt) {
        try {
            int port = Integer.parseInt(portField.getText().trim());

            if (port < 1024 || port > 65535) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid port number (1024-65535)",
                        "Invalid Port",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            serverProgress.setVisible(true);
            serverProgress.setIndeterminate(true);

            serverThread = new ServerThread(port, this);
            t = new Thread(serverThread);
            t.start();

            new Thread(new OnlineListThread(this)).start();

            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            portField.setEnabled(false);

            if (statusLabel != null) {
                statusLabel.setText("Online");
                statusLabel.setForeground(SUCCESS_COLOR);
            }

            serverProgress.setVisible(false);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid port number!",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopServerAction(ActionEvent evt) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to stop the server?\nAll active connections will be terminated.",
                "Confirm Server Shutdown",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == 0) {
            serverThread.stop();
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            portField.setEnabled(true);

            if (statusLabel != null) {
                statusLabel.setText("Offline");
                statusLabel.setForeground(DANGER_COLOR);
            }
            if (clientCountLabel != null) {
                clientCountLabel.setText("0");
            }
        }
    }

    // Server management methods
    public void appendMessage(String msg) {
        Date date = new Date();
        String timestamp = sdf.format(date);
        String formattedMsg = String.format("[%s] %s", timestamp, msg);

        SwingUtilities.invokeLater(() -> {
            if (logArea != null) {
                logArea.append(formattedMsg + "\n");
                logArea.setCaretPosition(logArea.getText().length());
            }

            // Update client count safely
            if (clientCountLabel != null) {
                clientCountLabel.setText(String.valueOf(clientList.size()));
            }
        });
    }

    // Existing server management methods (unchanged)
    public void setSocketList(Socket socket) {
        try {
            socketList.add(socket);
            appendMessage("New socket connection established");
        } catch (Exception e) {
            appendMessage("[setSocketList]: " + e.getMessage());
        }
    }

    public void setClientList(String client) {
        try {
            clientList.add(client);
            appendMessage("Client joined: " + client);
        } catch (Exception e) {
            appendMessage("[setClientList]: " + e.getMessage());
        }
    }

    public void setClientFileSharingUsername(String user) {
        try {
            clientFileSharingUsername.add(user);
        } catch (Exception e) { }
    }

    public void setClientFileSharingSocket(Socket soc) {
        try {
            clientFileSharingSocket.add(soc);
        } catch (Exception e) { }
    }

    public Socket getClientList(String client) {
        Socket tsoc = null;
        for(int x=0; x < clientList.size(); x++) {
            if(clientList.get(x).equals(client)) {
                tsoc = (Socket) socketList.get(x);
                break;
            }
        }
        return tsoc;
    }

    public void removeFromTheList(String client) {
        try {
            for(int x=0; x < clientList.size(); x++) {
                if(clientList.elementAt(x).equals(client)) {
                    clientList.removeElementAt(x);
                    socketList.removeElementAt(x);
                    appendMessage("👋 Client disconnected: " + client);
                    break;
                }
            }
        } catch (Exception e) {
            appendMessage("[RemovedException]: " + e.getMessage());
        }
    }

    public Socket getClientFileSharingSocket(String username) {
        Socket tsoc = null;
        for(int x=0; x < clientFileSharingUsername.size(); x++) {
            if(clientFileSharingUsername.elementAt(x).equals(username)) {
                tsoc = (Socket) clientFileSharingSocket.elementAt(x);
                break;
            }
        }
        return tsoc;
    }

    public void removeClientFileSharing(String username) {
        for(int x=0; x < clientFileSharingUsername.size(); x++) {
            if(clientFileSharingUsername.elementAt(x).equals(username)) {
                try {
                    Socket rSock = getClientFileSharingSocket(username);
                    if(rSock != null) {
                        rSock.close();
                    }
                    clientFileSharingUsername.removeElementAt(x);
                    clientFileSharingSocket.removeElementAt(x);
                    appendMessage("File sharing cancelled: " + username);
                } catch (IOException e) {
                    appendMessage("[FileSharing]: " + e.getMessage());
                    appendMessage("File sharing cancelled: " + username);
                }
                break;
            }
        }
    }

    public static void main(String args[]) {
        // Set system look and feel for better OS integration
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());

            // Enable antialiasing for text
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");

        } catch (Exception e) {
            // Use default look and feel
        }

        SwingUtilities.invokeLater(() -> {
            new ServerForm().setVisible(true);
        });
    }
}