import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;

import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.net.URLDecoder;
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

    public String writeFile(String filename,String location){
        return store.writeFile(filename,location);
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


    public String writeFile(String file,String location){
        //location is where i write the file to
        //File outPutFile = File.createTempFile("temp-", "-unsplit", new File(location));
        String filename=path+file;
        
        try{
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

            writehelper(fileContents,location);

            return "Succesful";
        }
        catch (IOException e) {
           return "Write unsuccessful";
        }
    }
    private void writehelper(byte[] contents,String location){
        try (FileOutputStream fos = new FileOutputStream(location)) {
            fos.write(contents);
            System.out.println("Data written to file successfully.");
        } catch (IOException e) {
            e.printStackTrace();
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
                handleGetRequest(exchange);
            }
        }

        private void handleGetRequest(HttpExchange exchange) throws IOException{
            String uri=exchange.getRequestURI().toString();
            //System.out.print(uri);

            //http://192.168.1.2:6000/fetchfile?filename=eight_kb.jpeg
            String file="";
            String[] parts = uri.split("\\?", 2);
            if (parts.length == 2) {
                String params = parts[1];
                String[] key_val = params.split("=", 2);
                if (key_val.length == 2 && key_val[0].equals("filename")) {
                    try{
                    file = URLDecoder.decode(key_val[1], "UTF-8");
                    //System.out.print(file);
                    }
                    catch(Exception e){
                        System.out.print(e);
                    }
                }
            }
            byte[] data=datanode.retrieveFile(file);
            sendResponse(exchange, data);

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
            //System.out.print(requestBodyString);
            //System.out.print(requestBodyString);

            String writepath="";
            String[] parts = requestBodyString.split(":", 2);


            String prefix_path="/Users/sumukhkowndinya/Desktop/Dummyfiles/written_files/";
            writepath=prefix_path+parts[1];
            
            String file_to_be_written=parts[0];
            System.out.println(writepath);
            // Access the Datanode instance from the enclosing class DatanodeServer
            datanode.writeFile(file_to_be_written,writepath);
             byte[] dummy_resp={};
            sendResponse(exchange, dummy_resp);
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
