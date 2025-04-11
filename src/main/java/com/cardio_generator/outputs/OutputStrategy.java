package com.cardio_generator.outputs;

/**
 * Interface for defining output strategies for patient data.
 * Implementing classes determine how the generated data is output or stored.
 */
public interface OutputStrategy {
    /**
     * Outputs the specified data for a patient.
     *
     * @param patientId The ID of the patient.
     * @param timestamp The timestamp of the data.
     * @param label     A label describing the type of data.
     * @param data      The data value to output.
     */
    void output(int patientId, long timestamp, String label, String data);
}
