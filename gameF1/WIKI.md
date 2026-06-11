# Documentazione Progetto - Street Racing RPG (Wiki)

Questo documento contiene la relazione tecnica e la documentazione del progetto, da caricare sulla Wiki del repository GitHub.

---

## 1. Funzionalità Implementate

Il gioco implementa una carriera automobilistica clandestina divisa in due fasi principali:

### Garage (RPG Hub)
Il Garage costituisce il fulcro gestionale del giocatore. In questa schermata è possibile:
- **Gestire il Profilo**: Visualizzazione del nome pilota, denaro disponibile, reputazione accumulata e Tier attuale.
- **Monitorare le Statistiche dell'Auto**: Calcolo in tempo reale di *Top Speed* (km/h), *Acceleration* (m/s²), *Grip* (unità) e *Weight* (kg).
- **Acquistare Potenziamenti**: Upgrade incrementali divisi in:
  - *Engine*: Aumenta velocità massima e accelerazione.
  - *Tires*: Aumenta aderenza ed accelerazione.
  - *Nitro*: Aumenta accelerazione e velocità di punta.
  - *Weight Reduction*: Riduce il peso, migliorando notevolmente l'accelerazione.
  - Ogni potenziamento ha un costo che scala in modo esponenziale in base al livello attuale.
- **Salvataggio/Caricamento**: Salvataggio persistente dello stato di gioco in file JSON ed opzione di ripristino carriera dal Menu Principale.

### Minigioco Drag Race (Drag Race Mode)
Schermata interattiva di gara con meccanica di precisione:
- **RPM Gauge**: Indicatore dei giri motore che sale continuamente.
- **Shift Zones**: Zona verde (*Perfect Shift*, 70%-82% RPM) per la massima spinta, zona gialla (*Good Shift*, 55%-90% RPM) per una spinta moderata, e zone esterne (*Bad Shift* o *Redline*) che penalizzano drasticamente l'accelerazione.
- **Cambi di Marcia**: Tasto `SPAZIO` per cambiare marcia (da 1 a 5), resettando l'indicatore RPM.
- **Avversario IA**: Simulato in tempo reale. Le cambiate dell'avversario e le sue prestazioni fisiche dipendono dal Tier e dalle statistiche della sua vettura.
- **Rappresentazione Visiva**: Due barre di avanzamento orizzontali e indicatori numerici di velocità/marcia. Vince chi raggiunge per primo il traguardo dei 402 metri.

### Progression System
- **Tier 1, 2 e 3**: Ciascun livello contiene 3 piloti regolari da sconfiggere.
- **Boss Race**: Dopo 3 vittorie regular, si sblocca il Boss di Tier (es. "Apex", "Ghost", "Zenith") dotato di un'auto nettamente superiore. Sconfiggendo il boss, si ottengono premi elevati e si sblocca il Tier successivo.

---

## 2. Definizione delle Responsabilità ed Architettura (SOLID)

Il progetto segue un'architettura ispirata al pattern **MVC (Model-View-Controller)**, separando nettamente i dati di dominio, la logica dei servizi e le interfacce grafiche.

### Classi e Interfacce Sviluppate

#### Modello (`it.unicam.cs.mpgc.rpg130017.model`)
- **`Upgrade` (Enum)**: Definisce i tipi di potenziamenti, le descrizioni e la formula per il costo scaling.
- **`Car`**: Mantiene i livelli degli upgrade e calcola dinamicamente le statistiche fisiche dell'auto.
- **`Player`**: Rappresenta il pilota, gestendo budget, reputazione e associazione con la propria auto.
- **`Racer`**: Classe base per qualsiasi corridore (giocatore o avversari).
- **`BossRacer` (Estende `Racer`)**: Rappresenta i boss con premi in denaro e sblocchi speciali.
- **`RaceMode` (Interfaccia)**: Contratto generico per la simulazione e la telemetria di una corsa.
- **`DragRaceMode` (Implementa `RaceMode`)**: Logica fisica e meccaniche di cambiata della corsa drag.
- **`GameState`**: DTO (Data Transfer Object) ottimizzato per il salvataggio dei dati.

#### Persistenza (`it.unicam.cs.mpgc.rpg130017.persistence`)
- **`SaveRepository` (Interfaccia)**: Definisce le operazioni di scrittura/lettura dello stato di gioco.
- **`JsonSaveRepository` (Implementa `SaveRepository`)**: Implementa la persistenza tramite serializzazione/deserializzazione JSON utilizzando la libreria Jackson.

#### Servizi (`it.unicam.cs.mpgc.rpg130017.service`)
- **`UpgradeService`**: Logica di acquisto e incremento dei componenti dell'auto.
- **`ProgressionService`**: Gestione del passaggio di Tier, del conteggio vittorie e della generazione degli avversari/boss.
- **`RaceService`**: Istanziazione della gara e calcolo dei premi in denaro/reputazione in base all'esito.
- **`SaveGameService`**: Facciata di comunicazione tra i controller e il SaveRepository.

