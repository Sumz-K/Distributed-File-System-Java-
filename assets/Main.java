import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

class Datanode {
    private String name;
    private String ip;
    private double availableStorage;
    private boolean isAlive;
    private DatanodeStore store;

    public Datanode(String name, String ip, double availableStorage, DatanodeStore store) {
        this.name = name;
        this.ip = ip;
        this.availableStorage = availableStorage;
        this.isAlive = true;
        this.store = store;
    }

    public byte[] retrieveFile(String filename) {
        //System.out.print("here now\n");
        return store.retrieve(filename);
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public double getAvailableStorage() {
        return availableStorage;
    }

    public boolean isAlive() {
        return isAlive;
    }
}

class DatanodeStore {
    private String path;
    private String filename;
    public DatanodeStore(String basePath) {
        this.path = basePath;
    }

    public byte[] retrieve(String file) {
       
        try {
           
            filename=path+file;
            System.out.print(filename);
            ProcessBuilder processBuilder = new ProcessBuilder("cat", filename);


            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            byte[] fileContents = outputStream.toByteArray();

            inputStream.close();
            outputStream.close();


            return fileContents;
        } catch (IOException e) {
            return null;
        }
    }
}

class DatanodeServer {
    Datanode datanode;

    public DatanodeServer(Datanode datanode) {
        this.datanode = datanode;
    }

    public void startServer() throws Exception {
        //System.out.print(datanode.getIp());
        HttpServer server = HttpServer.create(new InetSocketAddress(datanode.getIp(), 6000), 0);
        server.createContext("/fetchfile", new ConnectionHandler());
        server.start();
        System.out.println("Server running on port 6000");
    }

    class ConnectionHandler implements HttpHandler {
        //private Datanode datanode;
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                handlePostRequest(exchange);
            } else if ("GET".equals(exchange.getRequestMethod())) {
                // handleGetRequest(exchange);
            }
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {
            System.out.print("Request received\n");
            InputStream requestBody = exchange.getRequestBody();
            BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
            StringBuilder requestBodyBuilder = new StringBuilder();
        
            String line;
            while ((line = reader.readLine()) != null) {
                requestBodyBuilder.append(line);
            }
            String requestBodyString = requestBodyBuilder.toString();
            System.out.print(requestBodyString);
            // Access the Datanode instance from the enclosing class DatanodeServer
            byte[] buffer = datanode.retrieveFile(requestBodyString);
            sendResponse(exchange, buffer);
        }
        

        private void sendResponse(HttpExchange exchange, byte[] response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }
}

public class Main {
    public static void main(String[] args) throws Exception {

        DatanodeStore store = new DatanodeStore("/Users/sumukhkowndinya/Desktop/Dummyfiles/");


        Datanode datanode = new Datanode("Node1", "192.168.1.2", 100.0, store);

    
        DatanodeServer server = new DatanodeServer(datanode);

       
        server.startServer();

        
    }
}
