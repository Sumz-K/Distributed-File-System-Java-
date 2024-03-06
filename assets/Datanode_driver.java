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
            // Create a ProcessBuilder to execute the cat command
            //System.out.print(filename);
            ProcessBuilder processBuilder = new ProcessBuilder("cat", filename);

            // Start the process and capture its output
            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // Read the output of the process and write it to the byte array
            int data;
            while ((data = inputStream.read()) != -1) {
                outputStream.write(data);
            }

            // Close the streams
            inputStream.close();
            outputStream.close();

            // Get the byte array containing the file contents
            byte[] fileContents = outputStream.toByteArray();

            // Print the contents of the byte array
            return fileContents;
        } catch (IOException e) {
            return null;
        }
    }
    

}




public class Datanode_driver {
    public static void create_server(String ip) throws Exception{
        HttpServer server=HttpServer.create(new InetSocketAddress(ip,8080),0);

        server.createContext("/uploadfile",new Handler());
        server.start();
        System.out.println("Server running on port 8080");
    }

    public static void main(String[] args) throws Exception{
        String ip="192.168.0.108";
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
            String ascii_data="";
            for(byte i:buffer){
                ascii_data+=i;
                ascii_data+=" ";
            }
            //System.out.print(buffer);
            String response = new String(buffer, "UTF-8");

    
            
           
            sendResponse(exchange, ascii_data);
        }
        private void sendResponse(HttpExchange exchange, String response) throws IOException {
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }


}