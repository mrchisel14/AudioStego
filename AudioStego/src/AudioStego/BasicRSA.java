/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package AudioStego;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.apache.commons.codec.binary.Base32;
/**
 *
 * @author shawn
 */
public class BasicRSA {
    public BasicRSA(){
        
    }
    public BasicRSA(String publicKey, String privateKey){
        kPublic = publicKey;
        kPrivate = privateKey;
    }
    public static String Encrypt(String message, String pub_key) throws Exception{
        PublicKeyData data = new PublicKeyData(pub_key);
        System.out.println("Encrypting:\nModulo: " + data.n.toString());
        System.out.println("e: " + data.e.toString());
        byte[] paddedMessage = OAEPPadMessage(message);
        byte[] ensurePositive = new byte[paddedMessage.length + 1];
        System.arraycopy(paddedMessage, 0, ensurePositive, 1, paddedMessage.length);
        BigInteger padded = new BigInteger(ensurePositive);
        BigInteger paddedCipher = padded.modPow(data.e, data.n); 
        byte[] cipher = paddedCipher.toByteArray();
        System.out.println("Padded Cipher: " + paddedCipher);
        return new Base32().encodeToString(cipher);
    }
    public static String Decrypt(String cipher, String priv_key)throws Exception{
        PrivateKeyData data = new PrivateKeyData(priv_key);
        BigInteger n = data.p.multiply(data.q);
        BigInteger c = new BigInteger(new Base32().decode(cipher));
        BigInteger m_1 = c.modPow(data.d_p, data.p);
        BigInteger m_2 = c.modPow(data.d_q, data.q);
        BigInteger h = (data.q_inv.multiply(m_1.subtract(m_2))).mod(data.p);
        BigInteger paddedM = m_2.add(h.multiply(data.q));
        byte[] paddedMessage = paddedM.toByteArray();
        byte[] actual;
        if(paddedMessage[0] == 0){
            actual = new byte[paddedMessage.length - 1];
            System.arraycopy(paddedMessage, 1, actual, 0, paddedMessage.length - 1);
        }
        else{
            actual = paddedMessage;
        }
        
        byte[] m_array = OAEPUnPadMessage(actual);
        String message = new String(m_array);
        return message;
    }
    public int GenerateKeyPair(){
        System.out.println("Generating RSA Key Pair");
        BigInteger p, q, n, PhiN, e, d;
        p = BigInteger.probablePrime(SIZE - 1, new Random());
        q = BigInteger.probablePrime(SIZE - 1, new Random());
        
        n = p.multiply(q);
        PhiN = p.subtract(BigInteger.valueOf(1));
        PhiN = PhiN.multiply(q.subtract(BigInteger.valueOf(1)));
        e = BigInteger.valueOf(65537);
        d = e.modInverse(PhiN);
        
        kPublic = EncodePublicKey(e, n);
        System.out.println("The public key is \n" + kPublic);
        kPrivate = EncodePrivateKey(n, e, d, p, q);
        System.out.println("The private key is \n" + kPrivate);
        if(kPublic != null && kPrivate != null){
            System.out.println("Successfully Generated RSA Key Pair");
            return 1;
        }
        else
            System.out.println("Failed to generate RSA Key Pair");
            return 0;
    }
    public static String Sign(String Message, String priv_key){
        throw new UnsupportedOperationException();
    }
    private String EncodePrivateKey(BigInteger n, BigInteger e, BigInteger d, BigInteger p, BigInteger q){
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BigInteger d_p = d.mod(p.subtract(BigInteger.valueOf(1))), d_q = d.mod(q.subtract(BigInteger.valueOf(1))), q_inv = q.modInverse(p);
            System.out.println("Encode Private Key:\np: " + p);
            System.out.println("q: " + q);
            System.out.println("d_p: " + d_p);
            System.out.println("d_q: " + d_q);
            System.out.println("q_inv: " + q_inv);
            int size = (SIZE >> 3) + 4;
            byte[] paddedP = new byte[size];
            byte[] p_array = p.toByteArray();
            System.arraycopy(p_array, 0, paddedP, size - p_array.length, p_array.length);
            outputStream.write(paddedP);
            byte[] q_array = q.toByteArray();
            byte[] paddedQ = new byte[size];
            System.arraycopy(q_array, 0, paddedQ, size - q_array.length, q_array.length);
            outputStream.write(paddedQ);
            byte[] d_p_array = d_p.toByteArray();
            byte[] paddedD_p = new byte[size];
            System.arraycopy(d_p_array, 0, paddedD_p, size - d_p_array.length, d_p_array.length);
            outputStream.write(paddedD_p);
            byte[] d_q_array = d_q.toByteArray();
            byte[] paddedD_q = new byte[size];
            System.arraycopy(d_q_array, 0, paddedD_q, size - d_q_array.length, d_q_array.length);
            outputStream.write(paddedD_q);
            byte[] q_inv_array = q_inv.toByteArray();
            byte[] paddedQ_inv = new byte[size];
            System.arraycopy(q_inv_array, 0, paddedQ_inv, size - q_inv_array.length, q_inv_array.length);
            outputStream.write(paddedQ_inv);
            byte all_bytes[] = outputStream.toByteArray();
            outputStream.close();
            return Base64.getEncoder().encodeToString(all_bytes);
        } catch (IOException ex) {
            Logger.getLogger(BasicRSA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    private String EncodePublicKey(BigInteger e, BigInteger n){
        byte[] n_bytes = n.toByteArray(), e_bytes = e.toByteArray();
        System.out.println("Encoding Public Key:\nModulo: " + n.toString());
        System.out.println("e: " + e.toString());
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(n_bytes);
            outputStream.write(e_bytes);
            byte all_bytes[] = outputStream.toByteArray();
            outputStream.close();
            return Base64.getEncoder().encodeToString(all_bytes);
        } catch (IOException ex) {
            Logger.getLogger(BasicRSA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    public static void TestMGF1(){
        String s = "Hello World";
        byte[] s_array = s.getBytes();
        try{
            byte[] r1 = MGF1(s_array, 0, s_array.length, 30);
            byte[] r2 = MGF1(s_array, 0, s_array.length, 30);
            
            if(Arrays.equals(r1, r2)){
                System.out.println("MGF1 Test Succeeded");
            }
        }catch(Exception e){
            Logger.getLogger(BasicRSA.class.getName()).log(Level.SEVERE, null, e);
        }
        
    }
    private static byte[] MGF1(byte[] seed, int seedOffset, int seedLength, int desiredLength) throws NoSuchAlgorithmException{
        if(desiredLength > Math.pow(2, 32)*seedLength){
            //error mask to long
            throw new IllegalArgumentException("Error mask to long");
        }
        else{
            int hLen = HASH_SIZE >> 3;
            byte[] result = new byte[desiredLength];
            byte[] temp = new byte[seedLength + 4];
            int offset = 0, i = 0;
            System.arraycopy(seed, seedOffset, temp, 4, seedLength);
            while (offset < desiredLength) {
                temp[0] = (byte) (i >>> 24);
                temp[1] = (byte) (i >>> 16);
                temp[2] = (byte) (i >>> 8);
                temp[3] = (byte) i;
                int remaining = desiredLength - offset;
                System.arraycopy(SHA1(temp), 0, result, offset, remaining < hLen ? remaining : hLen);
                offset = offset + hLen;
                i = i + 1;
            }
            return result;
        }
    }
    private static byte[] OAEPPadMessage(String m) throws Exception{
        byte m_array[] = m.getBytes("UTF-8");
        if(m_array.length > MAX_MESSAGE_SIZE){
            //error message to long
            return null;
        }
        else{
            int messageLength = m_array.length;
            int hashLength = HASH_SIZE >> 3;
            int k_0 = hashLength;
            int length = (SIZE>>2) - 2; //size*2/8 - 1= desired size in bytes
            int k_1 = length - messageLength - (hashLength<<2) - 1; 
            byte[] data = new byte[length - k_0];
            System.arraycopy(SHA1(OAEP_TYPE.getBytes("UTF-8")), 0, data, 0, hashLength);
            System.arraycopy(m_array, 0, data, hashLength+k_1+1, messageLength);
            data[hashLength + k_1] = 1;
            SecureRandom random = new SecureRandom();
            byte[] r = new byte[k_0];
            random.nextBytes(r);
            byte[] dataMask = MGF1(r, 0, k_0, length - k_0);
            for(int i = 0; i < length - k_0; ++i){
                data[i] ^= dataMask[i];
            }
            byte[] rMask = MGF1(data, 0, length - k_0, k_0);
            for(int i = 0; i < k_0; ++i){
                r[i] ^= rMask[i];
            }
            byte [] padded = new byte[length];
            System.arraycopy(r, 0, padded, 0, k_0);
            System.arraycopy(data, 0, padded, k_0, length - k_0);
            
            return padded;
        }
        
    }
    private static byte[] OAEPUnPadMessage(byte[] m_array) throws Exception{
        int messageLength = m_array.length;
        int hashLength = HASH_SIZE >> 3;
        int k_0 = hashLength;
        
        byte[] copy = new byte[messageLength];
        System.arraycopy(m_array, 0, copy, 0, messageLength);
        byte[] rMask = MGF1(copy, hashLength, messageLength - hashLength, hashLength);
        
        for(int i = 0; i < hashLength; ++i){
            copy[i] ^= rMask[i];
        }
        byte[] pHash = SHA1(OAEP_TYPE.getBytes("UTF-8"));
        byte[] dataMask = MGF1(copy, 0, k_0, messageLength - k_0);
        int index = -1;
        for(int i = hashLength; i < messageLength; ++i){
            copy[i] ^= dataMask[i - hashLength];
            if(i < (hashLength << 1)){
                if(copy[i] != pHash[i - hashLength]){
                    return null;
                }
            }
            else if(index == -1){
                if(copy[i] == 1){
                    index = i + 1;
                }
            }
        }
        if(index == -1 || index == messageLength){
            return null;
        }
        byte[] result = new byte[messageLength - index];
        System.arraycopy(copy, index, result, 0, messageLength - index);
        return result;
    }
    public static byte[] SHA1(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        return digest.digest(data);
    }
    public String kPublic = null, kPrivate = null;
    private static String OAEP_TYPE = "SHA1 MGF1";
    private static int SIZE = 1024, HASH_SIZE = 160;
    public static int MAX_MESSAGE_SIZE = (SIZE << 1)/8 - ((HASH_SIZE/8) << 1) - 1;
    private static class PublicKeyData{
        public PublicKeyData(String pub_key){
            byte all_bytes[] = Base64.getDecoder().decode(pub_key);
            int sizeOfN = ((SIZE << 1)/8);
            byte n_bytes[] = Arrays.copyOfRange(all_bytes, 0, sizeOfN);
            byte e_bytes[] = Arrays.copyOfRange(all_bytes, sizeOfN, all_bytes.length);
            n = new BigInteger(n_bytes);
            e = new BigInteger(e_bytes);
        }
        public BigInteger getModulo(){
            return n;
        }
        public BigInteger getExponent(){
            return e;
        }
        private BigInteger n, e;
    }
    private static class PrivateKeyData{
        public PrivateKeyData(String priv_key) throws IOException{
            byte all_bytes[] = Base64.getDecoder().decode(priv_key);
            int size = (SIZE >> 3) + 4;
            ByteArrayInputStream input = new ByteArrayInputStream(all_bytes);
            byte[] paddedP = new byte[size];
            input.read(paddedP, 0, size);
            byte[] paddedQ = new byte[size];
            input.read(paddedQ, 0, size);
            byte[] paddedD_p = new byte[size];
            input.read(paddedD_p, 0, size);
            byte[] paddedD_q = new byte[size];
            input.read(paddedD_q, 0, size);
            byte[] paddedQ_inv = new byte[size];
            input.read(paddedQ_inv, 0, size);
            input.close();
            p = new BigInteger(paddedP);
            q = new BigInteger(paddedQ);
            d_p = new BigInteger(paddedD_p);
            d_q = new BigInteger(paddedD_q);
            q_inv = new BigInteger(paddedQ_inv);
        }
        private BigInteger q_inv, d_q, d_p, q, p;
    }
}
