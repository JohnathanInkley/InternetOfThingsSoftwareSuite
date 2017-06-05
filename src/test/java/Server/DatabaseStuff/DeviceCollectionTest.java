package Server.DatabaseStuff;

import org.junit.Test;
import static junit.framework.TestCase.assertEquals;

public class DeviceCollectionTest {

    @Test
    public void shouldBeAbleToGetNameOfCollection() {
        DeviceCollection underTest = new DeviceCollection("owner", "factory");
        assertEquals("factory", underTest.name());
    }

    @Test
    public void shouldBeAbleToGetDeviceOwner() {
        DeviceCollection underTest = new DeviceCollection("owner", "factory");
        assertEquals("owner", underTest.ownerName());
    }

    @Test
    public void collectionsWithSameNameAndOwnerShouldBeEqual() {
        DeviceCollection underTest = new DeviceCollection("owner", "factory");
        DeviceCollection otherCollection = new DeviceCollection("owner", "factory");
        assertEquals(otherCollection, underTest);
    }
}