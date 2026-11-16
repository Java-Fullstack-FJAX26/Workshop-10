public class LegacyInsuranceEngine {
    public double calculatePremium(int age, boolean isSmoker, boolean hasPreExistingConditions) {
        double premium = 1000.0;
        if (age >= 18) {
            if (age < 65) {
                if (!isSmoker) {
                    if (!hasPreExistingConditions) {
                        premium = 1200.0;
                    } else {
                        premium = 2500.0;
                    }
                } else {
                    premium = 3500.0;
                }
            } else {
                premium = 4500.0;
            }
        } else {
            throw new IllegalArgumentException("Försäkringstagare måste vara myndig");
        }
        return premium;
    }
}