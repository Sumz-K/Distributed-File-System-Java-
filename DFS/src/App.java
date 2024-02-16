import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class App {
    public static void main(String[] args) throws IOException, JSONException {
        request object = new request();
        String url = "https://dummy.restapiexample.com/api/v1/create";
        JSONObject postis = new JSONObject();
        postis.put("key", "Value");
        object.post(url,postis);
        System.out.println(object.reply_in_json());
    }
}
