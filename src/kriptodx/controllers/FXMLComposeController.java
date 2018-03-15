package kriptodx.controllers;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.imageio.ImageIO;
import kriptodx.CryptoTools;
import static kriptodx.CryptoTools.*;
import kriptodx.UserTools;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class FXMLComposeController implements Initializable {

    private File pickedImage;
            
    @FXML
    private TextArea txtMsg;
    @FXML
    private ComboBox<String> cbUsers;
    @FXML
    private Button btnPick;
    @FXML
    private Label tbImage;
    @FXML
    private Button buttonSEND;
    @FXML
    private Label tbInfo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbUsers.setItems(UserTools.getUsers(kriptodx.KriptoDX.currentUser));
        
        
        btnPick.setOnAction((event) -> {
            FileChooser fileChooser = new FileChooser();
            //Set extension filter
            FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.gif", "*.bmp");
            fileChooser.getExtensionFilters().addAll(extFilterJPG);
            //Show open file dialog
            pickedImage = fileChooser.showOpenDialog(null);
            if (pickedImage != null) {
                tbImage.setText(pickedImage.getName());
            }else{
                tbImage.setText("");
            }
        });
        
    
    
    
    }    

    @FXML
    private void sendMessage(ActionEvent event) {
        tbInfo.setText("");
        
        // Check cirtificate of cbUsers.getSelectionModel().getSelectedItem().toString()
        
        if(cbUsers.getSelectionModel().getSelectedItem() == null){
            tbInfo.setText("No user selected!");
            return;
        }
        if(txtMsg.getText().isEmpty()){
            tbInfo.setText("Message is empty!");
            return;
        }
        if(pickedImage == null){
            tbInfo.setText("No image selected!");
            return;
        }
         
        
        
        DateFormat dtFormat = new SimpleDateFormat("HH:mm  dd.MM.yyyy.");
        Date dateTime = new Date();
        String out = txtMsg.getText() + "#" + kriptodx.KriptoDX.currentUser + "#" + dtFormat.format(dateTime);
        
       // System.out.println("IN: " + pickedImage.getName() + " ("+pickedImage.getPath()+")");
       // System.out.println("SENDING: " + out);

        try {
            // Generate Symmetric Key
            SecretKey symmetricKey = AESKeyGenerator();
            // Crypt the image simetric
            byte encrypted[] = AESEncrypt(out.getBytes(), symmetricKey);
            String encodedKey = Base64.getEncoder().encodeToString(symmetricKey.getEncoded());
            String encryptedMsg = Base64.getEncoder().encodeToString(encrypted);
            // Crypt the key asimetric
            Security.addProvider(new BouncyCastleProvider());
            PublicKey publicKey = readPublicKey("XCA/PU/"+cbUsers.getSelectionModel().getSelectedItem()+".der");
            byte[] ek = encodedKey.getBytes("UTF8");
            byte[] secret = encrypt(publicKey, ek);
            String encryptedKey = Base64.getEncoder().encodeToString(secret);
            
            String hmac = calculateRFC2104HMAC(out, encodedKey);
            PrivateKey privateKey = readPrivateKey("XCA/PR/"+kriptodx.KriptoDX.currentUser+".der");
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initSign(privateKey);
            sig.update(hmac.getBytes());
            byte[] realSig = sig.sign();
            String signedHmac = Base64.getEncoder().encodeToString(realSig);
            // THIS IS WRITTEN IN STONE
            String send = encryptedKey + "@" + encryptedMsg + "@" + hmac + "@" + signedHmac;
            
            X509Certificate certificate = genCRT("XCA/CRT/" + cbUsers.getSelectionModel().getSelectedItem()+".crt");
            certificate.verify(kriptodx.KriptoDX.caCrt.getPublicKey()); // Throws SignatureException
            certificate.checkValidity(dateTime); // Throws CertificateException
            encode(pickedImage, send, cbUsers.getSelectionModel().getSelectedItem());
            
            UserTools.updateFolderStatus(cbUsers.getSelectionModel().getSelectedItem());
            ((Stage)(buttonSEND.getScene().getWindow())).close();
            
        } catch (IOException ex) {
            UserTools.displayErrorAletr("The certificate of the user you picked is missing!", "Please select android user.");
        } catch (CertificateException ex) {
            UserTools.displayErrorAletr("The certificate of the user you picked has expired!", "Please select android user.");
        } catch (SignatureException ex) {
            UserTools.displayErrorAletr("The certificate of the user you picked is from another CA!", "Please select android user.");
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException ex) {
            System.out.println("Error found in ComposeController X905, please check.");
            //Logger.getLogger(FXMLComposeController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException ex) {
            System.out.println("Error found in ComposeController AES, please check.");
            //Logger.getLogger(FXMLComposeController.class.getName()).log(Level.SEVERE, null, ex);
        }catch(IllegalArgumentException | ArrayIndexOutOfBoundsException ex){
            UserTools.displayErrorAletr("The image is to small!", "Please select android image.");
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(FXMLComposeController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    public boolean encode(File original, String message, String reciver) throws IOException, IllegalArgumentException {
        BufferedImage image_orig = ImageIO.read(original);

        //New image
        BufferedImage new_img = new BufferedImage(image_orig.getWidth(), image_orig.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = new_img.createGraphics();
        graphics.drawRenderedImage(image_orig, null);
        graphics.dispose(); //release all allocated memory for this image

        new_img = add_text(new_img, message);

        // Check if that file exists (add x if necessary)
        String endFile = "MESSAGES/" + CryptoTools.hashUser(reciver) + "/" + pickedImage.getName();

        File f = new File(endFile);
        while (f.exists()) {
            String[] path = endFile.split("\\.(?=[^\\.]+$)");
            endFile = path[0] + "x." + path[1]; // Check array out of index
            f = new File(endFile);
            if (!f.exists()) {
                f.delete();
                break;
            }
        }

        return (setImage(new_img, new File(endFile), "png"));
    }
    private boolean setImage(BufferedImage image, File file, String ext) {
        try {
            file.delete(); //delete resources used by the File
            ImageIO.write(image, ext, file);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private BufferedImage add_text(BufferedImage image, String text) throws IllegalArgumentException{
        //convert all items to byte arrays: image, message, message length
        byte img[] = get_byte_data(image);
        byte msg[] = text.getBytes();
        byte len[] = bit_conversion(msg.length);
        encode_text(img, len, 0); //0 first positiong
        encode_text(img, msg, 32); //4 bytes of space for length: 4bytes*8bit = 32 bits
        return image;
    }
    private byte[] get_byte_data(BufferedImage image) {
        WritableRaster raster = image.getRaster();
        DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
        return buffer.getData();
    }

    private byte[] bit_conversion(int i) {
        //originally integers (ints) cast into bytes
        //byte byte7 = (byte)((i & 0xFF00000000000000L) >>> 56);
        //byte byte6 = (byte)((i & 0x00FF000000000000L) >>> 48);
        //byte byte5 = (byte)((i & 0x0000FF0000000000L) >>> 40);
        //byte byte4 = (byte)((i & 0x000000FF00000000L) >>> 32);

        //only using 4 bytes
        byte byte3 = (byte) ((i & 0xFF000000) >>> 24); //0
        byte byte2 = (byte) ((i & 0x00FF0000) >>> 16); //0
        byte byte1 = (byte) ((i & 0x0000FF00) >>> 8); //0
        byte byte0 = (byte) ((i & 0x000000FF));
        //{0,0,0,byte0} is equivalent, since all shifts >=8 will be 0
        return (new byte[]{byte3, byte2, byte1, byte0});
    }
    private byte[] encode_text(byte[] image, byte[] addition, int offset) throws IllegalArgumentException {
        //check that the data + offset will fit in the image
        if (addition.length + offset > image.length) {
            throw new IllegalArgumentException("File not long enough!");
        }
        //loop through each addition byte
        for (int i = 0; i < addition.length; ++i) {
            //loop through the 8 bits of each byte
            int add = addition[i];
            for (int bit = 7; bit >= 0; --bit, ++offset) //ensure the new offset value carries on through both loops
            {
                //assign an integer to b, shifted by bit spaces AND 1
                //a single bit of the current byte
                int b = (add >>> bit) & 1;
                //assign the bit by taking: [(previous byte value) AND 0xfe] OR bit to add
                //changes the last bit of the byte in the image to be the bit of addition
                image[offset] = (byte) ((image[offset] & 0xFE) | b);
            }
        }
        return image;
    }
}
   
    
    

