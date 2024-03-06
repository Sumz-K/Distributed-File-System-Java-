import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class App {
    public static void main(String[] args) throws IOException, JSONException {
        request object = new request();
        String url = "http://192.168.0.112:8081/uploadfile";
        JSONObject postis = new JSONObject();
        postis.put("key", "Value");
        object.post(url, "temp-9146621541785134506-split");
        System.out.println(object.reply_in_text());
    }
}
