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
 * Node management commands
 */
@Command(name = "node", description = "Node operations",
         subcommands = {NodeCommand.Create.class, NodeCommand.Delete.class, NodeCommand.List.class})
public class NodeCommand {

    @ParentCommand
    private Main main;

    @Command(name = "create", description = "Create a new node")
    public static class Create implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Project name or ID")
        private String projectName;

        @CommandLine.Parameters(description = "Node name")
        private String nodeName;

        @CommandLine.Option(names = {"--node-type"}, description = "Node type (e.g., qemu, vpcs)", required = true)
        private String nodeType;

        @CommandLine.Option(names = {"--compute-id"}, description = "Compute identifier")
        private String computeId;

        @CommandLine.Option(names = {"--template"}, description = "Template to use")
        private String template;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object nodeData = new Object() {
                    public final String name = nodeName;
                    public final String node_type = nodeType;
                    public final String compute_id = computeId != null ? computeId : "local";
                };

                String response = client.postRaw("/v3/projects/" + projectName + "/nodes", nodeData);
                System.out.println("Created node: " + nodeName + " in project: " + projectName);

                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to create node: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "delete", description = "Delete a node")
    public static class Delete implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Project name or ID")
        private String projectName;

        @CommandLine.Parameters(description = "Node ID")
        private String nodeId;

        @CommandLine.Option(names = {"--confirm"}, description = "Skip confirmation prompt")
        private boolean confirm;

        @Override
        public Integer call() throws Exception {
            try {
                if (!confirm) {
                    System.out.print("Are you sure you want to delete node '" + nodeId + "'? (y/N): ");
                    String response = System.console().readLine();
                    if (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("yes")) {
                        System.out.println("Operation cancelled.");
                        return 0;
                    }
                }

                GNS3Client client = main.getAuthenticatedClient();
                String response = client.delete("/v3/projects/" + projectName + "/nodes/" + nodeId, String.class);

                System.out.println("Deleted node: " + nodeId + " from project: " + projectName);
                return 0;
            } catch (IOException e) {
                System.err.println("Failed to delete node: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "list", description = "List nodes in a project")
    public static class List implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Project name or ID")
        private String projectName;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/projects/" + projectName + "/nodes");

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode nodes = mapper.readTree(response);

                    System.out.println("Nodes in project '" + projectName + "':");
                    for (JsonNode node : nodes) {
                        String name = node.get("name").asText();
                        String nodeType = node.get("node_type").asText();
                        String status = node.get("status").asText();
                        System.out.println("  " + name + " (" + nodeType + ") - " + status);
                    }
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to list nodes: " + e.getMessage());
                return 1;
            }
        }
    }
}
