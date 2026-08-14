import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Simple post-login menu that routes to the Reservation, Cancellation, and Bookings-list screens. */
public class Dashboard extends JFrame {

    private final String loggedInUser;
    private final String sessionStartedAt;

    public Dashboard(String loggedInUser) {
        super("Train Reservation System - Dashboard");
        this.loggedInUser = loggedInUser;
        this.sessionStartedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        buildUi();
    }

    /** Called by child screens (Reservation/Cancellation/Bookings) when the user goes back. */
    public void showAgain() {
        setVisible(true);
        toFront();
        requestFocus();
    }

    private void buildUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 360);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel welcome = new JLabel("Welcome, " + loggedInUser + "!");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 16));
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel session = new JLabel("Session started at " + sessionStartedAt);
        session.setFont(new Font("SansSerif", Font.ITALIC, 11));
        session.setForeground(Color.GRAY);
        session.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton bookButton = new JButton("Book a Ticket");
        JButton cancelButton = new JButton("Cancel a Ticket (via PNR)");
        JButton viewBookingsButton = new JButton("View All Bookings");
        JButton logoutButton = new JButton("Logout");

        for (JButton b : new JButton[]{bookButton, cancelButton, viewBookingsButton, logoutButton}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setMaximumSize(new Dimension(260, 36));
        }

        bookButton.addActionListener(e -> {
            setVisible(false);
            new ReservationFrame(this).setVisible(true);
        });
        cancelButton.addActionListener(e -> {
            setVisible(false);
            new CancellationFrame(this).setVisible(true);
        });
        viewBookingsButton.addActionListener(e -> {
            setVisible(false);
            new ViewBookingsFrame(this).setVisible(true);
        });
        logoutButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        panel.add(welcome);
        panel.add(Box.createVerticalStrut(4));
        panel.add(session);
        panel.add(Box.createVerticalStrut(24));
        panel.add(bookButton);
        panel.add(Box.createVerticalStrut(12));
        panel.add(cancelButton);
        panel.add(Box.createVerticalStrut(12));
        panel.add(viewBookingsButton);
        panel.add(Box.createVerticalStrut(24));
        panel.add(logoutButton);

        add(panel);
    }
}
