package com.condation.cms.auth.utils;

/*-
 * #%L
 * cms-auth
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class SecurityUtil {

    private static final int SALT_LENGTH = 16; // in Bytes
    private static final int ITERATIONS = 100_000;
    private static final int KEY_LENGTH = 256; // in Bits

	private static SecureRandom random = new SecureRandom();

	
    /**
     * create a random salt
	 * @return random salt as byte array
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * creates PBKDF2-Hash (SHA-256) from password and salt.
	 * @param password
	 * @param salt
	 * @return the hash base64 encoded
     */
    public static String hashPBKDF2(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (InvalidKeySpecException | java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Fehler beim Passwort-Hashing", e);
        }
    }

    /**
     * verifies a password
	 * 
	 * @param inputPassword the password
	 * @param expectedBase64Hash the expected has
	 * @param salt the salt
	 * @return 
     */
    public static boolean verifyPassword(String inputPassword, String expectedBase64Hash, byte[] salt) {
        String inputBase64Hash = hashPBKDF2(inputPassword, salt);
        return MessageDigest.isEqual(
                inputBase64Hash.getBytes(StandardCharsets.UTF_8),
                expectedBase64Hash.getBytes(StandardCharsets.UTF_8)
        );
    }
}
