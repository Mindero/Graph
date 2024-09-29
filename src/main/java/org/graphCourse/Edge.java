package org.graphCourse;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Edge {
    @JsonProperty("from")
    private final String from;
    @JsonProperty("to")
    private final String to;
    @JsonProperty("weight")
    private final double weight;
    @JsonProperty("mark")
    private final String mark;

    public Edge(String from, String to, double weight, String mark) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.mark = mark;
    }

    public Edge(String from, String to, double weight) {
        this(from, to, weight, "");
    }

    public Edge(String from, String to) {
        this(from, to, 1.0, "");
    }
    public Edge(Edge e){
        this(e.getFrom(), e.getTo(), e.getWeight(), e.getMark());
    }

    static public Edge reversed(Edge edge) {
        return new Edge(edge.getTo(), edge.getFrom()
                , edge.getWeight(), edge.getMark());
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public double getWeight() {
        return weight;
    }

    public String getMark() {
        return mark;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return Double.compare(weight, edge.weight) == 0 && Objects.equals(from, edge.from) && Objects.equals(to, edge.to) && Objects.equals(mark, edge.mark);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, weight, mark);
    }

    public String toString(boolean weighted) {
        if (!weighted) {
            return STR."(\{to})";
        } else {
            return STR."(\{to}, \{weight})";
        }
    }
}
