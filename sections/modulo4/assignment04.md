---
layout: math
---
# Teoria Assignment-04

Generated File with AI

## Parte 1: RPC e Oggetti Distribuiti (Java RMI - Tic-Tac-Toe)

* **Architettura a Callback (RPC Bidirezionale):** Hai passato il riferimento del `RemoteClientListener` al server per permettere a quest'ultimo di invocare metodi sul client (push degli aggiornamenti).
* **Rilevamento Guasti tramite Heartbeat:** Per ovviare alla passività del server RMI (che non sa se un client crasha), hai implementato un `Timer` che effettua ping regolari verso i client. Se lancia `RemoteException`, chiudi la stanza e liberi risorse.
* **Pattern "Open Calls":** Hai protetto lo stato del gioco con blocchi `synchronized(this)`, ma hai avuto l'accortezza di **uscire dal blocco** prima di effettuare chiamate di rete verso i client, prevenendo deadlock distribuiti.
* **Gestione Risorse:** Chiusura pulita delle lobby tramite `UnicastRemoteObject.unexportObject` per evitare memory leak e "stanze zombie".

**Concetti teorici da studiare:**

* **RPC e RMI:** Come funziona teoricamente la Remote Procedure Call. I concetti di *Stub* (lato client) e *Skeleton* (lato server). La differenza tra passaggio per valore (richiede `Serializable`) e passaggio per riferimento (esportazione dell'oggetto remoto).
* **Trasparenza in Sistemi Distribuiti:** Perché la trasparenza totale (far sembrare una chiamata di rete identica a una locale) è considerata un'illusione (falsità della rete affidabile, latenza, ecc.).
* **Distributed Deadlock:** Cos'è e perché il pattern delle *Open Calls* è la soluzione architettonica standard per evitarlo quando server e client si chiamano a vicenda.

---

## Parte 2: Attori Distribuiti (Pekko Cluster - Alarm System)

* **Service Discovery:** Hai abbandonato la creazione gerarchica fissa locale (`context.spawn`) in favore del `Receptionist`. Gli attori (es. Keypad) si iscrivono per scoprire dinamicamente dove si trova il Guardian nella rete.
* **Seed Nodes e SBR:** Hai configurato nodi *Seed* multipli e impostato la strategia *Split-Brain Resolver (SBR)* su `keep-majority` per garantire la sopravvivenza del cluster anche se un nodo fondatore crasha.
* **Safe Recovery Mode:** Se il nodo Guardian riparte dopo un crash perdendo il suo stato in memoria, l'hai programmato per avviarsi in uno stato di `recovery` (sicuro), sbloccabile solo tramite l'inserimento del PIN.
* **Serializzazione:** Hai applicato Jackson CBOR tramite il trait `CborSerializable` per permettere ai messaggi di viaggiare sulla rete TCP/UDP.

**Concetti teorici da studiare:**

* **Teorema CAP:** Consistenza, Disponibilità (Availability) e Tolleranza alle Partizioni. Come Pekko Cluster gestisce le partizioni di rete (Split-Brain) e perché la strategia `keep-majority` sacrifica la disponibilità della minoranza per garantire la consistenza.
* **Gossip Protocol:** La teoria dietro a come i nodi di un cluster decentralizzato (Pekko) si scambiano informazioni sullo stato della rete senza un coordinatore centrale.
* **Fallimenti nei Sistemi ad Attori:** La differenza tra gestire un fallimento locale (Supervisione) e un fallimento di rete (raggiungibilità, nodi *Down* o *Unreachable*).

---

## Parte 3: MOM e Algoritmi Distribuiti (RabbitMQ - Ricart-Agrawala)

* **Architettura Pub/Sub Dinamica:** Uso di un exchange `TOPIC` di RabbitMQ. I nodi scoprono la rete inviando un `HELLO` in broadcast e gestiscono i ritardatari con risposte `HELLO` bidirezionali.
* **Ricart-Agrawala:** L'algoritmo distribuito decentralizzato per la mutua esclusione. Per entrare in sezione critica, si chiede il permesso a *tutti* e si attende un `OK` da tutti gli $N-1$ nodi.
* **Orologi Logici e Tie-Breaking:** Gestione dell'ordinamento delle richieste tramite incrementi del logical clock. Risoluzione dei pareggi deterministica basata sull'ID minore del nodo.
* **Deferred Replies:** Inserimento delle richieste con priorità minore in un Set `pendingNodes`, per sbloccarle inviando gli `OK` solo durante l'esecuzione di `exitCS()`.

**Concetti teorici da studiare (Rif. Module 4.2 & MOM):**

* **Modelli MOM (Message-Oriented Middleware):** Differenza tra code *Point-to-Point* e code *Publish/Subscribe*. Il ruolo del *Broker* (RabbitMQ) nel disaccoppiare mittente e destinatario spazialmente e temporalmente.
* **Happened-Before e Orologi Logici:** La teoria di Lamport. Perché in un sistema distribuito non possiamo usare il timestamp di sistema (clock globale assente) e come gli orologi logici creano un ordinamento parziale (e totale se si usa il tie-breaking con l'ID).
* **L'algoritmo Ricart-Agrawala (Teoria Pura):** Devi sapere esattamente il costo in termini di messaggi: $2(N-1)$ messaggi per entrare in sezione critica (una request a tutti, un OK da tutti).

---

## 💡 Le domande "Trappola" per il 30 e Lode

Queste domande incrociano i paradigmi distribuiti e testano la tua reale visione ingegneristica:

### 1 **Gestione dei Crash (RMI vs MOM):** *"In RMI hai dovuto inventare un Heartbeat per scoprire se un client era morto. Nell'esercizio di Ricart-Agrawala su RabbitMQ, cosa succede teoricamente all'intero sistema se un nodo crasha mentre qualcun altro sta aspettando il suo 'OK' per entrare in sezione critica?"*

**Risposta attesa:** Senza meccanismi aggiuntivi, l'algoritmo standard di Ricart-Agrawala va in **deadlock totale**, perché il nodo richiedente attenderà all'infinito l'OK del nodo morto. Richiederebbe l'implementazione di timeout o rilevatori di guasti.

### 2 **Architetture a confronto (Centralizzato vs Decentralizzato):** *"Perché implementare la Mutua Esclusione con Ricart-Agrawala (decentralizzato) invece che usare un singolo Nodo Coordinatore?"*

**Risposta attesa:** Il nodo coordinatore è un *Single Point of Failure (SPOF)* e un collo di bottiglia per le performance. Ricart-Agrawala elimina lo SPOF, ma di contro genera molto più traffico di rete ($2(N-1)$ messaggi contro i soli 3 messaggi dell'approccio centralizzato: Request, Grant, Release).

### 3 **Pekko Cluster e Split Brain:** *"Spiegami lo scenario dello Split-Brain. Se abbiamo 5 nodi Seed e la rete si divide isolando 2 nodi da una parte e 3 dall'altra, cosa fa esattamente la strategia 'keep-majority' che hai configurato?"*

**Risposta attesa:** La partizione con 3 nodi capisce di avere la maggioranza e dichiara "Down" i 2 nodi mancanti, continuando a funzionare. La partizione con 2 nodi capisce di essere in minoranza e si auto-termina per evitare di prendere decisioni inconsistenti col resto del cluster.

### 4 **Trasparenza RMI vs Asincronia Pekko:** *"Perché RMI è considerato un approccio 'vecchio' (o accoppiato) rispetto agli Attori di Pekko in un sistema distribuito?"*

**Risposta attesa:** RMI tenta di nascondere la rete usando chiamate sincrone bloccanti (RPC), che portano a thread fermi in attesa di risposte e vulnerabilità ai delay di rete. Pekko abbraccia la natura asincrona della rete col message passing "fire-and-forget", rendendo la gestione di latenze e fallimenti un concetto nativo di design.
