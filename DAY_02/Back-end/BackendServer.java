import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class BackendServer {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/use_auth";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Thameem@836";
    
    // Secret key used to sign the tokens. Keep this private!
    private static final String JWT_SECRET = "super-secret-transit-key-change-this-in-production-123456";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(5000), 0);

        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/mybookings",new MyBookingsHandler());

        server.setExecutor(null);
        System.out.println("Native Frameworkless Java API Server running on port 5000 with JWT...");
        server.start();
    }

    // --- REGISTER HANDLER (WRITE OPERATION) ---
    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            applyCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    String body = readRequestBody(exchange);
                    System.out.println("\n--- NEW REGISTER REQUEST ---");
                    
                    String username = extractJsonValue(body, "username");
                    String password = extractJsonValue(body, "password");

                    if (username.isEmpty() || password.isEmpty()) {
                        sendResponse(exchange, 400, "{\"error\":\"Missing fields\"}");
                        return;
                    }

                    int resultCode = writeUserToDatabase(username, password);
                    if (resultCode == 201) {
                        sendResponse(exchange, 201, "{\"message\":\"Registration successful!\"}");
                    } else if (resultCode == 409) {
                        sendResponse(exchange, 409, "{\"error\":\"Username already exists\"}");
                    } else {
                        sendResponse(exchange, 400, "{\"error\":\"Database rejected request\"}");
                    }
                } catch (Exception e) {
                    System.out.println("SYSTEM EXCEPTION IN REGISTRATION:");
                    e.printStackTrace();
                    sendResponse(exchange, 500, "{\"error\":\"Internal Server Error\"}");
                }
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            }
        }

        private int writeUserToDatabase(String username, String password) {
            String checkSql = "SELECT id FROM users WHERE username = ?";
            String insertSql = "INSERT INTO users (username, password) VALUES (?, ?)";
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                        checkStmt.setString(1, username);
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next()) return 409; 
                        }
                    }
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, username);
                        insertStmt.setString(2, password);
                        insertStmt.executeUpdate();
                        return 201;
                    }
                }
            } catch (Exception e) {
                System.out.println("DATABASE ERROR DURING WRITE:");
                e.printStackTrace();
                return 500; 
            }
        }
    }

    // --- LOGIN HANDLER (READ OPERATION) ---
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            applyCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    String body = readRequestBody(exchange);
                    System.out.println("\n--- NEW LOGIN REQUEST ---");

                    String username = extractJsonValue(body, "username");
                    String password = extractJsonValue(body, "password");

                    boolean isAuthenticated = verifyUserInDatabase(username, password);

                    if (isAuthenticated) {
                        // Generate a signed cryptographic JSON Web Token
                        String token = JwtUtil.generateToken(username, JWT_SECRET);
                        String jsonResponse = "{\"username\":\"" + username + "\", \"token\":\"" + token + "\"}";
                        sendResponse(exchange, 200, jsonResponse);
                    } else {
                        sendResponse(exchange, 401, "{\"error\":\"Invalid username or password\"}");
                    }
                } catch (Exception e) {
                    System.out.println("SYSTEM EXCEPTION IN LOGIN:");
                    e.printStackTrace();
                    sendResponse(exchange, 500, "{\"error\":\"Internal Server Error\"}");
                }
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            }
        }

        private boolean verifyUserInDatabase(String username, String password) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                        PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, password);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        return rs.next();
                    }
                }
            } catch (Exception e) {
                System.out.println("DATABASE ERROR DURING READ:");
                e.printStackTrace();
                return false;
            }
        }
    }

    static class MyBookingsHandler
