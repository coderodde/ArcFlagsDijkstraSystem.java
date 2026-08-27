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
 * This class implements the demonstration of the P2P-shortest path algorithms.
 */
public final class Demo {
    
    private static final int NODES = 50000;
    private static final int ARCS = NODES * 3;
    private static final int REGIONS = 100;
    private static final int CROSSING_ARCS = 300;
    private static final Random RANDOM = new Random(13L);
    private static final double GRID_WIDTH_HEIGHT = 10.0;
    private static final double MAXIMUM_NEIGHBOUR_DISTANCE = 0.15;
    private static final double ADD_ARC_FACTOR = 0.2;
    
    public static void main(String[] args) {
        System.out.printf(
            "=== %s demo ===%n", 
            ArcFlagsDijkstraSystem.class.getSimpleName());
        
        long t = System.currentTimeMillis();
        GraphData graphData = createRandomGraph();
        long duration = System.currentTimeMillis() - t;
        
        System.out.printf("Graph data built in %d ms.%n", duration);
        
        DirectedGraphNode source = choose(graphData.nodeList());
        DirectedGraphNode target = choose(graphData.nodeList());
        
        ArcFlagsDijkstraSystem dijkstraSystem = 
            new ArcFlagsDijkstraSystem(
                graphData.nodeList(), 
                graphData.weightFunction(), 
                graphData.coordinatesMap(), 
                REGIONS);
        
        t = System.currentTimeMillis();
        List<DirectedGraphNode> path1 = dijkstraSystem.queryViaDijkstra(source,
                                                                        target);
        
        duration = System.currentTimeMillis() - t;
        
        System.out.printf("Dijkstra in %d ms.%n", duration);
        
        t = System.currentTimeMillis();
        List<DirectedGraphNode> path2 = dijkstraSystem.queryViaAStar(source,
                                                                     target);
        
        duration = System.currentTimeMillis() - t;
        
        System.out.printf("A* in %d ms.%n", duration);
        
        t = System.currentTimeMillis();
        List<DirectedGraphNode> path3 = dijkstraSystem.queryViaArcFlags(source, 
                                                                        target);
        
        duration = System.currentTimeMillis() - t;
        System.out.printf("Dijkstra with arc-flags in %d ms.%n", duration);
        
        System.out.printf("Dijkstra and A* agree: %b.%n",
                          path1.equals(path2) && path2.equals(path3));
        
        
    }
    
    private static GraphData createRandomGraph() {
        List<DirectedGraphNode> nodeList = new ArrayList<>(NODES);
        DirectedGraphNodeCoordinatesMap coordsMap = 
            new DirectedGraphNodeCoordinatesMap();
        
        DirectedGraphWeightFunction weightFunction = 
            new DirectedGraphWeightFunction();
        
        for (int i = 0; i < NODES; ++i) {
            DirectedGraphNode node = new DirectedGraphNode(i);
            Coordinates2D coords   = getRandomCoordinates();
            nodeList.add(node);
            coordsMap.put(node, coords);
        }
        
        NodeRegionIDMap regionMap =
            new KdTreeNodeRegionIDMapBuilder()
                .build(nodeList, coordsMap, REGIONS);
        
        List<List<DirectedGraphNode>> regionLists = 
            new ArrayList<>(regionMap.numberOfRegions());
        
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
            --crossingArcs;
        }
        
        return new GraphData(nodeList, weightFunction, coordsMap);
    }
    
    private static <T> T choose(List<T> lst) {
        return lst.get(RANDOM.nextInt(lst.size()));
    }
    
    private static Coordinates2D getRandomCoordinates() {
        return new Coordinates2D(GRID_WIDTH_HEIGHT * RANDOM.nextDouble(),
                                 GRID_WIDTH_HEIGHT * RANDOM.nextDouble());
    }

    private static int getLocalRegionArcs(int regionSize, int graphSize) {
        return (ARCS * regionSize) / graphSize;
    }
    
    private static final record GraphData(
        List<DirectedGraphNode> nodeList,
        DirectedGraphWeightFunction weightFunction,
        DirectedGraphNodeCoordinatesMap coordinatesMap) {
        
        
    }
}
