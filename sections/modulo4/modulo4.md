# Modulo 4: Distributed Computing

## Fondamenti e Sfide dei Sistemi Distribuiti

### Le tre sfide: No shared memory, No shared clock, Partial failures

In un sistema distribuito, i nodi contengono processori indipendenti connessi eslcusivamente ad una rete. Questa natura distribuita introduce tre sfide fondamentali:

* **No shared Memory**: Non esiste nessuna memoria condivisa tra i processi.
* **No shared Clock**: Ogni nodo ha il proprio clock locale, che può divergere da quello degli altri nodi.
* **Partial Failures**: I messaggi possono essere persi, non si distibgue un nodo lento da un nodo che non risponde, questo rende la **faiulure detection** un problema complesso.

### L'illusione della Trasparenza

Un approccio iniziale e ingenuo, fu quello di applicare i paradigmi tradizionali nascondendo la rete al programmatore. Tecnologie middleware come **RPC** o **JavaRMI** nascono proprio con l'obiettivo di creare l'illusione che i processi risiedono nello stesso spazio di memoria, facendo sembrare una chiamata ad un metodo remoto del tutto identica ad una chiamata locale.

> Questo concetto è noto come **Trasparenza**.

#### Limiti dei paradigmi Tradizionali

Tentare di unificare il modello locale e quello distribuito, è un approccio fragile, una chiamata remota può causare problemi fisici: la **latenza**, la **gestione degli errori** e la **concorrenza**.

Per questo motivo, il paradigma distribuito ha richiesto approcci differenti, che tenga conto della natura dei sistemi distribuiti e delle loro sfide.

### Il Teorema CAP (Consistenza, Disponibilità, Tolleranza alle Partizioni)

Il Teorema CAP, stabilisce che in un sistema distribuito, è impossibile garantire simultaneamente tutte e tre le seguenti proprietà:

* **Consistenza (Consistency)**: Tutti i nodi vedono gli stessi dati nello stesso momento.
* **Disponibilità (Availability)**: Ogni richiesta riceve sempre una risposta, anche se non è garantito che sia l'ultima versione dei dati.
* **Tolleranza alle Partizioni (Partition Tolerance)**: Il sistema continua a funzionare anche in presenza di partizioni di rete (ovvero quando la comunicazione tra alcuni nodi è interrotta).

> Il Teorema CAP è profondamente legato alla **Latenza**, una **partizione può essere vista come un limite temporale imposto sulla comunicazione** (se entro un timeout non ricevo una risposta, considero il nodo come partizionato). In questo contesto, il sistema deve prendere la decisione: annullare l'operazione (non garantendo **Disponibilità**) oppure procedere lo stesso (rinunciando alla **Consistenza**).

## Il Modello di Computazione Distribuita

### Concetto di Tempo e Ordinamento degli Eventi

Per modellare il comportamento e ragiornare sulla correttezza di un sistema distribuito, è necessario astrarre in concetto di tempo e di ordinamento degli eventi:

* **Interleaving**: Presume l'esistenza di un **clock globale** che permette un **ordinamento totale degli eventi**, ma non è realistico in un sistema distribuito.
* **Happened-Before**: *Lamport* osservò che in un sistema distribuito, è possibile definire un **ordinamento parziale tra gli eventi**, basato sulla relazione di **happened-before**.

Come possiamo ordinare gli eventi? In un contesto completamente asincrono e distribuito?

![Happened-Before](/images/happened-before.webp)

Nell'esempio riportato nell'immagine, due nodi `p` e `q`, scambiano messaggi tra di loro.

* `p2: send(ch, 'a')`: è l'evento in cui il nodo `p` invia il messaggio `'a'` nel canale `ch`.
* `q2: receive(ch, 'a')`: è l'evento in cui il nodo `q` cerca messaggi nel canale `ch`.

Possiamo notare che l'evento `p2` è **happened-before** l'evento `q2`, perché il messaggio inviato da `p` deve essere ricevuto da `q` prima che quest'ultimo possa elaborarlo.

Costruendo il grafo delle raggiungibilità avremo, con certezza l'ordinamento dei seguenti eventi:
`p1 -> p2, p2 -> p3, p2 -> q2, q1 -> q2, q2 -> q3`
Affermando che per `q1 -> p3` non possiamo determinare l'ordinamento.

### Logical Clocks

