# Brainstorming: Lernkarten-Verwaltung für Lehrer

## Übersicht der Anforderungen

- **LF10** – Lernkarten erstellen: Die Funktion Lernkarten zum System hinzuzufügen
- **LF20** – Lernkarten bearbeiten: Die Funktion Lernkarten im System zu bearbeiten
- **LF30** – Lernkarten entfernen: Die Funktion Lernkarten im System zu entfernen

---

## 1. Datenpersistenz & Speicherung

### Option A: In-Memory Array (wie bei Nutzern)
**Vorteile:**
- Konsistent mit bestehender Architektur (`Server.nutzers`)
- Einfach zu implementieren
- Keine externe Abhängigkeit

**Nachteile:**
- Daten gehen bei Server-Neustart verloren
- Keine Persistenz

**Implementierung:**
```java
// In Server.java
public static FachbegriffItem[] fachbegriffe = new FachbegriffItem[0];
```

### Option B: Datei-basierte Persistenz (JSON/Serialisierung)
**Vorteile:**
- Daten bleiben nach Server-Neustart erhalten
- Einfache Backup-Möglichkeit

**Nachteile:**
- Zusätzliche Datei-IO-Logik
- Potenzielle Race-Conditions bei gleichzeitigen Schreibzugriffen

**Implementierung:**
- JSON-Datei: `fachbegriffe.json`
- Beim Server-Start laden, bei Änderungen speichern

### Option C: Datenbank (später erweiterbar)
**Vorteile:**
- Professionelle Lösung
- Skalierbar

**Nachteile:**
- Overkill für aktuellen Use-Case
- Zusätzliche Abhängigkeiten

### **Empfehlung:** Option A für MVP, später auf Option B erweitern

---

## 2. Datenstruktur & Identifikation

### Problem: Wie identifizieren wir Lernkarten eindeutig?

**Option 1: Eindeutige ID pro FachbegriffItem**
```java
public class FachbegriffItem extends SendableObject {
    private long id; // Eindeutige ID (z.B. Timestamp oder Auto-Increment)
    private String word;
    private int level;
    private int points;
    private int maxPoints;
    private String phrase;
    // ...
}
```

**Option 2: Word + Level als Composite Key**
- Problem: Was wenn derselbe Begriff in mehreren Levels existieren soll?

**Option 3: Auto-Increment ID im Server**
```java
// In Server.java
private static long nextFachbegriffId = 1;
```

### **Empfehlung:** Option 1 mit Auto-Increment ID im Server

---

## 3. Network Packets

### 3.1 Lernkarten abrufen (GET ALL)

**Request:** `C2SGETAllFachbegriffe`
- Keine Parameter (oder optional: Filter nach Level)
- Request-ID für Response-Matching

**Response:** `S2CPOSTAllFachbegriffe`
- `FachbegriffItem[] fachbegriffe`
- Request-ID

**Verwendung:**
- Lehrer öffnet Lernkarten-Verwaltung → lädt alle Lernkarten
- Wird auch für Quiz-Generierung benötigt (statt hardcoded Items)

---

### 3.2 Lernkarte erstellen (CREATE)

**Request:** `C2SPOSTFachbegriff`
- `FachbegriffItem fachbegriff` (ohne ID, wird vom Server generiert)
- Request-ID

**Response:** `S2CResponseFachbegriffOperation`
- `boolean success`
- `String message`
- Optional: `long fachbegriffId` (neue ID)
- Request-ID

**Validierung:**
- Word darf nicht leer sein
- Level muss >= 1 sein
- Phrase darf nicht leer sein
- MaxPoints muss > 0 sein

---

### 3.3 Lernkarte bearbeiten (UPDATE)

**Request:** `C2SPUTFachbegriff`
- `long fachbegriffId` (zur Identifikation)
- `FachbegriffItem fachbegriff` (aktualisierte Daten)
- Request-ID

**Response:** `S2CResponseFachbegriffOperation`
- `boolean success`
- `String message`
- Request-ID

**Validierung:**
- ID muss existieren
- Gleiche Validierung wie bei CREATE

---

### 3.4 Lernkarte löschen (DELETE)

