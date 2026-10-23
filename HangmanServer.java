// Libraries to handle input/output, files and data streams.
import java.io.*;

// Library for network communication through sockets.
import java.net.*;

// Library to use dynamic lists.
import java.util.ArrayList;
import java.util.List;

// Library to generate random numbers.
import java.util.Random;

public class HangmanServer {

    // Port used by the server to accept connections.
    private static final int PORT = 12345;

    // Name of the file containing the game words.
    private static final String WORD_FILE = "word.txt";

    // Maximum number of mistakes allowed for the player.
    private static final int MAX_LIVES = 6;

    // Main method: starts the server and handles the games.
    public static void main(String[] args) {
        System.out.println("[SERVER] Starting up...");

        // Creates the words file if it does not exist.
        createFileIfNotExists();

        // Opens the server on the given port and closes the socket at the end.
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] Listening on port " + PORT + ". Waiting for clients...\n");

            // Keeps the server alive to accept new clients.
            while (true) {

                // Accepts a client and prepares the streams to receive and send data.
                try (Socket clientSocket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    System.out.println("[SERVER] Client connected from: " + clientSocket.getRemoteSocketAddress());

                    // Loads from the file the list of available words.
                    List<String> dictionary = loadWordsFromFile();
                    System.out.println("[SERVER] Dictionary loaded: " + dictionary.size() + " words detected.");

                    // If there are no words, reports the error and ends the client handling.
                    if (dictionary.isEmpty()) {
                        out.println("END:Server error: the words dictionary is empty!");
                        continue;
                    }

                    // Picks a random word and converts it to upper case.
                    String secretWord = selectWord(dictionary).toUpperCase();

                    // Builds a character sequence showing the spaces and hiding the letters.
                    char[] currentWord = new char[secretWord.length()];
                    for (int i = 0; i < currentWord.length; i++) {
                        if (secretWord.charAt(i) == ' ') {
                            currentWord[i] = ' ';
                        } else {
                            currentWord[i] = '_';
                        }
                    }

                    // Initializes the lives, the attempts, the win state and the message.
                    int livesRemaining = MAX_LIVES;
                    List<String> triedLetters = new ArrayList<>();
                    boolean wordFullyGuessed = false;
                    String lastMessage = "Welcome to the Hangman game!";

                    // Keeps going while there are lives left, the word has not been guessed
                    // as a whole and not all the letters have been discovered yet.
                    while (livesRemaining > 0 && !wordFullyGuessed && !isWordComplete(currentWord)) {

                        // Sends to the client the message, the visible word and the remaining lives.
                        out.println("STATE:" + lastMessage + "|" + String.valueOf(currentWord) + "|"
                                + livesRemaining + "|END");

                        // Reads the attempt sent by the client.
                        String clientInput = in.readLine();
                        if (clientInput == null)
                            break;

                        // Removes the leading and trailing spaces and converts the input to upper case.
                        clientInput = clientInput.trim().toUpperCase();

                        // Handles empty or invalid inputs.
                        if (clientInput.isEmpty() || clientInput.equals("EMPTY")) {
                            lastMessage = "Invalid input!";
                            continue;
                        }

                        // If the input contains more than one character, it is treated as a word.
                        if (clientInput.length() > 1) {

                            // If the word matches the secret one, the player wins.
                            if (clientInput.equals(secretWord)) {
                                wordFullyGuessed = true;
                                lastMessage = "Correct word!";
                            } else {

                                // A wrong word costs one life.
                                livesRemaining--;
                                lastMessage = "Wrong! The word is not '" + clientInput + "'.";
                            }
                        } else {

                            // Extracts the letter entered by the client.
                            String letterStr = clientInput;
                            char letter = letterStr.charAt(0);

                            // Prevents repeating a letter that has already been tried.
                            if (triedLetters.contains(letterStr)) {
                                lastMessage = "Letter already tried!";
                                continue;
                            }

                            // Registers the letter among the tried attempts.
                            triedLetters.add(letterStr);

                            // Checks whether the letter is present in the secret word.
                            boolean guessed = false;
                            for (int i = 0; i < secretWord.length(); i++) {
                                if (secretWord.charAt(i) == letter) {

                                    // Reveals the letter in the correct position.
                                    currentWord[i] = letter;
                                    guessed = true;
                                }
                            }

                            // Updates the message and the lives according to the result.
                            if (guessed) {
                                lastMessage = "Great! The letter '" + letter + "' is correct.";
                            } else {
                                livesRemaining--;
                                lastMessage = "Wrong! The letter '" + letter + "' is not present.";
                            }
                        }
                    }

                    // Tells the client the final result of the game.
                    if (wordFullyGuessed || isWordComplete(currentWord)) {
                        out.println("END:YOU WIN! The word was: " + secretWord);
                        System.out.println("[SERVER] The client has WON.");
                    } else {
                        out.println("END:YOU LOSE! The word was: " + secretWord);
                        System.out.println("[SERVER] The client has LOST.");
                    }

                    // Signals the end of the game and waits for a new client.
                    System.out.println("[SERVER] Game over. Waiting for a new client...\n");

                } catch (IOException e) {
                    // Handles the errors occurred during the communication with the client.
                    System.err.println("[SERVER] Communication error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            // Handles the errors occurred while creating or starting the server.
            System.err.println("[SERVER] ServerSocket error: " + e.getMessage());
        }
    }

    // Reads the file and returns a list containing the non-empty words.
    private static List<String> loadWordsFromFile() {
        List<String> words = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(WORD_FILE))) {
            String line;

            // Reads the file one line at a time until the end.
            while ((line = br.readLine()) != null) {
                line = line.trim();

                // Adds to the list only the non-empty lines.
                if (!line.isEmpty())
                    words.add(line);
            }
        } catch (IOException e) {
            // Reports any error occurred while reading the file.
            System.err.println("[SERVER] File reading error: " + e.getMessage());
        }
        return words;
    }

    // Creates the words file with a few examples, only if it does not exist yet.
    private static void createFileIfNotExists() {
        File file = new File(WORD_FILE);
        if (!file.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {

                // Inserts a few initial words into the dictionary.
                pw.println("java programming");
                pw.println("single client socket");
                pw.println("computer science");
                pw.println("local network");
            } catch (IOException e) {
                // Reports a possible error while creating the text file.
                System.err.println("[SERVER] Error creating the text file.");
            }
        }
    }

    // Returns a word chosen randomly from the received list.
    private static String selectWord(List<String> list) {
        return list.get(new Random().nextInt(list.size()));
    }

    // Returns true if no hidden letters are left in the word.
    private static boolean isWordComplete(char[] word) {
        for (char c : word) {

            // If it finds an underscore, the word is not complete yet.
            if (c == '_')
                return false;
        }

        // If there are no underscores, all the letters have been guessed.
        return true;
    }
}
