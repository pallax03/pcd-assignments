---
layout: math
---
# Teoria Assignment-01

Generated File with AI

## 1. Architettura di Sistema e UI Concorrente

* Un'architettura basata sul pattern **Model-View-Controller (MVC)** unito al pattern **Observer**.
* L'uso di un **ViewModel** per disaccoppiare la vista (che legge dati coerenti) dalle strutture mutabili del modello.
* La gestione asincrona per non bloccare l'**Event Dispatch Thread (EDT)** di Swing, evitando freeze della GUI.

**Concetti teorici da studiare:**

* **Event-Driven Programming:** Come funzionano i framework GUI e il ruolo del thread dedicato alla UI (EDT).
* **Concorrenza nei Framework GUI:** Perché l'EDT non deve mai eseguire operazioni bloccanti e come si delega il lavoro a thread in background.
* **Thread-Safety nel pattern Observer:** Come gestire in modo sicuro le notifiche di stato da thread multipli (worker) a thread della UI.

## 2. Gestione Input e Game Loop

* Un `ActiveController` nella versione **V1** che sfrutta un `BoundedBuffer` per ricevere i comandi.
* L'utilizzo di `poll(waitMs)` anziché una `get()` bloccante per permettere al loop di procedere anche in assenza di input utente.
* Nella versione **V2**, l'uso di una `ConcurrentLinkedQueue` nativa di Java sfruttando il metodo non bloccante `offer()`.

**Concetti teorici da studiare:**

* **Pattern Producer-Consumer:** La teoria alla base dei buffer condivisi per la comunicazione tra thread.

### Pattern Producer-Consumer e Collezioni Concorrenti

* **Metodi bloccanti vs non bloccanti:** La differenza di semantica (e di impatto sulla liveness) tra `wait`/`get` e il polling a tempo.
* **Collezioni Concorrenti (Java):** La differenza tra collezioni sincronizzate (con lock intrinseco) e `java.util.concurrent` (es. `ConcurrentLinkedQueue` basata su algoritmi lock-free).

## 3. Sincronizzazione Low-Level (Versione V1)

* La classe `MultithreadingCollisions` che istanzia un gruppo fisso di thread worker tenuti in attesa in un ciclo continuo.
* Una `CyclicBarrier` custom usata per coordinare l'inizio (master prepara i frame) e la fine (master sa che la fisica è calcolata) del lavoro.
* L'implementazione esplicita della barriera usando monitor nativi Java (`synchronized`, `wait()`, `notifyAll()`).
* L'utilizzo di un ciclo `while` attorno alla chiamata `wait()`.

**Concetti teorici da studiare:**

* **Monitor in Java:** La teoria dei monitor, il lock intrinseco associato a ogni oggetto Java e le condition variables.
* **Spurious Wakeup (Risveglio spurio):** Il motivo teorico fondamentale per cui la `wait()` deve sempre risiedere dentro un costrutto `while(condizione)` e mai in un costrutto `if`.
* **Semantica di Notify:** La differenza formale tra `notify()` (Signal) e `notifyAll()` (SignalAll) e le conseguenze sulla schedulazione.
* **Barriere di Sincronizzazione:** Il concetto di *rendezvous* globale in sistemi concorrenti.

## 4. Approccio Task-Based (Versione V2)

* La classe `ExecutorCollisions` che utilizza l'**Executor Framework** di Java.
* La creazione di micro-task (che incapsulano le coppie di palline) sottomessi a un pool di worker.
* La sincronizzazione finale tramite la chiamata di alto livello `invokeAll`.

**Concetti teorici da studiare:**

* **Task-Oriented Programming:** La separazione concettuale tra il "Task" (unità logica di lavoro) e il "Thread" (unità fisica di esecuzione).
* **Thread Pools:** I vantaggi teorici del riciclo dei thread rispetto alla creazione/distruzione continua (overhead del sistema operativo).
* **Future e Promise:** (Se utilizzati implicitamente con `invokeAll`) Come il framework restituisce i risultati asincroni.

## 5. Prevenzione dei Deadlock e Ottimizzazioni

* L'acquisizione di lock multipli (per risolvere le collisioni) protetta da una politica di **Lock Ordering**, confrontando il `System.identityHashCode()`.
* Un'ottimizzazione locale tramite un "pre-check geometrico" eseguito senza acquisire alcun lock per scartare le coppie lontane.

**Concetti teorici da studiare:**

* **Condizioni di Coffman per il Deadlock:** Quali sono le quattro condizioni necessarie (Mutual Exclusion, Hold and Wait, No Preemption, Circular Wait).
* **Strategie di prevenzione:** Come il Lock Ordering rompe la condizione di "Circular Wait" imponendo un ordine totale sulle risorse.
* **Liveness vs Safety:** Il bilanciamento tra garantire consistenza (Safety, usando i lock) e garantire progresso (Liveness, ottimizzando per evitare contese inutili).

## 6. Verifica Formale (Modellazione e Model Checking)

* La modellazione della logica tramite **Reti di Petri**.
* L'analisi delle reti per dimostrare proprietà di **k-Boundedness** (nel buffer limitato) e di **Liveness** (assenza di deadlock globale nella barriera).
* Verifica formale del codice Java tramite **JPF (Java Pathfinder)** su classi critiche come il Buffer e la Barriera.

**Concetti teorici da studiare:**

