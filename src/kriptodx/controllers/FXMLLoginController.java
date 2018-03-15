package kriptodx.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import kriptodx.CryptoTools;
import kriptodx.UserTools;

/**
 *
 * @author DX
 */
public class FXMLLoginController implements Initializable {
    
    @FXML
    private Button buttonLogin;
    @FXML
    private Label labelErr;
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        password.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.ENTER)) {
                    loginAction(new ActionEvent()); 
                }
            }
        });
    }

    @FXML
    private void loginAction(ActionEvent event) {
        labelErr.setText("");
        
        String user = username.getText();
        String pass = password.getText();
        /*
        // First line checks
        if(user.isEmpty()){
            labelErr.setText("Username is empty");
            return;
        }
        if (pass.isEmpty()) {
            labelErr.setText("Password is empty");
            return;
        }
        if(user.length() < 3){
            labelErr.setText("Username is too short");
            return;
        }if(pass.length() < 3){
            labelErr.setText("Password is too short");
            return;
        }
        */
        //System.out.println("HASH NEW USER: " + CryptoTools.hashPassword(pass));
        
        // Launch the main window
        if (UserTools.check(user,pass)) {
            kriptodx.KriptoDX.currentUser = user;
            try{
                // Show main window
                Parent toot = (Parent) (new FXMLLoader(getClass().getResource("/kriptodx/fxmls/FXMLMain.fxml"))).load();
                Stage mainStage = new Stage();
                mainStage.setTitle("KRIPTO 2K18");
                mainStage.setMinWidth(590);
                mainStage.setMinHeight(400);
                mainStage.getIcons().add(new Image("kriptodx/assets/cryptologo.png"));
                mainStage.setScene(new Scene(toot));
                mainStage.show();
            
                // Hide this window
                ((Stage)(buttonLogin.getScene().getWindow())).close();
            
                // If you want to implement Logout
                //((Node)(event.getSource())).getScene().getWindow().hide();
            }catch(Exception ex){
                System.out.println("Error while opening the main window!");
                ex.printStackTrace();
            }
        }else{
            username.setText("");
            password.setText("");
            labelErr.setText("Wrong username or password!");
        }
    }
    
    
    
}
