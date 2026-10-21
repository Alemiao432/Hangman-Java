// Librerie per gestire input/output, file e flussi di dati.
import java.io.*;

// Libreria per la comunicazione di rete tramite socket.
import java.net.*;

// Libreria per utilizzare liste dinamiche.
import java.util.ArrayList;
import java.util.List;

// Libreria per generare numeri casuali.
import java.util.Random;

public class ServerImpiccato {

    // Porta utilizzata dal server per accettare le connessioni.
    private static final int PORT = 12345;

    // Nome del file contenente le parole del gioco.
    private static final String FILE_PAROLE = "parole.txt";

    // Numero massimo di errori consentiti al giocatore.
    private static final int MAX_VITE = 6;

    // Metodo principale: avvia il server e gestisce le partite.
    public static void main(String[] args) {
        System.out.println("[SERVER] Avvio in corso...");

        // Crea il file delle parole se non esiste.
        creaFileSeNonEsiste();

        // Apre il server sulla porta indicata e chiude il socket alla fine.
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] In ascolto sulla porta " + PORT + ". In attesa di client...\n");

            // Mantiene il server attivo per accettare nuovi client.
            while (true) {

                // Accetta un client e prepara i flussi per ricevere e inviare dati.
                try (Socket clientSocket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    System.out.println("[SERVER] Client connesso da: " + clientSocket.getRemoteSocketAddress());

                    // Carica dal file l'elenco delle parole disponibili.
                    List<String> dizionario = caricaParoleDaFile();
                    System.out.println("[SERVER] Dizionario caricato: " + dizionario.size() + " parole rilevate.");

                    // Se non ci sono parole, comunica l'errore e termina la gestione del client.
                    if (dizionario.isEmpty()) {
                        out.println("FINE:Errore sul server: il dizionario delle parole è vuoto!");
                        continue;
                    }

                    // Sceglie una parola casuale e la converte in maiuscolo.
                    String parolaSegreta = selezionaParola(dizionario).toUpperCase();

                    // Crea una sequenza di caratteri che mostra gli spazi e nasconde le lettere.
                    char[] parolaCorrente = new char[parolaSegreta.length()];
                    for (int i = 0; i < parolaCorrente.length; i++) {
                        if (parolaSegreta.charAt(i) == ' ') {
                            parolaCorrente[i] = ' ';
                        } else {
                            parolaCorrente[i] = '_';
                        }
                    }

                    // Inizializza le vite, i tentativi, lo stato della vittoria e il messaggio.
                    int viteRimaste = MAX_VITE;
                    List<String> tentativiEffettuati = new ArrayList<>();
                    boolean parolaIndovinataInteramente = false;
                    String ultimoMessaggio = "Benvenuto nel gioco dell'Impiccato!";

                    // Continua finché ci sono vite, la parola non è stata indovinata
                    // interamente e non sono state scoperte tutte le lettere.
                    while (viteRimaste > 0 && !parolaIndovinataInteramente && !isParolaCompleta(parolaCorrente)) {

                        // Invia al client il messaggio, la parola visibile e le vite rimaste.
                        out.println("STATO:" + ultimoMessaggio + "|" + String.valueOf(parolaCorrente) + "|"
                                + viteRimaste + "|END");

                        // Legge il tentativo inviato dal client.
                        String inputClient = in.readLine();
                        if (inputClient == null)
                            break;

                        // Rimuove gli spazi iniziali e finali e converte l'input in maiuscolo.
                        inputClient = inputClient.trim().toUpperCase();

                        // Gestisce gli input vuoti o non validi.
                        if (inputClient.isEmpty() || inputClient.equals("VUOTO")) {
                            ultimoMessaggio = "Input non valido!";
                            continue;
                        }

                        // Se l'input contiene più di un carattere, viene considerato una parola.
                        if (inputClient.length() > 1) {

                            // Se la parola coincide con quella segreta, il giocatore vince.
                            if (inputClient.equals(parolaSegreta)) {
                                parolaIndovinataInteramente = true;
                                ultimoMessaggio = "Parola esatta!";
                            } else {

                                // Una parola errata fa perdere una vita.
                                viteRimaste--;
                                ultimoMessaggio = "Sbagliato! La parola non è '" + inputClient + "'.";
                            }
                        } else {

                            // Estrae la lettera inserita dal client.
                            String letteraStr = inputClient;
                            char lettera = letteraStr.charAt(0);

                            // Impedisce di ripetere una lettera già tentata.
                            if (tentativiEffettuati.contains(letteraStr)) {
                                ultimoMessaggio = "Lettera già provata!";
                                continue;
                            }

                            // Registra la lettera tra i tentativi effettuati.
                            tentativiEffettuati.add(letteraStr);

                            // Verifica se la lettera è presente nella parola segreta.
                            boolean indovinata = false;
                            for (int i = 0; i < parolaSegreta.length(); i++) {
                                if (parolaSegreta.charAt(i) == lettera) {

                                    // Mostra la lettera nella posizione corretta.
                                    parolaCorrente[i] = lettera;
                                    indovinata = true;
                                }
                            }

                            // Aggiorna il messaggio e le vite in base al risultato.
                            if (indovinata) {
                                ultimoMessaggio = "Ottimo! La lettera '" + lettera + "' è corretta.";
                            } else {
                                viteRimaste--;
                                ultimoMessaggio = "Sbagliato! La lettera '" + lettera + "' non è presente.";
                            }
                        }
                    }

                    // Comunica al client il risultato finale della partita.
                    if (parolaIndovinataInteramente || isParolaCompleta(parolaCorrente)) {
                        out.println("FINE:HAI VINTO! La parola era: " + parolaSegreta);
                        System.out.println("[SERVER] Il client ha VINTO.");
                    } else {
                        out.println("FINE:HAI PERSO! La parola era: " + parolaSegreta);
                        System.out.println("[SERVER] Il client ha PERSO.");
                    }

                    // Segnala la fine della partita e attende un nuovo client.
                    System.out.println("[SERVER] Partita terminata. In attesa di un nuovo client...\n");

                } catch (IOException e) {
                    // Gestisce gli errori avvenuti durante la comunicazione con il client.
                    System.err.println("[SERVER] Errore di comunicazione: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            // Gestisce gli errori durante la creazione o l'avvio del server.
            System.err.println("[SERVER] Errore ServerSocket: " + e.getMessage());
        }
    }

    // Legge il file e restituisce una lista contenente le parole non vuote.
    private static List<String> caricaParoleDaFile() {
        List<String> parole = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PAROLE))) {
            String linea;

            // Legge il file una riga alla volta fino alla fine.
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();

                // Aggiunge alla lista solo le righe non vuote.
                if (!linea.isEmpty())
                    parole.add(linea);
            }
        } catch (IOException e) {
            // Segnala eventuali errori durante la lettura del file.
            System.err.println("[SERVER] Errore lettura file: " + e.getMessage());
        }
        return parole;
    }

    // Crea il file delle parole con alcuni esempi, solo se non esiste già.
    private static void creaFileSeNonEsiste() {
        File file = new File(FILE_PAROLE);
        if (!file.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {

                // Inserisce alcune parole iniziali nel dizionario.
                pw.println("programmazione java");
                pw.println("socket monoclient");
                pw.println("computer science");
                pw.println("rete locale");
            } catch (IOException e) {
                // Segnala un eventuale errore durante la creazione del file.
                System.err.println("[SERVER] Errore creazione file di testo.");
            }
        }
    }

    // Restituisce una parola scelta casualmente dalla lista ricevuta.
    private static String selezionaParola(List<String> lista) {
        return lista.get(new Random().nextInt(lista.size()));
    }

    // Restituisce true se non rimangono lettere nascoste nella parola.
    private static boolean isParolaCompleta(char[] parola) {
        for (char c : parola) {

            // Se trova un trattino basso, la parola non è ancora completa.
            if (c == '_')
                return false;
        }

        // Se non ci sono trattini bassi, tutte le lettere sono state indovinate.
        return true;
    }
}