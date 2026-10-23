// Libraries to handle communication, network addresses and keyboard input.
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class HangmanClient {

    // Server IP address: localhost refers to the local computer.
    private static final String SERVER_IP = "localhost";

    // Port used to connect to the server.
    private static final int SERVER_PORT = 12345;

    // Main method: starts the client and handles the game.
    public static void main(String[] args) {
        System.out.println("[CLIENT] Connecting to server...");

        // Creates the socket used for the connection to the server.
        try (Socket socket = new Socket()) {

            // Establishes the connection using IP address and port.
            socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));

            // Prepares the streams to receive and send data and to read the keyboard.
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    Scanner keyboard = new Scanner(System.in)) {

                // Clears the screen before showing the information.
                clearScreenInstantaneous();

                // Shows an initial message while waiting for the game.
                System.out.println("=================================");
                System.out.println("[WAITING] The server might be busy.");
                System.out.println("You are in the waiting queue, the game will start shortly...");
                System.out.println("=================================");

                String serverResponse;

                // Keeps reading the messages received from the server.
                while ((serverResponse = in.readLine()) != null) {

                    // Handles the messages containing the game state.
                    if (serverResponse.startsWith("STATE:")) {

                        // Clears the screen before updating the display.
                        clearScreenInstantaneous();

                        // Removes the STATE: prefix and splits the data using the | character.
                        String data = serverResponse.substring(6);
                        String[] parts = data.split("\\|", -1);

                        // Ignores the messages that do not contain all the expected data.
                        if (parts.length < 4)
                            continue;

                        // Extracts the message, the displayed word and the remaining lives.
                        String message = parts[0];
                        String displayedWord = parts[1];
                        String lives = parts[2];

                        // Shows the updated game state.
                        System.out.println("=================================");
                        if (!message.isEmpty()) {
                            System.out.println(message);
                        }
                        System.out.println("Current word:    " + formatWord(displayedWord));
                        System.out.println("Lives remaining: " + lives);
                        System.out.println("=================================");

                        // Asks the player to enter a letter or the whole word.
                        System.out.print("Enter a letter or the whole word: ");
                        String move = keyboard.nextLine();

                        // Sends the attempt to the server, handling empty inputs as well.
                        if (move.trim().isEmpty()) {
                            out.println("EMPTY");
                        } else {
                            out.println(move);
                        }

                    // Handles the end-of-game message sent by the server.
                    } else if (serverResponse.startsWith("END:")) {

                        // Clears the screen and shows the final result.
                        clearScreenInstantaneous();
                        System.out.println("=================================");
                        System.out.println("            GAME OVER            ");
                        System.out.println("=================================");
                        System.out.println(serverResponse.substring(4));
                        System.out.println("=================================");

                        // Breaks the loop because the game is over.
                        break;
                    }
                }

            } catch (IOException e) {
                // Handles errors during the communication with the server.
                System.err.println("\n[CLIENT] Communication error with the server.");
            }

        } catch (IOException e) {
            // Handles errors that prevent the connection from being established.
            System.err.println("[CLIENT] Unable to reach the server. Make sure it is running.");
        }
    }

    // Formats the word adding spaces between the letters to make it readable.
    private static String formatWord(String word) {
        StringBuilder sb = new StringBuilder();

        // Examines every character of the word.
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);

            // Keeps a wider separation between words.
            if (c == ' ') {
                sb.append("   ");
            } else {

                // Appends the character followed by a space.
                sb.append(c).append(" ");
            }
        }

        // Returns the string after removing the leading and trailing spaces.
        return sb.toString().trim();
    }

    // Clears the console to make the game state more readable.
    private static void clearScreenInstantaneous() {
        try {

            // Detects the operating system in use.
            String os = System.getProperty("os.name").toLowerCase();

            // On Windows it runs the cls command.
            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {

                // On other systems it runs the clear command.
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }

            // Uses ANSI sequences to reposition the cursor and clear the screen.
            System.out.print("\033[H\033[2J");
            System.out.flush();

        } catch (Exception e) {

            // If the clear command fails, it still tries with the ANSI sequences.
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }
}