Un **Orologio Logico** è una funzione che assegna un numero intero a ogni evento del processo p-esimo. Affinchè un orologio logico sia corretto deve rispettare la **Clock Condition**: $a \rightarrow b$, allora $C_i(a) \lt C_i(b)$.

#### Regole di aggiornamento dell'orologio

Per garantire la **Clock Condition**, ogni processo mantiene e aggiorna un proprio contatore locale tramite queste regole:

* **Eventi interni**: Quando un processo esegue un evento interno, incrementa il proprio contatore di 1, sia la send sia la receive di un messaggio sono considerate eventi interni.
* **Send**: Se l'evento $a$ consiste nell'invio di un messaggio, il processo include il contatore nel messaggio.
* **Receive**: Ricevendo il messaggio e il timestamp logico, il processo aggiorna il proprio contatore prendendo il valore massimo tra il proprio contatore locale e il timestamp ricevuto.

#### Limitazioni degli Orologi Logici

L'ordinamento garantito dal modello **Happened-Before** con gli orologici logici, hanno una limitiazione fondamentale. La **Clock Condition** consente di verificare che se due eventi $a \rightarrow b$, allora $C_i(a) \lt C_i(b)$, ma **non viceversa**, ovvero se $C_i(a) \lt C_i(b)$ non possiamo affermare che $a \rightarrow b$.

Esempio:
Se l'evento $a$ ha come orologio $3$ e l'evento $b$ ha come orologio $5$, la condizone: $L(a)<L(b)$ è verificata, ma non puoi essere sicuro che $a$ abbia causato $b$.

### Vector Clocks

Per superare le limitazioni degli orologi logici, sono stati introdotto i **Vector Clocks**, essi trasformano la **Clock Condition** in una relazione a doppia implicazione: $a \rightarrow b \iff V(a) < V(b)$.

Invece di usare un singolo numero intero, ogni processo mantiene un **vettore di contatori**, la cui dimensione k è pari al numero di processi nel sistema distribuito. Le regole di aggiornamento diventano:

* **Eventi interni**: il processo $P$, incrementa solo il proprio contatore $V[P]$ di 1, sia la send sia la receive di un messaggio sono considerate eventi interni.
* **Send**: Quando un processo $P_i$, invia un messaggio, include il vettore di contatori nel messaggio.
* **Receive**: Quando un processo $P_j$, riceve un messaggio, accompagnato da un vettore di contatori, aggiorna il proprio vettore di contatori prendendo massimo elemento per elemento rispetto al vettore ricevuto.

> Per confrontare due **vector clocks**: $v < w \iff v[i] \leq w[i]$ $\forall i$ e $\exists j, v[j] < w[j]$.
>
> Se i due vettori non sono confrontabili, allora gli eventi sono **concurrenti e indipendenti**.

## Algoritmi Distribuiti (Problemi di base)

### Mutua Esclusione Distribuita

