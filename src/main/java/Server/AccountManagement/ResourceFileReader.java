package Server.AccountManagement;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ResourceFileReader {
    private final String directoryToSearch;
    private final String extensionToFind;
    private HashMap<String, File> mapOfFiles;
    private HashMap<String, List<String>> linesOfTextOfFiles;

    public ResourceFileReader(String directoryToSearch, String extensionToFind) {
        this.directoryToSearch = directoryToSearch;
        this.extensionToFind = extensionToFind;
        mapOfFiles = new HashMap<>();
        linesOfTextOfFiles = new HashMap<>();
        checkIfDirectoryExists();
        generateListOfFiles();
    }

    private void checkIfDirectoryExists() {
        if(!Files.isDirectory().apply(new File(directoryToSearch))) {
            throw new RuntimeException(directoryToSearch + " is not a valid directory");
        }
    }

    private void generateListOfFiles() {
        File directory = new File(directoryToSearch);
        Arrays.stream(directory.listFiles())
                .filter(file -> file.getName().endsWith(extensionToFind))
                .forEach(file -> {
                    String prefix = file.getName().split("-")[0];
                    mapOfFiles.put(prefix, file);
                });
    }

    public HashMap<String, File> getListOfFiles() {
        return mapOfFiles;
    }

    public List<String> getTextLinesForResource(String filePrefix) {
        try {
            readInLinesOfTextIfNotAlreadyRead(filePrefix);
            return linesOfTextOfFiles.get(filePrefix);
        } catch (Exception e) {
            throw new RuntimeException("Resource file " + filePrefix + " could not be read. Please check files and try again");
        }

    }

    private void readInLinesOfTextIfNotAlreadyRead(String filePrefix) throws IOException {
        if (linesOfTextOfFiles.get(filePrefix) == null) {
            linesOfTextOfFiles.put(filePrefix, Files.readLines(mapOfFiles.get(filePrefix), Charset.defaultCharset()));
        }
    }

}
