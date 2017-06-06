package BaseStationCode;


import jssc.SerialPort;
import jssc.SerialPortException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

public class SensorReadingProducer {
    private static final int BAUDRATE = 115200;
    private static final int DATABITS = SerialPort.DATABITS_8;
    private static final int STOPBITS = SerialPort.STOPBITS_1;
    private static final int PARITY = SerialPort.PARITY_NONE;
    private static final String DEV_KEYWORD = "usbmodemL";

    private final SimpleDateFormat dateFormat;
    private String serialPortFilePath;
    private SerialPort serialPort;
    private String currentReadingAsString;
    private String deviceCollection;

    public SensorReadingProducer(String DeviceCollection) {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    public void findPortAndOpen() {
        findSerialPortPath();
        serialPort = new SerialPort(serialPortFilePath);
        try {
            serialPort.openPort();
            serialPort.setParams(BAUDRATE, DATABITS, STOPBITS, PARITY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findSerialPortPath() {
        File deviceFileFolder = new File("/dev/");
        File[] listOfDeviceFiles = deviceFileFolder.listFiles();
        Stream<File> possibleFiles = Arrays.stream(listOfDeviceFiles).filter((file) -> file.getName().contains(DEV_KEYWORD));
        Optional<File> serialPortFile = possibleFiles.min(Comparator.comparingInt(x -> Integer.valueOf(x.getName().split(DEV_KEYWORD)[1])));
        serialPortFilePath = "/dev/" + serialPortFile.get().getName();
    }

    public String getSerialPortPath() {
        return serialPortFilePath;
    }

    public String getSingleReading() {
        try {
            if (serialPort.getInputBufferBytesCount() == 0) {
                return "";
            }
            waitForStartOfReading();
            addTimestamp();
            waitForEndOfReading();
        } catch (SerialPortException e) {
            currentReadingAsString = "[]";
        }
        return currentReadingAsString;
    }

    private void waitForStartOfReading() throws SerialPortException {
        String currentCharacter = "";
        while (!currentCharacter.equals("[")) {
            if (serialPort.getInputBufferBytesCount() > 0) {
                currentCharacter = new String(serialPort.readBytes(1));
            }
        }
    }

    private void addTimestamp() {
        String currentTime = dateFormat.format(new Date());
        currentReadingAsString = "["+ "{time,," + currentTime + "};{DeviceCollection,," + deviceCollection + "};";
    }

    private void waitForEndOfReading() throws SerialPortException {
        while (!currentReadingAsString.endsWith("]")) {
            if (serialPort.getInputBufferBytesCount() > 0) {
                currentReadingAsString = currentReadingAsString + new String(serialPort.readBytes(1));
            }
        }
    }

    public void close() {
        try {
            serialPort.closePort();
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }
}
