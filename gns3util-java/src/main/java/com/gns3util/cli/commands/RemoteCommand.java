package com.gns3util.cli.commands;

import com.gns3util.cli.Main;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

/**
 * Remote server management commands
 */
@Command(name = "remote", description = "Remote operations via SSH",
         subcommands = {RemoteCommand.Install.class, RemoteCommand.Uninstall.class})
public class RemoteCommand {

    @ParentCommand
    private Main main;

    @Command(name = "install", description = "Install HTTPS reverse proxy and firewall rules")
    public static class Install implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Service type (https)")
        private String serviceType;

        @CommandLine.Parameters(description = "Username")
        private String username;

        @CommandLine.Option(names = {"--domain"}, description = "Domain name")
        private String domain;

        @CommandLine.Option(names = {"--firewall-allow"}, description = "Firewall allowed networks")
        private String firewallAllow;

        @Override
        public Integer call() throws Exception {
            System.out.println("Installing " + serviceType + " for user " + username);

            if (domain != null) {
                System.out.println("Domain: " + domain);
            }

            if (firewallAllow != null) {
                System.out.println("Firewall allow: " + firewallAllow);
            }

            System.out.println("Installation completed (simulated)");
            return 0;
        }
    }

    @Command(name = "uninstall", description = "Remove HTTPS configuration")
    public static class Uninstall implements Callable<Integer> {

        @ParentCommand
        private Main main;

        @CommandLine.Parameters(description = "Service type (https)")
        private String serviceType;

        @CommandLine.Parameters(description = "Username")
        private String username;

        @Override
        public Integer call() throws Exception {
            System.out.println("Uninstalling " + serviceType + " for user " + username);
            System.out.println("Uninstallation completed (simulated)");
            return 0;
        }
    }
}
