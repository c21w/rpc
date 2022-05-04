package test;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DataImpl implements Data{
    @Override
    public String testData(Object...obj) {
        return Arrays.stream(obj).map(e -> e.toString()).collect(Collectors.joining("\n"));
    }
}
