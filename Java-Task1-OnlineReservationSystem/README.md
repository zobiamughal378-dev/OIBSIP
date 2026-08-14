# Train Reservation System (Java Swing + JDBC + SQLite)

A desktop GUI app: log in, book a ticket (auto-generated PNR), and cancel a
booking by PNR. Built with plain Java Swing and `java.sql` — no frameworks,
no Maven/Gradle required.

## Project structure

```
reservation_system/
├── src/
│   ├── Main.java              entry point
│   ├── Database.java          JDBC connection, table setup, PNR generation
│   ├── TrainData.java         train number -> train name lookup table
│   ├── Reservation.java       plain data holder for one booking
│   ├── LoginFrame.java        login screen
│   ├── Dashboard.java         post-login menu
│   ├── ReservationFrame.java  booking form
│   └── CancellationFrame.java cancellation form
├── lib/                       put your sqlite-jdbc jar here
├── bin/                       compiled .class files go here
└── README.md
```

## 1. Get the SQLite JDBC driver

You mentioned you already downloaded `sqlite-jdbc-3.44.1.0.jar` — **that jar
works, but it needs one extra jar alongside it.** From version 3.43 onward,
sqlite-jdbc calls `org.slf4j.LoggerFactory` directly, so without slf4j on
the classpath you'll hit:

```
Exception in thread "main" java.lang.NoClassDefFoundError: org/slf4j/LoggerFactory
```

You have two options:

**Option A (keep your jar):** also download `slf4j-api-2.0.x.jar` (e.g. from
Maven Central) into `lib/`, and put both jars on the classpath (see below).

**Option B (simplest, recommended for this project):** use an older
sqlite-jdbc release that has zero external dependencies — a single jar, no
slf4j needed. This is what I compiled and tested this project against:

- Download `sqlite-jdbc-3.42.0.0.jar` from
  `https://github.com/xerial/sqlite-jdbc/releases/download/3.42.0.0/sqlite-jdbc-3.42.0.0.jar`
- Put it in `lib/`.

Either way, place the jar(s) in the `lib/` folder.

## 2. Compile

From the `reservation_system/` folder:

```
# Windows
javac -cp "lib\sqlite-jdbc-3.42.0.0.jar" -d bin src\*.java

# macOS/Linux
javac -cp "lib/sqlite-jdbc-3.42.0.0.jar" -d bin src/*.java
```

## 3. Run

```
# Windows
java -cp "bin;lib\sqlite-jdbc-3.42.0.0.jar" Main

# macOS/Linux
java -cp "bin:lib/sqlite-jdbc-3.42.0.0.jar" Main
```

(If you went with Option A, put both jars on the classpath, separated the
same way: `bin;lib\sqlite-jdbc-3.44.1.0.jar;lib\slf4j-api-2.0.9.jar` etc.)

A `reservation.db` SQLite file is created automatically in the folder you
run the app from, with two tables (`users`, `reservations`) and a seeded
default login.

## Default login (and creating new accounts)

```
Username: admin
Password: admin123
```

This one account is seeded automatically the first time the `users` table
is empty (`Database.seedDefaultUser`) — it's not special in any way, it's
just a normal row in `users`. There's no separate "admin role" in this
schema; every account that can log in can do everything (book, cancel,
view all bookings).

**To create more users/logins:** click **"Create New Account"** on the
Login screen (`RegisterFrame`). It validates the username (3-20
letters/numbers/underscore), requires a 4+ character password with
confirmation, and blocks duplicate usernames.

