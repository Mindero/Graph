package org.graphCourse;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

/*
2 - 18,
3 - 1,
4 - 18,
5 - 23,
6 - 30,
7 - к
8 - 11 (Дейкстра)
9 - 13 (Беллман-Форд)
10 - 18 (Флойд)
 */
public class Main {
    public static void main(String[] args) {
        InputStream in = System.in;
        PrintStream out = System.out;
        Scanner sc = new Scanner(in);
        boolean graphExist = false;
        Graph g = null;
        while (true) {
            if (!graphExist) {
                creatingMenu(out);
                String op = sc.next();
                if (op.equals("3")) {
                    out.println("Введите название файла без расширения");
                    String fileName = STR."\{sc.next()}.txt";
                    File file = new File(fileName);
                    if (!file.exists()) {
                        out.println("Такого файла нет\n");
                        continue;
                    }
                    try (InputStream inFile = new FileInputStream(file)) {
                        g = Graph.ReadGraphFromFile(inFile);
                    } catch (IOException ex) {
                        out.println(ex.getMessage());
                    }
                } else {
                    out.println("Граф взвешенный? [Y/n]");
                    boolean weighted = sc.next().equals( "Y");
                    if (op.equals("1")) {
                        g = new Graph(true, weighted);
                    } else if (op.equals("2")) {
                        g = new Graph(false, weighted);
                    } else throw new UnsupportedOperationException();
                }
                graphExist = true;
            }
            menu(out);
            String op = sc.next();
            assert g != null;
            try {
                if (op.equals("1")) {
                    out.println("Введите название вершины");
                    String vertex = sc.next();
                    g.addVertex(vertex);
                } else if (op.equals("2")) {
                    out.println("Введите название вершины");
                    String vertex = sc.next();
                    g.deleteVertex(vertex);
                } else if (op.equals("3")) {
                    out.println("Введите пару вершин");
                    String from = sc.next();
                    String to = sc.next();
                    double w = 1.0;
                    if (g.isWeighted()) {
                        out.println("Введите вес");
                        w = sc.nextDouble();
                    }
                    g.addEdge(new Edge(from, to, w));
                } else if (op.equals("4")) {
                    out.println("Введите пару вершин");
                    String from = sc.next();
                    String to = sc.next();
                    g.removeEdge(new Edge(from, to));
                } else if (op.equals("5")) {
                    out.println(g);
                } else if (op.equals("6")) {
                    out.println("Введите название файла без расширения");
                    String fileName = STR."\{sc.next()}.txt";
                    File file = new File(fileName);
                    try {
                        file.createNewFile();
                        OutputStream outFile = new FileOutputStream(file);
                        g.printInFile(outFile);
                    } catch (IOException e) {
                        out.println("Такого файла нет");
                    }
                } else if (op.equals("7")) {
                    graphExist = false;
                    g = null;
                } else if (op.equals("8")) {
                    out.println("Введите вершину x");
                    String x = sc.next();
                    out.println("Введите вершину y");
                    String y = sc.next();
                    Optional<String> res = g.task1(x, y);
                    if (res.isEmpty()) out.println("Такой вершины нет");
                    else out.println(res.get());
                } else if (op.equals("9")) {
                    out.println("Введите вершину");
                    String x = sc.next();
                    out.println(g.task2(x));
                } else if (op.equals("10")){
                    g = Graph.task3(g);
                }
                else if (op.equals("11")) {
                    Optional<String> node = g.task4();
                    if (node.isEmpty()) {
                        out.println("Нет такой вершины");
                    }
                    else{
                        out.println("Подходит вершина " + node.get());
                    }
                }
                else if (op.equals("12")){
                    out.println("Введите вершину u1");
                    String u1 = sc.next();
                    out.println("Введите вершину u2");
                    String u2 = sc.next();
                    out.println("Введите вершину v");
                    String v = sc.next();
                    Optional<String> node = g.task5(u1, u2, v);
                    if (node.isEmpty()){
                        out.println(STR."Обе вершины не достигают вершину \{v}");
                    }
                    else out.println(STR."От вершины \{node.get()} путь короче");
                }
                else if (op.equals("13")){
                    Graph mst = g.task6();
                    out.println(mst.toString());
                }
                else if (op.equals("14")){
                    Set<String> center = g.findCenter();
                    out.println("Центр графа = " + center);
                }
                else if (op.equals("15")){
                    out.println("Введите вершину u1");
                    String u1 = sc.next();
                    out.println("Введите вершину u2");
                    String u2 = sc.next();
                    out.println("Введите вершину v");
                    String v = sc.next();
                    List<Optional<List<String>>> path1 = g.shortestPath(v, u1, u2);
                    if (path1.getFirst().isEmpty()){
                        out.println(STR."Кратчайшего пути из \{u1}до \{v} не существует");
                    }
                    else{
                        out.println(STR."Кратчайший путь от \{u1} до \{v}:\n\{path1.getFirst().get()}\n");
                    }
                    if (path1.getLast().isEmpty()){
                        out.println(STR."Кратчайшего пути из \{u2}до \{v} не существует");
                    }
                    else{
                        out.println(STR."Кратчайший путь от \{u2} до \{v}:\n\{path1.getLast().get()}\n");
                    }
                }
                else if (op.equals("16")){
                    Optional<List<String>> cycle = g.findNegativeCycle();
                    if (cycle.isEmpty()) out.println("Цикла отрицательного веса не существует");
                    else out.println(cycle.get());
                }
                else throw new UnsupportedOperationException();
            }
            catch (Exception ex){
                out.println(ex.getMessage());
            }
        }
    }

    static void menu(PrintStream out) {
        out.println(
                "1 - добавить вершину\n" +
                "2 - удалить вершину\n" +
                "3 - добавить ребро\n" +
                "4 - удалить ребро\n" +
                "5 - Посмотреть список смежности\n" +
                "6 - Вывести список смежности в файл\n" +
                "7 - Создать новый граф\n" +
//                "8 - Определить существует ли вершина, в которую есть дуга из x, но нет из y\n" +
//                "9 - Вывести для вершины все <выходящие> соседние вершины\n" +
//                "10 - Построить граф, полученный однократным удалением вершин с нечётными степенями\n" +
//                "11 - Проверить, можно ли из орграфа удалить какую-либо вершину так, чтобы получилось дерево\n" +
//                "12 - Определить, от какой из вершин u1 и u2 путь до v короче (по числу дуг).\n" +
//                "13 - Построить минимальное остовное дерево (алгоритм Краскала)"
                "14 - Найти центр графа\n" +
                "15 - Вывести кратчайшие пути из u1 и u2 до v\n" +
                "16 - Вывести цикл отрицательного веса, если он есть\n"
                );
    }

    static void creatingMenu(PrintStream out) {
        out.println(
                "1 - создать пустой ориентированный граф\n" +
                        "2 - создать пустой неориентированный граф\n" +
                        "3 - Прочитать граф из файла");
    }
}