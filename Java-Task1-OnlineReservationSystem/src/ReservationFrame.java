import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

/**
 * Booking screen. Collects passenger + journey details, validates them,
 * shows a live "seats available" count for the chosen train/class/date,
 * and inserts a row into the reservations table inside a single
 * transaction (seat re-check + PNR generation + insert), then shows a
 * confirmation dialog with the auto-generated Ticket ID and PNR.
 */
public class ReservationFrame extends JFrame {

    private final JTextField nameField = new JTextField(18);
    private final JTextField trainNumberField = new JTextField(18);
    private final JTextField trainNameField = new JTextField(18);
    private final JComboBox<String> classCombo =
            new JComboBox<>(new String[]{"SL - Sleeper", "3A - AC 3 Tier", "2A - AC 2 Tier", "1A - AC First Class", "CC - Chair Car"});
    private final JTextField dateField = new JTextField(18);
    private final JComboBox<String> sourceCombo = new JComboBox<>(Stations.ALL);
    private final JComboBox<String> destinationCombo = new JComboBox<>(Stations.ALL);
    private final JLabel availabilityLabel = new JLabel(" ");
    private final JLabel statusLabel = new JLabel(" ");
    private final Dashboard dashboard;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    static {
        DATE_FORMAT.setLenient(false);
    }

    public ReservationFrame(Dashboard dashboard) {
        super("Book a Ticket");
        this.dashboard = dashboard;
        buildUi();
    }

