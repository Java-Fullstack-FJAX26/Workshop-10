public class RefactoredInsuranceEngine {
    public double calculatePremium (int age, boolean isSmoker, boolean hasPreExistingConditions) {
        // Guard: Ingen barnförsäkring
        if (age < 18) {
            throw new IllegalArgumentException("Försäkringstagaren måste vara myndig!");
        }
        //Guard: Seniortaxa
        if (age >=65) return 4500.0;

        //Guard: Rökare
        if (isSmoker) return 3500.0;
        
        //Guard: har tidigare sjukdom
        if (hasPreExistingConditions) return 2500.0;

        // ej rökande, ej tidigare sjukdom 18-64 år.
        return 1200.0;
    }
}
