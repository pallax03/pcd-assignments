# Modulo 1: Multithreading and Fundamentals

## Introduzione e Proprietà Fondamentali

### Concorrenza e Parallelismo

La **concorrenza** riguarda la strutturazione e la progettazione del software. Un programma concorrente gestisce attività multiple senza dover necessariamenete essere eseguito su processoori fisici separati, grazie ad una tecnica chiamata [interleaving](#interleaving-e-modello-di-esecuzione).

Il **parallelismo** riguarda l'esecuzione fisica e simultanea sull'hardware.
Si ha parallelismo quando l'esecuzione di più processi avviene simultaneamente su più processori o core fisici.

> La concorrenza abilita al parallelismo, ma il parallelismo non implica concorrenza.

### Interleaving e Modello di Esecuzione

L'**interleaving** è il meccanismo hardware/software per simulare concorrenze con un singolo processore, eseguendo sequenzialmente istruzioni appartenenti a processi diversi.
Il Sistema Operativo, utilizza lo scheduler per garantire questo meccanismo.

L'idea di base dell'interleaving, si appoggia alla definizione di **interleaving arbitrario**. L'esecuzione globale di un programma viene modellata come una sequenza di passi ottenuta "intrecciando" in modo del tutto casuale le diverse azioni dei processi concorrenti, senza alcuna garanzia di ordine o di frequenza. In questo modo, si simula un ambiente altamente non deterministico, dove ogni possibile interleaving rappresenta una possibile esecuzione del programma.

**Interleaved** è un termine che si riferisce a un programma o a un sistema in cui le operazioni di più processi o thread sono mescolate o intrecciate tra loro, spesso in modo non deterministico, a causa dell'interleaving.

### Problemi di Interferenza (Race Conditions)

I problemi di interferenza si verificano quando due o più processi accedono a una risorsa condivisa senza un'adeguata sincronizzazione, portando a risultati imprevedibili e comportamenti non deterministici.

Le **Race Conditions** Si verificano quando due o più processi accedono a una risorsa condivisa e tentano di modificarla contemporaneamente, causando risultati imprevedibili:

* **Dirty Read**: Un processo legge un dato mentre un altro processo lo sta modificando, ottenendo un valore sporco o non aggiornato.
* **Lost Update**: Due processi scrivono contemporaneamente su una stessa risorsa condivisa, causando la perdita di uno degli aggiornamenti.

### Critical Situations

Esistono tre principali situazioni critiche che possono verificarsi nei sistemi concorrenti, tutte legate alla gestione delle risorse condivise e alla sincronizzazione dei processi:

* **Deadlock**: Un processo si blocca in attesa di un segnale che non arriverà mai, risultando cosi il processo in un blocco permanente.
* **Starvation**: Un processo non riesce a progredire, poichè è penalizzato all'infinito da altri processi, ad esempio la sezione critica è sempre rubata da un altro processo.
* **Livelock**: Due processi si bloccano reciprocamente, causando anche uno spreco di risorse.

### Proprietà di Correttezza: Safety vs. Liveness

La correttezza di un programma concorrente si definisce attraverso due proprietà fondamentali:

* **Safety**: Richiedono che una certa condizione sia sempre vera in ogni singolo stato di computazione. Ad esempio, la **mutua esclusione** e l'**assenza di deadlock**.
  > Un singolo stato che viola le proprietà di safety è sufficiente per dimostrare che il programma è errato.
* **Liveness**: Richiedono che una certa condizione sia eventualmente vera in ogni computazione. Ad esempio, l'**assenza di starvation**, il **concetto di fairness** e l'**assenza di livelock**.
  > Per dimostrare queste proprietà è più complesso, dato che, è necessario analizzare intere sequenze di scenari futuri all'infinito.

#### Fairness

La **fairness** è una **proprietà di liveness**, è proprio la garanzia per la quale un programma concorrente, se verificata **non può essere soggetto a starvation**.
È strettamente legata al concetto di **scheduling**, in particolare all'**ordine di interleaving** in cui i processi vengono eseguiti, questo intreccio è proprio definito dalla **politica di scheduling**.
Esistono tre tipi principali di fairness associati alle politiche di scheduling:

* **Unconditional Fairness**: Ogni azione atomica senza condizioni verrà eseguita prima o poi.
* **Weak Fairness**: fairness incondizionata + garantisce inoltre che un'azione condizionale, se verificata, rimane continuamente vera, fino al momento in cui l'azione viene eseguita.
* **Strong Fairness**: fairness incondizionata + garantisce inoltre che un'azione condizionale, verrà eseguita prima o poi se la sua condizione si verifica infinitamente spesso (ovvero, anche se passa temporaneamente a falsa, continua a ridiventare vera nel tempo).

## Interazione tra processi

### Cooperazione e Competizione

Durante l'interazione dei processi, le dinamiche si suddividono in 3 categorie:

* **Cooperazione**: Definisce le interazioni tra processi attese e desiderate, si manifesta in due forme, con una comunicazione o una **sincronizzazione**.
* **Competizione**: Definisce le interazioni non desiderate, qelle che dovremmo gestire, necessarie per il corretto funzionamento, si manifestano come:
  * **Mutua Esclusione**: È la proprietà del sistema per cui un solo thread alla volta può accedere a una risorsa o porzione di codice.
  * **Sezioni Critiche**: È una porzione di codice che accede a una risorsa condivisa mutabile.
* **Interferenze** (problema di safety): È l'evento che si verifica quando le interazioni di competizione (una mancata sincronizzazione) non vanno a buon fine, causando problemi non deterministici, dette Race Conditions:
  * **dirty read**: un processo legge un dato, mentre l'altro lo sta modificando leggendo un valore sporco.
  * **lost update**: due processi scrivono contemporaneamente su una stessa risorsa, una delle due viene persa.

### Mutua Esclusione e Sincronizzazione

La **Sincronizzazione** definisce una relazione temporale tra le azioni dei processi. Il suo scopo è stabilire precise relazioni temporali tra le azioni dei processi.
NON richiede la presenza di dati o risorse condivise.

La **Mutua Esclusione** definisce una restrizione sull'accesso a risorse condivise da molteplici processi.
Due processi non devono mai trovarsi contemporaneamente nella sezione critica.

Le *mutua esclusione* fa tipicamente riferimento a tecniche di sincronizzazione per garantire operazione atomiche su risorse condivise.

## Costrutti di Coordinazione

### Semafori

Sono un costrutto primitivo per la sincronizzazione, creati da Dijkstra (1968), sono molto semplici, ma estremamente generalizzabili.
È composto da due campi, e forniscono 2 operazioni atomiche:

```scala
val value: Int = k >= 0 // where k is the number of permits available
val list: Set[Id] = Set.empty

def wait(S) = if (value > 0) 
  then value = value - 1 
  else 
    list = list + p // p is the current process
    // p.state <- BLOCK

def signal(S) = if (list.isEmpty) 
  then value = value + 1 
  else 
    val q = list.head // q is the first process in the waiting list
    list = list - q
    // q.state <- READY
```

#### Types of Semaphores (k-value)

* Mutex: A binary Semaphore, typical use for implementing mutual exclusion, the k value can only be 0 or 1.
* General Semaphore (or Counting / Resource): the k value can be any integer, $k \geq 0$
* Event Semaphore: initialised with k=0, used for synchronization purposes.

#### Main Definitions of Semaphores

* Strong vs Weak:
  * Strong: the order of the processes in the waiting list is respected, the first process to wait is the first to be signaled, (Using a FIFO queue) this respect fairness, so *no starvation* can occur.
  * Weak: the order of the processes in the waiting list is not respected, any process can be signaled, this is non deterministic, and can cause *starvation*.

* Busy-Wait: wait and signal operations only decrements and increments the value, there's no list of waiting processes, la wait e la signal girano all'inifito causando uno spreco di risorse ma garantendo sempre la sincronizzazione, può essere utile in contesti dove si utilizza  un solo processore fisico dedicato, e cosi non spreca risorse girando a vuoto. *loosing freedom from starvation*, ovvero, rischio di starvation molto alto.

#### Costrutti che utilizzano i Semafori

Dato che i semafori sono primitive a basso livello, molto generici, sono utilizzati per costruire costrutti di interazione tra processi più complessi, come:

* Competizione (Mutua Esclusione):
  * Locks (Binary Semaphore) -> [Critical Sections Problem](#locks---critical-sections-problem)
* Cooperazione (Sincronizzazione):
  * General Semaphore -> [Producer-Consumer Problem](#producer-consumer-problem)
  * Event Semaphore: Utilizzato per la notifica di eventi.
  * [Barrier / Latch](#barrier--latch-rendezvous-problem) -> Rendezvous Problem

### Locks -> Critical Sections Problem

k-value = 1, cosi che solo un processo alla volta passa alla wait, gli altri processi devono aspettare la signal, si usa per questo un semaforo binario.
![critical_section](/images/locks.webp)

Attenzione, possiamo notare con lo state diagram (grafo di raggiungibilità) che c'è mutual exclusion, free from deadlock and starvation.
MA in casi di N processi, un processo potrebbe essere penalizzato all'inifinito, causando **starvation**, per questo è importante utilizzare soluzioni come *strong* semaphores, o l'algoritmo dei fornai / ticket.

### Problemi Classici (Patterns)

#### Dining Philosophers

N filosofi sono seduti in una tavola rotonda, e hanno due singole forchette alla loro sinistra e destra.
Ognuno di loro necessita di entrambe le forchette per mangiare, si può notare che se tutti i filosofi prendono la forchetta alla loro destra, si bloccano reciprocamente in attesa della forchetta alla loro sinistra, causando un deadlock.

##### Lock Ordering

È la startegia utilizzata per prevenire l'attesa circolare (deadlock), in un problema come quello dei filosofi, definisce un ordinamento globale e totale su tutti i **lock** disponibili. Tutti i thread hanno l'obbligo tassativo quello di acquisire i lock seguendo quell'ordine.

Nel Problema dei Filosofi, potremmo assegnare un ID univoco a ogni forchetta, e imporre ai filosofi di acquisire prima la forchetta con l'ID più basso e poi quella con l'ID più alto. In questo modo, anche se tutti i filosofi prendono la forchetta alla loro destra, non si verificherà un deadlock, poiché non ci sarà un ciclo di attesa.
L'ultimo processo non può prendere la prima forchetta (0), poichè è già occupata dal primo processo, e quindi l'ultimo processo non prende nessuna forchetta aspetterà che il primo processo rilasci la forchetta.

Trasforma un grafo delle dipendenze circolare (un anello) in un grafo diretto aciclico (un albero o una linea), dove esiste sempre almeno un nodo foglia che può completare la sua esecuzione.

#### Producer-Consumer Problem

È un esempio di cooperazione tra processi, ci sono due tipi di processi:
Si basano sull'utilizzo di un buffer condiviso e qui la differenza tra unbounded e bounded buffer è fondamentale:

##### Bounded Buffer vs Unbounded Buffer

* inserimento:
  * put (Bounded): Se il boundend buffer è pieno, il thread produttore si blocca finché non c'è spazio.
  * offer (Unbounded): Il thread produttore non si blocca mai, ma rischia di saturare la memoria se la produzione è più veloce del consumo.
* get ~ estrazione bloccante:
  * Bounded: Se il buffer è vuoto, il thread consumatore si blocca finché non c'è un elemento da consumare.
  * Unbounded: `ConcurrentLinkedQueue` non ha metodi bloccanti, percui non ha la get.
* poll ~ estrazione non bloccante:
  * poll(waitMS) (Bounded): attende un certo tempo per un elemento, restituendo null se scade.
  * poll (Unbounded): restituisce null se la coda è vuota, senza bloccare.

* **Producer**: Si identificano qui processi produttori, che producono dati e li inseriscono nel buffer condiviso.
* **Consumer**: I consumatori sono quei processi che estraggono dal buffer i dati e li consumano.

Possono essere implementati attraverso semafori, con due semafori, uno per i produttori e uno per i consumatori, in combo con un semaforo binario per la mutua esclusione.
Oppure è possibile utilizzare i Monitor, in questo caso il buffer avrà bisogno di condition variables come notEmpty() e notFull(), che permettono di sincronizzare i produttori e i consumatori.

##### Limitazioni

Il problema principale del Producer-Consumer è che i produttori e i consumatori non possono comunicare direttamente tra loro, ma solo tramite il buffer condiviso. Questo può portare a problemi di sincronizzazione e di gestione della concorrenza, specialmente in scenari con più produttori e consumatori.
I produttori potrebbero produrre dati più velocemente di quando i consumatori riescano a consumarli, causando un overflow del buffer, in caso di bounded buffer la put è **bloccante** per cui il produttore si blocca fino a che un consumer non libera spazio.

##### Contesto Sincrono e Ascincrono

In un **contesto sincrono**, la comunicazione non può avvenire fino a che entrambi sono pronti contemporaneamente,l'invio di un messaggio da parte del produttore è bloccante fino a che non viene ricevuto dal consumer, NON vi è alcuna necessità di un buffer per memorizzare i messaggi.

In un **contesto asincrono**, il canale di comunicazione possiede una struttura di memorizzazione FIFO, questo approccio garantisce un disaccoppiamento temporale tra produttore e consumatore, il produttore può inviare messaggi anche se il consumatore non è pronto a riceverli, i messaggi vengono memorizzati nel buffer fino a quando il consumatore non è pronto a riceverli.

#### Readers and Writers Problem

Il problema dei lettori e scrittori è un classico problema di sincronizzazione che coinvolge più thread che accedono a una risorsa condivisa, come un database o un file. Ci sono due tipi di thread:

* **Lettori**: Possono accedere alla risorsa condivisa contemporaneamente, purché non ci siano scrittori attivi. I lettori non modificano la risorsa, quindi possono leggere in parallelo senza causare problemi di concorrenza.
* **Scrittori**: Devono avere accesso esclusivo alla risorsa condivisa, il che significa che quando uno scrittore è attivo, nessun altro thread (né lettore né scrittore) può accedere alla risorsa.

>Invarianti matematiche $nR \geq 0$ (number of readers) e $nW = [0, 1]$ (number of writers)

La soluzione attraverso i monitor, prevede l'utilizzo di due condition variables, una per i lettori e una per gli scrittori, oltre al numero di lettori e scrittori.

![readers and writers implementation](/images/readers-and-writers.webp)

### Barrier / Latch (Rendezvous Problem)

Il problema del **Rendezvous**, è strettamente legato al concetto di barrier e latch, che sono costrutti di sincronizzazione utilizzati per coordinare l'esecuzione di più thread o processi.
Due persone che scelgono un luogo in cui incontrarsi: il primo che arriva al punto di incontro deve obbligatoriamente aspettare l'arrivo del secondo prima di poter fare qualsiasi altra cosa.

Un latch agisce come un vero e proprio cancello, sono ideali per attendere il completamente di eventi o attività (una tantum). Ad esempio, si usano per aspettare che tutti i partecipanti in un gioco multiplayer siano "pronti" prima di iniziare la partita (un servizio).

Una barrier, bloccano un gruppo di thred finchè un determinato evento non si è verificato. Ad esempio, usiamo una cyclicBarrier per sincronizzare un gruppo di thread worker che devono attendere che tutti abbiano finito la loro task, prima di sbloccarli e farli proseguire con la fase successiva.

>La differenza sta proprio in: "i latch servono per aspettare eventi, le barriere servono per aspettare altri thread".

### Monitor

I Monitor sono costrutti ad alto livello progettati per combinare in un'unica struttura l'incapsulamento dei dati, la mutua esclusione e la cooperazione tra thread, riducendo i rischi di errore tipici dei semafori a basso livello.

#### Mutua esclusione implicita (Safety)

È garantita automaticamente dal costrutto stesso (o dal compilatore/runtime). Quando un metodo è definito all'interno di un monitor (o marcato come synchronized in Java), il sistema garantisce che un solo thread alla volta possa essere attivo all'interno dell'intero monitor.
Se il Thread A è dentro il monitor, qualsiasi altro thread che tenta di invocare un metodo dello stesso monitor viene automaticamente sospeso e inserito nella Entry Queue
La mutua esclusione implicita da sola non protegge dalla starvation. Spesso la Entry Queue non segue una logica strettamente FIFO (es. in Java dipende dallo scheduler del Sistema Operativo), quindi i thread potrebbero essere svegliati in modo non equo.

#### Sincronizzazione esplicita (Liveness)

Definisce la coordinazione logica tra i thread guidata esplicitamente dal programmatore tramite le Condition Variables + mutex (Variabili di Condizione). Serve quando un thread, pur avendo ottenuto la mutua esclusione, non può procedere perché una condizione logica non è soddisfatta.
A livello teorico, ogni singola Condition Variable possiede una propria coda interna gestita in modo strettamente FIFO, garantendo l'assenza di starvation per quella specifica condizione.

#### Discipline Semantiche di Signaling

Quando un Thread A (attivo nel monitor) esegue una signal per svegliare un Thread B (sospeso su una condizione), si crea una situazione critica: in quel momento entrambi avrebbero il diritto di eseguire codice, ma il monitor impone che un solo thread alla volta possa essere attivo.
Chi ha il diritto alla mutua esclusione in quell'istante?

* **Signal and Continue**: Il Thread S, dopo aver eseguito la signal, continua ad essere attivo all'interno del monitor, e il Thread B rimane sospeso fino a quando S non rilascia la mutua esclusione. **Questo è il comportamento di Java**.
* **Signal and Wait**: Il Thread segnalante (S) cede immediatamente il controllo del monitor e si blocca in una coda di attesa (Entry Queue). Il thread risvegliato (B) prende immediatamente possesso del monitor ed esegue il suo codice.
* **Signal and Urgent Wait**: È una variante della semantica di `Signal and wait`. Il thread segnalante (S) si sospende immediatamente per far passare il thread risvegliato (B). Tuttavia, S si inserisce in una coda speciale ad alta priorità (Urgent Queue). Non appena B termina o si blocca a sua volta, il monitor garantisce che il controllo ritorni immediatamente ad S, prima di servire qualsiasi altro thread esterno nella Entry Queue.

#### Spurious Wakeup

È un fenomeno spesso causato dallo scheduler, che vuole garantire la proprietà di interleaving tra thread.
Uno **Spurious Wakeup** si verifica quando "un thread si risveglia dal suo stato di attesa senza essere stato esplicitamente notificato, interrotto o scaduto un timeout".

Sebbene sia un evento raro, è importante gestirlo correttamente, poichè può portare a comportamenti non deterministici.

È la principale motivazione per la quale è necessario che le condition variables siano sempre utilizzare all'interno di un ciclo `while`, invece che un semplice `if`. In questo modo fino a che la variabile non è soddisfatta anche se il thread si risveglia da una `wait`, riesegue il ciclo e rifinirà in `wait` se la condizione non è soddisfatta, evitando cosi problemi di spurious wakeup.

## Architettura e Design dei Programmi Concorrenti

### From Threads to Tasks (Task-Parallelism)

L'idea di base è disaccoppiare la logica di business dalla gestione dei thread, per poter seguire il principio di `divide-et-impera`: decomponendo un problema in sottolivelli logici indipendenti.
I task rappresentano un'unità di lavoro astratta, discreta e indipendente, totalmente disaccoppiata dalla gestione dei thread, che può essere eseguita in modo concorrente o parallelo.

#### Approcci e Limitazioni dei task

Inizialmente l'approccio più istintivo è stato quello di accoppiare `One-Thread-Per-Task`, presenta gravi svantaggi architetturali, come ad esempio, l'**overhead**, la creazionee gestione dei thread è costosa in termini di risorse e tempo, soprattutto se il numero di task è elevato, causando cosi un degrado delle prestazioni.
Ricordiamo anche che i platform thread sono risorse limitate, rischiando se creati eccessivamente di saturare la memoria del sistema.

### Master-Worker Pattern

È un'architettura concorrente progettata per separare la definizione e la sottomissione di un lavoro dalla sua effettiva esecuzione. Si basa su 3 componenti principali:

* **Master Agent**: Il suo compito è decomporre i task globali in sottotask più piccoli, inserendoli all'interno di ua struttura di coordinazione, e infine, si occupa raccogliere i risultati.
* **Worker Agents**: Sono un insieme di componenti attivi, il cui compito è esclusivamente quello di prelevare continuamente i task da eseguire dalla struttura contenete i subtasks.
* **Bag of Tasks**: È la struttura di coordinazione che funge da risorsa condivisa tra il master e i worker, viene tipicamente implementata tramite strutture come un [Bounded-Buffer](#bounded-buffer-vs-unbounded-buffer).

> Spesso l'architettura viene paragonata ad un problema di [Producer-Consumer](#producer-consumer-problem), dove il master è il produttore che produce i task, e i worker sono i consumatori che consumano i task.

#### Executor Framework

Il framework fornisce un supporto diretto per disaccoppiare la sottomissione di un task dalla sua effettiva esecuzione, implementando un'architettura [Master-Worker](#master-worker-pattern) attraverso l'utilizzo di un pool di thread, che agisce come un insieme di worker agents, e una coda di task, che funge da bag of tasks.

> I task devono essere rigorosamente **indipendenti**. Se un task si bloccasse in attesa del completamente di un altro task, finirebbe per bloccare il thread fisico, portando ad un rischio di deadlock.

##### Execution Policies & Thread Pool

Il framework permette agli sviluppatori di specificare le **Execution Policies**, configurando chi, dove, quando e come i task verranno eseguiti, si possono definire:

* **FixedThreadPool**: mantiene un numero costante di thread, mettendo in coda i task se tutti i thread sono occupati.
* CachedThreadPool: adatta dinamicamente il numero di thread, in base alle task.
* ScheduledThreadPool: permette di pianificare l'esecuzione dei task a intervalli regolari o dopo un certo ritardo.
* SingleThreadExecutor: garantisce che i task vengano eseguiti in sequenza (FIFO / LIFO), utilizzando un solo thread.

##### Gestione dei Risultati (Callable / Future)

Dato che la sottomissione dei task avviene in modo asincrono, è stato necessario introdurre due interfacce per gestire i risultati:

* **Callable\<V\>**: espone il metodo `call()` che incapsula una computazione in grado di ritornare un valore `V`, oppure un'eccezione, a differenza di `Runnable` che non restituisce nulla.
* **Future\<V\>**: la callable viene sempre wrappata in un oggetto `Future`, che rappresenta un risultato futuro, fornisce metodi per verificare se la computazione è completata (`isDone()`), per attendere il completamento e ottenere il risultato (`get()`), o per cancellare l'esecuzione del task (`cancel()`).

> Anche se entriamo nel mondo asincrono, è importante distinguere che questa implementazione **Future**, è implementata in modo da essere utilizzata in un contesto sincrono, difatto è necesario usare il metodo `get()` per attendere il completamento del task e ottenere il risultato, bloccando cosi il thread.
>
> Percui l'implementazione di **Future** nell'Executor Framework è **BLOCCANTE**, mentre in contesti asincroni, utilizza le **Callback** e rimane **NON BLOCCANTE**.

## Modellazione e Verifica Formale

### Formalismi Visivi

La modellazione è fonadamentale in un constesto concorrente, dato che può produrre risultati diversi a seconda dello scenario di esecuzione, è necessaria la verifica formale per dimostrare che le [proprietà di correttezza](#proprietà-di-correttezza-safety-vs-liveness) valgano in tutti i possibili scenari

#### Petri-Nets

Sono un formalismo visivo astratto per la modellazione del flusso di informazioni e di controllo in sistemi concorrenti e asincroni, sono rappresentate da un grafo bipartito, composto da due tipi di nodi:

* **Places**: rappresentano stati o condizioni del sistema, sono rappresentati da cerchi.
* **Transitions**: rappresentano eventi o azioni che possono verificarsi, sono rappresentati da rettangoli.

I **Tokens**, rappresentano l'occupazione di un determinato stato o condizione, e sono rappresentati da punti neri all'interno dei cerchi (places). La distribuzione dei token tra i places rappresenta lo stato attuale del sistema.

I tokens si trasferiscono tra i places attraverso le transitions, seguendo il verso delle freccie, CONTEMPORANEAMENTE, ovvero nel caso in cui, quando da due places si converge ad una transition, solo se entrambi i places possiedono un token, la transition viene attivata e i token sono trasferiti ai places seguendo l'ordine delle frecce in uscita (se solo una freccia il token si riduce ad uno solo).

> Le reti di petri-net sono estremamente potenti poichè permettono di catturare il non determinismo.

##### Proprietà Verificabili

* **K-Boundness**: è una [proprietà di safety](#proprietà-di-correttezza-safety-vs-liveness), utile per garantire che il sistemi non consumi risorse infinite. Una rete è k-bounded se il numero di token in qualsiasi posto non supera mai un certo valore k.
* **Liveness**: Una transazione di definisce morta se non esiste alcun percorso che la abilita. Se esiste almeno una transizione viva la rete è priva di starvation, in caso le transizioni sono vive per tutti gli stati raggiungibili, la rete è priva di deadlock.
* **Reachability**: Definisce l'insieme degli stati raggiungibili, questo assicura che nessuna sequenza possa portare ad uno stato fallato.
* **Reversibility**: Una rete è reversibile se da ogni stato raggiungibile è possibile tornare allo stato iniziale, garantendo cosi che il sistema non si blocchi in uno stato fallato.

### Linear Temporal Logic (LTL)

Permette di esprimere proprietà che devono valere lungo i percorsi di esecuzione, si basa su operatori temporali fondamentali, principalmente per mappare le [due proprietà di correttezza](#proprietà-di-correttezza-safety-vs-liveness):

* L'operatore **Always**: $\Box p$, è vera se e solo se $p$ è vera in tutti gli stati lungo il percorso di esecuzione.
  > Utilizzato per dimostarare le [Proprietà di Safety](#proprietà-di-correttezza-safety-vs-liveness)
* L'operatore **Eventually**: $\Diamond p$, è vera se e solo se $p$ è vera in almeno un stato lungo il percorso di esecuzione.
  > Utilizzato per dimostrare le [Proprietà di Liveness](#proprietà-di-correttezza-safety-vs-liveness)

### Model Checking (JPF)

Il Model Checking è una tecnica automatica per la verifica formale dei sistemi concorrenti.
Funziona esplorando tutti gli stati possibili del sistema (tutti i possibili interleaving) per verificare se determinate proprietà logiche e temporali sono soddisfatte per ogni stato.
Il problema principale del Model Checking è la **state explosion**, ovvero l'esplosione combinatoria degli stati, che può rendere la verifica inapplicabile per sistemi complessi.

Java Path Finder (JPF) è uno strumento di model checking per java, sviluppato dalla NASA operando direttamente a livello di bytecode, è in grado di eseguire il model checking su programmi Java concorrenti, esplorando tutti i possibili interleaving dei thread e verificando le proprietà di safety e liveness.
In caso di una violazione restituisce l'esatto `error path` che ha portato al fallimento.

## Performance & Benchmark

### Costi del Multithreading

Sebbene il multithreading aumenti l'utilizzo delle risorse e la reattività, porta con sé diversi costi che vanno a impattare le performance generali:

* **Context Switching**: Il passaggio da un thread all'altro richiede al sistema operativo di salvare lo stato del thread attivo e caricare lo stato del thread successivo, questo processo è costoso in termini di tempo e risorse.
* **Memory Synchronization**: I lock portano con sé un forte overhead, i lock cointesi tra molti thread costringono il sistema operativo a intervenire bloccando il flusso di esecuzione e accondando i thread.
* **Thread Management**: La creazione e distruzione dei thread richiede tempo e memoria.

### Speedup & Efficiency

Queste due metriche misurano i benefici del parallelismo:

* **Speedup (S)**: È il calcolo di quanto un programma va più veloce aggiungendo processori.
  $S = \frac{T_s}{T_p}$ dove:
  * $T_s$: è il tempo di esecuzione impiegato dall'algoritmo sequenziale.
  * $T_p$: è il tempo di esecuzione impiegato dall'algoritmo parallelo su N processori.
* **Efficienza (E)**: Misura quanto ogni processore viene effettivamente utilizzato, calcolata come il rapporto tra lo speedup e il numero di processori.
  $$E = \frac{S}{N}$$

### Amdahl's Law

La legge di Amdahl afferma che lo speedup massimo ottenivile è fortemente limitato dalla porzione di codice che deve obbligatoriamente restare sequenziale:

$$ S = \frac{1}{(1 - P) + \frac{P}{N}}$$

* $P$ è la percentuale di programma parallelizzabile.
* $1-P$ è la percentuale di programma che deve restare sequenziale.
* $N$ è il numero di processori.

### Sizing del Thread Pool (CPU.RuntimeCore + 1)

Dimensionare correttamente il thread pool è fondamentale per massimizzare le performance, un approccio comune è quello di utilizzare un numero di thread pari al numero di core disponibili più uno, questo per la **Rule for the compute intensive task case**, qualora uno dei thread dovesse temporaneamente sospendersi (ad esempio un banale page fault) ci sarà sempre un thread di riserva pronto a coprire il core fisico, in questo modo i cicli della cpu non andranno mai sprecati.

---

## [Assignment-01](./assignment01.md)

### [Report](./report1.pdf)