#### UI (`it.unicam.cs.mpgc.rpg130017.ui` & `launcher`)
- **`MainApp`**: Gestore centrale del ciclo di vita JavaFX e del routing delle scene.
- **`Main`**: Wrapper statico per evitare problemi di classpath con i moduli JavaFX.
- **`MainMenuView`, `GarageView`, `RaceView`, `ResultsView`**: Interfacce grafiche accoppiate al rispettivo controller/layout.

### Rispetto dei Principi SOLID

1. **Single Responsibility Principle (SRP)**: Ogni classe ha un unico scopo ben definito. Ad esempio, `Car` si occupa esclusivamente delle statistiche fisiche dell'auto, delegando l'acquisto di upgrade a `UpgradeService` e il salvataggio a `JsonSaveRepository`.
2. **Open/Closed Principle (OCP)**: L'architettura è aperta all'estensione ma chiusa alle modifiche. L'uso dell'interfaccia `RaceMode` consente di introdurre nuove modalità di corsa (es. `CircuitRaceMode`) senza modificare i servizi di gara esistenti o il flusso principale.
3. **Liskov Substitution Principle (LSP)**: `BossRacer` estende `Racer` senza alterare il comportamento atteso. Qualsiasi metodo che accetti un `Racer` può elaborare un `BossRacer` in maniera del tutto trasparente.
4. **Interface Segregation Principle (ISP)**: Le interfacce come `SaveRepository` e `RaceMode` sono coese e minimali, contenendo solo i metodi strettamente necessari.
5. **Dependency Inversion Principle (DIP)**: I componenti ad alto livello (come `SaveGameService`) dipendono dall'astrazione `SaveRepository`, e non dal modulo concreto `JsonSaveRepository`, facilitando il cambio di persistenza (es. database SQL) tramite Dependency Injection.

---

## 3. Organizzazione dei Dati e Persistenza

La persistenza dello stato di avanzamento della carriera è realizzata tramite file in formato JSON salvati localmente nella cartella `saves/savegame.json`. 

La libreria utilizzata per il mapping dei dati è **Jackson Object Mapper**. La struttura dati persistita è modellata dalla classe `GameState`, che disaccoppia la struttura del salvataggio dalle entità live del gioco (`Player` e `Car`), assicurando l'assenza di riferimenti circolari o campi transitori inutili.

### Esempio di File JSON Generato
```json
{
  "playerName" : "RacerX",
  "money" : 1250,
  "reputation" : 150,
  "tier" : 1,
  "engineLevel" : 2,
  "tiresLevel" : 1,
  "nitroLevel" : 0,
  "weightReductionLevel" : 1,
  "winsInCurrentTier" : 2,
  "bossDefeated" : false
}
```

---

## 4. Meccanismi per Future Estensioni

L'applicazione è stata appositamente progettata per facilitare le seguenti estensioni future senza modificare il nucleo centrale del codice:

- **Nuove Modalità di Gara**: Per aggiungere gare a circuito o sprint, è sufficiente creare una classe (es. `CircuitRaceMode`) che implementi l'interfaccia `RaceMode`. La `RaceView` e il `RaceService` continueranno a funzionare senza alcuna modifica.
- **Persistenza su Database**: Per salvare lo stato di gioco su un database centralizzato (es. PostgreSQL o MySQL tramite JDBC), basta creare una nuova classe `SqlSaveRepository` che implementi l'interfaccia `SaveRepository`. Sarà poi sufficiente passarla al costruttore del `SaveGameService` al posto di `JsonSaveRepository`.
- **Supporto Multi-Dispositivo**: La netta separazione tra i servizi (`service`) e le visualizzazioni (`ui`) garantisce che la logica di gioco rimanga identica nel caso in cui si voglia creare un porting per piattaforme mobile (es. Android con Gluon Mobile) o web (es. con Spring Boot), riscrivendo solo lo strato di presentazione.
- **Multiplayer e Auto Multiple**: La classe `Racer` permette di associare auto diverse a piloti differenti. Questo consente sia di aggiungere un garage con auto acquistabili multipli, sia di agganciare una connessione di rete in cui la classe dell'avversario invia coordinate reali tramite socket.

---

## 5. Dichiarazione di Uso di Strumenti di Intelligenza Artificiale

### Scopo e Utilizzo
Lo sviluppo di questo applicativo ha beneficiato del supporto di **Antigravity (un assistente IA sviluppato da Google DeepMind)**. 
L'IA è stata utilizzata come strumento di co-programming per:
- La definizione dell'architettura e lo schema dei pacchetti nel rispetto dei principi SOLID.
- La stesura del codice sorgente relativo alle formule di calcolo delle statistiche auto.
- Il design e la formattazione dei componenti visuali in stile cyberpunk scuri di JavaFX tramite fogli di stile CSS.
- La configurazione iniziale del sistema di build Gradle, inclusa la gestione delle dipendenze Jackson e del plugin JavaFX compatibile con JVM 23.
- La generazione di test unitari automatizzati.
- La revisione generale del codice per garantire efficienza, pulizia e coerenza delle responsabilità delle classi.
