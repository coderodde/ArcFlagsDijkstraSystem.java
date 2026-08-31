package io.github.coderodde.graph.pathfinding.arcdijkstra.demo;

import io.github.coderodde.graph.pathfinding.arcdijkstra.ArcFlagsDijkstraSystem;
import io.github.coderodde.graph.pathfinding.arcdijkstra.Coordinates2D;
import io.github.coderodde.graph.pathfinding.arcdijkstra.DirectedGraphNode;
import io.github.coderodde.graph.pathfinding.arcdijkstra.DirectedGraphNodeCoordinatesMap;
import io.github.coderodde.graph.pathfinding.arcdijkstra.DirectedGraphWeightFunction;
import io.github.coderodde.graph.pathfinding.arcdijkstra.KdTreeNodeRegionIDMapBuilder;
import io.github.coderodde.graph.pathfinding.arcdijkstra.NodeRegionIDMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class implements the demonstration of the P2P-shortest path algorithns.
 */
public final class Demo {
    
    public static final int REGIONS = 50;
    private static final int NODES = 50_000;
    private static final int ARCS = NODES * 3 + NODES / 2; // Road network.
    private static final int CROSSING_ARCS = 200;
    private static final Random RANDOM = new Random(123);
    private static final double GRID_WIDTH_HEIGHT = 10.0;
    private static final double MAXIMUM_NEIGHBOUR_DISTANCE = 0.15;
    private static final double ADD_ARC_FACTOR = 0.2;
    
    public static void main(String[] args) {
        System.out.printf(
            "=== %s demo ===%n", 
            ArcFlagsDijkstraSystem.class.getSimpleName());
        
        long t = System.nanoTime();
        GraphData graphData = createRandomGraph();
        long duration = System.nanoTime() - t;
        
        System.out.printf("Graph data built in %d ns.%n", duration);
        
        DirectedGraphNode source = choose(graphData.nodeList());
        DirectedGraphNode target = choose(graphData.nodeList());
        
        ArcFlagsDijkstraSystem dijkstraSystem = 
            new ArcFlagsDijkstraSystem(
                graphData.nodeList(), 
                graphData.weightFunction(), 
                graphData.coordinatesMap(), 
                graphData.regionIdMap,
                REGIONS,
                true); // Parallel on.
        
        t = System.nanoTime();
        List<DirectedGraphNode> path1 = dijkstraSystem.queryViaDijkstra(source,
                                                                        target);
        
        duration = System.nanoTime() - t;
        
        System.out.printf("Dijkstra in %d ns.%n", duration);
        
        t = System.nanoTime();
        List<DirectedGraphNode> path2 = dijkstraSystem.queryViaAStar(source,
                                                                     target);
        
        duration = System.nanoTime() - t;
        
        System.out.printf("A* in %d ns.%n", duration);
        
        t = System.nanoTime();
        List<DirectedGraphNode> path3 = dijkstraSystem.queryViaArcFlags(source, 
                                                                        target);
        
        duration = System.nanoTime() - t;
        System.out.printf("Dijkstra with arc-flags in %d ns.%n", duration);
        
        boolean pathsEqual = path1.equals(path2) && path1.equals(path3);
        
        if (pathsEqual) {
            System.out.println("Algorithms agreed!");
            
            if (path1.isEmpty()) {
                System.out.println(
                    "The paths are empty. Target not reachable.");
            } else {
                System.out.printf("Path length: %d nodes.%n", path1.size());
            }
        } else {
            System.err.println("Algorithms disagreed!");
        }
    }
    
