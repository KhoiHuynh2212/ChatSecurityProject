package sendfile.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Enhanced login form with password and encryption settings
 */
public class SecureLoginForm extends javax.swing.JFrame {

    private javax.swing.JTextField txtUsername;
    private javax.swing.JTextField txtHost;
    private javax.swing.JTextField txtPort;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JComboBox<String> cmbKeySize;
    private javax.swing.JButton btnLogin;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JCheckBox chkShowPassword;

    public SecureLoginForm() {
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Secure Chat Login");
        setResizable(false);
        setBackground(new Color(240, 248, 255));

        // Create components
        JLabel lblTitle = new JLabel("Secure Chat");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(new Color(25, 25, 112));

        JLabel lblUsername = new JLabel("Username:");
        txtUsername = new JTextField(20);
        txtUsername.setFont(new Font("Tahoma", Font.PLAIN, 12));

        JLabel lblPassword = new JLabel("Password:");
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(new Font("Tahoma", Font.PLAIN, 12));

        chkShowPassword = new JCheckBox("Show Password");
        chkShowPassword.setFont(new Font("Tahoma", Font.PLAIN, 10));
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
        });

        JLabel lblHost = new JLabel("Server IP:");
        txtHost = new JTextField("127.0.0.1", 20);
        txtHost.setFont(new Font("Tahoma", Font.PLAIN, 12));

        JLabel lblPort = new JLabel("Port:");
        txtPort = new JTextField("3333", 20);
        txtPort.setFont(new Font("Tahoma", Font.PLAIN, 12));

        JLabel lblKeySize = new JLabel("Encryption:");
        cmbKeySize = new JComboBox<>(new String[]{
                "56-bit (DES) - Less Secure",
                "128-bit (AES) - Recommended"
        });
        cmbKeySize.setSelectedIndex(1); // Default to AES-128
        cmbKeySize.setFont(new Font("Tahoma", Font.PLAIN, 12));

        btnLogin = new JButton("SECURE LOGIN");
        btnLogin.setBackground(new Color(255, 153, 153));
        btnLogin.setForeground(Color.BLACK);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setBorder(BorderFactory.createRaisedBevelBorder());
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblStatus = new JLabel(" ");
        lblStatus.setForeground(Color.BLUE);
        lblStatus.setFont(new Font("Arial", Font.ITALIC, 11));

        // Security info panel
        JPanel securityInfo = new JPanel();
        securityInfo.setLayout(new BoxLayout(securityInfo, BoxLayout.Y_AXIS));
        securityInfo.setBorder(BorderFactory.createTitledBorder("Security Information"));
        securityInfo.setBackground(new Color(255, 255, 240));

        JLabel info1 = new JLabel("All messages are encrypted end-to-end");
        JLabel info2 = new JLabel("Password is used to derive encryption key");
        JLabel info3 = new JLabel("Choose AES-128 for better security");

        info1.setFont(new Font("Arial", Font.PLAIN, 10));
        info2.setFont(new Font("Arial", Font.PLAIN, 10));
        info3.setFont(new Font("Arial", Font.PLAIN, 10));

        securityInfo.add(info1);
        securityInfo.add(info2);
        securityInfo.add(info3);

        // Layout using GridBagLayout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(lblTitle, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        add(lblUsername, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        add(lblPassword, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        add(txtPassword, gbc);

        // Show password checkbox
        gbc.gridx = 1; gbc.gridy = 3;
        add(chkShowPassword, gbc);

        // Host
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        add(lblHost, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        add(txtHost, gbc);

        // Port
        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.EAST;
        add(lblPort, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        add(txtPort, gbc);

        // Encryption
        gbc.gridx = 0; gbc.gridy = 6; gbc.anchor = GridBagConstraints.EAST;
        add(lblKeySize, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        add(cmbKeySize, gbc);

        // Login button
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(btnLogin, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        add(lblStatus, gbc);

        // Security info
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(securityInfo, gbc);

        // Event handlers
        btnLogin.addActionListener(this::btnLoginActionPerformed);
        txtPassword.addActionListener(this::btnLoginActionPerformed);
        txtUsername.addActionListener(this::btnLoginActionPerformed);

        pack();
    }

    private void btnLoginActionPerformed(ActionEvent evt) {
        connectToServer();
    }

    private void connectToServer() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String host = txtHost.getText().trim();
        String port = txtPort.getText().trim();

        // Validation
        if (username.isEmpty() || password.isEmpty() || host.isEmpty() || port.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (username.length() > 15) {
            JOptionPane.showMessageDialog(this, "Username must be 15 characters or less!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Determine key size
            CryptoManager.KeySize keySize = cmbKeySize.getSelectedIndex() == 0 ?
                    CryptoManager.KeySize.BITS_56 : CryptoManager.KeySize.BITS_128;

            lblStatus.setText("Initializing encryption...");
            btnLogin.setEnabled(false);

            // Create crypto manager
            CryptoManager cryptoManager = new CryptoManager(password, keySize);

            lblStatus.setText("Connecting to server...");

            // Create secure main form
            SecureMainForm mainForm = new SecureMainForm();
            mainForm.initFrame(username.replace(" ", "_"), host, Integer.parseInt(port), cryptoManager);

            if (mainForm.isConnected()) {
                mainForm.setLocationRelativeTo(null);
                mainForm.setVisible(true);
                setVisible(false);
                lblStatus.setText("Connected successfully!");
            } else {
                lblStatus.setText("Connection failed!");
                btnLogin.setEnabled(true);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid port number!", "Error", JOptionPane.ERROR_MESSAGE);
            lblStatus.setText("Invalid port!");
            btnLogin.setEnabled(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Encryption setup failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            lblStatus.setText("Setup failed!");
            btnLogin.setEnabled(true);
        }

        // Clear password for security
        txtPassword.setText("");
    }

    public static void main(String args[]) {
        try {
            // Set look and feel
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Use default look and feel
        }

        java.awt.EventQueue.invokeLater(() -> {
            new SecureLoginForm().setVisible(true);
        });
    }
}