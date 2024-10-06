package org.graphCourse;
import java.util.*;

public class DSU {
    private final Map<String, Integer> sz;
    private final Map<String, String> to;

    public DSU(Set<String> vertex){
        sz = new HashMap<>();
        to = new HashMap<>();
        vertex.forEach(v -> {
            sz.put(v, 1);
            to.put(v, v);
        });
    }
    private String get (String v){
        if (to.get(v).equals(v)) {
            return v;
        }
        String nxt = get(to.get(v));
        to.put(v, nxt);
        return nxt;
    }

    public boolean merge (String x, String y){
        x = get(x);
        y = get(y);
        if (x.equals(y)) {
            return false;
        }
        int xSize = sz.get(x);
        int ySize = sz.get(y);
        // y -> x
        if (xSize > ySize){
            sz.put(x, xSize + ySize);
            to.put(y, x);
        }
        // x -> y
        else{
            sz.put(y, xSize + ySize);
            to.put(x, y);
        }
        return true;
    }
}
