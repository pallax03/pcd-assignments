# Modulo 3: Actor-Based Message-Passing

## Il Paradigma del Message-Passing

### Superamento della memoria condivisa: il dogma del "No Shared State"

Il paradigma abbandona completamente la memoria condivisa, l'unico modo in cui queste entità possono interagire, è univocamente attraverso lo **scambio di messaggi**.
Questo approccio mira proprio a eliminare alla radice i meccanismi di sincronizzazione e mutua esclusione.

### Primitive di base: Send e Receive (Semantiche e Non-Determinismo)

Un aspetto fondamentale per queste primitive è la gestione dell'attesa.
Mentre una `send` può essere **asincrona e non bloccante** (se c'è spazio nel buffer), la `receive` è intrinsecamente **bloccante**, se non ci sono messaggi pronti **l'attore** si sospende.

La [**guarded communication**](#guarded-communication-il-costrutto-select-e-la-scelta-non-deterministica) mira proprio a introdurre una scelta **non deterministica** in caso di più canali, senza rimanere bloccati alla prima receive.

### Architettura dei Sistemi: Componenti Attivi vs Monitor Passivi

Questo paradigma impona un radicale ripensameto di come progettiamo l'architettura dei nostri sistemi, specialmente nel modo in cui proteggiamo le risorse.

> Un [monitor](/sections/modulo1/modulo1.md#monitor) è un componente passivo. Non ha un proprio flusso di controllo, espone solo delle procedure che vengono chiamate dai componenti attivi, garantendo mutua esclusione e sincronizzazione interna.

Al contrario nei modelli **Message-Passing**, i componenti attivi (solo gli attori), possiedono ognuno la propria memoria, e comunicano esclusivamente attraverso lo scambio di messaggi. Non esistono più risorse condivise, e quindi non c'è più bisogno di meccanismi di sincronizzazione come mutex o semafori.

Questo approccio garantisce un fortissimo disaccoppiamento tra i componenti, seguendo anche la filosofia del *Let It Crash*, permettendo ai supervisori di degli attori di riavviare un attore fallito senza impattare sugli altri, e senza dover gestire complessi stati di errore condivisi.

### Comunicazione Sincrona (Rendez-vous) vs Asincrona (Code FIFO e Buffer)

Il paradigma si differenzia in base al contesto in cui avviene la comunicazione, in maniera molto simile al caso del [bounded buffer](/sections/modulo1/modulo1.md#contesto-sincrono-e-ascincrono).

* **Comunicazione Asincrona**: Solitamente in un modello ad Attori (come Pekko), si utilizza un **FIFO buffer**, la `send` accoda il messaggio sul buffer, mentre la `receive` si blocca solo in caso di buffer vuoto.
* **Comunicazione Sincrona**: È alla base del formalismo [CSP](#il-modello-csp-communicating-sequential-processes), adottato nei **channel non bufferizzati in Go**. Entrambe la `send` e la `receive` sono bloccanti, aspettandosi a vicenda, lo scambio diventa un passaggio atomico.

> La comunicazione Sincrona si ispira fortemente al concetto di [*Rendez-vous*](/sections/modulo1/modulo1.md#barrier--latch-rendezvous-problem), in cui due processi si incontrano per scambiare informazioni.

## Il Modello CSP (Communicating Sequential Processes)

### Fondamenti teorici

Nel modello CSP originale, i canali sono entità anonime, sono i processi ad avere un nome e un'identità. (es. Invia questo messaggio al processo P)

Il modello CSP nei linguaggi moderni, si distingue per due caratteristiche architetturali precise: **i canali** possiedono **un nome** e **un'identità** ben definita, mentre i processi che li utilizzano sono entità anonime. (es. Invia questo messaggio sul canale C)

> In questo paradigma, la **sincronizzazione** avviene direttamente sul mezzo trasmissivo, il **canale**.

### Componenti Attivi (Attori) e Goroutines

> I **componenti attivi**, gli attori sono implementati tramite le **goroutines**.

A differenza dei **Platform Threads**, le **goroutines** condividono in modo efficiente e sicuro un numero limitato di thread fisici sottostanti, venendo **mounted** e **unmounted** in base alla necessità, come avviene per i [Virtual Threads](/sections/modulo2/modulo2.md#architettura-interna-carrier-threads-e-mountunmount).

### Channels e la loro Struttura

I **canali** sono l'infrastruttura attraverso cui le **goroutines** scambiano messaggi, l'accesso al contenuto di ogni canale è **atomico**.

Il **canale** non è un semplice spazio di memoria volatile. È una struttura dati autonoma che possiede:

* Un proprio indirizzo in memoria o un identificativo univoco.
* Un proprio stato interno (es. **un buffer se è asincrono**).
* Le proprie regole di sincronizzazione (es. politiche di rendezvous).

> I processi non comunicano "puntando" l'uno all'altro, ma puntando all'identità del canale.

### Guarded Communication: il costrutto `select` e la scelta non-deterministica

Come fa un processo a ricevere messaggi che possono arrivare su canali multipli nello stesso istante, senza bloccarsi in attesa su un canale vuoto?

Fu Dijkstra a implementare la **Guarded Communication**, mira proprio a introdurre un espressione booleana che permette di abilitare o disabilitare la ricezione di un messaggio, in base a condizioni specifiche.
La semantica di risoluzione, in un ambiente multicanale, consiste in:

* Un sistema valuta tutte le guardie, controllando la condizione (es. se il buffer non è vuoto)
* Se più di una guardia viene verificata, il sistema ne sceglie una in modo **non-deterministico**.
* Se tutte le guardie sono bloccate, la **goroutine** viene sospesa.

> In un ambiente a singolo canale la **guarded communication** rimane comunque bloccante, ma solamente dopo che la **goroutine** finisce tutte le sue task.

## Il Modello ad Attori: Riferimento ad Apache Pekko (Akka)

### Scelte Architetturali

Nei modelli classici, la sincronizzazione tramite monitor, lock e semafori porta spesso a problemi di [race condition](/sections/modulo1/modulo1.md#problemi-di-interferenza-race-conditions).
Il modello ad attori abbraccia la filosofia per cui **"tutto è un'attore"**, l'interazione avviene solo tramite lo **scambio asincrono di messaggi**, e **ogni attore** possiede il **proprio stato privato**, eliminando completamente la possibilità di interferenze.

### Anatomia di un Attore: Stato privato, Comportamento (Behavior) e Mailbox

Un attore è **un'entità** computazionale **autonoma** e puramente **reattiva** che incapsula anche il **proprio flusso di controllo** logico.
È strutturato dai seguenti elementi chiave:

* **Identificatore Univoco (ID / ActorRef)**: Necessario affinchè gli attori possano comunicare tra loro, è l'unico modo per indirizzare un messaggio a un attore specifico.
* **Mailbox privata**: Ogni attore dispone della propria coda di messaggi.
* **Stato incapsulato**: Lo stato di un attore è completamente privato e non accessibile da altri attori.
* **Comportamento (Behavior)**: Definisce come un attore risponde ai messaggi (della sua **Mailbox**), può essere dinamico e modificabile nel tempo.

### Le dinamiche di elaborazione alla ricezione di un messaggio

Quando un attore elabora un messaggio, il suo comportamento si declina esclusivamente in tre azioni fondamentali:

#### 1. Invio di un messaggio

L'attore può inviare messaggi (`send`) in modo asincrono, verso altri attori di cui conosce l'identificatore. L'invio è asincrono e non bloccante, il mittente non si blocca in attesa di una risposta, semplicemente si mette in ascolto per gestire eventuali messaggi ricevuti nella sua **Mailbox**.

> La semantica garantisce la **fairness**.

In Pekko i messaggi possono essere inviati in tre modalità:

* **Fire and Forget** `!`: Il classico scambio di messaggi ascincrono e non bloccante.
* **Self-Message** (timer): Un attore può inviare messaggi a se stesso, grazie ad esempio ad un **timer**.
* **Ask Pattern & Future** `?`: L'attore invia un messaggio e si aspetta una risposta diretta sotto forma di Future. Pekko crea un attore temporaneo invisibile per catturare la risposta.
  > Attenzione dato che il modello puro prevede solo scambi asincroni: va usato con parsimonia perché reintroduce una forma di accoppiamento temporale!

#### 2. Dynamic Creation `context.spawn`

L'attore può istanziare dinamicamente nuovi attori specificandone il comportamento iniziale `Behaviour`, in modo da delegare e parallelizzare specifiche task.

#### 3. State/Behavior change

Originariamente , ogni attore puoteva modificare il proprio comportamento in risposta ai messaggi ricevuti, grazie ai costrutti `become` e `unbecome`.

Nei modelli più moderni, **come Pekko**, si è preferito adottare un **approccio più funzionale**, in cui il cambio di stato non avviene tramite mutazione interna, ma restituendo immutabilmente una nuova istanza di `Behavior` al termine dell'elaborazione del messaggio.

> Adattandosi dinamicamente alle condizioni del sistema, può implementare logiche di gestione dello stato più complesse, come ad esempio la realizzazione di macchine a stati finiti.

### Tolleranza ai guasti e Gerarchie di Supervisione

Come abbiamo introdotto, il paradigma [Message-Passing](#il-paradigma-del-message-passing) sposa la filosofia del ["Let it crash"](#architettura-dei-sistemi-componenti-attivi-vs-monitor-passivi).
> A differenza dei modelli tradizionali basati sul try-catch difensivo (che mischia la logica di business con la gestione degli errori).

Introduciamo cosi la gerarchia consigliata da pekko, essa permette di garantire supervisione e tolleranza ai guasti:
![Gerarchie di Supervisione](https://pekko.apache.org/docs/pekko/1.3/images/actor-paths-overview.png)

> Se un attore incontra un'eccezione imprevista, il suo thread non fa crashare l'intero sistema. L'attore si sospende, la sua mailbox viene messa in pausa, e il fallimento viene notificato come un messaggio speciale al suo padre.

#### Startegie di Supervisione

Il padre agisce da **supervisore** e deve decidere la strategia di ripristino per il figlio fallito:

* **Resume**: Ignora l'errore e fa ripartire l'attore mantenendo il suo stato attuale.
* **Restart**: Distrugge l'istanza corrotta e ne crea una nuova (resettando lo stato), ma preservando la Mailbox.
* **Stop**: Uccide permanentemente l'attore.
* **Escalate**: Se il padre non sa come gestire l'errore, fallisce a sua volta e delega il problema al suo supervisore (risalendo la gerarchia).

> Il supervisore può decidere se applicare la direttiva solo all'attore che ha fallito (**One-For-One**) o, se gli attori sono fortemente dipendenti tra loro, riavviare tutti i suoi figli contemporaneamente (**All-For-One**).

---

## [Assignment-03](./assignment03.md)

### [Report](./report3.pdf)

---

## [Index](/index.md)
