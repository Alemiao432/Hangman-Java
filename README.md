# Impiccato - Linguaggio Java (Hangman Game)

A text-based Hangman game written in Java, designed to be run and played entirely inside the terminal using a Client-Server architecture.

---

## 🇬🇧 English Description & Setup

This project implements a classic Hangman game. It features **no graphical user interface (GUI)** and runs completely within the command line. The server handles the game logic and picks the secret word, while the client allows the player to connect and **guess individual letters or the entire word**. 

### Repository Structure
* **ServerImpiccato.java**: Manages the game logic and secret words.
* **ClientImpiccato.java**: Terminal user interface for the player.
* **parole.txt**: Text file containing the word dictionary. **You can freely modify this file** to add, remove, or change the words used in the game.

### How to Run

1. **Compile the source files:**
   ```bash
   javac ServerImpiccato.java ClientImpiccato.java
   ```
2. **Start the Server first:**
   ```bash
   java ServerImpiccato
   ```
3. **Open a new terminal window and start the Client:**
   ```bash
   java ClientImpiccato
   ```

### ⚠️ Important Notes & Troubleshooting
* **Connection Error**: You must start the Server *before* launching the Client. If you start the Client first, it will fail to connect.
* **Single-client (Monoclient)**: The server handles one player at a time. If a second client tries to connect while a game is already in progress, it will wait in queue until the first player finishes.

---

## 🇮🇹 Descrizione e Configurazione in Italiano

Questo progetto implementa il classico gioco dell'impiccato. Il gioco **non ha interfaccia grafica** ed è eseguito interamente all'interno del terminale. Il server gestisce la logica di gioco e seleziona la parola segreta, mentre il client permette al giocatore di connettersi e **tentare di indovinare sia le singole lettere che la parola completa**.

### Struttura del Progetto
* **ServerImpiccato.java**: Gestisce la logica di gioco e le parole segrete.
* **ClientImpiccato.java**: Interfaccia utente nel terminale per il giocatore.
* **parole.txt**: File di testo contenente il dizionario delle parole. **È possibile modificare liberamente questo file** per aggiungere, rimuovere o cambiare le parole usate nel gioco.

### Come Avviare il Gioco

1. **Compila i file sorgenti:**
   ```bash
   javac ServerImpiccato.java ClientImpiccato.java
   ```
2. **Avvia prima il Server:**
   ```bash
   java ServerImpiccato
   ```
3. **Apri una nuova finestra del terminale e avvia il Client:**
   ```bash
   java ClientImpiccato
   ```

### ⚠️ Note Importanti sul Funzionamento
* **Errore di Connessione**: È obbligatorio avviare il Server *prima* del Client. Se si avvia prima il Client, questo mostrerà un errore e non potrà connettersi.
* **Architettura Monoclient**: Il server gestisce un solo giocatore alla volta. Se un secondo client tenta di connettersi mentre c'è già una partita in corso, rimarrà in attesa fino a quando il primo giocatore non avrà terminato.
