package util;

import java.io.Console;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for interactive operations and path handling
 */
public class InteractiveUtils {

    /**
     * Get user input for credentials interactively
     */
    public static Credentials getLoginCredentials() {
        Console console = System.console();
        if (console == null) {
            System.err.println("No console available for interactive input");
            return null;
        }

        String username = console.readLine("Username: ");
        String password = new String(console.readPassword("Password: "));

        return new Credentials(username, password);
    }

    /**
     * Get confirmation from user
     */
    public static boolean getConfirmation(String message) {
        Console console = System.console();
        if (console == null) {
            return false;
        }

        System.out.print(message + " (y/N): ");
        String response = console.readLine();
        return response.equalsIgnoreCase("y") || response.equalsIgnoreCase("yes");
    }

    /**
     * Get GNS3 directory path
     */
    public static String getGns3Dir() {
        return System.getProperty("user.home") + File.separator + ".gns3";
    }

    /**
     * Expand path with ~ to user home
     */
    public static String expandPath(String path) {
        if (path.startsWith("~")) {
            return System.getProperty("user.home") + path.substring(1);
        }
        return path;
    }

    /**
     * Credentials class for username/password
     */
    public static class Credentials {
        private final String username;
        private final String password;

        public Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}
