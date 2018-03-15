package kriptodx;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import static kriptodx.CryptoTools.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class KriptoDX extends Application {
    
    public static String currentUser;
    public static X509Certificate caCrt;
    public static X509Certificate myCrt;
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/kriptodx/fxmls/FXMLLogin.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("KRIPTO 2K18");
        stage.getIcons().add(new Image("kriptodx/assets/cryptologo.png"));
        stage.setMinWidth(250);
        stage.setMinHeight(380);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        launch(args);
    }
    
}
