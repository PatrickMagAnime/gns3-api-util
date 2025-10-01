package com.gns3util.cli;

import com.gns3util.auth.AuthenticationManager;
import com.gns3util.api.GNS3Client;
import com.gns3util.util.InteractiveUtils;
import com.gns3util.model.Credentials;
import com.gns3util.model.Token;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;

/**
 * Main CLI application class for GNS3 Utility
 */
@Command(name = "gns3util-java", mixinStandardHelpOptions = true, version = "1.0.0",
         description = "A Java utility for managing GNS3v3 servers",
         subcommands = {AclCommand.class, ApplianceCommand.class, AuthCommand.class, ClassCommand.class, ClusterCommand.class, ComputeCommand.class, DrawingCommand.class, ExerciseCommand.class, GroupCommand.class, ImageCommand.class, LinkCommand.class, NodeCommand.class, PoolCommand.class, ProjectCommand.class, RemoteCommand.class, RoleCommand.class, ShareCommand.class, SnapshotCommand.class, SymbolCommand.class, SystemCommand.class, TemplateCommand.class, UserCommand.class})
public class Main implements Callable<Integer> {
    @Option(names = {"-s", "--server"}, description = "GNS3v3 Server URL", required = true)
    private String server;
    @Option(names = {"-k", "--key-file"}, description = "Path to authentication keyfile")
    private String keyFile;

    @Option(names = {"-i", "--insecure"}, description = "Ignore SSL certificate errors")
    private boolean insecure;

    @Option(names = {"--raw"}, description = "Output raw JSON instead of formatted text")
    public boolean raw;

    private final AuthenticationManager authManager = new AuthenticationManager();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        // Main entry point - show help if no subcommand is provided
        CommandLine.usage(this, System.out);
        return 0;
    }

    /**
     * Get authenticated GNS3 client for the current server
     */
    public GNS3Client getAuthenticatedClient() throws IOException {
        String accessToken = authManager.getAccessTokenForServer(server, keyFile);
        if (accessToken == null) {
            throw new IOException("No authentication token found for server " + server + ". Please login first.");
        }
        return new GNS3Client(server, accessToken, insecure);
    }

    /**
     * Authenticate user and save token
     */
    public void authenticate(String username, String password) throws IOException {
        GNS3Client client = new GNS3Client(server, "", insecure);
        Credentials credentials = new Credentials(username, password);

        try {
            String response = client.postRaw("/v3/users/authenticate", credentials);
            JsonNode jsonResponse = objectMapper.readTree(response);

            if (jsonResponse.has("access_token") && jsonResponse.has("token_type")) {
                String accessToken = jsonResponse.get("access_token").asText();
                String tokenType = jsonResponse.get("token_type").asText();

                Token token = new Token(accessToken, tokenType);
                authManager.saveAuthData(server, token, username, keyFile);

                System.out.println("Successfully logged in as " + username);
            } else {
                throw new IOException("Invalid response from server");
            }
        } catch (IOException e) {
            if (e.getMessage().contains("401")) {
                throw new IOException("Authentication failed. Please check your username and password.");
            }
            throw e;
        }
    }

    /**
     * Get user input for credentials interactively
     */
    public Credentials getCredentialsInteractively() {
        InteractiveUtils.Credentials creds = InteractiveUtils.getLoginCredentials();
        return new Credentials(creds.getUsername(), creds.getPassword());
    }
}
