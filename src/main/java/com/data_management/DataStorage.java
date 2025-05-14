package com.data_management;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alerts.AlertGenerator;
import com.cardio_generator.generators.*;
import com.cardio_generator.outputs.WebSocketOutputStrategy;

/**
 * Manages storage and retrieval of patient data within a healthcare monitoring
 * system.
 * This class serves as a repository for all patient records, organized by
 * patient IDs.
 */
public class DataStorage {
    private static DataStorage instance;
    private final Map<Integer, Patient> patientMap;

    /**
     * Constructs a new instance of DataStorage, initializing the underlying storage
     * structure.
     */
    public DataStorage() {
        this.patientMap = new ConcurrentHashMap<>();
    }
    /**
     * Gets the instance of DataStorage. Makes one if instance is null.
     */
    public static synchronized DataStorage getInstance() {
        if (instance == null) {
            instance = new DataStorage();
        }
        return instance;
    }

    /**
     * Adds or updates patient data in the storage.
     * If the patient does not exist, a new Patient object is created and added to
     * the storage.
     * Otherwise, the new data is added to the existing patient's records.
     *
     * @param patientId        the unique identifier of the patient
     * @param measurementValue the value of the health metric being recorded
     * @param recordType       the type of record, e.g., "HeartRate",
     *                         "BloodPressure"
     * @param timestamp        the time at which the measurement was taken, in
     *                         milliseconds since the Unix epoch
     */
    public void addPatientData(int patientId, double measurementValue, String recordType, long timestamp) {
        Patient patient = patientMap.computeIfAbsent(patientId, Patient::new);
        patient.addRecord(measurementValue, recordType, timestamp);
    }

    /**
     * Retrieves a list of PatientRecord objects for a specific patient, filtered by
     * a time range.
     *
     * @param patientId the unique identifier of the patient whose records are to be
     *                  retrieved
     * @param startTime the start of the time range, in milliseconds since the Unix
     *                  epoch
     * @param endTime   the end of the time range, in milliseconds since the Unix
     *                  epoch
     * @return a list of PatientRecord objects that fall within the specified time
     *         range
     */
    public List<PatientRecord> getRecords(int patientId, long startTime, long endTime) {
        Patient patient = patientMap.get(patientId);
        if (patient != null) {
            return patient.getRecords();
        }
        return new ArrayList<>();
    }

    /**
     * Retrieves a collection of all patients stored in the data storage.
     *
     * @return a list of all patients
     */
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patientMap.values());
    }

    /**
     * Clears all patient data from the storage.
     * Useful for resetting state in tests or restarting data collection.
     */
    public void clear() {
        patientMap.clear();
    }


    /**
     * The main method for the DataStorage class.
     * Initializes the system, reads data into storage, and continuously monitors
     * and evaluates patient data using a websocket server pipeline.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) throws IOException {
        // DataReader is not defined in this scope, should be initialized appropriately.
        // DataStorage storage = new DataStorage();

        // Assuming the reader has been properly initialized and can read data into the
        // storage
        // reader.readData(storage);

        // Example of using DataStorage to retrieve and print records for a patient
//        List<PatientRecord> records = storage.getRecords(1, 1700000000000L, 1800000000000L);
//        for (PatientRecord record : records) {
//            System.out.println("Record for Patient ID: " + record.getPatientId() +
//                    ", Type: " + record.getRecordType() +
//                    ", Data: " + record.getMeasurementValue() +
//                    ", Timestamp: " + record.getTimestamp());
//        }

        // Initialize the AlertGenerator
        AlertGenerator alertGenerator = new AlertGenerator();

        // Start WebSocket Server
        WebSocketOutputStrategy strategy = new WebSocketOutputStrategy(8887);
        DataStorage dataStorage = DataStorage.getInstance();

        // Connect to the WebSocket as a client to receive data in real-time
        DataReader reader = new WebSocketDataReader("ws://localhost:8887");
        reader.readData(dataStorage);  // Starts the client inside


        int patientCount = 5; // Can be any value

        // Prepare data generators
        PatientDataGenerator ecgDataGenerator = new ECGDataGenerator(patientCount);
        PatientDataGenerator bloodSaturationDataGenerator  = new BloodSaturationDataGenerator(patientCount);
        PatientDataGenerator bloodPressureDataGenerator = new BloodPressureDataGenerator(patientCount);
        PatientDataGenerator bloodLevelsDataGenerator = new BloodLevelsDataGenerator(patientCount);

        // Schedule real-time data generation at a fixed interval
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            // generates data for each patient
            for (int i=0;i<patientCount;i++) {
                ecgDataGenerator.generate(i, strategy);
                bloodSaturationDataGenerator.generate(i, strategy);
                bloodPressureDataGenerator.generate(i, strategy);
                bloodLevelsDataGenerator.generate(i, strategy);
            }

            // evaluate all patients' data to check for conditions that may trigger alerts
            for (Patient patient : dataStorage.getAllPatients()) {
                alertGenerator.evaluateData(patient);
            }

        }, 0, 5, TimeUnit.SECONDS);
    }
}
