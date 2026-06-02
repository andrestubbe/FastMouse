# FastMouse Philosophie

## 1. Native-First & Zero-Compromise

Der JVM-Standard versucht, die unterliegende Plattform unter einer dicken Abstraktionsschicht (AWT/Swing) zu verstecken. Für Office-Anwendungen mag das genügen, doch bei latenzkritischen, datenintensiven Eingaben scheitert dieser Ansatz kläglich:
-   **Maus-Ballistik (Zeigerbeschleunigung):** Standard-Java-Listener erhalten manipulierte Koordinaten, die durch die Windows-Mausbeschleunigung verzerrt sind. Dies zerstört jede Vorhersagbarkeit für Algorithmen.
-   **Event-Verlust bei High-Polling:** Moderne Gaming-Mäuse tasten mit 1000 Hz, 4000 Hz oder gar 8000 Hz ab. AWT-Event-Queues verwerfen bei hoher Last Events (Frame-Dropping).
-   **Multi-Device Blindheit:** Standard-APIs können zwei physikalisch angeschlossene Mäuse nicht voneinander unterscheiden.

**FastMouse** bricht mit dieser Abstraktion. Wir gehen direkt auf Hardware-Ebene: **Raw Input (`WM_INPUT`)** via handgeschriebenem JNI C++.

---

## 2. Die Säulen von FastMouse

### I. Umgehung der Betriebssystem-Ballistik
FastMouse liest die rohen Sensordaten direkt vom Maussensor (`lLastX`, `lLastY`). Kein Filter, keine Beschleunigungskurven, keine Glättung. Jeder gezählte Sensorpunkt (Count) landet 1:1 in der Java-Schleife. Dies ist das Fundament für pixelgenaues Zeichnen, 3D-Kamerasteuerung und präzise Telemetrie.

### II. Dedizierte native Event-Pipes
Durch die Registrierung eines asynchronen Message-Only-Windows auf einem nativen C++ Background-Thread blockieren ankommende Eingaben niemals den Java-UI-Thread. JNI-Grenzübertritte werden extrem minimiert, indem die Events thread-sicher und direkt angebunden an die Event-Loop fließen.

### III. Identifikation physischer Hardware
Durch die Bereitstellung stabiler Device-Handles können Entwickler tracken, *welche* Maus das Signal gesendet hat. Perfekt für Multi-Monitor-Setups, Co-Op-Spiele an einem PC oder die Kalibrierung spezifischer Sensor-Hardware.
