import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class Graph {
    private static final int infinity = Integer.MAX_VALUE;

    private int[][] adj_matrix;
    private String[] vertex_names;
    private Map<String, Map<String, Integer>> adj_list;

    public Graph(String file_name) {
        read_file(file_name);
        build_adj_list();
    }

    private void read_file(String file_name) {
        try (BufferedReader br = new BufferedReader(new FileReader(file_name))) {
            String[] cities = br.readLine().split(",");
            vertex_names = Arrays.copyOfRange(cities, 1, cities.length);

            int size = vertex_names.length;
            adj_matrix = new int[size][size];

            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length != size + 1) {
                    throw new IOException("Invalid number of columns!!");
                }
                
                for (int col = 1; col < values.length; col++) {
                    if (values[col].equals("Infinity")) {
                        adj_matrix[row][col - 1] = infinity;
                    } else {
                        adj_matrix[row][col - 1] = Integer.parseInt(values[col]);
                    }
                }
                row++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void build_adj_list() {
        adj_list = new HashMap<>();

        for (int i = 0; i < vertex_names.length; i++) {
            Map<String, Integer> neighbors = new HashMap<>();

            for (int j = 0; j < vertex_names.length; j++) {
                if (adj_matrix[i][j] != infinity) {
                    neighbors.put(vertex_names[j], adj_matrix[i][j]);
                }
            }

            adj_list.put(vertex_names[i], neighbors);
        }
    }

    public int[][] getAdjacencyMatrix() {
        return adj_matrix;
    }

    public String[] getVertexNames() {
        return vertex_names;
    }

    public Map<String, Map<String, Integer>> getAdjacencyList() {
        return adj_list;
    }
}


class DFS {
    private static int dis_shortest = Integer.MAX_VALUE;
    private static List<String> path_shortest = new ArrayList<>();

    public static void depthFirstSearch(Graph graph, String startVertex, String endVertex, int depth_limit, JTextArea result1, JTextArea result2) {
    	String[] vertexNames = graph.getVertexNames();
    	int[][] adjacencyMatrix = graph.getAdjacencyMatrix();
        int infinity = Integer.MAX_VALUE;
        
        ArrayList<String> path_DFS = new ArrayList<>();
        boolean[] visited_DFS = new boolean[vertexNames.length];
        int sum_DFS = 0;

        path_DFS.add(startVertex);      
        dls(graph.getAdjacencyList(), startVertex, endVertex, 0, path_DFS, visited_DFS, sum_DFS, infinity, depth_limit);
        if (dis_shortest == Integer.MAX_VALUE) {
            System.out.println("No path found by DFS.");
        } else {
            System.out.println("Shortest distance found by DFS: " + dis_shortest);
            System.out.println("Shortest path found by DFS: \n\n" + path_format(path_shortest, graph));
            result1.setText("Shortest distance found by DFS: \n" + dis_shortest);
            result2.setText("Shortest path found by DFS: \n\n" + path_format(path_shortest, graph));
        }
    }

   
    private static void dls(Map<String, Map<String, Integer>> adj_list, String start_Vertex, String end_Vertex, int current_depth, ArrayList<String> current_path, boolean[] visited, int sum, int infinity, int depth_limit) {
        if (start_Vertex.equals(end_Vertex)) { 
            int dist = total_Dis_Calculator(current_path, adj_list); 
            if (dist < dis_shortest) { 
                dis_shortest = dist; 
                path_shortest = new ArrayList<>(current_path); 
            }
            return; 
        }

        if (current_depth >= depth_limit) { 
            return; 
        }

        visited[getVertexIndex(start_Vertex, adj_list)] = true;

        Map<String, Integer> neighbors = adj_list.get(start_Vertex); 
        for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) { 
            String next_vertex = neighbor.getKey(); 
            int dis = neighbor.getValue(); 
            if (dis < 99999 && !visited[getVertexIndex(next_vertex, adj_list)]) { 
                current_path.add(next_vertex); 
                dls(adj_list, next_vertex, end_Vertex, current_depth + 1, current_path, visited, sum, infinity, depth_limit); 
                current_path.remove(current_path.size() - 1); 
            }
        }

        visited[getVertexIndex(start_Vertex, adj_list)] = false;
    }

    
    private static int total_Dis_Calculator(ArrayList<String> path, Map<String, Map<String, Integer>> adj_list) {
        int dis_total = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            String current_vertex = path.get(i);
            String next_vertex = path.get(i + 1);
            dis_total += adj_list.get(current_vertex).get(next_vertex);
        }
        return dis_total;
    }

    
    private static int getVertexIndex(String vertex_name, Map<String, Map<String, Integer>> adj_list) {
        int i = 0;
        for (String vertex : adj_list.keySet()) {
            if (vertex.equals(vertex_name)) {
                return i;
            }
            i++;
        }
        throw new NullPointerException("Error: Vertex not found.");
    }

    private static String path_format(List<String> path, Graph graph) {
        StringBuilder formatted_path = new StringBuilder();
        int dis_total = 0;

        Map<String, Map<String, Integer>> adj_list = graph.getAdjacencyList(); 

        for (int i = 0; i < path.size() - 1; i++) {
            String current_vertex = path.get(i);
            String next_vertex = path.get(i + 1);
            int dis = adj_list.get(current_vertex).get(next_vertex);

            formatted_path.append(current_vertex).append(" \n ").append(dis).append(" km.\n ");
            dis_total += dis;
        }

        formatted_path.append(path.get(path.size() - 1));
        formatted_path.append(" \n\nTotal Distance: ").append(dis_total).append(" km.");
        return formatted_path.toString();
    }
}


 class BFS {
    private static Map<String, Integer> Map_distance;  

    public static void breadthFirstSearch(Graph graph, String start_vertex, String end_vertex, JTextArea text1, JTextArea text2) {
        
        String[] vertexNames = graph.getVertexNames();
        int[][] adjacencyMatrix = graph.getAdjacencyMatrix();

        Map<String, String> initial_Map = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        Map_distance = new HashMap<>();  

        initial_Map.put(start_vertex, null);
        Map_distance.put(start_vertex, 0);
        queue.offer(start_vertex);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            
            for (int i = 0; i < vertexNames.length; i++) {   
            	if (adjacencyMatrix[getVertexIndex(current , vertexNames)][i] != 0) {
                    String neighbor = vertexNames[i];
                    int dis_neighbor = adjacencyMatrix[getVertexIndex(current , vertexNames)][i];
                    int dis_total = Map_distance.get(current) + dis_neighbor;

                    if (!Map_distance.containsKey(neighbor) || dis_total < Map_distance.get(neighbor)) {
                        Map_distance.put(neighbor, dis_total);
                        initial_Map.put(neighbor, current);
                        queue.offer(neighbor);
                    }
                }
            }
        }

        bfs_results(initial_Map, start_vertex, end_vertex, adjacencyMatrix, vertexNames, text1, text2);
    }

   
    private static void bfs_results(Map<String, String> initial_map, String start_Vertex, String end_Vertex, int[][] adjacencyMatrix, String[] vertex_Names, JTextArea text1, JTextArea text2) {
        List<String> path = new ArrayList<>();
        String end = end_Vertex;

        while (end != null && !end.equals(start_Vertex)) {
            path.add(end);
            end = initial_map.get(end);
        }
        if (end != null && end.equals(start_Vertex)) {
            path.add(start_Vertex);
            Collections.reverse(path);
        }

        System.out.println("Shortest distance found by BFS: " + Map_distance.get(end_Vertex));
        System.out.println("Shortest path found by BFS: \n\n" + path_format(path, adjacencyMatrix, vertex_Names));
        text1.append("Shortest distance found by BFS: \n" + Map_distance.get(end_Vertex));
        text2.append("Shortest path found by BFS: " + "\n\n " + path_format(path, adjacencyMatrix, vertex_Names));
    }

    
    private static String path_format(List<String> path, int[][] adjacencyMatrix, String[] vertexNames) {
        StringBuilder formatted_path = new StringBuilder();
        int dis_total = 0;

        for (int i = 0; i < path.size() - 1; i++) {
            String current_vertex = path.get(i);
            String next_vertex = path.get(i + 1);
            int distance = adjacencyMatrix[getVertexIndex(current_vertex, vertexNames)][getVertexIndex(next_vertex, vertexNames)];

            formatted_path.append(current_vertex).append("\n ").append(distance).append(" km.\n ");
            dis_total += distance;
        }

        formatted_path.append(path.get(path.size() - 1));
        formatted_path.append("\n\n Total Distance: ").append(dis_total).append(" km.");

        return formatted_path.toString();
    }

   
    private static int getVertexIndex(String vertexName, String[] vertexNames) {
        for (int i = 0; i < vertexNames.length; i++) {
            if (vertexNames[i].equals(vertexName)) {
                return i;
            }
        }
        throw new NullPointerException("Error: Vertex not found.");
    }
}
 
 

