---
layout: math
---
# Modulo 2: Asynchronous & Reactive Programming

## I Limiti del Multithreading Tradizionale

### Il problema dell'I/O Bound e il blocco dei Thread

Nelle applicazioni I/O Bound, quando un task esegue un'operazione di I/O, il thread sottostante si blocca in attesa della risposta, sospendendo la propria esecuzione. Se assegnassimo un thread a ciascun task, i thread passeranno la maggior parte del tempo *dormendo* sprecando risorse preziose.

### Limiti architetturali

Come abbiamo accennato precedentemente, l'approccio [`One-Thread-Per-Task`](/sections/modulo1/modulo1.md#approcci-e-limitazioni-dei-task), in un contesto di I/O Bound, non può scalare, il sistema operativo finirebbe le risorse e farebbe crashare il tutto causando un `OutOfMemoryError`.

## Asynchronous & Event-Driven Programming

### L'Architettura a Event-Loop (Reactor Pattern)

L'architettura a **Event-Loop** è il motere di controllo alla base della **programmazione Event-Driven**. Si basa su un singolo thread di controllo fisico che attende costantemente gli eventi da ina coda e gli smista ai rispettivi event-handler.

Il thread estrae un evento dalla coda e lo esegue in modo atomico, non c'è concorrenza ogni evento viene gestito in modo atomico.

#### Multiple Event Loops

Sistemi comlpessi possono prevedere architetture complesse, in cui diversi componenti attivi possiedono il proprio Event Loop, Questi componenti non condividono memoria, ma interagiscono tra di loro univocamente scambiandosi eventi.

### Il dogma del "Never-Blocking"

È obbligatorio far si che l'intero Event Loop non si blocchi mai, altrimenti l'intero sistema si bloccherebbe. Se un evento consiste in un'operazione di I/O, questa deve essere eseguita in modo asincrono, e il thread di controllo deve essere liberato per gestire altri eventi.

Semplicemente per operazioni bloccanti il thread delega l'esecuzione ad un thread in background. Ma come fa a sapere quando l'operazione è terminata? Il thread di controllo non può aspettare, quindi deve essere notificato in qualche modo.
Questo ha generato diverse evoluzioni:

### Callbacks e Continuation-Passing Style (CPS)

L'implementazione più semplice è stata, aggregare una funzione da far eseguire all'event loop quando l'operazione bloccante è terminata.
Una volta che il task in background termina la sua esecuzione, invoca un evento per l'event loop con la funzione di callback da eseguire.

In programmazione funzionale, questo approccio è noto come **Continuation-Passing Style (CPS)**, in cui la funzione di callback rappresenta la continuazione del flusso di esecuzione. Esempio:

```scala
def sumCPS(x: Int, y: Int, cont: Int => Unit): Unit = {
  cont(x + y)
}
def multCPS(x: Int, y: Int, cont: Int => Unit): Unit = {
  cont(x * y)
}

sumCPS(5, 3, s => {
  println(s"5 + 3 = $s")
  multCPS(s, 2, r => {
    println(s"8 * 2 = $r")
  })
})
```

> Importante: L'esecuzione delle operazioni in un'architettura a **Event-Loop**, sono tutte eseguite dall'event-loop, solamente le operazioni bloccanti sono delegate ad un task in background, ma comunque la CPS viene eseguita dall'event-loop.

#### Il problema del "Callback Hell" (Pyramid of Doom)

Come possiamo notare anche nell'esempio sopra, l'uso di callback annidate porta a un codice difficile da leggere e mantenere, noto come **Callback Hell** o **Pyramid of Doom**. Questo problema è stato affrontato con l'introduzione di nuove astrazioni come **Promises e Futures**.

### Promises e Futures

Agiscono come dei **proxy**, per un risultato futuro, esse incapsulano l'azione asincrona, una prmise può trovarsi in 3 stati fondamentali:

* **Pending**: la promise è stata creata, ma ancora l'operazione è in corso.
* **Resolved (Fulfilled)**: L'operazione ascincrona si è conclusa con esito positivo, la promise contiene il risultato.
* **Rejected**: L'operazione asincrona è fallita, conterrà l'errore o il motivo del fallimento.

#### Promise Chaining

