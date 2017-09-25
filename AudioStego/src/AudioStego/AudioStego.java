/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AudioStego;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;

/**
 *
 * @author shawn
 */
public class AudioStego {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length < 1){
            System.out.println("Usage: AudioStego <-d|-e|-g> <option args>");
	    System.out.println("-d:\tDecode <encodedFile> <privKeyFile>");
	    System.out.println("-e:\tEncode <sourceAudioFile> <outputFile> <pubKeyFile> <\"Message\">");
	    System.out.println("-g:\tGenerate Public\\Private key pair. ");
	    
            return;
        }
        if(args[0].equals("-d")){
            //decrypt
            if(args.length != 3){
                System.out.println("Invalid Argument Length");
                System.out.println("Usage: AudioStego.jar -d <encodedFile> <privKeyFile>");
                return;
            }
	    else{
		try{
		    String eFile = args[1], privFile = args[2];
		    System.out.println("Decrypting..");
		    MorseAudioStego s = new MorseAudioStego();
		    String cipher = s.unHide(eFile);
		    String kPrivate = new BufferedReader(new FileReader(privFile)).readLine();
		    String message = BasicRSA.Decrypt(cipher, kPrivate);
		    System.out.println(message);
		}catch(Exception e){
		    System.out.println(e.getMessage());
		}
	    }
        }
        else if(args[0].equals("-e")){
            //encrypt
            if(args.length != 5){
                System.out.println("Invalid Argument Length");
                System.out.println("Usage: AudioStego.jar -e <sourceAudioFile> <outputFile> <pubKeyFile> <\"Message\">");
                return;
            }
            try{
                String sFile = args[1], oFile = args[2], pubFile = args[3], message = args[4];
                String kPublic = new BufferedReader(new FileReader(pubFile)).readLine();
                System.out.println("Encrypting..");
                MorseAudioStego s = new MorseAudioStego();
                String cipher = BasicRSA.Encrypt(message, kPublic);
                s.Hide(cipher, oFile, sFile);
                System.out.println("Encryption Succeeded.");
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
        else if(args[0].equals("-g")){
            //generate key pair
            BasicRSA test = new BasicRSA();
            test.TestMGF1();
            test.GenerateKeyPair();
            try{
                File pubFile = new File("key_pub.txt"), privFile = new File("key_priv.txt");
                PrintWriter pub = new PrintWriter(pubFile), priv = new PrintWriter(privFile);
                pub.println(test.kPublic);
                pub.close();
                priv.println(test.kPrivate);
                priv.close();
            }catch(Exception e){
                System.out.println("Failed to generate key pair.");
		System.out.println(e.getMessage());
            }
        }
        else{
            System.out.println("Usage: AudioStego <-d|-e> <option args>");
        }
        
        return;
    }
    
}
