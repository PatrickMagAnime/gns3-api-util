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
 * ACL management commands
 */
@Command(name = "acl", description = "ACL operations",
         subcommands = {AclCommand.Create.class, AclCommand.Delete.class, AclCommand.List.class, AclCommand.Get.class, AclCommand.Update.class})
public class AclCommand {

    @ParentCommand
    private Main main;

    @Command(name = "create", description = "Create a new ACL")
    public static class Create implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "ACL name")
        private String aclName;

        @CommandLine.Option(names = {"--description"}, description = "ACL description")
        private String description;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object aclData = new Object() {
                    public final String name = aclName;
                    public final String description = description;
                };

                String response = client.postRaw("/v3/acls", aclData);
                System.out.println("Created ACL: " + aclName);

                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to create ACL: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "delete", description = "Delete an ACL")
    public static class Delete implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "ACL name")
        private String aclName;

        @CommandLine.Option(names = {"--confirm"}, description = "Skip confirmation prompt")
        private boolean confirm;

        @Override
        public Integer call() throws Exception {
            try {
                if (!confirm) {
                    System.out.print("Are you sure you want to delete ACL '" + aclName + "'? (y/N): ");
                    String response = System.console().readLine();
                    if (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("yes")) {
                        System.out.println("Operation cancelled.");
                        return 0;
                    }
                }

                GNS3Client client = main.getAuthenticatedClient();
                client.delete("/v3/acls/" + aclName, String.class);

                System.out.println("Deleted ACL: " + aclName);
                return 0;
            } catch (IOException e) {
                System.err.println("Failed to delete ACL: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "list", description = "List all ACLs")
    public static class List implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/acls");

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode acls = mapper.readTree(response);

                    System.out.println("ACLs:");
                    for (JsonNode acl : acls) {
                        String name = acl.get("name").asText();
                        String description = acl.get("description").asText();
                        System.out.println("  " + name + " - " + description);
                    }
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to list ACLs: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "get", description = "Get information about a specific ACL")
    public static class Get implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "ACL name")
        private String aclName;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/acls/" + aclName);

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode acl = mapper.readTree(response);

                    System.out.println("ACL Information:");
                    System.out.println("  Name: " + acl.get("name").asText());
                    System.out.println("  Description: " + acl.get("description").asText());
                    System.out.println("  Rules: " + acl.get("rule_count").asInt());
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to get ACL info: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "update", description = "Update an ACL rule")
    public static class Update implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "ACL name")
        private String aclName;

        @CommandLine.Parameters(description = "Rule ID")
        private String ruleId;

        @CommandLine.Option(names = {"--action"}, description = "Action (permit/deny)")
        private String action;

        @CommandLine.Option(names = {"--source"}, description = "Source network")
        private String source;

        @CommandLine.Option(names = {"--destination"}, description = "Destination network")
        private String destination;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();

                Object updateData = new Object() {
                    public final String action = action;
                    public final String source = source;
                    public final String destination = destination;
                };

                String response = client.put("/v3/acls/" + aclName + "/rules/" + ruleId, updateData, String.class);

                System.out.println("Updated ACL rule: " + ruleId + " in ACL: " + aclName);
                if (main.raw) {
                    System.out.println(response);
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to update ACL rule: " + e.getMessage());
                return 1;
            }
        }
    }
}
