import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class FileSplitting {
    public List<byte[]> splitBysize(File file, int chunkSize) throws IOException {
        List<byte[]> list = new ArrayList<>();
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] buf = new byte[chunkSize];
            int bytesRead;
            while ((bytesRead = in.read(buf)) != -1) {
                byte[] chunk = new byte[bytesRead];
                System.arraycopy(buf, 0, chunk, 0, bytesRead);
                list.add(chunk);
            }
        }
        return list;
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

public class splitter {
    public static byte[] hashByteArray(byte[] array, String algorithm)  throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        return digest.digest(array);
    }
    
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    public static void main(String[] args) throws IOException {
        File file = new File("C:\\Users\\tejas\\OneDrive\\Desktop\\imp docs\\baba.vac.pdf");
        FileSplitting splitter = new FileSplitting();
        List<byte[]> chunks = splitter.splitBysize(file, 128_000);
        // You can join the chunks back if needed

        String algorithm = "SHA-256";
        for (byte[] i : chunks) {
            try {
                byte[] hash = hashByteArray(i, algorithm);
                System.out.println("Hash (hexadecimal): " + bytesToHex(hash));
            } catch (NoSuchAlgorithmException e) {
                System.err.println("Algorithm not supported: " + algorithm);
            }
        }
        // System.out.println(chunks);
        // Now, combinedBytes contains the contents of the file in memory
    }
}