**Request:** `C2SDELETEFachbegriff`
- `long fachbegriffId`
- Request-ID

**Response:** `S2CResponseFachbegriffOperation`
- `boolean success`
- `String message`
- Request-ID

**Validierung:**
- ID muss existieren
- Optional: Warnung wenn Lernkarte in aktiven Quizzes verwendet wird?

---

## 4. Server-Implementierung

### 4.1 Server.java - Datenstruktur

```java
public class Server {
    // Bestehend
    public static Nutzer[] nutzers;
    
    // Neu
    public static FachbegriffItem[] fachbegriffe = new FachbegriffItem[0];
    private static long nextFachbegriffId = 1;
    
    // Helper-Methoden
    public static FachbegriffItem findFachbegriffById(long id) { ... }
    public static void addFachbegriff(FachbegriffItem item) { ... }
    public static void updateFachbegriff(long id, FachbegriffItem updated) { ... }
    public static void removeFachbegriff(long id) { ... }
}
```

### 4.2 ClientState Interface - Neue Methoden

```java
public interface ClientState {
    // Bestehend...
    
    // Neu für Lernkarten-Verwaltung
    void getAllFachbegriffe(ServerClient client, long requestId) throws IOException;
    void createFachbegriff(ServerClient client, FachbegriffItem item, long requestId) throws IOException;
    void updateFachbegriff(ServerClient client, long id, FachbegriffItem item, long requestId) throws IOException;
    void deleteFachbegriff(ServerClient client, long id, long requestId) throws IOException;
}
```

### 4.3 LehrerState - Implementierung

```java
@Override
public void getAllFachbegriffe(ServerClient client, long requestId) throws IOException {
    // Nur für Lehrer erlaubt
    FachbegriffItem[] items = Server.fachbegriffe != null 
        ? Server.fachbegriffe 
        : new FachbegriffItem[0];
    
    S2CPOSTAllFachbegriffe response = new S2CPOSTAllFachbegriffe(items);
    response.setRequestId(requestId);
    client.send(response);
}

@Override
public void createFachbegriff(ServerClient client, FachbegriffItem item, long requestId) throws IOException {
    // Validierung
    if (item.getWord() == null || item.getWord().trim().isEmpty()) {
        sendError(client, requestId, "Fachbegriff darf nicht leer sein");
        return;
    }
    
    // ID setzen
    item.setId(Server.nextFachbegriffId++);
    
    // Hinzufügen
    Server.addFachbegriff(item);
    
    // Response
    sendSuccess(client, requestId, "Lernkarte erfolgreich erstellt", item.getId());
}

// Ähnlich für update/delete
```

### 4.4 SchuelerState & UnauthenticatedState

```java
// Alle Methoden werfen UnsupportedOperationException
@Override
public void getAllFachbegriffe(...) {
    throw new UnsupportedOperationException("Nur für Lehrer verfügbar");
}
```

---

## 5. Client-UI Implementierung

### 5.1 Neues Panel: LernkartenVerwaltungPanel

**Struktur:**
```
┌─────────────────────────────────────────┐
│ Lernkarten-Verwaltung                   │
├─────────────────────────────────────────┤
│ [+ Neue Lernkarte]                      │
├─────────────────────────────────────────┤
│ ┌─────────────────────────────────────┐ │
│ │ IDE                    Level: 1     │ │
│ │ "Eine integrierte..."  Punkte: 1    │ │
│ │ [Bearbeiten] [Löschen]              │ │
│ └─────────────────────────────────────┘ │
│ ┌─────────────────────────────────────┐ │
│ │ Compiler               Level: 1     │ │
│ │ "Ein Programm..."      Punkte: 2    │ │
│ │ [Bearbeiten] [Löschen]              │ │
│ └─────────────────────────────────────┘ │
│ ...                                      │
└─────────────────────────────────────────┘
```

**Features:**
- Liste aller Lernkarten (mit ScrollPane)
- Filter nach Level (optional)
- Suchfunktion (optional)
- Sortierung (nach Word, Level, Punkte)

### 5.2 Dialog: LernkarteErstellenDialog

