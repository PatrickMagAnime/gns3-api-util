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
 * Role management commands
 */
@Command(name = "role", description = "Role operations",
         subcommands = {RoleCommand.Create.class, RoleCommand.Delete.class, RoleCommand.List.class, RoleCommand.Get.class, RoleCommand.Update.class, RoleCommand.AddPrivilege.class, RoleCommand.DeletePrivilege.class})
public class RoleCommand {

    @ParentCommand
    private Main main;

    @Command(name = "create", description = "Create a new role")
    public static class Create implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Role name")
        private String roleName;

        @CommandLine.Option(names = {"--description"}, description = "Role description")
        private String description;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object roleData = new Object() {
                    public final String name = roleName;
                    public final String description = description;
                };

                String response = client.postRaw("/v3/roles", roleData);
                System.out.println("Created role: " + roleName);

                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to create role: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "delete", description = "Delete a role")
    public static class Delete implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Role name")
        private String roleName;

        @CommandLine.Option(names = {"--confirm"}, description = "Skip confirmation prompt")
        private boolean confirm;

        @Override
        public Integer call() throws Exception {
            try {
                if (!confirm) {
                    System.out.print("Are you sure you want to delete role '" + roleName + "'? (y/N): ");
                    String response = System.console().readLine();
                    if (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("yes")) {
                        System.out.println("Operation cancelled.");
                        return 0;
                    }
                }

                GNS3Client client = main.getAuthenticatedClient();
                client.delete("/v3/roles/" + roleName, String.class);

                System.out.println("Deleted role: " + roleName);
                return 0;
            } catch (IOException e) {
                System.err.println("Failed to delete role: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "list", description = "List all roles")
    public static class List implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/roles");

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode roles = mapper.readTree(response);

                    System.out.println("Roles:");
                    for (JsonNode role : roles) {
                        String name = role.get("name").asText();
                        String description = role.get("description").asText();
                        System.out.println("  " + name + " - " + description);
                    }
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to list roles: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "get", description = "Get information about a specific role")
    public static class Get implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Role name")
        private String roleName;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/roles/" + roleName);

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode role = mapper.readTree(response);

                    System.out.println("Role Information:");
                    System.out.println("  Name: " + role.get("name").asText());
                    System.out.println("  Description: " + role.get("description").asText());
                    System.out.println("  Privileges: " + role.get("privilege_count").asInt());
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to get role info: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "update", description = "Update role information")
    public static class Update implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Role name")
        private String roleName;

        @CommandLine.Option(names = {"--description"}, description = "New role description")
        private String description;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object updateData = new Object() {
                    public final String description = description;
                };

                String response = client.put("/v3/roles/" + roleName, updateData, String.class);

                System.out.println("Updated role: " + roleName);
                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to update role: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "add-privilege", description = "Add a privilege to a role")
    public static class AddPrivilege implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Role name")
        private String roleName;

        @CommandLine.Parameters(description = "Privilege name")
        private String privilege;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object privilegeData = new Object() {
                    public final String privilege = privilege;
                };

                String response = client.postRaw("/v3/roles/" + roleName + "/privileges", privilegeData);
                System.out.println("Added privilege '" + privilege + "' to role '" + roleName + "'");

                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to add privilege to role: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "delete-privilege", description = "Remove a privilege from a role")
    public static class DeletePrivilege implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Role name")
        private String roleName;

        @CommandLine.Parameters(description = "Privilege name")
        private String privilege;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                client.delete("/v3/roles/" + roleName + "/privileges/" + privilege, String.class);

                System.out.println("Removed privilege '" + privilege + "' from role '" + roleName + "'");
                return 0;
            } catch (IOException e) {
                System.err.println("Failed to remove privilege from role: " + e.getMessage());
                return 1;
            }
        }
    }
}