public class UserInterface {
	
    private static JFrame main_frame;
    private static JFrame app_frame;
    private static JTextField Textfield_start_city;
    private static JTextField Textfield_destination_city;
    private static String start_city;
    private static String destination_city;
	
    public static void main(String[] args) {
    	
    	SwingUtilities.invokeLater(UserInterface::create_main_menu); 
    	
    }
    
    private static void create_main_menu() {
        main_frame = new JFrame("Shortest Path Finder");
        main_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gb_cons = new GridBagConstraints();

        JLabel start_label = new JLabel("Enter the starting city:");
        Textfield_start_city = new JTextField(20);

        JLabel destination_label = new JLabel("Enter the destination city:");
        Textfield_destination_city = new JTextField(20);

        JButton cont_button = new JButton("Continue");
        cont_button.addActionListener(e -> {
            start_city = Textfield_start_city.getText().trim();
            destination_city = Textfield_destination_city.getText().trim();
            if (start_city.isEmpty() || destination_city.isEmpty()) {
                JOptionPane.showMessageDialog(main_frame, "Please enter both start and destination cities.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } else {
                main_frame.dispose();
                algorithm_choice_panel();
            }
        });

        panel.setBackground(Color.BLACK);  
        start_label.setForeground(Color.WHITE);  
        Textfield_start_city.setBackground(Color.BLACK);
        Textfield_start_city.setForeground(Color.WHITE);
        destination_label.setForeground(Color.WHITE);
        Textfield_destination_city.setBackground(Color.BLACK);
        Textfield_destination_city.setForeground(Color.WHITE);
        cont_button.setBackground(Color.BLACK);
        cont_button.setForeground(Color.WHITE);

        gb_cons.gridx = 0;
        gb_cons.gridy = 0;
        gb_cons.insets = new Insets(10, 10, 10, 10);
        panel.add(start_label, gb_cons);

        gb_cons.gridx = 1;
        gb_cons.gridy = 0;
        gb_cons.insets = new Insets(10, 10, 10, 10);
        panel.add(Textfield_start_city, gb_cons);

        gb_cons.gridx = 0;
        gb_cons.gridy = 1;
        panel.add(destination_label, gb_cons);

        gb_cons.gridx = 1;
        gb_cons.gridy = 1;
        panel.add(Textfield_destination_city, gb_cons);

        gb_cons.gridy = 2;
        panel.add(cont_button, gb_cons);

        main_frame.getContentPane().add(BorderLayout.CENTER, panel);
        main_frame.setLocationRelativeTo(null);
        main_frame.setSize(500, 200);
        main_frame.setVisible(true);
    }

    private static void algorithm_choice_panel() {
        JFrame algorithm_frame = new JFrame("Algorithm Choice");
        algorithm_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gb_cons = new GridBagConstraints();

        JButton dfs_button = menu_button("Find Shortest Path using DFS", e -> path_finder("DFS"));

        JButton bfs_button = menu_button("Find Shortest Path using BFS", e -> path_finder("BFS"));

        panel.setBackground(Color.BLACK);  
        dfs_button.setBackground(Color.BLACK);
        dfs_button.setForeground(Color.WHITE);
        bfs_button.setBackground(Color.BLACK);
        bfs_button.setForeground(Color.WHITE);

        gb_cons.gridx = 0;
        gb_cons.gridy = 0;
        gb_cons.insets = new Insets(10, 10, 10, 10);
        panel.add(dfs_button, gb_cons);

        gb_cons.gridx = 0;
        gb_cons.gridy = 1;
        gb_cons.insets = new Insets(10, 10, 10, 10);
        panel.add(bfs_button, gb_cons);

        algorithm_frame.getContentPane().add(BorderLayout.CENTER, panel);
        algorithm_frame.setLocationRelativeTo(null);
        algorithm_frame.setSize(500, 200);
        algorithm_frame.setVisible(true);
    }

    private static void path_finder(String algorithm) {
        app_frame = new JFrame("City Path Finder with " + algorithm);
        app_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);

        JLabel start_label = new JLabel("Start City:");
        JLabel start_city_label = new JLabel(start_city);

        JLabel destination_label = new JLabel("Destination City:");
        JLabel destination_city_label = new JLabel(destination_city);

        JTextArea text1 = new JTextArea(1, 30);
        text1.setLineWrap(true);
        text1.setEditable(false);

        JTextArea text2 = new JTextArea(30, 30);
        text2.setLineWrap(true);
        text2.setEditable(false);

        JLabel output = new JLabel("RESULT");

        JButton exit_button = new JButton("EXIT");

        exit_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app_frame.dispose();
            }
        });

        panel.setBackground(Color.BLACK);  
        start_label.setForeground(Color.WHITE);  
        start_city_label.setForeground(Color.WHITE);
        destination_label.setForeground(Color.WHITE);
        destination_city_label.setForeground(Color.WHITE);
        output.setForeground(Color.WHITE);
        text1.setBackground(Color.BLACK);
        text1.setForeground(Color.WHITE);
        text2.setBackground(Color.BLACK);
        text2.setForeground(Color.WHITE);
        exit_button.setBackground(Color.BLACK);
        exit_button.setForeground(Color.WHITE);

        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(start_label, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        panel.add(start_city_label, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(destination_label, constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        panel.add(destination_city_label, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        panel.add(output, constraints);

        JScrollPane result_scroll_pane = new JScrollPane(text1);
        result_scroll_pane.setPreferredSize(new Dimension(300, 50));
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 5;
        constraints.gridheight = 1;
        panel.add(result_scroll_pane, constraints);

        JScrollPane result_scroll_pane2 = new JScrollPane(text2);
        result_scroll_pane2.setPreferredSize(new Dimension(300, 200));
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 5;
        constraints.gridheight = 1;
        panel.add(result_scroll_pane2, constraints);

        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.gridwidth = 2;
        panel.add(exit_button, constraints);

        app_frame.getContentPane().add(BorderLayout.CENTER, panel);
        app_frame.setSize(800, 600);
        app_frame.setLocationRelativeTo(null);
        app_frame.setVisible(true);

        
        String file_path = "src\\Turkish cities.csv";
        Graph cityGraph;
        try {
            cityGraph = new Graph(file_path);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(app_frame, "Error loading city graph data.", "Graph Error", JOptionPane.ERROR_MESSAGE);
            return;  
        }

        if ("DFS".equals(algorithm)) {
            int depthLimit = 4;
            DFS.depthFirstSearch(cityGraph, start_city, destination_city, depthLimit, text1, text2);
        } else if ("BFS".equals(algorithm)) {
            BFS.breadthFirstSearch(cityGraph, start_city, destination_city, text1, text2);
        }
    }

    private static JButton menu_button(String button_text, ActionListener action_listener) {
        JButton button = new JButton(button_text);
        button.addActionListener(action_listener);
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        return button;
    } 
}