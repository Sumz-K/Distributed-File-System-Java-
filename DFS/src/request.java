import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Iterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class request {
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
            httpPost.setEntity(new StringEntity(data.toString()));
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
