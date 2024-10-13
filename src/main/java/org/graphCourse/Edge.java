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
    private double flow;

    public Edge(String from, String to, double weight, double flow) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.flow = flow;
    }

    public Edge(String from, String to, double weight) {
        this(from, to, weight, 0.0);
    }

    public Edge(String from, String to) {
        this(from, to, 1.0, 0.0);
    }
    public Edge(Edge e){
        this(e.getFrom(), e.getTo(), e.getWeight(), e.getFlow());
    }

    static public Edge reversed(Edge edge) {
        return new Edge(edge.getTo(), edge.getFrom()
                , edge.getWeight(), edge.getFlow());
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
    public double getCapacity(){
        return weight - flow;
    }

    public double getFlow() {
        return flow;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return Double.compare(weight, edge.weight) == 0 && Objects.equals(from, edge.from) && Objects.equals(to, edge.to) && Objects.equals(flow, edge.flow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, weight, flow);
    }

    public String toString(boolean weighted) {
        if (!weighted) {
            return STR."(\{to})";
        } else {
            return STR."(\{to}, \{weight})";
        }
    }
    public Edge edgeWithFlow(double flow){
        return new Edge(from, to, weight, flow);
    }
    public Edge edgeWithCap(double c){
        return new Edge(from, to, c, flow);
    }
    public void addFlow(double flow){
        this.flow += flow;
    }
}
