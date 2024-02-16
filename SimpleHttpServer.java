package Asset_files;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.InetSocketAddress;
public class SimpleHttpServer {
    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        

        server.createContext("/", new MyHandler());
        

        server.start();
        
        System.out.println("Server started on port 8000");
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if("POST".equals(exchange.getRequestMethod())){
                handlePostRequest(exchange);
            }
            else if("GET".equals(exchange.getRequestMethod())){
                handleGetRequest(exchange);
            }

            



        }
        private void handleGetRequest(HttpExchange exchange) throws IOException{
            String response="Received get request";
            sendResponse(exchange,response);
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException{
            InputStream requestBody = exchange.getRequestBody();
            BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
            StringBuilder requestBodyBuilder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                requestBodyBuilder.append(line);
            }
            String requestBodyString = requestBodyBuilder.toString();
            

            String response = "Received POST request with body: " + requestBodyString;
            sendResponse(exchange, response);
        }
        private void sendResponse(HttpExchange exchange, String response) throws IOException {
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
