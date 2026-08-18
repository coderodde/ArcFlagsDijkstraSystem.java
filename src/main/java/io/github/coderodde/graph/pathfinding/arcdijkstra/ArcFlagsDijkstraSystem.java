package io.github.coderodde.graph.pathfinding.arcdijkstra;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 *
 */
public final class ArcFlagsDijkstraSystem {

    /**
     * The minimum number of regions allowed.
     */
    private static final int MINIMUM_REGIONS = 2;
    
    private final DirectedGraphNodeCoordinatesMap coordinatesMap;
    private final NodeRegionIDMap regionIdMap = new NodeRegionIDMap();
    
    public ArcFlagsDijkstraSystem(
            List<DirectedGraphNode> nodeList,
            DirectedGraphNodeCoordinatesMap coordinateMap,
            int regions) {
        
        Objects.requireNonNull(nodeList, "The input node list is null.");
        
        this.coordinatesMap =
            Objects.requireNonNull(
                coordinateMap, 
                "The input coordinate map is null.");
        
        if (nodeList.isEmpty()) {
            throw new IllegalArgumentException("The input node list is empty.");
        }
        
        checkRegions(regions);
        
        // Explore the entire graph with all the reachable nodes:
        nodeList = explore(nodeList);
    }
    
    public static List<DirectedGraphNode> 
        explore(List<DirectedGraphNode> nodeList) {
        
        Deque<DirectedGraphNode> frontier = 
            new ArrayDeque<>(new HashSet<>(nodeList));
        
        Set<DirectedGraphNode> visited = new HashSet<>();
        
        while (!frontier.isEmpty()) {
            DirectedGraphNode current = frontier.removeFirst();
            visited.add(current);
            
            for (DirectedGraphNode child : current.children()) {
                if (!visited.contains(child)) {
                    frontier.addLast(child);
                }
            }
            
            for (DirectedGraphNode parent : current.parents()) {
                if (!visited.contains(parent)) {
                    frontier.addLast(parent);
                }
            }
        }
        
        return new ArrayList<>(visited);
    }
    
    private static void checkRegions(int regions) {
        if (regions < MINIMUM_REGIONS) {
            throw new IllegalArgumentException(
                String.format(
                    "The input number of regions (%d) is too small. " + 
                        "Must be at least %d.", 
                    regions, 
                    MINIMUM_REGIONS));
        }
    }
}
