import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * First screen shown to the user: username + password, with an explicit
 * "access denied" message for bad credentials. On success it opens the
 * Dashboard and closes itself.
 *
 * Default seeded login (see Database.seedDefaultUser): admin / admin123
 */
public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final JLabel statusLabel = new JLabel(" ");

    public LoginFrame() {
        super("Train Reservation System - Login");
        buildUi();
    }

    private void buildUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(380, 320);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Train Reservation System");
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

        statusLabel.setForeground(Color.RED);
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(statusLabel, gbc);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(this::onLogin);
        gbc.gridy = 4;
        panel.add(loginButton, gbc);

        JButton registerButton = new JButton("Create New Account");
        registerButton.addActionListener(e -> {
            setVisible(false);
            new RegisterFrame(this).setVisible(true);
        });
        gbc.gridy = 5;
        panel.add(registerButton, gbc);

        JLabel hint = new JLabel("Default login: admin / admin123");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);
        gbc.gridy = 6;
        panel.add(hint, gbc);

        // Enter key in the password field triggers login too
        passwordField.addActionListener(this::onLogin);

        add(panel);
    }

    /** Called by RegisterFrame after a successful signup so the username is ready to go. */
    public void prefillUsername(String username) {
        usernameField.setText(username);
        passwordField.setText("");
        passwordField.requestFocus();
    }

    private void onLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password are required.");
            return;
        }

        try {
            if (Database.validateLogin(username, password)) {
                statusLabel.setText(" ");
                new Dashboard(username).setVisible(true);
                dispose();
            } else {
                statusLabel.setText("Access denied: invalid username or password.");
                passwordField.setText("");
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
