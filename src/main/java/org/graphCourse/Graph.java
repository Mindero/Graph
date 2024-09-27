package org.graphCourse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;


public class Graph {
    // true- ориентированный, false - неориентированный
    @JsonProperty("oriented")
    private final boolean oriented;
    @JsonProperty("weighted")
    private final boolean weighted;
    @JsonProperty("graph")
    private final Map<String, Set<Edge>> graph;

    public Graph(boolean oriented, boolean weighted, Map<String, Set<Edge>> graph) {

        this.oriented = oriented;
        this.graph = graph.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> new HashSet<>(e.getValue())));
        this.weighted = weighted;
    }

    public Graph(boolean orientation, boolean weighted) {
        oriented = orientation;
        graph = new HashMap<>();
        this.weighted = weighted;
    }

    static public Graph copyOf(Graph g) {
        return new Graph(g.oriented, g.weighted, g.graph);
    }

    public static Graph ReadGraphFromFile(InputStream in) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(in);

        Map<String, Object> map = mapper.convertValue(jsonNode, Map.class);
        boolean oriented = (boolean) map.get("oriented");
        boolean weighted = (boolean) map.get("weighted");
        Map<String, List<Map<String, Object>>> graphJson =
                (Map<String, List<Map<String, Object>>>) map.get("graph");
        Map<String, Set<Edge>> graph = new HashMap<>();
        graphJson.forEach((key, value) -> {
            Set<Edge> edges = new HashSet<>();
            value.forEach(edge -> {
                String from = (String) edge.get("from");
                String to = (String) edge.get("to");
                double weight = (double) edge.get("weight");
                String mark = (String) edge.get("mark");
                edges.add(new Edge(from, to, weight, mark));
            });
            graph.put(key, edges);
        });
        return new Graph(oriented, weighted, graph);
    }

    @JsonIgnore
    public int getCountNodes() {
        return graph.size();
    }

    public void addVertex(String value) {
        if (graph.containsKey(value)) {
            throw new IllegalArgumentException("Вершина с таким номером уже добавлена");
        }
        graph.put(value, new HashSet<>());
    }

    public void deleteVertex(String value) {
        if (!graph.containsKey(value)) {
            throw new IllegalArgumentException("Нет такой вершины");
        }
        graph.forEach((key, edges) -> edges.removeIf(edge -> edge.getTo().equals(value)));
        graph.remove(value);
    }

    public void addEdge(Edge edge) {
        if (!graph.containsKey(edge.getFrom()) || !graph.containsKey(edge.getTo())) {
            throw new IllegalArgumentException("Нет такой вершины");
        }
        String source = edge.getFrom(), target = edge.getTo();
        if (graph.get(source).stream().anyMatch(ed -> ed.getTo().equals(target))){
            throw new IllegalArgumentException("Уже существует ребро");
        }
        graph.get(edge.getFrom()).add(edge);
        if (!oriented) {
            Edge newEdge = Edge.reversed(edge);
            graph.get(newEdge.getFrom()).add(newEdge);
        }
    }

    public void removeEdge(Edge edge) {
        String from = edge.getFrom(), to = edge.getTo();
        if (!graph.containsKey(from) || !graph.containsKey(to)) {
            throw new IllegalArgumentException("Нет такой вершины");
        }
        Set<Edge> fromEdges = graph.get(edge.getFrom()),
                toEdges = graph.get(edge.getTo());
        Optional<Edge> fromEdge = fromEdges.stream().filter(ed -> ed.getTo().equals(to)).findAny();
        Optional<Edge> toEdge = toEdges.stream().filter(ed -> ed.getTo().equals(from)).findAny();
        if (fromEdge.isEmpty()
                || (!oriented && toEdge.isEmpty())) {
            throw new IllegalArgumentException("Нет такого ребра");
        }
        fromEdges.remove(fromEdge.get());
        if (!oriented) {
            toEdges.remove(toEdge.get());
        }
    }

    public void printInFile(OutputStream file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(file, this);
    }

    @JsonIgnore
    public List<Edge> getEdgeList() {
        List<Edge> edgeList = new ArrayList<>();
        for (var entry : graph.entrySet()) {
            edgeList.addAll(entry.getValue().stream().toList());
        }
        return edgeList;
    }

    public boolean isWeighted() {
        return weighted;
    }

    @Override
    public String toString() {
        if (getCountNodes() == 0) {
            return "Граф пустой";
        }
        StringBuilder result = new StringBuilder();
        graph.forEach((key, value) -> {
            result.append(STR."\{key}: ");
            value.forEach(edge -> {
                result.append(edge.toString(weighted)).append(", ");
            });
            result.append('\n');
        });
        return result.toString();
    }

    public boolean isOriented() {
        return oriented;
    }

    public Map<String, Set<Edge>> getGraph() {
        return new HashMap<>(graph);
    }

    // Определить существует ли вершина, в которую есть дуга из x, но нет из y
    public Optional<String> task1(String x, String y) {
        Set<String> xNeighbours =
                graph.get(x).stream().map(Edge::getTo)
                        .collect(Collectors.toSet());
        Set<String> yNeighbours =
                graph.get(y).stream().map(Edge::getTo)
                        .collect(Collectors.toSet());
        return xNeighbours.stream().filter(val -> !yNeighbours.contains(val))
                .findAny();
    }

    // Для данной вершины орграфа вывести все «выходящие» соседние вершины.
    public List<String> task2(String x) {
        if (!graph.containsKey(x))
            throw new IllegalArgumentException("Такой вершины нет");
        if (!oriented)
            throw new UnsupportedOperationException("Задача не определена для неориентированного графа");
        return graph.get(x).stream().map(Edge::getTo).toList();
    }

    // Построить граф, полученный однократным удалением вершин с нечётными степенями.
    public static Graph task3(Graph g) {
        Graph h = Graph.copyOf(g);
        g.getGraph().forEach((key, value) -> {
            if (value.size() % 2 == 1){
                h.deleteVertex(key);
            }
        });
        return h;
    }

    private Map<String, Set<Edge>> reverse_graph(){
        Map<String, Set<Edge>> reversed_graph = new HashMap<>();
        graph.keySet().forEach(v -> reversed_graph.put(v, new HashSet<>()));
        for (var entry : graph.entrySet()){
            String v = entry.getKey();
            Set<Edge> edges = entry.getValue();
            for (Edge edge : edges){
                Edge reversed_edge = Edge.reversed(edge);
                String to = edge.getTo();
                Set<Edge> gr = reversed_graph.get(to);
                gr.add(reversed_edge);
                reversed_graph.put(to, gr);
            }
        }
        return reversed_graph;
    }

    // Проверить, можно ли из орграфа удалить какую-либо вершину так, чтобы получилось дерево.
    public Optional<String> task4() {
        Map<String, Set<Edge>> reversed_graph = reverse_graph();
        for (String del_v : graph.keySet()){
            List<String> start_vertexes =
                    graph.keySet().stream()
                                  .filter(v -> (!v.equals(del_v) && startedVertex(v, del_v, reversed_graph)))
                                  .toList();
            if (start_vertexes.size() == 1){
                String start_vertex = start_vertexes.getFirst();
                Set<String> used = new HashSet<>();
                used.add(del_v);
                Queue<String> q = new ArrayDeque<>();
                q.add(start_vertex);
                used.add(start_vertex);
                boolean result = true;
                while (!q.isEmpty()){
                    String v = q.poll();
                    for (Edge edge : graph.get(v)){
                        String u = edge.getTo();
                        if (u.equals(del_v)) continue;

                        if (used.contains(u)) {
                            result = false;
                            break;
                        }
                        else {
                            q.add(u);
                            used.add(u);
                        }
                    }
                }
                if (used.size() == graph.size() && result){
                    return Optional.of(del_v);
                }
            }
        }
        return Optional.empty();
    }
    // Вершина start может быть корнем дерева
    private boolean startedVertex (String start, String del_v, Map<String, Set<Edge>> graph){
        Set<Edge> startEdges = graph.get(start);
        // Если
        return startEdges.isEmpty() || startEdges.stream().allMatch(edge -> edge.getTo().equals(del_v));
    }

}
