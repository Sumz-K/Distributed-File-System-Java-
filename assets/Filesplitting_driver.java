//import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


class Filesplitting{
    private final static String TEMP_DIRECTORY = "/Users/sumukhkowndinya/Desktop/Dummyfiles/Tempfiles";
    public List<File> splitBysize(File file,int chunksize){
        List<File>list=new ArrayList<>();
        try(InputStream in=Files.newInputStream(file.toPath())){
            byte[] buf=new byte[chunksize];
            int data=in.read(buf);
            //System.out.println(data);
            while(data>-1){
                File chunk=createFile(buf,data);
                list.add(chunk);
                data=in.read(buf);
            }
        }
        catch(Exception e){
            System.out.print(e);
        }
        return list;
    }
    public File join(List<File> list) throws IOException {
        File outPutFile = File.createTempFile("temp-", "-unsplit", new File(TEMP_DIRECTORY));
        FileOutputStream fos = new FileOutputStream(outPutFile);
        for (File file : list) {
            Files.copy(file.toPath(), fos);
        }
        fos.close();
        return outPutFile;
    }

    private File createFile(byte[] buffer,int len) throws IOException{
        File out_file = File.createTempFile("temp-", "-split", new File(TEMP_DIRECTORY));
        try (FileOutputStream fos = new FileOutputStream(out_file)) {
            fos.write(buffer, 0, len);
        }
        return out_file;
    }
}


public class Filesplitting_driver{
    public static void main(String[] args) {
        File file=new File("/Users/sumukhkowndinya/Desktop/Dummyfiles/GENDU_IMG.jpeg");
        Filesplitting splitter=new Filesplitting();

        // NEVER EVER MAKE THE SECOND PARAMETER 0!!!!!!!!!!!!1
        List<File> l1=splitter.splitBysize(file, 128_000);
        // File outPut1 = splitter.join(splitter.splitBysize(file, 1024_000));
        // try (InputStream in = Files.newInputStream(file.toPath()); InputStream out = Files.newInputStream(outPut1.toPath())) {
        //     System.out.println(IOUtils.contentEquals(in, out));
        // }
        System.out.println(l1);

        
    }
}

