package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Interface for generating patient health data.
 */
public interface PatientDataGenerator {
    /**
     * Generates health data for the specified patient and sends it using the given output strategy.
     *
     * @param patientId The ID of the patient for whom data is generated.
     * @param outputStrategy The strategy used to output the generated data.
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
