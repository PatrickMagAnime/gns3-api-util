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
 * Group management commands
 */
@Command(name = "group", description = "Group operations",
         subcommands = {GroupCommand.Create.class, GroupCommand.Delete.class, GroupCommand.List.class, GroupCommand.Get.class, GroupCommand.Update.class, GroupCommand.AddMember.class})
public class GroupCommand {

    @ParentCommand
    private Main main;

    @Command(name = "create", description = "Create a new group")
    public static class Create implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Group name")
        private String groupName;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object groupData = new Object() {
                    public final String name = groupName;
                };

                String response = client.postRaw("/v3/groups", groupData);
                System.out.println("Created group: " + groupName);

                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to create group: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "delete", description = "Delete a group")
    public static class Delete implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Group name")
        private String groupName;

        @CommandLine.Option(names = {"--confirm"}, description = "Skip confirmation prompt")
        private boolean confirm;

        @Override
        public Integer call() throws Exception {
            try {
                if (!confirm) {
                    System.out.print("Are you sure you want to delete group '" + groupName + "'? (y/N): ");
                    String response = System.console().readLine();
                    if (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("yes")) {
                        System.out.println("Operation cancelled.");
                        return 0;
                    }
                }

                GNS3Client client = main.getAuthenticatedClient();
                client.delete("/v3/groups/" + groupName, String.class);

                System.out.println("Deleted group: " + groupName);
                return 0;
            } catch (IOException e) {
                System.err.println("Failed to delete group: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "list", description = "List all groups")
    public static class List implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/groups");

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode groups = mapper.readTree(response);

                    System.out.println("Groups:");
                    for (JsonNode group : groups) {
                        String name = group.get("name").asText();
                        System.out.println("  " + name);
                    }
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to list groups: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "get", description = "Get information about a specific group")
    public static class Get implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Group name")
        private String groupName;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/groups/" + groupName);

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode group = mapper.readTree(response);

                    System.out.println("Group Information:");
                    System.out.println("  Name: " + group.get("name").asText());
                    System.out.println("  Members: " + group.get("user_count").asInt());
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to get group info: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "update", description = "Update group information")
    public static class Update implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Group name")
        private String groupName;

        @CommandLine.Option(names = {"--name"}, description = "New group name")
        private String newName;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object updateData = new Object() {
                    public final String name = newName;
                };

                String response = client.put("/v3/groups/" + groupName, updateData, String.class);

                System.out.println("Updated group: " + groupName);
                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to update group: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "add-member", description = "Add a user to a group")
    public static class AddMember implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Group name")
        private String groupName;

        @CommandLine.Parameters(description = "Username to add")
        private String username;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object memberData = new Object() {
                    public final String username = username;
                };

                String response = client.postRaw("/v3/groups/" + groupName + "/members", memberData);
                System.out.println("Added user " + username + " to group " + groupName);

                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to add member to group: " + e.getMessage());
                return 1;
            }
        }
    }
}
