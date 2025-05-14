package com.data_management;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Reads patient data from text or CSV files in a specified directory and populates the {@link DataStorage}.
 * <p>
 * This class implements the {@link DataReader} interface.
 * <p>
 * Each file should contain lines in the format:
 * <pre>
 * Patient ID: 37, Timestamp: 1744113766180, Label: Cholesterol, Data: 174.57006353219262
 * </pre>
 */
public class FileDataReader implements DataReader {

    private final String outputDir;

    /**
     * Constructs a new FileDataReader for the specified directory.
     *
     * @param outputDir the path to the directory containing the .txt or .csv data files
     */
    public FileDataReader(String outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * Reads data from all .txt or .csv files in the specified directory and adds the records to the given {@link DataStorage}.
     *
     * @param storage the data storage object to populate with patient records
     * @throws IOException if the output directory is invalid or cannot be accessed
     */
    @Override
    public void readData(DataStorage storage) throws IOException {
        File dir = new File(outputDir);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException("Invalid output directory: " + outputDir);
        }
        // Grabs all the files in the directory
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt") || name.endsWith(".csv"));
        if (files == null) return;
        // Parses each one
        for (File file : files) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // Expected format:
                    // Patient ID: 37, Timestamp: 1744113766180, Label: Cholesterol, Data: 174.57006353219262
                    String[] parts = line.split(",\\s*");
                    if (parts.length != 4) continue;
                    // Parses values to respective variables
                    try {
                        int patientId = Integer.parseInt(parts[0].split(":")[1].trim());
                        long timestamp = Long.parseLong(parts[1].split(":")[1].trim());
                        String measurementType = parts[2].split(":")[1].trim();
                        String rawValue = parts[3].split(":")[1].trim().replace("%", "");
                        double measurementValue = Double.parseDouble(rawValue);

                        storage.addPatientData(patientId, measurementValue, measurementType, timestamp);
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        continue;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
