import javax.swing.*;
import java.awt.*;

/**
 * "Create New Account" screen, reachable from the Login screen. Any account
 * created here can log in and use the whole app (there's no separate
 * "admin" role in this schema -- the seeded admin/admin123 user is just a
 * normal row in the same users table, see Database.seedDefaultUser).
 */
public class RegisterFrame extends JFrame {

    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final JPasswordField confirmField = new JPasswordField(16);
    private final JLabel statusLabel = new JLabel(" ");
    private final LoginFrame loginFrame;

    public RegisterFrame(LoginFrame loginFrame) {
        super("Create New Account");
        this.loginFrame = loginFrame;
        buildUi();
    }

    private void buildUi() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(380, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                loginFrame.setVisible(true);
            }
        });

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Create New Account");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        panel.add(confirmField, gbc);

        statusLabel.setForeground(Color.RED);
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(statusLabel, gbc);

        JButton createButton = new JButton("Create Account");
        createButton.addActionListener(e -> onCreate());
        gbc.gridy = 5;
        panel.add(createButton, gbc);

        JButton backButton = new JButton("\u2190 Back to Login");
        backButton.addActionListener(e -> dispose());
        gbc.gridy = 6;
        panel.add(backButton, gbc);

        confirmField.addActionListener(e -> onCreate());

        add(panel);
    }

    private void onCreate() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            statusLabel.setText("All fields are required.");
            return;
        }
        if (!username.matches("[A-Za-z0-9_]{3,20}")) {
            statusLabel.setText("Username: 3-20 letters/numbers/underscore only.");
            return;
        }
        if (password.length() < 4) {
            statusLabel.setText("Password must be at least 4 characters.");
            return;
        }
        if (!password.equals(confirm)) {
            statusLabel.setText("Passwords do not match.");
            return;
        }

        try {
            if (Database.usernameExists(username)) {
                statusLabel.setText("That username is already taken.");
                return;
            }
            Database.registerUser(username, password);
            JOptionPane.showMessageDialog(this,
                    "Account \"" + username + "\" created. You can log in now.",
                    "Account Created", JOptionPane.INFORMATION_MESSAGE);
            loginFrame.prefillUsername(username);
            dispose();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not create account: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