implements HttpHandler
{
    @Override
    public void handle(HttpExchange exchange)
    throws IOException
    {
        //applyCorsHeaders(exchange);
          applyCorsHeaders(exchange);

    if("OPTIONS".equalsIgnoreCase(
        exchange.getRequestMethod()))
    {
        exchange.sendResponseHeaders(204,-1);
        return;
    }
        System.out.println("bookins list request.......");
        if("GET".equalsIgnoreCase(
            exchange.getRequestMethod()))
        {
            try
            {
                System.out.println(
"Username Header: " +
exchange.getRequestHeaders()
        .getFirst("Username")
);
                String username =
                exchange.getRequestHeaders()
                        .getFirst("Username");

                String json =
                getBookings(username);
                System.out.println("Bookings for "+username+": "+json);
                sendResponse(
                    exchange,
                    200,
                    json
                );
            }
            catch(Exception e)
            {
                e.printStackTrace();

                sendResponse(
                    exchange,
                    500,
                    "{\"error\":\"Failed to load bookings\"}"
                );
            }
        }
    }

    private String getBookings(
        String username)
    {
        System.out.println("Fetching from db bookings lists;");
        StringBuilder json =
        new StringBuilder("[");

        String sql =
        """
        SELECT *
        FROM bookings
        WHERE username = ?
        """;

        try
        {
            Connection conn =
            DriverManager.getConnection(
                DB_URL,
                DB_USER,
                DB_PASSWORD
            );

            PreparedStatement pstmt =
            conn.prepareStatement(sql);

            pstmt.setString(1, username);

            ResultSet rs =
            pstmt.executeQuery();

            boolean first = true;

            while(rs.next())
            {
                if(!first)
                {
                    json.append(",");
                }

                json.append("{")
                    .append("\"bookingId\":")
                    .append(rs.getInt("booking_id"))
                    .append(",")

                    .append("\"passengerName\":\"")
                    .append(rs.getString("passenger_name"))
                    .append("\",")

                    .append("\"source\":\"")
                    .append(rs.getString("source_station"))
                    .append("\",")

                    .append("\"destination\":\"")
                    .append(rs.getString("destination_station"))
                    .append("\",")

                    .append("\"journeyDate\":\"")
                    .append(rs.getDate("journey_date"))
                    .append("\",")

                    .append("\"seatNumber\":\"")
                    .append(rs.getString("seat_number"))
                    .append("\",")

                    .append("\"fare\":")
                    .append(rs.getDouble("fare"))
                    .append(",")

                    .append("\"status\":\"")
                    .append(rs.getString("booking_status"))
                    .append("\"")
                    .append("}");

                first = false;
            }

            json.append("]");

            conn.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return json.toString();
    }
}
    // --- REUSABLE UTILITIES ---
    private static void applyCorsHeaders(
    HttpExchange exchange)
{
    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Origin",
        "*"
    );

    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Methods",
        "GET, POST, OPTIONS"
    );

    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Headers",
        "Content-Type, Authorization, Username"
    );
}
    // private static void applyCorsHeaders(HttpExchange exchange) {
    //     exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "http://localhost:3000");
    //     exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
    //     exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    //     exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
    // }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private static String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(json);
        return m.find() ? m.group(1) : "";
    }

    // --- PURE NATIVE CRYPTOGRAPHIC JWT COMPONENT ---
    static class JwtUtil {
        public static String generateToken(String username, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
            String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            long now = System.currentTimeMillis() / 1000;
            long exp = now + 3600; // Token expiry configuration: 1 hour duration
            String payload = "{\"sub\":\"" + username + "\",\"iat\":" + now + ",\"exp\":" + exp + "}";

            String base64UrlHeader = base64UrlEncode(header.getBytes(StandardCharsets.UTF_8));
            String base64UrlPayload = base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8));

            String signatureInput = base64UrlHeader + "." + base64UrlPayload;
            
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSHA256.init(secretKey);
            
            byte[] signatureBytes = hmacSHA256.doFinal(signatureInput.getBytes(StandardCharsets.UTF_8));
            String base64UrlSignature = base64UrlEncode(signatureBytes);return signatureInput + "." + base64UrlSignature;}private static String base64UrlEncode(byte[] input) {return Base64.getUrlEncoder().withoutPadding().encodeToString(input);}}}
            