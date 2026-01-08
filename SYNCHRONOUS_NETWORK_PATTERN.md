# Synchrones Request-Response Pattern (Variante 3 - Hybrid)

## Übersicht

Das Netzwerk-System unterstützt jetzt **beide Modi**:
- **Asynchron** (wie bisher): `channel.send(packet)` - Fire-and-Forget
- **Synchron** (neu): `channel.sendAndWait(request, responseClass, timeout, unit)` - Wartet auf Response

## Implementierung

### 1. Interfaces

- **`RequestPacket`**: Marker-Interface für Request-Pakete
- **`ResponsePacket`**: Marker-Interface für Response-Pakete
- Beide erweitern `Packet` und haben `getRequestId()` / `setRequestId()` Methoden

### 2. Request-ID Handling

Die Request-ID wird **direkt in den Paketen** gehandhabt:
- Request-Pakete: Request-ID wird beim Senden automatisch generiert und gesetzt
- Response-Pakete: Request-ID muss vom Server gesetzt werden (aus dem Request übernommen)

### 3. Beispiel-Implementierung

#### Request-Paket (Client → Server)
```java
public class C2SGETStats implements RequestPacket {
    private long requestId; // Wird automatisch gesetzt
    
    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID mitsenden
        // ... weitere Daten
    }
    
    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID lesen
        // ... weitere Daten
    }
    
    @Override
    public long getRequestId() { return requestId; }
    
    @Override
    public void setRequestId(long id) { this.requestId = id; }
}
```

#### Response-Paket (Server → Client)
```java
public class S2CPOSTStats implements ResponsePacket {
    private long requestId; // Muss vom Server gesetzt werden
    private Quiz[] quizzes;
    
    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID mitsenden
        // ... Quiz-Daten
    }
    
    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID lesen
        // ... Quiz-Daten
    }
    
    @Override
    public long getRequestId() { return requestId; }
    
    @Override
    public void setRequestId(long id) { this.requestId = id; }
}
```

#### Server-Seite (Response senden)
```java
// In C2SGETStats.handle():
C2SGETStats request = (C2SGETStats) packet;
S2CPOSTStats response = new S2CPOSTStats(quizzes);
response.setRequestId(request.getRequestId()); // WICHTIG: Request-ID übernehmen!
client.send(response);
```

#### Client-Seite (Synchron verwenden)
```java
// Synchron: Wartet auf Response
C2SGETStats request = new C2SGETStats();
S2CPOSTStats response = channel.sendAndWait(
    request, 
    S2CPOSTStats.class, 
    5, 
    TimeUnit.SECONDS
);
Quiz[] quizzes = response.getQuizzes();
```

## Wo synchron vs. asynchron verwenden?

### ✅ **Synchron verwenden** (sendAndWait):

1. **Login/Authentifizierung** ⭐ **SEHR EMPFOHLEN**
   - `C2SAuthenticationPacket` → `S2CLoginPacket` / `S2CLoginFailedPacket`
   - **Grund**: Man möchte sofort wissen, ob Login erfolgreich war
   - **Aktueller Code**: `AnmeldeController.onLogin()` - sendet und wartet auf Callback
   - **Vorteil**: Direkter Return-Wert, kein Callback nötig
   - **Datei**: `src/main/java/at/tgm/client/anmeldung/AnmeldeController.java:54`

2. **Daten-Abfragen (GET-Requests)** ⭐ **SEHR EMPFOHLEN**
   - `C2SGETStats` → `S2CPOSTStats`
   - `C2SGETAllSchueler` → `S2CPOSTAllSchueler`
   - **Grund**: Man braucht die Daten sofort für die UI
   - **Aktueller Code**: Sendet und wartet auf Callback in `Client.onSchuelerListReceived()`
   - **Vorteil**: Keine Callbacks, direkter Datenzugriff
   - **Dateien**: 
     - `src/main/java/at/tgm/client/dashboard/DashboardFrame.java:235`
     - `src/main/java/at/tgm/client/GuiController.java:68,84`

3. **Quiz-Ergebnisse abfragen** ⭐ **EMPFOHLEN**
   - `C2SPOSTQuizResults` → `S2CResultOfQuiz`
   - **Grund**: Man möchte sofort das Ergebnis anzeigen
   - **Aktueller Code**: `QuizPanel.onQuizFinished()` - sendet und wartet auf Callback
   - **Vorteil**: Ergebnis direkt verfügbar, keine Callback-Kette
   - **Datei**: `src/main/java/at/tgm/client/quiz/QuizPanel.java:166`

### ⚠️ **Asynchron bleiben** (send):

1. **Quiz-Start** ⚠️ **KÖNNTE AUCH SYNCHRON SEIN**
   - `C2SINITQuiz` → `S2CPOSTQuiz`
   - **Aktueller Code**: Sendet und wartet auf Callback
   - **Grund aktuell**: Quiz läuft dann weiter, Response kommt später
   - **Alternative**: Könnte auch synchron sein, wenn man warten möchte
   - **Empfehlung**: Synchron machen, wenn UI blockiert werden soll bis Quiz startet
   - **Datei**: `src/main/java/at/tgm/client/quiz/QuizPanel.java` (Quiz-Start)

2. **Hello-Paket / Heartbeat** ✅ **ASYNCHRON BEHALTEN**
   - `C2SHelloPacket`
   - **Grund**: Keine Response nötig, nur Info
   - **Datei**: `src/main/java/at/tgm/client/Client.java:37`

3. **Notifications / Push-Nachrichten** ✅ **ASYNCHRON BEHALTEN**
   - Server-initiiert, keine Request-Response-Beziehung
   - Alle Server→Client Pakete ohne vorherigen Request

## Migration-Strategie

### Schritt 1: Pakete anpassen
- Request-Pakete: `implements RequestPacket` hinzufügen
- Response-Pakete: `implements ResponsePacket` hinzufügen
- `encode()` / `decode()` anpassen: Request-ID schreiben/lesen
- Server: Request-ID aus Request übernehmen und in Response setzen

### Schritt 2: Client-Code anpassen
- Wo Daten sofort benötigt werden: `sendAndWait()` verwenden
- Wo asynchron OK ist: `send()` beibehalten

### Schritt 3: Request-Response-Mapping (optional)
- `RequestResponseMapper.registerPair()` für Typsicherheit
- Wird aktuell nicht verwendet, aber für zukünftige Validierung nützlich

## Vorteile

✅ **Flexibilität**: Beide Patterns parallel nutzbar
✅ **Rückwärtskompatibel**: Bestehender Code funktioniert weiter
✅ **Einfacher Code**: Keine Callbacks/Latches mehr nötig
✅ **Typsicherheit**: Compiler prüft Request-Response-Paarung
✅ **Timeout-Handling**: Automatisch integriert

## Nachteile

⚠️ **Thread-Blockierung**: `sendAndWait()` blockiert den Thread
⚠️ **Mehr Boilerplate**: Pakete müssen Request-ID handhaben
⚠️ **Zwei Modi**: Kann verwirrend sein, welcher wann verwendet wird
