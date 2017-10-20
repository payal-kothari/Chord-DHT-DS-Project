package edu.rit.CSCI652.impl;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by payalkothari on 10/17/17.
 */
public class SHA1Code {


    public static String getSHACode(String nodeIPAndPortString) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] ipByteArr =  md.digest(nodeIPAndPortString.getBytes());
        // convert byte to hex string
        StringBuilder sb = new StringBuilder();
        for (byte b : ipByteArr) {
            int value = b & 0xFF;
            if (value < 16) {
                // value which is less than 16 , we prepend 0 to make two hex chars
                sb.append("0");
            }
            sb.append(Integer.toHexString(value).toUpperCase());
        }

        return sb.toString();
    }


}
