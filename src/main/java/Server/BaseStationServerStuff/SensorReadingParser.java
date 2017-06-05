package Server.BaseStationServerStuff;

import Server.DatabaseStuff.DatabaseEntry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SensorReadingParser {

    private final Pattern isDoublePattern;
    private DatabaseEntry currentResult;

    public SensorReadingParser() {
        String regExp = "[\\x00-\\x20]*[+-]?(((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*";
        isDoublePattern = Pattern.compile(regExp);
    }

    public DatabaseEntry parseReading(String reading) {
        currentResult = new DatabaseEntry();
        String withoutSquareBrackets = reading.substring(1, reading.length() - 1);
        String[] entries = withoutSquareBrackets.split(";");
        for (String entry : entries) {
            parseIndividualEntry(entry);
        }
        return currentResult;
    }

    private void parseIndividualEntry(String entry) {
        String withoutCurlyBrackets = entry.substring(1, entry.length() - 1);
        String[] fieldEntries = withoutCurlyBrackets.split(",");
        if (fieldEntries[0].equals("time")) {
            currentResult.setTimestamp(fieldEntries[2]);
        } else {
            parseNonTimeEntry(fieldEntries);
        }
    }

    private void parseNonTimeEntry(String[] fieldEntries) {
        Matcher matcher = isDoublePattern.matcher(fieldEntries[2]);
        boolean isDouble = matcher.matches();
        String fieldNameAndUnits = getFieldNameWithUnitsIfApplicable(fieldEntries);
        if (isDouble) {
            currentResult.add(fieldNameAndUnits, Double.parseDouble(fieldEntries[2]));
        } else {
            currentResult.add(fieldNameAndUnits, fieldEntries[2]);
        }
    }

    private String getFieldNameWithUnitsIfApplicable(String[] fieldEntries) {
        String fieldName;
        if (fieldEntries[1].equals("")) {
            fieldName = fieldEntries[0];
        } else {
            fieldName = fieldEntries[0] + "." + fieldEntries[1];
        }
        return fieldName;
    }
}
