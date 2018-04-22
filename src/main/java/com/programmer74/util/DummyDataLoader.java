package com.programmer74.util;

import java.util.ArrayList;
import java.util.List;

public class DummyDataLoader {
    public static List<Pair<Double>> loadDummyData() {
        List<Pair<Double>> data = new ArrayList<>();
        data.add(new Pair<>(0.0, 2.0));
        data.add(new Pair<>(1.0, 3.0));
        data.add(new Pair<>(2.0, 4.0));
        data.add(new Pair<>(3.0, 5.0));
        data.add(new Pair<>(4.0, 6.0));
        data.add(new Pair<>(5.0, 7.0));
        data.add(new Pair<>(6.0, 8.0));
        return data;
    }
}
