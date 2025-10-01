package cli.commands;

import cli.Main;
import api.GNS3Client;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Compute management commands
 */
@Command(name = "compute", description = "Compute operations",
         subcommands = {ComputeCommand.Create.class, ComputeCommand.Delete.class, ComputeCommand.List.class, ComputeCommand.Get.class, ComputeCommand.Update.class})
public class ComputeCommand {

    @ParentCommand
    private Main main;

    @Command(name = "create", description = "Create a new compute")
    public static class Create implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Compute name")
        private String computeName;

        @CommandLine.Option(names = {"--protocol"}, description = "Protocol (http/https)", defaultValue = "http")
        private String protocol;

        @CommandLine.Option(names = {"--host"}, description = "Host address", required = true)
        private String host;

        @CommandLine.Option(names = {"--port"}, description = "Port number", defaultValue = "3080")
        private int port;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object computeData = new Object() {
                    public final String name = computeName;
                    public final String protocol = protocol;
                    public final String host = host;
                    public final int port = port;
                };

                String response = client.postRaw("/v3/computes", computeData);
                System.out.println("Created compute: " + computeName + " at " + host + ":" + port);

                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to create compute: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "delete", description = "Delete a compute")
    public static class Delete implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Compute ID")
        private String computeId;

        @CommandLine.Option(names = {"--confirm"}, description = "Skip confirmation prompt")
        private boolean confirm;

        @Override
        public Integer call() throws Exception {
            try {
                if (!confirm) {
                    System.out.print("Are you sure you want to delete compute '" + computeId + "'? (y/N): ");
                    String response = System.console().readLine();
                    if (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("yes")) {
                        System.out.println("Operation cancelled.");
                        return 0;
                    }
                }

                GNS3Client client = main.getAuthenticatedClient();
                client.delete("/v3/computes/" + computeId, String.class);

                System.out.println("Deleted compute: " + computeId);
                return 0;
            } catch (IOException e) {
                System.err.println("Failed to delete compute: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "list", description = "List all computes")
    public static class List implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/computes");

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode computes = mapper.readTree(response);

                    System.out.println("Computes:");
                    for (JsonNode compute : computes) {
                        String name = compute.get("name").asText();
                        String host = compute.get("host").asText();
                        int port = compute.get("port").asInt();
                        String status = compute.get("status").asText();
                        System.out.println("  " + name + " (" + host + ":" + port + ") - " + status);
                    }
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to list computes: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "get", description = "Get information about a specific compute")
    public static class Get implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Compute ID")
        private String computeId;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/computes/" + computeId);

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode compute = mapper.readTree(response);

                    System.out.println("Compute Information:");
                    System.out.println("  Name: " + compute.get("name").asText());
                    System.out.println("  Host: " + compute.get("host").asText());
                    System.out.println("  Port: " + compute.get("port").asInt());
                    System.out.println("  Protocol: " + compute.get("protocol").asText());
                    System.out.println("  Status: " + compute.get("status").asText());
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to get compute info: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "update", description = "Update a compute")
    public static class Update implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Compute ID")
        private String computeId;

        @CommandLine.Option(names = {"--name"}, description = "New compute name")
        private String name;

        @CommandLine.Option(names = {"--host"}, description = "New host address")
        private String host;

        @CommandLine.Option(names = {"--port"}, description = "New port number")
        private Integer port;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                // Build update data dynamically based on provided options
                Object updateData = new Object() {
                    public final String name = name;
                    public final String host = host;
                    public final Integer port = port;
                };

                String response = client.put("/v3/computes/" + computeId, updateData, String.class);

                System.out.println("Updated compute: " + computeId);
                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to update compute: " + e.getMessage());
                return 1;
            }
        }
    }
}
