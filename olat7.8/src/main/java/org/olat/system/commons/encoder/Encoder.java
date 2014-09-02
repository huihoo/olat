/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.system.commons.encoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Description:
 * 
 * @author Sabina Jeger
 */
public class Encoder {

    private static final Logger LOG = LoggerHelper.getLogger();

    @Deprecated
    private static final String HASH_ALGORITHM = "MD5";
    private static final PasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(5);

    /**
     * The MD5 helper object for this class.
     */
    public static final MD5Encoder md5Encoder = new MD5Encoder();

    /**
     * This should NOT be used for password hashing, it is way too weak. <br>
     * 
     * encrypt the supplied argument with md5.
     * 
     * @param s
     * @return MD5 encrypted string
     */
    @Deprecated
    public static String encrypt(String s) {
        byte[] inbytes = s.getBytes();
        try {
            MessageDigest md5Helper = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] outbytes = md5Helper.digest(inbytes);
            String out = md5Encoder.encode(outbytes);
            return out;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertException("Cannot load MD5 Message Digest ," + HASH_ALGORITHM + " not supported");
        }
    }

    /**
     * This is the recommended password hashing, it uses a Spring Security implementation for bcrypt. <br>
     * Delegates to <code>org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder</code>.
     */
    public static String bCryptEncode(String rawPassword) {
        String hashedPassword = bCryptPasswordEncoder.encode(rawPassword);
        return hashedPassword;
    }

    /**
     * Checks the password against the encoded one (via bCryptEncode). <br>
     * Delegates to <code>org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder</code>.
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        try {
            return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
        } catch (Exception e) {
            LOG.error("password match check failed because of: " + e.getMessage());
        }
        return false;
    }

    /**
     * encrypt the first argument and show the result on the console
     * 
     * @param args
     */
    public static void main(String[] args) {
        String result = encrypt(args[0]);
        System.out.println("MD5-Hash of " + args[0] + ": " + result);
    }

}
