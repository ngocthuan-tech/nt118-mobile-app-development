package com.aicoach.ui.profile;

/** Numeric presentation rules; incomplete profiles must not display zero/NaN as BMI. */
public final class ProfileNumbers {
    private ProfileNumbers() {}
    public enum BmiCategory { UNKNOWN, UNDERWEIGHT, NORMAL, OVERWEIGHT, OBESITY }

    /** CDC adult (20+) reference bands; these do not diagnose health. */
    public static BmiCategory category(Double bmi) {
        if (bmi == null || !Double.isFinite(bmi) || bmi <= 0) return BmiCategory.UNKNOWN;
        if (bmi < 18.5) return BmiCategory.UNDERWEIGHT;
        if (bmi < 25) return BmiCategory.NORMAL;
        if (bmi < 30) return BmiCategory.OVERWEIGHT;
        return BmiCategory.OBESITY;
    }

    public static boolean validHeight(Double height) {
        return height != null && Double.isFinite(height) && height >= 100 && height <= 250;
    }

    public static boolean validWeight(Double weight) {
        return weight != null && Double.isFinite(weight) && weight >= 30 && weight <= 250;
    }

    public static Double bmi(Double heightCm, Double weightKg) {
        if (!validHeight(heightCm) || !validWeight(weightKg)) return null;
        double metres = heightCm / 100.0;
        return weightKg / (metres * metres);
    }
}
