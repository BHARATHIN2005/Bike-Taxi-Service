import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BikeTaxiApp {
    static Scanner scanner = new Scanner(System.in);
    static List<User> users = new ArrayList<>();
    static List<Booking> bookings = new ArrayList<>();
    static User currentUser = null;
    static final double FARE_PER_KM = 1.5;

    public static void main(String[] args) {
        System.out.println("========= Welcome to Bike Taxi Application =========");
        boolean exit = false;
        while (!exit) {
            if (currentUser == null) {
                System.out.println("\n1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Choose an option: ");
                String input = scanner.nextLine();
                switch (input) {
                    case "1" -> registerUser();
                    case "2" -> loginUser();
                    case "3" -> {
                        System.out.println("Thank you for using Bike Taxi!");
                        exit = true;
                    }
                    default -> System.out.println("Invalid option, please try again.");
                }
            } else {
                System.out.println("\nLogged in as: " + currentUser.getName());
                System.out.println("1. Book a Ride");
                System.out.println("2. View Current Bookings");
                System.out.println("3. Logout");
                System.out.print("Choose an option: ");
                String input = scanner.nextLine();
                switch (input) {
                    case "1" -> bookRide();
                    case "2" -> viewBookings();
                    case "3" -> {
                        currentUser = null;
                        System.out.println("Logged out successfully.");
                    }
                    default -> System.out.println("Invalid option, please try again.");
                }
            }
        }
        scanner.close();
    }

    private static void registerUser() {
        System.out.println("\n-- User Registration --");
        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        System.out.print("Enter your email: ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) {
            System.out.println("Email cannot be empty.");
            return;
        }
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                System.out.println("User with this email already exists.");
                return;
            }
        }
        System.out.print("Set your password: ");
        String password = scanner.nextLine();
        users.add(new User(name, email, password));
        System.out.println("User registered successfully! You can now login.");
    }

    private static void loginUser() {
        System.out.println("\n-- User Login --");
        System.out.print("Enter your email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email) && user.getPassword().equals(password)) {
                currentUser = user;
                System.out.println("Login successful! Welcome, " + currentUser.getName() + ".");
                return;
            }
        }
        System.out.println("Invalid email or password. Please try again.");
    }

    private static void bookRide() {
        System.out.println("\n-- Book a Ride --");
        System.out.print("Enter pickup location: ");
        String source = scanner.nextLine().trim();
        System.out.print("Enter destination location: ");
        String destination = scanner.nextLine().trim();
        if (source.isEmpty() || destination.isEmpty()) {
            System.out.println("Source and destination cannot be empty.");
            return;
        }
        System.out.print("Enter distance in km (numeric): ");
        String distInput = scanner.nextLine().trim();
        double distance;
        try {
            distance = Double.parseDouble(distInput);
            if (distance <= 0) {
                System.out.println("Distance must be positive.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid distance format.");
            return;
        }

        double fare = distance * FARE_PER_KM;
        Booking booking = new Booking(currentUser.getName(), source, destination, distance, fare);
        bookings.add(booking);
        System.out.printf("Ride booked successfully! Estimated fare: $%.2f%n", fare);
    }

    private static void viewBookings() {
        System.out.println("\n-- Your Current Bookings --");
        boolean found = false;
        for (Booking b : bookings) {
            if (b.getUserName().equals(currentUser.getName())) {
                System.out.println(b);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No bookings found.");
        }
    }
}

class User {
    private final String name;
    private final String email;
    private final String password;

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}

class Booking {
    private final String userName;
    private final String source;
    private final String destination;
    private final double distanceKm;
    private final double fare;

    public Booking(String userName, String source, String destination, double distanceKm, double fare) {
        this.userName = userName;
        this.source = source;
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.fare = fare;
    }

    public String getUserName() { return userName; }

    @Override
    public String toString() {
        return String.format("From %s to %s - Distance: %.2f km - Fare: $%.2f", source, destination, distanceKm, fare);
    }
}
