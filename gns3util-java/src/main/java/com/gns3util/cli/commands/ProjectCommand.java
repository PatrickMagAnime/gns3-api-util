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
 * Project management commands
 */
@Command(name = "project", description = "Project operations",
         subcommands = {ProjectCommand.Create.class, ProjectCommand.Delete.class, ProjectCommand.List.class, ProjectCommand.Duplicate.class})
public class ProjectCommand {

    @ParentCommand
    private Main main;

    @Command(name = "create", description = "Create a new project")
    public static class Create implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Project name")
        private String projectName;

        @CommandLine.Option(names = {"--auto-close"}, description = "Auto-close project after creation")
        private boolean autoClose;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object projectData = new Object() {
                    public final String name = projectName;
                    public final boolean auto_close = autoClose;
                };

                String response = client.postRaw("/v3/projects", projectData);
                System.out.println("Created project: " + projectName);

                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to create project: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "delete", description = "Delete a project")
    public static class Delete implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Project name or ID to delete")
        private String projectName;

        @CommandLine.Option(names = {"--confirm"}, description = "Skip confirmation prompt")
        private boolean confirm;

        @Override
        public Integer call() throws Exception {
            try {
                if (!confirm) {
                    System.out.print("Are you sure you want to delete project '" + projectName + "'? (y/N): ");
                    String response = System.console().readLine();
                    if (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("yes")) {
                        System.out.println("Operation cancelled.");
                        return 0;
                    }
                }

                GNS3Client client = main.getAuthenticatedClient();
                String response = client.delete("/v3/projects/" + projectName, String.class);

                System.out.println("Deleted project: " + projectName);
                return 0;
            } catch (IOException e) {
                System.err.println("Failed to delete project: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "list", description = "List all projects")
    public static class List implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/projects");

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode projects = mapper.readTree(response);

                    System.out.println("Projects:");
                    for (JsonNode project : projects) {
                        String name = project.get("name").asText();
                        String status = project.get("status").asText();
                        System.out.println("  " + name + " (" + status + ")");
                    }
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to list projects: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "duplicate", description = "Duplicate an existing project")
    public static class Duplicate implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Source project name or ID")
        private String sourceProject;

        @CommandLine.Parameters(description = "New project name")
        private String newProjectName;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object duplicateData = new Object() {
                    public final String name = newProjectName;
                };

                String response = client.postRaw("/v3/projects/" + sourceProject + "/duplicate", duplicateData);
                System.out.println("Duplicated project '" + sourceProject + "' to '" + newProjectName + "'");

                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to duplicate project: " + e.getMessage());
                return 1;
            }
        }
    }
}
