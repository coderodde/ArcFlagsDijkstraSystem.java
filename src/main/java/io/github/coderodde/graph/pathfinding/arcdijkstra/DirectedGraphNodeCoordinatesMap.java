package io.github.coderodde.graph.pathfinding.arcdijkstra;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rodio
 */
public class DirectedGraphNodeCoordinatesMap {
    private final Map<DirectedGraphNode, Coordinates2D> map = new HashMap<>();
    
    public void put(DirectedGraphNode node, Coordinates2D coordinates) {
        map.put(node, coordinates);
    }
    
    public Coordinates2D get(DirectedGraphNode node) {
        return map.get(node);
    }
}
