package com.sipclient.sip.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class DigestCalculator {

    public String calculateResponse(
            String username,
            String realm,
            String password,
            String method,
            String uri,
            String nonce,
            String nc,
            String cnonce,
            String qop) {

        String ha1 =
                md5(username + ":" +
                        realm + ":" +
                        password);


        String ha2 =
                md5(method + ":" +
                        uri);
                

        return md5(
                ha1 + ":" +
                nonce + ":" +
                nc + ":" +
                cnonce + ":" +
                qop + ":" +
                ha2);

    }

    private String md5(String text) {

        try {

            MessageDigest md =
                    MessageDigest.getInstance("MD5");

            byte[] digest =
                    md.digest(
                            text.getBytes(
                                    StandardCharsets.UTF_8));

            StringBuilder sb =
                    new StringBuilder();

            for (byte b : digest) {

                sb.append(
                        String.format("%02x", b));

            }

            return sb.toString();

        } catch (Exception e) {

            throw new RuntimeException(e);

        }

    }

}