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
 * Resource pool management commands
 */
@Command(name = "pool", description = "Resource pool operations",
         subcommands = {PoolCommand.Create.class, PoolCommand.Delete.class, PoolCommand.List.class, PoolCommand.Get.class, PoolCommand.Update.class, PoolCommand.AddResource.class, PoolCommand.DeleteResource.class})
public class PoolCommand {

    @ParentCommand
    private Main main;

    @Command(name = "create", description = "Create a new resource pool")
    public static class Create implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Pool name")
        private String poolName;

        @CommandLine.Option(names = {"--description"}, description = "Pool description")
        private String description;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object poolData = new Object() {
                    public final String name = poolName;
                    public final String description = description;
                };

                String response = client.postRaw("/v3/pools", poolData);
                System.out.println("Created pool: " + poolName);

                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to create pool: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "delete", description = "Delete a resource pool")
    public static class Delete implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Pool name")
        private String poolName;

        @CommandLine.Option(names = {"--confirm"}, description = "Skip confirmation prompt")
        private boolean confirm;

        @Override
        public Integer call() throws Exception {
            try {
                if (!confirm) {
                    System.out.print("Are you sure you want to delete pool '" + poolName + "'? (y/N): ");
                    String response = System.console().readLine();
                    if (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("yes")) {
                        System.out.println("Operation cancelled.");
                        return 0;
                    }
                }

                GNS3Client client = main.getAuthenticatedClient();
                client.delete("/v3/pools/" + poolName, String.class);

                System.out.println("Deleted pool: " + poolName);
                return 0;
            } catch (IOException e) {
                System.err.println("Failed to delete pool: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "list", description = "List all resource pools")
    public static class List implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/pools");

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode pools = mapper.readTree(response);

                    System.out.println("Resource Pools:");
                    for (JsonNode pool : pools) {
                        String name = pool.get("name").asText();
                        String description = pool.get("description").asText();
                        System.out.println("  " + name + " - " + description);
                    }
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to list pools: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "get", description = "Get information about a specific pool")
    public static class Get implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Pool name")
        private String poolName;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/pools/" + poolName);

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode pool = mapper.readTree(response);

                    System.out.println("Pool Information:");
                    System.out.println("  Name: " + pool.get("name").asText());
                    System.out.println("  Description: " + pool.get("description").asText());
                    System.out.println("  Resources: " + pool.get("resource_count").asInt());
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to get pool info: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "update", description = "Update pool information")
    public static class Update implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Pool name")
        private String poolName;

        @CommandLine.Option(names = {"--description"}, description = "New pool description")
        private String description;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object updateData = new Object() {
                    public final String description = description;
                };

                String response = client.put("/v3/pools/" + poolName, updateData, String.class);

                System.out.println("Updated pool: " + poolName);
                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to update pool: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "add-resource", description = "Add a resource to a pool")
    public static class AddResource implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Pool name")
        private String poolName;

        @CommandLine.Parameters(description = "Resource type")
        private String resourceType;

        @CommandLine.Parameters(description = "Resource ID")
        private String resourceId;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object resourceData = new Object() {
                    public final String resource_type = resourceType;
                    public final String resource_id = resourceId;
                };

                String response = client.postRaw("/v3/pools/" + poolName + "/resources", resourceData);
                System.out.println("Added " + resourceType + " " + resourceId + " to pool " + poolName);

                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to add resource to pool: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "delete-resource", description = "Remove a resource from a pool")
    public static class DeleteResource implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Pool name")
        private String poolName;

        @CommandLine.Parameters(description = "Resource ID")
        private String resourceId;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                client.delete("/v3/pools/" + poolName + "/resources/" + resourceId, String.class);

                System.out.println("Removed resource " + resourceId + " from pool " + poolName);
                return 0;
            } catch (IOException e) {
                System.err.println("Failed to remove resource from pool: " + e.getMessage());
                return 1;
            }
        }
    }
}
