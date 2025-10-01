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
 * Link management commands
 */
@Command(name = "link", description = "Link operations",
         subcommands = {LinkCommand.Create.class, LinkCommand.Delete.class, LinkCommand.List.class, LinkCommand.Get.class, LinkCommand.Update.class})
public class LinkCommand {

    @ParentCommand
    private Main main;

    @Command(name = "create", description = "Create a new link")
    public static class Create implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Project name or ID")
        private String projectName;

        @CommandLine.Parameters(description = "Node A ID")
        private String nodeA;

        @CommandLine.Parameters(description = "Node B ID")
        private String nodeB;

        @CommandLine.Option(names = {"--adapter-a"}, description = "Adapter number for node A")
        private int adapterA = 0;

        @CommandLine.Option(names = {"--port-a"}, description = "Port number for node A")
        private int portA = 0;

        @CommandLine.Option(names = {"--adapter-b"}, description = "Adapter number for node B")
        private int adapterB = 0;

        @CommandLine.Option(names = {"--port-b"}, description = "Port number for node B")
        private int portB = 0;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object linkData = new Object() {
                    public final String node_a = nodeA;
                    public final String node_b = nodeB;
                    public final int adapter_a = adapterA;
                    public final int port_a = portA;
                    public final int adapter_b = adapterB;
                    public final int port_b = portB;
                };

                String response = client.postRaw("/v3/projects/" + projectName + "/links", linkData);
                System.out.println("Created link between " + nodeA + " and " + nodeB);

                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to create link: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "delete", description = "Delete a link")
    public static class Delete implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Project name or ID")
        private String projectName;

        @CommandLine.Parameters(description = "Link ID")
        private String linkId;

        @CommandLine.Option(names = {"--confirm"}, description = "Skip confirmation prompt")
        private boolean confirm;

        @Override
        public Integer call() throws Exception {
            try {
                if (!confirm) {
                    System.out.print("Are you sure you want to delete link '" + linkId + "'? (y/N): ");
                    String response = System.console().readLine();
                    if (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("yes")) {
                        System.out.println("Operation cancelled.");
                        return 0;
                    }
                }

                GNS3Client client = main.getAuthenticatedClient();
                client.delete("/v3/projects/" + projectName + "/links/" + linkId, String.class);

                System.out.println("Deleted link: " + linkId);
                return 0;
            } catch (IOException e) {
                System.err.println("Failed to delete link: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "list", description = "List all links in a project")
    public static class List implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Project name or ID")
        private String projectName;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/projects/" + projectName + "/links");

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode links = mapper.readTree(response);

                    System.out.println("Links in project '" + projectName + "':");
                    for (JsonNode link : links) {
                        String linkId = link.get("link_id").asText();
                        String nodeA = link.get("node_a").asText();
                        String nodeB = link.get("node_b").asText();
                        System.out.println("  " + linkId + ": " + nodeA + " <-> " + nodeB);
                    }
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to list links: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "get", description = "Get information about a specific link")
    public static class Get implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Project name or ID")
        private String projectName;

        @CommandLine.Parameters(description = "Link ID")
        private String linkId;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/projects/" + projectName + "/links/" + linkId);

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode link = mapper.readTree(response);

                    System.out.println("Link Information:");
                    System.out.println("  ID: " + link.get("link_id").asText());
                    System.out.println("  Node A: " + link.get("node_a").asText());
                    System.out.println("  Node B: " + link.get("node_b").asText());
                    System.out.println("  Status: " + link.get("status").asText());
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to get link info: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "update", description = "Update a link")
    public static class Update implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Project name or ID")
        private String projectName;

        @CommandLine.Parameters(description = "Link ID")
        private String linkId;

        @CommandLine.Option(names = {"--suspend"}, description = "Suspend the link")
        private boolean suspend;

        @CommandLine.Option(names = {"--resume"}, description = "Resume the link")
        private boolean resume;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object updateData = new Object() {
                    public final boolean suspend = suspend;
                    public final boolean resume = resume;
                };

                String response = client.put("/v3/projects/" + projectName + "/links/" + linkId, updateData, String.class);

                System.out.println("Updated link: " + linkId);
                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to update link: " + e.getMessage());
                return 1;
            }
        }
    }
}
