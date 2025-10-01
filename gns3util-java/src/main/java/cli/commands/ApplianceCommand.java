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
 * Appliance management commands
 */
@Command(name = "appliance", description = "Appliance operations",
         subcommands = {ApplianceCommand.List.class, ApplianceCommand.Get.class})
public class ApplianceCommand {

    @ParentCommand
    private Main main;

    @Command(name = "list", description = "List all appliances")
    public static class List implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/appliances");

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode appliances = mapper.readTree(response);

                    System.out.println("Appliances:");
                    for (JsonNode appliance : appliances) {
                        String name = appliance.get("name").asText();
                        String category = appliance.get("category").asText();
                        String description = appliance.get("description").asText();
                        System.out.println("  " + name + " (" + category + ") - " + description);
                    }
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to list appliances: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "get", description = "Get information about a specific appliance")
    public static class Get implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Appliance ID")
        private String applianceId;

        @Override
        public Integer call() throws Exception {
            try {
                GNS3Client client = main.getAuthenticatedClient();
                String response = client.getRaw("/v3/appliances/" + applianceId);

                if (main.raw) {
                    System.out.println(response);
                } else {
                    // Parse and format the response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode appliance = mapper.readTree(response);

                    System.out.println("Appliance Information:");
                    System.out.println("  Name: " + appliance.get("name").asText());
                    System.out.println("  Category: " + appliance.get("category").asText());
                    System.out.println("  Description: " + appliance.get("description").asText());
                    System.out.println("  Version: " + appliance.get("version").asText());
                }

                return 0;
            } catch (IOException e) {
                System.err.println("Failed to get appliance info: " + e.getMessage());
                return 1;
            }
        }
    }
}
