package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated alert data for patients, representing triggered or resolved alerts.
 * Alerts are generated based on a Poisson distribution to simulate random occurrences.
 */
public class AlertGenerator implements PatientDataGenerator {

    public static final Random randomGenerator = new Random();
    // edit: AlertStates -> alertStates for lowerCamelCase
    private boolean[] alertStates; // false = resolved, true = pressed

    /**
     * Constructs a new {@code AlertGenerator} for the specified number of patients.
     *
     * @param patientCount The number of patients to simulate.
     */
    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }

    /**
     * Generates an alert event for the specified patient based on current alert state.
     * May either trigger or resolve an alert and sends the result via the output strategy.
     *
     * @param patientId The ID of the patient.
     * @param outputStrategy The strategy used to output the generated alert data.
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                if (randomGenerator.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                // edit: Lambda -> LAMBDA constants are all caps
                double LAMBDA = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-LAMBDA); // Probability of at least one alert in the period
                boolean alertTriggered = randomGenerator.nextDouble() < p;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
