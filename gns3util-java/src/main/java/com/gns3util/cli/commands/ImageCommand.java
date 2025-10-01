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
 * Image management commands
 */
@Command(name = "image", description = "Image operations",
         subcommands = {ImageCommand.List.class, ImageCommand.Get.class, ImageCommand.Delete.class, ImageCommand.Prune.class})
public class ImageCommand {

    @ParentCommand
    private Main main;

    @Command(name = "list", description = "List all images")
    public static class List implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/images");

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode images = mapper.readTree(response);

                    System.out.println("Images:");
                    for (JsonNode image : images) {
                        String filename = image.get("filename").asText();
                        String path = image.get("path").asText();
                        String size = image.get("file_size").asText();
                        System.out.println("  " + filename + " (" + size + ") - " + path);
                    }
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to list images: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "get", description = "Get information about a specific image")
    public static class Get implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Image ID")
        private String imageId;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/images/" + imageId);

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode image = mapper.readTree(response);

                    System.out.println("Image Information:");
                    System.out.println("  Filename: " + image.get("filename").asText());
                    System.out.println("  Path: " + image.get("path").asText());
                    System.out.println("  Size: " + image.get("file_size").asText());
                    System.out.println("  MD5: " + image.get("md5sum").asText());
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to get image info: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "delete", description = "Delete an image")
    public static class Delete implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Image ID")
        private String imageId;

        @CommandLine.Option(names = {"--confirm"}, description = "Skip confirmation prompt")
        private boolean confirm;

        @Override
        public Integer call() throws Exception {
            try {
                if (!confirm) {
                    System.out.print("Are you sure you want to delete image '" + imageId + "'? (y/N): ");
                    String response = System.console().readLine();
                    if (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("yes")) {
                        System.out.println("Operation cancelled.");
                        return 0;
                    }
                }

                GNS3Client client = main.getAuthenticatedClient();
                client.delete("/v3/images/" + imageId, String.class);

                System.out.println("Deleted image: " + imageId);
                return 0;
            } catch (IOException e) {
                System.err.println("Failed to delete image: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "prune", description = "Remove unused images")
    public static class Prune implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.delete("/v3/images", String.class);

                System.out.println("Pruned unused images");
                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to prune images: " + e.getMessage());
                return 1;
            }
        }
    }
}
