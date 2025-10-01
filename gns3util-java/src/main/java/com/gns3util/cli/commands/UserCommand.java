package com.gns3util.cli.commands;

import com.gns3util.cli.Main;
import com.gns3util.api.GNS3Client;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * User management commands
 */
@Command(name = "user", description = "User operations",
         subcommands = {UserCommand.Create.class, UserCommand.Delete.class, UserCommand.List.class, UserCommand.Get.class, UserCommand.Me.class, UserCommand.Update.class, UserCommand.ChangePassword.class})
public class UserCommand {

    @ParentCommand
    private Main main;

    @Command(name = "create", description = "Create a new user")
    public static class Create implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Username")
        private String username;

        @CommandLine.Parameters(description = "Password")
        private String password;

        @CommandLine.Option(names = {"--email"}, description = "User email")
        private String email;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object userData = new Object() {
                    public final String username = username;
                    public final String password = password;
                    public final String email = email;
                };

                String response = client.postRaw("/v3/users", userData);
                System.out.println("Created user: " + username);

                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to create user: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "delete", description = "Delete a user")
    public static class Delete implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Username")
        private String username;

        @CommandLine.Option(names = {"--confirm"}, description = "Skip confirmation prompt")
        private boolean confirm;

        @Override
        public Integer call() throws Exception {
            try {
                if (!confirm) {
                    System.out.print("Are you sure you want to delete user '" + username + "'? (y/N): ");
                    String response = System.console().readLine();
                    if (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("yes")) {
                        System.out.println("Operation cancelled.");
                        return 0;
                    }
                }

                GNS3Client client = main.getAuthenticatedClient();
                client.delete("/v3/users/" + username, String.class);

                System.out.println("Deleted user: " + username);
                return 0;
            } catch (IOException e) {
                System.err.println("Failed to delete user: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "list", description = "List all users")
    public static class List implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/users");

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode users = mapper.readTree(response);

                    System.out.println("Users:");
                    for (JsonNode user : users) {
                        String username = user.get("username").asText();
                        String email = user.get("email").asText();
                        boolean isActive = user.get("is_active").asBoolean();
                        System.out.println("  " + username + " (" + email + ") - " + (isActive ? "Active" : "Inactive"));
                    }
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to list users: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "get", description = "Get information about a specific user")
    public static class Get implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Username")
        private String username;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/users/" + username);

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode user = mapper.readTree(response);

                    System.out.println("User Information:");
                    System.out.println("  Username: " + user.get("username").asText());
                    System.out.println("  Email: " + user.get("email").asText());
                    System.out.println("  Status: " + (user.get("is_active").asBoolean() ? "Active" : "Inactive"));
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to get user info: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "me", description = "Get current user information")
    public static class Me implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/users/me");

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode user = mapper.readTree(response);

                    System.out.println("Current User Information:");
                    System.out.println("  Username: " + user.get("username").asText());
                    System.out.println("  Email: " + user.get("email").asText());
                    System.out.println("  Status: " + (user.get("is_active").asBoolean() ? "Active" : "Inactive"));
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to get current user info: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "update", description = "Update user information")
    public static class Update implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Username")
        private String username;

        @CommandLine.Option(names = {"--email"}, description = "New email address")
        private String email;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object updateData = new Object() {
                    public final String email = email;
                };

                String response = client.put("/v3/users/" + username, updateData, String.class);

                System.out.println("Updated user: " + username);
                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to update user: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "change-password", description = "Change user password")
    public static class ChangePassword implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Username")
        private String username;

        @CommandLine.Parameters(description = "New password")
        private String newPassword;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object passwordData = new Object() {
                    public final String password = newPassword;
                };

                String response = client.put("/v3/users/" + username + "/password", passwordData, String.class);

                System.out.println("Changed password for user: " + username);
                return 0;
            } catch (IOException e) {
                System.err.println("Failed to change password: " + e.getMessage());
                return 1;
            }
        }
    }
}