If you'd rather add one by hand without the UI, open `reservation.db` in
any SQLite tool (e.g. [DB Browser for SQLite](https://sqlitebrowser.org/))
and insert a row directly:

```sql
INSERT INTO users (username, password) VALUES ('myadmin', 'mypassword');
```

## How each checklist item is implemented

- **Login form** — `LoginFrame`. Wrong credentials show a red "Access
  denied" message inline; empty fields are rejected before hitting the DB.
  A "Create New Account" button opens `RegisterFrame` for self-service
  signup (username/password validation, duplicate-username check).
- **Reservation form** — `ReservationFrame`. Train Name auto-fills from
  Train Number for the trains in `TrainData` (10 sample trains); unknown
  numbers just leave the name field editable so you can type your own.
  Source/Destination are dropdowns populated from `Stations.java` (still
  editable, so you can type a station not on the list).
- **Live seat availability** — as soon as train number, class and date are
  all filled in, a label shows "Seats available: X of 30 (Y occupied)" for
  that exact train + class + date combination (`Database.countOccupiedSeats`).
  Booking is blocked once a train/class/date combo hits the cap
  (`Database.TOTAL_SEATS_PER_CLASS`, 30 by default — change that constant to
  raise/lower it), with the check re-run inside the booking transaction so
  two near-simultaneous bookings can't both grab the last seat.
- **Book button** — inserts the row and generates a random PNR inside one
  JDBC transaction (`conn.setAutoCommit(false)` / commit / rollback), so a
  failed insert never leaves a "used" PNR behind. A numeric **Ticket ID**
  is also auto-assigned by SQLite itself (`id INTEGER PRIMARY KEY
  AUTOINCREMENT`), separate from the PNR, and returned via
  `Statement.RETURN_GENERATED_KEYS`.
- **Confirmation dialog** — shown via `JOptionPane` right after a
  successful insert, listing the Ticket ID, PNR, and every field.
- **View All Bookings** — `ViewBookingsFrame`, reachable from the
  Dashboard. Sortable `JTable` of every reservation (click a column header
  to sort), with a Refresh button and a live row count.
- **Cancellation form** — `CancellationFrame`. Fetch button looks up the PNR
  and fills a read-only details box; the Confirm button is disabled until a
  booking has actually been fetched.
- **"Are you sure?" dialog** — `JOptionPane.showConfirmDialog` with
  YES/NO before the `DELETE` statement runs.
- **Input validation** — required-field checks, `train number must be
  numeric` (regex), date parsed strictly as `yyyy-MM-dd` via a non-lenient
  `SimpleDateFormat` (rejects things like `2026-13-40`), and a same-station
  check as a bonus.
- **SQL injection prevention** — every query uses `PreparedStatement` with
  bound `?` parameters, never string concatenation.
- **Screen navigation** — every screen after the Dashboard has a
  "← Back to Dashboard" button, and the Dashboard hides itself while a
  child screen is open, reappearing automatically on Back or window close.
- **Session** — the Dashboard shows which user is logged in and when the
  session started; Logout returns to the Login screen.

## Switching to MySQL instead of SQLite

Only `Database.java` needs to change:

```java
private static final String URL = "jdbc:mysql://localhost:3306/reservation_db";
// DriverManager.getConnection(URL, "youruser", "yourpassword");
```

Add the MySQL Connector/J jar to `lib/` and the classpath the same way as
the SQLite jar, and swap `AUTOINCREMENT`-style SQL if you add any (this
schema uses a randomly generated text PNR as the primary key instead, so no
change needed there).

## Notes / possible extensions

- **Upgrading from an earlier version of this project:** the reservations
  table gained an `id` (Ticket ID) column. If you already have a
  `reservation.db` file from before, delete it once so it gets recreated
  with the new schema — `CREATE TABLE IF NOT EXISTS` won't add the new
  column to an existing table automatically.
- Passwords are stored in plain text in `users` for simplicity — for
  anything beyond a class assignment, hash them (e.g. with `jBCrypt`) before
  storing.
- `TrainData` and `Stations` are hard-coded in-memory lists. A more complete
  version would give each its own DB table with an admin screen to manage it.
- PNR is a random 10-digit numeric string checked for uniqueness against
  the DB before insert (loops until a free one is found). Ticket ID is a
  separate, simpler auto-incrementing integer assigned by SQLite.
- Seat cap is one constant (`Database.TOTAL_SEATS_PER_CLASS`) applied per
  train + class + date. A real system would store per-train, per-coach
  capacity in its own table instead.