    public static GraphData createRandomGraph() {
        List<DirectedGraphNode> nodeList = new ArrayList<>(NODES);
        DirectedGraphNodeCoordinatesMap coordsMap = 
            new DirectedGraphNodeCoordinatesMap();
        
        DirectedGraphWeightFunction weightFunction = 
            new DirectedGraphWeightFunction();
        
        long t = System.nanoTime();
        
        for (int i = 0; i < NODES; ++i) {
            DirectedGraphNode node = new DirectedGraphNode(i);
            Coordinates2D coords   = getRandomCoordinates();
            nodeList.add(node);
            coordsMap.put(node, coords);
        }
        
        System.out.printf("[STATUS] Created nodes and coordinates in %d ns.%n",
                          System.nanoTime() - t);
        
        t = System.nanoTime();
        
        NodeRegionIDMap regionMap =
            new KdTreeNodeRegionIDMapBuilder()
                .build(nodeList, coordsMap, REGIONS);
        
        System.out.printf("[STATUS] Created region assignments in %d ns.%n", 
                          System.nanoTime() - t);
        
        List<List<DirectedGraphNode>> regionLists = 
            new ArrayList<>(regionMap.numberOfRegions());
        
        t = System.nanoTime();
        
        // Create internal regions:
        for (int region = 0; region < regionMap.numberOfRegions(); ++region) {
            List<DirectedGraphNode> regionNodeList = 
                new ArrayList<>(regionMap.getRegionNodes(region));
            
            regionLists.addLast(regionNodeList);
            
            int localRegionArcs = getLocalRegionArcs(regionNodeList.size(),
                                                     nodeList.size());
            
            // Create arcs within the regions:
            while (localRegionArcs > 0) {
                DirectedGraphNode tail = choose(regionNodeList);
                DirectedGraphNode head = choose(regionNodeList);
                
                Coordinates2D coordsTail = coordsMap.get(tail);
                Coordinates2D coordsHead = coordsMap.get(head);

                double distance = coordsTail.euclideanDistance(coordsHead);

                if (distance <= MAXIMUM_NEIGHBOUR_DISTANCE) {
                    if (weightFunction.containsArcWeight(tail, head)) {
                        // Omit already present arc:
                        continue;
                    }

                    tail.connectTo(head);

                    double arcWeight = distance * (1.0 + ADD_ARC_FACTOR);

                    weightFunction.put(tail, head, arcWeight);
                    --localRegionArcs;
                }
            }
        }
        
        int crossingArcs = CROSSING_ARCS;
        
        while (crossingArcs > 0) {
            int regionIndexTail = RANDOM.nextInt(regionLists.size());
            int regionIndexHead = RANDOM.nextInt(regionLists.size());
            
            if (regionIndexTail == regionIndexHead) {
                continue;
            }
            
            DirectedGraphNode tail = choose(regionLists.get(regionIndexTail));
            DirectedGraphNode head = choose(regionLists.get(regionIndexHead));
            
            if (tail.equals(head)) {
                continue;
            }
            
            if (weightFunction.containsArcWeight(tail, head)) {
                continue;
            }
            
            Coordinates2D tailCoords = coordsMap.get(tail);
            Coordinates2D headCoords = coordsMap.get(head);
            
            double distance = tailCoords.euclideanDistance(headCoords);
            
            distance *= (1.0 + ADD_ARC_FACTOR);
            
            weightFunction.put(tail, head, distance);
            tail.connectTo(head);
            --crossingArcs;
        }
        
        System.out.printf("[STATUS] Created arcs in %d ns.%n", 
                          System.nanoTime() - t);
        
        return new GraphData(nodeList, 
                             weightFunction,
                             coordsMap, 
                             regionMap);
    }
    
    public static <T> T choose(List<T> lst) {
        return lst.get(RANDOM.nextInt(lst.size()));
    }
    
    private static Coordinates2D getRandomCoordinates() {
        return new Coordinates2D(GRID_WIDTH_HEIGHT * RANDOM.nextDouble(),
                                 GRID_WIDTH_HEIGHT * RANDOM.nextDouble());
    }

    private static int getLocalRegionArcs(int regionSize, int graphSize) {
        return (ARCS * regionSize) / graphSize;
    }
    
    public static final record GraphData(
        List<DirectedGraphNode> nodeList,
        DirectedGraphWeightFunction weightFunction,
        DirectedGraphNodeCoordinatesMap coordinatesMap,
        NodeRegionIDMap regionIdMap) {
        
    }
}
