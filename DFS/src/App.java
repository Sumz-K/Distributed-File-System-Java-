import java.util.ArrayList;

class DummyClass {
    static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
public class App {
    public static void main(String[] args) throws Exception {
        ArrayList<String> datanodes = new ArrayList<String>();
        datanodes.add("https://datanode1.run-ap-south1.goorm.site/fetchfile");
        datanodes.add("https://datanode2.run-ap-south1.goorm.site/fetchfile");
        datanodes.add("https://datanode5.run-ap-south1.goorm.site/fetchfile");
        datanodes.add("https://datanode6.run-ap-south1.goorm.site/fetchfile");
        

        manage_server server = new manage_server(datanodes);
        server.startServer();
    }
}
