package com.lekan.bodyfattracker.ui.home

import kotlin.math.pow

// Enum to define gender for clarity and safety
enum class Gender {
    MALE,
    FEMALE
}

/**
 * Calculates the body fat percentage using the Jackson-Pollock 3-site method.
 *
 * This function uses different formulas for males and females based on the Jackson-Pollock 3-site method.
 *
 * @param skinfold1 The first skinfold measurement in millimeters (mm).
 * @param skinfold2 The second skinfold measurement in millimeters (mm).
 * @param skinfold3 The third skinfold measurement in millimeters (mm).
 * @param age The person's age in years.
 * @param gender The person's gender (Male or Female) as a Gender enum.
 * @return The calculated body fat percentage as a Double.
 */
fun calculateBodyFatPercentageThreeSites(
    skinfold1: Double,
    skinfold2: Double,
    skinfold3: Double,
    age: Int,
    gender: Gender
): Double {
    // Sum of the three skinfold measurements
    val sumOfSkinFolds = skinfold1 + skinfold2 + skinfold3

    // Body density calculation
    val bodyDensity = when (gender) {
        Gender.MALE -> {
            // Jackson-Pollock 3-site formula for men: Chest, Abdomen, Thigh
            // BD = 1.10938 - (0.0008267 * S) + (0.0000016 * S^2) - (0.0002574 * A)
            val sSquared = sumOfSkinFolds.pow(2)
            1.10938 - (0.0008267 * sumOfSkinFolds) + (0.0000016 * sSquared) - (0.0002574 * age)
        }
        Gender.FEMALE -> {
            // Jackson-Pollock 3-site formula for women: Triceps, Suprailiac, Thigh
            // BD = 1.0994921 - (0.0009929 * S) + (0.0000023 * S^2) - (0.0001392 * A)
            val sSquared = sumOfSkinFolds.pow(2)
            1.0994921 - (0.0009929 * sumOfSkinFolds) + (0.0000023 * sSquared) - (0.0001392 * age)
        }
    }

    // Body fat percentage calculation using the Siri equation
    // Body Fat % = (495 / BD) - 450
    return (495.0 / bodyDensity) - 450.0
}

/**
 * Calculates the body fat percentage using the Jackson-Pollock 7-site method.
 * This function uses a single formula for both genders.
 *
 * @param chest The chest skinfold measurement in mm.
 * @param midaxillary The midaxillary skinfold measurement in mm.
 * @param triceps The triceps skinfold measurement in mm.
 * @param subscapular The subscapular skinfold measurement in mm.
 * @param abdomen The abdomen skinfold measurement in mm.
 * @param suprailiac The suprailiac skinfold measurement in mm.
 * @param thigh The thigh skinfold measurement in mm.
 * @param age The person's age in years.
 * @param gender The person's gender (Male or Female) as a Gender enum.
 * @return The calculated body fat percentage as a Double.
 */
fun calculate7SiteBodyFatPercentage(
    chest: Double,
    midaxillary: Double,
    triceps: Double,
    subscapular: Double,
    abdomen: Double,
    suprailiac: Double,
    thigh: Double,
    age: Int,
    gender: Gender
): Double {
    val sumOfSkinFolds = chest + midaxillary + triceps + subscapular + abdomen + suprailiac + thigh

    val bodyDensity = when (gender) {
        Gender.MALE -> {
            // Jackson-Pollock 7-site formula for men
            // BD = 1.112 - (0.00043499 * S) + (0.00000055 * S^2) - (0.00028826 * A)
            val sSquared = sumOfSkinFolds.pow(2)
            1.112 - (0.00043499 * sumOfSkinFolds) + (0.00000055 * sSquared) - (0.00028826 * age)
        }
        Gender.FEMALE -> {
            // Jackson-Pollock 7-site formula for women
            // BD = 1.0970 - (0.00046971 * S) + (0.00000056 * S^2) - (0.00012828 * A)
            val sSquared = sumOfSkinFolds.pow(2)
            1.0970 - (0.00046971 * sumOfSkinFolds) + (0.00000056 * sSquared) - (0.00012828 * age)
        }
    }

    // Body fat percentage calculation using the Siri equation
    // Body Fat % = (495 / BD) - 450
    return (495.0 / bodyDensity) - 450.0
}