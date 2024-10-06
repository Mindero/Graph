package org.graphCourse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Double.min;


public class Graph {
    // true- ориентированный, false - неориентированный
    @JsonProperty("oriented")
    private final boolean oriented;
    @JsonProperty("weighted")
    private final boolean weighted;
    @JsonProperty("graph")
    private final Map<String, Set<Edge>> graph;
    private final double EPSILON = -1e6;

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
    int nodes_count(){
        return graph.size();
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
    public boolean vertexExist(String v){
        return graph.containsKey(v);
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
            throw new IllegalArgumentException(STR."Уже существует ребро между \{source}, \{target}");
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
        return new ArrayList<>(edgeList);
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
        if (!oriented)
            throw new UnsupportedOperationException("Задача не определена для неориентированного графа");
        Map<String, Set<Edge>> reversed_graph = reverse_graph();
        for (String del_v : graph.keySet()){
            List<String> start_vertexes =
                    graph.keySet().stream()
                                  .filter(v -> (!v.equals(del_v) && startedVertex(v, del_v, reversed_graph)))
                                  .toList();
            if (start_vertexes.size() == 1){
                Map<String, Integer> used = new HashMap<>();
                used.put(del_v, 1);
                boolean result = dfs(start_vertexes.getFirst(), used);
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

    public boolean dfs(String v, Map<String, Integer> used){
        used.put(v, 1);
        for (Edge edge : graph.get(v)){
            String u = edge.getTo();
            if (used.containsKey(u)) return false;
            if (!dfs(u, used)) return false;
        }
        return true;
    }

    // Определить, от какой из вершин u1 и u2 путь до v короче (по числу дуг).
    // Возвращает Optional.empty если обе вершины не достигают v.
    public Optional<String> task5 (String u1, String u2, String v){
        Optional<Integer> dist1 = findMinDistUnweighted(u1, v);
        Optional<Integer> dist2 = findMinDistUnweighted(u2, v);
        if (dist1.isEmpty() && dist2.isEmpty()) return Optional.empty();
        if (dist1.isEmpty()) return Optional.of(u2);
        if (dist2.isEmpty()) return Optional.of(u1);
        if (dist1.get() <= dist2.get()) return Optional.of(u1);
        else return Optional.of(u2);
    }

    // Найти длину кратчайшего пути от вершины s до вершины t
    // Работает для невзвешенных графов
    // возращает Optional.empty(), если из s не достижима t
    // bfs
    public Optional<Integer> findMinDistUnweighted (String s, String t){
        Map<String, Integer> dist = new HashMap<>();
        Queue<String> q = new ArrayDeque<>();
        dist.put(s, 0);
        q.add(s);
        while(!q.isEmpty()){
            String v = q.poll();
            Integer distV = dist.get(v);
            if (v.equals(t)) return Optional.of(distV);
            for (Edge edge : graph.get(v)){
                String u = edge.getTo();
                if (!dist.containsKey(u) || dist.get(u) > distV + 1){
                    dist.put(u, distV + 1);
                    q.add(u);
                }
            }
        }
        return Optional.empty();
    }

    // Построение MST алгоритмом Краскала
    public Graph task6(){
        if (oriented)
            throw new UnsupportedOperationException("Задача не определена для неориентированного графа");
        Graph mst = new Graph(false, weighted);
        graph.keySet().forEach(mst::addVertex);
        List<Edge> edges = getEdgeList();
        edges.sort(Comparator.comparingDouble(Edge::getWeight));
        DSU d = new DSU(graph.keySet());
        for (Edge edge : edges){
            String x = edge.getFrom();
            String y = edge.getTo();
            if (d.merge(x, y)){
                mst.addEdge(new Edge(edge));
            }
        }
        return mst;
    }
    public Set<String> findCenter(){
        Set<String> center = new HashSet<>();
        Double minDist = null;
        for (String node : graph.keySet()){
            double eccentricity = findEccentricity(node);
            if (minDist == null || eccentricity <= minDist){
                if (minDist != null && eccentricity < minDist) center.clear();
                minDist = eccentricity;
                center.add(node);
            }
        }
        return center;
    }
    // Dijkstra
    double findEccentricity(String node){
        Map<String, Double> dist = new HashMap<>();
        TreeSet<Pair<Double, String>> q = new TreeSet<>();
        dist.put(node, 0.0);
        q.add(new Pair<>(0.0, node));
        while (!q.isEmpty()){
            String v = q.getFirst().second();
            double d = q.getFirst().first();
            System.out.println(STR."\{v} \{d}");
            q.pollFirst();
            for (Edge edge : graph.get(v)){
                String u = edge.getTo();
                double w = edge.getWeight();
                if (!dist.containsKey(u) || dist.get(u) > d + w){
                    if (dist.containsKey(u))
                        q.remove(new Pair<>(dist.get(u), u));
                    dist.put(u, d + w);
                    q.add(new Pair<>(d + w, u));
                }
            }
        }
        return dist.values().stream().max(Double::compare).get();
    }
    // Найти кратчайшие пути из u1 и u2 до v.
    // Возвращает массив:
    // [0] - Optional из пути из u1 до v или empty
    // [1] - Optional из пути из u2 до v или empty
    // Ford-Bellman
    public List<Optional<List<String>>> shortestPath (String v, String u1, String u2){
        List<Optional<List<String>>> paths = new ArrayList<>();
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> pr = new HashMap<>();
        pr.put(v, v);
        dist.put(v, 0.0);
        List<Edge> edges = getEdgeList();
        for (int i = 0; i < nodes_count(); ++i){
            for (Edge edge : edges){
                String from = edge.getFrom();
                String to = edge.getTo();
                double w = edge.getWeight();
                if (dist.containsKey(from) &&
                        (!dist.containsKey(to) || dist.get(to) > dist.get(from) + w)){
                    dist.put(to, dist.get(from) + w);
                    pr.put(to, from);
                }
            }
        }
        paths.add(recover_path(v, u1, pr, dist));
        paths.add( recover_path(v, u2, pr, dist));
        return paths;
    }
    // Восстановление кратчайшего пути
    private Optional<List<String>> recover_path(String from, String to
                                                ,Map<String, String> pr, Map<String, Double> dist){
        if (!dist.containsKey(to)) return Optional.empty();
        String cur = to;
        List<String> path = new ArrayList<>();
        while(!cur.equals(from)){
            path.add(cur);
            cur = pr.get(cur);
        }
        path.add(from);
        return Optional.of(path);
    }

    // Floyd-Warshall
    public Optional<List<String>> findNegativeCycle(){
        BiMap<String, Integer> index = HashBiMap.create();
        graph.keySet().forEach(k-> index.put(k, index.size()));
        int sz = index.size();
        double[][] dist = new double[sz][sz];
        int[][] from = new int[sz][sz];
        // Заполняем бесконечностью
        for (int i = 0; i < sz; ++i){
            for (int j = 0; j < sz; ++j){
                from[i][j] = -1;
                if (i != j) dist[i][j] = Double.POSITIVE_INFINITY;
                else dist[i][j] = 0;
            }
        }
        // Проход по ребрам
        for (String node : graph.keySet()){
            for (Edge edge : graph.get(node)){
                String to = edge.getTo();
                double w = edge.getWeight();
                int node_index = index.get(node);
                int to_index = index.get(to);
                from[node_index][to_index] = node_index;
                dist[node_index][to_index] =
                        Double.min(dist[node_index][to_index], w);
            }
        }
        // алгоритм
        for (int k = 0; k < sz; ++ k)
            for (int i = 0; i < sz; ++i)
                for (int j = 0; j < sz; ++j){
                    if ( dist[i][k] != Double.POSITIVE_INFINITY
                         && dist[k][j] != Double.POSITIVE_INFINITY
                         && dist[i][k] + dist[k][j] < dist[i][j]){
                        dist[i][j] = dist[i][k] + dist[k][j];
                        from[i][j] = from[k][j];
                    }
                }
        // Нахождение первой вершины в цикле
        List<String> cycle = null;
        for (int i = 0; i < sz; ++i) if (dist[i][i] < 0.0){
            for (int j = 0 ; j < sz - 1; ++j)
                i = from[i][i];
            cycle = new ArrayList<>();
            cycle.add(index.inverse().get(i));
            for (int v = from[i][i]; v != i; v = from[i][v]){
                cycle.add(index.inverse().get(v));
            }
            cycle = cycle.reversed();
            break;
        }
        return Optional.ofNullable(cycle);
    }
}