**Formular:**
```
┌──────────────────────────────┐
│ Neue Lernkarte erstellen    │
├──────────────────────────────┤
│ Fachbegriff: [___________]   │
│ Level:       [1 ▼]           │
│ Punkte:      [___]           │
│ Beschreibung:                │
│ [________________________]   │
│ [________________________]   │
│                              │
│ [Abbrechen] [Erstellen]      │
└──────────────────────────────┘
```

### 5.3 Dialog: LernkarteBearbeitenDialog

- Gleiche Struktur wie Erstellen-Dialog
- Vorausgefüllt mit bestehenden Werten
- Button: "Speichern" statt "Erstellen"

### 5.4 Integration in DashboardFrame

```java
// In DashboardFrame.java
private LernkartenVerwaltungPanel lernkartenPanel;

// In buildSidebar() - nur für Lehrer sichtbar
if (nutzer instanceof Lehrer) {
    JButton lernkartenBtn = new JButton("Lernkarten");
    lernkartenBtn.addActionListener(e -> showLernkartenVerwaltung());
    sidebar.add(lernkartenBtn);
}

private void showLernkartenVerwaltung() {
    // Lade Lernkarten vom Server
    loadAllFachbegriffe();
}
```

---

## 6. Quiz-Integration

### Problem: Aktuell sind Fachbegriffe hardcoded in `Quiz.getRandomItems()`

**Lösung:**
```java
// In Quiz.java
public void getRandomItems(FachbegriffItem[] items) {
    // Statt hardcoded Items:
    FachbegriffItem[] allItems = Server.fachbegriffe;
    
    // Zufällige Auswahl aus allen verfügbaren Lernkarten
    // (z.B. basierend auf Level, mindestens 10 Items)
    
    // Falls nicht genug Items vorhanden:
    if (allItems.length < items.length) {
        throw new IllegalArgumentException("Nicht genug Lernkarten vorhanden");
    }
    
    // Random Selection Logic...
}
```

**Alternative:** Quiz-Generierung auf Server-Seite
- Schüler fragt Quiz an → Server generiert Quiz aus verfügbaren Lernkarten
- Konsistenter mit bestehender Architektur

---

## 7. Implementierungsreihenfolge

### Phase 1: Datenstruktur & Server-Logik
1. ✅ `FachbegriffItem` um `id` erweitern
2. ✅ `Server.java` um `fachbegriffe` Array erweitern
3. ✅ Helper-Methoden in `Server.java` (add, update, remove, find)
4. ✅ `ClientState` Interface erweitern
5. ✅ `LehrerState` implementiert neue Methoden
6. ✅ `SchuelerState` & `UnauthenticatedState` werfen Exceptions

### Phase 2: Network Packets
7. ✅ `C2SGETAllFachbegriffe` + `S2CPOSTAllFachbegriffe`
8. ✅ `C2SPOSTFachbegriff` + `S2CResponseFachbegriffOperation`
9. ✅ `C2SPUTFachbegriff`
10. ✅ `C2SDELETEFachbegriff`
11. ✅ Packets in `NetworkSystem.init()` registrieren

### Phase 3: Client-UI
12. ✅ `LernkartenVerwaltungPanel` erstellen
13. ✅ `LernkarteErstellenDialog` erstellen
14. ✅ `LernkarteBearbeitenDialog` erstellen
15. ✅ Integration in `DashboardFrame`
16. ✅ Network-Calls im Client implementieren

### Phase 4: Quiz-Integration
17. ✅ `Quiz.getRandomItems()` refactoren
18. ✅ Testen mit echten Lernkarten

### Phase 5: Validierung & Fehlerbehandlung
19. ✅ Validierung auf Server-Seite
20. ✅ Fehlerbehandlung im Client
21. ✅ User-Feedback (Success/Error Messages)

---

## 8. Offene Fragen & Entscheidungen

### 8.1 Duplikate
- **Frage:** Darf derselbe Fachbegriff mehrfach existieren (z.B. in verschiedenen Levels)?
- **Empfehlung:** Ja, aber mit eindeutiger ID

### 8.2 Löschen von verwendeten Lernkarten
- **Frage:** Was passiert wenn eine Lernkarte gelöscht wird, die bereits in einem Quiz verwendet wurde?
- **Option A:** Löschen verbieten (mit Fehlermeldung)
- **Option B:** Löschen erlauben (historische Quizzes bleiben unverändert)
- **Empfehlung:** Option B (einfacher)

