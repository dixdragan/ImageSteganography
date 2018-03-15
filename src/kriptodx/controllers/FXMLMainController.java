package kriptodx.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import kriptodx.CryptoTools;
import static kriptodx.CryptoTools.genCRT;
import kriptodx.UserTools;

/**
 * FXML Controller class
 *
 * @author DX
 */
public class FXMLMainController implements Initializable {
    
    private static final String caPath = "XCA/CRT/RootCA.crt";
    private static final String myCrtPath = "XCA/CRT/"+kriptodx.KriptoDX.currentUser+".crt";
    private static File msgsFolder;
    private static File[] listOfMsgs;

    @FXML
    private Label labelWelcome;

    @FXML
    private GridPane gridMsg54;
    @FXML
    private AnchorPane mainPane;
    @FXML
    private HBox boxMessages;

    // Initializes the controller class.
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        try {
            kriptodx.KriptoDX.caCrt = genCRT(caPath);
            kriptodx.KriptoDX.myCrt = genCRT(myCrtPath);
            
            kriptodx.KriptoDX.myCrt.verify(kriptodx.KriptoDX.caCrt.getPublicKey()); // Throws SignatureException
            kriptodx.KriptoDX.myCrt.checkValidity(new Date()); // Throws CertificateException
        } catch (IOException | CertificateException ex) {
            UserTools.displayErrorAletr("The certificate is missing!", "Please contact the administrator.");
            //DISABLE EVERYTHING
            mainPane.setDisable(true);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException ex) {
            System.out.println("Error found in MainController.initialize, please check.");
            //Logger.getLogger(FXMLMainController.class.getName()).log(Level.SEVERE, null, ex);
            mainPane.setDisable(true);
        } catch (SignatureException ex) {
            UserTools.displayErrorAletr("Your certificate is from another CA!", "Please contact the administrator.");
            mainPane.setDisable(true);
        }
        
        
        msgsFolder = new File("MESSAGES/"+ CryptoTools.hashUser(kriptodx.KriptoDX.currentUser));
        if(msgsFolder.exists()){
            initLoad();
            labelWelcome.setText("Hello " + kriptodx.KriptoDX.currentUser + ", you have [ " + listOfMsgs.length + " ] messages.");
        }else{
            msgsFolder.mkdir();
            labelWelcome.setText("Welcome " + kriptodx.KriptoDX.currentUser + ", your inbox has just been created.");
        }
       
    }

    
    public void initLoad(){
        
            listOfMsgs = msgsFolder.listFiles();
            
            String state = CryptoTools.hashUser(Arrays.toString(listOfMsgs));
            int code = UserTools.checkFolderStatus(state,listOfMsgs.length);
            if (code != 0) {
                UserTools.displayErrorAletr("Message folder accessed!", "Someone removed " + code + " images.");
            }
                    
                    
            
            for (int i = 0; i < listOfMsgs.length; i++) {
                if (listOfMsgs[i].isFile()) {
                    
//                    System.out.println("File: " + listOfMsgs[i].getName());
                    
                    Button btn = new Button();
                    btn.setText(listOfMsgs[i].getName());
                    btn.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            String txt = ((Button) event.getSource()).getText();
                            FXMLReadController.setFile(txt);
                            gridMsg54.getChildren().remove(event.getSource());
                            labelWelcome.setText("");
                            try {
                                Parent boot = (Parent) (new FXMLLoader(getClass().getResource("/kriptodx/fxmls/FXMLRead.fxml"))).load();
                                Stage readStage = new Stage();
                                readStage.setTitle("READ MESSAGE");
                                readStage.getIcons().add(new Image("kriptodx/assets/cryptologo.png"));
                                readStage.setScene(new Scene(boot));
                                readStage.show();
                            } catch (Exception e) {
                                System.out.println("There is some error.");
                                e.printStackTrace();
                            }
                        }
                    });
                    gridMsg54.add(btn, i % 5, i / 5);
                }
            }
    }
    

    @FXML
    private void newMsgAction(ActionEvent event) {
        try{
            Parent boot = (Parent) (new FXMLLoader(getClass().getResource("/kriptodx/fxmls/FXMLCompose.fxml"))).load();
            Stage readStage = new Stage();
            readStage.setTitle("NEW MESSAGE");
            readStage.getIcons().add(new Image("kriptodx/assets/cryptologo.png"));
            readStage.setScene(new Scene(boot));
            readStage.show();
        }catch(Exception e){
            System.out.println("There is some error.");
            e.printStackTrace();
        }
        
    }
    
    
    
    
}
