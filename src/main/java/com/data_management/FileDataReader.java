package com.data_management;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileDataReader implements DataReader {

    private final String outputDir;

    public FileDataReader(String outputDir) {
        this.outputDir = outputDir;
    }

    @Override
    public void readData(DataStorage storage) throws IOException {
        File dir = new File(outputDir);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException("Invalid output directory: " + outputDir);
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt") || name.endsWith(".csv"));
        if (files == null) return;

        for (File file : files) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // Expected format: patientId,measurementValue,measurementType,timestamp
                    String[] parts = line.split(",");
                    if (parts.length != 4) continue;

                    int patientId = Integer.parseInt(parts[0].trim());
                    double measurementValue = Double.parseDouble(parts[1].trim());
                    String measurementType = parts[2].trim();
                    long timestamp = Long.parseLong(parts[3].trim());

                    storage.addPatientData(patientId, measurementValue, measurementType, timestamp);
                }
            }
        }
    }
}
