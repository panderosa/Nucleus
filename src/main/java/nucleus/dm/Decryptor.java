package nucleus.dm;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEParameterSpec;
import java.util.Base64;
import java.security.SecureRandom;
import java.nio.file.Paths;
import java.security.Key;
import java.util.Arrays;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.jasypt.salt.RandomSaltGenerator;
import org.jasypt.salt.SaltGenerator;

public class Decryptor {  
    private final String OO_ALGORITHM = "AES";
    private final String OO_PROVIDER = "AES/CBC/PKCS5Padding";
    private byte[] RKBA;
    private final int ITERATIONS = 1000;
    private final String ALGORITHM = "PBEWithMD5AndDES";
    private final String PROVIDER = "PBEWithMD5AndDES/CBC/PKCS5Padding";
    private String PASSWORD = "gk8=347jG4;O<6";
    private final String GENERATOR = "SHA1PRNG";
    private final int SS = 8;
    private final String PREFIX = "ENC(";
    private final String SUFFIX = ")";
        
    public Decryptor() throws Exception {
    }
    
    public Decryptor(String myPassword) {
        PASSWORD = myPassword;
    }
    
    public Decryptor(JsonArray rk) throws Exception {
        RKBA = jsonToByteArray(rk);
    }
    
    public static void main(String[] args) throws Exception {
        Decryptor Dec;
        switch (args[0]) {
            case "cenc":
                Dec = new Decryptor();
                System.out.println(Dec.encryptCSAPassword(args[1]));
                break;
            case "cdec":
                Dec = new Decryptor();
                System.out.println(Dec.decryptCSAPassword(args[1]));
                break;
            case "enc": 
                Dec = new Decryptor();
                System.out.println(Dec.encryptPassword(args[1]));
                break;
            case "dec": 
                Dec = new Decryptor();
                System.out.println(Dec.decryptPassword(args[1]));
                break;
            case "decoo":
                Dec = new Decryptor();
                byte[] cfg = Dec.readRKToByteArray(args[1]);
                System.out.println(Dec.decryptOOPassword(cfg, args[2]));
                break;
            default:
                System.out.println("Bad option");
        }
    }
    
