package com.aicoach.ui.profile;

import org.junit.Test;
import static org.junit.Assert.*;

public class ProfileNumbersTest {
    @Test public void adultReferenceCategoriesUseUnroundedBoundaryValues() {
        assertEquals(ProfileNumbers.BmiCategory.UNKNOWN, ProfileNumbers.category(null));
        assertEquals(ProfileNumbers.BmiCategory.UNKNOWN, ProfileNumbers.category(Double.NaN));
        assertEquals(ProfileNumbers.BmiCategory.UNDERWEIGHT, ProfileNumbers.category(18.49));
        assertEquals(ProfileNumbers.BmiCategory.NORMAL, ProfileNumbers.category(18.5));
        assertEquals(ProfileNumbers.BmiCategory.NORMAL, ProfileNumbers.category(24.99));
        assertEquals(ProfileNumbers.BmiCategory.OVERWEIGHT, ProfileNumbers.category(25.0));
        assertEquals(ProfileNumbers.BmiCategory.OVERWEIGHT, ProfileNumbers.category(29.99));
        assertEquals(ProfileNumbers.BmiCategory.OBESITY, ProfileNumbers.category(30.0));
    }
    @Test public void convertsCentimetresBeforeCalculatingBmi() {
        assertEquals(22.491349, ProfileNumbers.bmi(170.0, 65.0), 0.00001);
        assertEquals(25.0, ProfileNumbers.bmi(200.0, 100.0), 0.00001);
    }

    @Test public void missingOrInvalidMeasurementsDoNotProduceBmi() {
        assertNull(ProfileNumbers.bmi(null, 65.0));
        assertNull(ProfileNumbers.bmi(170.0, null));
        for (double height : new double[]{0, -1, 99, 251, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertNull(ProfileNumbers.bmi(height, 65.0));
        }
        for (double weight : new double[]{0, -1, 29, 251, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertNull(ProfileNumbers.bmi(170.0, weight));
        }
    }

    @Test public void acceptsServerMeasurementBoundaries() {
        assertNotNull(ProfileNumbers.bmi(100.0, 30.0));
        assertNotNull(ProfileNumbers.bmi(250.0, 250.0));
    }
}
