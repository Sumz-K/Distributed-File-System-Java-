import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

class Datanode{
    public String filename;
    Datanode(String name){
        this.filename = "/Users/sumukhkowndinya/Desktop/Dummyfiles/"+name;
    }
    public byte[] retrieve() {
        try {
            
            //System.out.print(filename);
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




public class Datanode_driver {
    public static void create_server(String ip) throws Exception{
        HttpServer server=HttpServer.create(new InetSocketAddress(ip,8080),0);

        server.createContext("/fetchfile",new Handler());
        server.start();
        System.out.println("Server running on port 8080");
    }

    public static void main(String[] args) throws Exception{
        String ip="x.x.x.x";
        create_server(ip);
        
    }
    static class Handler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if("POST".equals(exchange.getRequestMethod())){
                handlePostRequest(exchange);
                

            }
            else if("GET".equals(exchange.getRequestMethod())){
                //handleGetRequest(exchange);
            }

        }
        private void handlePostRequest(HttpExchange exchange) throws IOException{
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


            Datanode dn1=new Datanode(requestBodyString);
            byte[] buffer=dn1.retrieve();
            sendResponse(exchange, buffer);
            // exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            // exchange.sendResponseHeaders(200, buffer.length);

            // try (OutputStream os = exchange.getResponseBody()) {
            //     os.write(buffer);
            //     os.close();
            // }
            
        }
        private void sendResponse(HttpExchange exchange, byte[] response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
                os.close();
            }
        }
    }


}
