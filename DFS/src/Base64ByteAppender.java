import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

public class Base64ByteAppender {
    public static void main(String[] args) {
        // Base64-encoded strings
        request Request = new request();
        Request.get(
                "https://datanode1.run-ap-south1.goorm.site/fetchfile?filename=99371fc5763233358fc28579c43eaa251bb2170b5a4ad330028241314f4475e8");
        
        String base64String1 = (Request.reply_in_text()); // "Hello World!"
        Request.get("https://datanode1.run-ap-south1.goorm.site/fetchfile?filename=ddc4aef23136c63924e514e7f2f07d6afa1421bd42a68d2cc77d7b4ecd6d8cee");
        String base64String2 = (Request.reply_in_text()); // "Base64 Rocks"

        // Convert base64 strings to bytes
        byte[] bytes1 = Base64.getDecoder().decode(base64String1);
        byte[] bytes2 = Base64.getDecoder().decode(base64String2);

        // Append the byte arrays
        byte[] combinedBytes = new byte[bytes1.length + bytes2.length];
        System.arraycopy(bytes1, 0, combinedBytes, 0, bytes1.length);
        System.arraycopy(bytes2, 0, combinedBytes, bytes1.length, bytes2.length);

        // Write the combined bytes to a file
        String outputFilePath = "output.bin";
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath)) {
            fileOutputStream.write(combinedBytes);
            System.out.println("Combined bytes written to " + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}