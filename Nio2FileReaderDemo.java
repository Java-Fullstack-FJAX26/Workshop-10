import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
public class Nio2FileReaderDemo {

    public static void main(String[] args) {
        Path filePath = Path.of("equipment_data.csv");
        System.out.println("=== Läser in utrustning från: " + filePath.toAbsolutePath() + "=====");

        try {
            List<EquipmentItem> items = loadEquipmentfromCSV(filePath);
            items.forEach(IO::println);
        } catch (IOException e) {
            System.err.println("Systemfel vid inläsning: " + e.getMessage() );
        }
        
    }
    // Tar emot path till fil
    // Läser fil rad för rad till EquipmentItem
    // Adderar till lista som returneras
    public static List<EquipmentItem> loadEquipmentfromCSV(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("Filen finns inte: " + path.getFileName());
        }
        try (Stream<String> lines = Files.lines(path)) {
            return lines
                .skip(1) //Hoppa över rubrikraden
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .map(Nio2FileReaderDemo::parseCsvLine)
                .toList();
        }

    }

    private static EquipmentItem parseCsvLine(String line) {
        String[] parts = line.split(",");
    
        if (parts.length < 5) {
            throw new IllegalArgumentException("Felaktigt format på CSV-rad: " + line);
        }

        String id = parts[0].trim();
        String name = parts[1].trim();
        String category = parts[2].trim();
        double dailyRate = Double.parseDouble(parts[3].trim());
        boolean available = Boolean.parseBoolean(parts[4].trim());

        return new EquipmentItem(id, name, category, dailyRate, available);
    }
    
}
