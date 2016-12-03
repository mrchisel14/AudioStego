/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AudioStego;
import java.util.*;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.io.*;
import java.io.*;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayOutputStream;
import javax.sound.sampled.*;
import java.lang.Thread;
import java.nio.ByteBuffer;
/**
 *
 * @author shawn
 */
public class MorseAudioStego {
    enum Note {
        SPACE, DOT, DASH;
        public static final int SAMPLE_RATE = 44100; // ~44KHz
        public static final double PERIOD = SAMPLE_RATE / 700d;
        public static final float FULL_AMP = 50f;
        private byte[] sin = new byte[(int)PERIOD];
        
        Note() {
            float amp = FULL_AMP;
            int n = this.ordinal();
            if(n == 1){
                amp /= 2f;
            }
            if(n == 0){
                amp = 0;
            }
            
            if (n > 0) {
                for (int i = 0; i < sin.length; ++i) {
                    double period = Note.PERIOD;
                    double angle = 2.0 * Math.PI * i / period;
                    sin[i] = (byte)(Math.sin(angle) * amp);
                }
            }
        }
        public byte[] data() {
            return sin;
        }
    }
    private static HashMap<String, String> codes = new HashMap<String, String>();
    private static HashMap<String, String> codesToChar = new HashMap<String, String>();
    private static String separator = " ";
    static{
        codes.put("A", ".-");
        codes.put("B", "-...");
        codes.put("C", "-.-.");
        codes.put("D", "-..");
        codes.put("E", ".");
        codes.put("F", "..-.");
        codes.put("G", "--.");
        codes.put("H", "....");
        codes.put("I", "..");
        codes.put("J", ".---");
        codes.put("K", "-.-");
        codes.put("L", ".-..");
        codes.put("M", "--");
        codes.put("N", "-.");
        codes.put("O", "---");
        codes.put("P", ".--.");
        codes.put("Q", "--.-");
        codes.put("R", ".-.");
        codes.put("S", "...");
        codes.put("T", "-");
        codes.put("U", "..-");
        codes.put("V", "...-");
        codes.put("W", ".--");
        codes.put("X", "-..-");
        codes.put("Y", "-.--");
        codes.put("Z", "--..");
        
        // numbers
        codes.put("1", ".----");
        codes.put("2", "..---");
        codes.put("3", "...--");
        codes.put("4", "....-");
        codes.put("5", ".....");
        codes.put("6", "-....");
        codes.put("7", "--...");
        codes.put("8", "---..");
        codes.put("9", "----.");
        codes.put("0", "-----");
        
        // special codes
        codes.put(".", ".-.-.-");
        codes.put(",", "--..--");
        codes.put("?", "..--..");
        codes.put("\"", ".----.");
        codes.put("!", "-.-.--");
        codes.put("/", "-..-.");
        codes.put("(", "-.--.-");
        codes.put(")", "-.--.-");
        codes.put("&", ".-...");
        codes.put(":", "---...");
        codes.put(";", "-.-.-.");
        codes.put("=", "-...-");
        codes.put("+", "-...-");
        codes.put("-", "-....-");
        codes.put("_", "..--.-");
        codes.put("\"", ".-..-.");
        codes.put("$", "...-.. -");
        codes.put("@", ".--.-.");
        
        // word separators
        codes.put(" ", separator);
        codes.put("\n", separator);
        codes.put("\t", separator);
        // CR is ignored
        codes.put("\r", "");
    }    
    MorseAudioStego(){
        //initializae codesToChar
        for (Map.Entry<String, String> entry : codes.entrySet())
        {
            if(entry.getKey() != "\n" && entry.getKey() != "\t" && entry.getKey() != "\r"){
                codesToChar.put(entry.getValue(), entry.getKey());
            }
        }
    }
    private String convertToMorse(String cipher){
        String result = "";
        for(int i = 0; i < cipher.length(); ++i){
            result += (String)codes.get("" + cipher.charAt(i));
            if(i != cipher.length() - 1)
                result += " ";
        }
        return result;
    }
    private String morseToString(String morse){
        String result = "", morseChar = "", c = "";
        StringTokenizer tokens = new StringTokenizer(morse);
        while (tokens.hasMoreTokens()) {
            result += codesToChar.get(tokens.nextToken());
        }
        return result;
    }
    private String convertBytesToMorse(byte[] a, int offset){
        int max = 0;
        String result = "";
        byte [] array = Arrays.copyOfRange(a, offset, a.length);
        for(int i = 1; i <= array.length; ++i){
            if((i % Note.PERIOD) == 0){
                if(max > (Note.FULL_AMP - 10)){
                    //dash
                    result += "-";
                }
                else if(max == 0){
                    //space
                    result += " ";
                }
                else{
                    //dot
                    result += ".";
                }
                if(i != array.length)
                    max = array[i];
            }
            else{
                if(i != array.length && array[i] > max){
                    max = array[i];
                }
            }
        }
        return result;
    }
    public String unHide(String inFile)throws UnsupportedAudioFileException, IOException{
        File aFile = new File(inFile);
        AudioInputStream inStream = AudioSystem.getAudioInputStream(aFile);
        inStream = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, inStream);
        AudioFormat af = inStream.getFormat();
        //int size = inStream.available();
        byte[] audioBytes = IOUtils.toByteArray(inStream);
        int pos = 0, count = 0, max = 0;
                int off = 0;
        //remove beginning 0's
        while((audioBytes[off] & 0xFF) == 0){
            ++off;
        }
        //find the beginning of the encoded bytes
        for(int i = off; i < audioBytes.length; ++i){
            if((audioBytes[i] & 0xFF) == 0){
                count += 1;
                if(count > max) max = count;
                if(count >= (Note.PERIOD*2)){
                    pos = i + 1;
                    break;
                }
            }
            else{
                count = 0;
            }
        }
        while((audioBytes[pos] & 0xFF) == 0){
            ++pos;
        }
        pos -= 1;
        return morseToString(convertBytesToMorse(audioBytes, pos));
    }
    public void Hide(String cipher, String outFile, String inFile) throws IOException, LineUnavailableException, UnsupportedAudioFileException{
        cipher = convertToMorse(cipher);
        File fileOut = new File(outFile);
        ByteArrayOutputStream oS = new ByteArrayOutputStream();
        final AudioFormat af =
            new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
        File aFile = new File(inFile);
        AudioInputStream inStream = AudioSystem.getAudioInputStream(aFile);
        AudioInputStream newStream = AudioSystem.getAudioInputStream(af, inStream);
        byte[] inBytes = IOUtils.toByteArray(newStream);
        
        oS.write(inBytes);
        byte zByte = 0;
        int numZeroes = (int)Note.PERIOD * 3; 
        for(int i = 0; i < numZeroes; ++i){
            oS.write(zByte);
        }
        
        for(int i = 0; i < cipher.length(); ++i){
            if(cipher.charAt(i) == '.'){
                oS.write(Note.DOT.data(), 0, Note.DOT.data().length);
            }
            else if(cipher.charAt(i) == '-'){
                oS.write(Note.DASH.data(), 0, Note.DASH.data().length);
            }
            else if(cipher.charAt(i) == ' '){
                oS.write(Note.SPACE.data(), 0, Note.SPACE.data().length);
            }
        }
        oS.write(Note.SPACE.data(), 0, Note.SPACE.data().length);
        byte[] audioBytes = oS.toByteArray();
                ByteArrayInputStream byteIS = new ByteArrayInputStream(audioBytes);
        AudioInputStream encodedStream = new AudioInputStream(byteIS,
                af, audioBytes.length / af.getFrameSize());
        AudioSystem.write(encodedStream, AudioFileFormat.Type.WAVE, fileOut);
    }
    public void PCMtoFile(OutputStream os, byte[] data, int srate, int channel, int format) throws IOException {
        byte[] header = new byte[44];
        
        long totalDataLen = data.length + 36;
        long bitrate = srate * channel * format;
        
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = (byte) format;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) channel;
        header[23] = 0;
        header[24] = (byte) (srate & 0xff);
        header[25] = (byte) ((srate >> 8) & 0xff);
        header[26] = (byte) ((srate >> 16) & 0xff);
        header[27] = (byte) ((srate >> 24) & 0xff);
        header[28] = (byte) ((bitrate / 8) & 0xff);
        header[29] = (byte) (((bitrate / 8) >> 8) & 0xff);
        header[30] = (byte) (((bitrate / 8) >> 16) & 0xff);
        header[31] = (byte) (((bitrate / 8) >> 24) & 0xff);
        header[32] = (byte) ((channel * format) / 8);
        header[33] = 0;
        header[34] = 16;
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (data.length  & 0xff);
        header[41] = (byte) ((data.length >> 8) & 0xff);
        header[42] = (byte) ((data.length >> 16) & 0xff);
        header[43] = (byte) ((data.length >> 24) & 0xff);
        
        os.write(header, 0, 44);
        os.write(data);
        os.close();
    }
}
