# AdvancedBanSystem

## Descrizione
**AdvancedBanSystem** è un plugin avanzato per Minecraft che offre un sistema completo di gestione dei bannati e delle blacklist. Con questo plugin, puoi bannare giocatori, bannare indirizzi IP e aggiungere giocatori alla lista nera, impedendo loro di accedere al server. Tutto viene salvato in un database SQLite per garantire persistenza e facilità di gestione.

---

## Funzionalità
- **Ban per Giocatori:** Banna un giocatore con un motivo personalizzato.
- **Sban per Giocatori:** Sbanna un giocatore precedentemente bannato.
- **Ban per IP:** Banna un indirizzo IP specifico.
- **Sban per IP:** Sbanna un indirizzo IP precedentemente bannato.
- **Blacklist:** Aggiungi o rimuovi giocatori dalla lista nera.
- **Lista dei Bannati:** Visualizza tutti i giocatori bannati.
- **Lista della Blacklist:** Visualizza tutti i giocatori nella lista nera.
- **Compatibilità Console:** Tutti i comandi possono essere eseguiti sia dai giocatori che dalla console.
- **Database Persistente:** Tutte le azioni vengono registrate in un database SQLite per garantire persistenza.

---

## Comandi Disponibili

| Comando         | Descrizione                                    | Permesso                          |
|-----------------|-----------------------------------------------|-----------------------------------|
| `/ban <player>` | Banna un giocatore con un motivo opzionale.    | `advancedbansystem.ban`          |
| `/unban <player>` | Sbanna un giocatore precedentemente bannato. | `advancedbansystem.unban`        |
| `/ipban <player>` | Banna l'IP di un giocatore con un motivo opzionale. | `advancedbansystem.ipban`       |
| `/ipunban <ip>` | Sbanna un indirizzo IP precedentemente bannato.| `advancedbansystem.ipunban`      |
| `/blacklist add <player/ip>` | Aggiunge un giocatore o un IP alla lista nera. | `advancedbansystem.blacklist.add`|
| `/blacklist remove <player/ip>` | Rimuove un giocatore o un IP dalla lista nera. | `advancedbansystem.blacklist.remove` |
| `/banlist`      | Visualizza la lista dei giocatori bannati.     | `advancedbansystem.banlist`      |
| `/blacklist list` | Visualizza la lista nera.                    | `advancedbansystem.blacklist.list` |

---

## Installazione

1. **Scarica il Plugin:**
   - Scarica il file JAR del plugin da [qui](https://github.com/tuo-username/AdvancedBanSystem/releases).

2. **Carica il Plugin sul Server:**
   - Copia il file JAR nella cartella `plugins` del tuo server Minecraft.

3. **Avvia il Server:**
   - Avvia o riavvia il server per caricare il plugin.

4. **Configura i Permessori:**
   - Usa un plugin di gestione dei permessi (ad esempio LuckPerms o PermissionsEx) per assegnare i permessi appropriati ai giocatori o ai gruppi.

---

## Configurazione

Il plugin crea automaticamente un database SQLite (`bans.db`) nella sua cartella principale. Non è necessaria alcuna configurazione manuale, ma puoi personalizzare i permessi nel file `plugin.yml`.

---

## Esempi di Uso

### Ban di un Giocatore
/ban Matty47ghigo Cheating

### Sban di un Giocatore
/unban Matty47ghigo

### Ban di un IP
/ipban Matty47ghigo Multiconto

### Sban di un IP
/ipunban 192.168.1.1

### Gestione della Blacklist
/blacklist add Matty47ghigo
/blacklist remove Matty47ghigo

### Visualizza la Lista dei Bannati
/banlist

### Visualizza la Lista Nera
/blacklist list


---

## Requisiti
- **Server Minecraft:** Spigot/Paper 1.21.0 o versione successiva.
- **Java:** Versione 17 o superiore.
- **Dipendenze:** Nessuna dipendenza esterna richiesta (il database SQLite è incluso).

---

## Contribuire
Se desideri contribuire a questo progetto, segui questi passaggi:
1. Clona il repository: `git clone https://github.com/tuo-username/AdvancedBanSystem.git`
2. Apri il progetto in un IDE come IntelliJ IDEA o Eclipse.
3. Compila il plugin usando Maven: `mvn clean package`
4. Invia una pull request con le tue modifiche.

---

## Licenza
Questo plugin è rilasciato sotto licenza **MIT**. Consulta il file [LICENSE](LICENSE) per ulteriori dettagli.

---

## Crediti
- **Autore:** [Tuo Nome](https://github.com/tuo-username)
- **Repository GitHub:** [https://github.com/tuo-username/AdvancedBanSystem](https://github.com/tuo-username/AdvancedBanSystem)

Grazie per utilizzare **AdvancedBanSystem**! Se hai domande o suggerimenti, non esitare a contattarmi.
