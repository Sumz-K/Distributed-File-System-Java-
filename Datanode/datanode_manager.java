import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

class request {
    public String reply;
    public JSONObject header;

    public void set_header(JSONObject header) {
        this.header = header;

    }

    public void get(String url) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            if (header != null) {
                Iterator<String> keys = header.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = this.header.get(key);
                    httpGet.setHeader(key, value.toString());
                }
            }
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String responseBody = EntityUtils.toString(entity);
                httpClient.close();
                this.reply = responseBody;
            } else {
                httpClient.close();
                this.reply = "Empty response received from the server.";
            }
        } catch (Exception e) {
            this.reply = "Error in getting the response from the server. " + e.getMessage();
        }

    }

    public void post(String endpoint, JSONObject data) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(endpoint);

            if (header != null) {
                Iterator<String> keys = header.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    try {
                        Object value = this.header.get(key);
                        httpPost.setHeader(key, value.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            String filename = "";

            try {
                filename = data.get("filename").toString();
                filename += ":";
                String contents = data.getString("contents");
                filename += contents;
            } catch (Exception e) {
                System.err.println("Failed to get 'filename' field from json object.");
            }

            httpPost.setEntity(new StringEntity(filename));
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // Convert the response entity to a string
                String responseBody = EntityUtils.toString(entity);
                this.reply = responseBody;
            } else {
                this.reply = ("Empty response received from the server.");
            }
            httpClient.close();
        } catch (IOException e) {
            this.reply = ("Error occurred while making the HTTP POST request: " + e.getMessage());
        }
    }

    public String reply_in_text() {
        return this.reply.toString();
    }

    public JSONObject reply_in_json() {
        try {
            JSONObject obj = new JSONObject(reply);
            return obj;
        } catch (Exception e) {
            return null;
        }

    }

}

class DummyClass {
    static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String byteArrayToString(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            sb.append(b).append(" ");
        }
        return sb.toString();
    }
}

class hash_and_file {
    byte[] file;
    HashMap<String, byte[]> map = new HashMap<>();

    hash_and_file(byte[] file) {
        this.file = file;
    }

    public Map<String, byte[]> split_and_give_me() throws IOException {
        FileSplitting splitter = new FileSplitting();
        List<byte[]> chunks = splitter.splitBysize(file, 128000);
        String[] hashes = splitter.getHash(chunks);
        for (int i = 0; i < hashes.length; i++) {
            map.put(hashes[i], chunks.get(i));
        }
        return map;
    }
}

class manager_server {

    public void startServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/", new ConnectionHandler());
        server.start();
        System.out.println("Server running on port 8080");
    }

    class ConnectionHandler implements HttpHandler {
        // private Datanode datanode;
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                handlePostRequest(exchange);
            } else if ("GET".equals(exchange.getRequestMethod())) {
                handleGetRequest(exchange);
            }
        }

        private void handleGetRequest(HttpExchange exchange) throws IOException {
            String uri = exchange.getRequestURI().toString();
            // System.out.p rint(uri);

            // http://192.168.1.2:6000/fetchfile?filename=eight_kb.jpeg

            byte[] data = {};
            sendResponse(exchange, data);

        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {

            // filename:data

            int replicationFactor = 3;
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
            hash_and_file obj = new hash_and_file(array);
            Map<String, byte[]> mapping = new HashMap<>();
            mapping = obj.split_and_give_me();

            delivery(mapping, replicationFactor);

            // Send a response
            String response = "Request body received successfully!";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            byte[] dummy_resp = {};
            sendResponse(exchange, dummy_resp);
        }

        private void delivery(Map<String, byte[]> map, int replicationFactor) {
            String url1 = "https://datanode1.run-ap-south1.goorm.site/fetchfile";
            String url2 = "https://datanode2.run-ap-south1.goorm.site/fetchfile";

            ArrayList<String> nodes=new ArrayList<>();
            nodes.add(url1);
            nodes.add(url2);

            
                for (Map.Entry<String, byte[]> entry : map.entrySet()) {
                    request object = new request();
                    JSONObject postis = new JSONObject();
                    String filename = entry.getKey();
                    byte[] value = entry.getValue();
                    try {
                        postis.put("contents", Base64.getEncoder().encodeToString(value));
                        postis.put("filename", filename);
                        for(int i=0;i<replicationFactor;i++){
                            object.post(nodes.get(i), postis);
                        }
                        System.out.println(object.reply_in_text());
                    } catch (Exception e) {
                        // TODO: handle exception
                        System.out.println("Error in delivery");
                    }
                }
            
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

class FileSplitting {
    String algorithm = "SHA-256";

    public List<byte[]> splitBysize(byte[] x, int chunkSize) throws IOException {
        List<byte[]> chunks = new ArrayList<>();
        int offset = 0;

        while (offset < x.length) {
            int length = Math.min(chunkSize, x.length - offset);
            byte[] chunk = new byte[length];
            System.arraycopy(x, offset, chunk, 0, length);
            chunks.add(chunk);
            offset += length;
        }

        return chunks;
    }

    public String[] getHash(List<byte[]> array) {
        ArrayList<String> list = new ArrayList<>();
        for (byte[] i : array) {
            try {
                byte[] hash = hashByteArray(i, algorithm);
                list.add(DummyClass.bytesToHex(hash));
            } catch (NoSuchAlgorithmException e) {
                System.out.println("errorin getting hash");
            }
        }
        return list.toArray(new String[list.size()]);
    }

    private byte[] hashByteArray(byte[] array, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        return digest.digest(array);
    }

    public byte[] join(List<byte[]> list) {
        int totalLength = list.stream().mapToInt(arr -> arr.length).sum();
        byte[] result = new byte[totalLength];
        int offset = 0;
        for (byte[] arr : list) {
            System.arraycopy(arr, 0, result, offset, arr.length);
            offset += arr.length;
        }
        return result;
    }
}

public class datanode_manager {
    public static void main(String[] args) throws Exception {
        manager_server server = new manager_server();
        server.startServer();
    }
}
