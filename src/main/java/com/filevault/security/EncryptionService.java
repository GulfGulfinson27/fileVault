package com.filevault.security;

import com.filevault.model.UserManager;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Verarbeitet die Verschlüsselung und Entschlüsselung von Dateien mit AES-GCM.
 */
public class EncryptionService {

    /** Der verwendete Verschlüsselungsalgorithmus */
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    
    /** Länge des Initialisierungsvektors in Bytes (96 Bits) */
    private static final int GCM_IV_LENGTH = 12;
    
    /** Länge des Authentifizierungs-Tags in Bits (16 Bytes) */
    private static final int GCM_TAG_LENGTH = 128;
    
    /** Die einzige Instanz des EncryptionService */
    private static EncryptionService instance;
    
    /**
     * Privater Konstruktor für das Singleton-Pattern.
     */
    private EncryptionService() {
    }
    
    /**
     * Gibt die einzige Instanz des EncryptionService zurück.
     * 
     * @return Die Singleton-Instanz des EncryptionService
     */
    public static synchronized EncryptionService getInstance() {
        if (instance == null) {
            instance = new EncryptionService();
        }
        return instance;
    }
    
    /**
     * Verschlüsselt eine Datei mit dem Master-Schlüssel, der aus dem Benutzerpasswort abgeleitet wurde.
     * 
     * @param inputFile Die zu verschlüsselnde Datei
     * @param outputFile Die verschlüsselte Ausgabedatei
     * @return true, wenn die Verschlüsselung erfolgreich war
     * @throws Exception wenn ein Fehler während der Verschlüsselung auftritt
     */
    public boolean encryptFile(File inputFile, File outputFile) throws Exception {
        byte[] keyBytes = UserManager.getInstance().getMasterKey();
        if (keyBytes == null) {
            throw new IllegalStateException("Kein Master-Schlüssel verfügbar. Benutzer muss authentifiziert sein.");
        }
        
        // Generiere einen zufälligen Initialisierungsvektor
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        
        // Erstelle den Cipher
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);
        
        try (FileInputStream inputStream = new FileInputStream(inputFile);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            
            // Schreibe den Initialisierungsvektor zuerst in die Ausgabedatei
            outputStream.write(iv);
            
            // Verschlüssele dann den Dateiinhalt
            try (CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    cipherOutputStream.write(buffer, 0, bytesRead);
                }
            }
        }
        
        return true;
    }
    
    /**
     * Entschlüsselt eine Datei mit dem Master-Schlüssel, der aus dem Benutzerpasswort abgeleitet wurde.
     * 
     * @param inputFile Die verschlüsselte Datei
     * @param outputFile Die entschlüsselte Ausgabedatei
     * @return true, wenn die Entschlüsselung erfolgreich war
     * @throws Exception wenn ein Fehler während der Entschlüsselung auftritt
     */
    public boolean decryptFile(File inputFile, File outputFile) throws Exception {
        byte[] keyBytes = UserManager.getInstance().getMasterKey();
        if (keyBytes == null) {
            throw new IllegalStateException("Kein Master-Schlüssel verfügbar. Benutzer muss authentifiziert sein.");
        }
        
        try (FileInputStream inputStream = new FileInputStream(inputFile);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            
            // Lese den Initialisierungsvektor aus der Eingabedatei
            byte[] iv = new byte[GCM_IV_LENGTH];
            int bytesRead = inputStream.read(iv);
            if (bytesRead < GCM_IV_LENGTH) {
                throw new IOException("Eingabedatei zu kurz oder beschädigt");
            }
            
            // Erstelle den Cipher
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);
            
            // Entschlüssele den Dateiinhalt
            try (CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher)) {
                byte[] buffer = new byte[8192];
                while ((bytesRead = cipherInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }
        
        return true;
    }
} 