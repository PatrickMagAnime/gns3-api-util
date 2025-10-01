package cli.commands;

import cli.Main;
import model.Credentials;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Authentication command group
 */
@Command(name = "auth", description = "Authentication commands",
         subcommands = {AuthCommand.Login.class, AuthCommand.Status.class})
public class AuthCommand {

    @ParentCommand
    private Main main;

    @Command(name = "login", description = "Log in as user")
    public static class Login implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Option(names = {"-u", "--user"}, description = "Username")
        private String username;

        @CommandLine.Option(names = {"-p", "--password"}, description = "Password")
        private String password;

        @Override
        public Integer call() throws Exception {
            try {
                Credentials creds = null;

                // If credentials not provided via flags, get them interactively
                if (username == null || password == null) {
                    creds = main.getCredentialsInteractively();
                    if (creds == null) {
                        System.err.println("Failed to get credentials");
                        return 1;
                    }
                } else {
                    creds = new com.gns3util.model.Credentials(username, password);
                }

                main.authenticate(creds.getUsername(), creds.getPassword());
                return 0;

            } catch (IOException e) {
                System.err.println("Authentication failed: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "status", description = "Check authentication status")
    public static class Status implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @Override
        public Integer call() throws Exception {
            try {
                main.getAuthenticatedClient();
                System.out.println("Authenticated to " + main.server);
                return 0;
            } catch (IOException e) {
                System.out.println("Not authenticated: " + e.getMessage());
                return 1;
            }
        }
    }
}
