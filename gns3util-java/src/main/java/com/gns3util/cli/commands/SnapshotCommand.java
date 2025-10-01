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
 * Snapshot management commands
 */
@Command(name = "snapshot", description = "Snapshot operations",
         subcommands = {SnapshotCommand.Create.class, SnapshotCommand.Delete.class, SnapshotCommand.List.class})
public class SnapshotCommand {

    @ParentCommand
    private Main main;

    @Command(name = "create", description = "Create a snapshot")
    public static class Create implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Project name or ID")
        private String projectName;

        @CommandLine.Parameters(description = "Snapshot name")
        private String snapshotName;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object snapshotData = new Object() {
                    public final String name = snapshotName;
                };

                String response = client.postRaw("/v3/projects/" + projectName + "/snapshots", snapshotData);
                System.out.println("Created snapshot: " + snapshotName + " for project: " + projectName);

                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to create snapshot: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "delete", description = "Delete a snapshot")
    public static class Delete implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Project name or ID")
        private String projectName;

        @CommandLine.Parameters(description = "Snapshot name")
        private String snapshotName;

        @CommandLine.Option(names = {"--confirm"}, description = "Skip confirmation prompt")
        private boolean confirm;

        @Override
        public Integer call() throws Exception {
            try {
                if (!confirm) {
                    System.out.print("Are you sure you want to delete snapshot '" + snapshotName + "'? (y/N): ");
                    String response = System.console().readLine();
                    if (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("yes")) {
                        System.out.println("Operation cancelled.");
                        return 0;
                    }
                }

                GNS3Client client = main.getAuthenticatedClient();
                client.delete("/v3/projects/" + projectName + "/snapshots/" + snapshotName, String.class);

                System.out.println("Deleted snapshot: " + snapshotName + " from project: " + projectName);
                return 0;
            } catch (IOException e) {
                System.err.println("Failed to delete snapshot: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "list", description = "List all snapshots for a project")
    public static class List implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Project name or ID")
        private String projectName;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/projects/" + projectName + "/snapshots");

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode snapshots = mapper.readTree(response);

                    System.out.println("Snapshots for project '" + projectName + "':");
                    for (JsonNode snapshot : snapshots) {
                        String name = snapshot.get("name").asText();
                        String created = snapshot.get("created_at").asText();
                        System.out.println("  " + name + " (created: " + created + ")");
                    }
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to list snapshots: " + e.getMessage());
                return 1;
            }
        }
    }
}
