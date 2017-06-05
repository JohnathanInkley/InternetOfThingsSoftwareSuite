package Server.DatabaseStuff;

public class DatabaseEntryField {
    private String fieldName;
    private Object fieldValue;

    public DatabaseEntryField(String fieldName, Object fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    @Override
    public boolean equals(Object other) {
        DatabaseEntryField otherField = (DatabaseEntryField) other;
        return fieldName.equals(otherField.fieldName) && fieldValue.equals(otherField.fieldValue);
    }

    @Override
    public int hashCode() {
        return fieldName.hashCode() + fieldValue.hashCode();
    }
}
