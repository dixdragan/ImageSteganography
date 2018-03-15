package kriptodx.controllers;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import kriptodx.CryptoTools;
import static kriptodx.CryptoTools.calculateRFC2104HMAC;
import static kriptodx.CryptoTools.decrypt;
import static kriptodx.CryptoTools.hashUser;
import static kriptodx.CryptoTools.readPrivateKey;
import static kriptodx.CryptoTools.readPublicKey;
import kriptodx.UserTools;

/**
 * FXML Controller class
 *
 * @author DX
 */
public class FXMLReadController implements Initializable {
    
    private static String file;

    public static void setFile(String file) {
        FXMLReadController.file = file;
    }
    
    @FXML
    private Button buttonOK;
    @FXML
    private TextArea txtMsg;
    @FXML
    private Label txtUser;
    @FXML
    private Label txtTime;

    // Initializes the controller class.
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        String readThis = "MESSAGES/"+ hashUser(kriptodx.KriptoDX.currentUser)+"/"+file;
        txtUser.setText(file);
        try {
            String recived = decode(readThis);
            String[] part = recived.split("@");
            
            // DECRIPT
            byte[] decodedCryptedKey = Base64.getDecoder().decode(part[0]);
            PrivateKey privateKey = readPrivateKey("XCA/PR/"+kriptodx.KriptoDX.currentUser+".der");
            byte[] recovered_key = decrypt(privateKey, decodedCryptedKey);
            String decryptedKey = new String(recovered_key, "UTF8");
            
            // AES KEY
            byte[] decodedKey = Base64.getDecoder().decode(decryptedKey);
            SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
            
            // CRYPTED MSG
            byte[] in = Base64.getDecoder().decode(part[1]);
            // HMAC
            String recivedHMAC = part[2];
            
            // Symmetric Decrypt
            byte decrypted[] = CryptoTools.AESDecrypt(in, originalKey);
            String originalMsg = new String(decrypted);
            String calculatedHMAC = calculateRFC2104HMAC(originalMsg, decryptedKey);
            if (calculatedHMAC.equals(recivedHMAC)) {
                System.out.println("Message authentication successful.");
            }else{
                UserTools.displayErrorAletr("Message authentication not successful!", "");
            }
            
            
            String[] original = originalMsg.split("#");
            txtMsg.setText(original[0]);
            txtUser.setText(original[1]);
            txtTime.setText(original[2]);
            
            // Signed check
            Signature sig = Signature.getInstance("SHA1withRSA");
            PublicKey publicKey = readPublicKey("XCA/PU/"+original[1]+".der");
            sig.initVerify(publicKey);
            sig.update(recivedHMAC.getBytes());
            
            String recivedSIGN = part[3];
            byte[] decodedSign = Base64.getDecoder().decode(recivedSIGN);
            boolean verifies = sig.verify(decodedSign);
            if (verifies) {
                System.out.println("Digital signature successful!");
            }else{
                UserTools.displayErrorAletr("Digital signature failed!", "Cannot verify the digital signature.");
            }
            removeImage();
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | IllegalArgumentException | BadPaddingException ex) {
            UserTools.displayErrorAletr("Image is corrupt!", "Someone edited the image.");
            removeImage();
        } catch (SignatureException | InvalidKeySpecException ex) {
            System.out.println("Signature or Key failed.");
            //Logger.getLogger(FXMLReadController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    

    @FXML
    private void actionOK(ActionEvent event) {
        
        ((Stage)(buttonOK.getScene().getWindow())).close();
    }
    public void removeImage() {
        String removeThis = "MESSAGES/" + hashUser(kriptodx.KriptoDX.currentUser) + "/" + file;
        if ((new File(removeThis)).delete()) {
            UserTools.updateFolderStatus(kriptodx.KriptoDX.currentUser);
        } else {
            System.out.println("Image delete failed, close apps that use the image.");
        }
        FXMLReadController.file = "";
    }
    
    public void stop() {
        System.out.println("Stage is closing");
        actionOK(new ActionEvent());
    }
    
    public String decode(String imagePath) throws IOException {
        byte[] decode;
        //user space is necessary for decrypting
        BufferedImage image = user_space(getImage(imagePath));
        decode = decode_text(get_byte_data(image));
        return (new String(decode));
    }
    private BufferedImage user_space(BufferedImage image) {
        //create new_img with the attributes of image
        BufferedImage new_img = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = new_img.createGraphics();
        graphics.drawRenderedImage(image, null);
        graphics.dispose(); //release all allocated memory for this image
        return new_img;
    }
    private byte[] get_byte_data(BufferedImage image) {
        WritableRaster raster = image.getRaster();
        DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
        return buffer.getData();
    }
    private BufferedImage getImage(String f) throws IOException{
        BufferedImage image = null;
        File file = new File(f);
        image = ImageIO.read(file);
        return image;
    }
    private byte[] decode_text(byte[] image) {
        int length = 0;
        int offset = 32;
        //loop through 32 bytes of data to determine text length
        for (int i = 0; i < 32; ++i) //i=24 will also work, as only the 4th byte contains real data
        {
            length = (length << 1) | (image[i] & 1);
        }
        byte[] result = new byte[length];
        //loop through each byte of text
        for (int b = 0; b < result.length; ++b) {
            //loop through each bit within a byte of text
            for (int i = 0; i < 8; ++i, ++offset) {
                //assign bit: [(new byte value) << 1] OR [(text byte) AND 1]
                result[b] = (byte) ((result[b] << 1) | (image[offset] & 1));
            }
        }
        return result;
    }

    
}
