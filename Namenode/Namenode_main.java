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
import com.mongodb.client.FindIterable;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.net.URI;
import java.util.*;

import javax.swing.SortOrder;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import org.json.JSONException;
import org.json.JSONObject;

interface DatabaseConnection {
    HashMap<String,ArrayList<String>> connect();
    MongoClientSettings connect_todb();
    void fetch_blockdetails(MongoClientSettings settings,String filename);
}

class MongoDBConnection implements DatabaseConnection {
    private String connectionString;

    public MongoDBConnection(String connectionString) {
        this.connectionString = connectionString;
    }                                                                                         


    public MongoClientSettings connect_todb(){
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();

        return settings;
    }

    public void fetch_blockdetails(MongoClientSettings settings,String filename){
        try(MongoClient mongoclient=MongoClients.create(settings)){
            MongoDatabase database=mongoclient.getDatabase("Storage");
            MongoCollection<Document> collection=database.getCollection("Blocks to node mapping");


            //System.out.print("count "+collection.countDocuments());

            FindIterable<Document> iterDoc = collection.find();

            HashMap<String,List<String>> details=new HashMap<>();
            System.out.print(filename+"\n");
            for(Document doc:iterDoc){
                String f=doc.get("Filename").toString();
                //System.out.print(f+" ");

                if(f.trim().equals(filename.trim())){
                    ArrayList<String> props=new ArrayList<>();
                    //System.out.print(doc.get("data"));
                    Object dataObject = doc.get("data");
                    Document data_doc=new Document();
                    if (dataObject instanceof Document) {
                        data_doc = (Document) dataObject;
                    }

                    for (Map.Entry<String, Object> entry : data_doc.entrySet()) {
                        String blockId = entry.getKey();
                        Document blockDocument = (Document) entry.getValue();
        
                        List<String> urlList = new ArrayList<>();
                        for (Map.Entry<String, Object> urlEntry : blockDocument.entrySet()) {
                            String hash=urlEntry.getKey();
                            List<String> urls = (List<String>) urlEntry.getValue();
                            urlList.add(hash);
                            urlList.addAll(urls);
                        }
            
                        System.out.print(blockId+"\n");

                        for(String u:urlList){
                            System.out.print(u+"\n");
                        }

                        details.put(blockId,urlList);
                        
                    }
            
                }

            }



        }
        catch(Exception e){
            System.out.print(e.getStackTrace());
        }
    }


    @Override
    public HashMap<String,ArrayList<String>> connect() {
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings=connect_todb();

        try (MongoClient mongoClient = MongoClients.create(settings)) {
            MongoDatabase database = mongoClient.getDatabase("Storage");
            MongoCollection<Document> collection = database.getCollection("Datanode info");
            System.out.println("Count: " + collection.countDocuments());
            FindIterable<Document> iterDoc = collection.find();
            HashMap<String,ArrayList<String>> details=new HashMap<>();

            for (Document doc : iterDoc) {

                String name=doc.get("Name").toString();
                String endpoint=doc.get("Endpoint").toString();
                String available_storage=doc.get("AvailableStorage").toString();

                ArrayList<String>props=new ArrayList<>();
                props.add(endpoint);
                props.add(available_storage);

                details.put(name, props);

            }
            

            
            return details;
     
        } catch (MongoException e) {
            System.out.println("Error in connecting to MongoDB database");
            throw e;
        }
        
    }
}


class DatabaseConnectionFactory {
    public static DatabaseConnection createConnection(String connectionString) {

        return new MongoDBConnection(connectionString);
    }
}

class Namenode_server {
    private DatabaseConnection databaseConnection;

    public Namenode_server(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 6000), 0);
        server.createContext("/receive", new ConnectionHandler());
        server.start();
        System.out.println("Server running on port 6000");
    }

    class ConnectionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                handlePostRequest(exchange);
            } else if ("GET".equals(exchange.getRequestMethod())) {
                handleGetRequest(exchange);
            }
        }

        private void handleGetRequest(HttpExchange exchange) throws IOException{
            String url=exchange.getRequestURI().toString();
            //System.out.print(url);
            String filename="";
            try {
                URI uri = new URI(url);
                String query = uri.getQuery();
                
                if (query != null) {
                    String[] params = query.split("&");
                    for (String param : params) {
                        String[] keyValue = param.split("=");
                        if (keyValue.length == 2 && keyValue[0].equals("filename")) {
                            filename = keyValue[1];
                            break; 
                        }
                    }
                } else {
                    System.out.println("No query parameters found.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.print(filename);

            MongoClientSettings settings=databaseConnection.connect_todb();


            databaseConnection.fetch_blockdetails(settings,filename);

            sendResponse(exchange, 200);
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                requestBody.append(line).append("\n");
            }
            br.close();
            String body = requestBody.toString();
            System.out.println("Received Post Body");
            System.out.println(body);


            HashMap<String,ArrayList<String>> details=databaseConnection.connect();
            ArrayList<String> useable_datanodes=new ArrayList<>();
            for (Map.Entry<String, ArrayList<String>> entry : details.entrySet()) {
                String key = entry.getKey();
                ArrayList<String> values = entry.getValue();

                double space=Double.parseDouble(values.get(1));
                if(space>=128){
                    useable_datanodes.add(values.get(0));
                }
                
            }
            for(String s:useable_datanodes){
                System.out.println(s);
            }

            sendDatanodeList(useable_datanodes,body);


            sendResponse(exchange,200);
        }

        private void sendDatanodeList(ArrayList<String> list,String body){
            String endpoint="";
            request req=new request();
            JSONObject json=new JSONObject();
            String[] lines = body.split(":", 2);
            String file_name=lines[0];
            String file_content=lines[1];
            try{
            json.put("filename", file_name);
            json.put("contents", file_content);
            json.put("Endpoints", list);

            req.post(endpoint, json);
            }
            catch(JSONException e){
                System.out.println(e.getStackTrace());
            }
        }

        private void sendResponse(HttpExchange exchange,int StatusCode) throws IOException {
            String response="Successful";
            exchange.sendResponseHeaders(StatusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}

public class Namenode_main {
    public static void main(String[] args) throws IOException {
        String mongoConnectionString = "mongodb+srv://Datanode_Manager:Datanode_Manager_Passcode@storage.rt8sdgl.mongodb.net/?retryWrites=true&w=majority&appName=Storage";
        DatabaseConnection databaseConnection = DatabaseConnectionFactory.createConnection(mongoConnectionString);
        Namenode_server server = new Namenode_server(databaseConnection);
        server.startServer();
    }
}



// client->namenode->datanodemanager
