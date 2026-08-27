package io.github.coderodde.graph.pathfinding.arcdijkstra;

import java.util.HashMap;
import java.util.Map;

/**
 * This class maps graph nodes to their 2D-coordinates.
 */
public class DirectedGraphNodeCoordinatesMap {
    private final Map<DirectedGraphNode, Coordinates2D> map = new HashMap<>();
    
    public void put(DirectedGraphNode node, Coordinates2D coordinates) {
        map.put(node, coordinates);
    }
    
    public Coordinates2D get(DirectedGraphNode node) {
        return map.get(node);
    }
    
    /**
     * Returns the number of mappings in this map.
     * 
     * @return the number of mappings.
     */
    public int size() {
        return map.size();
    }
}
