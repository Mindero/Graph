package org.graphCourse;


import java.util.Objects;

import static java.util.Objects.compare;

public record Pair<F extends Comparable<?>, S extends Comparable<?>>(F first, S second) implements Comparable<Pair<F, S>> {
    @Override
    public int compareTo(Pair<F, S> o) {
        int cmp = compare(first, o.first);
        return cmp == 0 ? compare(second, o.second) : cmp;
    }
    private static int compare(Object o1, Object o2) {
        return o1 == null ? o2 == null ? 0 : -1 : o2 == null ? +1
                : ((Comparable) o1).compareTo(o2);
    }

    @Override
    public F first() {
        return first;
    }

    @Override
    public S second() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
