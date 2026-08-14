import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * Read-only table of every reservation in the database: Ticket ID, PNR,
 * passenger, train, class, date, source/destination and when it was booked.
 * Reachable from the Dashboard's "View All Bookings" button.
 */
public class ViewBookingsFrame extends JFrame {

    private final Dashboard dashboard;
    private final DefaultTableModel tableModel;
    private final JLabel countLabel = new JLabel(" ");

    private static final String[] COLUMNS = {
            "Ticket ID", "PNR", "Passenger", "Train No.", "Train Name",
            "Class", "Date", "From", "To", "Booked On"
    };

    public ViewBookingsFrame(Dashboard dashboard) {
        super("All Bookings");
        this.dashboard = dashboard;
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only table
            }
        };
        buildUi();
        loadData();
    }

    private void buildUi() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(950, 520);
        setLocationRelativeTo(null);

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
        JLabel headerTitle = new JLabel("All Bookings", SwingConstants.CENTER);
        headerTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(headerTitle, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        countLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadData());
        bottomPanel.add(countLabel, BorderLayout.WEST);
        bottomPanel.add(refreshButton, BorderLayout.EAST);
        centerPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM reservations ORDER BY id DESC";
        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int count = 0;
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("pnr"),
                        rs.getString("passenger_name"),
                        rs.getString("train_number"),
                        rs.getString("train_name"),
                        rs.getString("class_type"),
                        rs.getString("journey_date"),
                        rs.getString("source_station"),
                        rs.getString("destination_station"),
                        rs.getString("booking_time")
                });
                count++;
            }
            countLabel.setText(count == 0 ? "No bookings yet." : count + " booking(s) total.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load bookings: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
