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
 * Class management commands
 */
@Command(name = "class", description = "Class operations",
         subcommands = {ClassCommand.Create.class, ClassCommand.Delete.class, ClassCommand.List.class})
public class ClassCommand {

    @ParentCommand
    private Main main;

    @Command(name = "create", description = "Create a new class")
    public static class Create implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Class name")
        private String className;

        @CommandLine.Option(names = {"-f", "--file"}, description = "JSON file with class definition")
        private String file;

        @CommandLine.Option(names = {"--interactive"}, description = "Interactive class creation")
        private boolean interactive;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                // For now, create a simple class - in the future this could read from file or interactive input
                Object classData = new Object() {
                    public final String name = className;
                };

                String response = client.postRaw("/v3/classes", classData);

                if (main.raw) {
                    System.out.println(response);
                } else {
                    System.out.println("Created class: " + className);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to create class: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "delete", description = "Delete a class")
    public static class Delete implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Class name to delete")
        private String className;

        @CommandLine.Option(names = {"--confirm"}, description = "Skip confirmation prompt")
        private boolean confirm;

        @Override
        public Integer call() throws Exception {
            try {
                if (!confirm) {
                    System.out.print("Are you sure you want to delete class '" + className + "'? (y/N): ");
                    String response = System.console().readLine();
                    if (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("yes")) {
                        System.out.println("Operation cancelled.");
                        return 0;
                    }
                }

                GNS3Client client = main.getAuthenticatedClient();
                client.delete("/v3/classes/" + className, String.class);

                System.out.println("Deleted class: " + className);
                return 0;
            } catch (IOException e) {
                System.err.println("Failed to delete class: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "list", description = "List all classes")
    public static class List implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/classes");

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode classes = mapper.readTree(response);

                    System.out.println("Classes:");
                    for (JsonNode classNode : classes) {
                        String name = classNode.get("name").asText();
                        System.out.println("  " + name);
                    }
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to list classes: " + e.getMessage());
                return 1;
            }
        }
    }
}
