import static spark.Spark.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.*;

public class BikeTaxiBackend {
    static class User {
        String name, email, password;
        User(String name, String email, String password) {
            this.name = name; this.email = email; this.password = password;
        }
    }

    static class Booking {
        String userEmail, source, destination;
        double distanceKm, fare;
        Booking(String userEmail, String source, String destination, double distanceKm, double fare) {
            this.userEmail = userEmail; this.source = source; this.destination = destination;
            this.distanceKm = distanceKm; this.fare = fare;
        }
    }

    static Map<String, User> users = Collections.synchronizedMap(new HashMap<>());
    static Map<String, String> sessions = Collections.synchronizedMap(new HashMap<>());
    static List<Booking> bookings = Collections.synchronizedList(new ArrayList<>());
    static final double FARE_PER_KM = 1.5;
    static Gson gson = new Gson();

    public static void main(String[] args) {
        port(4567);
        enableCORS();

        post("/register", (req, res) -> {
            User u = gson.fromJson(req.body(), User.class);
            if (u.name == null || u.email == null || u.password == null
               || u.name.isEmpty() || u.email.isEmpty() || u.password.isEmpty()) {
                res.status(400);
                return gson.toJson(Map.of("error", "All fields required"));
            }
            synchronized(users) {
                if (users.containsKey(u.email.toLowerCase())) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "Email already registered"));
                }
                users.put(u.email.toLowerCase(), u);
            }
            res.type("application/json");
            return gson.toJson(Map.of("success", "Registered"));
        });

        post("/login", (req, res) -> {
            JsonObject obj = gson.fromJson(req.body(), JsonObject.class);
            String email = obj.has("email") ? obj.get("email").getAsString() : "";
            String password = obj.has("password") ? obj.get("password").getAsString() : "";
            if (email.isEmpty() || password.isEmpty()) {
                res.status(400);
                return gson.toJson(Map.of("error", "Email and password needed"));
            }
            User u = users.get(email.toLowerCase());
            if (u == null || !u.password.equals(password)) {
                res.status(401);
                return gson.toJson(Map.of("error", "Invalid credentials"));
            }
            String token = UUID.randomUUID().toString();
            sessions.put(token, u.email.toLowerCase());
            res.type("application/json");
            return gson.toJson(Map.of("token", token, "name", u.name));
        });

        post("/book", (req, res) -> {
            String token = req.headers("Authorization");
            if (token == null || !sessions.containsKey(token)) {
                res.status(401);
                return gson.toJson(Map.of("error", "Unauthorized"));
            }
            JsonObject obj = gson.fromJson(req.body(), JsonObject.class);
            String source = obj.has("source") ? obj.get("source").getAsString() : "";
            String destination = obj.has("destination") ? obj.get("destination").getAsString() : "";
            double distance = obj.has("distance") ? obj.get("distance").getAsDouble() : -1;
            if (source.isEmpty() || destination.isEmpty() || distance <= 0) {
                res.status(400);
                return gson.toJson(Map.of("error", "Valid source, destination and distance required"));
            }
            String userEmail = sessions.get(token);
            double fare = distance * FARE_PER_KM;
            bookings.add(new Booking(userEmail, source, destination, distance, fare));
            res.type("application/json");
            return gson.toJson(Map.of("success", "Booking created", "fare", fare));
        });

        get("/bookings", (req, res) -> {
            String token = req.headers("Authorization");
            if (token == null || !sessions.containsKey(token)) {
                res.status(401);
                return gson.toJson(Map.of("error", "Unauthorized"));
            }
            String userEmail = sessions.get(token);
            List<Booking> userBookings = new ArrayList<>();
            synchronized(bookings) {
                for (Booking b : bookings) {
                    if (b.userEmail.equalsIgnoreCase(userEmail)) userBookings.add(b);
                }
            }
            res.type("application/json");
            return gson.toJson(userBookings);
        });

        post("/logout", (req, res) -> {
            String token = req.headers("Authorization");
            if (token != null) sessions.remove(token);
            return gson.toJson(Map.of("success", "Logged out"));
        });
    }

    private static void enableCORS() {
        options("/*", (req, res) -> {
            String reqHeaders = req.headers("Access-Control-Request-Headers");
            if (reqHeaders != null) res.header("Access-Control-Allow-Headers", reqHeaders);
            String reqMethod = req.headers("Access-Control-Request-Method");
            if (reqMethod != null) res.header("Access-Control-Allow-Methods", reqMethod);
            return "OK";
        });
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Credentials", "true");
        });
    }
}