Il problema della [mutua esclusione](/sections/modulo1/modulo1.md#mutua-esclusione-e-sincronizzazione) nei sistemi distribuiti richiede di garantire che due o più processi remoti non possano accedere contemporaaneamente alla medesima risorsa (sezione critica), rispettando al contempo le proprietà di [**liveness**](/sections/modulo1/modulo1.md#proprietà-di-correttezza-safety-vs-liveness), [**safety**](/sections/modulo1/modulo1.md#proprietà-di-correttezza-safety-vs-liveness) e [**fairness**](/sections/modulo1/modulo1.md#fairness).

#### Approccio Centralizzato (Coordinator)

Viene introdotto un nodo coordinatore, che ha il compito esclusivo di assegnare il token necessario per accedere alla sezione critica.

> L'approccio soffre di **Single Point of Failure**, se il coordinatore si guasta, l'intero sistema si blocca.

![Mutual Exclusion Coordinator](/images/mutual_exclusion-coordinator.webp)

Prendendo come esempio l'immagine riportata, a destra abbiamo un caso ottimale in cui non ci sono **problemi di fairness**, le richieste avvengono senza alcun intreccio.

##### Soluzione alla Fairness e alla Latenza

A sinistra invece, come fa il coordinatore ad aspettare la prima richiesta effettuata da `p1` affetta di latenza, se nel frattempo arriva prima una richiesta da `p2`?

La soluzione è quella di utilizzare i **vector clocks**, ogni processo mantiene un vettore di contatori, e quando esegue la richiesta, include il vettore nel messaggio, e lo propaga a tutti i processi $p_i$ connessi in rete.

> In questo modo `p2` includerà nel suo `vector clock` anche la richiesta di `p1`, grazie alla condizione intrinseca dei **vector clocks**, si riesce a garantire la **fairness**, anche in presenza di latenze asincrone e richieste che arrivano in ordine diverso da quello di invio.

*Nota teorica: I **Vector Clocks** sono sufficienti in questo scenario poiché l'ordinamento e la decisione spetta solo ed esclusivamente al coordinatore. [Vedere strumenti come i **Matrix Clocks** per scenari distribuiti.](#message-ordering-causal-e-total-ordering)*

#### L'Algoritmo Decentralizzato di Ricart-Agrawala

Per ovviare ai limiti dell'approccio centralizzato, è possibile adottare un approccio decentralizzato.
Ogni processo, quando vuole accedere alla sezione critica, invia una richiesta a tutti gli altri processi, e attende di ricevere una risposta `OK` da tutti prima di procedere.

![Mutual Exclusion Decentalized](/images/mutual_exclusion-ricart_agrawala.webp)

Per garantire la **fairness**, non è necessario l'implementazione di **vector clock**, è sufficiente un **logical clock**.

##### Proprietà fondamentali

* **Numero di nodi**: è fondamentali conoscere il numero di nodi totali $n$.
* **Numero di messaggi**: $2(n-1)$, dove $n$ è il numero di nodi.
* Il **canale di comunicazione** **non deve essere** necessariamente di tipo **FIFO**.

#### Limitazioni degli algoritmi di mutua esclusione distribuita

Entrambe le soluzioni di mutua esclusione distribuita, richiedono esplicitamente il numero di nodi totali $n$:

* Nel caso **Centralizzato**, i **vector clocks** devono essere allocati con dimensione $n$.
  > In questo caso è necessario anche un algoritmo di [**elezione del leader**](#elezione-del-leader-leader-election), per determinare il **coordinatore**.
* Nel caso **Decentralizzato**, ogni processo deve inviare la richiesta a tutti gli altri $n-1$ processi.

### Elezione del Leader (Leader Election)

Il problema dell'elezione consiste nello scegliere un leader, in un insieme di N nodi per assegnargli un ruolo di controllo o **coordinamento**.

> Quasi tutti gli algoritmi di elezione del leader, si basano sulla **logical ring topology**.

#### Algoritmo di Chang-Roberts (Ring)

L'algoritmo opera sotto l'assunzione che ogni nodo possieda un **identificativo unico**. L'obiettivo è eleggere come leader **il nodo con l'id più alto**.

Un nodo (anche più nodi) seguendo una topologia ad anello, invia il proprio **id** al nodo successivo.

Il nodo ricevente confronta l'id ricevuto con il proprio, e se il proprio id è maggiore, lo inoltra al nodo successivo, altrimenti scarta il messaggio.

> Costringe ad effettuare un secondo giro dell'anello per notificare a tutti i nodi chi è il leader eletto.
>
> Questo approccio richiede nel caso peggiore $2n - 1$ messaggi di elezione e $n$ messaggi di proclamazione del leader.

### Message Ordering (Causal e Total Ordering)

In un sistema distribuito, i messaggi possono arrivare in ordine diverso da quello di invio, a causa della natura asincrona della rete. Per garantire la correttezza del sistema, è necessario implementare meccanismi di **ordinamento dei messaggi**.

![Message Ordering & Matrix Clock](/images/matrix_clock.webp)

La figura mostra un esempio di **Matrix Clock**, che estende il concetto di **Vector Clock** per fornire informazioni più dettagliate sull'ordinamento dei messaggi tra i processi, mostrando:

* **Nelle righe**: I messaggi inviati da quel nodo.
* **Nelle colonne**: I messaggi ricevuti da quel nodo.

### Global Snapshot and Consensus

L'algoritmo di **Chandy-Lamport** permette di ottenere uno **snapshot globale** dello stato del sistema distribuito, catturando lo stato dei processi e dei canali di comunicazione in un momento specifico.

> Ci interessa per sapere se ad esempio il sistema è in uno stato di **deadlock**.

Il problema del **consenso** in un sistema distribuito, consiste nel garantire che tutti i nodi raggiungano un accordo su un valore comune, anche in presenza di guasti o comportamenti malevoli ([**Byzantine failures**](#consensus-under-byzantine-failures)).

Non puoi raggiungere un accordo (**Consenso**) se non hai una visione condivisa dello stato del sistema (**Global State**).

#### Algoritmo di Chandy-Lamport

È un algoritmo di **snapshot globale** che consente di catturare lo stato di un sistema distribuito in un momento specifico, senza interrompere l'esecuzione del sistema.

Il suo funzionamento si basa su un'assunzione fondamentale: tutti i canali di comunicazione tra i processi devono essere unidirezionali e garantire l'ordine **FIFO**:

1. **Gli stati e i Colori**: l'algoritmo assegna a ciascun processo il colore bianco, una volta che ha completato la sua fase di cattura dello snapshot, passerà al colore rosso.
2. **Inizio dello Snapshot**: un processo inizia lo snapshot catturando il proprio stato locale (diventando rosso) e inviando un messaggio speciale di **marker** a tutti i suoi canali in uscita.
3. **Ricezione del Marker**: quando un processo riceve un marker, il suo comportamente dipende dal colore del processo:
   * **Bianco**: cattura il proprio stato, diventa rosso e invia i **marker** a tutti i suoi canali in uscita.
   * **Rosso**: il processo aveva già catturato il proprio stato, il canale viene chiuso ai fini della cattura dello snapshot.
4. **Cattura dei messaggi in transito**: non appena un processo diventa rosso, inizia a memorizzare in un **buffer** interno tutti i messaggi in arrivo sul suo canale, in questo modo si catturano anche i messaggi in transito.

> L'algoritmo garantisce coerenza e un **Consistent Cut**, proprio grazie all'assunzione che i canali siano **FIFO**. Poichè i messaggi non possono sorpassarsi, **è fisicamente garantito che nessun processo Bianco riceverà mai un messaggio inviato da un processo Rosso**. Il **Marker** arriverà sempre prima, costringendo. ad ogni processo di diventare rosso prima di poter elaborare i messaggi in transito.

### Consensus Under Byzantine Failures

Immagina $N$ generali dell'esercito bizantino che accerchiano una città nemica. Devono decidere tutti insieme se Attaccare o Ritirarsi. Se attaccano solo in pochi, verranno sconfitti. Devono raggiungere il **Consenso**.

Il problema è che alcuni generali possono essere traditori e cercheranno di confondere i generali leali inviando ordini discordanti.

La teoria afferma che un sistema distribuito può tollerare al massimo $m$ nodi traditori solo se ci sono in totale almeno $N=3m+1$ nodi.

**Esempio**: Se hai 1 traditore ($m=1$), hai bisogno di almeno $3(1)+1=4$ generali in totale per raggiungere il consenso.

## Message-Oriented Middleware

### Il disaccoppiamento Spaziale e Temporale

Il **vantaggio architetturale** più profondo introdotto dai **MOM** è il superamento del forte [accoppiamento](#limiti-dei-paradigmi-tradizionali) tipico dei modelli tradizionali come **RMI**.

I nodi interagiscono unicamente con il **Middleware**. Questo permette di aggiungere, rimuovere o scalare i nodi del sistema in modo del tutto indipendente.

I MOM offrono un modello di comunicazione **persistente**. I messaggi vengono memorizzati in modo sicuro all'interno del middleware. Se un consumatore è offline, il **Middleware** conserva il messaggio e glielo recapita non appena torna disponibile.

### Code asincrone, Message Broker e Quality of Service (QoS)

Il cuore di un'architettura **MOM** è basato sull'**interazione** puramente **asincrona e non bloccante**.

I componenti fisici e logici che rendono possibile questo meccanismo sono:

* **Message Queues**: Le code sono fondamentali per il concetto di immagazzinare i messaggi i dati. Possonoo essere pubbliche, private temporanee o dead-letters (messaggi non recapitabili).
* **Message Broker**: Il **broker** è un componente attivo che si occupa di gestire le code, gestire il routing dinamico e lo smistamento dei messaggi.
* **Quality of Service (QoS)**: È possibile configurare delle politiche per garantire la persistenza dei messaggi, la loro consegna e l'ordine di recapito.

## Attori Distribuiti

### Da ActorSystem locale a Cluster distribuito

Se in locale l'**ActorSystem** gestisce i thread e il ciclo di vita degli attori su una singola macchina, in un **ambiente distribuito** l'applicazione si espande su più ActorSystem, ognuno dei quali viene eseguito come un nodo logico distinto.

Un **nodo** all'interno del **cluster** è **identificato** univocamente da `hostname:port:uid`, il che permette di far girare anche più nodi logici sulla medesima macchina fisica.

### Protocolli Gossip e Failure Detection

Il **protocollo Gossip** è un meccanismo di comunicazione distribuita che permette ai nodi di scambiarsi informazioni in modo efficiente e scalabile.
I nodi si scambiano periodicamente la propria "visione" dei membri del sistema; per stabilire l'ordine degli aggiornamenti e risolvere i conflitti temporali, grazie ai **Vector Clocks**.
Il sistema raggiunge la convergenza quando tutti i nodi raggiungibili hanno osservato l'aggiornamento corrente.

La **Failure Detection** si basa sul sospetto, si esegue un heartbeat di rete come un "livello di sospetto" continuo. Quando un nodo viene ritenuto irraggiungibile, questa informazione si propaga nel cluster tramite il **gossip**.

### Split Brain e Split Brain Resolver (SBR)

Quando un cluster distribuito subisce una **partizione di rete**, i nodi possono perdere la capacità di comunicare tra loro, portando alla creazione di due o più sottogruppi di nodi che operano in modo indipendente. Questo fenomeno è noto come **Split Brain**.

Per ripristinare la rete in modo sicuro, si usa lo **Split Brain Resolver (SBR)**, un meccanismo che applica strategie deterministiche (come mantenere la fazione con la maggioranza dei nodi, **keep-majority**) per far sì che solo un lato della partizione sopravviva e l'altro venga abbattuto.

## Service-Oriented Architectures (SOA) e Microservizi

### Dal concetto di Servizio al Microservizio

**Service-Oriented Architecture (SOA)** è uno stile architetturale in cui la logica di business di un'applicazione è scomposta in unità più piccole e distinte chiamate **servizi**.

L'evoluzione naturale della SOA ha portato allo stile a **Microservizi**. Mentre un'applicazione monolitica viene sviluppata e rilasciata come un singolo blocco eseguibile, l'architettura a microservizi sviluppa l'applicazione come una suite di piccoli **servizi autonomi indipendenti**, ciascuno in esecuzione nel proprio processo e comunicante tramite meccanismi leggeri.

### Web Services, API RESTful e Choreography vs Orchestration

Come comunicano i microservizi tra di loro (API)? Esistono diversi approcci:

* **Web Services**: Utilizzano protocolli standard come **SOAP** e **WSDL** per la comunicazione tra servizi.
  * **SOAP**: Un protocollo basato su XML per lo scambio di messaggi tra servizi, che definisce un formato standard per le richieste e le risposte.
  * **WSDL**: Un linguaggio basato su XML che descrive le interfacce dei servizi web, specificando le operazioni disponibili e i tipi di dati utilizzati.
* **API RESTful**: Utilizzano **HTTP** e principi **REST** per la comunicazione tra servizi, favorendo l'uso di risorse e operazioni standard.
* **Choreography**: I servizi interagiscono tra loro in modo autonomo, senza un coordinatore centrale, seguendo regole predefinite.
* **Orchestration**: Un servizio centrale (orchestratore) coordina le interazioni tra i servizi, gestendo il flusso di lavoro e le dipendenze.

### Event-Driven Microservices

Per superare le sfide legate alla latenza di rete e all'accoppiamento dei modelli sincroni, le architetture moderne si stanno spostando massicciamente verso i Microservizi guidati dagli eventi (Event-Driven Microservices) e le architetture basate su flussi.

In questo paradigma, lo scambio di informazioni non avviene tramite chiamate dirette, ma appoggiandosi a un middleware intermedio. C'è però una differenza fondamentale introdotta di recente nel mondo dei Message-Oriented Middleware (MOM): il passaggio dai tradizionali **Message Brokers** agli **Event Brokers**:

* **Message Brokers**: Una volta che un messaggio è stato elaborato con successo e confermato (acknowledged), il broker elimina e distrugge il messaggio.
* **Event Brokers**: Non cancella gli eventi dopo la lettura, li conserva per tutto il tempo necessario all'organizzazione.

> In questo modo, l'**event broker** diventa la **Single Source of Truth**

---

## [Assignment-04](./assignment04.md)

### [Report](./report4.pdf)
