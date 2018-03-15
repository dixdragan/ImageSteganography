package kriptodx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

/**
 *
 * @author DX
 */
public class UserTools {
    
    public static boolean exist(String username){
        boolean ret = false;
        try {
            BufferedReader in = new BufferedReader(new FileReader("USER/user.txt"));
            String line;
            String[] elements;
            while((line=in.readLine())!=null){
                elements = line.split("#");
                if(username.equals(elements[0])){
                    ret = true;
                }
            }
            in.close();
        } catch (FileNotFoundException ex) {
            System.out.println("users.txt not found!");
        } catch (IOException ex) {
            System.out.println("users.txt failed to read!");
        } 
        return ret;
    }
    
    public static boolean check(String username, String password){
        boolean ret = false;
        String hash = CryptoTools.hashPassword(password);
        try {
            BufferedReader in = new BufferedReader(new FileReader("USER/user.txt"));
            String line;
            String[] elements;
            while((line=in.readLine())!=null){
                elements = line.split("#");
                if(username.equals(elements[0]) && hash.equals(elements[1])){
                    ret = true;
                }
            }
            in.close();
        } catch (FileNotFoundException ex) {
            System.out.println("users.txt not found!");
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("illegal entry for this user in users.txt!");
        } 
        catch (IOException ex) {
            System.out.println("users.txt failed to read!");
        } 
        return ret;
    }
    
    public static ObservableList<String> getUsers(String user){
        ObservableList<String> users = FXCollections.observableArrayList();
        try {
            BufferedReader in = new BufferedReader(new FileReader("USER/user.txt"));
            String line;
            String[] elements;
            while((line=in.readLine())!=null){
                elements = line.split("#");
                users.add(elements[0]);
            }
            in.close();
        } catch (FileNotFoundException ex) {
            System.out.println("users.txt not found!");
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("illegal entry for this user in users.txt!");
        } 
        catch (IOException ex) {
            System.out.println("users.txt failed to read!");
        } 
        users.remove(user);
        return users;
    }
    public static void displayInfoAletr(String header, String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(content);
        alert.setHeaderText(header);
        alert.setTitle("Info");
        alert.show();   
    }
    public static void displayErrorAletr(String header, String content){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(content);
        alert.setHeaderText(header);
        alert.setTitle("Error");
        alert.showAndWait();   
    }
    
    public static void updateFolderStatus(String username){
        String msgsFolder = "MESSAGES/"+ CryptoTools.hashUser(username);
        File msgFolder = new File(msgsFolder);
        File[] msgs;
        if (msgFolder.exists()) {
            msgs = msgFolder.listFiles();
            
            String write = CryptoTools.hashUser(Arrays.toString(msgs))+"$"+msgs.length;
            
            FileWriter out = null;
            try {
                out = new FileWriter("USER/" + CryptoTools.hashUser(username));
                out.write(write);
                out.close();
            } catch (IOException ex) {
                System.out.println("updateFolderStatus Failed!");
                //Logger.getLogger(UserTools.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    
    }
    public static int checkFolderStatus(String hash, int count){
        int ret = 509;
        try {
            BufferedReader in = new BufferedReader(new FileReader("USER/"+CryptoTools.hashUser(kriptodx.KriptoDX.currentUser)));
            String line;
            String[] elements;
            while((line=in.readLine())!=null){
                //System.out.println(line);
                elements = line.split("\\$");
                // System.out.println(elements[0]);
                int has = Integer.parseInt(elements[1]);
                if(hash.equals(elements[0]) && has==count){
                    ret = 0;
                }else{
                    ret = has - count;
                }
            }
            in.close();
        } catch (FileNotFoundException ex) {
            System.out.println("user msg folder not found!");
        } catch (IOException ex) {
            System.out.println("user msg folder failed to read!");
        } 
        return ret;
    }
}
