package com.example.lbp;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;

public class sound_api {

    public void call_sound() {
        try {
            // Trust all certificates
            trustAllCertificates();

            // Your HTTPS API URL and token
            String API_URL = "https://10.81.52.33:5000/test";
            String API_TOKEN = "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";

            // Create URL object
            URL url = new URL(API_URL);

            // Open connection
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

            // Set request method
            con.setRequestMethod("POST");

            // Set request headers
            con.setRequestProperty("Authorization", "Bearer " + API_TOKEN);
            con.setRequestProperty("Content-Type", "application/json");

            // Create payload
            String input = "Your input data"; // Replace this with your actual input
            String payload = "{\"inputs\": \"" + input + "\"}";

            // Enable output stream and write payload
            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(payload);
                wr.flush();
            }

            // Get response code
            int responseCode = con.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            // Read response
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                // Print response
                System.out.println("Response : " + response.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to trust all certificates
    private static void trustAllCertificates() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustManagers = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    }
}
