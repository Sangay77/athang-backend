package com.bfs.rma.payment.helper;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

@Component
public class KeyLoader {


    @Value("${keys.private-path}")
    private String pvtKeyFileName;

    public RSAPrivateKey readPKCS8PrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // If the private path is classpath-based, use ClassPathResource
        ClassPathResource resource = new ClassPathResource(pvtKeyFileName.replace("classpath:", ""));

        // Read the content of the private key file from the classpath
        String key = Files.readString(resource.getFile().toPath());


        // Clean up the PEM formatting by removing the beginning and end markers, and line breaks
        String privateKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        // Decode the Base64 encoded string
        byte[] encoded = Base64.decodeBase64(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }
}