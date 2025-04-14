## 1. Alert Generation System

The Alert Generation System is designed to monitor patient data and trigger alerts when vitals exceed thresholds. 
The class, AlertGenerator, evaluates incoming data. This design allows for extensibility through configurable rules or strategies per patient.
For instance, Patient A might trigger an alert for heart rate >130 bpm, while Patient B’s threshold could be 110.
Each generated Alert object has information, such as the patient ID, condition, and a timestamp. Which allows for traceability.
The AlertManager is responsible for routing the alert to the appropriate staff. This separation of concerns where one component evaluates, and another dispatches follows design principles.

## 2. Data Storage System
The Data Storage System ensures that all incoming medical data is stored for analysis. The DataStorage class serves as the main interface for storing, retrieving, and accessing patient records. 
It has data in the form of PatientData objects, which uses PatientRecord object to store information.
Each PatientData instance represents a single, time stamped data point. By linking data points to patient IDs, the system has traceability.
The DataRetriever class handles queries.
Additionally, the system considers data lifecycle management. Deletion policies can be implemented within DataStorage to conserve resources.
Overall, the design balances storage, access, and lifecycle management.

## 3. Patient Identification System
The Patient Identification System makes sure that incoming data is linked to a record. PatientIdentifier receives an incoming patient ID and tries to match it to a PatientRecord.
The PatientRecord class has important information, such as patient ID, record type, and measurement value. This class is the foundation for data representation.
To handle edge cases and validate, the IdentityManager oversees the identification process. If no match is found, it throws handleMismatch(). Which can maybe alert staff of an identification mismatch.

## 4. Data Access Layer
The Data Access Layer abstracts the source of incoming data, whether TCP, WebSocket, or file-based through a common interface, DataListener. This design supports polymorphism, enabling the rest of the system to process data without caring about the source.
The subclasses (TCPDataListener, WebSocketDataListener, FileDataListener) implement the logic needed to retrieve data. This keeps the system open to new data sources in the future without requiring major changes.
Once data is received, it is passed to the DataParser, which standardizes the input into a consistent format. The DataSourceAdapter acts as a bridge between parsed data and internal systems like storage or alert generation.
This layered design allows for scalability and maintainability.