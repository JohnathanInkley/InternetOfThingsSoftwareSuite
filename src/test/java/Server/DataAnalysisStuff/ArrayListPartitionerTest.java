package Server.DataAnalysisStuff;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertTrue;

public class ArrayListPartitionerTest {

    @Test
    public void shouldTakeArrayListAndPartionBasedOnLambda() {
        ArrayList<Integer> sampleArray = new ArrayList<>();
        for (int i = 3; i >= 0; i--) {
            sampleArray.add(i);
        }

        ArrayList<Integer> subResult1 = new ArrayList<>();
        subResult1.add(0);
        subResult1.add(1);
        ArrayList<Integer> subResult2 = new ArrayList<>();
        subResult2.add(2);
        subResult2.add(3);

        ArrayList<ArrayList<Integer>> expectedResult = new ArrayList<>();
        expectedResult.add(subResult1);
        expectedResult.add(subResult2);

        ArrayListPartitioner underTest = new ArrayListPartitioner(sampleArray);
        ArrayList partionedArray = underTest.partition((x) -> (Integer) x/2);

        assertTrue(expectedResult.get(0).containsAll((Collection<?>) partionedArray.get(0)));
        assertTrue(expectedResult.get(1).containsAll((Collection<?>) partionedArray.get(1)));
    }
}