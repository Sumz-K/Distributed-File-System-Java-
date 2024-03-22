import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class localserver {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new PostHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server is running on port 8080");
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    static class PostHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Only accept POST requests
            if ("POST".equals(exchange.getRequestMethod())) {
                // Read the request body
                BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    requestBody.append(line).append("\n");
                }
                br.close();

                // Print the request body
                System.out.println("Received POST request body:");
                String body = requestBody.toString();
                System.out.println(body);
                String[] lines = body.split(":", 2);
                byte[] array = Base64.getDecoder().decode(lines[1].trim());
                System.out.println("Decoded byte array: " + bytesToHex(array));
                
                // Send a response
                String response = "Request body received successfully!";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                // Reject non-POST requests with a 405 Method Not Allowed response
                System.out.println("erroring");
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }
}
