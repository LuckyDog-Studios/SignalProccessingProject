package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
// edit: fileOutputStrategy -> FileOutputStrategy   :   class names should be formatted in UpperCamelCase
/**
 * An implementation of {@link OutputStrategy} that writes patient data to text files.
 * Each data label is written to a separate file within the specified base directory.
 */
public class FileOutputStrategy implements OutputStrategy {
    // edit: BaseDirectory -> baseDirectory  :  variable names should be formatted in lowerCamelCase
    private String baseDirectory;
    // edit: file_map -> fileMap  :  variable names should be formatted in lowerCamelCase
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    /**
     * Constructs a new FileOutputStrategy with the given base directory.
     *
     * @param baseDirectory The directory where output files will be created.
     */
    public FileOutputStrategy(String baseDirectory) {

        this.baseDirectory = baseDirectory;
    }

    /**
     * Outputs the data for a specific patient by writing it to a file with a data label.
     * Creates the base directory and file if they do not exist.
     *
     * @param patientId The ID of the patient.
     * @param timestamp The timestamp of the data.
     * @param label     The label describing the data type.
     * @param data      The data value to be written.
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Set the FilePath variable
        // edit: FilePath -> filePath  :  variable names should be formatted in lowerCamelCase
        String filePath = //Line wrap due to exceeding 100 characters
                fileMap.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}