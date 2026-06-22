# Teoria Assignment-02

Generated File with AI

## 1. Architettura di Base: Isolamento e Composizione (Divide et Impera)

* Hai abbandonato le strutture condivise (niente lock, niente monitor).
* Hai creato un oggetto `Report` locale per ogni directory/task.
* Hai usato una funzione `merge()` per sommare i report dei figli nel report del padre.
* Hai gestito in modo silente gli errori di accesso all'I/O (restituendo report vuoti).

**Concetti teorici da studiare:**

* **Condivisione vs Isolamento:** Perché evitare lo "shared mutable state" elimina alla radice il problema delle *race conditions* e dei *deadlock*.
* **Task Decomposition (Divide et Impera):** La teoria della scomposizione di un problema in sotto-task indipendenti e la ricomposizione dei risultati (pattern concettualmente vicino al Map-Reduce).
* **I/O Bound vs CPU Bound:** La differenza teorica tra un task che consuma cicli di processore (es. calcolo collisioni nel Poool) e uno che passa il tempo ad attendere il disco/rete (FSStat).

---

## 2. Paradigma Asincrono basato su Event-Loop (Vert.x)

* Composizione di `Future` e `Promise` (`compose`, `all`).
* Un'architettura che non blocca *mai* il thread (niente `join()`, `get()`, o `sleep()`).
* Ricorsione modellata asincronamente tramite l'aggregazione delle Promise dei figli.

**Concetti teorici da studiare:**

* **Il pattern Event-Loop / Reactor:** Cos'è un event-loop, come funziona la coda degli eventi e il dispatching.
* **Il Dogma "Never Block the Event Loop":** Cosa succede a livello di sistema se fai una chiamata bloccante dentro un event-loop (Starvation dell'intero sistema).
* **Future e Promise:** La differenza semantica tra i due (Promise come "write-side", Future come "read-side"). Come risolvono il problema del *Callback Hell* (la piramide della morte) rendendo il codice asincrono componibile linearmente.

---

## 3. Programmazione Reattiva e Backpressure (RxJava)

* La scansione modellata come un flusso dati continuo (`Flowable<File>`).
* L'operatore `scan` per emettere stati intermedi (report progressivi) senza aspettare la fine.
* L'integrazione con la GUI tramite il pattern `Observer`.
* La gestione della velocità di emissione tramite `onBackpressureLatest()` e il campionamento tramite `sample()`.
* La cancellazione del task tramite il rilascio del `Disposable`.

**Concetti teorici da studiare:**

* **Push vs Pull Models:** La differenza fondamentale tra richiedere dati attivamente (Pull - es. iteratori) e reagire a dati che arrivano (Push - es. stream reattivi).
* **Observable e Observer:** I concetti base del paradigma reattivo. Come i dati si propagano lungo una pipeline di operatori.
* **Il problema della Backpressure:** Fondamentale! Cosa succede quando il *Producer* (il disco che legge i file) è ordini di grandezza più veloce del *Consumer* (la GUI che deve ridisegnare il grafico).
* **Strategie di Backpressure:** Perché hai scelto il "Drop" dei dati vecchi (`onBackpressureLatest` + `sample`) rispetto al "Buffer" (che esaurirebbe la RAM) o al blocco del producer.

---

## 4. Virtual Threads (Project Loom)

* L'uso di `Executors.newVirtualThreadPerTaskExecutor()`.
* La creazione di un thread dedicato per *ogni singolo nodo/figlio* della directory.
* L'uso esplicito della chiamata bloccante `join()` sui task figli.

**Concetti teorici da studiare:**

* **Platform Threads vs Virtual Threads:** La differenza architetturale. Perché un Platform Thread è un "thin wrapper" attorno a un thread del Sistema Operativo (pesante, costoso) mentre un Virtual Thread è gestito dalla JVM (leggero, economico).
* **Carrier Threads e Yielding:** Questo è il punto focale. Quando chiami la `join()` (operazione bloccante) in un Virtual Thread, il thread *non blocca* il thread fisico del sistema operativo (Carrier Thread). Invece, fa "yield" (cede il passo), permettendo alla JVM di montare un altro Virtual Thread sullo stesso Carrier Thread.
* **Thread-per-Task Model:** Perché i Virtual Threads rendono di nuovo valido il modello sincrono/bloccante (scrivere codice asincrono come se fosse sincrono), superando i limiti di scalabilità del passato.

---

## 💡 Le domande "Trappola" per il 30Lode

Preparati a rispondere a queste domande comparative, sono quelle che i professori usano per testare la vera comprensione:

### 1 *"Nella versione Event-Loop hai vietato categoricamente la `join()`. Nella versione Virtual Threads l'hai usata liberamente per aspettare i figli. Perché questa differenza non fa esplodere l'applicazione nel secondo caso?"*

**Risposta attesa:** Spiegazione del meccanismo di *unmounting/yielding* dei Virtual Threads dal Carrier Thread.

### 2 *"Se invece di RxJava avessi usato la versione Event-Loop per aggiornare la GUI in tempo reale, che problemi avresti avuto?"*

**Risposta attesa:** Difficoltà nel gestire la backpressure nativamente, rischio di inondare l'EDT di Swing di eventi `invokeLater` causando freeze visivi, a differenza di Rx che ha operatori temporali integrati.

### 3 *"Cosa succede alla memoria e al Garbage Collector se in RxJava tolgo `onBackpressureLatest()` e lascio generare migliaia di eventi al secondo verso la GUI?"*

**Risposta attesa:** OutOfMemoryError, perché le code interne si riempiono di oggetti non smaltiti abbastanza in fretta dal consumatore lento.
