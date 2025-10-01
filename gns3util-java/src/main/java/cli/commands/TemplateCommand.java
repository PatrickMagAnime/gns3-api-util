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
 * Template management commands
 */
@Command(name = "template", description = "Template operations",
         subcommands = {TemplateCommand.Create.class, TemplateCommand.Delete.class, TemplateCommand.List.class, TemplateCommand.Get.class, TemplateCommand.Duplicate.class})
public class TemplateCommand {

    @ParentCommand
    private Main main;

    @Command(name = "create", description = "Create a new template")
    public static class Create implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Template name")
        private String templateName;

        @CommandLine.Parameters(description = "Project name or ID to use as template")
        private String projectName;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object templateData = new Object() {
                    public final String name = templateName;
                    public final String project_id = projectName;
                };

                String response = client.postRaw("/v3/templates", templateData);
                System.out.println("Created template: " + templateName + " from project: " + projectName);

                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to create template: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "delete", description = "Delete a template")
    public static class Delete implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Template name or ID")
        private String templateName;

        @CommandLine.Option(names = {"--confirm"}, description = "Skip confirmation prompt")
        private boolean confirm;

        @Override
        public Integer call() throws Exception {
            try {
                if (!confirm) {
                    System.out.print("Are you sure you want to delete template '" + templateName + "'? (y/N): ");
                    String response = System.console().readLine();
                    if (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("yes")) {
                        System.out.println("Operation cancelled.");
                        return 0;
                    }
                }

                GNS3Client client = main.getAuthenticatedClient();
                client.delete("/v3/templates/" + templateName, String.class);

                System.out.println("Deleted template: " + templateName);
                return 0;
            } catch (IOException e) {
                System.err.println("Failed to delete template: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "list", description = "List all templates")
    public static class List implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/templates");

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode templates = mapper.readTree(response);

                    System.out.println("Templates:");
                    for (JsonNode template : templates) {
                        String name = template.get("name").asText();
                        String projectId = template.get("project_id").asText();
                        System.out.println("  " + name + " (Project: " + projectId + ")");
                    }
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to list templates: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "get", description = "Get information about a specific template")
    public static class Get implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Template name or ID")
        private String templateName;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/templates/" + templateName);

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode template = mapper.readTree(response);

                    System.out.println("Template Information:");
                    System.out.println("  Name: " + template.get("name").asText());
                    System.out.println("  Project ID: " + template.get("project_id").asText());
                    System.out.println("  Status: " + template.get("status").asText());
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to get template info: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "duplicate", description = "Duplicate a template")
    public static class Duplicate implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Source template name or ID")
        private String sourceTemplate;

        @CommandLine.Parameters(description = "New template name")
        private String newTemplateName;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object duplicateData = new Object() {
                    public final String name = newTemplateName;
                };

                String response = client.postRaw("/v3/templates/" + sourceTemplate + "/duplicate", duplicateData);
                System.out.println("Duplicated template '" + sourceTemplate + "' to '" + newTemplateName + "'");

                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to duplicate template: " + e.getMessage());
                return 1;
            }
        }
    }
}
