package Server.DataAnalysisStuff;

import java.util.ArrayList;
import java.util.function.ToIntFunction;

public class ArrayListPartitioner<T> {
    private final ArrayList<T> arrayToPartition;

    public ArrayListPartitioner(ArrayList<T> arrayToPartition) {
        this.arrayToPartition = arrayToPartition;
    }

    public ArrayList<ArrayList<T>> partition(ToIntFunction<T> functionToProduceArrayPosition) {
        ArrayList<ArrayList<T>> partionedArray = new ArrayList<>();

        for (T entry : arrayToPartition) {
            int positionInArray = functionToProduceArrayPosition.applyAsInt(entry);
            if (partionedArray.size() <= positionInArray || partionedArray.get(positionInArray) == null) {
                for (int i = partionedArray.size(); i <= positionInArray; i++) {
                    partionedArray.add(i, new ArrayList<T>());
                }
            }
            partionedArray.get(positionInArray).add(entry);
        }
        return partionedArray;
    }
}
