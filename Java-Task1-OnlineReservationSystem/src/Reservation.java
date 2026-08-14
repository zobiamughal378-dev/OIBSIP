/** Plain data holder for a single reservation record. */
public class Reservation {
    public final int ticketId;
    public final String pnr;
    public final String passengerName;
    public final String trainNumber;
    public final String trainName;
    public final String classType;
    public final String journeyDate;
    public final String sourceStation;
    public final String destinationStation;
    public final String bookingTime;

    public Reservation(int ticketId, String pnr, String passengerName, String trainNumber, String trainName,
                        String classType, String journeyDate, String sourceStation,
                        String destinationStation, String bookingTime) {
        this.ticketId = ticketId;
        this.pnr = pnr;
        this.passengerName = passengerName;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.classType = classType;
        this.journeyDate = journeyDate;
        this.sourceStation = sourceStation;
        this.destinationStation = destinationStation;
        this.bookingTime = bookingTime;
    }

    /** Multi-line summary used in confirmation dialogs and the cancellation screen. */
    public String toDisplayString() {
        return "Ticket ID: " + ticketId +
                "\nPNR: " + pnr +
                "\nPassenger Name: " + passengerName +
                "\nTrain Number: " + trainNumber +
                "\nTrain Name: " + trainName +
                "\nClass: " + classType +
                "\nDate of Journey: " + journeyDate +
                "\nFrom: " + sourceStation +
                "\nTo: " + destinationStation +
                "\nBooked On: " + bookingTime;
    }
}
