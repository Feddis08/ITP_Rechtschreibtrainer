# Beispiel: Migration zu synchronem Pattern

## Beispiel: C2SGETStats → S2CPOSTStats

### Schritt 1: Request-Paket anpassen (C2SGETStats)

**Vorher:**
```java
public class C2SGETStats implements Packet {
    @Override
    public void encode(DataOutputStream out) throws IOException {
        // Leer
    }
    
    @Override
    public void decode(DataInputStream in) throws IOException {
        // Leer
    }
}
```

**Nachher:**
```java
import at.tgm.network.core.RequestPacket;

public class C2SGETStats implements RequestPacket {
    private long requestId; // Wird automatisch vom NetworkChannel gesetzt
    
    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID mitsenden
    }
    
    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID lesen
    }
    
    @Override
    public long getRequestId() { 
        return requestId; 
    }
    
    @Override
    public void setRequestId(long id) { 
        this.requestId = id; 
    }
}
```

### Schritt 2: Response-Paket anpassen (S2CPOSTStats)

**Vorher:**
```java
public class S2CPOSTStats implements Packet {
    private Quiz[] quizzes;
    
    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeInt(quizzes.length);
        for (Quiz item : quizzes) {
            item.encode(out);
        }
    }
    
    @Override
    public void decode(DataInputStream in) throws IOException {
        int size = in.readInt();
        quizzes = new Quiz[size];
        for (int i = 0; i < size; i++) {
            quizzes[i] = Quiz.decode(in);
        }
    }
}
```

**Nachher:**
```java
import at.tgm.network.core.ResponsePacket;

public class S2CPOSTStats implements ResponsePacket {
    private long requestId; // Muss vom Server gesetzt werden
    private Quiz[] quizzes;
    
    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID ZUERST mitsenden
        out.writeInt(quizzes.length);
        for (Quiz item : quizzes) {
            item.encode(out);
        }
    }
    
    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID ZUERST lesen
        int size = in.readInt();
        quizzes = new Quiz[size];
        for (int i = 0; i < size; i++) {
            quizzes[i] = Quiz.decode(in);
        }
    }
    
    @Override
    public long getRequestId() { 
        return requestId; 
    }
    
    @Override
    public void setRequestId(long id) { 
        this.requestId = id; 
    }
    
    // Getter für Quiz-Daten
    public Quiz[] getQuizzes() {
        return quizzes;
    }
}
```

### Schritt 3: Server-Seite anpassen (SchuelerState.postStats)

**Vorher:**
```java
public void postStats(ServerClient client) {
    Schueler s = (Schueler) client.getNutzer();
    Quiz[] quizzes = s.getQuizzes();
    client.send(new S2CPOSTStats(quizzes));
}
```

**Nachher:**
```java
// In C2SGETStats.handle():
@Override
public void handle(NetworkContext ctx) {
    if (ctx instanceof ServerClient) {
        ServerClient serverClient = (ServerClient) ctx;
        C2SGETStats request = this; // Aktuelles Request-Paket
        
        // Daten vorbereiten
        Schueler s = (Schueler) serverClient.getNutzer();
        Quiz[] quizzes = s.getQuizzes();
        
        // Response erstellen und Request-ID übernehmen
        S2CPOSTStats response = new S2CPOSTStats(quizzes);
        response.setRequestId(request.getRequestId()); // WICHTIG!
        
        try {
            serverClient.send(response);
        } catch (IOException e) {
            logger.error("Fehler beim Senden der Stats", e);
        }
    }
}
```

### Schritt 4: Client-Seite anpassen (synchron verwenden)

**Vorher (asynchron):**
```java
// In DashboardFrame oder ähnlich
C2SGETStats packet = new C2SGETStats();
ClientNetworkController.socketClient.send(packet);
// Response kommt später über S2CPOSTStats.handle() → Client.GUI.showStats()
```

**Nachher (synchron):**
```java
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// In DashboardFrame oder ähnlich
try {
    C2SGETStats request = new C2SGETStats();
    S2CPOSTStats response = ClientNetworkController.socketClient
        .getChannel()
        .sendAndWait(
            request, 
            S2CPOSTStats.class, 
            5, 
            TimeUnit.SECONDS
        );
    
    // Direkt mit den Daten arbeiten
    Quiz[] quizzes = response.getQuizzes();
    updateStatsUI(quizzes);
    
} catch (TimeoutException e) {
    logger.error("Timeout beim Laden der Stats", e);
    showError("Stats konnten nicht geladen werden (Timeout)");
} catch (IOException e) {
    logger.error("Fehler beim Laden der Stats", e);
    showError("Fehler beim Laden der Stats");
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    logger.error("Unterbrochen beim Laden der Stats", e);
}
```

## Wichtige Punkte

1. **Request-ID immer ZUERST** in encode/decode (am Anfang)
2. **Server muss Request-ID übernehmen** aus dem Request-Paket
3. **Client kann synchron oder asynchron** verwenden - beide funktionieren
4. **Backward Compatibility**: Alte asynchrone Verwendung funktioniert weiterhin, wenn Response-Paket auch ohne Request-ID funktioniert (optional)

## Alternative: Request-ID optional machen

Falls man beide Modi parallel unterstützen möchte, kann man die Request-ID optional machen:

```java
// In ResponsePacket
private long requestId = 0; // 0 = keine Request-ID (asynchron)

@Override
public void encode(DataOutputStream out) throws IOException {
    out.writeLong(requestId); // Immer mitschreiben, auch wenn 0
    // ... rest
}

// Im NetworkChannel:
if (requestId != 0) { // Nur wenn Request-ID gesetzt
    CompletableFuture<ResponsePacket> future = pendingRequests.remove(requestId);
    if (future != null) {
        future.complete(responsePacket);
        continue; // Nicht weiter verarbeiten
    }
}
// Sonst normale asynchrone Verarbeitung
packet.handle(context);
```
