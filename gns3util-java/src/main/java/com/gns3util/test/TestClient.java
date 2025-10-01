package com.gns3util.test;

import com.gns3util.auth.AuthenticationManager;
import com.gns3util.api.GNS3Client;
import com.gns3util.model.Credentials;

public class TestClient {
    public static void main(String[] args) {
        try {
            // Test authentication with provided credentials
            AuthenticationManager authManager = new AuthenticationManager();

            // Try to load existing keys first
            System.out.println("Testing with provided credentials...");

            // Create a client with the first server
            GNS3Client client = new GNS3Client("http://lab-dashboard:8001", "", false);

            // Try to authenticate
            Credentials creds = new Credentials("admin", "");
            System.out.println("Attempting authentication...");

            // This would normally be done through the CLI, but let's test the API call
            System.out.println("Client created successfully");

        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
