// Librerie per gestire la comunicazione, gli indirizzi di rete e l'input da tastiera.
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientImpiccato {

    // Indirizzo IP del server: localhost indica il computer locale.
    private static final String SERVER_IP = "localhost";

    // Porta utilizzata per connettersi al server.
    private static final int SERVER_PORT = 12345;

    // Metodo principale: avvia il client e gestisce la partita.
    public static void main(String[] args) {
        System.out.println("[CLIENT] Connessione al server...");

        // Crea il socket utilizzato per la connessione al server.
        try (Socket socket = new Socket()) {

            // Stabilisce la connessione usando indirizzo IP e porta.
            socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));

            // Prepara i flussi per ricevere e inviare dati e leggere la tastiera.
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    Scanner tastiera = new Scanner(System.in)) {

                // Pulisce lo schermo prima di mostrare le informazioni.
                pulisciSchermoIstantaneo();

                // Mostra un messaggio iniziale mentre si attende la partita.
                System.out.println("=================================");
                System.out.println("[ATTESA] Il server potrebbe essere occupato.");
                System.out.println("Sei in coda d'attesa, la partita inizierà a breve...");
                System.out.println("=================================");

                String rispostaServer;

                // Legge continuamente i messaggi ricevuti dal server.
                while ((rispostaServer = in.readLine()) != null) {

                    // Gestisce i messaggi contenenti lo stato della partita.
                    if (rispostaServer.startsWith("STATO:")) {

                        // Pulisce lo schermo prima di aggiornare la visualizzazione.
                        pulisciSchermoIstantaneo();

                        // Rimuove il prefisso STATO: e divide i dati usando il carattere |.
                        String dati = rispostaServer.substring(6);
                        String[] parti = dati.split("\\|", -1);

                        // Ignora i messaggi che non contengono tutti i dati previsti.
                        if (parti.length < 4)
                            continue;

                        // Estrae il messaggio, la parola visualizzata e le vite rimaste.
                        String messaggio = parti[0];
                        String parolaVisualizzata = parti[1];
                        String vite = parti[2];

                        // Mostra lo stato aggiornato della partita.
                        System.out.println("=================================");
                        if (!messaggio.isEmpty()) {
                            System.out.println(messaggio);
                        }
                        System.out.println("Parola attuale: " + formattaParola(parolaVisualizzata));
                        System.out.println("Vite rimaste:   " + vite);
                        System.out.println("=================================");

                        // Chiede al giocatore di inserire una lettera o la parola intera.
                        System.out.print("Inserisci una lettera o la parola intera: ");
                        String mossa = tastiera.nextLine();

                        // Invia al server il tentativo, gestendo anche gli input vuoti.
                        if (mossa.trim().isEmpty()) {
                            out.println("VUOTO");
                        } else {
                            out.println(mossa);
                        }

                    // Gestisce il messaggio di fine partita inviato dal server.
                    } else if (rispostaServer.startsWith("FINE:")) {

                        // Pulisce lo schermo e mostra il risultato finale.
                        pulisciSchermoIstantaneo();
                        System.out.println("=================================");
                        System.out.println("         PARTITA TERMINATA       ");
                        System.out.println("=================================");
                        System.out.println(rispostaServer.substring(5));
                        System.out.println("=================================");

                        // Interrompe il ciclo perché la partita è terminata.
                        break;
                    }
                }

            } catch (IOException e) {
                // Gestisce gli errori durante la comunicazione con il server.
                System.err.println("\n[CLIENT] Errore di comunicazione con il server.");
            }

        } catch (IOException e) {
            // Gestisce gli errori che impediscono di stabilire la connessione.
            System.err.println("[CLIENT] Impossibile raggiungere il server. Assicurati che sia avviato.");
        }
    }

    // Formatta la parola aggiungendo spazi tra le lettere per renderla leggibile.
    private static String formattaParola(String parola) {
        StringBuilder sb = new StringBuilder();

        // Esamina ogni carattere della parola.
        for (int i = 0; i < parola.length(); i++) {
            char c = parola.charAt(i);

            // Mantiene una separazione maggiore tra le parole.
            if (c == ' ') {
                sb.append("   ");
            } else {

                // Aggiunge il carattere seguito da uno spazio.
                sb.append(c).append(" ");
            }
        }

        // Restituisce la stringa eliminando gli spazi iniziali e finali.
        return sb.toString().trim();
    }

    // Pulisce la console per rendere più leggibile lo stato della partita.
    private static void pulisciSchermoIstantaneo() {
        try {

            // Rileva il sistema operativo utilizzato.
            String os = System.getProperty("os.name").toLowerCase();

            // Su Windows esegue il comando cls.
            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {

                // Sugli altri sistemi esegue il comando clear.
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }

            // Usa sequenze ANSI per riposizionare il cursore e pulire lo schermo.
            System.out.print("\033[H\033[2J");
            System.out.flush();

        } catch (Exception e) {

            // Se il comando di pulizia fallisce, prova comunque con le sequenze ANSI.
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }
}