    private JsonObject readConfiguration(String fileName) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(fileName)));
        StringReader reader = new StringReader(content);
        JsonReader jreader = Json.createReader(reader);
        return jreader.readObject();  
    }
    
    byte[] concatArrays(byte[] a, byte[] b) {
        int al = a.length;
        int bl = b.length;
        byte[] c = new byte[al+bl];
        for ( int i = 0; i < al; i++ ) {
            c[i] = a[i];
        }
        for ( int i = 0; i < bl; i++ ) {
            c[al+i] = b[i];
        }
        return c;
    }
    
    byte[] getSaltPrefix(byte[] a) {
        byte[] salt = new byte[SS];
        for ( int i = 0; i < SS; i++ ) {
            salt[i] = a[i];
        }
        return salt;
    }
    
    byte[] trimSaltPrefix(byte[] a) {
        int al = a.length;
        byte[] na = new byte[al - SS]; 
        for ( int i = SS; i < al; i++ ) {
            na[i-SS] = a[i];
        }
        return na;
    }

    String encryptCSAPassword(String plainMessage) {
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        SaltGenerator saltGenerator = new RandomSaltGenerator(GENERATOR);
        config.setAlgorithm(ALGORITHM);
        config.setPassword(PASSWORD);
        config.setSaltGenerator(saltGenerator);
        encryptor.setConfig(config);
        return encryptor.encrypt(plainMessage);
        //return PropertyValueEncryptionUtils.decrypt(encryptedMessage, encryptor);
    } 
    
    void breakCSAPassword(String encryptedMessage) throws Exception {
        byte[] utf8b64 = encryptedMessage.getBytes("UTF-8");
        System.out.format("BASE 64Encrypted password: %s%n", Arrays.toString(utf8b64));
        byte[] enc = Base64.getDecoder().decode(utf8b64);
        System.out.format("Encrypted password with plain unencrypted salt prefix: %s%n", Arrays.toString(enc));
    }
    
    String decryptCSAPassword(String encryptedMessage) {
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        config.setAlgorithm(ALGORITHM);
        config.setPassword(PASSWORD);
        encryptor.setConfig(config);
        return PropertyValueEncryptionUtils.decrypt(encryptedMessage, encryptor);
    } 
    
    String encryptPassword (String password) throws Exception {
        SecureRandom sr = SecureRandom.getInstance(GENERATOR);
        byte[] salt = new byte[SS];        
        sr.nextBytes(salt);
        PBEParameterSpec ps = new PBEParameterSpec(salt,ITERATIONS);
        SecretKeyFactory kf = SecretKeyFactory.getInstance(ALGORITHM);
        char[] pass = PASSWORD.toCharArray();
        SecretKey key = kf.generateSecret(new javax.crypto.spec.PBEKeySpec(pass));
        Cipher cipher = Cipher.getInstance(PROVIDER);             
        cipher.init(Cipher.ENCRYPT_MODE, key, ps);
        byte[] utf8 = password.getBytes("UTF-8");
        byte[] enc = cipher.doFinal(utf8);
        byte[] result = concatArrays(salt,enc);
        byte[] encb64 = Base64.getEncoder().encode(result);
        return new String(encb64,"UTF-8");
    }
    
    
    String decryptPassword(String password) throws Exception { 
        if(isEncrypted(password)) {
            String pp = getInnerEncryptedValue(password);
            byte[] utf8b64 = pp.getBytes("UTF-8");
            byte[] result = Base64.getDecoder().decode(utf8b64);
            byte[] salt = getSaltPrefix(result);
            byte[] enc = trimSaltPrefix(result);
            PBEParameterSpec ps = new PBEParameterSpec(salt,ITERATIONS);
            SecretKeyFactory kf = SecretKeyFactory.getInstance(ALGORITHM);
            char[] pass = PASSWORD.toCharArray();
            SecretKey key = kf.generateSecret(new javax.crypto.spec.PBEKeySpec(pass));
            Cipher cipher = Cipher.getInstance(PROVIDER);             
            cipher.init(Cipher.DECRYPT_MODE, key,ps);       
            byte[] utf8 = cipher.doFinal(enc);
            return new String(utf8,"UTF-8");
        }
        else {
            return password;
        }
    } 
    
    byte[] readRKToByteArray(String fileName) throws Exception{
        String content = new String(Files.readAllBytes(Paths.get(fileName)));
        StringReader reader = new StringReader(content);
        JsonReader jreader = Json.createReader(reader);
        JsonArray ja = jreader.readObject().getJsonArray("RK");
        byte[] rkba = new byte[ja.size()];
        for ( int i = 0 ; i < rkba.length ; i++ ) {
            rkba[i] = (byte) ja.getInt(i);
        }
        return rkba;
    }
    
    public byte[] jsonToByteArray(JsonArray jjaa) throws Exception{
        byte[] ba = new byte[jjaa.size()];
        for ( int i = 0 ; i < ba.length ; i++ ) {
            ba[i] = (byte) jjaa.getInt(i);
        }
        return ba;
    }
    
    String decryptOOPassword(byte[] rkba, String encryptedPassword) throws Exception {
        String ep = encryptedPassword.replaceFirst("\\{ENCRYPTED\\}", "");
        byte[] newIv = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        Key key = new SecretKeySpec(rkba,OO_ALGORITHM);
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec isp = new IvParameterSpec(newIv);
        c.init(Cipher.DECRYPT_MODE, key,isp);
        //byte[] cypherBytes = org.apache.commons.codec.binary.Base64.decodeBase64(ep);
        byte[] cypherBytes = Base64.getDecoder().decode(ep);
        return new String(c.doFinal(cypherBytes),"UTF-8");
    }
    
    String decryptOOPassword(String encryptedPassword) throws Exception {
        String ep = encryptedPassword.replaceFirst("\\{ENCRYPTED\\}", "");
        byte[] newIv = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        Key key = new SecretKeySpec(RKBA,OO_ALGORITHM);
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec isp = new IvParameterSpec(newIv);
        c.init(Cipher.DECRYPT_MODE, key,isp);
        //byte[] cypherBytes = org.apache.commons.codec.binary.Base64.decodeBase64(ep);
        byte[] cypherBytes = Base64.getDecoder().decode(ep);
        return new String(c.doFinal(cypherBytes),"UTF-8");
    }
    
    String encryptOOPassword(byte[] rkba, String password) throws Exception {
        byte[] plainBytes = password.getBytes("UTF-8");
        byte[] newIv = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        Key key = new SecretKeySpec(rkba,OO_ALGORITHM);
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec isp = new IvParameterSpec(newIv);
        c.init(Cipher.ENCRYPT_MODE, key,isp);
        byte[] encBytes = c.doFinal(plainBytes);
        System.out.println("Encoded: " + Arrays.toString(encBytes));
        //byte[] enc64Bytes = org.apache.commons.codec.binary.Base64.encodeBase64(encBytes);
        byte[] enc64Bytes = Base64.getEncoder().encode(encBytes);
        System.out.println("64 Encoded: " + Arrays.toString(enc64Bytes));
        return new String(c.doFinal(enc64Bytes),"UTF-8");
    }
    
    
    boolean isEncrypted(String enc) {
        String encMessage = enc.trim();     
        return (encMessage.startsWith(PREFIX) && encMessage.endsWith(SUFFIX));
    }
    
    String getInnerEncryptedValue(String enc) {
        String encMessage = enc.trim();
        return encMessage.substring(PREFIX.length(), encMessage.length() - SUFFIX.length());
    }
    
    byte[] generateSalt(int saltlength) throws Exception {
        SecureRandom sr = SecureRandom.getInstance(GENERATOR);
        byte[] salt = new byte[saltlength];
        sr.nextBytes(salt);
        return salt;
    }
    
    void printSalt(byte[] salt) {
        System.out.println(Arrays.toString(salt));
    }
}
