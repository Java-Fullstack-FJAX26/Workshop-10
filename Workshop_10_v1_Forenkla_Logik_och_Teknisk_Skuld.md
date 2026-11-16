# Workshop 10 – Förenkla Komplex Logik och Hantera Teknisk Skuld

#### Mål
Efter workshopen ska du kunna:
* Identifiera och platta ut djup nästlad logik (*Arrow Anti-Pattern*) med **Guard Clauses** (*early return*).
* Läsa och skriva filer med **Java NIO.2** (`Files.lines`, `Files.writeString`) och `try-with-resources`.
* Ersätta magiska returvärden med typ-säkra Enums och Records.
* Dokumentera arkitekturbeslut och minskad teknisk skuld med en **ADR (Architecture Decision Record)**.

---

### 📖 Teorisammanfattning

#### 1. Arrow Anti-Pattern & Guard Clauses
Kod som växer horisontellt med nästlade `if`-satser blir svår att följa. Med **Guard Clauses** avhandlas alla fel- och specialfall överst i metoden, så att huvudflödet kan köras platt utan nästling.

#### 2. Vad är en ADR (Architecture Decision Record)?
En ADR är ett kort, standardiserat Markdown-dokument som förklarar varför teamet har fattat ett visst tekniskt beslut (t.ex. att refaktorera en kraschbenägen fil-inläsare eller byta ut `if/else` mot Guard Clauses).

---

### 📦 Boilerplate-kod för studenter (Startpaket)

Kopiera in följande klass med djup nästling och manuell felhantering:

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// LEGACY-KOD: Djup nästling (Arrow Anti-Pattern) & svåröverskådlig logik
public class ComplexLoanEvaluator {

    public String evaluateCredit(int age, double income, int activeLoans, boolean hasDefaults) {
        String result;
        // Arrow Anti-Pattern: 5 nivås nästlade if-satser!
        if (age >= 18) {
            if (!hasDefaults) {
                if (income >= 250000) {
                    if (activeLoans < 3) {
                        result = "APPROVED_LOW_INTEREST";
                    } else {
                        if (activeLoans <= 5) {
                            result = "APPROVED_HIGH_INTEREST";
                        } else {
                            result = "REJECTED_TOO_MANY_LOANS";
                        }
                    }
                } else {
                    result = "REJECTED_LOW_INCOME";
                }
            } else {
                result = "REJECTED_PAYMENT_DEFAULT";
            }
        } else {
            result = "REJECTED_UNDERAGE";
        }
        return result;
    }
}
```

---

### Förmiddag

#### Övning 1 – Säkerhetsnät & Refaktorering med Guard Clauses

##### Uppgift 1A: Skriv JUnit 5-säkerhetsnätet
Skriv `ComplexLoanEvaluatorTest` som verifierar samtliga utfall:
* `REJECTED_UNDERAGE` (age < 18)
* `REJECTED_PAYMENT_DEFAULT` (hasDefaults = true)
* `REJECTED_LOW_INCOME` (income < 250 000)
* `APPROVED_LOW_INTEREST` (aktiva lån < 3)
* `APPROVED_HIGH_INTEREST` (aktiva lån 3–5)
* `REJECTED_TOO_MANY_LOANS` (aktiva lån > 5)

##### Uppgift 1B: Refaktorera till Guard Clauses
Refaktorera `evaluateCredit`-metoden så att den blir helt platt utan nästling. Använd *early return*:
```java
if (age < 18) return "REJECTED_UNDERAGE";
if (hasDefaults) return "REJECTED_PAYMENT_DEFAULT";
// fortsätt för övriga villkor...
```
**Kör JUnit 5-testerna och säkerställ att alla tester är gröna!**

---

### Eftermiddag

#### Övning 2 – NIO.2-Filpersistering & ADR-dokumentation

##### Uppgift 2A: Persistering med Java NIO.2
Skapa en klass `LoanReportService` som läser sökande från en CSV-fil med **Java NIO.2** (`Files.lines`), utvärderar dem via `ComplexLoanEvaluator` och sparar beviljade lån till en ny fil:

```java
public record LoanApplicantRecord(String name, int age, double income, int activeLoans, boolean hasDefaults) {}

public void processReport(Path inputCsv, Path outputReport) throws IOException {
    try (var lines = Files.lines(inputCsv)) {
        List<String> approved = lines.skip(1)
            .map(this::parseApplicant)
            .filter(app -> evaluator.evaluateCredit(app.age(), app.income(), app.activeLoans(), app.hasDefaults()).startsWith("APPROVED"))
            .map(app -> app.name() + " -> BEVILJAD")
            .toList();

        Files.write(outputReport, approved);
    }
}
```

##### Uppgift 2B: Skriv en ADR för er Refaktorering
Skapa en fil `ADR_001_Refactoring_Guard_Clauses_och_NIO2.md` i er projektmapp med följande struktur:

```markdown
# ADR 001: Refaktorering med Guard Clauses och Java NIO.2

## Status
Godkänd / Implementerad

## Kontext
Den gamla kreditvärderingsmodulen och filinläsningen led av djup nästling (Arrow Anti-Pattern) samt Primitive Obsession vid stränghantering. Detta gjorde koden svårläst och kraschbenägen vid saknade filer.

## Beslut
1. Refaktorerade villkorslogiken till platta **Guard Clauses** (early return).
2. Införde **Java Records** (`LoanApplicantRecord`) och **Java NIO.2** (`Files.lines`) med `try-with-resources`.
3. Låste fast beteendet med JUnit 5-enhetstester före och efter refaktoreringen.

## Konsekvenser
* **Positivt:** Den cyklomatiska komplexiteten minskade drastiskt. Filresurser stängs nu garanterat säkert utan minnesläckor.
* **Risker:** Inga identifierade. Testerna visar 100 % grön täckning.
```

---

### Reflektion & Avslutande sammanfattning

| Mönster | Arrow Anti-Pattern | Guard Clauses (Early Return) |
| :--- | :--- | :--- |
| **Läsbarhet** | Svårläst, djup indragning | Hög, linjär läsning ovanifrån och ned |
| **Felhantering** | Felhantering hamnar längst ned i metoden | Felhantering avklaras direkt högst upp |
| **Underhåll** | Hög risk för fel vid nya `if`-satser | Enkelt att lägga till nya valideringsregler |
