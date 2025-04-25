package data_management;

import com.data_management.DataReader;
import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class FileDataReaderTest {
    @Test
    void testReadDataFromFile() throws IOException {
        DataStorage storage = new DataStorage();

        // Assuming test resources are located in "src/test/resources/output_dir"
        DataReader reader = new FileDataReader("src/test/resources");
        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0, System.currentTimeMillis());
        assertFalse(records.isEmpty());
    }


}