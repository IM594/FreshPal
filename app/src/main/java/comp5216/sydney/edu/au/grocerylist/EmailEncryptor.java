package comp5216.sydney.edu.au.grocerylist;
import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;
import android.security.keystore.KeyProtection.Builder;
import android.util.Base64;
import android.widget.Toast;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class EmailEncryptor {

    private static final String KEY_ALIAS = "COMP5216";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    public static String encryptEmail(String email, Context context) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            if (!keyStore.containsAlias(KEY_ALIAS)) {
                generateSecretKey();
            }

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), cipher.getParameters());

            byte[] encryptedEmailBytes = cipher.doFinal(email.getBytes());
            return Base64.encodeToString(encryptedEmailBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Email encryption failed.", Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    public static String decryptEmail(String encryptedEmail, Context context) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), cipher.getParameters());

            byte[] encryptedEmailBytes = Base64.decode(encryptedEmail, Base64.DEFAULT);
            byte[] decryptedEmailBytes = cipher.doFinal(encryptedEmailBytes);

            return new String(decryptedEmailBytes);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Email decryption failed.", Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    private static void generateSecretKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
        );

        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
        )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .setUserAuthenticationValidityDurationSeconds(5 * 60)
                .setRandomizedEncryptionRequired(true)
                .setKeySize(256)
                .build();

        keyGenerator.init(keyGenParameterSpec);
        keyGenerator.generateKey();
    }

    private static SecretKey getSecretKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        try {
            return (SecretKey) keyStore.getKey(KEY_ALIAS, null);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