### 8.3 Level-Validierung
- **Frage:** Welche Level-Werte sind erlaubt? (1-5? 1-10?)
- **Empfehlung:** 1-5 (konsistent mit bestehenden Items)

### 8.4 Initiale Lernkarten
- **Frage:** Sollen die aktuell hardcoded Items beim Server-Start geladen werden?
- **Empfehlung:** Ja, als Fallback wenn Array leer ist

### 8.5 Persistenz
- **Frage:** Wann soll Persistenz implementiert werden?
- **Empfehlung:** Nach MVP, als separate Phase

---

## 9. Code-Beispiele (Auszüge)

### 9.1 Server.addFachbegriff()

```java
public static void addFachbegriff(FachbegriffItem item) {
    if (item == null) {
        throw new IllegalArgumentException("FachbegriffItem darf nicht null sein");
    }
    
    // Suche nach freiem Platz
    for (int i = 0; i < fachbegriffe.length; i++) {
        if (fachbegriffe[i] == null) {
            fachbegriffe[i] = item;
            logger.info("Fachbegriff '{}' hinzugefügt (ID: {})", item.getWord(), item.getId());
            return;
        }
    }
    
    // Array vergrößern
    FachbegriffItem[] neu = new FachbegriffItem[fachbegriffe.length + 1];
    System.arraycopy(fachbegriffe, 0, neu, 0, fachbegriffe.length);
    neu[fachbegriffe.length] = item;
    fachbegriffe = neu;
    
    logger.info("Fachbegriff '{}' hinzugefügt (ID: {}, Array vergrößert)", 
                item.getWord(), item.getId());
}
```

### 9.2 C2SPOSTFachbegriff.handle()

```java
@Override
public void handle(NetworkContext ctx) {
    SocketClient sc = ((SocketClient) ctx);
    String username = sc.getNutzer() != null ? sc.getNutzer().getUsername() : "unknown";
    
    if (sc instanceof ServerClient && sc.getNutzer() instanceof Lehrer) {
        logger.info("Fachbegriff-Erstellung von Lehrer: {} (Request-ID: {})", username, requestId);
        try {
            ((ServerClient) sc).createFachbegriff(fachbegriff, requestId);
        } catch (IOException e) {
            logger.error("Fehler beim Erstellen des Fachbegriffs für Lehrer: {}", username, e);
            throw new RuntimeException(e);
        }
    } else {
        logger.warn("Fachbegriff-Erstellung von nicht-Lehrer: {}", username);
        // Optional: Error-Response senden
    }
}
```

---

## 10. Testing-Strategie

### Unit Tests
- Server-Methoden (add, update, remove, find)
- Validierung
- Edge Cases (null, leere Arrays, etc.)

### Integration Tests
- Packet-Roundtrip (Request → Response)
- Lehrer vs. Schüler Permissions
- Fehlerbehandlung

### Manual Testing
- UI-Flows komplett durchtesten
- Verschiedene Szenarien (leere Liste, viele Items, etc.)

---

## Zusammenfassung

**Kernkomponenten:**
1. Datenstruktur: `Server.fachbegriffe[]` mit Auto-Increment IDs
2. 4 neue Packets (GET ALL, CREATE, UPDATE, DELETE)
3. 4 neue Methoden im `ClientState` Interface
4. UI: `LernkartenVerwaltungPanel` + 2 Dialoge
5. Quiz-Integration: Refactoring von `Quiz.getRandomItems()`

**Geschätzter Aufwand:**
- Server-Logik: ~2-3 Stunden
- Packets: ~2-3 Stunden
- Client-UI: ~4-5 Stunden
- Quiz-Integration: ~1 Stunde
- Testing: ~2 Stunden
- **Gesamt: ~12-14 Stunden**

**Risiken:**
- Quiz-Generierung muss angepasst werden
- Keine Persistenz (Daten gehen bei Neustart verloren)
- Potenzielle Race-Conditions bei gleichzeitigen Zugriffen (später mit Locks lösen)