    private void buildUi() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(480, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        // Bring the dashboard back no matter how this window closes
        // (Back button, the X close box, Alt+F4, etc.)
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
        JLabel headerTitle = new JLabel("Book a Ticket", SwingConstants.CENTER);
        headerTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(headerTitle, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        sourceCombo.setEditable(true);
        destinationCombo.setEditable(true);
        sourceCombo.setSelectedItem("");
        destinationCombo.setSelectedItem("");

        int row = 0;
        row = addRow(panel, gbc, row, "Passenger Name:", nameField);
        row = addRow(panel, gbc, row, "Train Number:", trainNumberField);
        row = addRow(panel, gbc, row, "Train Name:", trainNameField);
        row = addRow(panel, gbc, row, "Class:", classCombo);
        row = addRow(panel, gbc, row, "Date of Journey (yyyy-MM-dd):", dateField);
        row = addRow(panel, gbc, row, "Source Station:", sourceCombo);
        row = addRow(panel, gbc, row, "Destination Station:", destinationCombo);

        availabilityLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(availabilityLabel, gbc);
        row++;

        // Auto-populate train name whenever the train number field loses focus,
        // and refresh the live seat-availability count.
        trainNumberField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                String number = trainNumberField.getText().trim();
                String known = TrainData.lookup(number);
                if (known != null) {
                    trainNameField.setText(known);
                }
                refreshAvailability();
            }
        });
        classCombo.addItemListener(e -> refreshAvailability());
        dateField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                refreshAvailability();
            }
        });

        statusLabel.setForeground(Color.RED);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(statusLabel, gbc);
        row++;

        JButton bookButton = new JButton("Book Ticket");
        bookButton.addActionListener(e -> onBook());
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(bookButton, gbc);

        add(panel, BorderLayout.CENTER);
    }

    private int addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
        return row + 1;
    }

    /** Looks up how many seats are already booked for the chosen train+class+date and shows it. */
    private void refreshAvailability() {
        String trainNumber = trainNumberField.getText().trim();
        String classType = (String) classCombo.getSelectedItem();
        String dateText = dateField.getText().trim();

        if (trainNumber.isEmpty() || !trainNumber.matches("\\d+") || dateText.isEmpty()) {
            availabilityLabel.setText(" ");
            return;
        }
        try {
            DATE_FORMAT.parse(dateText);
        } catch (ParseException ex) {
            availabilityLabel.setText(" ");
            return;
        }

        try (Connection conn = Database.connect()) {
            int occupied = Database.countOccupiedSeats(conn, trainNumber, classType, dateText);
            int available = Database.TOTAL_SEATS_PER_CLASS - occupied;
            if (available <= 0) {
                availabilityLabel.setForeground(Color.RED);
                availabilityLabel.setText("Seats available: 0 of " + Database.TOTAL_SEATS_PER_CLASS + " (FULLY BOOKED)");
            } else {
                availabilityLabel.setForeground(new Color(0, 130, 0));
                availabilityLabel.setText("Seats available: " + available + " of " + Database.TOTAL_SEATS_PER_CLASS
                        + "  (" + occupied + " occupied)");
            }
        } catch (SQLException ex) {
            availabilityLabel.setText(" ");
        }
    }

    private void onBook() {
        String name = nameField.getText().trim();
        String trainNumber = trainNumberField.getText().trim();
        String trainName = trainNameField.getText().trim();
        String classType = (String) classCombo.getSelectedItem();
        String dateText = dateField.getText().trim();
        String source = ((String) sourceCombo.getSelectedItem()).trim();
        String destination = ((String) destinationCombo.getSelectedItem()).trim();

        String validationError = validate(name, trainNumber, trainName, dateText, source, destination);
        if (validationError != null) {
            statusLabel.setText(validationError);
            return;
        }
        statusLabel.setText(" ");

        String bookingTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (Connection conn = Database.connect()) {
            conn.setAutoCommit(false);
            int ticketId;
            String pnr;
            try {
                // Re-check availability inside the transaction right before inserting,
                // so two almost-simultaneous bookings can't both squeeze into the last seat.
                int occupied = Database.countOccupiedSeats(conn, trainNumber, classType, dateText);
                if (occupied >= Database.TOTAL_SEATS_PER_CLASS) {
                    conn.rollback();
                    statusLabel.setText("Sorry, no seats left for this train/class/date. Try another class or date.");
                    refreshAvailability();
                    return;
                }

                pnr = Database.generateUniquePnr(conn);
                String sql = "INSERT INTO reservations " +
                        "(pnr, passenger_name, train_number, train_name, class_type, journey_date, " +
                        "source_station, destination_station, booking_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, pnr);
                    ps.setString(2, name);
                    ps.setString(3, trainNumber);
                    ps.setString(4, trainName);
                    ps.setString(5, classType);
                    ps.setString(6, dateText);
                    ps.setString(7, source);
                    ps.setString(8, destination);
                    ps.setString(9, bookingTime);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        ticketId = keys.next() ? keys.getInt(1) : -1;
                    }
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }

            Reservation reservation = new Reservation(ticketId, pnr, name, trainNumber, trainName, classType,
                    dateText, source, destination, bookingTime);
            showConfirmation(reservation);
            clearForm();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Booking failed due to a database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Returns a human-readable error string, or null if everything is valid. */
    private String validate(String name, String trainNumber, String trainName,
                             String dateText, String source, String destination) {
        if (name.isEmpty() || trainNumber.isEmpty() || trainName.isEmpty()
                || dateText.isEmpty() || source.isEmpty() || destination.isEmpty()) {
            return "All fields are required.";
        }
        if (!trainNumber.matches("\\d+")) {
            return "Train number must be numeric.";
        }
        try {
            java.util.Date parsed = DATE_FORMAT.parse(dateText);
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            if (parsed.before(today.getTime())) {
                return "Date of journey cannot be in the past.";
            }
        } catch (ParseException ex) {
            return "Date must be in yyyy-MM-dd format, e.g. 2026-08-15.";
        }
        if (source.equalsIgnoreCase(destination)) {
            return "Source and destination stations cannot be the same.";
        }
        return null;
    }

    private void showConfirmation(Reservation reservation) {
        JTextArea details = new JTextArea(reservation.toDisplayString());
        details.setEditable(false);
        details.setBackground(getBackground());
        details.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, details,
                "Booking Confirmed - Ticket #" + reservation.ticketId, JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearForm() {
        nameField.setText("");
        trainNumberField.setText("");
        trainNameField.setText("");
        classCombo.setSelectedIndex(0);
        dateField.setText("");
        sourceCombo.setSelectedItem("");
        destinationCombo.setSelectedItem("");
        availabilityLabel.setText(" ");
    }
}
