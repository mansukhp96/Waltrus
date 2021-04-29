package com.izmansuk.securepasswordmanager.utils;

import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class AESHelper {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String encrypt(String strToEncrypt, Context context, SecretKey secKey) {
        try {
            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES
                    + "/"
                    + KeyProperties.BLOCK_MODE_CBC
                    + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            cipher.init(Cipher.ENCRYPT_MODE, secKey);
            byte[] encryptionIv = cipher.getIV();

            UtilsHelper.getEncryptedSharedPreferences(context)
                    .edit()
                    .putString("recordEncryptionIV", Base64.encodeToString(encryptionIv, Base64.DEFAULT))
                    .apply();

            byte[] passwordBytes = strToEncrypt.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedPasswordBytes = cipher.doFinal(passwordBytes);

            return android.util.Base64.encodeToString(encryptedPasswordBytes, android.util.Base64.DEFAULT);

        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String decrypt(String strToDecrypt, Context context, SecretKey secKey) {
        try {

            byte[] encryptedDbPassword = Base64.decode(strToDecrypt, Base64.DEFAULT);
            String base64EncIv = UtilsHelper.getEncryptedSharedPreferences(context)
                    .getString("recordEncryptionIV", null);
            byte[] encryptionIv = android.util.Base64.decode(base64EncIv, android.util.Base64.DEFAULT);

            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES
                    + "/"
                    + KeyProperties.BLOCK_MODE_CBC
                    + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            cipher.init(Cipher.DECRYPT_MODE, secKey, new IvParameterSpec(encryptionIv));
            byte[] dbPasswordBytes = cipher.doFinal(encryptedDbPassword);
            return new String(dbPasswordBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

    public static SecretKey generateSecretKey() {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keygen.init(new KeyGenParameterSpec.Builder("Key",
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setUserAuthenticationValidityDurationSeconds(20)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            return keygen.generateKey();
        } catch (NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return null;
        }
    }

}
