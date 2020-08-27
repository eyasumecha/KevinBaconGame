import java.util.*;

/**
 * Library for graph analysis
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2016
 *
 */
public class GraphLib {
    /**
     * Takes a random walk from a vertex, up to a given number of steps
     * So a 0-step path only includes start, while a 1-step path includes start and one of its out-neighbors,
     * and a 2-step path includes start, an out-neighbor, and one of the out-neighbor's out-neighbors
     * Stops earlier if no step can be taken (i.e., reach a vertex with no out-edge)
     * @param g		graph to walk on
     * @param start	initial vertex (assumed to be in graph)
     * @param steps	max number of steps
     * @return		a list of vertices starting with start, each with an edge to the sequentially next in the list;
     * 			    null if start isn't in graph
     */
    public static <V,E> List<V> randomWalk(Graph<V,E> g, V start, int steps) {
        List<V> path = new ArrayList<V>();
        V curr = start;
        path.add(curr);
        for (int i = steps; i > 0; i--) {
            List<V> neighboursList = new ArrayList<V>();
            Iterable<V> neighbours = g.outNeighbors(curr);
            for (V u : neighbours) {
                neighboursList.add(u);
            }
            if (!neighboursList.isEmpty()) {
                 curr = neighboursList.get((int) (Math.random() * (neighboursList.size())));
                path.add(curr);

            }

        }
        return path;
    }

    /**
     * Orders vertices in decreasing order by their in-degree
     * @param g		graph
     * @return		list of vertices sorted by in-degree, decreasing (i.e., largest at index 0)
     */
    public static <V,E> List<V> verticesByInDegree(Graph<V,E> g) {
        List<V> sortedList = new ArrayList<V>();
        for (V u : g.vertices()){
            sortedList.add(u);
        }
        sortedList.sort((V v1, V v2)-> g.inDegree(v2)-g.inDegree(v1));

        return sortedList;
    }

    public static <V,E> AdjacencyMapGraph<V,E> bfs(AdjacencyMapGraph<V,E> g, V source){
        AdjacencyMapGraph<V, E> pathGraph = new AdjacencyMapGraph<>();

        Set<V> visited = new HashSet<V>(); //Set to track which vertices have already been visited
        Queue<V> queue = new LinkedList<V>(); //queue to implement BFS
        queue.add(source); //enqueue start vertex
        visited.add(source);
        pathGraph.insertVertex(source);
        //add start to visited Set
        while (!queue.isEmpty()) { //loop until no more vertices
            V u = queue.remove(); //dequeue
            for (V v : g.outNeighbors(u)) { //loop over out neighbors
                if (!visited.contains(v)) { //if neighbor not visited, then neighbor is discovered from this vertex
                    visited.add(v); //add neighbor to visited Set
                    queue.add(v);
                    pathGraph.insertVertex(v);
                    pathGraph.insertDirected(v, u, g.getLabel(u, v));//enqueue neighbour
                }
            }
        }
        return pathGraph;
    }

    public static <V,E> ArrayList<V> getPath(Graph<V,E> tree, V v){
        Map<V, V> backtrack = new HashMap<>();
        backtrack.put(v,null);
        Stack<V> stack = new Stack<>();
        stack.push(v);
        V u = v;
        while(tree.outDegree(u) > 0){
            u = stack.pop();
            for( V a: tree.outNeighbors(u)){
                stack.push(a);
                backtrack.put(a, u);
            }
        }

        ArrayList<V> path = new ArrayList<>();
        V current = u;
        while(current != null){
            path.add(0, current);
            current = backtrack.get(current);
        }

        return path;
    }

    public static <V,E> Set<V> missingVertices(Graph<V,E> graph, Graph<V,E> subgraph){
        Set<V> missingVertices = new HashSet<>();
        Iterable<V> graphVertices  = graph.vertices();
        for(V i: graphVertices){
            if(!subgraph.hasVertex(i)){
                missingVertices.add(i);
            }

        }
        return missingVertices;
    }

    public static <V,E> double averageSeparation(Graph<V,E> tree, V root){
        return avgSepHelper(tree, root, 0)/(tree.numVertices()-1);
    }

    public static <V,E> double avgSepHelper(Graph<V,E> tree, V root, double length){
        double value = length;
        for( V u: tree.inNeighbors(root)){
            value += avgSepHelper(tree, u, length+1);
        }
        return value;
    }
}
