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
 * Handles encryption and decryption of files using AES-GCM.
 */
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // 16 bytes
    
    private static EncryptionService instance;
    
    private EncryptionService() {
        // Private constructor for singleton pattern
    }
    
    public static synchronized EncryptionService getInstance() {
        if (instance == null) {
            instance = new EncryptionService();
        }
        return instance;
    }
    
    /**
     * Encrypts a file using the master key derived from the user's password.
     * 
     * @param inputFile The file to encrypt
     * @param outputFile The encrypted output file
     * @return true if encryption was successful
     * @throws Exception if an error occurs during encryption
     */
    public boolean encryptFile(File inputFile, File outputFile) throws Exception {
        byte[] keyBytes = UserManager.getInstance().getMasterKey();
        if (keyBytes == null) {
            throw new IllegalStateException("No master key available. User must be authenticated.");
        }
        
        // Generate a random IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        
        // Create the cipher
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);
        
        try (FileInputStream inputStream = new FileInputStream(inputFile);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            
            // Write the IV to the output file first
            outputStream.write(iv);
            
            // Then encrypt the file content
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
     * Decrypts a file using the master key derived from the user's password.
     * 
     * @param inputFile The encrypted file
     * @param outputFile The decrypted output file
     * @return true if decryption was successful
     * @throws Exception if an error occurs during decryption
     */
    public boolean decryptFile(File inputFile, File outputFile) throws Exception {
        byte[] keyBytes = UserManager.getInstance().getMasterKey();
        if (keyBytes == null) {
            throw new IllegalStateException("No master key available. User must be authenticated.");
        }
        
        try (FileInputStream inputStream = new FileInputStream(inputFile);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            
            // Read the IV from the input file
            byte[] iv = new byte[GCM_IV_LENGTH];
            int bytesRead = inputStream.read(iv);
            if (bytesRead < GCM_IV_LENGTH) {
                throw new IOException("Input file too short or corrupt");
            }
            
            // Create the cipher
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);
            
            // Decrypt the file content
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