Nascono proprio per risolvere il [Pyramid of Doom / Callback Hell](#il-problema-del-callback-hell-pyramid-of-doom).

```javascript
let promise = new Promise((resolve, reject) => {resolve(1);});

let p1 = promise.then(result => {
  console.log(result); // 1
  return result + 2;
}).then(result => {
  console.log(result); // 3
});
```

#### Error Handling

In caso la promise fallisca, l'errore salta alla prima promise di gestione dell'errore, se non c'è, l'errore viene propagato fino a che non viene gestito o fino alla fine del programma.

#### Composizione Parallela

Le Promise offrono API avanzate per orchestrare più task, come `Promise.all` che attende che tutte le promise siano risolte, o `Promise.race` che restituisce il risultato della prima promise risolta.

#### Limitazioni

Nonostante migliorino enormemente la gestione asincrona, le Promise presentano difetti intrinseci:

* **Eagerness**: Le Promise sono "impazienti". Nel momento stesso in cui una Promise viene istanziata tramite `new Promise(...)`, il codice asincrono al suo interno inizia immediatamente la sua esecuzione.
* **No Loops**: Non è possibile utilizzare costrutti di iterazione come `for` o `while` direttamente con le Promise.
* **Impossibilità di Cancellazione**: Se un'operazione asincrona è in corso, non c'è modo di interromperla o di ignorarne il risultato.

### Async/Await

Il suo obiettivo principale è fornire allo sviluppatore uno stile di programmazione sincrono, imperativo e sequenziale, pur mantenendo sotto il cofano una semantica e un'esecuzione puramente asincrona. Il costrutto si basa sulle due parole chiave:

* **async**: Una funzione dichiarata con `async`, restituirà sempre una **promise**.
* **await**: Può essere usato solo all'interno di funzioni `async`, il suo scopo è sospendere l'esecuzione della funzione corrente fino a quando la Promise non viene risolta o rigettata.

> La `await` non blocca in alcun modo il flusso di controllo del thread (o dell'Event Loop). Quando la **Promise** attesa viene finalmente risolta in background, l'evento di completamento viene **re-inserito nell'Event Loop**. Non appena possibile, la funzione `async` viene "risvegliata" e riprende la sua esecuzione **esattamente dal punto in cui era stata sospesa**.

#### Utilizzo delle await

L'operatore await si occupa anche di "scartare" automaticamente la Promise, mascherandola da chiamata sincrona:

* Se la Promise ha successo, `await` estrae il valore interno e lo restituisce direttamente.
* Se la Promise è fallita, `await` lancia fisicamente un'eccezione contenente il motivo del rifiuto.

#### Co-routine

Per implementare la semantica di `async/await`, il compilatore trasforma la funzione `async` in una **co-routine**. Una co-routine è un'unità di esecuzione che può essere sospesa e ripresa, mantenendo il proprio stato interno. Quando si incontra un `await`, la co-routine viene sospesa, e quando la Promise viene risolta, la co-routine viene ripresa.

#### Limitazioni di Design

Il costrutto nasconde insidie molto pericolose a livello di design concorrente:

* **Perdita di Atomicità e Race Conditions**: All'interno di una funzione `async`, **non è più garantita l'atomicità**. Poiché un `await` cede il controllo all'Event Loop, **l'esecuzione logica di un singolo blocco può frammentarsi ed estendersi su molteplici iterazioni dell'Event Loop**. Se altre funzioni asincrone modificano lo stato condiviso durante questa sospensione, si verificano inaspettate Race Conditions estremamente difficili da debuggare.
* Mescolare codice sincrono bloccante e asincrono guidato da eventi crea un netto contrasto architetturale.
* **Vincolo del Livello Globale**: Poiché `await` può esistere solo dentro una funzione `async`, non è possibile usare un `await` direttamente nel corpo globale del programma.
* **Composizione Parallela Complicata**: Se l'obiettivo è lanciare task in parallelo e aspettarli tutti, non è possibile farlo solo con la sintassi `await`. È necessario mescolare `async / await` con le [composizioni di promise](#composizione-parallela).

## Reactive Programming - Rx

### Oltre gli eventi singoli: i Data Streams (Flussi asincroni)

La **programmazione reattiva** è un paradigma orientato ai flussi di dati e alla propragazione automatica dei cambiamenti. L'idea centrale è l'astrazione dei valori che cambiano nel tempo in veri e propri **data streams asincroni**.

La programmazione reattiva astrae i valori che cambiano nel tempo fornendo due astrazioni reattive fondamentali:

* **Event Streams**: Rappresentano una sequenza di eventi discreti intermittenti, come il click del mouse o i tast premuti.
* **Behaviours (o Signals)**: Rappresentano un flusso di dati fluido e ininterrotto derivante da un evento costante, come ad esempio il tempo che scorre in un timer.

### Il Pattern Observable/Observer (Push vs Pull)

Vengono combinate le migliori idee del pattern **Observer**, trasformando qualsiasi sorgente asincrona in un data stream osservabile.
A livello di propragazione dei cambiamenti, esistono due modelli di valutazione fondamentali:

* **Pull-based** (Demand-driven): È il consumatore che richiede i dati dalla sorgente. In `Rx`, sono detti **Cold Observables**, sono pigri e non producono dati finché non c'è un osservatore che si iscrive.
  > In programmazione funzionale è un meccanismo tipico dei `lazy iterators`.
* **Push-based** (Data-driven): È la sorgente che fornisce i nuovi dati verso i consumatori, indipendentemente dalla loro richiesta. In `Rx`, sono rappresentati come **Hot Observables**, emettono segnali continuamente al proprio ritmo. I consumatori ricevano aggiornamenti da dopo la loro iscrizione.

### Operatori Reattivi (Map, Filter, FlatMap) e la Composizione

La forza della Programmazione Reattiva risiede nella Componibilità, i data streams, possiedono vari operatori, spesso documentati con i [**Marble Diagrams**](https://rxmarbles.com/):

* **Map**:
  ![map](https://raw.githubusercontent.com/wiki/ReactiveX/RxJava/images/rx-operators/map.v3.png)
* **FlatMap**:
  ![FlatMap](https://raw.githubusercontent.com/wiki/ReactiveX/RxJava/images/rx-operators/flatMap.v3.png)
* **Filter**:
  ![Filter](https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/filter.v3.png)

Nello specifico anche quelle che ho usato nell'`Assignment-02`:

* **scan**:
  ![Scan](https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/scanSeed.v3.png)
* **just**:
  ![Just](https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/just.v3.png)
* **empty**:
  ![Empty](https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/empty.v3.png)
* **using**:
  ![Using](https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/using.v3.png)
* **fromStream**:
  ![fromStream](https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/fromStream.f.png)

### Schedulers in RxJava (Gestione dei thread: `subscribeOn` e `observeOn`)

Per controllare esplicitamente su quali **thread** devono essere eseguiti i flussi, si utilizzano gli **Scheduler**, vi sono due operatori per impostare le politiche di esecuzione:

* `subscribeOn(<Scheduler>)`: Specifica su quale thread deve essere eseguita la sorgente, serve solitamente per spostare computazioni pesanti (o operazioni I/O Bloccanti) su thread di background.
* `observeOn(<Scheduler>)`: Specifica su quale thread devono essere inviati gli aggiornamenti dei dati e eseguirne le task. Serve solitamente per spostare l'esecuzione di operazioni che interagiscono con la GUI sull'**Event Dispatch Thread**.

**Schedulers** predefiniti integrati in `RxJava`:

* **Schedulers.io()**: Ottimizzato per operazioni di I/O bloccanti, come accesso a file o chiamate di rete.
* **Schedulers.computation()**: Ottimizzato per operazioni CPU-bound, come elaborazione di dati o calcoli complessi.
* **Schedulers.single()**: Un singolo thread per esecuzioni sequenziali FIFO.
* **Schedulers.from(SwingUtilities::invokeLater)**: Ottimizzato per operazioni che devono essere eseguite sull'**Event Dispatch Thread** (EDT) di Swing, come aggiornamenti della GUI.

### La Gestione della Backpressure (Buffering, Dropping)

La **Backpressure** è un meccanismo di controllo del flusso. Si verifica in un modello **push**, quando un **Produttore** emette i fati ad una velocità nettamente superiore rispetto a quanto il **Consumatore** riesce a processare. Se non gestita, la backpressure può portare a problemi di memoria, crash o perdita di dati.

Per gestire i casi, esistono diverse strategie:

* **Buffering**: Accumula i dati in una coda (fissa o dinamica) senza perderli, rallentando se la coda si riempie.
* **Batching**: Raggruppa più elementi in batch, accumulando un certo numero di elementi prima di inviarli al consumatore.
* **Dropping**: Consiste nell'ignorare gli elementi in eccesso, come la `onBackpressureLatest()`, che mantiene l'ultimo elemento.

> I `Flowable` si basano su un **modello Push**, ma utilizza la richieta esplicita dei dati (**modello Pull**), unicamente per negoziare il meccanismo di **backpressure**, trasformano il modello in un **ibrido push-pull**.

## Virtual Threads

### Lightweight Threads: Ritorno al paradigma "Thread-per-task"

L'introduzione dei **Virtual Threads** segna un ritorno al paradigma [Thread-Per-Task](/sections/modulo1/modulo1.md#approcci-e-limitazioni-dei-task), in cui ogni task indipendente viene eseguito su un thread dedicato. Tuttavia, a differenza dei tradizionali **Platform Threads**, i Virtual Threads sono estremamente leggeri e scalabili, consentendo di creare milioni di thread senza esaurire le risorse del sistema.

#### Fork-Join

È un'architettura concorrente, ideale per implementare algoritmi di tipo `map-reduce`, in cui un thread può generare nuovi thread (**fork**) e attendere che questi completino la loro task (**join**).

L'approccio di [Master-Worker](/sections/modulo1/modulo1.md#master-worker-pattern) viene applicato in modo ricorsivo, un **worker** diventa a sua volta un **master**, generando nuovi sub-task e mettendosi in attesa del loro termine, l'alrgoritmo si divide in due fasi logiche:

* **Fase di Fork (map)**: Il problema iniziale viene diviso *asincronamente* in task più piccoli e indipendenti, fino a che non diventano banali.
* **Fase di Join (reduce)**: Man mano che i sub-task terminan, i loro risultati parziali vengono raccolti uniti e fatti risalire gerarchicamente fino a ottenere il risultato finale.

> Nella mia implementazione dell'`Assignment-02`, ho utilizzato un approccio molto simile al **Fork-Join**: **fork** per ciascuna sotto-directory viene creato un task `CompletableFuture.supplyAsync`, **join** eseguo delle chiamate bloccanti `join()` per attendere il completamento dei sub-task.

### Differenza tra Platform Threads e Virtual Threads

La differenza principale tra **Platform Threads** e **Virtual Threads** risiede nella gestione delle risorse e nell'integrazione del Sistema Operativo:

* **Platform Threads**: Sono gestiti direttamente dal sistema operativo, ogni thread è una vera e propria entità del sistema, con un proprio stack di memoria e risorse dedicate. Il numero di thread è limitato dalle risorse del sistema, e la creazione di un thread è relativamente costosa in termini di tempo e memoria.
* **Virtual Threads**: Sono gestiti dalla JVM, non sono entità del sistema operativo, ma piuttosto astrazioni leggere che condividono risorse. La JVM gestisce la schedulazione dei Virtual Threads su un pool di thread fisici, consentendo di creare milioni di thread senza esaurire le risorse del sistema. La creazione di un Virtual Thread è molto più veloce e meno costosa rispetto a un Platform Thread.

#### Se abbiamo tante operazioni di I/O, ha senso aumentare ulteriormente il numero di thread? Cambia?

Come abbiamo discusso, nei [Limiti del Multithreading Tradizionale](#i-limiti-del-multithreading-tradizionale), le operazioni I/O sono bloccanti e fanno passare i thread in uno stato "dormiente" per la maggior parte del tempo, percui ha senso aumentarne il numero di thread per gestire più operazioni contemporaneamente. Tuttavia, non gestisci il fatto che stai consumando un sacco di risorse, con thread fisici in attesa.

I **Virtual Threads**, invece, appena un thread è "dormiente", effettua **l'unmount** immediato e libera il **Platform Thread** sottostante. La JVM sta già mantenendo i **Platform Thread** saturi al 100% eseguendo altri **Virtual Threads** pronti, sfruttando al massimo delle possibilità la CPU o la velocità fisica di lettura del disco.

### Architettura interna: Carrier Threads e Mount/Unmount

Il meccanismo di aggancio tra i **Virtual Threads** con i Platform Threads, si basa su due fasi fondamentali:

* **Mounting**: La JVM copia i frame del virtual Thread (dall'Heap) sullo stack del Platform Thread.
* **UnMounting**: Nel momento in cui il Thread incontra un operazione bloccante, la JVM copia lo stack nuovamente nello Heap, e libera il Platform Thread per eseguire altri Virtual Threads pronti.

#### Il problema del "Pinning" (Quando un Virtual Thread non può smontarsi)

Il **Pinning** è una **situazione critica** in cui un **Virtual Thread**, pur incontrando un'operazione bloccante, non può essere smontato (**unmounted**), questo può verificarsi in due scenari specifici:

* Quando il Virtual Thread sta eseguendo porzioni di codice `synchronized`, è bloccato su un monitor, e non può essere smontato fino a quando non rilascia il monitor.
* Quando esegue chiamate a funzioni native.

> La soluzione progettuale per risolvere il **pinning** nei monitor, è utilizzare al posto di `synchronized`, lock espliciti come `ReentrantLock`.

---

## [Assignment-02](./assignment02.md)

### [Report](./report2.pdf)