* **Reti di Petri (Visual Formalisms):** La semantica di Places, Transitions e Token. Saper definire formalmente cosa sono *Safety* (il sistema non entra in stati scorretti) e *Liveness* (il sistema prima o poi fa qualcosa di utile).
* **Model Checking:** Come funziona l'esplorazione esaustiva degli stati (state-space exploration) e degli *interleaving* (le possibili sequenze di esecuzione dei thread) da parte di tool come JPF.

---

## 💡 Le domande "Trappola" per il 30 e Lode

### 1. La Trappola dello Spurious Wakeup (Sui Monitor)

* **Domanda del Professore:** *"Nella tua implementazione custom della `CyclicBarrier`, hai messo la chiamata `wait()` dentro un ciclo `while (attesa > 0)`. Cosa succederebbe esattamente, a livello visivo e logico nel gioco, se avessi usato un semplice `if (attesa > 0)` e la JVM generasse un risveglio spurio (spurious wakeup)?"*
* **Risposta Attesa:** Se avessi usato un `if`, al verificarsi di un risveglio spurio il thread worker uscirebbe dal blocco condizionale senza ricontrollare se tutti gli altri thread sono effettivamente arrivati. Procederebbe quindi a sbloccare la barriera prematuramente, avviando il calcolo del frame successivo (o il rendering) mentre gli altri worker stanno ancora modificando la posizione delle palline del frame corrente. Questo genererebbe race condition spaventose e artefatti grafici imprevedibili (es. palline che "teletrasportano" o compenetrano).

### 2. La Trappola dell'Hash Collision (Sui Deadlock)

* **Domanda del Professore:** *"Per evitare il deadlock sulle collisioni, hai usato un Lock Ordering basato su `System.identityHashCode()`. Ottima intuizione. Ma cosa succede se, per pura sfortuna, due palline diverse restituiscono esattamente lo stesso hashcode? Il tuo sistema va in deadlock?"*
* **Risposta Attesa:** L'hash collision con `identityHashCode` in Java è un evento raro ma possibile. Nel mio codice, se due hash sono identici, il controllo `if (hash1 < hash2)` fallisce e si entra nell'`else` in modo non deterministico. In quel caso millimetrico, il rischio di deadlock teoricamente si ripresenta. Per risolverlo in modo matematicamente perfetto, avrei dovuto introdurre un "Tie-Breaking Lock" (un terzo lock globale usato solo in caso di hash identici) oppure, più semplicemente, assegnare un identificativo univoco intero (`final int id`) incrementale alla creazione di ogni pallina, e ordinare su quello.

### 3. La Trappola dell'Event Loop (Sul Bounded Buffer)

* **Domanda del Professore:** *"Nell'`ActiveController` (V1) hai usato il metodo `poll(waitMs)` per prelevare i comandi utente. Perché non hai usato una semplice `take()` (che è bloccante) risparmiando cicli di CPU anziché fare polling?"*
* **Risposta Attesa:** Perché il paradigma di un Game Loop impone che il tempo avanzi sempre. Se avessi usato una `take()`, il thread dell'`ActiveController` si sarebbe letteralmente "congelato" nell'attesa che l'utente premesse un tasto (UP, DOWN, ecc.). Visivamente, l'intero gioco si sarebbe fermato: le palline avrebbero smesso di muoversi fino al momento in cui l'utente non avesse fornito un input. Il polling garantisce che, in assenza di comandi, la simulazione fisica prosegua indisturbata.

### 4. La Trappola del Garbage Collector (Sui Task Executor)

* **Domanda del Professore:** *"Nella versione V2 (Task-based) hai sostituito i thread fissi con l'Executor, sottomettendo micro-task per le collisioni ad ogni singolo frame. Considerando che un gioco gira a 60 FPS, non hai paura che creare continuamente migliaia di oggetti `Runnable` saturi il Garbage Collector, causando fastidiosi micro-scatti (stuttering) rispetto alla versione V1?"*
* **Risposta Attesa:** È un'osservazione corretta. La versione V1 (worker dormienti su barriera) è estremamente ottimizzata in termini di allocazione memoria perché non crea nuovi oggetti durante il loop, minimizzando il lavoro del GC. La V2 scambia una maggiore pressione sulla memoria (creazione continua di micro-task) in cambio di una pulizia architetturale superiore e di un bilanciamento del carico (Load Balancing) dinamico gestito nativamente dalla libreria Java. Sui PC moderni, il GC generazionale assorbe bene queste allocazioni a vita brevissima, ma in sistemi embedded la V1 risulterebbe superiore.

### 5. La Trappola del Freeze della GUI (Sull'EDT)

* **Domanda del Professore:** *"Spiegami cosa sarebbe successo di preciso se, per pigrizia, non avessi fatto un controller separato e avessi chiamato il metodo di calcolo collisioni ($O(n^2)$) direttamente all'interno dell'`actionPerformed` o del `keyPressed` generato da Swing."*
* **Risposta Attesa:** L'elaborazione delle collisioni sarebbe finita sull'**Event Dispatch Thread (EDT)**. Poiché l'EDT è il thread singoletto responsabile anche di ridisegnare la finestra e catturare i clic del mouse, tenerlo impegnato in un calcolo così lungo gli avrebbe impedito di processare qualsiasi altro evento in coda. Il risultato pratico sarebbe stato il totale *congelamento* della GUI (finestra bloccata, cursore a clessidra, nessun repaint a schermo) rendendo il gioco ingiocabile. Questo viola il dogma assoluto dei framework UI: mai bloccare l'EDT.
