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
 * Exercise management commands
 */
@Command(name = "exercise", description = "Exercise operations",
         subcommands = {ExerciseCommand.Create.class, ExerciseCommand.Delete.class, ExerciseCommand.List.class, ExerciseCommand.Info.class})
public class ExerciseCommand {

    @ParentCommand
    private Main main;

    @Command(name = "create", description = "Create an exercise (project) for every group in a class with ACLs")
    public static class Create implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Class name")
        private String className;

        @CommandLine.Parameters(description = "Exercise name")
        private String exerciseName;

        @CommandLine.Option(names = {"--template"}, description = "Template project to use")
        private String template;

        @CommandLine.Option(names = {"--select-template"}, description = "Interactively select template")
        private boolean selectTemplate;

        @CommandLine.Option(names = {"--format"}, description = "Project name format")
        private String format;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                // For now, create a simple exercise structure
                Object exerciseData = new Object() {
                    public final String name = exerciseName;
                    public final String class_name = className;
                };

                String response = client.postRaw("/v3/exercises", exerciseData);
                System.out.println("Created exercise: " + exerciseName + " for class: " + className);

                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to create exercise: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "delete", description = "Delete an exercise")
    public static class Delete implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Exercise name")
        private String exerciseName;

        @CommandLine.Option(names = {"--confirm"}, description = "Skip confirmation prompt")
        private boolean confirm;

        @Override
        public Integer call() throws Exception {
            try {
                if (!confirm) {
                    System.out.print("Are you sure you want to delete exercise '" + exerciseName + "'? (y/N): ");
                    String response = System.console().readLine();
                    if (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("yes")) {
                        System.out.println("Operation cancelled.");
                        return 0;
                    }
                }

                GNS3Client client = main.getAuthenticatedClient();
                client.delete("/v3/exercises/" + exerciseName, String.class);

                System.out.println("Deleted exercise: " + exerciseName);
                return 0;
            } catch (IOException e) {
                System.err.println("Failed to delete exercise: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "list", description = "List all exercises")
    public static class List implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/exercises");

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode exercises = mapper.readTree(response);

                    System.out.println("Exercises:");
                    for (JsonNode exercise : exercises) {
                        String name = exercise.get("name").asText();
                        String className = exercise.get("class_name").asText();
                        System.out.println("  " + name + " (Class: " + className + ")");
                    }
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to list exercises: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "info", description = "Get detailed information about an exercise")
    public static class Info implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Exercise name")
        private String exerciseName;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/exercises/" + exerciseName);

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode exercise = mapper.readTree(response);

                    System.out.println("Exercise Information:");
                    System.out.println("  Name: " + exercise.get("name").asText());
                    System.out.println("  Class: " + exercise.get("class_name").asText());
                    System.out.println("  Status: " + exercise.get("status").asText());
                    System.out.println("  Projects: " + exercise.get("project_count").asInt());
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to get exercise info: " + e.getMessage());
                return 1;
            }
        }
    }
}
