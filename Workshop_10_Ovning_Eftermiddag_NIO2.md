# Workshop 10: Eftermiddagsövning – Java NIO.2 Filinläsning, Filtrering & Filskrivning

Denna övning bygger vidare på förmiddagens fil-demo. Ni ska utveckla en klass som läser in en CSV-fil med utrustningsdata, parsar raderna till Java Records, filtrerar ut ett urval med Streams API och skriver det nya urvalet till en ny CSV-fil på disk med Java NIO.2.

---

##  Övningens Mål
1. Läsa in och parsa en CSV-fil med `java.nio.file.Path` och `Files.lines()` i ett `try-with-resources`-block.
2. Använda **Java Records** som immutabla databärare.
3. Filtrera data i minnet med Java Streams API.
4. Skriva det filtrerade urvalet till en ny CSV-fil med `Files.newBufferedWriter()`.
5. Skriva ett integrationstest i JUnit 5 med `@TempDir` som verifierar att målfilen skapas och innehåller rätt data.

---

##  1. Testfil för indata (`equipment_data.csv`)

Placera följande innehåll i en fil kallad `equipment_data.csv` i projektets rot:

```csv
id,name,category,dailyRate,available
E101,Slagborrmaskin,Verktyg,150.0,true
E102,Skruvdragare 18V,Verktyg,95.0,true
E103,Laddhybridbil,Fordon,450.0,false
E104,Projektor 4K,Elektronik,200.0,true
E105,Röjsåg Bensin,Trädgård,180.0,false
E106,Cirkelsåg,Verktyg,130.0,true
E107,Ljudanläggning,Elektronik,350.0,false
```

---

##  2. Steg-för-steg Instruktioner

### Steg 1: Skapa DTO (Java Record)
Skapa `EquipmentItem.java` som representerar en CSV-rad. Lägg till hjälpmotoden `toCsvLine()` som formaterar objektet tillbaka till CSV-text:

```java
import java.util.Objects;

public record EquipmentItem(String id, String name, String category, double dailyRate, boolean available) {
    public EquipmentItem {
        Objects.requireNonNull(id, "ID får inte vara null");
        Objects.requireNonNull(name, "Namn får inte vara null");
    }

    public String toCsvLine() {
        return String.format("%s,%s,%s,%.1f,%b", id, name, category, dailyRate, available);
    }
}
```

---

### Steg 2: Skapa `EquipmentFileProcessor.java`

Klassen ska innehålla tre huvudmetoder:
1. **`readFromCsv(Path inputPath)`**: Läser in och parsar rader med `Files.lines()`.
2. **`filterAvailableByCategory(List<EquipmentItem> items, String category)`**: Filtrerar ut artiklar där `available == true` och kategorin matchar.
3. **`writeToCsv(Path outputPath, List<EquipmentItem> items)`**: Skriver ut en CSV-rubrikrad och alla filtrerade rader till `outputPath` med `Files.newBufferedWriter()`.

```java
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class EquipmentFileProcessor {

    public List<EquipmentItem> readFromCsv(Path inputPath) throws IOException {
        if (!Files.exists(inputPath)) {
            throw new IOException("Källfilen existerar inte: " + inputPath);
        }

        try (Stream<String> lines = Files.lines(inputPath)) {
            return lines
                .skip(1) // Hoppa över CSV-rubrik
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .map(this::parseLine)
                .toList();
        }
    }

    public List<EquipmentItem> filterAvailableByCategory(List<EquipmentItem> items, String category) {
        return items.stream()
            .filter(EquipmentItem::available)
            .filter(item -> item.category().equalsIgnoreCase(category))
            .toList();
    }

    public void writeToCsv(Path outputPath, List<EquipmentItem> items) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("id,name,category,dailyRate,available");
            writer.newLine();

            for (EquipmentItem item : items) {
                writer.write(item.toCsvLine());
                writer.newLine();
            }
        }
    }

    private EquipmentItem parseLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 5) {
            throw new IllegalArgumentException("Ogiltigt CSV-format: " + line);
        }
        return new EquipmentItem(
            parts[0].trim(),
            parts[1].trim(),
            parts[2].trim(),
            Double.parseDouble(parts[3].trim()),
            Boolean.parseBoolean(parts[4].trim())
        );
    }
}
```

---

### Steg 3: Verifiera med JUnit 5 och `@TempDir`

Skriv ett test som verifierar hela pipelinen i en isolerad, tillfällig katalog:

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EquipmentFileProcessorTest {

    @Test
    void testReadFilterAndWriteCsv(@TempDir Path tempDir) throws IOException {
        Path inputPath = tempDir.resolve("input.csv");
        Path outputPath = tempDir.resolve("filtered_output.csv");

        List<String> inputLines = List.of(
            "id,name,category,dailyRate,available",
            "E101,Slagborrmaskin,Verktyg,150.0,true",
            "E102,Skruvdragare 18V,Verktyg,95.0,true",
            "E103,Laddhybridbil,Fordon,450.0,false",
            "E104,Projektor 4K,Elektronik,200.0,true",
            "E105,Röjsåg Bensin,Trädgård,180.0,false",
            "E106,Cirkelsåg,Verktyg,130.0,true"
        );
        Files.write(inputPath, inputLines);

        EquipmentFileProcessor processor = new EquipmentFileProcessor();

        List<EquipmentItem> allItems = processor.readFromCsv(inputPath);
        List<EquipmentItem> filteredTools = processor.filterAvailableByCategory(allItems, "Verktyg");
        processor.writeToCsv(outputPath, filteredTools);

        assertTrue(Files.exists(outputPath), "Målfilen ska ha skapats");

        List<String> resultLines = Files.readAllLines(outputPath);

        assertEquals(4, resultLines.size(), "Ska innehålla rubrik + 3 tillgängliga verktyg");
        assertEquals("id,name,category,dailyRate,available", resultLines.get(0));
        assertTrue(resultLines.get(1).contains("Slagborrmaskin"));
        assertTrue(resultLines.get(2).contains("Skruvdragare 18V"));
        assertTrue(resultLines.get(3).contains("Cirkelsåg"));
    }
}
```

---

##  Viktiga Lärdomar

* **`Files.lines()`**: Läser in och strömmar rader minneseffektivt.
* **`Files.newBufferedWriter()`**: Ger kontrollerad och kraschfri filskrivning.
* **`try-with-resources`**: Ser till att alla filhandtag stängs automatiskt.
* **`@TempDir`**: Möjliggör isolerade, rena och upprensningsfria enhetstester av fil-I/O i JUnit 5.
