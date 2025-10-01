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
 * Drawing management commands
 */
@Command(name = "drawing", description = "Drawing operations",
         subcommands = {DrawingCommand.Create.class, DrawingCommand.Delete.class, DrawingCommand.List.class, DrawingCommand.Get.class, DrawingCommand.Update.class})
public class DrawingCommand {

    @ParentCommand
    private Main main;

    @Command(name = "create", description = "Create a new drawing")
    public static class Create implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Project name or ID")
        private String projectName;

        @CommandLine.Parameters(description = "Drawing name")
        private String drawingName;

        @CommandLine.Option(names = {"--x"}, description = "X coordinate")
        private int x = 0;

        @CommandLine.Option(names = {"--y"}, description = "Y coordinate")
        private int y = 0;

        @CommandLine.Option(names = {"--width"}, description = "Drawing width")
        private int width = 100;

        @CommandLine.Option(names = {"--height"}, description = "Drawing height")
        private int height = 100;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object drawingData = new Object() {
                    public final String name = drawingName;
                    public final int x = x;
                    public final int y = y;
                    public final int width = width;
                    public final int height = height;
                };

                String response = client.postRaw("/v3/projects/" + projectName + "/drawings", drawingData);
                System.out.println("Created drawing: " + drawingName + " in project: " + projectName);

                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to create drawing: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "delete", description = "Delete a drawing")
    public static class Delete implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Project name or ID")
        private String projectName;

        @CommandLine.Parameters(description = "Drawing ID")
        private String drawingId;

        @CommandLine.Option(names = {"--confirm"}, description = "Skip confirmation prompt")
        private boolean confirm;

        @Override
        public Integer call() throws Exception {
            try {
                if (!confirm) {
                    System.out.print("Are you sure you want to delete drawing '" + drawingId + "'? (y/N): ");
                    String response = System.console().readLine();
                    if (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("yes")) {
                        System.out.println("Operation cancelled.");
                        return 0;
                    }
                }

                GNS3Client client = main.getAuthenticatedClient();
                client.delete("/v3/projects/" + projectName + "/drawings/" + drawingId, String.class);

                System.out.println("Deleted drawing: " + drawingId + " from project: " + projectName);
                return 0;
            } catch (IOException e) {
                System.err.println("Failed to delete drawing: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "list", description = "List all drawings in a project")
    public static class List implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Project name or ID")
        private String projectName;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/projects/" + projectName + "/drawings");

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode drawings = mapper.readTree(response);

                    System.out.println("Drawings in project '" + projectName + "':");
                    for (JsonNode drawing : drawings) {
                        String name = drawing.get("name").asText();
                        int x = drawing.get("x").asInt();
                        int y = drawing.get("y").asInt();
                        System.out.println("  " + name + " at (" + x + "," + y + ")");
                    }
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to list drawings: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "get", description = "Get information about a specific drawing")
    public static class Get implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Project name or ID")
        private String projectName;

        @CommandLine.Parameters(description = "Drawing ID")
        private String drawingId;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/projects/" + projectName + "/drawings/" + drawingId);

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode drawing = mapper.readTree(response);

                    System.out.println("Drawing Information:");
                    System.out.println("  Name: " + drawing.get("name").asText());
                    System.out.println("  Position: (" + drawing.get("x").asInt() + "," + drawing.get("y").asInt() + ")");
                    System.out.println("  Size: " + drawing.get("width").asInt() + "x" + drawing.get("height").asInt());
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to get drawing info: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "update", description = "Update a drawing")
    public static class Update implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Project name or ID")
        private String projectName;

        @CommandLine.Parameters(description = "Drawing ID")
        private String drawingId;

        @CommandLine.Option(names = {"--x"}, description = "New X coordinate")
        private Integer x;

        @CommandLine.Option(names = {"--y"}, description = "New Y coordinate")
        private Integer y;

        @CommandLine.Option(names = {"--width"}, description = "New width")
        private Integer width;

        @CommandLine.Option(names = {"--height"}, description = "New height")
        private Integer height;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                // Build update data dynamically based on provided options
                Object updateData = new Object() {
                    public final Integer x = x;
                    public final Integer y = y;
                    public final Integer width = width;
                    public final Integer height = height;
                };

                String response = client.put("/v3/projects/" + projectName + "/drawings/" + drawingId, updateData, String.class);

                System.out.println("Updated drawing: " + drawingId);
                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to update drawing: " + e.getMessage());
                return 1;
            }
        }
    }
}
