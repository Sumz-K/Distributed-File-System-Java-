import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

import javafx.event.ActionEvent;

public class MainSceneController {

    public static void clearScreen()
    {  
        System.out.print("\033[H\033[2J");  
        System.out.flush();  
    }

    @FXML
    private Button BTNSwitchScene;
    
    @FXML
    private TextField tfTitle;
    int c = 0;

    @FXML
    void btnOKClicked(ActionEvent event) {
        //obtain main window
        Stage mainWindow = (Stage) tfTitle.getScene().getWindow();
        //read text from text field
        String title = tfTitle.getText();
        clearScreen();
        System.out.println("Title has been changed "+(++c)+" time(s).");
        //modify title
        mainWindow.setTitle(title);
    }

    @FXML
    void switchScene(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("AlternateScene.fxml"));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e)
        {
            
        }
    }

}