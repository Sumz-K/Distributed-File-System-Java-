import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

//For PDF
// import org.apache.pdfbox.pdmodel.PDDocument;
// import org.apache.pdfbox.rendering.PDFRenderer;

public class AlternateController {

    Stage stage;
    File selectedFile;

    @FXML
    private TextArea pasteTxt;
    @FXML
    private ImageView fileViewer;
    @FXML
    private Label fileNameLBL;
    @FXML
    private Button BTNSwitchScene;
    @FXML
    private Button BTNUploadfFile;

    ExtensionFilter ex1 = new ExtensionFilter("Text files", "*.txt");
    ExtensionFilter ex2 = new ExtensionFilter("PDF files", "*.pdf");

    void readUploadedFile(File filename) throws FileNotFoundException
    {
        Scanner sc = new Scanner(filename);
        while (sc.hasNext())
            pasteTxt.setText(pasteTxt.getText() +"\n"+ sc.next());            
            //pasteTxt.setText(sc.next());
        sc.close();
    }

    @SuppressWarnings("null")
    @FXML
    void openFiles(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(ex2,ex1);
        selectedFile = fileChooser.showOpenDialog(stage);
        if(selectedFile != null)
            System.out.println("Received File");
            System.out.println(selectedFile.getName());
        fileNameLBL.setText(selectedFile.getName());
    }

    @FXML
    void showFile(MouseEvent event) throws FileNotFoundException {
        if(fileNameLBL.getText() != "")
        {
            System.out.println("File clicked");
            System.out.println(selectedFile.getName());
            System.out.println(selectedFile.canRead());
            readUploadedFile(selectedFile);
            //PDDocument document = PDDocument.load(selectedFile)
        }
        else
            System.out.println("No file");
    }

    @FXML
    void switchScene(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("MainScene.fxml"));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e)
        {
            
        }
    }

}
