import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BackendServer {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/use_auth";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Thameem@836";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(5000), 0);

        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/register", new RegisterHandler());

        server.setExecutor(null);
        System.out.println("Native Frameworkless Java API Server running on port 5000...");
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
                    // Read the body EXACTLY ONCE here
                    String body = readRequestBody(exchange);
                    System.out.println("\n--- NEW REGISTER REQUEST ---");
                    System.out.println("RECEIVED RAW BODY: " + body);

                    String username = extractJsonValue(body, "username");
                    String password = extractJsonValue(body, "password");
                    System.out.println("PARSED DATA -> User: " + username + " | Pass: " + password);

                    if (username.isEmpty() || password.isEmpty()) {
                        sendResponse(exchange, 400, "{\"error\":\"Missing fields\"}");
                        return;
                    }

                    // DB WRITE
                    boolean success = writeUserToDatabase(username, password);
                    if (success) {
                        sendResponse(exchange, 201, "{\"message\":\"Registration successful!\"}");
                    } else {
                        sendResponse(exchange, 400, "{\"error\":\"Username already exists or DB rejected request\"}");
                    }
                } catch (Exception e) {
                    System.out.println("SYSTEM EXCEPTION IN REGISTRATION:");
                    e.printStackTrace(); // This will print the actual error stack trace to the console
                    sendResponse(exchange, 500, "{\"error\":\"Internal Server Error\"}");
                }
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            }
        }

        private boolean writeUserToDatabase(String username, String password) {
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                        PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, password);
                    pstmt.executeUpdate();
                    return true;
                }
            } catch (Exception e) {
                System.out.println("DATABASE ERROR DURING WRITE:");
                e.printStackTrace(); // Tells you if the table name is wrong or database is down
                return false; 
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
                    System.out.println("RECEIVED RAW BODY: " + body);

                    String username = extractJsonValue(body, "username");
                    String password = extractJsonValue(body, "password");

                    boolean isAuthenticated = verifyUserInDatabase(username, password);

                    if (isAuthenticated) {
                        String jsonResponse = "{\"username\":\"" + username + "\", \"token\":\"native-session-key-example\"}";
                        sendResponse(exchange, 200, jsonResponse);
                    } else {
                        sendResponse(exchange, 400, "{\"error\":\"Invalid username or password\"}");
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

    // --- REUSABLE UTILITIES ---
    private static void applyCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

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
}
