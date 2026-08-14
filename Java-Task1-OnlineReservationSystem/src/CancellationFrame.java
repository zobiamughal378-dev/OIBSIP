import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * Cancellation screen. User enters a PNR, hits Fetch to look it up and see
 * the full booking details, then Confirm Cancellation (behind an "Are you
 * sure?" dialog) actually deletes the row.
 */
public class CancellationFrame extends JFrame {

    private final JTextField pnrField = new JTextField(16);
    private final JTextArea detailsArea = new JTextArea(10, 30);
    private final JButton cancelButton = new JButton("Confirm Cancellation");
    private final JLabel statusLabel = new JLabel(" ");

    private Reservation currentReservation; // the fetched booking, if any
    private final Dashboard dashboard;

    public CancellationFrame(Dashboard dashboard) {
        super("Cancel a Ticket");
        this.dashboard = dashboard;
        buildUi();
    }

    private void buildUi() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(460, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        // Bring the dashboard back no matter how this window closes
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                dashboard.showAgain();
            }
        });

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));
        JButton backButton = new JButton("\u2190 Back to Dashboard");
        backButton.addActionListener(e -> dispose());
        JLabel headerTitle = new JLabel("Cancel a Ticket", SwingConstants.CENTER);
        headerTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(headerTitle, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 24, 20, 24));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("PNR Number:"));
        topPanel.add(pnrField);
        JButton fetchButton = new JButton("Fetch");
        fetchButton.addActionListener(e -> onFetch());
        topPanel.add(fetchButton);

        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        detailsArea.setBorder(BorderFactory.createTitledBorder("Booking Details"));

        statusLabel.setForeground(Color.RED);

        cancelButton.setEnabled(false);
        cancelButton.addActionListener(e -> onCancel());

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(cancelButton, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel, BorderLayout.CENTER);
    }

    private void onFetch() {
        String pnr = pnrField.getText().trim();
        currentReservation = null;
        cancelButton.setEnabled(false);

        if (pnr.isEmpty()) {
            statusLabel.setText("Please enter a PNR number.");
            detailsArea.setText("");
            return;
        }
        if (!pnr.matches("\\d+")) {
            statusLabel.setText("PNR must be numeric.");
            detailsArea.setText("");
            return;
        }

        String sql = "SELECT * FROM reservations WHERE pnr = ?";
        try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    currentReservation = new Reservation(
                            rs.getInt("id"),
                            rs.getString("pnr"),
                            rs.getString("passenger_name"),
                            rs.getString("train_number"),
                            rs.getString("train_name"),
                            rs.getString("class_type"),
                            rs.getString("journey_date"),
                            rs.getString("source_station"),
                            rs.getString("destination_station"),
                            rs.getString("booking_time"));
                    detailsArea.setText(currentReservation.toDisplayString());
                    statusLabel.setText(" ");
                    cancelButton.setEnabled(true);
                } else {
                    detailsArea.setText("");
                    statusLabel.setText("No booking found for PNR " + pnr + ".");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Lookup failed due to a database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCancel() {
        if (currentReservation == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel booking PNR " + currentReservation.pnr + "?\n" +
                        "This action cannot be undone.",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM reservations WHERE pnr = ?";
        try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currentReservation.pnr);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this,
                        "Booking with PNR " + currentReservation.pnr + " has been cancelled.",
                        "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                detailsArea.setText("");
                pnrField.setText("");
                currentReservation = null;
                cancelButton.setEnabled(false);
            } else {
                statusLabel.setText("Booking could not be found (already cancelled?).");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Cancellation failed due to a database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
