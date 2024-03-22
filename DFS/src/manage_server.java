import java.util.*;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.io.*;
import org.json.JSONException;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
public class manage_server {
    ArrayList<String> datanodes = new ArrayList<String>();
    int replication_factor = 3;

    public manage_server(ArrayList<String> list) {
        this.datanodes = list;
    }
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
            // System.out.print(uri);

            // http://192.168.1.2:6000/fetchfile?filename=eight_kb.jpeg

            
            sendResponse(exchange, "",200);

        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {

            // filename:data
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
            // System.out.println(body);
            String[] lines = body.split(":", 2);
            byte[] array = Base64.getDecoder().decode(lines[1].trim());
            hash_and_file obj = new hash_and_file(array);
            String filename = lines[0];
            FileSplitting filehashing = new FileSplitting();
            String file_hash = filehashing.getFilehash(array);
            System.out.println(filename + ":"+file_hash);
            Map<String, byte[]> mapping = new HashMap<>();
            mapping = obj.split_and_give_me();
            Map<String, ArrayList<String>> file_block_locations = delivery(mapping);
            System.out.println("Successfully written now writing into mongo database");
            insertIntoMongo(file_block_locations,filename,file_hash);
            // Send a response
            String response = "Request body received successfully!";
            sendResponse(exchange, response,200);
            
        }

        private void insertIntoMongo(Map<String, ArrayList<String>> dict, String filename,String file_hash) {
            String connectionString = "mongodb+srv://Datanode_Manager:Datanode_Manager_Passcode@storage.rt8sdgl.mongodb.net/?retryWrites=true&w=majority&appName=Storage";

            ServerApi serverApi = ServerApi.builder()
                    .version(ServerApiVersion.V1)
                    .build();

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .serverApi(serverApi)
                    .build();
                System.out.println("entring trying to connect");
            try (MongoClient mongoClient = MongoClients.create(settings)) {
                try {
                    // Send a ping to confirm a successful connection
                    MongoDatabase database = mongoClient.getDatabase("Storage");
                    MongoCollection<Document> collection = database.getCollection("Blocks to node mapping");
                    Document doc = new Document("Filename", filename);
                    doc.append("Hash", file_hash);
                    Document data = new Document();
                    try {
                        fillin_data(data, dict);
                    } catch (Exception e) {
                        // TODO: handle exception
                        System.out.println("Error in fillin_data");
                    }
                    
                    doc.append("data",data);
                    collection.insertOne(doc);
                    System.out.println("Insert into db");
                } catch (MongoException e) {
                    System.out.println("error in connecting to mongo database");
                }
            }

        }

        private void fillin_data(Document obj, Map<String,ArrayList<String>> dict)
                throws IOException, JSONException {
                    System.out.println("populating");
            for (String key : dict.keySet()) {
                String[] keys = key.split("-", 2);
                Document doc1 = new Document(keys[1], dict.get(key));
                obj.append(keys[0], doc1);
            }

        }

        private Map<String,ArrayList<String>> delivery(Map<String, byte[]> map) {
            request object = new request();
            Random random = new Random();
            System.out.println("in delivery");
            Map<String, ArrayList<String>> block_to_location = new HashMap<>();
            int count = 0;
            for (Map.Entry<String, byte[]> entry : map.entrySet()) {

                ArrayList<String> copy = DeepCopy(datanodes);
                ArrayList<String> visited = new ArrayList<String>();

                for (int i = 0; i < replication_factor; i++) {
                    int randomIndex = random.nextInt(copy.size());
                    String random_rul = copy.get(randomIndex);
                    JSONObject post_json = new JSONObject();
                    String filename = entry.getKey();
                    byte[] value = entry.getValue();
                    try {
                        post_json.put("contents", Base64.getEncoder().encodeToString(value));
                        post_json.put("filename", filename);
                        object.post(random_rul, post_json);
                        System.out.println(object.reply_in_text());
                    } catch (Exception e) {
                        System.out.println("Error in delivery");
                    }
                    visited.add(copy.get(randomIndex));
                    copy.remove(randomIndex);
                }
                block_to_location.put("Block " + count++ +"-"+entry.getKey(), visited);
            }
            return block_to_location;
        }

        private ArrayList<String> DeepCopy(ArrayList<String> list) {
            ArrayList<String> copiedList = new ArrayList<>();
            for (String item : list) {
                copiedList.add(new String(item));
            }
            return copiedList;
        }
        private void sendResponse(HttpExchange exchange, String response,int StatusCode) throws IOException {
            exchange.sendResponseHeaders(StatusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

    }
}