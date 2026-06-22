---
layout: math
---
# Teoria Assignment-03

Generated File with AI

## Parte 1: Modello ad Attori e Sistemi Asincroni (Scala / Pekko)

* **Gerarchia di Attori:** Un albero di supervisione con `AlarmSystemGuardian` in cima, che crea e coordina figli come `KeypadActor`, `SirenActor` e delega la creazione dei sensori al router `SensorsManager`.
* **Macchina a Stati (FSM) via Behavior Switching:** Invece di usare variabili booleane mutabili (es. `isArmed`), hai modellato gli stati (Disarmed, Armed, Exit Delay, ecc.) facendo restituire all'attore un nuovo `Behavior[Command]`.
* **Timer Non Bloccanti:** Uso di `Behaviors.withTimers` e `startSingleTimer` per gestire i ritardi di ingresso/uscita scatenando l'auto-invio di messaggi (es. `ExitTimeExpired`).
* **Fault Tolerance (Death Watch e Supervisione):** Hai usato `context.watch(sensor)` in `SensorsGroup` per intercettare il segnale `Terminated` (se un sensore va offline o viene manomesso) e `SupervisorStrategy.restart` nel Guardian per far ripartire attori come la sirena in caso di crash.

**Concetti teorici da studiare (Rif. Modulo 3.2):**

* **L'Astrazione dell'Attore:** Quali sono le 3 primitive fondamentali del modello ad attori originario di Hewitt/Agha? (*Send* asincrona, *Create* nuovi attori, *Become* per cambiare comportamento). **Nota:** Il tuo *Behavior Switching* è l'implementazione pratica della primitiva *Become*.
* **Assenza di Stato Condiviso:** Come gli attori garantiscono la thread-safety "gratis" (ogni attore elabora un messaggio alla volta dalla sua mailbox in modo sequenziale, eliminando le race condition).
* **Supervision Trees (Let it crash):** La filosofia di delegare la gestione degli errori a un supervisore (padre) invece di riempire il codice di `try/catch`. La differenza tra "Restart", "Resume" e "Stop".
* **Routing e Dispatching:** I pattern di invio messaggi (Point-to-Point vs Publish-Subscribe / Broadcast) che hai usato per inviare i comandi di armamento a tutte le zone.

---

## Parte 2: Message Passing Sincrono (Go / CSP)

* **Architettura Decentralizzata (Symmetric Peer-to-Peer):** $N$ giocatori che comunicano direttamente tramite una matrice di canali calcolata staticamente, senza un "coordinatore/arbitro" centrale.
* **Prevenzione del Deadlock (Simmetria spezzata):** Per evitare che due attori si blocchino a vicenda cercando di scrivere o leggere contemporaneamente sullo stesso canale sincrono, hai assegnato ruoli deterministici basati sull'indice dell'albero: i giocatori a sinistra sono **Leader (Server/Receiver)**, quelli a destra sono **Follower (Client/Sender)**.
* **Canali non bufferizzati:** Hai utilizzato i channel di default di Go, che implicano un rendezvous (incontro) sincrono tra chi invia e chi riceve.

**Concetti teorici da studiare (Rif. Modulo 3.1 e 4.2):**

* **Modello CSP (Communicating Sequential Processes):** La teoria di Tony Hoare su cui si basa Go. La differenza concettuale tra una Goroutine e un Thread del sistema operativo.
* **Sincronia vs Asincronia:** Qual è la differenza semantica tra una `send` in Pekko (fire-and-forget) e una `send` in Go (`ch <- msg`)? (In Go la send si blocca finché qualcuno non fa la receive, forzando una sincronizzazione temporale esatta).
* **Soluzioni Simmetriche vs Centralizzate:** Perché hai scelto una soluzione P2P decentralizzata rispetto a un Master-Worker? (Pro: no bottleneck, no single point of failure; Contro: maggiore complessità nel setup e nel routing).
* **Deadlock nei sistemi a scambio di messaggi:** Come le condizioni di Coffman si applicano qui. Il tuo stratagemma matematico (`isLeader = (ID / 2^r) % 2 == 0`) ha rotto la condizione di *Attesa Circolare (Circular Wait)* definendo un "Lock Ordering" implicito sui canali di comunicazione.

---

## 💡 Le domande "Trappola" per il 30 e Lode

Queste sono le domande in cui il professore incrocia i paradigmi. Mettiti alla prova su queste prima dell'esame:

### 1 **Il confronto sui Deadlock:** *"Nel progetto in Go hai dovuto inventare una formula matematica per stabilire chi invia e chi riceve per evitare il deadlock. Perché nel progetto Pekko non ti sei preoccupato del deadlock quando due sensori comunicano con il manager?"*

**Risposta attesa:** Perché Pekko usa code di messaggi (mailbox) asincrone. La `send` (! / tell) non è mai bloccante, quindi non si forma mai un'attesa circolare a livello di thread.

### 2 **Il confronto sulle Performance/Scalabilità:** *"Se nel progetto in Go avessi usato un Coordinatore Centrale per arbitrare le partite di pari e dispari, cosa sarebbe successo a livello di prestazioni scalando a milioni di giocatori?"*

**Risposta attesa:** Il canale del coordinatore sarebbe diventato un bottleneck (collo di bottiglia) spaventoso, rallentando tutti. Il tuo approccio P2P decentralizzato permette alle partite di avvenire in parallelo reale.

### 3 **Il Modello di Guasto (Fault Tolerance):** *"Cosa succede nel tuo codice Go se una goroutine (un giocatore) crasha improvvisamente prima di inviare il suo numero? Come differisce questa situazione dalla gestione di un sensore rotto in Pekko?"*

**Risposta attesa:** In Go (messaggistica sincrona nativa), l'avversario rimarrebbe bloccato per sempre in attesa sulla `receive` (Goroutine leak/Deadlock locale), a meno di non implementare timeout espliciti (tramite `select`). In Pekko, grazie alla supervisione e al `DeathWatch` (`context.watch`), il fallimento genera un messaggio di sistema (`Terminated`) che permette di reagire in modo reattivo e pulito.

### 4 **Behavior Switching vs Variabili di Stato:** *"Cosa garantisce che l'Allarme in Pekko non processi un evento 'Pin Inserito' mentre sta suonando la sirena, creando stati incoerenti?"*

**Risposta attesa:** Il fatto che lo stato *Alarm* restituisce un comportamento (Behavior) che semplicemente non definisce la gestione per quel particolare messaggio, oppure lo scarta esplicitamente. Non essendoci uno stato condiviso globale, è impossibile per i thread sovrascrivere variabili mentre l'attore processa quell'evento.
