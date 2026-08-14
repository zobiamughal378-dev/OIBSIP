import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A small hard-coded "master list" of trains so the Reservation form can
 * auto-populate the train name when the user types a known train number.
 * In a real system this would live in its own DB table -- kept simple here
 * per the assignment scope. Unknown numbers just leave the name editable.
 */
public class TrainData {

    private static final Map<String, String> TRAINS = new LinkedHashMap<>();

    static {
        TRAINS.put("12951", "Mumbai Rajdhani Express");
        TRAINS.put("12301", "Howrah Rajdhani Express");
        TRAINS.put("12002", "Bhopal Shatabdi Express");
        TRAINS.put("12622", "Tamil Nadu Express");
        TRAINS.put("12909", "Garib Rath Express");
        TRAINS.put("12723", "Telangana Express");
        TRAINS.put("12841", "Coromandel Express");
        TRAINS.put("12649", "Karnataka Sampark Kranti");
        TRAINS.put("12429", "Rajdhani Express (Secunderabad)");
        TRAINS.put("12057", "Jan Shatabdi Express");
    }

    /** Returns the train name for a number, or null if not in the master list. */
    public static String lookup(String trainNumber) {
        return TRAINS.get(trainNumber);
    }

    public static Map<String, String> allTrains() {
        return TRAINS;
    }
}
