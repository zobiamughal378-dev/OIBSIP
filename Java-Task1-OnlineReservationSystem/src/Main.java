import javax.swing.*;

/** Application entry point. */
public class Main {
    public static void main(String[] args) {
        // Make sure the SQLite JDBC driver class is loadable (helpful early error
        // message if the jar isn't on the classpath instead of a confusing NPE later).
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found on classpath.");
            System.err.println("Make sure the sqlite-jdbc jar (in lib/) is included, e.g.:");
            System.err.println("  java -cp \"bin;lib/sqlite-jdbc-3.42.0.0.jar\" Main      (Windows)");
            System.err.println("  java -cp \"bin:lib/sqlite-jdbc-3.42.0.0.jar\" Main      (macOS/Linux)");
            System.err.println("See README.md for a note about newer sqlite-jdbc versions needing slf4j-api too.");
            System.exit(1);
        } catch (NoClassDefFoundError e) {
            System.err.println("A class the SQLite driver depends on is missing: " + e.getMessage());
            System.err.println("If you're using sqlite-jdbc 3.43+ you also need slf4j-api on the classpath.");
            System.err.println("See README.md 'Troubleshooting' section for the two fixes.");
            System.exit(1);
        }

        Database.initialize();

        // Swing UI must be built/updated on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
