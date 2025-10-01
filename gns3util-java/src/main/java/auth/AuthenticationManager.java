package auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import model.GNS3Key;
import model.Token;
import model.Credentials;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling GNS3 authentication
 */
public class AuthenticationManager {

    private static final String GNS3_DIR = System.getProperty("user.home") + File.separator + ".gns3";
    private static final String DEFAULT_KEYFILE = GNS3_DIR + File.separator + "gns3key";
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Save authentication data to keyfile
     */
    public void saveAuthData(String serverUrl, Token token, String username, String keyFilePath) throws IOException {
        String filePath = keyFilePath != null ? keyFilePath : DEFAULT_KEYFILE;

        // Ensure GNS3 directory exists
        Path gns3Dir = Paths.get(GNS3_DIR);
        if (!Files.exists(gns3Dir)) {
            Files.createDirectories(gns3Dir);
        }

        List<GNS3Key> keys = loadKeys(keyFilePath);

        GNS3Key newKey = new GNS3Key(serverUrl, username, token.getAccessToken(), token.getTokenType());

        // Check if key already exists for this server
        boolean found = false;
        for (int i = 0; i < keys.size(); i++) {
            if (normalizeUrl(keys.get(i).getServerUrl()).equals(normalizeUrl(serverUrl))) {
                keys.set(i, newKey);
                found = true;
                break;
            }
        }

        if (!found) {
            keys.add(newKey);
        }

        // Write keys to file
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (GNS3Key key : keys) {
                String json = objectMapper.writeValueAsString(key);
                writer.println(json);
            }
        }
    }

    /**
     * Load all keys from keyfile
     */
    public List<GNS3Key> loadKeys(String keyFilePath) throws IOException {
        String filePath = keyFilePath != null ? keyFilePath : DEFAULT_KEYFILE;
        List<GNS3Key> keys = new ArrayList<>();

        File file = new File(filePath);
        if (!file.exists()) {
            return keys;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    GNS3Key key = objectMapper.readValue(line, GNS3Key.class);
                    keys.add(key);
                }
            }
        }

        return keys;
    }

    /**
     * Get access token for a specific server
     */
    public String getAccessTokenForServer(String serverUrl, String keyFilePath) throws IOException {
        List<GNS3Key> keys = loadKeys(keyFilePath);

        for (GNS3Key key : keys) {
            if (normalizeUrl(key.getServerUrl()).equals(normalizeUrl(serverUrl))) {
                return key.getAccessToken();
            }
        }

        return null;
    }

    /**
     * Normalize URL for comparison (remove protocol and port)
     */
    private String normalizeUrl(String url) {
        if (url.startsWith("http://")) {
            url = url.substring(7);
        } else if (url.startsWith("https://")) {
            url = url.substring(8);
        }

        int colonIndex = url.indexOf(":");
        if (colonIndex != -1) {
            url = url.substring(0, colonIndex);
        }

        return url;
    }
}
