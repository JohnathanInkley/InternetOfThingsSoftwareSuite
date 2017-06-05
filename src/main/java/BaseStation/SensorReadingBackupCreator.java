package BaseStation;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class SensorReadingBackupCreator {
    private static String BACKUP_ARRAY_LIST_EXTENSION = "ListOfBackupFiles.backup";
    private final String fileContainingListOfBackups;

    private ArrayList<String> backupFilesToBeReadLater;
    private String currentFile;
    private String prefixForBackUps;

    public SensorReadingBackupCreator(String prefix) {
        prefixForBackUps = prefix;
        fileContainingListOfBackups = prefixForBackUps + BACKUP_ARRAY_LIST_EXTENSION;
        backupFilesToBeReadLater = new ArrayList<>();
        if (checkIfBackupsWithPrefixExist()) {
            readInListOfExistingBackups();
        }
    }

    private boolean checkIfBackupsWithPrefixExist() {
        File potentialListOfBackups = new File(prefixForBackUps + BACKUP_ARRAY_LIST_EXTENSION);
        return potentialListOfBackups.exists() && !potentialListOfBackups.isDirectory();
    }

    private void readInListOfExistingBackups() {
        try (Stream<String> backups = Files.lines(Paths.get(fileContainingListOfBackups))) {
            backups.forEach((backupFileName) -> backupFilesToBeReadLater.add(backupFileName));
            deleteFile(fileContainingListOfBackups);
        } catch (Exception e) {
            throw new RuntimeException("Check list of backups in " + fileContainingListOfBackups
                    + "; backup files cannot be read");
        }
    }

    public synchronized void write(SensorReadingHandler handler) {
        try {
            writeHandlerToFileWithCurrentTimestamp(handler);
            updateFileContainingListOfExistingBackupFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeHandlerToFileWithCurrentTimestamp(SensorReadingHandler handler) throws IOException {
        String fileName = prefixForBackUps + System.currentTimeMillis() + ".backup";
        FileOutputStream outputStream = new FileOutputStream(fileName);
        ObjectOutput output = new ObjectOutputStream(outputStream);
        output.writeObject(handler);
        output.close();
        backupFilesToBeReadLater.add(fileName);
    }

    private void updateFileContainingListOfExistingBackupFiles() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileContainingListOfBackups));
            for (String backupFileName : backupFilesToBeReadLater) {
                writer.write(backupFileName);
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException("Could not write backups. Check " + fileContainingListOfBackups
            + "is not in use and try again");
        }
    }

    public synchronized SensorReadingHandler read() {
        updateCurrentFileToRead();
        SensorReadingHandler handler = readFileOrReturnEmptyHandler();
        deleteFile(currentFile);
        updateFileContainingListOfExistingBackupFiles();
        return handler;
    }


    private void updateCurrentFileToRead() {
        currentFile = backupFilesToBeReadLater.get(backupFilesToBeReadLater.size() - 1);
        backupFilesToBeReadLater.remove(backupFilesToBeReadLater.size() - 1);
    }

    private SensorReadingHandler readFileOrReturnEmptyHandler() {
        try {
            return readFile();
        } catch (Exception e) {
            return new SensorReadingHandler();
        }
    }

    private SensorReadingHandler readFile() throws IOException, ClassNotFoundException {
        FileInputStream inputStream = new FileInputStream(currentFile);
        ObjectInputStream  objectInputStream = new ObjectInputStream(inputStream);
        SensorReadingHandler readHandler = (SensorReadingHandler) objectInputStream.readObject();
        inputStream.close();
        return readHandler;
    }

    private void deleteFile(String fileNameToDelete) {
        try {
            Files.delete(Paths.get(fileNameToDelete));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized int getNumberOfBackups() {
        return backupFilesToBeReadLater.size();
    }

    public synchronized void removeBackups() {
        while (getNumberOfBackups() > 0) {
            read();
        }
        if (checkIfBackupsWithPrefixExist()) {
            deleteFile(fileContainingListOfBackups);
        }
    }
}
